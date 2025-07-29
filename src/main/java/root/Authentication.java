package root;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import root.entity.User;
import root.repo.UserRepo;

@RestController
@RequestMapping("/authentication")
public class Authentication extends SessionManager {
    final UserRepo userRepo;

    public Authentication(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping
    public boolean authenticate(@RequestBody @Valid User dto, HttpSession session) {
        User requester = userRepo.findAll().stream().filter(item -> item.id.equals(dto.id) && item.pw.equals(dto.pw)).findFirst().orElse(null);
        if(requester == null) return false;
        session.setAttribute(ak, requester.n);
        return true;
    }
    @GetMapping
    public boolean check(HttpSession session) {
        return session.getAttribute(ak) != null;
    }
}
