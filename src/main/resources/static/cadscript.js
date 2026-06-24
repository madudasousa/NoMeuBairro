const API_URL = "http://localhost:8080";
const $ = (sel) => document.querySelector(sel);
const services = new Set();

//Verifica acesso ADM
const token = localStorage.getItem("token");
const usuarioStr = localStorage.getItem("usuario");

if (!token || !usuarioStr){
    window.location.href = "/login.html";
} else {
    const usuario = JSON.parse(usuarioStr);
    if (usuario.perfil !== "ROLE_ADM"){
        //window.location.href = "/home.html";
    }
}

function showToast(message, type = "ok") {
  const toast = $("#toast");
  if (!toast) return;
  toast.textContent = message;
  toast.className = "toast " + (type === "ok" ? "toast--ok" : "toast--err");
  toast.style.display = "block";
  setTimeout(() => { toast.style.display = "none"; }, 3500);
}

//erro de campo
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

function gerarSlug(nome) {
    return nome.trim()
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-z0-9\s-]/g, "")
        .trim()
        .replace(/\s+/g, "-");
}
// categoriass dinamicas
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
      option.dataset.id = cat.id;
      datalist.appendChild(option);
    });
  } catch (err) { console.error("Erro ao carregar categorias:", err); }
}

// Verifica se a categoria digitada existe; se não, cria uma nova no backend
async function obterOuCriarCategoria(nome) {
    if (!nome || !nome.trim()) return null;
    const nomeLimpo = nome.trim();
    const slug = gerarSlug(nomeLimpo);

  try {
    const res = await fetch(`http://localhost:8080/categorias/${slug}`);
    if (res.ok) {
      const cat = await res.json();
      return cat.id;
    }
    const resNovo = await fetch("http://localhost:8080/categorias", {
      method: "POST",
      headers: { "Content-Type": "application/json" ,
      "Authorization": "Bearer " + token },
      body: JSON.stringify({ name: nomeLimpo })
    });
    if (!resNovo.ok) throw new Error("Erro ao criar categoria")
    const novaCat = await resNovo.json();

    const datalist = $("#categoriasExistentes");
    if (datalist){
        const option = document.createElement("option");
        option.value = novaCat.name;
        option.dataset.id = novaCat.id;
        datalist.appendChild(option);
    }
    return novaCat.id;

  } catch (e) {
    console.error("Erro no processamento da categoria:", e);
    return null;
  }
}

// servicos (TAGS)
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
  const value = (input.value || "").trim();
  if (!value) {showToast("Digite um serviço antes de adicionar.", "err"); return; }
  if([...services].some((s)=> s.toLowerCase() === value.toLowerCase())) {
      showToast("Esse serviço já está cadastrado.", "err");
      return;
  }
    services.add(value);
    input.value = "";
    renderServices();
}

// imagens(MÚLTIPLAS)
const imageInput = $("#imagem");
const previewWrap = $("#imagePreview");
const previewImg = $("#previewImg");

imageInput?.addEventListener("change", () => {
  const files = imageInput.files;
  if (files.length > 0) {
    previewImg.src = URL.createObjectURL(files[0]); // Preview da primeira imagem
    previewWrap.style.display = "block";
    const removeBtn = $("#removeImageBtn");
    if (removeBtn) removeBtn.textContent = `Remover (${files.length} imagem${files.length > 1 ? "ns" : ""})`;
  }
});

function clearImage() {
  imageInput.value = "";
  previewWrap.style.display = "none";
}

// submissao para o backend
$("#formEstabelecimento").addEventListener("submit", async (e) => {
  e.preventDefault();

  const nomeCategoria = $("#categoriaInput").value?.trim();
  if (!nomeCategoria) {
      setFieldError("categoriaInput", "Selecione ou digite uma categoria.");
      return;
  }

  const submitBtn = $("#submitBtn");
  if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Cadastrando..."; }

  try {
      const categoryId = await obterOuCriarCategoria(nomeCategoria);
      if (!categoryId){
      throw new Error("Não foi possível processar a categoria. Tente novamente.");
  }

  const payload = {
      name: $("#nome").value?.trim(),
      document: $("#documento").value?.trim(),
      description: $("#descricao").value?.trim(),
      address: $("#bairro").value?.trim(),
      time: $("#horario").value?.trim(),
      phone: onlyDigits($("#whatsapp").value || ""),
      categoryId: categoryId,
      services: [...services],
      active: true,
      ownerName: $("#ownerName")?.value?.trim(),
      password: $("#senha").value
  };


    const resEstab = await fetch("http://localhost:8080/estabelecimentos", {
      method: "POST",
      headers: { "Content-Type": "application/json" ,
                "Authorization": "Bearer " + token},
      body: JSON.stringify(payload)
    });

    if (!resEstab.ok) {
        const err = await resEstab.json().catch(() => ({}));
        throw new Error(err.message || err.erro || "Erro ao criar estabelecimento");
    }
    const estabSalvo = await resEstab.json();


    const arquivos = imageInput.files;
    if (arquivos && arquivos.length > 0) {
      const formData = new FormData();
      for (let i = 0; i < arquivos.length; i++) {
        formData.append("arquivos", arquivos[i]); // "arquivos" mapeia para o @RequestParam no Java
      }
      await fetch(`http://localhost:8080/estabelecimentos/${estabSalvo.id}/imagens`, {
        method: "POST",
          headers: { "Authorization": "Bearer " + token },
        body: formData
      });
    }

    showToast("Cadastrado com sucesso!", "ok");
    setTimeout(() => { window.location.href = "/admin.html"; }, 1500);
  } catch (err) {
    console.error(err);
    showToast("Erro ao conectar com o servidor.", "err");
    if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Cadastrar Estabelecimento"; }
  }
});

// INICIALIZAÇÃO
document.addEventListener("DOMContentLoaded", () => {
  carregarCategoriasNoDatalist();
  $("#addServicoBtn")?.addEventListener("click", addServico);
  $("#removeImageBtn")?.addEventListener("click", clearImage);
  $("#novoServico").addEventListener("keydown", (e) => {
      if (e.key === "Enter") { e.preventDefault(); addServico(); }
  })

  $("#cancelBtn")?.addEventListener("click", () => {
    services.clear();
    renderServices();
    clearImage();
    showToast("Formulário limpo.", "ok");
  });
});