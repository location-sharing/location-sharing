import ErrorBoundary from "../components/error-boundary/ErrorBoundary";
import Dashboard from "../pages/Dashboard";
import { createBrowserRouter } from 'react-router-dom';

const router = createBrowserRouter([
  {
    path: "/",
    element: <Dashboard/>,
    errorElement: <ErrorBoundary/>
    // children: []
  }
]);

export default router;