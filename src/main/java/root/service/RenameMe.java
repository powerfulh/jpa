package root.service;

import org.springframework.stereotype.Service;

@Service
public class RenameMe {
    public int levenshteinDistance(String source, String target) {
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
