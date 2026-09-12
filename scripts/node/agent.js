// 학습 서버(8079) / 모델 서버(8081) 호출 도구
// 사용: node scripts/node/agent.js <command> <args...>
//   understand <text>                          - 분해 1건
//   understand-batch <text1> <text2> ...        - 분해 다건 (JSON으로 출력)
//   understand-top <text1> <text2> ...           - 분해 다건, 각 src의 상위 3개 후보를 한 줄 요약
//   understand-topN <n> <text1> <text2> ...      - 분해 다건, 각 src의 상위 N개 후보를 한 줄 요약
//   word <word>                                - 단어 조회 (JSON)
//   word-summary <word>                        - 단어 동음이의어 요약 (n type memo 한 줄씩)
//   word-n <n1> <n2> ...                       - n 으로 단어 조회 (n word type memo 한 줄씩)
//   task <wordN> <request> <response>          - task 생성 (사전 프롬프트 자동 호출 → prePrompt 첨부, 서버 필수)
//   add-word <taskId> <word> <type> [memo]     - 신규 단어 등록
//   update-word <taskId> <n> <type> [memo]     - 단어 type/memo 변경
//   add-context <taskId> <left> <right> <kind> [src] - 콘텍스트 추가 (kind: cnt|space). src 주면 점수 대신 순위 변동 여부만 표시
//   add-compound <taskId> <wordN> <left> <right>- 컴파운드 추가 (word/left/right 모두 n; 결합 단어 등록 시 필수)
//   commit <taskId> <request|response>         - 스마트 커밋 (서버가 false 실패 시 true 자동 재시도)
//   qa <taskId>                                - Q-A 연결 (sentenceN 는 commit 시 task 에 자동 저장)
//   re-prompt <taskId>                          - task 의 요청 문장으로 재프롬프트 후 top 응답을 task.rePrompt 에 저장
//   reload                                     - 모델 서버 리로드
//   prompt <src>                               - 모델 프롬프트
//   learn <wordN> <request> <response>         - 전체 파이프라인 (task→commit×2→qa→reload→재프롬프트)

const LEARN = 'http://localhost:8079';
const MODEL = 'http://localhost:8081';

async function get(url) {
  const r = await fetch(url);
  const text = await r.text();
  try { return { ok: r.ok, status: r.status, body: JSON.parse(text) }; }
  catch { return { ok: r.ok, status: r.status, body: text }; }
}
async function post(url, body) {
  const init = { method: 'POST', headers: { 'Content-Type': 'application/json; charset=utf-8' } };
  if (body !== undefined) init.body = JSON.stringify(body);
  const r = await fetch(url, init);
  const text = await r.text();
  try { return { ok: r.ok, status: r.status, body: JSON.parse(text) }; }
  catch { return { ok: r.ok, status: r.status, body: text }; }
}

