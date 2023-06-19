import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import GroupCreatePage from '../pages/groups/GroupCreatePage';
import GroupDetailPage from '../pages/groups/GroupDetailPage';
import GroupEditPage from '../pages/groups/GroupEditPage';
import GroupUsersPage from '../pages/groups/GroupUsersPage';
import GroupsPage from '../pages/groups/GroupsPage';
import SessionPage from '../pages/sessions/SessionPage';
import ProtectedRoutes from './ProtectedRoutes';
import ErrorBoundary from '../components/error-boundary/ErrorBoundary';

export enum LinkType {
  HOME,
  LOGIN,
  REGISTER,
  GROUPS,
  GROUP_CREATE,
  GROUP_DETAIL,
  GROUP_EDIT,
  GROUP_USERS,
  GROUP_SESSIONS,
  WILDCARD,
}

const params = {
  GROUP_ID: ":groupId",
}

export const LINKS: {[key in LinkType]: {template: string, build: (params?: any) => string}} = {
  [LinkType.HOME]: {
    template: "/",
    build: () => LINKS[LinkType.HOME].template
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
    build: (pathParams: {groupId: string}) => LINKS[LinkType.GROUP_DETAIL].template.replace(params.GROUP_ID, pathParams.groupId)
  },
  [LinkType.GROUP_EDIT]: {
    template: `/groups/${params.GROUP_ID}/edit`,
    build: (pathParams: {groupId: string}) => LINKS[LinkType.GROUP_EDIT].template.replace(params.GROUP_ID, pathParams.groupId)
  },
  [LinkType.GROUP_USERS]: {
    template: `/groups/${params.GROUP_ID}/members`,
    build: (pathParams: {groupId: string}) => LINKS[LinkType.GROUP_USERS].template.replace(params.GROUP_ID, pathParams.groupId)
  },
  [LinkType.GROUP_SESSIONS]: {
    template: `/groups/${params.GROUP_ID}/sessions`,
    build: (pathParams: {groupId: string}) => LINKS[LinkType.GROUP_SESSIONS].template.replace(params.GROUP_ID, pathParams.groupId)
  },
  [LinkType.WILDCARD]: {
    template: "/*",
    build: () => LINKS[LinkType.WILDCARD].template
  }
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
          {
            path: LINKS[LinkType.GROUP_SESSIONS].template,
            element: <SessionPage/>
          }
        ]
      },
      {
        path: LINKS[LinkType.WILDCARD].template,
        element: <HomePage/>
      },
    ]
  },
]);

export default router;