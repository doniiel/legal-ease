import { Navigate } from "react-router-dom";
import Login from "../../pages/login";
import Register from "../../pages/register";
import ForgotPassword from "../../pages/forgot-password";
import Confirm from "../../pages/confirm";
import Home from "../../pages/dashboard-home";
import Documents from "../../pages/documents";
import Profile from "../../pages/profile";
import Settings from "../../pages/settings";
import LawyerApplication from "../../pages/lawyer-application";
import AdminApplications from "../../pages/admin-applications";
import AdminUsers from "../../pages/admin-users";
import AdminCategories from "../../pages/admin-categories";
import LawyerTemplates from "../../pages/lawyer-templates";
import LawyerMatchingRules from "../../pages/lawyer-matching-rules";
import DocumentDetail from "../../pages/document-detail";
import { ROUTES } from "./router";

export const PAGES = [
  { path: "/", element: <Navigate to={ROUTES.HOME} replace /> },
  { path: ROUTES.LOGIN, element: <Login /> },
  { path: ROUTES.REGISTER, element: <Register /> },
  { path: ROUTES.FORGOT, element: <ForgotPassword /> },
  { path: ROUTES.CONFIRM, element: <Confirm /> },
  { path: ROUTES.HOME, element: <Home /> },
  { path: ROUTES.DOCUMENTS, element: <Documents /> },
  { path: `${ROUTES.DOCUMENTS}/:id`, element: <DocumentDetail /> },
  { path: ROUTES.PROFILE, element: <Profile /> },
  { path: ROUTES.LAWYER_APPLICATION, element: <LawyerApplication /> },
  { path: ROUTES.SETTINGS, element: <Settings /> },
  { path: ROUTES.ADMIN_APPLICATIONS, element: <AdminApplications /> },
  { path: ROUTES.ADMIN_USERS, element: <AdminUsers /> },
  { path: ROUTES.ADMIN_CATEGORIES, element: <AdminCategories /> },
  { path: ROUTES.LAWYER_TEMPLATES, element: <LawyerTemplates /> },
  { path: ROUTES.LAWYER_MATCHING_RULES, element: <LawyerMatchingRules /> },
];
