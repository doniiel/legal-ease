import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface MatchingRule {
  id: number;
  templateId: number;
  templateTitle: string;
  categoryId: number;
  categoryName: string;
  keywords: string;
  baseScore: number;
  active: boolean;
}

export interface MatchingRuleRequest {
  templateId: number;
  categoryId: number;
  keywords: string;
  baseScore: number;
}

export const lawyerMatchingRulesApi = createApi({
  reducerPath: "lawyerMatchingRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ["MatchingRule"],
  endpoints: (builder) => ({
    getRules: builder.query<MatchingRule[], void>({
      query: () => "/lawyer/matching-rules",
      providesTags: ["MatchingRule"],
    }),
    getRulesByTemplate: builder.query<MatchingRule[], number>({
      query: (templateId) => `/lawyer/matching-rules/template/${templateId}`,
      providesTags: ["MatchingRule"],
    }),
    createRule: builder.mutation<MatchingRule, MatchingRuleRequest>({
      query: (body) => ({
        url: "/lawyer/matching-rules",
        method: "POST",
        body,
      }),
      invalidatesTags: ["MatchingRule"],
    }),
    updateRule: builder.mutation<MatchingRule, { id: number; body: MatchingRuleRequest }>({
      query: ({ id, body }) => ({
        url: `/lawyer/matching-rules/${id}`,
        method: "PUT",
        body,
      }),
      invalidatesTags: ["MatchingRule"],
    }),
    deleteRule: builder.mutation<void, number>({
      query: (id) => ({
        url: `/lawyer/matching-rules/${id}`,
        method: "DELETE",
      }),
      invalidatesTags: ["MatchingRule"],
    }),
  }),
});

export const {
  useGetRulesQuery,
  useGetRulesByTemplateQuery,
  useCreateRuleMutation,
  useUpdateRuleMutation,
  useDeleteRuleMutation,
} = lawyerMatchingRulesApi;
