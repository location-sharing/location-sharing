import jwtDecode, { JwtPayload } from "jwt-decode";
import { useContext, useEffect } from "react";
import {AuthContext} from "../context/AuthContext";
import AuthTokenResponse from "../models/auth/AuthToken";
import LoginCredentials from "../models/auth/LoginCredentials";
import UserCreate from "../models/user/UserCreate";


const AUTH_USER_KEY = "authUser";

interface AuthToken extends JwtPayload {
  userId: string,
  username: string
}

export interface AuthenticatedUser { 
  token: string,
  userId: string,
  username: string
}

async function getAuthFromResponse(response: Response): Promise<AuthenticatedUser> {
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
  
  return {
    token: token,
    userId: payload.userId,
    username: payload.username
  } as AuthenticatedUser;
}

// hook which mutates the auth context state
function useUserAuth() {

  const { user, setUser } = useContext(AuthContext)

  useEffect(() => {
    // try to fetch the user from sessionStorage
    const userString = sessionStorage.getItem(AUTH_USER_KEY)
    if (!userString) {
      return
    }

    const userAuth: AuthenticatedUser = JSON.parse(userString)
    setUser(userAuth)
  }, [setUser])

  const setUserFromResponse = async (response: Response) => {
    const user = await getAuthFromResponse(response)
    sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
    setUser(user)
  }

  const removeUser = () => {
    sessionStorage.removeItem(AUTH_USER_KEY)
    setUser(undefined)
  }

  return { user, setUserFromResponse, removeUser };
}

export default function useAuth() {
  const { user, setUserFromResponse, removeUser } = useUserAuth()

  const storeUser = async (response: Response) => setUserFromResponse(response)

  return { user, storeUser, removeUser };
}


const loginUrl = `${process.env.REACT_APP_API_USER_SERVICE}/api/user/authenticate`
export const login = async (loginData: LoginCredentials) => fetch(
  loginUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(loginData)
  }
)

const registerUrl = `${process.env.REACT_APP_API_USER_SERVICE}/api/user`
export const register = async (data: UserCreate) => fetch(
  registerUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  }
)