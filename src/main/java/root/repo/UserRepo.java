package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {
}
