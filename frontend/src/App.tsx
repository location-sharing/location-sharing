import { useState } from "react";
import { Outlet, Link } from "react-router-dom";
import Heading from './components/base/Heading';
import NavItem from "./components/base/NavItem";
import AuthContext from "./context/AuthContext";
import { LINKS } from './router/router';
import { AuthenticatedUser } from "./services/auth";
import Header from "./pages/header/PageHeader";
import PageHeader from "./pages/header/PageHeader";

function App() {

  const [user, setUser] = useState<AuthenticatedUser>()

  return (

    <AuthContext.Provider value={{ user, setUser }}>
        <PageHeader/>
        <Outlet/>
    </AuthContext.Provider>

  );
}

export default App;
