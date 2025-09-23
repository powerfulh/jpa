package root.repo.plm;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.plm.PlmUnderstandBox;

public interface UnderstandBoxRepo extends JpaRepository<PlmUnderstandBox, Integer> {
    void deleteByActivate(boolean activate);
}
