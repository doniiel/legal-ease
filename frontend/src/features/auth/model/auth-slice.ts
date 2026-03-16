import { createSlice } from "@reduxjs/toolkit";
import { authApi } from "../api/auth-api";

export interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  role: string | null;
}

const decodeRole = (token: string | null): string | null => {
  if (!token) return null;
  try {
    return JSON.parse(atob(token.split(".")[1])).role ?? null;
  } catch {
    return null;
  }
};

const storedToken = localStorage.getItem("accessToken");

const initialState: AuthState = {
  accessToken: storedToken,
  refreshToken: localStorage.getItem("refreshToken"),
  isAuthenticated: !!storedToken,
  role: decodeRole(storedToken),
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout(state) {
      state.accessToken = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
      state.role = null;
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
    },
  },
  extraReducers: (builder) => {
    builder
      .addMatcher(authApi.endpoints.login.matchFulfilled, (state, { payload }) => {
        state.accessToken = payload.accessToken;
        state.refreshToken = payload.refreshToken;
        state.isAuthenticated = true;
        state.role = decodeRole(payload.accessToken);
        localStorage.setItem("accessToken", payload.accessToken);
        localStorage.setItem("refreshToken", payload.refreshToken);
      })
      .addMatcher(authApi.endpoints.logout.matchFulfilled, (state) => {
        state.accessToken = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
      });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
