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

    public record TaskReq(Integer word, String request, String response, String prePrompt) {}
    public record WordReq(int taskId, String word, String type, String memo) {}
    public record WordUpdateReq(int taskId, int n, String type, String memo) {}
    public record CompoundReq(int taskId, int word, int leftword, int rightword) {}
    public record ContextReq(int taskId, int leftword, int rightword, String kind) {}
    public record CommitReq(int taskId, String which, boolean learnContext) {}
    public record QaReq(int taskId) {}

    @PostMapping("/task")
    public AgentTask createTask(@RequestBody TaskReq req) {
        return core.createTask(req.word(), req.request(), req.response(), req.prePrompt());
    }

    @PostMapping("/word")
    public Map<String, Integer> addWord(@RequestBody WordReq req) {
        return Map.of("n", core.addWord(req.taskId(), req.word(), req.type(), req.memo()));
    }

    @PostMapping("/word/update")
    public void updateWord(@RequestBody WordUpdateReq req) {
        core.updateWord(req.taskId(), req.n(), req.type(), req.memo());
    }

    @PostMapping("/compound")
    public Map<String, Integer> addCompound(@RequestBody CompoundReq req) {
        return Map.of("n", core.addCompound(req.taskId(), req.word(), req.leftword(), req.rightword()));
    }

    @PostMapping("/context")
    public Map<String, Object> adjustContext(@RequestBody ContextReq req) {
        return core.adjustContext(req.taskId(), req.leftword(), req.rightword(), req.kind());
    }

    @PostMapping("/commit")
    public Map<String, Integer> commit(@RequestBody CommitReq req) {
        return Map.of("n", core.commit(req.taskId(), req.which(), req.learnContext()));
    }

    @PostMapping("/qa")
    public void linkQa(@RequestBody QaReq req) {
        core.linkQa(req.taskId());
    }

    @PostMapping("/reload")
    public String reload() {
        return core.reloadModel();
    }
}
