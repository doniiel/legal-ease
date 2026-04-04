import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { BASE_URL } from "../../../shared/api";

export interface DocumentVerificationResponse {
  status: "VALID" | "INVALID";
  documentId: string | null;
  createdAt: string | null;
  createdBy: string | null;
  hashValid: boolean;
}

export const documentVerificationApi = createApi({
  reducerPath: "documentVerificationApi",
  baseQuery: fetchBaseQuery({ baseUrl: BASE_URL }),
  endpoints: (builder) => ({
    verifyDocument: builder.query<DocumentVerificationResponse, number>({
      query: (id) => `/documents/verify/${id}`,
    }),
  }),
});

export const { useVerifyDocumentQuery } = documentVerificationApi;
