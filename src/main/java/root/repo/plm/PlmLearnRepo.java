package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.PlmLearn;

public interface PlmLearnRepo extends JpaRepository<PlmLearn, Integer> {
}
