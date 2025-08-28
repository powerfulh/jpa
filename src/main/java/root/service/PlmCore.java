package root.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import root.entity.plm.LlmWord;
import root.entity.plm.PlmLearn;
import root.repo.LlmWordRepo;
import root.repo.PlmLearnRepo;

@Service
public class PlmCore {
    final LlmWordRepo llmWordRepo;
    final PlmLearnRepo plmLearnRepo;

    public PlmCore(LlmWordRepo llmWordRepo, PlmLearnRepo plmLearnRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmLearnRepo = plmLearnRepo;
    }

    void learn(String w, String src) {
        if(llmWordRepo.findAllByWord(w).isEmpty()) {
            var nw = new LearnWord();
            nw.set(w);
            var l = new PlmLearn();
            l.word = llmWordRepo.save(LlmWord.to(nw)).getN();
            l.src = src;
            plmLearnRepo.save(l);
        }
    }
    @Transactional
    public void learn(String input) {
        token: for(var item: input.split(" ")) {
            var w = llmWordRepo.findAllByWord(item);
            if(w.isEmpty()) {
                for (int ii = 0; ii < item.length(); ii++) {
                    var cut = item.length() - 1 - ii;
                    var current = item.substring(cut);
                    var frontWord = item.substring(0, cut);
                    w = llmWordRepo.findAllByWord(current);
                    if(w.isEmpty()) continue;
                    learn(frontWord, input);
                    continue token;
                }
                learn(item, input);
            }
        }
    }
}

class LearnWord extends LlmWord {
    void set(String lw) {
        word = lw;
        type = "학습";
    }
}