package root.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Dsl {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager manager) {
        return new JPAQueryFactory(manager);
    }
}
