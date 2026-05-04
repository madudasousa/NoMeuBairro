const API_URL = "http://localhost:8080";

function buildGoogleMapsLink(address) {
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address || "São Paulo")}`;
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value || "";
}

function getIdFromUrl() {
  return new URLSearchParams(window.location.search).get("id");
}

/* SERVIÇOS */
function renderServiceTags(services) {
  const wrap = document.getElementById("serviceTags");
  if (!wrap) return;
  wrap.innerHTML = "";
  (services || []).forEach((service) => {
    const tag = document.createElement("span");
    tag.className = "service-tag";
    tag.textContent = service.name;
    wrap.appendChild(tag);
  });
}

/* CARROSSEL */
const carouselTrack = document.getElementById("carouselTrack");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");

let currentIndex = 0;
let imageList = [];
let visibleItems = 3;

function updateVisibleItems() {
  visibleItems = window.innerWidth >= 1024 ? 4 : 3;
}

function renderCarousel(images) {
  imageList = (images || []).map((img) => img.url);
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


  requestAnimationFrame(() => updateCarousel());
}

function updateCarousel() {
  const item = document.querySelector(".carousel-item");
  if (!item || !carouselTrack) return;

  requestAnimationFrame(() => {
    const gap = parseInt(getComputedStyle(carouselTrack).gap) || 10;
    const itemWidth = item.offsetWidth + gap;

    carouselTrack.style.transform = `translateX(-${currentIndex * itemWidth}px)`;

    const maxIndex = Math.max(0, imageList.length - visibleItems);

    prevBtn.style.opacity = currentIndex === 0 ? "0.3" : "1";
    prevBtn.style.pointerEvents = currentIndex === 0 ? "none" : "auto";

    nextBtn.style.opacity = currentIndex >= maxIndex ? "0.3" : "1";
    nextBtn.style.pointerEvents = currentIndex >= maxIndex ? "none" : "auto";
  });
}

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

/* MODAL */
const galleryModal = document.getElementById("galleryModal");
const modalImage = document.getElementById("modalImage");
let modalIndex = 0;

function openModal(index) {
  modalIndex = index;
  modalImage.src = imageList[modalIndex];
  galleryModal.classList.add("active");
  document.body.style.overflow = "hidden";
}

function closeModal() {
  galleryModal.classList.remove("active");
  document.body.style.overflow = "";
}

function modalPrev() {
  if (!imageList.length) return;
  // Volta para a última imagem se estiver na primeira (loop circular)
  modalIndex = (modalIndex - 1 + imageList.length) % imageList.length;
  modalImage.src = imageList[modalIndex];
}

function modalNext() {
  if (!imageList.length) return;
  // Volta para a primeira imagem se estiver na última (loop circular)
  modalIndex = (modalIndex + 1) % imageList.length;
  modalImage.src = imageList[modalIndex];
}

document.getElementById("closeModalBtn")?.addEventListener("click", closeModal);
document.getElementById("modalPrevBtn")?.addEventListener("click", modalPrev);
document.getElementById("modalNextBtn")?.addEventListener("click", modalNext);

// Fecha o modal ao clicar fora da imagem
galleryModal?.addEventListener("click", (e) => {
  if (e.target === galleryModal) closeModal();
});

// Navegação pelo teclado no modal
document.addEventListener("keydown", (e) => {
  if (!galleryModal?.classList.contains("active")) return;
  if (e.key === "Escape") closeModal();
  if (e.key === "ArrowLeft") modalPrev();
  if (e.key === "ArrowRight") modalNext();
});

/* CARREGAR DADOS DA API */
async function init() {
  const id = getIdFromUrl();

  if (!id) {
    setText("storeName", "Estabelecimento não encontrado");
    setText("storeDesc", "Volte para a página inicial.");
    return;
  }

  try {
    const res = await fetch(`${API_URL}/estabelecimentos/${id}`);
    if (!res.ok) throw new Error();

    const data = await res.json();

    setText("storeName", data.name);
    setText("storeDesc", data.description || "Sem descrição disponível.");

    renderServiceTags(data.services || []);
    renderCarousel(data.images || []);

    const whatsBtn = document.getElementById("whatsBtn");
    if (whatsBtn) {
      if (data.linkWhatsapp) {
        whatsBtn.href = data.linkWhatsapp;
        whatsBtn.style.display = "flex";
      } else {
        whatsBtn.style.display = "none";
      }
    }

    const mapFrame = document.getElementById("mapFrame");
    const mapLink = document.getElementById("mapLink");
    const endereco = data.address || "São Paulo";
    if (mapFrame) mapFrame.src = `https://www.google.com/maps?q=${encodeURIComponent(endereco)}&output=embed`;
    if (mapLink) mapLink.href = buildGoogleMapsLink(endereco);

  } catch (err) {
    console.error("Erro ao carregar estabelecimento:", err);
    setText("storeName", "Erro ao carregar");
    setText("storeDesc", "Não foi possível buscar os dados. Tente novamente.");
  }
}

init();