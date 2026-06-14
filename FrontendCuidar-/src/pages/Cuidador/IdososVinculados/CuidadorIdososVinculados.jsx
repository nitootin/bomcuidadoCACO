import { useEffect, useMemo, useState } from "react";
import BcButton from "../../../components/Bcbutton/BcButton";
import BcFormModal, { BcFormModalRow, BcFormModalTextarea } from "../../../components/BcFormModal/BcFormModal";
import BcInput from "../../../components/Bcinput/BcInput";
import BcModal from "../../../components/BcModal/BcModal";
import BcTopbar from "../../../components/BcTopbar/BcTopbar";
import {
  IconeBusca,
  IconeIdosos,
  IconeSair,
  IconeSetaEsquerda,
} from "../../../components/icons/Icons";
import { cadastrarIdoso, listarIdososDoCuidador } from "../../../api/pessoasApi";
import "./CuidadorIdososVinculados.css";

function formatarCPF(valor = "") {
  const numeros = String(valor).replace(/\D/g, "").slice(0, 11);
  return numeros
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

function formatarTelefone(idoso) {
  const contato = idoso?.contato || {};
  const ddd = contato.ddd || idoso?.ddd;
  const telefone = contato.telefone || idoso?.telefone;

  if (!telefone) return "Nao informado";

  const numero = String(telefone).replace(/\D/g, "");
  const telefoneFormatado = numero.length > 8
    ? numero.replace(/(\d{5})(\d{4})$/, "$1-$2")
    : numero.replace(/(\d{4})(\d{4})$/, "$1-$2");

  return ddd ? `(${ddd}) ${telefoneFormatado}` : telefoneFormatado;
}

function normalizarBusca(valor = "") {
  return String(valor).toLowerCase().trim();
}

const FORM_IDOSO_INICIAL = {
  nome: "",
  cpf: "",
  ddd: "",
  telefone: "",
  observacoes: "",
};

function IdosoCard({ idoso }) {
  const inicial = String(idoso.nome || "?").charAt(0).toUpperCase();

  return (
    <article className="cuidador-idosos-card">
      <span className="cuidador-idosos-card__avatar">{inicial}</span>
      <div className="cuidador-idosos-card__content">
        <h3>{idoso.nome || "Idoso sem nome"}</h3>
        <dl>
          <div>
            <dt>CPF</dt>
            <dd>{formatarCPF(idoso.cpf) || "Nao informado"}</dd>
          </div>
          <div>
            <dt>Telefone</dt>
            <dd>{formatarTelefone(idoso)}</dd>
          </div>
        </dl>
      </div>
    </article>
  );
}

export default function CuidadorIdososVinculados({ onBack, onLogout }) {
  const [idosos, setIdosos] = useState([]);
  const [busca, setBusca] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [modalCadastro, setModalCadastro] = useState(false);
  const [formIdoso, setFormIdoso] = useState(FORM_IDOSO_INICIAL);
  const [erroCadastro, setErroCadastro] = useState("");
  const [salvandoCadastro, setSalvandoCadastro] = useState(false);

  async function carregarIdosos(ativo = true) {
    try {
      setCarregando(true);
      setErro("");
      const lista = await listarIdososDoCuidador();

      if (ativo) {
        setIdosos(Array.isArray(lista) ? lista : []);
      }
    } catch {
      if (ativo) {
        setErro("Nao foi possivel carregar as pessoas acompanhadas.");
      }
    } finally {
      if (ativo) {
        setCarregando(false);
      }
    }
  }

  useEffect(() => {
    carregarIdosos();
  }, []);

  const idososFiltrados = useMemo(() => {
    const termo = normalizarBusca(busca);
    const cpfBusca = busca.replace(/\D/g, "");

    if (!termo && !cpfBusca) return idosos;

    return idosos.filter((idoso) => {
      const nome = normalizarBusca(idoso.nome);
      const cpf = String(idoso.cpf || "").replace(/\D/g, "");
      return nome.includes(termo) || (cpfBusca && cpf.includes(cpfBusca));
    });
  }, [busca, idosos]);

  function abrirCadastro() {
    setFormIdoso(FORM_IDOSO_INICIAL);
    setErroCadastro("");
    setModalCadastro(true);
  }

  function fecharCadastro() {
    if (salvandoCadastro) return;
    setModalCadastro(false);
    setErroCadastro("");
    setFormIdoso(FORM_IDOSO_INICIAL);
  }

  function atualizarCampoIdoso(campo, valor) {
    setFormIdoso((atual) => ({ ...atual, [campo]: valor }));
    if (erroCadastro) setErroCadastro("");
  }

  function validarIdoso() {
    if (!formIdoso.nome.trim()) return "Informe o nome da pessoa.";
    if (formIdoso.cpf.replace(/\D/g, "").length !== 11) return "Informe um CPF valido.";
    if (formIdoso.ddd.replace(/\D/g, "").length < 2) return "Informe o DDD.";
    if (formIdoso.telefone.replace(/\D/g, "").length < 8) return "Informe um telefone valido.";
    return "";
  }

  async function handleCadastrarIdoso(evento) {
    evento.preventDefault();
    const erroValidacao = validarIdoso();
    if (erroValidacao) {
      setErroCadastro(erroValidacao);
      return;
    }

    try {
      setSalvandoCadastro(true);
      setErroCadastro("");
      await cadastrarIdoso(formIdoso);
      setModalCadastro(false);
      setFormIdoso(FORM_IDOSO_INICIAL);
      await carregarIdosos(true);
    } catch (error) {
      setErroCadastro(error.message || "Nao foi possivel cadastrar a pessoa.");
    } finally {
      setSalvandoCadastro(false);
    }
  }

  return (
    <div className="cuidador-idosos-page">
      <BcTopbar
        title="Pessoas acompanhadas"
        subtitle="Todos os idosos associados ao seu acompanhamento"
        actionLabel="Sair"
        actionIcon={<IconeSair />}
        onAction={onLogout}
      />

      <main className="cuidador-idosos-main">
        <button className="cuidador-idosos-voltar" type="button" onClick={onBack}>
          <IconeSetaEsquerda />
          Voltar ao painel
        </button>

        <section className="cuidador-idosos-header" aria-labelledby="usuarios-vinculados-titulo">
          <div>
            <span><IconeIdosos /></span>
            <div>
              <h1 id="usuarios-vinculados-titulo">Pessoas acompanhadas</h1>
              <p>{idosos.length} pessoa(s) vinculada(s) ao cuidador.</p>
            </div>
          </div>

          <div className="cuidador-idosos-header__acoes">
            <label className="cuidador-idosos-busca">
              <IconeBusca />
              <input
                type="search"
                value={busca}
                onChange={(event) => setBusca(event.target.value)}
                placeholder="Buscar por nome ou CPF"
              />
            </label>
            <BcButton type="button" fullWidth={false} onClick={abrirCadastro}>
              Adicionar idoso
            </BcButton>
          </div>
        </section>

        {erro && (
          <div className="cuidador-idosos-alerta" role="alert">
            {erro}
          </div>
        )}

        {carregando ? (
          <div className="cuidador-idosos-empty">
            <p>Carregando usuarios vinculados...</p>
          </div>
        ) : idososFiltrados.length > 0 ? (
          <section className="cuidador-idosos-lista" aria-label="Lista de usuarios vinculados">
            {idososFiltrados.map((idoso) => (
              <IdosoCard
                key={idoso.id || idoso.cpf || idoso.nome}
                idoso={idoso}
              />
            ))}
          </section>
        ) : (
          <div className="cuidador-idosos-empty">
            <span><IconeIdosos /></span>
            <p>{busca ? "Nenhuma pessoa encontrada." : "Nenhuma pessoa acompanhada ainda."}</p>
            <small>
              {busca
                ? "Tente buscar por outro nome ou CPF."
                : "Use o botao Adicionar idoso para cadastrar a primeira pessoa."}
            </small>
          </div>
        )}
      </main>

      <BcModal aberto={modalCadastro} onFechar={fecharCadastro}>
        <BcFormModal
          title="Adicionar idoso"
          subtitle="Cadastre uma pessoa para acompanhar diretamente pela sua conta."
          error={erroCadastro}
          onSubmit={handleCadastrarIdoso}
          className="cuidador-idosos-cadastro-modal"
        >
          <BcInput
            label="Nome"
            name="nome"
            type="text"
            placeholder="Nome completo"
            value={formIdoso.nome}
            onChange={(event) => atualizarCampoIdoso("nome", event.target.value)}
          />
          <BcInput
            label="CPF"
            name="cpf"
            type="text"
            placeholder="000.000.000-00"
            value={formatarCPF(formIdoso.cpf)}
            onChange={(event) => atualizarCampoIdoso("cpf", event.target.value.replace(/\D/g, "").slice(0, 11))}
            inputMode="numeric"
            maxLength={14}
          />
          <BcFormModalRow>
            <BcInput
              label="DDD"
              name="ddd"
              type="text"
              placeholder="11"
              value={formIdoso.ddd}
              onChange={(event) => atualizarCampoIdoso("ddd", event.target.value.replace(/\D/g, "").slice(0, 2))}
              inputMode="numeric"
              maxLength={2}
            />
            <BcInput
              label="Telefone"
              name="telefone"
              type="text"
              placeholder="999999999"
              value={formIdoso.telefone}
              onChange={(event) => atualizarCampoIdoso("telefone", event.target.value.replace(/\D/g, "").slice(0, 9))}
              inputMode="numeric"
              maxLength={9}
            />
          </BcFormModalRow>
          <BcFormModalTextarea
            id="observacoes"
            label="Observacoes"
            name="observacoes"
            placeholder="Alergias, cuidados importantes ou informacoes uteis"
            value={formIdoso.observacoes}
            onChange={(event) => atualizarCampoIdoso("observacoes", event.target.value)}
          />
          <div className="cuidador-idosos-cadastro-modal__acoes">
            <BcButton type="button" variant="ghost" fullWidth={false} onClick={fecharCadastro} disabled={salvandoCadastro}>
              Cancelar
            </BcButton>
            <BcButton type="submit" fullWidth={false} loading={salvandoCadastro}>
              Cadastrar
            </BcButton>
          </div>
        </BcFormModal>
      </BcModal>
    </div>
  );
}

