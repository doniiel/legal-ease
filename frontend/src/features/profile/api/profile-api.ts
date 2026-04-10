import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface UserProfile {
  id: number;
  fio: string;
  email: string;
  phone: string;
  iin: string;
  gender: "MALE" | "FEMALE" | null;
  dateOfBirth: string | null;
  role: "ADMIN" | "LAWYER" | "USER";
  active: boolean;
}

export interface UpdateProfileRequest {
  fio: string;
  phone: string;
  gender?: "MALE" | "FEMALE" | null;
}

export const profileApi = createApi({
  reducerPath: "profileApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["Profile"],
  endpoints: (builder) => ({
    getProfile: builder.query<UserProfile, void>({
      query: () => "/profile",
      providesTags: ["Profile"],
    }),
    updateProfile: builder.mutation<UserProfile, UpdateProfileRequest>({
      query: (body) => ({ url: "/profile", method: "PUT", body }),
      invalidatesTags: ["Profile"],
    }),
  }),
});

export const { useGetProfileQuery, useUpdateProfileMutation } = profileApi;