const understand = src => get(`${LEARN}/llm/understand?src=${encodeURIComponent(src)}`);
const lookupWord = w => get(`${LEARN}/llm/${encodeURIComponent(w)}`);
async function lookupCompound(wordN) {
  const r = (await get(`${LEARN}/jpaui/LlmWordCompound`)).body;
  if (!Array.isArray(r)) return { error: 'jpaui fail', body: r };
  const hit = r.find(x => x.word === wordN);
  return hit || { notFound: wordN };
}
async function createTask(word, request, response) {
  // task 생성 직전 자동으로 사전 프롬프트 호출 → prePrompt 텍스트 task에 첨부 (서버 필수)
  const p = await prompt(request);
  const out0 = p.ok && p.body && Array.isArray(p.body.out) && p.body.out[0];
  const prePromptText = out0 ? out0.export : JSON.stringify(p.body ?? p);
  return post(`${LEARN}/agent/task`, { word, request, response, prePrompt: prePromptText });
}
const addWord = (taskId, word, type, memo) => post(`${LEARN}/agent/word`, { taskId, word, type, memo: memo || null });
const updateWord = (taskId, n, type, memo) => post(`${LEARN}/agent/word/update`, { taskId, n, type, memo: memo || null });
const addContext = (taskId, leftword, rightword, kind) => post(`${LEARN}/agent/context`, { taskId, leftword, rightword, kind });
// 후보 시그니처 (단어 n 시퀀스) — 순위 비교용
const candSig = c => c.list.map(w => w.n).join(',');
// 조정 후 점수 대신 '순위 변동 여부'만 표시. src 주면 조정 전/후 분해 순서를 비교 (의도 분해가 [0]에 오면 조정 중단 신호)
async function addContextChecked(taskId, leftword, rightword, kind, src) {
  if (!src) return addContext(taskId, leftword, rightword, kind);
  const beforeBody = (await understand(src)).body;
  const beforeOrder = Array.isArray(beforeBody) ? beforeBody.map(candSig) : null;
  const r = await addContext(taskId, leftword, rightword, kind);
  const afterBody = (await understand(src)).body;
  if (!Array.isArray(afterBody)) return { applied: r.body, understandFail: afterBody };
  const afterOrder = afterBody.map(candSig);
  const topChanged = !beforeOrder || beforeOrder[0] !== afterOrder[0];
  const anyChanged = !beforeOrder || beforeOrder.join('|') !== afterOrder.join('|');
  const status = topChanged ? '순위 변동: [0] 변경됨'
    : anyChanged ? '순위 변동: 하위 순위만 변동 ([0] 동일)'
    : '순위 변동: 없음';
  const lines = [`적용: ${leftword}→${rightword} (${kind}) [${r.body && r.body.applied}]`, status, '--- 분해 (점수 미표시) ---'];
  afterBody.slice(0, 5).forEach((c, i) => {
    lines.push(`  [${i}] ${c.list.map(w => `${w.word}(${w.n} ${w.type}/${w.memo})`).join(' + ')}`);
  });
  return lines.join('\n');
}
const addCompound = (taskId, word, leftword, rightword) => post(`${LEARN}/agent/compound`, { taskId, word, leftword, rightword });
async function commit(taskId, which) {
  const r = await post(`${LEARN}/agent/smart-commit`, { taskId, which });
  return { learnContext: r.body?.learnContext, ...r };
}
// API 변경 (2026-05-29): qa 가 taskId 만 받음. commit 시 task 에 sentenceN 자동 저장.
const linkQa = (taskId) => post(`${LEARN}/agent/qa`, { taskId });
// 재프롬프트 (10단계): task 요청 문장으로 모델 재프롬프트 → top 응답을 task.rePrompt 에 저장 후 프롬프트 결과 반환
async function rePrompt(taskId) {
  const detail = await get(`${LEARN}/llm/agent/task/${taskId}`);
  const task = detail.body && detail.body.task;
  if (!task) return { error: 'no task', detail };
  const p = await prompt(task.request);
  const out0 = p.ok && p.body && Array.isArray(p.body.out) && p.body.out[0];
  if (!out0) return { error: 'prompt fail', p };
  const saved = await post(`${LEARN}/agent/reprompt`, { taskId, rePrompt: out0.export });
  return { saved: saved.ok, rePrompt: out0.export, prompt: p.body };
}
const reloadModel = () => post(`${LEARN}/agent/reload`);
const prompt = src => get(`${MODEL}/plm?pureSrc=${encodeURIComponent(src)}&export=true`);

async function understandBatch(srcs) {
  const out = {};
  for (const s of srcs) out[s] = (await understand(s)).body;
  return out;
}

// n 들로 단어 조회 (n word type memo 한 줄씩)
async function wordByN(ns) {
  const r = (await get(`${LEARN}/jpaui/LlmWord`)).body;
  if (!Array.isArray(r)) return `FAIL ${JSON.stringify(r)}`;
  return ns.map(n => {
    const w = r.find(x => x.n === +n);
    return w ? `${w.n}\t${w.word}\t${w.type}\t${w.memo ?? ''}` : `${n}\t(미등록)`;
  }).join('\n');
}

