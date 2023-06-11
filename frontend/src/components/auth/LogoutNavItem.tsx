import { LINKS } from "../../router/router";
import useAuth from "../../services/auth";
import Button from "../base/Button";
import { useNavigate } from "react-router-dom"
import NavItem from "../base/NavItem";

export default function LogoutNavItem() {

  const { removeUser } = useAuth()
  const navigate = useNavigate()

  const logout = () => {
    removeUser()
    navigate(LINKS.HOME)
  }

  return (
    <NavItem link="#">
      <button onClick={logout}>Sign Out</button>
    </NavItem>
  )
}