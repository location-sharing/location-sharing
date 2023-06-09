const TOKEN_KEY = "authToken";

interface IAuthTokenBody {
  token: string
}

export async function setAuthToken(response: Response) {
  const body: IAuthTokenBody = await response.json()
  if (!body || !body.token) {
    throw Error("Malformed token")
  }

  sessionStorage.setItem(TOKEN_KEY, body.token)
}

export function getAuthToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function isAuthenticated(): boolean {
  const token = getAuthToken()
  return token !== null && token !== undefined
}

export function removeAuthToken() {
  sessionStorage.removeItem(TOKEN_KEY)
}