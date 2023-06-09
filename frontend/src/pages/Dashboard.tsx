import LoginForm from "../components/login-form/LoginForm"

export default function Dashboard() {

  if (!sessionStorage.getItem("token")) {
    return <LoginForm />
  }
  
  return (
    <h1 className="text-center mx-auto font-bold text-xl">Dashboard will be here (eventually)</h1>
  )
}