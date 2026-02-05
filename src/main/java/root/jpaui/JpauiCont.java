package root.jpaui;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jpaui")
public class JpauiCont {
    final EntityManager entityManager;
    final Map<String, JpaRepository<?, Integer>> repositoryMap;

    public JpauiCont(EntityManager entityManager, List<JpaRepository<?, Integer>> repos) {
        this.entityManager = entityManager;
        repositoryMap = new HashMap<>();
        for (var item: entityManager.getMetamodel().getEntities()) {
            for(var r: repos) {
                Class<?> e = item.getJavaType();
                Class<?> re = ResolvableType.forClass(r.getClass().getInterfaces()[0]).as(Repository.class).getGeneric(0).resolve();
                if(e == re) repositoryMap.put(e.getSimpleName(), r);
            }
        }
    }

    @GetMapping("/meta")
    public List<String> getMeta() {
        return entityManager.getMetamodel().getEntities().stream().map(EntityType::getName).toList();
    }
    @GetMapping("/{table}")
    public List<?> getList(@PathVariable String table) {
        return repositoryMap.get(table).findAll();
    }
}
