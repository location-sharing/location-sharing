import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { LINKS } from "../../router/router";
import Button from "../base/Button";
import Input from "../base/Input";
import InputLabel from "../base/InputLabel";
import ErrorAlert from "../base/alerts/ErrorAlert";
import { removeAuthToken } from "../../services/auth";


interface RegisterData {
  username?: string,
  email?: string,
  password?: string,
}

const registerUrl = 'http://localhost:8082/api/user'

const register = async (RegisterData: RegisterData) => fetch(
  registerUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(RegisterData)
  }
)

export default function RegisterForm() {

  const [username, setUsername] = useState<string>()
  const [email, setEmail] = useState<string>()
  const [password, setPassword] = useState<string>()

  const [error, setError] = useState<string>()

  const navigate = useNavigate()

  const handleSubmit: React.FormEventHandler = async event => {

    event.preventDefault();
    try {
      const credentials: RegisterData = {
        username, email, password
      }

      const response = await register(credentials)
      console.log(response);
      console.log(await response.json());

      if (response.ok) {    
        removeAuthToken()
        navigate(LINKS.DASHBOARD)
      } else {
        setError("An error occurred.")      
      }
    } catch (error: any) {
      setError("An error occurred.")
    }
  }

  return (
    <div>
      { error ? <ErrorAlert title="Registration error" message={error} onClose={ () => setError(undefined) }/> : null}

      <div className="mx-auto w-full bg-theme-bg-1 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0 ">
        <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl">
                Sign up
            </h1>
            <form className="space-y-4 md:space-y-6"
              onSubmit={handleSubmit}
            >
                <div>
                    <InputLabel htmlFor="username">Username</InputLabel>
                    <Input name="username" type="text" id="username" required placeholder="username"
                    onChange={ e => setUsername(e.target.value) }
                    />
                </div>
                <div>
                    <InputLabel htmlFor="email">Email</InputLabel>
                    <Input name="email" type="email" id="email" required placeholder="name@company.com"
                    onChange={ e => setEmail(e.target.value) }
                    />
                </div>
                <div>
                    <InputLabel htmlFor="password">Password</InputLabel>
                    <Input name="password" type="password" id="password" required placeholder="••••••••"
                      onChange={ e => setPassword(e.target.value) }
                    />
                </div>
                <Button type="submit">Register</Button>
                <p className="text-sm text-gray-500 text-center">
                  Already have an account? 
                  <Link to={LINKS.LOGIN} className="font-medium text-primary-600 hover:underline"> Sign in</Link>
                </p>
            </form>
        </div>
      </div>
    </div>

  )
}