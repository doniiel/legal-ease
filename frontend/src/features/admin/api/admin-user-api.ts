import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface UserRole {
  id: number;
  code: string;
  active: boolean;
  createdBy: string;
  createdDate: string;
  updatedBy: string;
  updatedDate: string;
}

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string; // enum: "USER" | "LAWYER" | "ADMIN"
  telephone: string;
  userProfileImageUrl: string;
}

export interface UsersPage {
  content: AdminUser[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  pageable: {
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
    offset: number;
    sort: { sorted: boolean; unsorted: boolean; empty: boolean };
  };
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
}

export interface GetUsersParams {
  page?: number;
  size?: number;
}

export const adminUserApi = createApi({
  reducerPath: "adminUserApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["AdminUser"],
  endpoints: (builder) => ({
    getUsers: builder.query<UsersPage, GetUsersParams>({
      query: (params) => ({ url: "/admin/users", params }),
      providesTags: ["AdminUser"],
    }),
    getUserById: builder.query<AdminUser, number>({
      query: (id) => `/admin/users/${id}`,
      providesTags: ["AdminUser"],
    }),
    blockUser: builder.mutation<void, number>({
      query: (id) => ({ url: `/admin/users/${id}/block`, method: "POST" }),
      invalidatesTags: ["AdminUser"],
    }),
    unblockUser: builder.mutation<void, number>({
      query: (id) => ({ url: `/admin/users/${id}/unblock`, method: "POST" }),
      invalidatesTags: ["AdminUser"],
    }),
    revokeLawyer: builder.mutation<void, number>({
      query: (id) => ({ url: `/admin/users/${id}/revoke-lawyer`, method: "POST" }),
      invalidatesTags: ["AdminUser"],
    }),
  }),
});

export const {
  useGetUsersQuery,
  useGetUserByIdQuery,
  useBlockUserMutation,
  useUnblockUserMutation,
  useRevokeLawyerMutation,
} = adminUserApi;
