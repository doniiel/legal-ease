import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface AdminValidationRule {
  id: number;
  templateId: number;
  fieldKey: string;
  label: string;
  ruleType: string;
  ruleValue: string;
  errorMessage: string;
  active: boolean;
}

export interface AdminRiskRule {
  id: number;
  templateId: number;
  ruleCode: string;
  message: string;
  level: string;
  condition: string;
  active: boolean;
}

export interface AdminMatchingRule {
  id: number;
  templateId: number;
  templateTitle: string;
  categoryId: number;
  categoryName: string;
  keywords: string;
  baseScore: number;
  active: boolean;
}

export interface AdminConditionalRule {
  id: number;
  templateId: number;
  conditionField: string;
  conditionOperator: string;
  conditionValue: string;
  actionType: string;
  actionTarget: string;
  active: boolean;
}

export interface AdminRequiredDocRule {
  id: number;
  templateId: number;
  title: string;
  reason: string;
  mandatory: boolean;
  active: boolean;
}

export const adminRulesApi = createApi({
  reducerPath: "adminRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["AdminValidationRule", "AdminRiskRule", "AdminMatchingRule", "AdminConditionalRule", "AdminRequiredDocRule"],
  endpoints: (builder) => ({
    getValidationRules: builder.query<AdminValidationRule[], void>({
      query: () => "/rules/validation",
      providesTags: ["AdminValidationRule"],
    }),
    toggleValidationRule: builder.mutation<AdminValidationRule, number>({
      query: (id) => ({ url: `/rules/validation/${id}/toggle`, method: "PATCH" }),
      invalidatesTags: ["AdminValidationRule"],
    }),
    getRiskRules: builder.query<AdminRiskRule[], void>({
      query: () => "/rules/risk",
      providesTags: ["AdminRiskRule"],
    }),
    toggleRiskRule: builder.mutation<AdminRiskRule, number>({
      query: (id) => ({ url: `/rules/risk/${id}/toggle`, method: "PATCH" }),
      invalidatesTags: ["AdminRiskRule"],
    }),
    getMatchingRules: builder.query<AdminMatchingRule[], void>({
      query: () => "/rules/matching",
      providesTags: ["AdminMatchingRule"],
    }),
    toggleMatchingRule: builder.mutation<AdminMatchingRule, number>({
      query: (id) => ({ url: `/rules/matching/${id}/toggle`, method: "PATCH" }),
      invalidatesTags: ["AdminMatchingRule"],
    }),
    getConditionalRules: builder.query<AdminConditionalRule[], void>({
      query: () => "/rules/conditional",
      providesTags: ["AdminConditionalRule"],
    }),
    toggleConditionalRule: builder.mutation<AdminConditionalRule, number>({
      query: (id) => ({ url: `/rules/conditional/${id}/toggle`, method: "PATCH" }),
      invalidatesTags: ["AdminConditionalRule"],
    }),
    getRequiredDocRules: builder.query<AdminRequiredDocRule[], void>({
      query: () => "/rules/required-docs",
      providesTags: ["AdminRequiredDocRule"],
    }),
    toggleRequiredDocRule: builder.mutation<AdminRequiredDocRule, number>({
      query: (id) => ({ url: `/rules/required-docs/${id}/toggle`, method: "PATCH" }),
      invalidatesTags: ["AdminRequiredDocRule"],
    }),
  }),
});

export const {
  useGetValidationRulesQuery,
  useToggleValidationRuleMutation,
  useGetRiskRulesQuery,
  useToggleRiskRuleMutation,
  useGetMatchingRulesQuery,
  useToggleMatchingRuleMutation,
  useGetConditionalRulesQuery,
  useToggleConditionalRuleMutation,
  useGetRequiredDocRulesQuery,
  useToggleRequiredDocRuleMutation,
} = adminRulesApi;
