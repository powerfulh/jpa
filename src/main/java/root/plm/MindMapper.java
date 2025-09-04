package root.plm;

import org.springframework.stereotype.Service;
import root.repo.plm.LlmWordCompoundRepo;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmContextRepo;

@Service
public class MindMapper {
    final LlmWordRepo llmWordRepo;
    final PlmContextRepo plmContextRepo;
    final LlmWordCompoundRepo llmWordCompoundRepo;

    public MindMapper(LlmWordRepo llmWordRepo, PlmContextRepo plmContextRepo, LlmWordCompoundRepo llmWordCompoundRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmContextRepo = plmContextRepo;
        this.llmWordCompoundRepo = llmWordCompoundRepo;
    }

    public MindMap map(int n) {
        return new MindMap(n, plmContextRepo.findAll(), llmWordRepo.findAll(), llmWordCompoundRepo.findAll(), null);
    }
}
