package root.plm;

import org.springframework.stereotype.Service;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmContextRepo;

@Service
public class MindMapper {
    final LlmWordRepo llmWordRepo;
    final PlmContextRepo plmContextRepo;

    public MindMapper(LlmWordRepo llmWordRepo, PlmContextRepo plmContextRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmContextRepo = plmContextRepo;
    }

    public MindMap map(int n) {
        MindMap root = new MindMap(n, plmContextRepo.findAll(), llmWordRepo.findAll());

        return root;
    }
}
