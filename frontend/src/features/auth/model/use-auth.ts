import { useSelector, useDispatch } from "react-redux";
import { logout } from "./auth-slice";
import type { AuthState } from "./auth-slice";

// Typed selector without importing RootState from app/ (FSD compliant)
export function useAuth() {
  const dispatch = useDispatch();
  const { isAuthenticated } = useSelector(
    (state: { auth: AuthState }) => state.auth
  );

  return {
    isAuthenticated,
    logout: () => dispatch(logout()),
  };
}
