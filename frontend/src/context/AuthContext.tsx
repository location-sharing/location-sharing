import { AuthenticatedUser } from "../services/auth"
import { createContext } from "react"

export interface IAuthContext {
  user: AuthenticatedUser | undefined,
  setUser: (user: AuthenticatedUser | undefined) => void
}

const AuthContext = createContext<IAuthContext>({
  user: undefined,
  setUser: () => {}
})

export default AuthContext