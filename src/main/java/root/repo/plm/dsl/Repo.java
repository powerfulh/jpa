package root.repo.plm.dsl;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import root.entity.plm.*;

import java.util.List;

@Repository
public class Repo {
    final JPAQueryFactory factory;

    final QLlmWord w = QLlmWord.llmWord;
    final QPlmContext c = QPlmContext.plmContext;
    final QPlmUnderstandBox ub =  QPlmUnderstandBox.plmUnderstandBox;
    final QPlmUnderstandBoxWord bw = QPlmUnderstandBoxWord.plmUnderstandBoxWord;
	final QPlmUltronSentence s = QPlmUltronSentence.plmUltronSentence;
	final QPlmUltronContext uc = QPlmUltronContext.plmUltronContext;

    public Repo(JPAQueryFactory factory) {
        this.factory = factory;
    }

    public List<Integer> selectSuffix() {
        return factory.select(w.n).from(w)
				.innerJoin(c).on(
						w.n.eq(c.rightword).and(c.cnt.gt(0))
								.and(JPAExpressions.selectFrom(s)
										.innerJoin(uc).on(uc.sentence.eq(s.n))
										.where(s.target.isNotNull()).where(uc.context.eq(c.n)).exists())
				)
                .where(w.type.eq("무엇")).where(w.word.length().eq(1))
                .groupBy(w.n).having(w.n.count().gt(2)).fetch();
    }

    public List<PlmUnderstandBox> selectUnreadable() {
        return factory.selectFrom(ub).where(JPAExpressions.selectFrom(bw).where(ub.n.eq(bw.understand)).notExists())
                .orderBy(ub.n.asc()).fetch();
    }
}
