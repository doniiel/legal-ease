import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export type DocumentStatus = "DRAFT" | "COMPLETED" | "PROCESSING";
export type RiskLevel = "HIGH" | "MEDIUM" | "LOW";

export interface FieldValue {
  id: number;
  fieldKey: string;
  fieldValue: string;
}

export interface DocumentListItem {
  id: number;
  title: string;
  templateTitle: string;
  categoryName: string;
  status: DocumentStatus;
  createdDate: string;
}

export interface DocumentDetail {
  id: number;
  title: string;
  templateId: number;
  templateTitle: string;
  categoryName: string;
  status: DocumentStatus;
  fieldValues: FieldValue[];
  missingRequiredFields: string[];
  createdDate: string;
  updatedDate: string;
}

export interface DocumentsPage {
  content: DocumentListItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface MatchedTemplate {
  templateId: number;
  title: string;
  categoryName: string;
  score: number;
  aiNote: string;
}

export interface ValidationError {
  fieldKey: string;
  label: string;
  message: string;
}

export interface Risk {
  ruleCode: string;
  message: string;
  level: RiskLevel;
  aiExplanation: string;
}

export interface RequiredDocument {
  templateId: number;
  title: string;
  reason: string;
  mandatory: boolean;
}

export interface FieldSuggestion {
  fieldKey: string;
  label: string;
  suggestedValue: string;
  reason: string;
}

export interface AnalysisResult {
  matchedTemplates: MatchedTemplate[];
  validationErrors: ValidationError[];
  risks: Risk[];
  requiredDynamicFields: string[];
  requiredDocuments: RequiredDocument[];
  fieldSuggestions: FieldSuggestion[];
  valid: boolean;
  aborted: boolean;
  abortReason: string;
  aiSummary: string;
  aiRecommendation: string;
  aiIntentLabel: string;
  aiIntentConfidence: number;
}

export interface CompleteDocumentResponse {
  success: boolean;
  document: DocumentDetail;
  result: AnalysisResult;
}

export interface CreateDocumentRequest {
  templateId: number;
  title: string;
  fieldValues: Record<string, string>;
}

export interface UpdateDocumentRequest {
  title: string;
  fieldValues: Record<string, string>;
}

export interface GetDocumentsParams {
  page?: number;
  size?: number;
  sort?: string[];
}

export const documentApi = createApi({
  reducerPath: "documentApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["Document"],
  endpoints: (builder) => ({
    getDocuments: builder.query<DocumentsPage, GetDocumentsParams>({
      query: (params) => ({ url: "/user/documents", params }),
      providesTags: ["Document"],
    }),
    getDocumentById: builder.query<DocumentDetail, number>({
      query: (id) => `/user/documents/${id}`,
      providesTags: ["Document"],
    }),
    createDocument: builder.mutation<DocumentDetail, CreateDocumentRequest>({
      query: (body) => ({ url: "/user/documents", method: "POST", body }),
      invalidatesTags: ["Document"],
    }),
    updateDocument: builder.mutation<DocumentDetail, { id: number } & UpdateDocumentRequest>({
      query: ({ id, ...body }) => ({ url: `/user/documents/${id}`, method: "PUT", body }),
      invalidatesTags: ["Document"],
    }),
    deleteDocument: builder.mutation<void, number>({
      query: (id) => ({ url: `/user/documents/${id}`, method: "DELETE" }),
      invalidatesTags: ["Document"],
    }),
    completeDocument: builder.mutation<CompleteDocumentResponse, number>({
      query: (id) => ({ url: `/user/documents/${id}/complete`, method: "POST" }),
      invalidatesTags: ["Document"],
    }),
    getDocumentSuggestions: builder.query<AnalysisResult, number>({
      query: (id) => `/user/documents/${id}/suggestions`,
    }),
  }),
});

export const {
  useGetDocumentsQuery,
  useGetDocumentByIdQuery,
  useCreateDocumentMutation,
  useUpdateDocumentMutation,
  useDeleteDocumentMutation,
  useCompleteDocumentMutation,
  useGetDocumentSuggestionsQuery,
} = documentApi;
