import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface ClauseExplainRequest {
  text: string;
}

export interface ClauseExplainResponse {
  explanation: string;
  simplifiedText: string;
  risks: string[];
  recommendations: string[];
}

export const aiApi = createApi({
  reducerPath: "aiApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  endpoints: (builder) => ({
    explainClause: builder.mutation<ClauseExplainResponse, ClauseExplainRequest>({
      query: (body) => ({
        url: "/ai/explain-clause",
        method: "POST",
        body,
      }),
    }),
  }),
});

export interface DocumentExplainResponse {
  summary: string;
  obligations: string;
  warnings: string;
  nextSteps: string;
}

export const { useExplainClauseMutation } = aiApi;
