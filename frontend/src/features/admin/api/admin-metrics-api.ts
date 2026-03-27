import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface SystemMetrics {
  totalUsers: number;
  totalLawyers: number;
  totalDocuments: number;
  totalTemplates: number;
  pendingLawyerApplications: number;
  documentsThisMonth: number;
  activeUsers: number;
}

export const adminMetricsApi = createApi({
  reducerPath: "adminMetricsApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  endpoints: (builder) => ({
    getSystemMetrics: builder.query<SystemMetrics, void>({
      query: () => "/admin/metrics",
    }),
  }),
});

export const { useGetSystemMetricsQuery } = adminMetricsApi;
