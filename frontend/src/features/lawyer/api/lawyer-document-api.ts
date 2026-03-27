import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { DocumentListItem, DocumentDetail, DocumentsPage } from "../../documents/api/document-api";

export interface GetLawyerDocumentsParams {
  page?: number;
  size?: number;
  sort?: string;
}

export type { DocumentListItem, DocumentDetail, DocumentsPage };

export const lawyerDocumentApi = createApi({
  reducerPath: "lawyerDocumentApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["LawyerDocument"],
  endpoints: (builder) => ({
    getLawyerDocuments: builder.query<DocumentsPage, GetLawyerDocumentsParams>({
      query: ({ page = 0, size = 10, sort = "createdDate,DESC" } = {}) => ({
        url: "/lawyer/documents",
        params: { page, size, sort },
      }),
      providesTags: ["LawyerDocument"],
    }),
    getLawyerDocumentsByTemplate: builder.query<DocumentsPage, { templateId: number } & GetLawyerDocumentsParams>({
      query: ({ templateId, page = 0, size = 10, sort = "createdDate,DESC" }) => ({
        url: `/lawyer/documents/by-template/${templateId}`,
        params: { page, size, sort },
      }),
      providesTags: ["LawyerDocument"],
    }),
    getLawyerDocumentById: builder.query<DocumentDetail, number>({
      query: (id) => `/lawyer/documents/${id}`,
      providesTags: (_result, _error, id) => [{ type: "LawyerDocument", id }],
    }),
  }),
});

export const {
  useGetLawyerDocumentsQuery,
  useGetLawyerDocumentsByTemplateQuery,
  useGetLawyerDocumentByIdQuery,
} = lawyerDocumentApi;
