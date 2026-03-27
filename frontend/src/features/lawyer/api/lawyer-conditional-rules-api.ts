import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { RuleConditionOperator } from "./lawyer-validation-rules-api";

export interface ConditionalRule {
  id: number;
  templateId: number;
  templateTitle: string;
  conditionFieldKey: string;
  operator: RuleConditionOperator;
  conditionValue: string;
  targetFieldKey: string;
  active: boolean;
}

export interface ConditionalRuleRequest {
  templateId: number;
  conditionFieldKey: string;
  operator: RuleConditionOperator;
  conditionValue: string;
  targetFieldKey: string;
}

export const lawyerConditionalRulesApi = createApi({
  reducerPath: "lawyerConditionalRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["ConditionalRule"],
  endpoints: (builder) => ({
    getConditionalRulesByTemplate: builder.query<ConditionalRule[], number>({
      query: (templateId) => `/lawyer/conditional-rules/template/${templateId}`,
      providesTags: ["ConditionalRule"],
    }),
    createConditionalRule: builder.mutation<ConditionalRule, ConditionalRuleRequest>({
      query: (body) => ({ url: "/lawyer/conditional-rules", method: "POST", body }),
      invalidatesTags: ["ConditionalRule"],
    }),
    updateConditionalRule: builder.mutation<ConditionalRule, { id: number; body: ConditionalRuleRequest }>({
      query: ({ id, body }) => ({ url: `/lawyer/conditional-rules/${id}`, method: "PUT", body }),
      invalidatesTags: ["ConditionalRule"],
    }),
    deleteConditionalRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/lawyer/conditional-rules/${id}`, method: "DELETE" }),
      invalidatesTags: ["ConditionalRule"],
    }),
  }),
});

export const {
  useGetConditionalRulesByTemplateQuery,
  useCreateConditionalRuleMutation,
  useUpdateConditionalRuleMutation,
  useDeleteConditionalRuleMutation,
} = lawyerConditionalRulesApi;
