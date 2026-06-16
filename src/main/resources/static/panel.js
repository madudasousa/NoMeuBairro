const API_URL = "http://localhost:8080";

// Redireciona para login se não estiver autenticado
const token = localStorage.getItem("token");
const usuarioStr = localStorage.getItem("usuario");
if (!token || !usuarioStr) {
    window.location.href = "/login_cadastro.html";
}

const usuario = JSON.parse(usuarioStr);
let estabId = null;

/* HELPERS */
function showToast(id, message, type = "ok") {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = message;
    el.className = "toast " + (type === "ok" ? "toast--ok" : "toast--err");
    el.style.display = "block";
    setTimeout(() => { el.style.display = "none"; }, 3500);
}

function authHeaders() {
    return { "Content-Type": "application/json", "Authorization": `Bearer ${token}` };
}

/* CARREGAR DADOS DA LOJA */
async function carregarDados() {
    try {
        const res = await fetch(`${API_URL}/usuarios/minha-loja`, {
            headers: authHeaders()
        });

        if (res.status === 401) {
            // Token expirado — desloga
            localStorage.removeItem("token");
            localStorage.removeItem("usuario");
            window.location.href = "/login_cadastro.html";
            return;
        }

        if (!res.ok) throw new Error();

        const data = await res.json();
        estabId = data.id;

        // Preenche os campos com os dados vindos da API
        document.getElementById("p-nome").value = data.name || "";
        document.getElementById("p-categoria").value = data.category?.name || "";
        document.getElementById("p-descricao").value = data.description || "";
        document.getElementById("p-address").value = data.address || "";
        document.getElementById("p-time").value = data.time || "";
        document.getElementById("p-phone").value = data.phone || "";
        document.getElementById("p-activeOwner").checked = data.activeOwner;

        // Se ADM desativou — bloqueia o checkbox e mostra aviso
        const wrapActiveOwner = document.getElementById("wrapActiveOwner");
        const hintActiveAdmin = document.getElementById("hintActiveAdmin");
        if (!data.activeAdmin) {
            document.getElementById("p-activeOwner").disabled = true;
            hintActiveAdmin.textContent = "⚠️ Sua loja foi desativada pelo administrador. Entre em contato para mais informações.";
            hintActiveAdmin.style.color = "#e53e3e";
        }

        // Banner de status no topo do painel
        renderStatusBanner(data.activeOwner, data.activeAdmin);

    } catch (err) {
        console.error("Erro ao carregar dados:", err);
        showToast("toastDados", "Não foi possível carregar os dados da loja.", "err");
    }
}

function renderStatusBanner(activeOwner, activeAdmin) {
    const banner = document.getElementById("painelStatus");
    if (!activeAdmin) {
        banner.className = "painel-status painel-status--admin";
        banner.textContent = "🔴 Loja desativada pelo administrador — não aparece nas buscas.";
    } else if (!activeOwner) {
        banner.className = "painel-status painel-status--owner";
        banner.textContent = "🟠 Loja pausada por você — não aparece nas buscas.";
    } else {
        banner.className = "painel-status painel-status--active";
        banner.textContent = "🟢 Loja ativa — visível para os clientes.";
    }
}

/* SALVAR DADOS */
document.getElementById("formDados").addEventListener("submit", async (e) => {
    e.preventDefault();

    const payload = {
        name: document.getElementById("p-nome").value.trim(),
        description: document.getElementById("p-descricao").value.trim(),
        address: document.getElementById("p-address").value.trim(),
        time: document.getElementById("p-time").value.trim(),
        phone: document.getElementById("p-phone").value.replace(/\D/g, ""),
        activeOwner: document.getElementById("p-activeOwner").checked
    };

    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Salvando...";

    try {
        const res = await fetch(`${API_URL}/estabelecimentos/${estabId}`, {
            method: "PUT",
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error();

        const data = await res.json();
        renderStatusBanner(data.activeOwner, data.activeAdmin);
        showToast("toastDados", "Dados salvos com sucesso!", "ok");

    } catch (err) {
        showToast("toastDados", "Erro ao salvar. Tente novamente.", "err");
    } finally {
        btn.disabled = false;
        btn.textContent = "Salvar alterações";
    }
});

/* TROCAR SENHA */
document.getElementById("formSenha").addEventListener("submit", async (e) => {
    e.preventDefault();

    document.getElementById("erroSenha").textContent = "";
    document.getElementById("erroSenhaAtual").textContent = "";

    const senhaAtual = document.getElementById("p-senhaAtual").value;
    const novaSenha = document.getElementById("p-novaSenha").value;
    const confirmaSenha = document.getElementById("p-confirmaSenha").value;

    if (novaSenha !== confirmaSenha) {
        document.getElementById("erroSenha").textContent = "As senhas não coincidem.";
        return;
    }
    if (novaSenha.length < 6) {
        document.getElementById("erroSenha").textContent = "A nova senha deve ter no mínimo 6 caracteres.";
        return;
    }

    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Alterando...";

    try {
        const res = await fetch(`${API_URL}/usuarios/trocar-senha`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ senhaAtual, novaSenha, confirmaNovaSenha: confirmaSenha })
        });

        const data = await res.json();

        if (!res.ok) {
            // Se erro for na senha atual, mostra no campo certo
            if (data.erro?.includes("atual")) {
                document.getElementById("erroSenhaAtual").textContent = data.erro;
            } else {
                showToast("toastSenha", data.erro || "Erro ao alterar senha.", "err");
            }
            return;
        }

        showToast("toastSenha", "Senha alterada com sucesso!", "ok");
        e.target.reset();

    } catch (err) {
        showToast("toastSenha", "Erro ao alterar senha. Tente novamente.", "err");
    } finally {
        btn.disabled = false;
        btn.textContent = "Alterar senha";
    }
});

/* LOGOUT */
document.getElementById("btnLogout").addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "/home.html";
});

/* INICIALIZAÇÃO */
carregarDados();