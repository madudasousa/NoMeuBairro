/* --- HELPER FUNCTIONS --- */

// Remove caracteres não numéricos
function onlyDigits(str) {
  return (str || "").replace(/\D/g, "");
}

// Constrói o link do Google Maps com base no endereço
function buildGoogleMapsLink(address) {
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address || "São Paulo")}`;
}

// Atalho para definir texto de um elemento
function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value || "";
}

// Renderiza as tags de serviços
function renderServiceTags(services) {
  const wrap = document.getElementById("serviceTags");
  if (!wrap) return;
  wrap.innerHTML = "";

  (services || []).forEach((service) => {
    const tag = document.createElement("span");
    tag.className = "service-tag";
    tag.textContent = service.name; // Acessa a propriedade 'name' do ServiceResponse
    wrap.appendChild(tag);
  });
}

/* --- CARROSSEL E MODAL --- */

const carouselTrack = document.getElementById("carouselTrack");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");
const galleryModal = document.getElementById("galleryModal");
const modalImage = document.getElementById("modalImage");

let currentIndex = 0;
let imageList = [];
let visibleItems = 3;
let modalIndex = 0;

function updateVisibleItems() {
  visibleItems = window.innerWidth >= 1024 ? 4 : 3;
}

function renderCarousel(images) {
  imageList = images.map(img => img.url) || []; // Extrai as URLs do ImageResponse
  if (!carouselTrack) return;

  carouselTrack.innerHTML = "";
  currentIndex = 0;
  updateVisibleItems();

  imageList.forEach((src, index) => {
    const item = document.createElement("div");
    item.className = "carousel-item";
    const img = document.createElement("img");
    img.src = src;
    img.alt = "Imagem do estabelecimento";
    item.appendChild(img);
    carouselTrack.appendChild(item);
    item.addEventListener("click", () => openModal(index));
  });

  updateCarousel();
}

function updateCarousel() {
  const item = document.querySelector(".carousel-item");
  if (!item || !carouselTrack) return;

  const style = getComputedStyle(carouselTrack);
  const gap = parseInt(style.gap) || 10;
  const itemWidth = item.offsetWidth + gap;

  carouselTrack.style.transform = `translateX(-${currentIndex * itemWidth}px)`;

  prevBtn.style.opacity = currentIndex === 0 ? "0.5" : "1";
  prevBtn.style.pointerEvents = currentIndex === 0 ? "none" : "auto";

  const maxIndex = Math.max(0, imageList.length - visibleItems);
  nextBtn.style.opacity = currentIndex >= maxIndex ? "0.5" : "1";
  nextBtn.style.pointerEvents = currentIndex >= maxIndex ? "none" : "auto";
}

/* --- MODAL FUNCTIONS --- */

function openModal(index) {
  modalIndex = index;
  updateModalImage();
  galleryModal.classList.add("active");
  document.body.style.overflow = "hidden";
}

function closeModal() {
  galleryModal.classList.remove("active");
  document.body.style.overflow = "";
}

function updateModalImage() {
  if (imageList.length > 0) modalImage.src = imageList[modalIndex];
}

/* --- INTEGRAÇÃO COM BACKEND --- */

// 1. Captura o ID do estabelecimento da URL (ex: estab.html?id=...)
const urlParams = new URLSearchParams(window.location.search);
const estabId = urlParams.get('id');

async function carregarDadosDoEstabelecimento() {
  if (!estabId) {
    console.error("ID do estabelecimento não fornecido na URL.");
    return;
  }

  try {
    // Chamada ao endpoint do EstabController
    const response = await fetch(`http://localhost:8080/estabalecimentos/${estabId}`);

    if (!response.ok) throw new Error("Erro ao buscar dados do servidor");

    const data = await response.json(); // Recebe um EstabResponse

    // Preenche os dados básicos
    setText("storeName", data.name);
    // Nota: No seu DTO Java está escrito 'descriptiom' com 'm'
    setText("storeDesc", data.descriptiom || "Sem descrição disponível.");

    // Renderiza serviços e carrossel
    renderServiceTags(data.services);
    renderCarousel(data.images);

    // Configura o botão de WhatsApp (usa o link gerado pelo backend)
    const whatsBtn = document.getElementById("whatsBtn");
    if (whatsBtn && data.linkWhatsapp) {
      whatsBtn.href = data.linkWhatsapp;
    }

    // Configura o Mapa
    const mapFrame = document.getElementById("mapFrame");
    const mapLink = document.getElementById("mapLink");
    if (mapFrame) {
      const endereco = data.address || "São Paulo";
      mapFrame.src = `https://www.google.com/maps/embed/v1/place?key=SUA_CHAVE_AQUI&q=${encodeURIComponent(endereco)}`;
      if (mapLink) mapLink.href = buildGoogleMapsLink(endereco);
    }

  } catch (error) {
    console.error("Erro na requisição:", error);
    setText("storeName", "Erro ao carregar");
  }
}

/* --- EVENT LISTENERS --- */

document.getElementById("closeModalBtn")?.addEventListener("click", closeModal);

prevBtn?.addEventListener("click", () => {
  if (currentIndex > 0) {
    currentIndex--;
    updateCarousel();
  }
});

nextBtn?.addEventListener("click", () => {
  const maxIndex = Math.max(0, imageList.length - visibleItems);
  if (currentIndex < maxIndex) {
    currentIndex++;
    updateCarousel();
  }
});

window.addEventListener("resize", () => {
  updateVisibleItems();
  updateCarousel();
});

// Inicialização
carregarDadosDoEstabelecimento();
