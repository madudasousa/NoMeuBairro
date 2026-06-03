const API_URL = "http://localhost:8080";

/* ALTERNAR ENTRE LOGIN E CADASTRO */
function alternarTelas() {
    document.getElementById("section-login").classList.toggle("hidden");
    document.getElementById("section-cadastro").classList.toggle("hidden");
    limparErros("erro-identificador", "erro-senha", "erro-login-geral", "erro-cadastro-geral");
}

/* ADAPTAR LABELS CONFORME PERFIL (CLIENTE / ESTABELECIMENTO) */
function adaptarLabels() {
    const perfil = document.getElementById("cad-perfil").value;
    const labelNome = document.getElementById("label-nome");
    const labelDoc = document.getElementById("label-doc");
    const labelData = document.getElementById("label-data");
    const inputNome = document.getElementById("cad-nome");
    const inputDoc = document.getElementById("cad-doc");

    if (perfil === "estabelecimento_cnpj") {
        labelNome.textContent = "Razão Social / Nome do Comércio";
        inputNome.placeholder = "Ex: Padaria Silva LTDA";
        labelDoc.textContent = "CNPJ";
        inputDoc.placeholder = "00.000.000/0001-00";
        labelData.textContent = "Data de Criação / Abertura";
    } else if (perfil === "estabelecimento_cpf") {
        labelNome.textContent = "Nome do Responsável / Comércio";
        inputNome.placeholder = "Ex: João Silva";
        labelDoc.textContent = "CPF";
        inputDoc.placeholder = "000.000.000-00";
        labelData.textContent = "Data de Nascimento";
    } else {
        labelNome.textContent = "Nome Completo";
        inputNome.placeholder = "Digite o nome completo";
        labelDoc.textContent = "CPF";
        inputDoc.placeholder = "000.000.000-00";
        labelData.textContent = "Data de Nascimento";
    }
}

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
    if (!senha || senha.length < 6) {
        mostrarErro("erro-senha", "A senha deve ter no mínimo 6 caracteres.");
        return;
    }

    const submitBtn = e.target.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.textContent = "Entrando...";

    try {
        const res = await fetch(`${API_URL}/auth/login`, {
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

document.getElementById("form-cadastro").addEventListener("submit", async (e) => {
    e.preventDefault();
    limparErros("erro-cadastro-geral");

    const perfilSelect = document.getElementById("cad-perfil").value;
    const nome = document.getElementById("cad-nome").value.trim();
    const data = document.getElementById("cad-data").value;
    const senha = document.getElementById("cad-senha").value;
    const documento = onlyDigits(document.getElementById("cad-doc").value);

    // Validações básicas
    if (!nome || nome.length < 2) {
        mostrarErro("erro-cadastro-geral", "Informe um nome válido.");
        return;
    }

    if (perfilSelect === "estabelecimento_cnpj" && documento.length !== 14) {
        mostrarErro("erro-cadastro-geral", "CNPJ inválido. Digite 14 dígitos.");
        return;
    }
    if ((perfilSelect === "cliente" || perfilSelect === "estabelecimento_cpf") && documento.length !== 11) {
        mostrarErro("erro-cadastro-geral", "CPF inválido. Digite 11 dígitos.");
        return;
    }
// Converte para o enum do backend
// cliente → CLIENTE | estabelecimento_cnpj → DONO | estabelecimento_cpf → DONO
    const perfilBackend = perfilSelect === "cliente" ? "CLIENTE" : "DONO";
    if (!data) {
        mostrarErro("erro-cadastro-geral", "Informe a data.");
        return;
    }

    if (!senha || senha.length < 6) {
        mostrarErro("erro-cadastro-geral", "A senha deve ter no mínimo 6 caracteres.");
        return;
    }

    const submitBtn = e.target.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    submitBtn.textContent = "Cadastrando...";

    try {
        const perfil = perfilSelect === "estabelecimento" ? "DONO" : "CLIENTE";

        const res = await fetch(`${API_URL}/auth/cadastro`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, documento, senha, perfil: perfilBackend })
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.erro || "Erro ao cadastrar. Tente novamente.");
        }

        // Volta para o login com mensagem de sucesso
        alternarTelas();
        mostrarErro("erro-login-geral", "✅ " + data.mensagem + " Faça login para continuar.");

    } catch (err) {
        mostrarErro("erro-cadastro-geral", err.message || "Erro ao cadastrar. Tente novamente.");
        submitBtn.disabled = false;
        submitBtn.textContent = "Finalizar Cadastro";
    }
});