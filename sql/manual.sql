SELECT w.*
FROM llm_word w
WHERE w.n <> 2903
  AND NOT EXISTS (
    SELECT 1
    FROM plm_context c
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE c.leftword = w.n
)
  AND NOT EXISTS (
    SELECT 1
    FROM plm_context c
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE c.rightword = w.n
)
  AND NOT EXISTS (
    SELECT 1
    FROM llm_word_compound wc
             JOIN plm_context c ON c.leftword = wc.word
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE wc.leftword = w.n
)
  AND NOT EXISTS (
    SELECT 1
    FROM llm_word_compound wc
             JOIN plm_context c ON c.leftword = wc.word
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE wc.rightword = w.n
)
  AND NOT EXISTS (
    SELECT 1
    FROM llm_word_compound wc
             JOIN plm_context c ON c.rightword = wc.word
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE wc.leftword = w.n
)
  AND NOT EXISTS (
    SELECT 1
    FROM llm_word_compound wc
             JOIN plm_context c ON c.rightword = wc.word
             JOIN plm_ultron_context uc ON uc.context = c.n
    WHERE wc.rightword = w.n
)