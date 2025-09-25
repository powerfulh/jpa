package root.plm;

import root.entity.plm.PlmContext;
import root.entity.plm.PlmUnderstandBox;
import root.entity.plm.PlmUnderstandBoxWord;
import root.repo.plm.PlmContextRepo;
import root.repo.plm.UnderstandBoxWordRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sentence extends ArrayList<Toke> {
    final int contextPoint;

    public Sentence(List<Toke> list, List<PlmContext> contextList) {
        super(list);
        var openerContext = contextList.stream().filter(StaticUtil.getContextFinder(StaticUtil.opener, get(0).getN())).findAny().orElse(null);
        int p = openerContext == null ? 0 : (openerContext.cnt * get(0).getWord().length());
        contextPoint = p + list.stream().mapToInt(Toke::getRightContext).sum();
    }

    PlmContext getContext(int li, int ri, List<PlmContext> list) {
        return list.stream().filter(StaticUtil.getContextFinder(get(li).getN(), get(ri).getN())).findAny().orElse(null);
    }
    public void learnContext(PlmContextRepo plmContextRepo) {
        var contextList = plmContextRepo.findAll();
        for (int i = 0; i < size() - 1; i++) {
            var context = getContext(i, i + 1, contextList);
            if(context == null) {
                context = new PlmContext();
                context.leftword = get(i).getN();
                context.rightword = get(i + 1).getN();
                if(get(i).rightSpace) context.space++;
                else context.cnt++;
                plmContextRepo.save(context);
                contextList.add(context);
            } else if(get(i).rightSpace) context.space++;
            else context.cnt++;
        }
    }
    public int getContextPoint() {
        return contextPoint;
    }
    public Map<String, Object> getDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("point", contextPoint);
        dto.put("list", this);
        return dto;
    }
    public void box(PlmUnderstandBox box, UnderstandBoxWordRepo understandBoxWordRepo) {
        for (int i = 0; i < size(); i++) {
            var word = new PlmUnderstandBoxWord();
            word.understand = box.getN();
            word.i = i;
            word.word = get(i).getN();
            understandBoxWordRepo.save(word);
        }
    }
}
