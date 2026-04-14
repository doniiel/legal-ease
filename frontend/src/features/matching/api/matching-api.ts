import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { AnalysisResult } from "../../documents/api/document-api";

export interface MatchingRequest {
  fieldValues: Record<string, string>;
  categoryId?: number;
}

export const matchingApi = createApi({
  reducerPath: "matchingApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  endpoints: (builder) => ({
    matchTemplate: builder.mutation<AnalysisResult, MatchingRequest>({
      query: (body) => ({
        url: "/matching",
        method: "POST",
        body,
      }),
    }),
  }),
});

export const { useMatchTemplateMutation } = matchingApi;
