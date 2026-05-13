package root.service;

import jakarta.transaction.Transactional;
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

import java.util.List;

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

    final RestClient model = RestClient.create();

    public AgentCore(AgentTaskRepo taskRepo, AgentChangeRepo changeRepo, LlmWordRepo llmWordRepo,
                     LlmWordCompoundRepo compoundRepo, PlmContextRepo contextRepo,
                     PlmUltronSentenceRepo sentenceRepo, PlmUltronContextRepo ultronContextRepo, PlmCore plmCore) {
        this.taskRepo = taskRepo;
        this.changeRepo = changeRepo;
        this.llmWordRepo = llmWordRepo;
        this.compoundRepo = compoundRepo;
        this.contextRepo = contextRepo;
        this.sentenceRepo = sentenceRepo;
        this.ultronContextRepo = ultronContextRepo;
        this.plmCore = plmCore;
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
    public Integer addWord(int taskId, String word, String type) {
        requireTask(taskId);
        AgentLearnWord w = new AgentLearnWord();
        w.set(word, type);
        LlmWord saved = llmWordRepo.save(LlmWord.to(w));
        changeRepo.save(new AgentChange(taskId, AgentChange.WORD, saved.getN(), null));
        return saved.getN();
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
        if (existing == null) {
            PlmContext c = new PlmContext();
            c.leftword = leftword;
            c.rightword = rightword;
            if ("cnt".equals(kind)) c.cnt = 1;
            else c.space = 1;
            PlmContext saved = contextRepo.save(c);
            changeRepo.save(new AgentChange(taskId, AgentChange.CONTEXT, saved.getN(), null));
            return saved.getN();
        } else {
            if ("cnt".equals(kind)) existing.cnt++;
            else existing.space++;
            contextRepo.save(existing);
            changeRepo.save(new AgentChange(taskId,
                    "cnt".equals(kind) ? AgentChange.CONTEXT_CNT : AgentChange.CONTEXT_SPACE,
                    existing.getN(), null));
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
                if (ctx == null) {
                    ctx = new PlmContext();
                    ctx.leftword = sentence.get(i).getN();
                    ctx.rightword = sentence.get(i + 1).getN();
                    if (space) ctx.space = 1;
                    else ctx.cnt = 1;
                    ctx = contextRepo.save(ctx);
                    changeRepo.save(new AgentChange(taskId, AgentChange.CONTEXT, ctx.getN(), null));
                    contextList.add(ctx);
                } else {
                    if (space) ctx.space++;
                    else ctx.cnt++;
                    changeRepo.save(new AgentChange(taskId,
                            space ? AgentChange.CONTEXT_SPACE : AgentChange.CONTEXT_CNT,
                            ctx.getN(), null));
                }
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
        sentenceRepo.save(response);
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

    @Transactional
    public void rollback(int taskId) {
        requireTask(taskId);
        for (AgentChange c : changeRepo.findByTaskOrderByNDesc(taskId)) {
            switch (c.op) {
                case AgentChange.WORD -> llmWordRepo.deleteById(c.entityN);
                case AgentChange.COMPOUND -> compoundRepo.deleteById(c.entityN);
                case AgentChange.CONTEXT -> contextRepo.deleteById(c.entityN);
                case AgentChange.CONTEXT_CNT -> {
                    PlmContext ctx = contextRepo.findById(c.entityN)
                            .orElseThrow(() -> new PlmException("Missing context for rollback", String.valueOf(c.entityN)));
                    ctx.cnt--;
                    contextRepo.save(ctx);
                }
                case AgentChange.CONTEXT_SPACE -> {
                    PlmContext ctx = contextRepo.findById(c.entityN)
                            .orElseThrow(() -> new PlmException("Missing context for rollback", String.valueOf(c.entityN)));
                    ctx.space--;
                    contextRepo.save(ctx);
                }
                case AgentChange.ULTRON_SENTENCE -> sentenceRepo.deleteById(c.entityN);
                case AgentChange.ULTRON_CONTEXT -> ultronContextRepo.deleteById(c.entityN);
                case AgentChange.QA -> {
                    PlmUltronSentence us = sentenceRepo.findById(c.entityN)
                            .orElseThrow(() -> new PlmException("Missing sentence for QA rollback", String.valueOf(c.entityN)));
                    us.target = c.prev;
                    sentenceRepo.save(us);
                }
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
    void set(String w, String t) {
        word = w;
        type = t;
    }
}
