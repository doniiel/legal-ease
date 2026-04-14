import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { type LawyerInfo } from "../../lawyer-application/api/lawyer-application-api";

export interface AdminLawyerApplication {
  id: number;
  lawyerInfo: LawyerInfo;
  licenseNumber: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  submittedAt: string;
  reviewerInfo: LawyerInfo | null;
  reviewedAt: string | null;
  rejectionReason: string | null;
}

export interface ApplicationsPage {
  totalPages: number;
  totalElements: number;
  content: AdminLawyerApplication[];
  numberOfElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GetApplicationsParams {
  status?: "PENDING" | "APPROVED" | "REJECTED";
  page?: number;
  size?: number;
  licenseNumber?: string;
  userId?: number;
}

export const adminLawyerApi = createApi({
  reducerPath: "adminLawyerApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["AdminApplication"],
  endpoints: (builder) => ({
    getApplications: builder.query<ApplicationsPage, GetApplicationsParams>({
      query: (params) => ({ url: "/lawyer-applications", params }),
      providesTags: ["AdminApplication"],
    }),
    getApplicationById: builder.query<AdminLawyerApplication, number>({
      query: (id) => `/lawyer-applications/${id}`,
      providesTags: ["AdminApplication"],
    }),
    approveApplication: builder.mutation<void, number>({
      query: (id) => ({
        url: `/lawyer-applications/${id}/approve`,
        method: "POST",
      }),
      invalidatesTags: ["AdminApplication"],
    }),
    rejectApplication: builder.mutation<void, { id: number; reason: string }>({
      query: ({ id, reason }) => ({
        url: `/lawyer-applications/${id}/reject`,
        method: "POST",
        body: { reason },
      }),
      invalidatesTags: ["AdminApplication"],
    }),
    deleteApplication: builder.mutation<void, number>({
      query: (id) => ({
        url: `/lawyer-applications/${id}`,
        method: "DELETE",
      }),
      invalidatesTags: ["AdminApplication"],
    }),
  }),
});

export const {
  useGetApplicationsQuery,
  useApproveApplicationMutation,
  useRejectApplicationMutation,
  useDeleteApplicationMutation,
} = adminLawyerApi;
