package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.PowerfulApi;

public interface ApiRepo extends JpaRepository<PowerfulApi, Integer> {
}
