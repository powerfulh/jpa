package root.repo.plm.dsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import root.entity.plm.QLlmWord;
import root.entity.plm.QPlmContext;

import java.util.List;

@Repository
public class Repo {
    final JPAQueryFactory factory;

    final QLlmWord w = QLlmWord.llmWord;
    final QPlmContext c = QPlmContext.plmContext;

    public Repo(JPAQueryFactory factory) {
        this.factory = factory;
    }

    public List<Integer> selectSuffix() {
        return factory.select(w.n).from(w).innerJoin(c).on(w.n.eq(c.rightword).and(c.cnt.gt(0)))
                .where(w.type.eq("무엇")).where(w.word.length().eq(1))
                .groupBy(w.n).having(w.n.count().gt(2)).fetch();
    }
}
