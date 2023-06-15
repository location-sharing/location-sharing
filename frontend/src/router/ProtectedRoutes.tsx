import useAuth from "../services/auth"
import { Outlet, Link } from "react-router-dom"
import { LINKS, LinkType } from "./router";
import Heading from "../components/base/Heading";
import Button from "../components/base/Button";

export default function ProtectedRoutes() {
  const { user } = useAuth();

  if (!user) {
    return (
      <div className="flex flex-col items-center gap-y-4 mt-36">
        <Heading className="text-">It seems that you are not logged in.</Heading>
        <Link to={LINKS[LinkType.LOGIN].build()}>
          <Button btnType="basic">Sign In</Button>
        </Link>
      </div>
    )
  } 

  return <Outlet/>
}