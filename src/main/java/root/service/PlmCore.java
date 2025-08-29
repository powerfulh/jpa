package root.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmLearn;
import root.repo.plm.LlmWordCompoundRepo;
import root.repo.plm.LlmWordRepo;
import root.repo.plm.PlmLearnRepo;

@Service
public class PlmCore {
    final LlmWordRepo llmWordRepo;
    final PlmLearnRepo plmLearnRepo;
    final LlmWordCompoundRepo llmWordCompoundRepo;

    public PlmCore(LlmWordRepo llmWordRepo, PlmLearnRepo plmLearnRepo, LlmWordCompoundRepo llmWordCompoundRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmLearnRepo = plmLearnRepo;
        this.llmWordCompoundRepo = llmWordCompoundRepo;
    }

    void learn(String w, String src, String rightword, String type) {
        var nw = new LearnWord();
        nw.set(w, type);
        var l = new PlmLearn();
        l.word = llmWordRepo.save(LlmWord.to(nw)).getN();
        l.src = src;
        l.rightword = rightword;
        l.value = w;
        plmLearnRepo.save(l);
    }
    /**
     * @return Token has been solved?
     */
    boolean learnCouple(String leftword, String rightword, String src) {
        if(!llmWordRepo.findAllByWord(leftword).isEmpty()) {
            learn(leftword.concat(rightword), src, null, "학습 결합");
            return true;
        }
        return false;
    }
    @Transactional
    public void learn(String input) {
        token: for(var item: input.split(" ")) {
            var w = llmWordRepo.findAllByWord(item);
            if(w.isEmpty()) {
                String target = item;
                String cutter = null;
                nextCut: for (int ii = 0; ii < item.length(); ii++) {
                    int cut = item.length() - 1 - ii;
                    String current = item.substring(cut);
                    w = llmWordRepo.findAllByWord(current);
                    if(w.isEmpty()) continue;
                    for (var wi: w) {
                        for (var rw: llmWordCompoundRepo.findByRightword(wi.getN())) {
                            var c = llmWordRepo.findById(rw.word).orElseThrow().getWord();
                            if(item.endsWith(c)) {
                                target = item.substring(0, item.length() - c.length());
                                if(learnCouple(target, c, input)) continue token;
                                ii += c.length() - wi.getWord().length();
                                continue nextCut;
                            }
                        }
                    }
                    target = item.substring(0, cut);
                    if(learnCouple(target, current, input)) continue token;
                    cutter = current;
                }
                learn(target, input, cutter, "학습");
            }
        }
    }
}

class LearnWord extends LlmWord {
    void set(String lw, String t) {
        word = lw;
        type = t;
    }
}