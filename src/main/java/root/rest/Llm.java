package root.rest;

import org.springframework.web.bind.annotation.*;
import root.entity.plm.LlmWord;
import root.plm.MindMap;
import root.plm.MindMapper;
import root.plm.Sentence;
import root.repo.plm.LlmWordRepo;
import root.service.PlmCore;

import java.util.List;

@RestController
@RequestMapping("/llm")
public class Llm {
    final LlmWordRepo llmWordRepo;
    final PlmCore plmCore;
    final MindMapper mindMapper;

    public Llm(LlmWordRepo llmWordRepo, PlmCore plmCore, MindMapper mindMapper) {
        this.llmWordRepo = llmWordRepo;
        this.plmCore = plmCore;
        this.mindMapper = mindMapper;
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
    @GetMapping("/mindmap/{n}")
    public MindMap getMindMap(@PathVariable int n) {
        return mindMapper.map(n);
    }
    @GetMapping("/understand")
    public Sentence understand(String src) {
        return plmCore.understand(src);
    }
    @PostMapping("/learn/context")
    public void learnContext(String src) {
        plmCore.understandThenLearn(src);
    }
}
