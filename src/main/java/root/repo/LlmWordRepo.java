package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.LlmWord;

import java.util.List;

public interface LlmWordRepo extends JpaRepository<LlmWord, Integer> {
    List<LlmWord> findAllByWord(String word);
}
