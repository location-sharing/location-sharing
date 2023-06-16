import { useNavigate } from "react-router-dom";
import { LINKS, LinkType } from "../../router/router";
import useAuth from "../../services/auth";
import NavItem from "../base/NavItem";

export default function LogoutNavItem() {

  const { removeUser } = useAuth()
  const navigate = useNavigate()

  const logout = () => {
    removeUser()
    navigate(LINKS[LinkType.HOME].build())
  }

  return (
    <NavItem to="#">
      <button onClick={logout}>Sign Out</button>
    </NavItem>
  )
}