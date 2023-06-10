import { createBrowserRouter } from 'react-router-dom';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import { List } from '../components/base/List';
import GroupsPage from '../pages/GroupsPage';

export const LINKS = {
  DASHBOARD: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  GROUPS: "/groups",
  GROUPDETAIL: "/groups/:groupId",
}

const router = createBrowserRouter([
  {
    path: LINKS.DASHBOARD,
    element: <Dashboard/>,
    errorElement: <ErrorBoundary/>
  },
  {
    path: LINKS.LOGIN,
    element: <LoginPage/>
  },
  {
    path: LINKS.REGISTER,
    element: <RegisterPage/>
  },
  {
    path: LINKS.GROUPS,
    element: <GroupsPage/>,
  },
]);

export default router;