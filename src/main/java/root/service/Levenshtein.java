package root.service;

import org.springframework.stereotype.Service;

/**
 * @author Chat gpt
 */
@Service
public class Levenshtein {
    String decomposeHangul(String input) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch >= 0xAC00 && ch <= 0xD7A3) {  // 한글 완성형 영역
                int base = ch - 0xAC00;

                int initial = base / (21 * 28);      // 초성
                int medial = (base % (21 * 28)) / 28; // 중성
                int finalConsonant = base % 28;       // 종성

                char[] initials = {
                        'ᄀ','ᄁ','ᄂ','ᄃ','ᄄ','ᄅ','ᄆ','ᄇ','ᄈ','ᄉ','ᄊ','ᄋ','ᄌ','ᄍ','ᄎ','ᄏ','ᄐ','ᄑ','ᄒ'
                };
                char[] medials = {
                        'ᅡ','ᅢ','ᅣ','ᅤ','ᅥ','ᅦ','ᅧ','ᅨ','ᅩ','ᅪ','ᅫ','ᅬ','ᅭ','ᅮ','ᅯ','ᅰ','ᅱ','ᅲ','ᅳ','ᅴ','ᅵ'
                };
                char[] finals = {
                        '\0','ᆨ','ᆩ','ᆪ','ᆫ','ᆬ','ᆭ','ᆮ','ᆯ','ᆰ','ᆱ','ᆲ','ᆳ','ᆴ','ᆵ','ᆶ','ᆷ','ᆸ','ᆹ','ᆺ','ᆻ','ᆼ','ᆽ','ᆾ','ᆿ','ᇀ','ᇁ','ᇂ'
                };

                sb.append(initials[initial]);
                sb.append(medials[medial]);
                if (finalConsonant != 0) {
                    sb.append(finals[finalConsonant]);
                }
            } else {
                sb.append(ch); // 한글 아닌 글자는 그대로
            }
        }

        return sb.toString();
    }
    public int levenshteinDistance(String source, String target) {
        source = decomposeHangul(source);
        target = decomposeHangul(target);
        int sourceLen = source.length();
        int targetLen = target.length();

        // dp 배열 생성 (sourceLen+1) x (targetLen+1)
        int[][] dp = new int[sourceLen + 1][targetLen + 1];

        // 초기값 설정: 빈 문자열에서 각 위치까지의 거리
        for (int i = 0; i <= sourceLen; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= targetLen; j++) {
            dp[0][j] = j;
        }

        // 편집 거리 계산
        for (int i = 1; i <= sourceLen; i++) {
            for (int j = 1; j <= targetLen; j++) {
                if (source.charAt(i - 1) == target.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];  // 문자가 같으면 변경 없음
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],    // 삭제
                            Math.min(
                                    dp[i][j - 1],    // 삽입
                                    dp[i - 1][j - 1] // 치환
                            )
                    );
                }
            }
        }
        return dp[sourceLen][targetLen];
    }
}
