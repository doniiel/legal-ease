import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "../../features/auth/api/auth-api";
import authReducer from "../../features/auth/model/auth-slice";
import { lawyerApplicationApi } from "../../features/lawyer-application/api/lawyer-application-api";
import { adminLawyerApi } from "../../features/admin/api/admin-lawyer-api";
import { adminUserApi } from "../../features/admin/api/admin-user-api";
import { documentApi } from "../../features/documents/api/document-api";
import { adminCategoryApi } from "../../features/admin/api/admin-category-api";
import { lawyerTemplateApi } from "../../features/lawyer/api/lawyer-template-api";
import { lawyerMatchingRulesApi } from "../../features/lawyer/api/lawyer-matching-rules-api";
import { publicCategoryApi } from "../../features/categories/api/public-category-api";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [lawyerApplicationApi.reducerPath]: lawyerApplicationApi.reducer,
    [adminLawyerApi.reducerPath]: adminLawyerApi.reducer,
    [adminUserApi.reducerPath]: adminUserApi.reducer,
    [documentApi.reducerPath]: documentApi.reducer,
    [adminCategoryApi.reducerPath]: adminCategoryApi.reducer,
    [lawyerTemplateApi.reducerPath]: lawyerTemplateApi.reducer,
    [lawyerMatchingRulesApi.reducerPath]: lawyerMatchingRulesApi.reducer,
    [publicCategoryApi.reducerPath]: publicCategoryApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      authApi.middleware,
      lawyerApplicationApi.middleware,
      adminLawyerApi.middleware,
      adminUserApi.middleware,
      documentApi.middleware,
      adminCategoryApi.middleware,
      lawyerTemplateApi.middleware,
      lawyerMatchingRulesApi.middleware,
      publicCategoryApi.middleware,
    ),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
