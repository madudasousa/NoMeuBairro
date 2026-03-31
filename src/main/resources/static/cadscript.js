const $ = (sel) => document.querySelector(sel);
const services = new Set();

/* --- NOTIFICAÇÕES --- */
function showToast(message, type = "ok") {
  const toast = $("#toast");
  if (!toast) return;
  toast.textContent = message;
  toast.className = "toast " + (type === "ok" ? "toast--ok" : "toast--err");
  toast.style.display = "block";
  setTimeout(() => { toast.style.display = "none"; }, 3500);
}

/* --- VALIDAÇÃO E UTILITÁRIOS --- */
function setFieldError(fieldId, message) {
  const fieldWrap = document.getElementById(fieldId)?.closest(".field");
  const errorEl = document.querySelector(`[data-error-for="${fieldId}"]`);
  if (fieldWrap) fieldWrap.classList.add("is-invalid");
  if (errorEl) errorEl.textContent = message || "";
}

function clearFieldError(fieldId) {
  const fieldWrap = document.getElementById(fieldId)?.closest(".field");
  const errorEl = document.querySelector(`[data-error-for="${fieldId}"]`);
  if (fieldWrap) fieldWrap.classList.remove("is-invalid");
  if (errorEl) errorEl.textContent = "";
}

function onlyDigits(str) { return (str || "").replace(/\D/g, ""); }

/* --- GESTÃO DE CATEGORIAS DINÂMICAS --- */
// Carrega as categorias existentes no datalist para sugestão
async function carregarCategoriasNoDatalist() {
  const datalist = $("#categoriasExistentes");
  if (!datalist) return;
  try {
    const res = await fetch("http://localhost:8080/categorias");
    if (!res.ok) throw new Error();
    const categorias = await res.json();
    datalist.innerHTML = "";
    categorias.forEach(cat => {
      const option = document.createElement("option");
      option.value = cat.name;
      datalist.appendChild(option);
    });
  } catch (err) { console.error("Erro ao carregar categorias:", err); }
}

// Verifica se a categoria digitada existe; se não, cria uma nova no backend
async function obterOuCriarCategoria(nome) {
  const slug = nome.trim().toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, '-');
  try {
    const res = await fetch(`http://localhost:8080/categorias/${slug}`);
    if (res.ok) {
      const cat = await res.json();
      return cat.id; // Retorna o UUID da categoria existente
    }
    const resNovo = await fetch("http://localhost:8080/categorias", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: nome })
    });
    const novaCat = await resNovo.json();
    return novaCat.id; // Retorna o UUID da nova categoria criada
  } catch (e) {
    console.error("Erro no processamento da categoria:", e);
    return null;
  }
}

/* --- GESTÃO DE SERVIÇOS (TAGS) --- */
function renderServices() {
  const ul = $("#listaServicos");
  ul.innerHTML = "";
  [...services].forEach((name) => {
    const li = document.createElement("li");
    li.className = "service-item";
    li.innerHTML = `<span class="service-item__name">${name}</span>
                    <button type="button" class="service-item__remove">Remover</button>`;
    li.querySelector("button").onclick = () => { services.delete(name); renderServices(); };
    ul.appendChild(li);
  });
}

function addServico() {
  const input = $("#novoServico");
  const value = input.value.trim();
  if (value && !services.has(value)) {
    services.add(value);
    input.value = "";
    renderServices();
  }
}

/* --- GESTÃO DE IMAGENS (MÚLTIPLAS) --- */
const imageInput = $("#imagem");
const previewWrap = $("#imagePreview");
const previewImg = $("#previewImg");

imageInput.addEventListener("change", () => {
  const files = imageInput.files;
  if (files.length > 0) {
    previewImg.src = URL.createObjectURL(files[0]); // Preview da primeira imagem
    previewWrap.style.display = "block";
    $("#removeImageBtn").textContent = `Remover (${files.length} imagens)`;
  }
});

function clearImage() {
  imageInput.value = "";
  previewWrap.style.display = "none";
}

/* --- SUBMISSÃO PARA O BACKEND --- */
$("#formEstabelecimento").addEventListener("submit", async (e) => {
  e.preventDefault();

  const nomeCategoria = $("#categoriaInput").value;
  if (!nomeCategoria) {
    showToast("Digite ou selecione uma categoria.", "err");
    return;
  }

  const categoryId = await obterOuCriarCategoria(nomeCategoria);

  const payload = {
    name: $("#nome").value,
    description: $("#descricao").value,
    address: $("#bairro").value,
    time: $("#horario").value,
    phone: onlyDigits($("#whatsapp").value),
    categoryId: categoryId, // Envia o UUID real para o backend
    services: [...services], // Envia a lista de strings para o ServicesService
    active: true
  };

  try {
    // 1. Criar Estabelecimento
    const resEstab = await fetch("http://localhost:8080/estabelecimentos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!resEstab.ok) throw new Error("Erro ao criar estabelecimento");
    const estabSalvo = await resEstab.json();

    // 2. Upload de Múltiplas Imagens
    const arquivos = imageInput.files;
    if (arquivos.length > 0) {
      const formData = new FormData();
      for (let i = 0; i < arquivos.length; i++) {
        formData.append("arquivos", arquivos[i]); // "arquivos" mapeia para o @RequestParam no Java
      }
      await fetch(`http://localhost:8080/estabelecimento/${estabSalvo.id}/imagens`, {
        method: "POST",
        body: formData
      });
    }

    showToast("Cadastrado com sucesso!", "ok");
    setTimeout(() => { window.location.href = `estab.html?id=${estabSalvo.id}`; }, 1500);
  } catch (err) {
    console.error(err);
    showToast("Erro ao conectar com o servidor.", "err");
  }
});

/* --- INICIALIZAÇÃO --- */
document.addEventListener("DOMContentLoaded", () => {
  carregarCategoriasNoDatalist();
  $("#addServicoBtn").onclick = addServico;
  $("#removeImageBtn").onclick = clearImage;

  $("#novoServico").onkeydown = (e) => { if (e.key === "Enter") { e.preventDefault(); addServico(); } };

  $("#cancelBtn").onclick = () => {
    services.clear();
    renderServices();
    clearImage();
    showToast("Formulário limpo.", "ok");
  };
});