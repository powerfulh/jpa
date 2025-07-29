package root;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import root.entity.PowerfulJpa;
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
    public ResponseEntity<List<PowerfulJpa>> get(HttpSession s) {
        int current = getAk(s);
        return ResponseEntity.ok(repo.findAll().stream().filter(item -> item.owner == current).toList());
    }
    @PostMapping
    public void save(@RequestBody PowerfulJpa dto, HttpSession s) {
        PowerfulJpa target = repo.findByNameAndOwner(dto.name, getAk(s));
        if (target == null) {
            dto.owner = getAk(s);
            repo.save(dto);
        } else {
            target.data = dto.data;
            target.updated_date = dto.updated_date;
            repo.save(target);
        }
    }
}
