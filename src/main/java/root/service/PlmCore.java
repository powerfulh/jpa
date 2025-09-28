package root.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import root.entity.plm.*;
import root.exception.PlmException;
import root.plm.*;
import root.plm.entity.Context;
import root.plm.entity.Word;
import root.repo.plm.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlmCore {
    final LlmWordRepo llmWordRepo;
    final PlmLearnRepo plmLearnRepo;
    final LlmWordCompoundRepo llmWordCompoundRepo;
    final PlmSrcBoxRepo plmSrcBoxRepo;
    final ReplaceRepeatedChars replaceRepeatedChars;
    final PlmContextRepo plmContextRepo;
    final UnderstandBoxRepo understandBoxRepo;
    final UnderstandBoxWordRepo understandBoxWordRepo;
    final SmartStartBooster smartStartBooster;
    final PlmUltronSentenceRepo ultronSentenceRepo;
    final PlmUltronContextRepo ultronContextRepo;

    final String symbolType = "기호";
    final String learnedCompoundType = "학습 결합";

    public PlmCore(LlmWordRepo llmWordRepo, PlmLearnRepo plmLearnRepo, LlmWordCompoundRepo llmWordCompoundRepo, PlmSrcBoxRepo plmSrcBoxRepo, ReplaceRepeatedChars replaceRepeatedChars, PlmContextRepo plmContextRepo, UnderstandBoxRepo understandBoxRepo, UnderstandBoxWordRepo understandBoxWordRepo, SmartStartBooster smartStartBooster, PlmUltronSentenceRepo ultronSentenceRepo, PlmUltronContextRepo ultronContextRepo) {
        this.llmWordRepo = llmWordRepo;
        this.plmLearnRepo = plmLearnRepo;
        this.llmWordCompoundRepo = llmWordCompoundRepo;
        this.plmSrcBoxRepo = plmSrcBoxRepo;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.plmContextRepo = plmContextRepo;
        this.understandBoxRepo = understandBoxRepo;
        this.understandBoxWordRepo = understandBoxWordRepo;
        this.smartStartBooster = smartStartBooster;
        this.ultronSentenceRepo = ultronSentenceRepo;
        this.ultronContextRepo = ultronContextRepo;
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
     * @return Token should be compounded
     */
    boolean learnCouple(String leftword) {
        return !llmWordRepo.findAllByWord(leftword).isEmpty();
    }
    void learn(String item, String input) {
        var w = llmWordRepo.findAllByWord(item);
        if(w.isEmpty()) {
            String target = item;
            String cutter = null;
            String type = null;
            boolean compoundMode = false;
            nextCut: for (int ii = 0; ii < item.length() - 1; ii++) {
                if(!compoundMode) type = "학습";
                int cut = item.length() - 1 - ii;
                String current = item.substring(cut);
                w = llmWordRepo.findAllByWord(current);
                if(w.isEmpty()) continue;
                for (var wi: w) {
                    for (var compound: llmWordCompoundRepo.findByRightword(wi.getN())) {
                        String c = llmWordRepo.findById(compound.word).orElseThrow().getWord();
                        if(item.endsWith(c)) {
                            target = item.substring(0, item.length() - c.length());
                            cutter = c;
                            if(learnCouple(target)) {
                                // 이 경우 다음까지도 돌아봐야 하는 경우를 못 봐서 일단 바로 외워버리자
                                learn(target.concat(c), input, cutter, learnedCompoundType);
                                return;
                            }
                            ii += c.length() - wi.getWord().length();
                            continue nextCut;
                        }
                    }
                }
                if(w.size() == 1 && w.get(0).getType().equals(symbolType)) {
                    learn(item.substring(0, cut), input);
                    return;
                }
                if(!compoundMode) {
                    target = item.substring(0, cut);
                    cutter = current;
                }
                if(learnCouple(target)) {
                    target = target.concat(current);
                    type = learnedCompoundType;
                    compoundMode = true;
                    // 학결 캐이스가 존재하면 그때부턴 학결만 본다
                    // 원래 학결이 뜨면 아예 루프 끄고 외워버리다가 더 돌아봐야 하는 단어(기억 안 남)가 발견돼서 돌게 했는데, 이번엔 먼저 뜬 학결이 올바른 '모으다' 가 발견돼서
                    // 학결이 뜨면 더 돌아보는 이유를 학결에 한해본다
                }
            }
            learn(target, input, cutter, type);
        }
    }
    @Transactional
    public void learn(String input) {
        var symbols = llmWordRepo.findByType(symbolType).stream().map(LlmWord::getWord).collect(Collectors.joining()).toCharArray();
        String[] cleanInput = replaceRepeatedChars.replaceRepeatedChars(input, symbols).split(" ");
        for(int i = 0; i < cleanInput.length; i++) {
            String item = cleanInput[i];
            int next = i + 1;
            // '고' 로 끝났다는 것은 '고' 로 시작하는 공백을 포함한 토큰일 가능성이 있다 250905
            if(item.charAt(item.length() - 1) == '고' && next < cleanInput.length) {
                String compoundNext = item.concat(" ").concat(cleanInput[next]);
                boolean spacyToken = llmWordRepo.findByWordStartingWith("고 ").stream()
                        .anyMatch(gi -> compoundNext.contains(gi.getWord()));
                if(spacyToken) {
                    learn(compoundNext, input);
                    ++i;
                    continue;
                }
            }
            learn(item, input);
        }
    }
    public void learnSrcBox() {
        plmSrcBoxRepo.findAll().forEach(item -> learn(item.src));
    }
    void separateToken(List<Toke> understandList, UnderstandTarget src, final List<LlmWord> wordList, Map<String, List<Word>> failHistory, List<Context> contextList, List<Sentence> sentenceList, List<LlmWordCompound> compoundList, SuccessHistory successHistory) {
        if(src.success()) sentenceList.add(new Sentence(understandList, contextList));
        else {
            Toke lastUnderstand = understandList.get(understandList.size() - 1);
            var sh = successHistory.get(src.getRight(), lastUnderstand.getN());
            if(sh != null) {
                sh.forEach(item -> {
                    var merge = new ArrayList<>(understandList);
                    merge.addAll(item);
                    sentenceList.add(new Sentence(merge, contextList));
                });
                return;
            }
            var h = failHistory.get(src.getRight());
            var sameList = wordList.stream()
                    .map(item -> {
                        Toke toke = src.getAvailableToke(item);
                        if(toke == null || understandList.isEmpty()) return toke;
                        try {
                            smartStartBooster.rightContext(toke, lastUnderstand, toke, contextList, compoundList, wordList, lastUnderstand.isRightSpace(), lastUnderstand.otherOption);
                        } catch (PlmException e) {
                            return null;
                        }
                        return smartStartBooster.lengthRate(toke);
                    })
                    .filter(item -> {
                        if(item != null) {
                            if(h == null) return true;
                            return h.stream().noneMatch(hi -> Objects.equals(hi.getN(), item.getN()));
                        }
                        return false;
                    })
                    .sorted(Comparator.comparing(Toke::getRightContext))
                    .toList();
            if(sameList.isEmpty()) {
                if(understandList.size() < 2) throw failHistory.isEmpty() ? new PlmException("Fail to continue after open", lastUnderstand.getWord()) : new PlmException("Fail to understand", failHistory);
                src.rollback(lastUnderstand);
                failHistory.computeIfAbsent(src.getRight(), k -> new ArrayList<>());
                failHistory.get(src.getRight()).add(lastUnderstand);
                understandList.remove(understandList.size() - 1);
                separateToken(understandList, src, wordList, failHistory, contextList, sentenceList, compoundList, successHistory);
                return;
            }
            final Toke best = sameList.get(sameList.size() - 1);
            int ss = sentenceList.size();
            if(sameList.size() > 1) {
                sameList.subList(0, sameList.size() - 1).stream()
                        .filter(item -> item.getRightContext() > 0)
                        .forEach(item -> {
                            var clone = new ArrayList<>(understandList);
                            separateToken(clone, src.clone().pushToke(clone, item), wordList, failHistory, contextList, sentenceList, compoundList, successHistory);
                        });
                if(best.getRightContext() < 1) best.otherOption = true;
            }
            final String right = src.getRight();
            final int understandSize = understandList.size();
            separateToken(understandList, src.pushToke(understandList, best), wordList, failHistory, contextList, sentenceList, compoundList, successHistory);
            var branchList = sentenceList.subList(ss, sentenceList.size());
            if(!branchList.isEmpty()) {
                successHistory.put(right, lastUnderstand.getN(), branchList.stream().map(item -> item.subList(understandSize, item.size())).toList());
            }
        }
    }
    public List<Sentence> understand(String pureSrc) {
        var symbols = llmWordRepo.findByType(symbolType).stream().map(LlmWord::getWord).collect(Collectors.joining()).toCharArray();
        final String src = replaceRepeatedChars.replaceRepeatedChars(pureSrc.replaceAll("\\s", ""), symbols);
        var wordList = llmWordRepo.findByTypeNot("opener");
        final UnderstandTarget understandTarget = new UnderstandTarget(replaceRepeatedChars.replaceRepeatedChars(pureSrc, symbols));
        var openerList = wordList.stream().map(understandTarget::getAvailableToke).filter(Objects::nonNull).toList();
        if (openerList.isEmpty()) throw new PlmException("Fail to set the opening word", src);
        PlmException e = null;
        Map<String, List<Word>> failHistory = new HashMap<>();
        List<Sentence> sentenceList = new ArrayList<>();
        List<Context> contextList = plmContextRepo.findAll().stream().map(item -> (Context) item).toList();
        var compoundList = llmWordCompoundRepo.findAll();
        SuccessHistory successHistory = new SuccessHistory();
        for (var opener: openerList) {
            List<Toke> understandList = new ArrayList<>();
            try {
                separateToken(understandList, understandTarget.pushToke(understandList, opener), wordList, failHistory, contextList, sentenceList, compoundList, successHistory);
            } catch (PlmException plmException) {
                e = plmException;
            }
        }
        if(sentenceList.isEmpty() && e != null) throw e; // 싹 다 실패한 경우 나중에는 편집 거리로 리트해봐야겠지
        sentenceList.sort(Comparator.comparing(item -> item.getContextPoint() * -1));
        return sentenceList.size() > 9 ? sentenceList.subList(0, 9) : sentenceList;
    }

    void learnSentence(Sentence sentence) {
        var contextList = plmContextRepo.findAll();
        for (int i = 0; i < sentence.size() - 1; i++) {
            var context = sentence.getContext(i, i + 1, contextList);
            if(context == null) {
                context = new PlmContext();
                context.leftword = sentence.get(i).getN();
                context.rightword = sentence.get(i + 1).getN();
                if(sentence.get(i).isRightSpace()) context.space++;
                else context.cnt++;
                plmContextRepo.save(context);
                contextList.add(context);
            } else if(sentence.get(i).isRightSpace()) context.space++;
            else context.cnt++;
        }
    }
    @Transactional
    public void understandThenLearn(String pureSrc) {
        learnSentence(understand(pureSrc).get(0));
    }

    void beforeUnderstandBox() {
        understandBoxWordRepo.deleteAll();
        understandBoxRepo.deleteByActivate(false);
        understandBoxRepo.flush();
    }
    void boxSentence(Sentence sentence, PlmUnderstandBox box) {
        for (int i = 0; i < sentence.size(); i++) {
            var word = new PlmUnderstandBoxWord();
            word.understand = box.getN();
            word.i = i;
            word.word = sentence.get(i).getN();
            understandBoxWordRepo.save(word);
        }
    }
    @Transactional
    public void understandBox() {
        beforeUnderstandBox();
        var boxList = understandBoxRepo.findAll();
        boxList.forEach(box -> boxSentence(understand(box.src).get(0), box));
        List<String> list = boxList.stream().map(item -> item.src).toList();
        plmSrcBoxRepo.findAll().stream()
                .filter(item -> !list.contains(item.src))
                .forEach(item -> {
            var box = new PlmUnderstandBox();
            box.src = item.src;
            box.activate = true;
            understandBoxRepo.save(box);
            boxSentence(understand(item.src).get(0), box);
        });
    }

    @Transactional
    public void reunderstand() {
        beforeUnderstandBox();
        understandBoxRepo.findAll().stream()
                .filter(box -> box.src.length() < 110)
                .forEach(box -> boxSentence(understand(box.src).get(0), box));
    }

    void commitSentence(Sentence sentence, int usn) {
        var contextList = plmContextRepo.findAll();
        for (int i = 0; i < sentence.size() - 1; i++) {
            var context = new PlmUltronContext();
            context.sentence = usn;
            context.context = Optional.of(sentence.getContext(i, i + 1, contextList)).orElseThrow(() -> new PlmException("No context", sentence.stream().map(Toke::getWord).collect(Collectors.joining()))).getN();
            context.i = i;
            ultronContextRepo.save(context);
        }
    }
    @Transactional
    public void understandThenCommit(String pureSrc, boolean learnContext) {
        Sentence sentence = understand(pureSrc).get(0);
        if(learnContext) learnSentence(sentence);
        commitSentence(sentence, ultronSentenceRepo.save(new PlmUltronSentence(sentence.get(0).getN())).getN());
    }
}

class LearnWord extends LlmWord {
    void set(String lw, String t) {
        word = lw;
        type = t;
    }
}