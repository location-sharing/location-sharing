import { useState } from "react";
import { Outlet } from "react-router-dom";
import AuthContext from "./context/AuthContext";
import PageHeader from "./pages/header/PageHeader";
import { AuthenticatedUser } from "./services/auth";

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
