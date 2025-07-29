package root;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entity.PowerfulApi;
import root.repo.ApiRepo;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController extends SessionManager {
    final ApiRepo repo;

    public ApiController(ApiRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<PowerfulApi>> get(HttpSession s) {
        int current = getAk(s);
        return ResponseEntity.ok(repo.findAll().stream().filter(item -> item.owner == current).toList());
    }
}
