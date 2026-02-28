import { createApi } from "@reduxjs/toolkit/query/react";
import { baseQuery } from "../../../shared/api";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  timestamp: string;
}

export const authApi = createApi({
  reducerPath: "authApi",
  baseQuery,
  endpoints: (builder) => ({
    login: builder.mutation<AuthResponse, { email: string; password: string }>({
      query: (body) => ({
        url: "/auth/login",
        method: "POST",
        body,
      }),
    }),

    register: builder.mutation<AuthResponse, {
      firstName: string;
      middleName: string;
      lastName: string;
      iin: string;
      gender: "MALE" | "FEMALE";
      email: string;
      phone: string;
      password: string;
    }>({
      query: (body) => ({
        url: "/auth/register",
        method: "POST",
        body,
      }),
    }),

    logout: builder.mutation<void, void>({
      query: () => ({
        url: "/auth/logout",
        method: "POST",
      }),
    }),

    sendResetCode: builder.mutation<void, { email: string }>({
      query: (body) => ({
        url: "/auth/reset-password",
        method: "POST",
        body,
      }),
    }),

    verifyResetCode: builder.mutation<void, { email: string; code: string }>({
      query: (body) => ({
        url: "/auth/confirm",
        method: "POST",
        body,
      }),
    }),

    resetPassword: builder.mutation<void, { email: string; code: string; newPassword: string }>({
      query: (body) => ({
        url: "/auth/change-password",
        method: "POST",
        body,
      }),
    }),
  }),
});

export const {
  useLoginMutation,
  useRegisterMutation,
  useLogoutMutation,
  useSendResetCodeMutation,
  useVerifyResetCodeMutation,
  useResetPasswordMutation,
} = authApi;
