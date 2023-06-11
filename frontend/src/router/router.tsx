import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import GroupDetailPage from '../pages/GroupDetailPage';
import GroupsPage from '../pages/GroupsPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ProtectedRoutes from './ProtectedRoutes';
import HomePage from '../pages/HomePage';
import CreateGroupsPage from '../pages/CreateGroupPage';

export const LINKS = {
  HOME: "/",
  DASHBOARD: "/dashboard",
  LOGIN: "/login",
  REGISTER: "/register",
  GROUPS: "/groups",
  GROUP_CREATE: "/groups/new",
  GROUP_DETAIL: "/groups/:groupId",
}

const router = createBrowserRouter([
  {
    element: <App/>,
    children: [
      {
        element: <ProtectedRoutes/>,
        children: [
          {
            path: LINKS.DASHBOARD,
            element: <Dashboard/>,
            errorElement: <ErrorBoundary/>
          },
          {
            path: LINKS.GROUPS,
            element: <GroupsPage/>,
          },
          {
            path: LINKS.GROUP_CREATE,
            element: <CreateGroupsPage/>,
          },
          {
            path: LINKS.GROUP_DETAIL,
            element: <GroupDetailPage />
          }
        ]
      },
      {
        path: LINKS.HOME,
        element: <HomePage/>
      },
      {
        path: LINKS.LOGIN,
        element: <LoginPage/>
      },
      {
        path: LINKS.REGISTER,
        element: <RegisterPage/>
      },
    ]
  },
]);

export default router;