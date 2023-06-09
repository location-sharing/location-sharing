import { createBrowserRouter } from 'react-router-dom';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

export const LINKS = {
  DASHBOARD: "/",
  LOGIN: "/login",
  REGISTER: "/register"
}

const router = createBrowserRouter([
  {
    path: LINKS.DASHBOARD,
    element: <Dashboard/>,
    errorElement: <ErrorBoundary/>
    // children: []
  },
  {
    path: LINKS.LOGIN,
    element: <LoginPage/>
  },
  {
    path: LINKS.REGISTER,
    element: <RegisterPage/>
  }
]);

export default router;