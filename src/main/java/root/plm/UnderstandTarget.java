package root.plm;

import root.entity.plm.LlmWord;

public class UnderstandTarget {
    final String src;
    int currentCut = 0;

    public UnderstandTarget(String src) {
        this.src = src;
    }

    String getLeft() {
        return src.substring(0, currentCut);
    }
    String getRight() {
        return src.substring(currentCut);
    }
    public Toke getAvailableToke(LlmWord word) {
        final String right = getRight();
        int ignored = 0;
        int wordSpace = 0;
        for (int i = 0; i < word.getWord().length(); i++) {
            char currentWord = word.getWord().charAt(i);
            char currentSrc = right.charAt(i - wordSpace + ignored);
            if(currentSrc == currentWord) continue;
            if(currentWord == ' ') {
                wordSpace++;
                continue;
            }
            if(right.charAt(i) == ' ') {
                ignored++;
                i--;
            } else return null;
        }
        int nextSpace = 0;
        final int consumedLength = word.getWord().length() + ignored;
        while (right.length() > consumedLength + nextSpace && right.charAt(consumedLength + nextSpace) == ' ') nextSpace++;
        return new Toke(word, currentCut, currentCut + consumedLength + nextSpace);
    }
}
