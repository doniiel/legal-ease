import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface DocumentReview {
  id: number;
  documentId: number;
  documentTitle: string;
  lawyerInfo: { id: number; fio: string; email: string };
  comment: string;
  status: "APPROVED" | "REJECTED" | "NEEDS_REVISION";
  createdDate: string;
}

export interface DocumentReviewsPage {
  content: DocumentReview[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface DocumentReviewRequest {
  comment: string;
  status: "APPROVED" | "REJECTED" | "NEEDS_REVISION";
}

export interface GetReviewsParams {
  page?: number;
  size?: number;
  sort?: string;
}

export const lawyerDocumentReviewsApi = createApi({
  reducerPath: "lawyerDocumentReviewsApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["DocumentReview"],
  endpoints: (builder) => ({
    createReview: builder.mutation<DocumentReview, { documentId: number; body: DocumentReviewRequest }>({
      query: ({ documentId, body }) => ({
        url: `/documents/${documentId}/reviews`,
        method: "POST",
        body,
      }),
      invalidatesTags: ["DocumentReview"],
    }),
    getDocumentReviews: builder.query<DocumentReview[], number>({
      query: (documentId) => `/documents/${documentId}/reviews`,
      providesTags: ["DocumentReview"],
    }),
    getMyReviews: builder.query<DocumentReviewsPage, GetReviewsParams>({
      query: ({ page = 0, size = 10, sort = "createdDate,DESC" } = {}) => ({
        url: "/reviews",
        params: { page, size, sort },
      }),
      providesTags: ["DocumentReview"],
    }),
    deleteReview: builder.mutation<void, number>({
      query: (reviewId) => ({ url: `/reviews/${reviewId}`, method: "DELETE" }),
      invalidatesTags: ["DocumentReview"],
    }),
  }),
});

export const {
  useCreateReviewMutation,
  useGetDocumentReviewsQuery,
  useGetMyReviewsQuery,
  useDeleteReviewMutation,
} = lawyerDocumentReviewsApi;
