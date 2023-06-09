import { isAuthenticated } from "../services/auth"
import LoginPage from "./LoginPage"

export default function Dashboard() {

  if (!isAuthenticated()) {
    return <LoginPage/>
  }
  
  return (
    <h1 className="text-center mx-auto font-bold text-xl">Dashboard will be here (eventually)</h1>
  )
}