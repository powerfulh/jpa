package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.LlmWord;

import java.util.List;

public interface LlmWordRepo extends JpaRepository<LlmWord, Integer> {
    List<LlmWord> findAllByWord(String word);
    List<LlmWord> findByType(String type);
    List<LlmWord> findByWordStartingWith(String s);
    List<LlmWord> findByTypeNot(String type);
}
