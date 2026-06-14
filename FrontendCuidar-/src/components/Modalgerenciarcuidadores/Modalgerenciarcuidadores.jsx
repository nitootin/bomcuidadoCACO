/**
 * ModalGerenciarCuidadores
 *
 * Props:
 *   aberto      : boolean
 *   onFechar    : fn
 *   idoso       : { id, nome }
 *   cuidadores  : Array  â€” lista de cuidadores da instituiÃ§Ã£o
 */
import { useCallback, useEffect, useState } from "react";
import BcButton from "../Bcbutton/BcButton";
import BcModal from "../BcModal/BcModal";
import BcToast, { useBcToast } from "../BcToast/BcToast";
import { listarVinculosPorIdoso, criarVinculo, deletarVinculo } from "../../api/pessoasApi";
import "./Modalgerenciarcuidadores.css";

function inicial(nome = "") {
  return String(nome).charAt(0).toUpperCase() || "?";
}

function formatarCPF(valor = "") {
  const n = String(valor).replace(/\D/g, "").slice(0, 11);
  return n
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

function formatarTelefone(valor = "") {
  const n = String(valor).replace(/\D/g, "").slice(0, 9);
  return n.replace(/(\d{5})(\d{0,4})$/, "$1-$2").replace(/-$/, "");
}

export default function ModalGerenciarCuidadores({ aberto, onFechar, idoso, cuidadores = [] }) {
  const { toastProps, mostrarToast } = useBcToast();
  const [vinculos, setVinculos]         = useState([]);
  const [carregando, setCarregando]     = useState(false);
  const [salvando, setSalvando]         = useState(false);
  // set de cuidadorId com vÃ­nculo ativo (do servidor)
  const [vinculados, setVinculados]     = useState(new Set());
  // mapa cuidadorId â†’ vinculoId (para deletar)
  const [mapaVinculos, setMapaVinculos] = useState({});
  // seleÃ§Ãµes locais (estado local antes de confirmar)
  const [selecoesLocais, setSelecoesLocais] = useState(new Set());

  const carregarVinculos = useCallback(async () => {
    if (!idoso?.id) return;
    setCarregando(true);
    try {
      const lista = await listarVinculosPorIdoso(idoso.id);
      setVinculos(lista);
      const ids = new Set(lista.map((v) => Number(v.cuidadorId)));
      const mapa = {};
      lista.forEach((v) => { mapa[Number(v.cuidadorId)] = v.id; });
      setVinculados(ids);
      setMapaVinculos(mapa);
      // Inicializa seleÃ§Ãµes locais com os vÃ­nculos atuais
      setSelecoesLocais(new Set(ids));
    } catch (err) {
      mostrarToast("erro", "Erro ao carregar vÃ­nculos", err.message);
    } finally {
      setCarregando(false);
    }
  }, [idoso?.id, mostrarToast]);

  useEffect(() => {
    if (aberto) carregarVinculos();
  }, [aberto, carregarVinculos]);

  function handleToggleLocal(cuidador) {
    const cuidadorId = Number(cuidador.id);
    setSelecoesLocais((ant) => {
      const nova = new Set(ant);
      if (nova.has(cuidadorId)) {
        nova.delete(cuidadorId);
      } else {
        nova.add(cuidadorId);
      }
      return nova;
    });
  }

  async function handleConcluir() {
    setSalvando(true);
    try {
      // Calcula diferenÃ§as
      const adicionados = Array.from(selecoesLocais).filter((id) => !vinculados.has(id));
      const removidos = Array.from(vinculados).filter((id) => !selecoesLocais.has(id));

      // Se nÃ£o hÃ¡ mudanÃ§as, apenas fecha
      if (adicionados.length === 0 && removidos.length === 0) {
        onFechar();
        return;
      }

      // Cria novos vÃ­nculos
      for (const cuidadorId of adicionados) {
        await criarVinculo({ cuidadorId, idosoId: idoso.id });
      }

      // Remove vÃ­nculos
      for (const cuidadorId of removidos) {
        const vinculoId = mapaVinculos[cuidadorId];
        if (vinculoId) {
          await deletarVinculo(vinculoId);
        }
      }

      // Recarrega os vÃ­nculos
      await carregarVinculos();

      // Mostra um Ãºnico toast com o resumo
      let mensagem = "";
      if (adicionados.length > 0 && removidos.length > 0) {
        mensagem = `${adicionados.length} cuidador(es) vinculado(s) e ${removidos.length} desvinculado(s) com sucesso.`;
      } else if (adicionados.length > 0) {
        mensagem = `${adicionados.length} cuidador(es) vinculado(s) com sucesso.`;
      } else if (removidos.length > 0) {
        mensagem = `${removidos.length} cuidador(es) desvinculado(s) com sucesso.`;
      }

      if (mensagem) {
        mostrarToast("sucesso", "VÃ­nculos atualizados", mensagem);
      }

      onFechar();
    } catch (err) {
      mostrarToast("erro", "Erro ao atualizar vÃ­nculos", err.message);
    } finally {
      setSalvando(false);
    }
  }

  const totalAutorizados = selecoesLocais.size;

  return (
    <>
      <BcToast {...toastProps} />
      <BcModal aberto={aberto} onFechar={onFechar}>
        <div className="mgc-wrap">
          {/* CabeÃ§alho */}
          <div className="mgc-header">
            <div className="mgc-header__avatar">
              {inicial(idoso?.nome)}
            </div>
            <div>
              <h2 className="mgc-header__titulo">Gerenciar Cuidadores</h2>
              <p className="mgc-header__subtitulo">{idoso?.nome}</p>
            </div>
          </div>

          {/* InstruÃ§Ãµes */}
          <div className="mgc-instrucoes">
            <strong>InstruÃ§Ãµes:</strong> Selecione os cuidadores que terÃ£o acesso Ã s
            informaÃ§Ãµes deste idoso. Os cuidadores marcados poderÃ£o visualizar dados
            e gerenciar medicamentos.
          </div>

          {/* Contador */}
          <p className="mgc-contador">
            Total de cuidadores: {cuidadores.length} | Autorizados: {totalAutorizados}
          </p>

          {/* Lista de cuidadores */}
          <div className="mgc-lista">
            {carregando ? (
              <p className="mgc-vazio">Carregando...</p>
            ) : cuidadores.length === 0 ? (
              <p className="mgc-vazio">Nenhum cuidador cadastrado na instituiÃ§Ã£o.</p>
            ) : (
              cuidadores.map((c) => {
                const ativo = selecoesLocais.has(Number(c.id));
                return (
                  <label
                    key={c.id}
                    className={`mgc-item ${ativo ? "mgc-item--ativo" : ""}`}
                  >
                    <input
                      type="checkbox"
                      className="mgc-item__check"
                      checked={ativo}
                      disabled={salvando}
                      onChange={() => handleToggleLocal(c)}
                    />
                    <div className="mgc-item__avatar">{inicial(c.nome)}</div>
                    <div className="mgc-item__info">
                      <strong>{c.nome}</strong>
                      <span>CPF: {formatarCPF(c.cpf)}</span>
                      {c.contato && (
                        <span>
                          Tel: ({c.contato.ddd}) {formatarTelefone(c.contato.telefone)}
                        </span>
                      )}
                    </div>
                  </label>
                );
              })
            )}
          </div>

          {/* Footer */}
          <div className="mgc-footer">
            <p className="mgc-footer__info">
              {totalAutorizados} cuidador(es) autorizado(s)
            </p>
            <BcButton onClick={handleConcluir} fullWidth={false} loading={salvando}>
              Concluir
            </BcButton>
          </div>
        </div>
      </BcModal>
    </>
  );
}
