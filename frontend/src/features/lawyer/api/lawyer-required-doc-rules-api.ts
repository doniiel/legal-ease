import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { RuleConditionOperator } from "./lawyer-validation-rules-api";

export interface RequiredDocRule {
  id: number;
  templateId: number;
  templateTitle: string;
  requiredDocTitle: string;
  reason: string;
  conditionFieldKey?: string;
  conditionOperator?: RuleConditionOperator;
  conditionValue?: string;
  mandatory: boolean;
  active: boolean;
}

export interface RequiredDocRuleRequest {
  templateId: number;
  requiredDocTitle: string;
  reason: string;
  conditionFieldKey?: string;
  conditionOperator?: RuleConditionOperator;
  conditionValue?: string;
  mandatory: boolean;
}

export const lawyerRequiredDocRulesApi = createApi({
  reducerPath: "lawyerRequiredDocRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["RequiredDocRule"],
  endpoints: (builder) => ({
    getRequiredDocRulesByTemplate: builder.query<RequiredDocRule[], number>({
      query: (templateId) => `/required-doc-rules/template/${templateId}`,
      providesTags: ["RequiredDocRule"],
    }),
    createRequiredDocRule: builder.mutation<RequiredDocRule, RequiredDocRuleRequest>({
      query: (body) => ({ url: "/required-doc-rules", method: "POST", body }),
      invalidatesTags: ["RequiredDocRule"],
    }),
    updateRequiredDocRule: builder.mutation<RequiredDocRule, { id: number; body: RequiredDocRuleRequest }>({
      query: ({ id, body }) => ({ url: `/required-doc-rules/${id}`, method: "PUT", body }),
      invalidatesTags: ["RequiredDocRule"],
    }),
    deleteRequiredDocRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/required-doc-rules/${id}`, method: "DELETE" }),
      invalidatesTags: ["RequiredDocRule"],
    }),
  }),
});

export const {
  useGetRequiredDocRulesByTemplateQuery,
  useCreateRequiredDocRuleMutation,
  useUpdateRequiredDocRuleMutation,
  useDeleteRequiredDocRuleMutation,
} = lawyerRequiredDocRulesApi;
