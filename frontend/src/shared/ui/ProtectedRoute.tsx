import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../features/auth/model/use-auth";
import { ROUTES } from "../../app/router/router";
import type { ReactNode } from "react";

interface ProtectedRouteProps {
  children: ReactNode;
  /** Roles allowed to access this route. Empty = any authenticated user. */
  roles?: string[];
  /** If true, redirect away if already logged in (login/register pages) */
  public?: boolean;
}

export default function ProtectedRoute({
  children,
  roles = [],
  public: isPublic = false,
}: ProtectedRouteProps) {
  const { isAuthenticated, role } = useAuth();
  const location = useLocation();

  // Public route: redirect to home if already logged in
  if (isPublic) {
    if (isAuthenticated) {
      return <Navigate to={ROUTES.HOME} replace />;
    }
    return <>{children}</>;
  }

  // Private route: must be logged in
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  // Role-restricted route: redirect to home if role doesn't match
  if (roles.length > 0 && role && !roles.includes(role)) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
}
