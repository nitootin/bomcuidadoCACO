import { getAuthHeaders } from "./authApi";
import { API_BASE_URL } from "./env";

async function requestGet(path) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data.message || "Erro ao buscar dados do perfil.");
  }

  return data;
}

export function buscarPerfilUsuario() {
  const id = localStorage.getItem("usuarioId") || sessionStorage.getItem("usuarioId");
  const perfil = localStorage.getItem("perfil") || sessionStorage.getItem("perfil");

  if (perfil === "CUIDADOR") {
    return requestGet(`/cuidador/listar/${id}`);
  }

  return Promise.reject(new Error("Perfil nao identificado."));
}
