import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { LINKS } from "../../router/router";
import Button from "../base/Button";
import Input from "../base/Input";
import InputLabel from "../base/InputLabel";
import ErrorAlert from "../base/alerts/ErrorAlert";
import { setAuth } from "../../services/auth";
import httpStatus from "http-status";
import LoginCredentials from "../../models/auth/LoginCredentials";
import { getErrorFromResponse } from "../../util/util";

const loginUrl = 'http://localhost:8082/api/user/authenticate'

const login = async (loginData: LoginCredentials) => fetch(
  loginUrl,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(loginData)
  }
)

export default function LoginForm() {

  const [username, setUsername] = useState<string>()
  const [password, setPassword] = useState<string>()

  const [error, setError] = useState<string>()

  const navigate = useNavigate()

  const handleSubmit: React.FormEventHandler = async event => {

    event.preventDefault();
    try {
      const credentials = {
        username, password
      }

      const response = await login(credentials as LoginCredentials)

      console.log(response)

      if (response.status === httpStatus.OK) {        
        await setAuth(response)
        // redirect to where the user came from
        navigate(LINKS.DASHBOARD)
      } else if (response.status === httpStatus.UNAUTHORIZED) {
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : "Username or password invalid." )
      } else {
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : "An error occurred.")      
      }
    } catch (error: any) {      
      setError("An error occurred.")      
    }
  }

  return (
    <div>
      { error ? <ErrorAlert title="Login error" message={error} onClose={ () => setError(undefined) }/> : null}

      <div className="mx-auto w-full bg-theme-bg-1 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0 ">
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
                <p className="text-sm text-gray-500 text-center">
                    Don’t have an account yet?
                    <Link to={LINKS.REGISTER} className="font-medium text-primary-600 hover:underline"> Sign up</Link>
                </p>
            </form>
        </div>
      </div>
    </div>
  )
}