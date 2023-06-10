import { Outlet } from "react-router-dom";
import Heading from './components/base/Heading';
import NavLink from './components/base/NavLink';
import { LINKS } from './router/router';

function App() {
  return (
    <div className='w-full'>
    <header className="sticky top-0 flex flex-row justify-between items-center sm:w-11/12 mx-auto">
      <Heading>Demo</Heading>
      <nav className='flex flex-row justify-evenly gap-x-3 items-center'>
        <NavLink link={LINKS.DASHBOARD}>Dashboard</NavLink>
        <NavLink link={LINKS.LOGIN}>Sign In</NavLink>
        <NavLink link={LINKS.REGISTER}>Sign Up</NavLink>
        <NavLink link={LINKS.GROUPS}>Groups</NavLink>
      </nav>
    </header>
    <Outlet/>
  </div>
  );
}

export default App;
