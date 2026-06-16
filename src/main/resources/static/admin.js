const API_URL = "http://localhost:8080";

// Redireciona para login se não for ADM
// LER O TOKEN E USUARIO DO LOCALSTORAGE AQUI PARA GARANTIR QUE ESTÃO ATUALIZADOS
const currentToken = localStorage.getItem("token");
const currentUsuarioStr = localStorage.getItem("usuario");

if (!currentToken || !currentUsuarioStr) {
    window.location.href = "/login.html"; // Corrigido para /login.html
} else {
    const usuario = JSON.parse(currentUsuarioStr);
    if (usuario.perfil !== "ADM") {
        window.location.href = "/home.html";
    }
}


let todosEstabs = [];

// FUNÇÃO authHeaders AGORA LÊ O TOKEN DO LOCALSTORAGE A CADA CHAMADA
function authHeaders() {
    const token = localStorage.getItem("token"); // Lê o token mais recente
    return { "Content-Type": "application/json", "Authorization": `Bearer ${token}` };
}

function showToast(message, type = "ok") {
    const el = document.getElementById("toastAdmin");
    el.textContent = message;
    el.className = "toast " + (type === "ok" ? "toast--ok" : "toast--err");
    el.style.display = "block";
    setTimeout(() => { el.style.display = "none"; }, 3500);
}

/* CARREGAR TODOS OS ESTABELECIMENTOS */
async function carregarEstabs() {
    const lista = document.getElementById("adminLista");
    lista.innerHTML = `<div class="empty-state">Carregando...</div>`;

    try {
        const res = await fetch(`${API_URL}/estabelecimentos/admin/todos?size=100`, {
            headers: authHeaders()
        });

        if (res.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("usuario");
            window.location.href = "/login.html"; // Corrigido para /login.html
            return;
        }

        if (!res.ok) throw new Error();

        const page = await res.json();
        todosEstabs = page.content || [];
        renderLista(todosEstabs);

    } catch (err) {
        lista.innerHTML = `<div class="empty-state">Erro ao carregar estabelecimentos.</div>`;
    }
}

/* RENDERIZAR LISTA */
function renderLista(estabs) {
    const lista = document.getElementById("adminLista");
    lista.innerHTML = "";

    if (!estabs.length) {
        lista.innerHTML = `<div class="empty-state">Nenhum estabelecimento encontrado.</div>`;
        return;
    }

    estabs.forEach((estab) => {
        const card = document.createElement("div");

        // Define a classe de cor conforme o estado de ativação
        // Verde = ambos ativos | Laranja = dono pausou | Vermelho = ADM bloqueou
        let statusClass = "admin-card--ativo";
        let statusTexto = "Ativo";
        if (!estab.activeAdmin) {
            statusClass = "admin-card--admin";
            statusTexto = "Bloqueado pelo admin";
        } else if (!estab.activeOwner) {
            statusClass = "admin-card--owner";
            statusTexto = "Pausado pelo dono";
        }

        card.className = `admin-card ${statusClass}`;
        card.innerHTML = `
      <div class="admin-card__info">
        <div class="admin-card__name">${estab.name}</div>
        <div class="admin-card__meta">
          📍 ${estab.address || "—"} &nbsp;|&nbsp; 🏷 ${estab.category || "—"}
        </div>
        <div class="admin-card__status">${statusTexto}</div>
      </div>
      <div class="admin-card__action">
        <label class="toggle-label">
          <input
            type="checkbox"
            class="toggle-admin"
            data-id="${estab.id}"
            ${estab.activeAdmin ? "checked" : ""}
          />
          <span class="toggle-track"></span>
        </label>
        <small>${estab.activeAdmin ? "Liberar" : "Bloqueado"}</small>
      </div>
    `;

        // Toggle soberano do ADM
        card.querySelector(".toggle-admin").addEventListener("change", async (e) => {
            const novoStatus = e.target.checked;
            await alterarStatusAdmin(estab.id, novoStatus, card, estab);
        });

        lista.appendChild(card);
    });
}

/* ALTERAR STATUS PELO ADM */
async function alterarStatusAdmin(id, activeAdmin, card, estab) {
    try {
        const res = await fetch(`${API_URL}/estabelecimentos/${id}/admin-status`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify({ activeAdmin })
        });

        if (!res.ok) throw new Error();

        // Atualiza o estado local e re-renderiza o card
        estab.activeAdmin = activeAdmin;

        let statusClass = "admin-card--ativo";
        let statusTexto = "Ativo";
        if (!activeAdmin) {
            statusClass = "admin-card--admin";
            statusTexto = "Bloqueado pelo admin";
        } else if (!estab.activeOwner) {
            statusClass = "admin-card--owner";
            statusTexto = "Pausado pelo dono";
        }

        card.className = `admin-card ${statusClass}`;
        card.querySelector(".admin-card__status").textContent = statusTexto;
        card.querySelector("small").textContent = activeAdmin ? "Liberar" : "Bloqueado";

        showToast(
            activeAdmin
                ? `${estab.name} foi reativado.`
                : `${estab.name} foi bloqueado.`,
            "ok"
        );

    } catch (err) {
        showToast("Erro ao alterar status. Tente novamente.", "err");
        // Reverte o checkbox visualmente
        card.querySelector(".toggle-admin").checked = !activeAdmin;
    }
}

/* FILTRO LOCAL POR NOME */
document.getElementById("adminSearch").addEventListener("input", (e) => {
    const query = e.target.value.toLowerCase().trim();
    if (!query) {
        renderLista(todosEstabs);
        return;
    }
    const filtrados = todosEstabs.filter((estab) =>
        estab.name.toLowerCase().includes(query)
    );
    renderLista(filtrados);
});

/* LOGOUT */
document.getElementById("btnLogoutAdmin").addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "/home.html";
});

/* INICIALIZAÇÃO */
carregarEstabs();