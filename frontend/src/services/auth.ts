import AuthTokenResponse from "../models/auth/AuthToken";
import jwtDecode, { JwtPayload } from "jwt-decode";

const AUTH_USER_KEY = "authUser";

interface AuthToken extends JwtPayload {
  userId: string,
  username: string
}

interface AuthenticatedUser { 
  token: string,
  userId: string,
  username: string
}

export async function setAuth(response: Response) {
  const { token } : AuthTokenResponse = await response.json()
  if (!token) {
    throw Error("Malformed token response")
  }

  let payload;
  try {
    payload = jwtDecode<AuthToken>(token)
  } catch (err) {
    throw Error("Error while parsing auth token")
  }

  const user : AuthenticatedUser = {
    token: token,
    userId: payload.userId,
    username: payload.username
  }
  sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
}

export function removeAuth() {
  sessionStorage.removeItem(AUTH_USER_KEY)
}

export function getAuth(): AuthenticatedUser | undefined {
  const authString = sessionStorage.getItem(AUTH_USER_KEY)
  if (!authString) {
    return undefined
  }
  return JSON.parse(authString)
}

export function getAuthToken(): string | undefined {
  const authString = sessionStorage.getItem(AUTH_USER_KEY)
  if (!authString) {
    return undefined
  }
  const auth: AuthenticatedUser = JSON.parse(authString)
  return auth.token
}

export function isAuthenticated(): boolean {
  return getAuth() !== undefined
}