package root.rest;

import org.springframework.web.bind.annotation.*;
import root.entity.plm.LlmWord;
import root.plm.Sentence;
import root.repo.plm.LlmWordRepo;
import root.service.PlmCore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/llm")
public class Llm {
    final LlmWordRepo llmWordRepo;
    final PlmCore plmCore;

    public Llm(LlmWordRepo llmWordRepo, PlmCore plmCore) {
        this.llmWordRepo = llmWordRepo;
        this.plmCore = plmCore;
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
    public void commit(String src, boolean learnContext) {
        plmCore.understandThenCommit(src, learnContext);
    }
}
