const API_URL = "http://localhost:8080";

/* HELPERS DE ERRO */
function mostrarErro(id, mensagem) {
    const el = document.getElementById(id);
    if (el) el.textContent = mensagem;
}

function limparErros(...ids) {
    ids.forEach((id) => {
        const el = document.getElementById(id);
        if (el) el.textContent = "";
    });
}

function onlyDigits(str) {
    return (str || "").replace(/\D/g, "");
}

document.getElementById("form-login").addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErros("erro-identificador", "erro-senha", "erro-login-geral");

    const documento = onlyDigits(document.getElementById("login-identificador").value);
    const senha = document.getElementById("login-senha").value.trim();

    // Validação básica no frontend
    if (!documento) {
        mostrarErro("erro-identificador", "Informe seu CPF ou CNPJ.");
        return;
    }
    if (documento.length !== 11 && documento.length !== 14) {
        mostrarErro("erro-identificador", "CPF deve ter 11 dígitos ou CNPJ 14 dígitos.");
        return;
    }
    if (!senha || senha.length < 6) {
        mostrarErro("erro-senha", "A senha deve ter no mínimo 6 caracteres.");
        return;
    }

    const submitBtn = e.target.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.textContent = "Entrando...";

    try {
        const res = await fetch(`${API_URL}/usuarios/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ documento, senha })
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.erro || "CPF/CNPJ ou senha incorretos.");
        }

        // Salva o token e dados do usuário no localStorage
        // O token será enviado em todas as requisições protegidas
        localStorage.setItem("token", data.token);
        localStorage.setItem("usuario", JSON.stringify({
            id: data.id,
            nome: data.nome,
            perfil: data.perfil
        }));

        window.location.href = "/home.html";

    } catch (err) {
        mostrarErro("erro-login-geral", err.message || "Erro ao fazer login. Tente novamente.");
        submitBtn.disabled = false;
        submitBtn.textContent = "Entrar";
    }
});
document.addEventListener("DOMContentLoaded", () => {
    $("#cancelBtn").onclick = () => {
        showToast("Formulário limpo.", "ok");
    };
});