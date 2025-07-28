import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "root")
// scanBasePackages 를 따라갈 줄 알았더니 안 따라간다
@EntityScan(basePackages = "root")
@EnableJpaRepositories(basePackages = "root")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
