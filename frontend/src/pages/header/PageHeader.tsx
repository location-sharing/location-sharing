import { Link } from "react-router-dom";
import Heading from "../../components/base/Heading";
import NavItem from "../../components/base/NavItem";
import { LINKS } from "../../router/router";
import useAuth from "../../services/auth";
import LogoutNavItem from "../../components/auth/LogoutNavItem";

export default function PageHeader() {

  const { user, removeUser } = useAuth()

  const loggedInLinks = () => {
    return (
      <nav className='flex flex-row justify-evenly gap-x-3 items-center'>
        <NavItem link={LINKS.HOME}>Home</NavItem>
        <NavItem link={LINKS.DASHBOARD}>Dashboard</NavItem>
        <NavItem link={LINKS.GROUPS}>Groups</NavItem>
        <LogoutNavItem/>
      </nav>
    )
  }

  const notLoggedInLinks = () => {
    return (
      <nav className='flex flex-row justify-evenly gap-x-3 items-center'>
        <NavItem link={LINKS.HOME}>Home</NavItem>
        <NavItem link={LINKS.LOGIN}>Sign In</NavItem>
        <NavItem link={LINKS.REGISTER}>Sign Up</NavItem>
      </nav>
    )
  }

  return (
    <header className="sticky top-0 flex flex-row justify-between items-center sm:w-11/12 mx-auto">
      <Link to={LINKS.HOME}>
        <Heading>Demo</Heading>
      </Link>
      <nav className='flex flex-row justify-evenly gap-x-3 items-center'>
        {/* <NavItem link={LINKS.HOME}>Home</NavItem>

        { user ? <NavItem link={LINKS.DASHBOARD}>Dashboard</NavItem> : null }
        { user ? <NavItem link={LINKS.GROUPS}>Groups</NavItem> : null }
        { user ? 
          <NavItem link='#'>
            <LogoutButton/>
          </NavItem> 
          : 
          null 
        }

        { user !== undefined ? <NavItem link={LINKS.LOGIN}>Sign In</NavItem> : null }
        { user !== undefined ? <NavItem link={LINKS.REGISTER}>Sign Up</NavItem> : null } */}

        { user? loggedInLinks() : notLoggedInLinks() }
      </nav>
    </header>
  )
}