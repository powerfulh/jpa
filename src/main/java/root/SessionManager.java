package root;

import jakarta.servlet.http.HttpSession;
import root.exception.NoAuthenticationKey;

public class SessionManager {
    final String ak = "AK";

    int getAk(HttpSession s) {
        try {
            return (int) s.getAttribute(ak);
        } catch (NullPointerException e) {
            throw new NoAuthenticationKey();
        }
    }
}
