package root.plm;

import root.entity.plm.PlmContext;

import java.util.function.Predicate;

public class StaticUtil {
    static final int opener = 2903;

    static Predicate<PlmContext> getContextFinder(int lw, int rw) {
        return item -> item.getLeftword() == lw && item.getRightword() == rw;
    }
}
