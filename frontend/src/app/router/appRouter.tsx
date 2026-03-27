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
import AdminDashboard from "../../pages/admin-dashboard";
import AdminApplications from "../../pages/admin-applications";
import AdminUsers from "../../pages/admin-users";
import AdminCategories from "../../pages/admin-categories";
import AdminRules from "../../pages/admin-rules";
import AdminAudit from "../../pages/admin-audit";
import LawyerTemplates from "../../pages/lawyer-templates";
import LawyerMatchingRules from "../../pages/lawyer-matching-rules";
import LawyerRuleManager from "../../pages/lawyer-rule-manager";
import DocumentDetail from "../../pages/document-detail";
import ProtectedRoute from "../../shared/ui/ProtectedRoute";
import { ROUTES } from "./router";
import type { JSX } from "react";

const pub = (el: JSX.Element) => (
  <ProtectedRoute public>{el}</ProtectedRoute>
);

const auth = (el: JSX.Element, roles: string[] = []) => (
  <ProtectedRoute roles={roles}>{el}</ProtectedRoute>
);

export const PAGES = [
  { path: "/", element: <Navigate to={ROUTES.HOME} replace /> },

  // ── Public (redirect to home if already logged in) ──
  { path: ROUTES.LOGIN,    element: pub(<Login />) },
  { path: ROUTES.REGISTER, element: pub(<Register />) },
  { path: ROUTES.FORGOT,   element: pub(<ForgotPassword />) },
  { path: ROUTES.CONFIRM,  element: pub(<Confirm />) },

  // ── Authenticated (all roles) ──
  { path: ROUTES.HOME,      element: auth(<Home />) },
  { path: ROUTES.DOCUMENTS, element: auth(<Documents />) },
  { path: `${ROUTES.DOCUMENTS}/:id`, element: auth(<DocumentDetail />) },
  { path: ROUTES.PROFILE,   element: auth(<Profile />) },
  { path: ROUTES.SETTINGS,  element: auth(<Settings />) },

  // ── USER only ──
  { path: ROUTES.LAWYER_APPLICATION, element: auth(<LawyerApplication />, ["USER"]) },

  // ── ADMIN only ──
  { path: ROUTES.ADMIN_DASHBOARD,   element: auth(<AdminDashboard />,   ["ADMIN"]) },
  { path: ROUTES.ADMIN_APPLICATIONS,element: auth(<AdminApplications />,["ADMIN"]) },
  { path: ROUTES.ADMIN_USERS,       element: auth(<AdminUsers />,       ["ADMIN"]) },
  { path: ROUTES.ADMIN_CATEGORIES,  element: auth(<AdminCategories />,  ["ADMIN"]) },
  { path: ROUTES.ADMIN_RULES,       element: auth(<AdminRules />,       ["ADMIN"]) },
  { path: ROUTES.ADMIN_AUDIT,       element: auth(<AdminAudit />,       ["ADMIN"]) },

  // ── LAWYER only ──
  { path: ROUTES.LAWYER_TEMPLATES,     element: auth(<LawyerTemplates />,    ["LAWYER"]) },
  { path: ROUTES.LAWYER_MATCHING_RULES,element: auth(<LawyerMatchingRules />,["LAWYER"]) },
  { path: ROUTES.LAWYER_RULE_MANAGER,  element: auth(<LawyerRuleManager />,  ["LAWYER"]) },
];
