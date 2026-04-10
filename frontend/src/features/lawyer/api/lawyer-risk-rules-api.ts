import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { RuleConditionOperator } from "./lawyer-validation-rules-api";

export type RiskLevel = "HIGH" | "MEDIUM" | "LOW";

export interface RiskRule {
  id: number;
  templateId: number;
  templateTitle: string;
  ruleCode: string;
  fieldKey: string;
  operator: RuleConditionOperator;
  expectedValue: string;
  riskMessage: string;
  riskLevel: RiskLevel;
  active: boolean;
}

export interface RiskRuleRequest {
  templateId: number;
  ruleCode: string;
  fieldKey: string;
  operator: RuleConditionOperator;
  expectedValue?: string;
  riskMessage: string;
  riskLevel: RiskLevel;
}

export const lawyerRiskRulesApi = createApi({
  reducerPath: "lawyerRiskRulesApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["RiskRule"],
  endpoints: (builder) => ({
    getRiskRulesByTemplate: builder.query<RiskRule[], number>({
      query: (templateId) => `/risk-rules/template/${templateId}`,
      providesTags: ["RiskRule"],
    }),
    createRiskRule: builder.mutation<RiskRule, RiskRuleRequest>({
      query: (body) => ({ url: "/risk-rules", method: "POST", body }),
      invalidatesTags: ["RiskRule"],
    }),
    updateRiskRule: builder.mutation<RiskRule, { id: number; body: RiskRuleRequest }>({
      query: ({ id, body }) => ({ url: `/risk-rules/${id}`, method: "PUT", body }),
      invalidatesTags: ["RiskRule"],
    }),
    deleteRiskRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/risk-rules/${id}`, method: "DELETE" }),
      invalidatesTags: ["RiskRule"],
    }),
  }),
});

export const {
  useGetRiskRulesByTemplateQuery,
  useCreateRiskRuleMutation,
  useUpdateRiskRuleMutation,
  useDeleteRiskRuleMutation,
} = lawyerRiskRulesApi;
