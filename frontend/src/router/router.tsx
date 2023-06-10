import { createBrowserRouter } from 'react-router-dom';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import { List } from '../components/base/List';

export const LINKS = {
  DASHBOARD: "/",
  LOGIN: "/login",
  REGISTER: "/register"
}

const router = createBrowserRouter([
  {
    path: "/",
    element: <List/>,
  },
  // {
  //   path: LINKS.DASHBOARD,
  //   element: <Dashboard/>,
  //   errorElement: <ErrorBoundary/>
  //   // children: []
  // },
  // {
  //   path: LINKS.LOGIN,
  //   element: <LoginPage/>
  // },
  // {
  //   path: LINKS.REGISTER,
  //   element: <RegisterPage/>
  // }
]);

export default router;