// 단어 동음이의어를 한 줄씩 요약 (n type memo)
async function wordSummary(word) {
  const r = (await lookupWord(word)).body;
  if (!Array.isArray(r)) return `FAIL ${JSON.stringify(r)}`;
  if (r.length === 0) return `(미등록) ${word}`;
  return r.map(w => `${w.n}\t${w.type}\t${w.memo ?? ''}`).join('\n');
}

// 각 src의 상위 N개 후보를 한 줄로 요약 (텍스트, JSON 아님)
async function understandTop(srcs, topN = 3) {
  const lines = [];
  for (const s of srcs) {
    const r = (await understand(s)).body;
    if (!Array.isArray(r)) { lines.push(`=== ${s} === FAIL ${JSON.stringify(r)}`); continue; }
    lines.push(`=== ${s} ===`);
    for (const c of r.slice(0, topN)) {
      const ws = c.list.map(w => `${w.word}(${w.n} ${w.type}/${w.memo})`).join(' + ');
      lines.push(`  [${c.point}] ${ws}`);
    }
  }
  return lines.join('\n');
}

async function learn(word, request, response) {
  const t = await createTask(word, request, response);
  if (!t.ok) return { error: 'task failed', t };
  const taskId = t.body.n;
  const req = await commit(taskId, 'request');
  if (!req.ok) return { taskId, error: 'request commit failed', req };
  const resp = await commit(taskId, 'response');
  if (!resp.ok) return { taskId, error: 'response commit failed', resp };
  const reqN = req.body.n;
  const respN = resp.body.n;
  const qaR = await linkQa(taskId);
  await reloadModel();
  const p = await prompt(request);
  return {
    taskId,
    requestN: reqN,
    responseN: respN,
    requestLearnContext: req.learnContext,
    responseLearnContext: resp.learnContext,
    qaOk: qaR.ok,
    rePrompt: p.body
  };
}

module.exports = {
  understand, understandBatch, understandTop, lookupWord, wordSummary, createTask, addWord, updateWord,
  addContext, addCompound, commit, linkQa, rePrompt, reloadModel, prompt, learn
};

if (require.main === module) {
  const [cmd, ...args] = process.argv.slice(2);
  const cmds = {
    'understand':       () => understand(args[0]),
    'understand-batch': () => understandBatch(args),
    'understand-top':   () => understandTop(args),
    'understand-topN':  () => understandTop(args.slice(1), +args[0]),
    'word':             () => lookupWord(args[0]),
    'word-summary':     () => wordSummary(args[0]),
    'word-n':           () => wordByN(args),
    'compound':         () => lookupCompound(+args[0]),
    'task':             () => createTask(+args[0], args[1], args[2]),
    'add-word':         () => addWord(+args[0], args[1], args[2], args[3]),
    'update-word':      () => updateWord(+args[0], +args[1], args[2], args[3]),
    'add-context':      () => addContextChecked(+args[0], +args[1], +args[2], args[3], args[4]),
    'add-compound':     () => addCompound(+args[0], +args[1], +args[2], +args[3]),
    'commit':           () => commit(+args[0], args[1]),  // args[1]: 'request' or 'response'
    'qa':               () => linkQa(+args[0]),
    're-prompt':        () => rePrompt(+args[0]),
    'reload':           () => reloadModel(),
    'prompt':           () => prompt(args[0]),
    'learn':            () => learn(+args[0], args[1], args[2])
  };
  const fn = cmds[cmd];
  if (!fn) { console.error(`Unknown command: ${cmd}`); process.exit(1); }
  fn().then(r => console.log(typeof r === 'string' ? r : JSON.stringify(r, null, 2))).catch(e => { console.error(e); process.exit(1); });
}
