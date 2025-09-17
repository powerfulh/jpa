package root.plm;

import org.springframework.stereotype.Service;
import root.entity.plm.PlmContext;

import java.util.List;

/**
 * 더 빠른 초기 시드 구성을 위한 도구
 */
@Service
public class SmartStartBooster {

    public Toke rightContext(Toke target, Toke left, List<PlmContext> contextList) {
        target.rightContext = contextList.stream().filter(StaticUtil.getContextFinder(left.getN(), target.getN())).mapToInt(item -> left.isRightSpace() ? item.space : item.cnt).sum();
        if(left.getType().equals("무엇") && target.getType().equals("조사")) target.rightContext++;
        if(left.isRightSpace() && target.getType().equals("어미")) target.rightContext--;
        return target;
    }
}
