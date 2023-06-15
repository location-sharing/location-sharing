import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { LINKS, LinkType } from "../../router/router";
import Button from "../base/Button";
import Input from "../base/Input";
import InputLabel from "../base/InputLabel";
import ErrorAlert from "../base/alerts/ErrorAlert";
import httpStatus from "http-status";
import LoginCredentials from "../../models/auth/LoginCredentials";
import { getErrorFromResponse } from "../../util/util";
import useAuth from "../../services/auth";

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

  const { storeUser } = useAuth()
  const navigate = useNavigate()

  const handleSubmit: React.FormEventHandler = async event => {
    event.preventDefault();
    let errorMessage = "An error occurred."

    try {
      const credentials = {
        username, password
      }

      const response = await login(credentials as LoginCredentials)

      if (response.status === httpStatus.OK) {        
        await storeUser(response)
        navigate(LINKS[LinkType.GROUPS].build())
      } else {
        if (response.status === httpStatus.UNAUTHORIZED) {
          errorMessage = "Username or password invalid."
        }
        const errorResponse = await getErrorFromResponse(response)
        setError(errorResponse ? errorResponse.detail : errorMessage)
      }
    } catch (error: any) {      
      setError(errorMessage)      
    }
  }

  return (
    <div className="relative">
      { error ? 
        <div className="relative bottom-12 w-full">
          <ErrorAlert title="Login error" message={error} onClose={ () => setError(undefined) }/> 
        </div>
        :
        null
      }

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
                <Button btnType="basic" type="submit" className="w-full">Sign in</Button>
                <p className="text-sm text-gray-500 text-center">
                    Don’t have an account yet?
                    <Link to={LINKS[LinkType.REGISTER].build()} className="font-medium text-primary-600 hover:underline"> Sign up</Link>
                </p>
            </form>
        </div>
      </div>
    </div>
  )
}