package root.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entity.LlmWord;
import root.repo.LlmWordRepo;

import java.util.List;

@RestController
@RequestMapping("/llm")
public class Llm {
    final LlmWordRepo llmWordRepo;

    public Llm(LlmWordRepo llmWordRepo) {
        this.llmWordRepo = llmWordRepo;
    }

    @GetMapping("/{w}")
    public List<LlmWord> getWord(@PathVariable String w) {
        return llmWordRepo.findAllByWord(w);
    }
}
