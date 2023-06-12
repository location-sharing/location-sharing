import { Link } from "react-router-dom";
import Heading from "../../components/base/Heading";
import NavItem from "../../components/base/NavItem";
import { LINKS, LinkType } from "../../router/router";
import useAuth from "../../services/auth";
import LogoutNavItem from "../../components/auth/LogoutNavItem";

export default function PageHeader() {

  const { user } = useAuth()

  const loggedInLinks = () => {
    return (
      <nav className='flex flex-row justify-evenly gap-3 items-center flex-wrap'>
        <NavItem link={LINKS[LinkType.HOME].build()}>Home</NavItem>
        <NavItem link={LINKS[LinkType.GROUPS].build()}>Groups</NavItem>
        <LogoutNavItem/>
      </nav>
    )
  }

  const notLoggedInLinks = () => {
    return (
      <nav className='flex flex-row justify-evenly gap-3 items-center flex-wrap'>
        <NavItem link={LINKS[LinkType.HOME].build()}>Home</NavItem>
        <NavItem link={LINKS[LinkType.LOGIN].build()}>Sign In</NavItem>
        <NavItem link={LINKS[LinkType.REGISTER].build()}>Sign Up</NavItem>
      </nav>
    )
  }

  return (
    <header className="sticky top-0 flex flex-row justify-between items-center flex-wrap sm:w-11/12 mx-auto">
      <Link to={LINKS[LinkType.HOME].build()}>
        <Heading>Demo</Heading>
      </Link>
      { user? loggedInLinks() : notLoggedInLinks() }
    </header>
  )
}