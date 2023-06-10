import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import GroupDetailPage from '../pages/GroupDetailPage';
import GroupsPage from '../pages/GroupsPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

export const LINKS = {
  DASHBOARD: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  GROUPS: "/groups",
  GROUPDETAIL: "/groups/:groupId",
}

const router = createBrowserRouter([
  {
    element: <App/>,
    children: [
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
      {
        path: LINKS.GROUPDETAIL,
        element: <GroupDetailPage />
      }
    ]
  },
]);

export default router;