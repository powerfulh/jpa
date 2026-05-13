package root.rest;

import org.springframework.web.bind.annotation.*;
import root.entity.agent.AgentTask;
import root.service.AgentCore;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentCont {
    final AgentCore core;

    public AgentCont(AgentCore core) {
        this.core = core;
    }

    public record TaskReq(String word, String request, String response) {}
    public record WordReq(int taskId, String word, String type) {}
    public record CompoundReq(int taskId, int word, int leftword, int rightword) {}
    public record ContextReq(int taskId, int leftword, int rightword, String kind) {}
    public record CommitReq(int taskId, String src, boolean learnContext) {}
    public record QaReq(int taskId, int requestSentenceN, int responseSentenceN) {}

    @PostMapping("/task")
    public AgentTask createTask(@RequestBody TaskReq req) {
        return core.createTask(req.word(), req.request(), req.response());
    }

    @PostMapping("/word")
    public Map<String, Integer> addWord(@RequestBody WordReq req) {
        return Map.of("n", core.addWord(req.taskId(), req.word(), req.type()));
    }

    @PostMapping("/compound")
    public Map<String, Integer> addCompound(@RequestBody CompoundReq req) {
        return Map.of("n", core.addCompound(req.taskId(), req.word(), req.leftword(), req.rightword()));
    }

    @PostMapping("/context")
    public Map<String, Integer> adjustContext(@RequestBody ContextReq req) {
        return Map.of("n", core.adjustContext(req.taskId(), req.leftword(), req.rightword(), req.kind()));
    }

    @PostMapping("/commit")
    public Map<String, Integer> commit(@RequestBody CommitReq req) {
        return Map.of("n", core.commit(req.taskId(), req.src(), req.learnContext()));
    }

    @PostMapping("/qa")
    public void linkQa(@RequestBody QaReq req) {
        core.linkQa(req.taskId(), req.requestSentenceN(), req.responseSentenceN());
    }

    @PostMapping("/reload")
    public String reload() {
        return core.reloadModel();
    }
}
