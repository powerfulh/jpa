package root.plm;

import root.entity.plm.LlmWord;
import root.entity.plm.PlmContext;

import java.util.List;
import java.util.function.Predicate;

public class StaticUtil {
    static final int opener = 2903;

    static Predicate<PlmContext> getContextFinder(int lw, int rw) {
        return item -> item.getLeftword() == lw && item.getRightword() == rw;
    }
    static LlmWord selectWord(int n, List<LlmWord> data) {
        return data.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
    }
}
