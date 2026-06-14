import { useState } from "react";
import BcButton from "../../components/Bcbutton/BcButton";
import BcInput from "../../components/Bcinput/BcInput";
import BcPasswordStrength from "../../components/BcPasswordStrength/BcPasswordStrength";
import BcToast, { useBcToast } from "../../components/BcToast/BcToast";
import { cadastrarCuidador } from "../../api/authApi";
import { formatarCPF } from "../../utils/validacaoDocumento";
import AuthLayout from "./AuthLayout";
import "./LoginPage.css";

const cadastroHighlights = [
  {
    icon: "+",
    title: "Conta pessoal",
    description: "Crie sua conta de cuidador para começar a acompanhar pessoas.",
  },
  {
    icon: "[]",
    title: "Dados centralizados",
    description: "Organize idosos, contatos, prescrições e alertas no mesmo ambiente.",
  },
  {
    icon: "OK",
    title: "Acesso protegido",
    description: "Sua senha segue regras de segurança antes de liberar a conta.",
  },
];

function validarSenha(senha) {
  if (senha.length < 8) return "Mínimo de 8 caracteres.";
  if (!/[A-Z]/.test(senha)) return "Deve conter ao menos uma letra maiúscula.";
  if (!/[a-z]/.test(senha)) return "Deve conter ao menos uma letra minúscula.";
  if (!/[0-9]/.test(senha)) return "Deve conter ao menos um número.";
  if (!/[^A-Za-z0-9]/.test(senha)) return "Deve conter ao menos um caractere especial.";
  return null;
}

export default function CadastroPage({ onBackToLogin }) {
  const { toastProps, mostrarToast, fecharToast } = useBcToast();
  const [form, setForm] = useState({
    nome: "",
    cpf: "",
    email: "",
    ddd: "",
    telefone: "",
    senha: "",
    confirmarSenha: "",
  });
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  function atualizarCampo(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }));
    if (erro) setErro("");
  }

  function validar() {
    if (!form.nome.trim()) return "Informe seu nome.";
    if (form.cpf.replace(/\D/g, "").length !== 11) return "Informe um CPF válido.";
    if (!form.email.trim()) return "Informe seu email.";
    if (form.ddd.replace(/\D/g, "").length < 2) return "Informe o DDD.";
    if (form.telefone.replace(/\D/g, "").length < 8) return "Informe um telefone válido.";
    const erroSenha = validarSenha(form.senha);
    if (erroSenha) return erroSenha;
    if (form.senha !== form.confirmarSenha) return "As senhas não coincidem.";
    return "";
  }

  async function handleSubmit(event) {
    event.preventDefault();
    fecharToast();
    const erroValidacao = validar();
    if (erroValidacao) {
      setErro(erroValidacao);
      return;
    }

    try {
      setLoading(true);
      await cadastrarCuidador(form);
      mostrarToast("sucesso", "Cadastro criado", "Sua conta foi criada. Faça login como cuidador para continuar.");
      setTimeout(() => onBackToLogin?.(), 1200);
    } catch (error) {
      setErro(error.message || "Não foi possível criar o cadastro.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      eyebrow="Cadastro de cuidador"
      title="Crie sua conta pessoal de cuidado."
      description="Cadastre seus dados para acompanhar pessoas idosas com rotinas, contatos e prescrições em um só lugar."
      highlights={cadastroHighlights}
    >
      <BcToast {...toastProps} />
      <div className="login-card">
        <div className="login-card__header">
          <span className="login-card__tag">Cadastro</span>
          <h2>Comece pelo seu acesso</h2>
          <p>Informe seus dados de cuidador para criar uma conta pessoal.</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit} noValidate>
          {erro ? <div className="login-form__message login-form__message--error">{erro}</div> : null}
          <BcInput
            label="Nome completo"
            name="nome"
            type="text"
            placeholder="Seu nome"
            value={form.nome}
            onChange={(event) => atualizarCampo("nome", event.target.value)}
            autoComplete="name"
          />
          <BcInput
            label="CPF"
            name="cpf"
            type="text"
            placeholder="000.000.000-00"
            value={form.cpf}
            onChange={(event) => atualizarCampo("cpf", formatarCPF(event.target.value).slice(0, 14))}
            inputMode="numeric"
            maxLength={14}
          />
          <BcInput
            label="Email"
            name="email"
            type="email"
            placeholder="seuemail@exemplo.com"
            value={form.email}
            onChange={(event) => atualizarCampo("email", event.target.value)}
            autoComplete="email"
          />
          <div className="login-form__split">
            <BcInput
              label="DDD"
              name="ddd"
              type="text"
              placeholder="11"
              value={form.ddd}
              onChange={(event) => atualizarCampo("ddd", event.target.value.replace(/\D/g, "").slice(0, 2))}
              inputMode="numeric"
              maxLength={2}
            />
            <BcInput
              label="Telefone"
              name="telefone"
              type="text"
              placeholder="99999-9999"
              value={form.telefone}
              onChange={(event) => atualizarCampo("telefone", event.target.value.replace(/\D/g, "").slice(0, 9))}
              inputMode="numeric"
              maxLength={9}
            />
          </div>
          <BcInput
            label="Senha"
            name="senha"
            type="password"
            placeholder="Crie uma senha forte"
            value={form.senha}
            onChange={(event) => atualizarCampo("senha", event.target.value)}
            autoComplete="new-password"
            hint={<BcPasswordStrength password={form.senha} />}
          />
          <BcInput
            label="Confirmar senha"
            name="confirmarSenha"
            type="password"
            placeholder="Repita a senha"
            value={form.confirmarSenha}
            onChange={(event) => atualizarCampo("confirmarSenha", event.target.value)}
            autoComplete="new-password"
          />
          <BcButton type="submit" loading={loading}>Criar cadastro</BcButton>
        </form>

        <div className="login-card__footer">
          <span>Já tem conta?</span>
          <button type="button" className="login-form__link" onClick={onBackToLogin}>
            Voltar para o login
          </button>
        </div>
      </div>
    </AuthLayout>
  );
}
