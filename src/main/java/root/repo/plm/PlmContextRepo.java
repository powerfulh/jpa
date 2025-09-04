package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.PlmContext;

public interface PlmContextRepo extends JpaRepository<PlmContext, Integer> {
}
