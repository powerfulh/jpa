package root;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entity.User;
import root.repo.TestRepo;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestC {
    final TestRepo repo;

    public TestC(TestRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<User> test() {
        return repo.findAll();
    }
}
