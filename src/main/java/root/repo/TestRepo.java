package root.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import root.entity.TestTable;

public interface TestRepo extends JpaRepository<TestTable, Integer> {
}
