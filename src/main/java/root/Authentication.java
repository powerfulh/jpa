package root;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entity.User;
import root.repo.UserRepo;

@RestController
@RequestMapping("/authentication")
public class Authentication {
    final String ak = "AK";

    final UserRepo userRepo;

    public Authentication(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping
    public boolean authenticate(@RequestBody @Valid User dto, HttpSession session) {
        boolean ok = userRepo.findAll().stream().anyMatch(item -> item.id.equals(dto.id) && item.pw.equals(dto.pw));
        if(ok) session.setAttribute(ak, dto.n);
        return ok;
    }
}
