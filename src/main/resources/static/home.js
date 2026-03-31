
const API_URL = "http://localhost:8080";

/* ELEMENTOS */
const categoryTagsEl = document.getElementById("categoryTags");
const sectionsContainerEl = document.getElementById("sectionsContainer");
const searchInputEl = document.getElementById("searchInput");
const searchFormEl = document.getElementById("searchForm");

let activeCategory = "";
let currentSearch = "";
let allEstablishments = [];
let allCategories = [];

/* HELPERS */
function normalizeText(text) {
  return (text || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}
function showLoading() {
  sectionsContainerEl.innerHTML = `
    <div class="empty-state">Carregando estabelecimentos...</div>
  `;
}

function showError(message) {
  sectionsContainerEl.innerHTML = `
    <div class="empty-state">${message}</div>
  `;
}

async function fetchCategorias() {
  try {
    const res = await fetch(`${API_URL}/categorias`);
    if (!res.ok) throw new Error("Erro ao buscar categorias");
    allCategories = await res.json();
    renderCategoryTags();
  } catch (err) {
    console.error("Erro ao carregar categorias:", err);
  }
}

async function fetchEstabelecimentos() {
  showLoading();

  try {
    // Monta a URL com os filtros que estiverem preenchidos
    const params = new URLSearchParams();
    if (currentSearch) params.append("name", currentSearch);
    if (activeCategory) params.append("categorySlug", activeCategory);

    // size=50 traz até 50 estabelecimentos por vez
    params.append("size", "50");

    const res = await fetch(`${API_URL}/estabelecimentos?${params.toString()}`);
    if (!res.ok) throw new Error("Erro ao buscar estabelecimentos");

    // A API retorna um objeto Page com o array em data.content
    const page = await res.json();
    allEstablishments = Array.isArray(page) ? page : (page.content || []);

    renderSections();
  } catch (err) {
    console.error("Erro ao carregar estabelecimentos:", err);
    showError("Não foi possível carregar os estabelecimentos. Tente novamente.");
  }
}
/*filtro local*/
function filterEstablishments() {
  if (!currentSearch) return allEstablishments;

  const query = normalizeText(currentSearch);
  return allEstablishments.filter((item) =>
      normalizeText(item.name).includes(query) ||
      normalizeText(item.category).includes(query) ||
      normalizeText(item.descriptionCurta).includes(query)
  );
}

function getGroupedByCategory(items) {
  const grouped = {};

  items.forEach((item) => {
    const cat = item.category || "Outros";
    if (!grouped[cat]) grouped[cat] = [];
    grouped[cat].push(item);
  });

  return grouped;
}

/*RENDER CATEGORIAS*/
function renderCategoryTags() {
  categoryTagsEl.innerHTML = "";

  allCategories.forEach((category) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className =
        "category-tag" + (activeCategory === category.slug ? " is-active" : "");
    button.textContent = category.name;

    button.addEventListener("click", () => {
      // Clicou na categoria já ativa → desativa o filtro
      activeCategory = activeCategory === category.slug ? "" : category.slug;
      renderCategoryTags();
      // Busca novamente na API com o novo filtro de categoria
      fetchEstabelecimentos();
    });

    categoryTagsEl.appendChild(button);
  });
}

/* RENDER CARDS */
function createCard(item) {
  const card = document.createElement("article");
  card.className = "store-card-mini";

  card.innerHTML = `
    <div class="store-card-mini__image">
      <img src="${item.imageCapa || ""}" alt="${item.name}">
    </div>
    <div class="store-card-mini__body">
      <h3 class="store-card-mini__name">${item.name}</h3>
      <p class="store-card-mini__desc">${item.descriptionCurta || ""}</p>
      <div class="store-card-mini__meta">
        <div>📍 ${item.address || ""}</div>
        <div>🏷 ${item.category || ""}</div>
      </div>
      <div class="store-card-mini__footer">Abrir info</div>
    </div>
  `;

  // Ao clicar no card, salva o ID na URL e navega para a tela de detalhes
  // O estab.js vai ler esse ID e buscar os dados completos na API
  card.addEventListener("click", () => {
    window.location.href = `estab.html?id=${item.id}`;
  });

  return card;
}

/* RENDER SEÇÕES */
function renderSections() {
  const filtered = filterEstablishments();
  const grouped = getGroupedByCategory(filtered);
  const visibleCategories = Object.keys(grouped).filter(
      (cat) => grouped[cat].length > 0
  );

  sectionsContainerEl.innerHTML = "";

  if (!visibleCategories.length) {
    sectionsContainerEl.innerHTML = `
      <div class="empty-state">
        Nenhum estabelecimento encontrado para essa busca.
      </div>
    `;
    return;
  }

  visibleCategories.forEach((category) => {
    const section = document.createElement("section");
    section.className = "category-section";

    const header = document.createElement("div");
    header.className = "category-section__header";
    header.innerHTML = `
      <h2 class="category-section__title">${category}</h2>
      <span class="category-section__arrow">›</span>
    `;

    const row = document.createElement("div");
    row.className = "cards-row";

    grouped[category].forEach((item) => row.appendChild(createCard(item)));

    section.appendChild(header);
    section.appendChild(row);
    sectionsContainerEl.appendChild(section);
  });
}

/* BUSCA */
searchFormEl.addEventListener("submit", (e) => {
  e.preventDefault();
  currentSearch = searchInputEl.value.trim();
  fetchEstabelecimentos();
});

searchInputEl.addEventListener("input", () => {
  currentSearch = searchInputEl.value.trim();
  renderSections();
});

/* INICIALIZAÇÃO */
fetchCategorias();
fetchEstabelecimentos();