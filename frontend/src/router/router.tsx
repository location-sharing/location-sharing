import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import GroupsPage from '../pages/groups/GroupsPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import ProtectedRoutes from './ProtectedRoutes';
import HomePage from '../pages/HomePage';
import GroupCreatePage from '../pages/groups/GroupCreatePage';
import GroupDetailPage from '../pages/groups/GroupDetailPage';
import GroupEditPage from '../pages/groups/GroupEditPage';
import GroupUsersPage from '../pages/groups/GroupUsersPage';

export enum LinkType {
  HOME,
  DASHBOARD,
  LOGIN,
  REGISTER,
  GROUPS,
  GROUP_CREATE,
  GROUP_DETAIL,
  GROUP_EDIT,
  GROUP_USERS,
}

const params = {
  GROUP_ID: ":groupId",
  GROUP_USER_ID: ":groupUserId",
}

export const LINKS: {[key in LinkType]: {template: string, build: (params?: any) => string}} = {
  [LinkType.HOME]: {
    template: "/",
    build: () => LINKS[LinkType.HOME].template
  },
  [LinkType.DASHBOARD]: {
    template: "/dashboard",
    build: () => LINKS[LinkType.DASHBOARD].template
  },
  [LinkType.LOGIN]: {
    template: "/login",
    build: () => LINKS[LinkType.LOGIN].template
  },
  [LinkType.REGISTER]: {
    template: "/register",
    build: () => LINKS[LinkType.REGISTER].template
  },
  [LinkType.GROUPS]: {
    template: "/groups",
    build: () => LINKS[LinkType.GROUPS].template
  },
  [LinkType.GROUP_CREATE]: {
    template: "/groups/new",
    build: () => LINKS[LinkType.GROUP_CREATE].template
  },
  [LinkType.GROUP_DETAIL]: {
    template: `/groups/${params.GROUP_ID}`,
    build: (buildParams: {groupId: string}) => LINKS[LinkType.GROUP_DETAIL].template.replace(params.GROUP_ID, buildParams.groupId)
  },
  [LinkType.GROUP_EDIT]: {
    template: `/groups/${params.GROUP_ID}/edit`,
    build: (buildParams: {groupId: string}) => LINKS[LinkType.GROUP_EDIT].template.replace(params.GROUP_ID, buildParams.groupId)
  },
  [LinkType.GROUP_USERS]: {
    template: `/groups/${params.GROUP_ID}/members`,
    build: (buildParams: {groupId: string}) => LINKS[LinkType.GROUP_USERS].template.replace(params.GROUP_ID, buildParams.groupId)
  },
}

const router = createBrowserRouter([
  {
    element: <App/>,
    children: [
      {
        path: LINKS[LinkType.HOME].template,
        element: <HomePage/>
      },
      {
        path: LINKS[LinkType.LOGIN].template,
        element: <LoginPage/>
      },
      {
        path: LINKS[LinkType.REGISTER].template,
        element: <RegisterPage/>
      },
      {
        element: <ProtectedRoutes/>,
        children: [
          {
            path: LINKS[LinkType.DASHBOARD].template,
            element: <Dashboard/>,
            errorElement: <ErrorBoundary/>
          },
          {
            path: LINKS[LinkType.GROUPS].template,
            element: <GroupsPage/>,
          },
          {
            path: LINKS[LinkType.GROUP_CREATE].template,
            element: <GroupCreatePage/>,
          },
          {
            path: LINKS[LinkType.GROUP_EDIT].template,
            element: <GroupEditPage/>
          },
          {
            path: LINKS[LinkType.GROUP_DETAIL].template,
            element: <GroupDetailPage />,
          },
          {
            path: LINKS[LinkType.GROUP_USERS].template,
            element: <GroupUsersPage />,
          },
        ]
      },
    ]
  },
]);

export default router;