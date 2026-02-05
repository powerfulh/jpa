const fab = document.getElementById("fab");
const metaModal = document.getElementById("metaModal");
const confirmModal = document.getElementById("confirmModal");
const metaList = document.getElementById("metaList");
const itemContainer = document.getElementById("itemContainer");
const paginationEl = document.getElementById("pagination");
const selectedMetaEl = document.getElementById("selectedMeta");
const changeList = document.getElementById("changeList");
const finalSubmitBtn = document.getElementById("finalSubmit");

let metas = [];
let currentMeta = null;
let items = [];
let originalItems = [];

let currentPage = 1;
const PAGE_SIZE = 10;

/* ---------- 공통 ---------- */
function openModal(m) { m.classList.remove("hidden"); }
function closeModal(m) { m.classList.add("hidden"); }

document.querySelectorAll(".overlay").forEach(o =>
  o.addEventListener("click", e => closeModal(e.target.parentElement))
);

/* ---------- 초기 메타 로드 ---------- */
(async function initMeta() {
  const res = await fetch("/jpaui/meta");
  metas = await res.json();
})();

/* ---------- 메타 선택 ---------- */
fab.addEventListener("click", () => {
  metaList.innerHTML = "";
  metas.forEach(m => {
    const li = document.createElement("li");
    li.textContent = m;
    li.onclick = () => selectMeta(m);
    metaList.appendChild(li);
  });
  openModal(metaModal);
});

async function selectMeta(meta) {
  currentMeta = meta;
  currentPage = 1;
  selectedMetaEl.textContent = meta;
  selectedMetaEl.classList.remove("empty");
  closeModal(metaModal);
  await loadItems();
}

/* ---------- 아이템 로드 ---------- */
async function loadItems() {
  const res = await fetch(`/jpaui/${currentMeta}`);
  items = await res.json();
  originalItems = JSON.parse(JSON.stringify(items));
  render();
}

/* ---------- 렌더 ---------- */
function render() {
  renderItems();
  renderPagination();
}

function renderItems() {
  itemContainer.innerHTML = "";
  const start = (currentPage - 1) * PAGE_SIZE;
  const pageItems = items.slice(start, start + PAGE_SIZE);

  pageItems.forEach((item, pageIndex) => {
    const realIndex = start + pageIndex;
    const div = document.createElement("div");
    div.className = "item";

    Object.entries(item).forEach(([key, value]) => {
      const field = document.createElement("div");
      field.className = "field";

      const label = document.createElement("label");
      label.textContent = key;

      const input = document.createElement("input");
      input.value = value;
      if (key === "n") input.readOnly = true;

      input.dataset.index = realIndex;
      input.dataset.key = key;

      field.append(label, input);
      div.appendChild(field);
    });

    const submit = document.createElement("button");
    submit.className = "submit-btn";
    submit.textContent = "제출";
    submit.onclick = () => openConfirm(realIndex);

    div.appendChild(submit);
    itemContainer.appendChild(div);
  });
}

/* ---------- 패이징 ---------- */
function renderPagination() {
  paginationEl.innerHTML = "";
  const totalPage = Math.ceil(items.length / PAGE_SIZE);

  const start = Math.max(1, currentPage - 2);
  const end = Math.min(totalPage, currentPage + 2);

  for (let i = start; i <= end; i++) {
    const btn = document.createElement("button");
    btn.className = "page-btn" + (i === currentPage ? " active" : "");
    btn.textContent = i;
    btn.onclick = () => {
      currentPage = i;
      render();
    };
    paginationEl.appendChild(btn);
  }
}

/* ---------- 변경 내역 ---------- */
function openConfirm(index) {
  changeList.innerHTML = "";
  const inputs = document.querySelectorAll(`input[data-index="${index}"]`);
  const changes = [];

  inputs.forEach(input => {
    const key = input.dataset.key;
    const oldVal = originalItems[index][key];
    if (input.value !== String(oldVal)) {
      changes.push({ key, oldVal, newVal: input.value });
    }
  });

  if (!changes.length) {
    alert("변경된 값이 없습니다.");
    return;
  }

  changes.forEach(c => {
    const li = document.createElement("li");
    li.textContent = `${c.key}: ${c.oldVal} → ${c.newVal}`;
    changeList.appendChild(li);
  });

  finalSubmitBtn.onclick = () => {
    console.log("TODO 제출", changes);
    closeModal(confirmModal);
  };

  openModal(confirmModal);
}
