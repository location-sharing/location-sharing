import Button from "../base/Button";
import Input from "../base/Input";
import InputLabel from "../base/InputLabel";

export default function LoginForm() {
  return (
    <div className="mx-auto w-full bg-theme-bg-1 rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
      <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
          <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl">
              Sign in to your account
          </h1>
          <form className="space-y-4 md:space-y-6" action="#">
              <div>
                  <InputLabel htmlFor="email">Your email</InputLabel>
                  <Input name="email" type="email" id="email" required placeholder="name@company.com"/>
              </div>
              <div>
                  <InputLabel htmlFor="password">Password</InputLabel>
                  <Input name="password" type="password" id="password" required placeholder="••••••••"/>
              </div>
              <Button type="submit">Sign in</Button>
              <p className="text-sm text-gray-500">
                  Don’t have an account yet? <a href="#" className="font-medium text-primary-600 hover:underline">Sign up</a>
              </p>
          </form>
      </div>
    </div>
  )
}