import { configureStore } from "@reduxjs/toolkit";
import { authApi } from "../../features/auth/api/auth-api";
import authReducer from "../../features/auth/model/auth-slice";
import { lawyerApplicationApi } from "../../features/lawyer-application/api/lawyer-application-api";
import { adminLawyerApi } from "../../features/admin/api/admin-lawyer-api";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [lawyerApplicationApi.reducerPath]: lawyerApplicationApi.reducer,
    [adminLawyerApi.reducerPath]: adminLawyerApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(
      authApi.middleware,
      lawyerApplicationApi.middleware,
      adminLawyerApi.middleware,
    ),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
