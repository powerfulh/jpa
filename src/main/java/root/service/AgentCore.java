package root.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import root.entity.agent.AgentChange;
import root.entity.agent.AgentTask;
import root.entity.plm.LlmWord;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;
import root.entity.plm.PlmUltronContext;
import root.entity.plm.PlmUltronSentence;
import root.plm.PlmException;
import root.plm.Sentence;
import root.repo.agent.AgentChangeRepo;
import root.repo.agent.AgentTaskRepo;
import root.repo.plm.LlmWordCompoundRepo;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmContextRepo;
import root.repo.plm.PlmUltronContextRepo;
import root.repo.plm.PlmUltronSentenceRepo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentCore {
    final AgentTaskRepo taskRepo;
    final AgentChangeRepo changeRepo;
    final LlmWordRepo llmWordRepo;
    final LlmWordCompoundRepo compoundRepo;
    final PlmContextRepo contextRepo;
    final PlmUltronSentenceRepo sentenceRepo;
    final PlmUltronContextRepo ultronContextRepo;
    final PlmCore plmCore;
    final JdbcTemplate jdbc;

    final RestClient model = RestClient.create();
    final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);
    final File logFile = new File("agent-log/rollback.json");
    final DateTimeFormatter logTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AgentCore(AgentTaskRepo taskRepo, AgentChangeRepo changeRepo, LlmWordRepo llmWordRepo,
                     LlmWordCompoundRepo compoundRepo, PlmContextRepo contextRepo,
                     PlmUltronSentenceRepo sentenceRepo, PlmUltronContextRepo ultronContextRepo,
                     PlmCore plmCore, JdbcTemplate jdbc) {
        this.taskRepo = taskRepo;
        this.changeRepo = changeRepo;
        this.llmWordRepo = llmWordRepo;
        this.compoundRepo = compoundRepo;
        this.contextRepo = contextRepo;
        this.sentenceRepo = sentenceRepo;
        this.ultronContextRepo = ultronContextRepo;
        this.plmCore = plmCore;
        this.jdbc = jdbc;
    }

    void requireTask(int taskId) {
        if (!taskRepo.existsById(taskId)) throw new PlmException("No agent task", String.valueOf(taskId));
    }

    public AgentTask createTask(String word, String request, String response) {
        AgentTask t = new AgentTask();
        t.word = word;
        t.request = request;
        t.response = response;
        return taskRepo.save(t);
    }

    @Transactional
    public Integer addWord(int taskId, String word, String type, String memo) {
        requireTask(taskId);
        AgentLearnWord w = new AgentLearnWord();
        w.set(word, type, memo);
        LlmWord saved = llmWordRepo.save(LlmWord.toWithMemo(w));
        changeRepo.save(new AgentChange(taskId, AgentChange.WORD, saved.getN(), null));
        return saved.getN();
    }

    @Transactional
    public void updateWord(int taskId, int n, String type, String memo) {
        requireTask(taskId);
        LlmWord w = llmWordRepo.findById(n)
                .orElseThrow(() -> new PlmException("No word", String.valueOf(n)));
        if (type != null && !type.equals(w.getType())) {
            AgentChange c = new AgentChange(taskId, AgentChange.WORD_TYPE, n, null);
            c.prevString = w.getType();
            changeRepo.save(c);
            w.setType(type);
        }
        if (memo != null && !memo.equals(w.getMemo())) {
            AgentChange c = new AgentChange(taskId, AgentChange.WORD_MEMO, n, null);
            c.prevString = w.getMemo();
            changeRepo.save(c);
            w.setMemo(memo);
        }
    }

    @Transactional
    public Integer addCompound(int taskId, int word, int leftword, int rightword) {
        requireTask(taskId);
        LlmWordCompound saved = compoundRepo.save(new LlmWordCompound(word, leftword, rightword));
        changeRepo.save(new AgentChange(taskId, AgentChange.COMPOUND, saved.getN(), null));
        return saved.getN();
    }

    @Transactional
    public Integer adjustContext(int taskId, int leftword, int rightword, String kind) {
        requireTask(taskId);
        if (!"cnt".equals(kind) && !"space".equals(kind))
            throw new PlmException("Invalid kind (cnt|space)", kind);

        PlmContext existing = contextRepo.findByLeftwordAndRightword(leftword, rightword).orElse(null);
        AgentChange c;
        if (existing == null) {
            PlmContext nc = new PlmContext();
            nc.leftword = leftword;
            nc.rightword = rightword;
            if ("cnt".equals(kind)) nc.cnt = 1;
            else nc.space = 1;
            PlmContext saved = contextRepo.save(nc);
            c = new AgentChange(taskId,
                    "cnt".equals(kind) ? AgentChange.CONTEXT_NEW_CNT : AgentChange.CONTEXT_NEW_SPACE,
                    saved.getN(), null);
            c.viaCommit = false;
            changeRepo.save(c);
            return saved.getN();
        } else {
            if ("cnt".equals(kind)) existing.cnt++;
            else existing.space++;
            c = new AgentChange(taskId,
                    "cnt".equals(kind) ? AgentChange.CONTEXT_CNT : AgentChange.CONTEXT_SPACE,
                    existing.getN(), null);
            c.viaCommit = false;
            changeRepo.save(c);
            return existing.getN();
        }
    }

    @Transactional
    public Integer commit(int taskId, String src, boolean learnContext) {
        requireTask(taskId);
        Sentence sentence = plmCore.understand(src).get(0);

        if (learnContext) {
            var contextList = contextRepo.findAll();
            for (int i = 0; i < sentence.size() - 1; i++) {
                PlmContext ctx = sentence.getContext(i, i + 1, contextList);
                boolean space = sentence.get(i).isRightSpace();
                AgentChange c;
                if (ctx == null) {
                    ctx = new PlmContext();
                    ctx.leftword = sentence.get(i).getN();
                    ctx.rightword = sentence.get(i + 1).getN();
                    if (space) ctx.space = 1;
                    else ctx.cnt = 1;
                    ctx = contextRepo.save(ctx);
                    c = new AgentChange(taskId,
                            space ? AgentChange.CONTEXT_NEW_SPACE : AgentChange.CONTEXT_NEW_CNT,
                            ctx.getN(), null);
                    contextList.add(ctx);
                } else {
                    if (space) ctx.space++;
                    else ctx.cnt++;
                    c = new AgentChange(taskId,
                            space ? AgentChange.CONTEXT_SPACE : AgentChange.CONTEXT_CNT,
                            ctx.getN(), null);
                }
                c.viaCommit = true;
                changeRepo.save(c);
            }
        }

        PlmUltronSentence us = sentenceRepo.save(new PlmUltronSentence(sentence.get(0).getN()));
        changeRepo.save(new AgentChange(taskId, AgentChange.ULTRON_SENTENCE, us.getN(), null));

        var contextList = contextRepo.findAll();
        for (int i = 0; i < sentence.size() - 1; i++) {
            PlmContext ctx = sentence.getContext(i, i + 1, contextList);
            if (ctx == null) {
                String pair = sentence.get(i).getWord() + (sentence.get(i).isRightSpace() ? " " : "") + sentence.get(i + 1).getWord();
                throw new PlmException("No context", pair);
            }
            PlmUltronContext uc = new PlmUltronContext();
            uc.sentence = us.getN();
            uc.context = ctx.getN();
            uc.i = i * 2;
            uc = ultronContextRepo.save(uc);
            changeRepo.save(new AgentChange(taskId, AgentChange.ULTRON_CONTEXT, uc.getN(), null));
        }

        return us.getN();
    }

    @Transactional
    public void linkQa(int taskId, int requestSentenceN, int responseSentenceN) {
        requireTask(taskId);
        PlmUltronSentence response = sentenceRepo.findById(responseSentenceN)
                .orElseThrow(() -> new PlmException("No response sentence", String.valueOf(responseSentenceN)));
        if (!sentenceRepo.existsById(requestSentenceN))
            throw new PlmException("No request sentence", String.valueOf(requestSentenceN));
        Integer prev = response.target;
        response.target = requestSentenceN;
        changeRepo.save(new AgentChange(taskId, AgentChange.QA, responseSentenceN, prev));
    }

    public List<AgentTask> listTasks() {
        return taskRepo.findAll();
    }

    public List<AgentChange> listChanges(int taskId) {
        return changeRepo.findByTaskOrderByNAsc(taskId);
    }

    @Transactional
    public void confirm(int taskId) {
        requireTask(taskId);
        changeRepo.deleteByTask(taskId);
        taskRepo.deleteById(taskId);
    }

    /** SELECT → DELETE → 로그 append. 우리 소유 아닌 외부 테이블 전용. */
    void cleanupExternal(int taskId, AgentChange change, String table, String whereSql, Object... args) {
        String select = "SELECT * FROM " + table + " WHERE " + whereSql;
        List<Map<String, Object>> rows = jdbc.queryForList(select, args);
        if (rows.isEmpty()) return;
        jdbc.update("DELETE FROM " + table + " WHERE " + whereSql, args);

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("at", LocalDateTime.now().format(logTimeFmt));
        entry.put("task", taskId);
        entry.put("agentChangeN", change.n);
        entry.put("op", change.op);
        entry.put("entityN", change.entityN);
        entry.put("table", table);
        entry.put("rows", rows);
        appendLog(entry);
    }

    void appendLog(Map<String, Object> entry) {
        try {
            File dir = logFile.getParentFile();
            if (dir != null && !dir.exists() && !dir.mkdirs())
                throw new PlmException("Failed to create log dir", dir.getPath());
            List<Object> log;
            if (logFile.exists() && logFile.length() > 0) {
                log = json.readValue(logFile, json.getTypeFactory().constructCollectionType(List.class, Object.class));
            } else {
                log = new ArrayList<>();
            }
            log.add(entry);
            json.writeValue(logFile, log);
        } catch (IOException e) {
            throw new PlmException("Failed to write rollback log", e.getMessage());
        }
    }

    @Transactional
    public void rollback(int taskId) {
        requireTask(taskId);
        for (AgentChange c : changeRepo.findByTaskOrderByNDesc(taskId)) {
            switch (c.op) {
                case AgentChange.WORD -> {
                    cleanupExternal(taskId, c, "ultron_parameter", "word = ?", c.entityN);
                    llmWordRepo.deleteById(c.entityN);
                }
                case AgentChange.COMPOUND -> compoundRepo.deleteById(c.entityN);
                case AgentChange.CONTEXT_NEW_CNT, AgentChange.CONTEXT_NEW_SPACE -> {
                    cleanupExternal(taskId, c, "plm_ultron_closer", "context = ?", c.entityN);
                    cleanupExternal(taskId, c, "plm_ultron_triplet", "`lead` = ? OR context = ?", c.entityN, c.entityN);
                    cleanupExternal(taskId, c, "plm_ultron_experienced_opener", "context = ?", c.entityN);
                    contextRepo.deleteById(c.entityN);
                }
                case AgentChange.CONTEXT_CNT -> contextRepo.findById(c.entityN)
                        .orElseThrow(() -> new PlmException("Missing context for rollback", String.valueOf(c.entityN)))
                        .cnt--;
                case AgentChange.CONTEXT_SPACE -> contextRepo.findById(c.entityN)
                        .orElseThrow(() -> new PlmException("Missing context for rollback", String.valueOf(c.entityN)))
                        .space--;
                case AgentChange.WORD_TYPE -> llmWordRepo.findById(c.entityN)
                        .orElseThrow(() -> new PlmException("Missing word for type rollback", String.valueOf(c.entityN)))
                        .setType(c.prevString);
                case AgentChange.WORD_MEMO -> llmWordRepo.findById(c.entityN)
                        .orElseThrow(() -> new PlmException("Missing word for memo rollback", String.valueOf(c.entityN)))
                        .setMemo(c.prevString);
                case AgentChange.ULTRON_SENTENCE -> sentenceRepo.deleteById(c.entityN);
                case AgentChange.ULTRON_CONTEXT -> ultronContextRepo.deleteById(c.entityN);
                case AgentChange.QA -> sentenceRepo.findById(c.entityN)
                        .orElseThrow(() -> new PlmException("Missing sentence for QA rollback", String.valueOf(c.entityN)))
                        .target = c.prev;
                default -> throw new PlmException("Unknown op", c.op);
            }
        }
        changeRepo.deleteByTask(taskId);
        taskRepo.deleteById(taskId);
    }

    public String reloadModel() {
        return model.post().uri("http://localhost:8081/local/fetch").retrieve().body(String.class);
    }
}

class AgentLearnWord extends LlmWord {
    void set(String w, String t, String m) {
        word = w;
        type = t;
        memo = m;
    }
}
