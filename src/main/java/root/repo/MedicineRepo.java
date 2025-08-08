package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.Medicine;

public interface MedicineRepo extends JpaRepository<Medicine, Integer> {
}
