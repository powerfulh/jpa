package root.rest;

import org.springframework.web.bind.annotation.*;
import root.entity.plm.LlmWord;
import root.repo.plm.LlmWordRepo;
import root.service.PlmCore;

import java.util.List;

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
    @PostMapping("learn")
    public void learn(@RequestBody String src) {
        plmCore.learn(src);
    }
}
