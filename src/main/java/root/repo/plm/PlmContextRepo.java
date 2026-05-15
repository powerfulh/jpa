package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.PlmContext;

import java.util.Optional;

public interface PlmContextRepo extends JpaRepository<PlmContext, Integer> {
    Optional<PlmContext> findByLeftwordAndRightword(int leftword, int rightword);
}
