import Button from "../base/Button";
import Input from "../base/Input";
import InputLabel from "../base/InputLabel";
import { useState } from "react"
import ErrorAlert from "../base/alerts/ErrorAlert";

interface LoginData {
  username?: string,
  password?: string,
}

const loginUrl = 'http://localhost:8081/api/users/authenticate'

const login = async (loginData: LoginData) => fetch(
  loginUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(loginData)
  }
)

class LoginError {
  static title = "Login error";
  message: string;

  constructor(message: string) {
    this.message = message
  }
}

export default function LoginForm() {

  const [username, setUsername] = useState<string>()
  const [password, setPassword] = useState<string>()

  const [error, setError] = useState<string>()

  const handleSubmit: React.FormEventHandler = async event => {

    console.log(`submit ${event}`);

    event.preventDefault();
    try {
      if (!username || !password) {
        setError("Username and password must not be empty.")
      }

      const credentials: LoginData = {
        username, password
      }

      const response = await login(credentials)
      console.log(`${response}`);
      
      
      sessionStorage.setItem("token", "dummy")
      

    } catch (error: any) {
      setError("An unknown error occurred.")
    }
  }

  return (
    <div className="mx-auto w-full">
      { error ? <ErrorAlert title="Login error" message={error} onClose={ () => setError(undefined) }/> : null}

      <div className="mx-auto w-full bg-theme-bg-1 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
        <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl">
                Sign in to your account
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
                    <InputLabel htmlFor="password">Password</InputLabel>
                    <Input name="password" type="password" id="password" required placeholder="••••••••"
                      onChange={ e => setPassword(e.target.value) }
                    />
                </div>
                <Button type="submit">Sign in</Button>
                <p className="text-sm text-gray-500">
                    Don’t have an account yet? <a href="#" className="font-medium text-primary-600 hover:underline">Sign up</a>
                </p>
            </form>
        </div>
      </div>
    </div>

  )
}