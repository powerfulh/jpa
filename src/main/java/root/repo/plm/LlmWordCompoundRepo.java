package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.LlmWordCompound;

import java.util.List;

public interface LlmWordCompoundRepo extends JpaRepository<LlmWordCompound, Integer> {
    List<LlmWordCompound> findByRightword(int n);
}
