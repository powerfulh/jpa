package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.PowerfulJpa;

public interface ApiRepo extends JpaRepository<PowerfulJpa, Integer> {
    PowerfulJpa findByNameAndOwner(String name, int owner);
}
