import Login from "../../pages/login";
import Register from "../../pages/register";
import ForgotPassword from "../../pages/forgot-password";
import Dashboard from "../../pages/dashboard";
import { ROUTES } from "./router";

export const PAGES = [
  {
    path: ROUTES.DASHBOARD,
    element: <Dashboard />,
  },
  {
    path: ROUTES.LOGIN,
    element: <Login/>,
  },
  {
    path: ROUTES.REGISTER,
    element: <Register/>,
  },
  {
    path: ROUTES.FORGOT,
    element: <ForgotPassword/>,
  },
];