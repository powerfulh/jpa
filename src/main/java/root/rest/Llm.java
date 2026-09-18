package root.rest;

import org.springframework.web.bind.annotation.*;
import root.entity.agent.AgentTask;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;
import root.entity.plm.PlmUltronSentence;
import root.entity.plm.PlmUnderstandBox;
import root.plm.Sentence;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmContextRepo;
import root.repo.plm.dsl.Repo;
import root.service.AgentCore;
import root.service.PlmCore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/llm")
public class Llm {
    final LlmWordRepo llmWordRepo;
    final PlmContextRepo contextRepo;
    final PlmCore plmCore;
    final AgentCore agentCore;
    final Repo dsl;
    final Map<Integer, String> commitedSentence;

    public Llm(LlmWordRepo llmWordRepo, PlmContextRepo contextRepo, PlmCore plmCore, AgentCore agentCore, Repo dsl) {
        this.llmWordRepo = llmWordRepo;
        this.contextRepo = contextRepo;
        this.plmCore = plmCore;
        this.agentCore = agentCore;
        this.dsl = dsl;
        commitedSentence = new HashMap<>();
    }

    @GetMapping("/{w}")
    public List<LlmWord> getWord(@PathVariable String w) {
        return llmWordRepo.findAllByWord(w);
    }
    @PostMapping("/learn")
    public void learn(@RequestBody String src) {
        plmCore.learn(src);
    }
    @PostMapping("/learnbox")
    public void learnBox() {
        plmCore.learnSrcBox();
    }
    @GetMapping("/understand")
    public List<Map<String, Object>> understand(String src) {
        return plmCore.understand(src).stream().map(Sentence::getDto).toList();
    }
    @PostMapping("/learn/context")
    public void learnContext(String src) {
        plmCore.understandThenLearn(src);
    }
    @PostMapping("/understand/box")
    public void understandBox() {
        plmCore.understandBox();
    }
    @PostMapping("/reunderstand/box")
    public void reunderstandBox() {
        plmCore.reunderstand();
    }
    @PostMapping("/commit")
    public int commit(String src, boolean learnContext) {
        final int sn = plmCore.understandThenCommit(src, learnContext);
        commitedSentence.put(sn, src);
        return sn;
    }
    @GetMapping("/unreadable")
    public List<PlmUnderstandBox> getUnreadable() {
        return dsl.selectUnreadable();
    }
    @GetMapping("/commited-sentence")
    public Map<Integer, String> getCommitedSentence() {
        return commitedSentence;
    }
    @PostMapping("/qa/{sn}/{target}")
    public PlmUltronSentence postQa(@PathVariable int sn, @PathVariable int target) {
        commitedSentence.remove(sn);
        commitedSentence.remove(target);
        return plmCore.qa(sn, target);
    }
    @GetMapping("/context")
    public List<PlmContext> getContext() {
        return contextRepo.findByOrderByUpdatedDateDesc();
    }

    @GetMapping("/agent/task")
    public List<AgentTask> listAgentTasks() {
        return agentCore.listTasks();
    }
    @GetMapping("/agent/task/{n}")
    public Map<String, Object> agentTaskDetail(@PathVariable int n) {
        Map<String, Object> r = new HashMap<>();
        r.put("task", agentCore.listTasks().stream().filter(t -> t.n == n).findFirst().orElse(null));
        r.put("changes", agentCore.listChanges(n));
        return r;
    }
    @PostMapping("/agent/task/{n}/confirm")
    public void confirmAgentTask(@PathVariable int n) {
        agentCore.confirm(n);
    }
    @PostMapping("/agent/task/{n}/rollback")
    public void rollbackAgentTask(@PathVariable int n) {
        agentCore.rollback(n);
    }
}
