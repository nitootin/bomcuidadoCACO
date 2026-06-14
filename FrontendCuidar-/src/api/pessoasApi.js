import { getAuthHeaders } from "./authApi";
import { API_BASE_URL } from "./env";

function somenteNumeros(valor = "") {
  return String(valor).replace(/\D/g, "");
}

function getUsuarioId() {
  return Number(localStorage.getItem("usuarioId") || sessionStorage.getItem("usuarioId"));
}

async function getErrorMessage(response, fallback) {
  const erro = await response.json().catch(() => ({}));
  if (response.status === 401) return "Sua sessao expirou ou o login nao foi encontrado.";
  if (response.status === 403) return "Seu perfil nao tem permissao para executar esta acao.";
  return erro.message || fallback;
}

async function requestApi(path, { method = "GET", dados, fallback } = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: getAuthHeaders(),
    body: dados ? JSON.stringify(dados) : undefined,
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, fallback));
  }

  return response.json().catch(() => null);
}

function conteudoPaginado(data) {
  return Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
}

function normalizarIdoso(dados) {
  return {
    nome: dados.nome,
    cpf: somenteNumeros(dados.cpf),
    observacoes: dados.observacoes,
    contatoId: dados.contatoId,
  };
}

function normalizarContato(dados) {
  const contato = {
    id: dados.contatoId || dados.contato?.id,
    ddd: somenteNumeros(dados.contato?.ddd || dados.ddd),
    telefone: somenteNumeros(dados.contato?.telefone || dados.telefone),
    cuidadorId: dados.cuidadorId,
  };
  if (dados.idosos) contato.idosos = dados.idosos;
  return contato;
}

export async function listarIdosos(page = 0, size = 100) {
  const data = await requestApi(`/idoso/listar_todos?page=${page}&size=${size}`, {
    fallback: "Erro ao buscar idosos.",
  });
  return conteudoPaginado(data);
}

export async function listarIdososDoCuidador(cuidadorId = getUsuarioId(), page = 0, size = 100) {
  const vinculosData = await requestApi(`/vinculo/cuidador/${cuidadorId}?page=${page}&size=${size}`, {
    fallback: "Erro ao buscar vinculos do cuidador.",
  });
  const vinculos = conteudoPaginado(vinculosData);
  if (vinculos.length === 0) return [];

  const idosos = await listarIdosos(0, 100);
  const idsVinculados = new Set(vinculos.map((v) => Number(v.idosoId)));
  return idosos
    .filter((i) => idsVinculados.has(Number(i.id)))
    .map((i) => ({ ...i, vinculo: vinculos.find((v) => Number(v.idosoId) === Number(i.id)) }));
}

export async function buscarIdosoPorCpf(cpf) {
  const cpfLimpo = somenteNumeros(cpf);
  if (cpfLimpo.length !== 11) return null;

  const response = await fetch(`${API_BASE_URL}/idoso/trazerdados/${cpfLimpo}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await getErrorMessage(response, "Erro ao buscar idoso pelo CPF."));
  return response.json().catch(() => null);
}

export async function cadastrarContato(dados) {
  return requestApi("/contato/cadastrar", {
    method: "POST",
    dados: normalizarContato(dados),
    fallback: "Erro ao cadastrar contato.",
  });
}

export async function atualizarContato(id, dados) {
  return requestApi(`/contato/atualizar/${id}`, {
    method: "PUT",
    dados: normalizarContato(dados),
    fallback: "Erro ao atualizar contato.",
  });
}

export async function deletarContato(id) {
  return requestApi(`/contato/deletar/${id}`, {
    method: "DELETE",
    fallback: "Erro ao deletar contato.",
  });
}

async function salvarContatoDoIdoso(dados) {
  if (dados.contatoId) return atualizarContato(dados.contatoId, dados);
  return cadastrarContato(dados);
}

async function salvarIdoso(dados) {
  return requestApi("/idoso/cadastrar", {
    method: "POST",
    dados: normalizarIdoso(dados),
    fallback: "Erro ao cadastrar idoso.",
  });
}

export async function cadastrarIdoso(dados) {
  const contato = await cadastrarContato(dados);
  try {
    return await salvarIdoso({ ...dados, contatoId: contato?.id });
  } catch (erro) {
    if (contato?.id) await deletarContato(contato.id).catch(() => null);
    throw erro;
  }
}

export async function atualizarIdoso(id, dados) {
  const contato = await salvarContatoDoIdoso(dados);
  return requestApi(`/idoso/atualizar/${id}`, {
    method: "PUT",
    dados: normalizarIdoso({ ...dados, contatoId: contato?.id || dados.contatoId }),
    fallback: "Erro ao atualizar idoso.",
  });
}

export async function deletarIdoso(id) {
  return requestApi(`/idoso/deletar/${id}`, {
    method: "DELETE",
    fallback: "Erro ao deletar idoso.",
  });
}

export async function listarVinculosPorIdoso(idosoId, page = 0, size = 100) {
  const data = await requestApi(`/vinculo/idoso/${idosoId}?page=${page}&size=${size}`, {
    fallback: "Erro ao buscar vinculos do idoso.",
  });
  return conteudoPaginado(data);
}

export async function criarVinculo({ cuidadorId, idosoId }) {
  return requestApi("/vinculo/cadastrar", {
    method: "POST",
    dados: { cuidadorId, idosoId },
    fallback: "Erro ao criar vinculo.",
  });
}

export async function deletarVinculo(id) {
  return requestApi(`/vinculo/deletar/${id}`, {
    method: "DELETE",
    fallback: "Erro ao deletar vinculo.",
  });
}

export async function definirVinculoEmergencia(vinculoId) {
  return requestApi(`/vinculo/${vinculoId}/emergencia`, {
    method: "PUT",
    fallback: "Erro ao definir vinculo de emergencia.",
  });
}
