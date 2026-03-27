import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export type RuleConditionOperator =
  | "IS_EMPTY"
  | "IS_NOT_EMPTY"
  | "EQUALS"
  | "NOT_EQUALS"
  | "CONTAINS"
  | "GREATER_THAN"
  | "LESS_THAN"
  | "GREATER_OR_EQUAL"
  | "LESS_OR_EQUAL";

export interface ValidationRule {
  id: number;
  templateId: number;
  templateTitle: string;
  fieldKey: string;
  fieldLabel: string;
  operator: RuleConditionOperator;
  expectedValue: string;
  errorMessage: string;
  active: boolean;
}

export interface ValidationRuleRequest {
  templateId: number;
  fieldKey: string;
  fieldLabel: string;
  operator: RuleConditionOperator;
  expectedValue?: string;
  errorMessage: string;
}

export const lawyerValidationRulesApi = createApi({
  reducerPath: "lawyerValidationRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["ValidationRule"],
  endpoints: (builder) => ({
    getValidationRulesByTemplate: builder.query<ValidationRule[], number>({
      query: (templateId) => `/lawyer/validation-rules/template/${templateId}`,
      providesTags: ["ValidationRule"],
    }),
    createValidationRule: builder.mutation<ValidationRule, ValidationRuleRequest>({
      query: (body) => ({ url: "/lawyer/validation-rules", method: "POST", body }),
      invalidatesTags: ["ValidationRule"],
    }),
    updateValidationRule: builder.mutation<ValidationRule, { id: number; body: ValidationRuleRequest }>({
      query: ({ id, body }) => ({ url: `/lawyer/validation-rules/${id}`, method: "PUT", body }),
      invalidatesTags: ["ValidationRule"],
    }),
    deleteValidationRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/lawyer/validation-rules/${id}`, method: "DELETE" }),
      invalidatesTags: ["ValidationRule"],
    }),
  }),
});

export const {
  useGetValidationRulesByTemplateQuery,
  useCreateValidationRuleMutation,
  useUpdateValidationRuleMutation,
  useDeleteValidationRuleMutation,
} = lawyerValidationRulesApi;
