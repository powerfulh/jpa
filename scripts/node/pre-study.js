// 학습 사전 학습 도구: 기존 학습된 문장 표본을 분해해 타입·메모 직관 다지기
// 사용: node scripts/node/pre-study.js [count]
//   count: 표본 개수 (default 40, 권장 30~50)
//
// 데이터 소스: jpaui (PlmUltronSentence + PlmUltronContext + PlmContext + LlmWord)
//   - PlmUltronSentence 의 최신 n 내림차순으로 채택
//   - 각 sentence 의 opener + PlmUltronContext(i 순) 를 따라가며 텍스트 재구성
//   - 공백 여부는 PlmContext.space vs cnt 비율로 추정 (space > cnt 면 공백)
//
// 출력:
//   1. 각 sentence 의 재구성 텍스트 + 분해 1등 단어별 (n type memo)
//   2. 타입 빈도 히스토그램
//   3. 메모 표기 패턴 샘플

const LEARN = 'http://localhost:8079';

async function get(url) {
  const r = await fetch(url);
  const text = await r.text();
  try { return JSON.parse(text); } catch { return text; }
}

const understand = src => get(`${LEARN}/llm/understand?src=${encodeURIComponent(src)}`);

function buildIndexes(uContexts, contexts, words) {
  const wordById = new Map(words.map(w => [w.n, w]));
  const ctxById = new Map(contexts.map(c => [c.n, c]));
  const uCtxBySentence = new Map();   // sentence -> sorted PlmUltronContext list
  for (const uc of uContexts) {
    if (!uCtxBySentence.has(uc.sentence)) uCtxBySentence.set(uc.sentence, []);
    uCtxBySentence.get(uc.sentence).push(uc);
  }
  for (const arr of uCtxBySentence.values()) arr.sort((a, b) => a.i - b.i);
  return { wordById, ctxById, uCtxBySentence };
}

function reconstruct(sentence, idx) {
  const { wordById, ctxById, uCtxBySentence } = idx;
  const openerW = wordById.get(sentence.opener);
  if (!openerW) return null;
  let text = openerW.word;
  const ucs = uCtxBySentence.get(sentence.n) ?? [];
  for (const uc of ucs) {
    const c = ctxById.get(uc.context);
    if (!c) return null;
    const w = wordById.get(c.rightword);
    if (!w) return null;
    const space = c.space > c.cnt;        // 공백 추정 휴리스틱
    text += (space ? ' ' : '') + w.word;
  }
  return text;
}

async function decomposeTop(src) {
  const r = await understand(src);
  if (!Array.isArray(r) || r.length === 0) return null;
  return r[0];
}

const formatCand = c =>
  c.list.map(w => `${w.word}(${w.n} ${w.type}/${w.memo ?? 'null'})`).join(' + ');

async function main() {
  const count = +process.argv[2] || 40;
  console.error('fetching caches...');
  const [sentences, uContexts, contexts, words] = await Promise.all([
    get(`${LEARN}/jpaui/PlmUltronSentence`),
    get(`${LEARN}/jpaui/PlmUltronContext`),
    get(`${LEARN}/jpaui/PlmContext`),
    get(`${LEARN}/jpaui/LlmWord`)
  ]);
  if (![sentences, uContexts, contexts, words].every(Array.isArray)) {
    console.error('fetch fail'); process.exit(1);
  }
  console.error(`sentences=${sentences.length}, uContexts=${uContexts.length}, contexts=${contexts.length}, words=${words.length}`);
  const idx = buildIndexes(uContexts, contexts, words);

  sentences.sort((a, b) => b.n - a.n);   // 최신 우선

  const typeHist = {};
  const memoPatterns = { eq: new Set(), eng: new Set(), suffix: new Set(), other: new Set() };
  const lines = [];

  let picked = 0;
  for (const s of sentences) {
    if (picked >= count) break;
    const text = reconstruct(s, idx);
    if (!text) continue;
    const top = await decomposeTop(text);
    if (!top) continue;
    picked++;
    lines.push(`\n=== sentence ${s.n} (opener=${s.opener}, target=${s.target ?? '-'}) ===`);
    lines.push(`  text: ${text}   [${top.point}]`);
    lines.push(`    ${formatCand(top)}`);
    for (const w of top.list) {
      typeHist[w.type] = (typeHist[w.type] ?? 0) + 1;
      const m = w.memo;
      if (!m) continue;
      if (m.startsWith('=')) memoPatterns.eq.add(`${w.word}(${w.n}): ${m}`);
      else if (/접미|접두/.test(m)) memoPatterns.suffix.add(`${w.word}(${w.n}): ${m}`);
      else if (/^[A-Za-z][A-Za-z ,()/.]*$/.test(m)) memoPatterns.eng.add(`${w.word}(${w.n}): ${m}`);
      else memoPatterns.other.add(`${w.word}(${w.n}): ${m}`);
    }
  }

  console.log(lines.join('\n'));
  console.log(`\n채택 ${picked} / 요청 ${count}`);

  console.log('\n=== 타입 빈도 ===');
  for (const [t, c] of Object.entries(typeHist).sort((a, b) => b[1] - a[1])) {
    console.log(`  ${t}: ${c}`);
  }
  console.log('\n=== 메모 표기 패턴 샘플 ===');
  for (const [k, set] of Object.entries(memoPatterns)) {
    if (set.size === 0) continue;
    console.log(`\n[${k}]`);
    [...set].slice(0, 15).forEach(s => console.log(`  ${s}`));
  }
}

main().catch(e => { console.error(e); process.exit(1); });
