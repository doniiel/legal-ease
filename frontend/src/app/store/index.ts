import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "../../features/auth/api/auth-api";
import authReducer from "../../features/auth/model/auth-slice";
import { lawyerApplicationApi } from "../../features/lawyer-application/api/lawyer-application-api";
import { adminLawyerApi } from "../../features/admin/api/admin-lawyer-api";
import { adminUserApi } from "../../features/admin/api/admin-user-api";
import { documentApi } from "../../features/documents/api/document-api";
import { adminCategoryApi } from "../../features/admin/api/admin-category-api";
import { adminRulesApi } from "../../features/admin/api/admin-rules-api";
import { adminMetricsApi } from "../../features/admin/api/admin-metrics-api";
import { adminAuditApi } from "../../features/admin/api/admin-audit-api";
import { lawyerTemplateApi } from "../../features/lawyer/api/lawyer-template-api";
import { lawyerMatchingRulesApi } from "../../features/lawyer/api/lawyer-matching-rules-api";
import { lawyerValidationRulesApi } from "../../features/lawyer/api/lawyer-validation-rules-api";
import { lawyerRiskRulesApi } from "../../features/lawyer/api/lawyer-risk-rules-api";
import { lawyerConditionalRulesApi } from "../../features/lawyer/api/lawyer-conditional-rules-api";
import { lawyerRequiredDocRulesApi } from "../../features/lawyer/api/lawyer-required-doc-rules-api";
import { lawyerDocumentApi } from "../../features/lawyer/api/lawyer-document-api";
import { lawyerClauseApi } from "../../features/lawyer/api/lawyer-clause-api";
import { lawyerDocumentReviewsApi } from "../../features/lawyer/api/lawyer-document-reviews-api";
import { publicCategoryApi } from "../../features/categories/api/public-category-api";
import { userTemplateApi } from "../../features/templates/api/user-template-api";
import { matchingApi } from "../../features/matching/api/matching-api";
import { aiApi } from "../../features/ai/api/ai-api";
import { publicShareApi } from "../../features/share/api/public-share-api";
import { profileApi } from "../../features/profile/api/profile-api";
import { documentVerificationApi } from "../../features/documents/api/document-verification-api";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [lawyerApplicationApi.reducerPath]: lawyerApplicationApi.reducer,
    [adminLawyerApi.reducerPath]: adminLawyerApi.reducer,
    [adminUserApi.reducerPath]: adminUserApi.reducer,
    [adminCategoryApi.reducerPath]: adminCategoryApi.reducer,
    [adminRulesApi.reducerPath]: adminRulesApi.reducer,
    [adminMetricsApi.reducerPath]: adminMetricsApi.reducer,
    [adminAuditApi.reducerPath]: adminAuditApi.reducer,
    [documentApi.reducerPath]: documentApi.reducer,
    [lawyerTemplateApi.reducerPath]: lawyerTemplateApi.reducer,
    [lawyerMatchingRulesApi.reducerPath]: lawyerMatchingRulesApi.reducer,
    [lawyerValidationRulesApi.reducerPath]: lawyerValidationRulesApi.reducer,
    [lawyerRiskRulesApi.reducerPath]: lawyerRiskRulesApi.reducer,
    [lawyerConditionalRulesApi.reducerPath]: lawyerConditionalRulesApi.reducer,
    [lawyerRequiredDocRulesApi.reducerPath]: lawyerRequiredDocRulesApi.reducer,
    [lawyerDocumentApi.reducerPath]: lawyerDocumentApi.reducer,
    [lawyerClauseApi.reducerPath]: lawyerClauseApi.reducer,
    [lawyerDocumentReviewsApi.reducerPath]: lawyerDocumentReviewsApi.reducer,
    [publicCategoryApi.reducerPath]: publicCategoryApi.reducer,
    [userTemplateApi.reducerPath]: userTemplateApi.reducer,
    [matchingApi.reducerPath]: matchingApi.reducer,
    [aiApi.reducerPath]: aiApi.reducer,
    [publicShareApi.reducerPath]: publicShareApi.reducer,
    [profileApi.reducerPath]: profileApi.reducer,
    [documentVerificationApi.reducerPath]: documentVerificationApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      authApi.middleware,
      lawyerApplicationApi.middleware,
      adminLawyerApi.middleware,
      adminUserApi.middleware,
      adminCategoryApi.middleware,
      adminRulesApi.middleware,
      adminMetricsApi.middleware,
      adminAuditApi.middleware,
      documentApi.middleware,
      lawyerTemplateApi.middleware,
      lawyerMatchingRulesApi.middleware,
      lawyerValidationRulesApi.middleware,
      lawyerRiskRulesApi.middleware,
      lawyerConditionalRulesApi.middleware,
      lawyerRequiredDocRulesApi.middleware,
      lawyerDocumentApi.middleware,
      lawyerClauseApi.middleware,
      lawyerDocumentReviewsApi.middleware,
      publicCategoryApi.middleware,
      userTemplateApi.middleware,
      matchingApi.middleware,
      aiApi.middleware,
      publicShareApi.middleware,
      profileApi.middleware,
      documentVerificationApi.middleware,
    ),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
