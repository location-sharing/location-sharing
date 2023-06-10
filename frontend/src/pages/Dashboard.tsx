import Heading from "../components/base/Heading"
import { isAuthenticated } from "../services/auth"
import LoginPage from "./LoginPage"

export default function Dashboard() {

  if (!isAuthenticated()) {
    return <LoginPage/>
  }
  
  return (
    <div>
      <Heading>Dashboard will be here (eventually)</Heading>
    </div>
  )
}