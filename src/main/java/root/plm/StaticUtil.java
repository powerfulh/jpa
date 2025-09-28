package root.plm;

import root.plm.entity.Ntity;
import root.plm.entity.Twoken;

import java.util.List;
import java.util.function.Predicate;

public class StaticUtil {
    static final int opener = 2903;

    static Predicate<Twoken> getContextFinder(int lw, int rw) {
        return item -> item.getLeftword() == lw && item.getRightword() == rw;
    }
    static <T extends Ntity>T selectWord(int n, List<T> data) {
        return data.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
    }
}
