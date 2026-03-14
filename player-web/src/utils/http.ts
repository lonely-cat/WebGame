const API_BASE = "/api";

export async function http<T>(url: string, options?: RequestInit): Promise<T> {
  const token = localStorage.getItem("webgame_token");
  const response = await fetch(`${API_BASE}${url}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options?.headers ?? {})
    },
    ...options
  });
  return response.json() as Promise<T>;
}
