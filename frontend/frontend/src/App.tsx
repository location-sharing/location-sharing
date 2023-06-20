import { Outlet } from "react-router-dom";
import ErrorBoundary from "./components/error-boundary/ErrorBoundary";
import AuthContextProvider from "./context/AuthContext";
import NotificationProvider from "./context/NotificationContext";
import PageHeader from "./pages/header/PageHeader";
import NotificationPanel from "./pages/notifications/NotificationPanel";

function App() {

  return (
    <ErrorBoundary>
      <NotificationProvider>
          <AuthContextProvider>
              <NotificationPanel/>
              <PageHeader/>
              <Outlet/>
          </AuthContextProvider>
      </NotificationProvider>
    </ErrorBoundary>
  );
}

export default App;
