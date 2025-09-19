package root.plm;

import org.springframework.stereotype.Component;
import root.entity.plm.LlmWordCompound;
import root.entity.plm.PlmContext;

import java.util.List;

/**
 * 더 빠른 초기 시드 구성을 위한 도구
 */
@Component
public class SmartStartBooster {

    int contextPoint(List<PlmContext> contextList, Toke left, int right) {
        return contextList.stream().filter(StaticUtil.getContextFinder(left.getN(), right)).mapToInt(item -> left.isRightSpace() ? item.space : item.cnt).sum();
    }
    public Toke rightContext(Toke target, Toke left, List<PlmContext> contextList, List<LlmWordCompound> compoundList) {
        target.rightContext = contextPoint(contextList, left, target.getN());
//        if(target.getType().equals("결합"))
        target.rightContext *= left.getWord().length() + target.getWord().length();
        if(left.getType().equals("0") && target.getType().equals("조사") && !target.getN().equals(191)) target.rightContext--;
        if(left.isRightSpace() && target.getType().equals("어미")) target.rightContext--;
        if(!left.isRightSpace() && (left.getType().equals("무엇") || left.getType().equals("대명사")) && target.getType().equals("조사")) target.rightContext++;
        if(!left.isRightSpace() && !left.getN().equals(StaticUtil.opener) && target.getType().equals("감탄사")) target.rightContext--;
        return target;
    }
}
