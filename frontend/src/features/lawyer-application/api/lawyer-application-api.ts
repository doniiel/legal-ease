import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface LawyerInfo {
  id: number;
  email: string;
  fio: string;
  iin: string;
}

export interface LawyerApplication {
  id: number;
  lawyerInfo: LawyerInfo;
  licenseNumber: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  submittedAt: string;
  rejectionReason: string | null;
}

export const lawyerApplicationApi = createApi({
  reducerPath: "lawyerApplicationApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["MyApplication"],
  endpoints: (builder) => ({
    submitApplication: builder.mutation<LawyerApplication, { licenseNum: string }>({
      query: (body) => ({
        url: "/lawyer-applications",
        method: "POST",
        body,
      }),
      invalidatesTags: ["MyApplication"],
    }),
    getMyApplication: builder.query<LawyerApplication, void>({
      query: () => "/lawyer-applications/my",
      providesTags: ["MyApplication"],
    }),
  }),
});

export const { useSubmitApplicationMutation, useGetMyApplicationQuery } = lawyerApplicationApi;
