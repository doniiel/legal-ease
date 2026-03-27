import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface AuditLog {
  id: number;
  userId: number;
  userEmail: string;
  action: string;
  entityType: string;
  entityId: number;
  details: string;
  createdDate: string;
}

export interface AuditLogsPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GetAuditLogsParams {
  userId?: number;
  action?: string;
  entityType?: string;
  entityId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export const adminAuditApi = createApi({
  reducerPath: "adminAuditApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["AuditLog"],
  endpoints: (builder) => ({
    getAuditLogs: builder.query<AuditLogsPage, GetAuditLogsParams>({
      query: ({ page = 0, size = 20, sort = "createdDate,DESC", ...filters } = {}) => ({
        url: "/admin/audit-logs",
        params: { page, size, sort, ...filters },
      }),
      providesTags: ["AuditLog"],
    }),
    getAuditLogById: builder.query<AuditLog, number>({
      query: (id) => `/admin/audit-logs/${id}`,
      providesTags: (_result, _error, id) => [{ type: "AuditLog", id }],
    }),
  }),
});

export const {
  useGetAuditLogsQuery,
  useGetAuditLogByIdQuery,
} = adminAuditApi;
