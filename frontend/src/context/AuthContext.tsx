import { AuthenticatedUser } from "../services/auth"
import { createContext, useState, PropsWithChildren } from "react"

export interface IAuthContext {
  user: AuthenticatedUser | undefined,
  setUser: (user: AuthenticatedUser | undefined) => void
}

export const AuthContext = createContext<IAuthContext>({
  user: undefined,
  setUser: () => {}
})

export default function AuthContextProvider(props: PropsWithChildren) {
  
  const [user, setUser] = useState<AuthenticatedUser>()

  return (
    <AuthContext.Provider value={{user, setUser}}>
      {props.children}
    </AuthContext.Provider>
  )
}