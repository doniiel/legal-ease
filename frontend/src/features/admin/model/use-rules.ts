import { App } from "antd";
import {
  useGetValidationRulesQuery,  useToggleValidationRuleMutation,
  useGetRiskRulesQuery,        useToggleRiskRuleMutation,
  useGetMatchingRulesQuery,    useToggleMatchingRuleMutation,
  useGetConditionalRulesQuery, useToggleConditionalRuleMutation,
  useGetRequiredDocRulesQuery, useToggleRequiredDocRuleMutation,
} from "../api/admin-rules-api";

export function useRules() {
  const { message } = App.useApp();

  const { data: validationRules = [], isLoading: vLoading } = useGetValidationRulesQuery();
  const { data: riskRules = [],       isLoading: rLoading } = useGetRiskRulesQuery();
  const { data: matchingRules = [],   isLoading: mLoading } = useGetMatchingRulesQuery();
  const { data: conditionalRules = [],isLoading: cLoading } = useGetConditionalRulesQuery();
  const { data: requiredDocRules = [],isLoading: dLoading } = useGetRequiredDocRulesQuery();

  const [toggleValidation]  = useToggleValidationRuleMutation();
  const [toggleRisk]        = useToggleRiskRuleMutation();
  const [toggleMatching]    = useToggleMatchingRuleMutation();
  const [toggleConditional] = useToggleConditionalRuleMutation();
  const [toggleRequiredDoc] = useToggleRequiredDocRuleMutation();

  const wrap = (fn: (id: number) => Promise<unknown>) => async (id: number) => {
    try { await fn(id); }
    catch { message.error("Ошибка при переключении правила"); }
  };

  const totalActive =
    validationRules.filter((r) => r.active).length +
    riskRules.filter((r) => r.active).length +
    matchingRules.filter((r) => r.active).length +
    conditionalRules.filter((r) => r.active).length +
    requiredDocRules.filter((r) => r.active).length;

  return {
    validationRules,  vLoading,
    riskRules,        rLoading,
    matchingRules,    mLoading,
    conditionalRules, cLoading,
    requiredDocRules, dLoading,
    totalActive,
    totalAll: validationRules.length + riskRules.length + matchingRules.length + conditionalRules.length + requiredDocRules.length,
    handleToggleValidation:  wrap((id) => toggleValidation(id).unwrap()),
    handleToggleRisk:        wrap((id) => toggleRisk(id).unwrap()),
    handleToggleMatching:    wrap((id) => toggleMatching(id).unwrap()),
    handleToggleConditional: wrap((id) => toggleConditional(id).unwrap()),
    handleToggleRequiredDoc: wrap((id) => toggleRequiredDoc(id).unwrap()),
  };
}
