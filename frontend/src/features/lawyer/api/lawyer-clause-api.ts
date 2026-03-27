import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface LegalClause {
  id: number;
  title: string;
  content: string;
  category: { id: number; name: string };
  createdDate: string;
  updatedDate: string;
}

export interface LegalClausesPage {
  content: LegalClause[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface LegalClauseRequest {
  title: string;
  content: string;
  categoryId: number;
}

export interface GetClausesParams {
  categoryId?: number;
  keyword?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const lawyerClauseApi = createApi({
  reducerPath: "lawyerClauseApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["Clause"],
  endpoints: (builder) => ({
    getClauses: builder.query<LegalClausesPage, GetClausesParams>({
      query: ({ categoryId, keyword, page = 0, size = 10, sort = "createdDate,DESC" } = {}) => ({
        url: "/lawyer/clauses",
        params: {
          ...(categoryId !== undefined && { categoryId }),
          ...(keyword !== undefined && { keyword }),
          page,
          size,
          sort,
        },
      }),
      providesTags: ["Clause"],
    }),
    getClauseById: builder.query<LegalClause, number>({
      query: (id) => `/lawyer/clauses/${id}`,
      providesTags: (_result, _error, id) => [{ type: "Clause", id }],
    }),
    createClause: builder.mutation<LegalClause, LegalClauseRequest>({
      query: (body) => ({ url: "/lawyer/clauses", method: "POST", body }),
      invalidatesTags: ["Clause"],
    }),
    updateClause: builder.mutation<LegalClause, { id: number; body: LegalClauseRequest }>({
      query: ({ id, body }) => ({ url: `/lawyer/clauses/${id}`, method: "PUT", body }),
      invalidatesTags: ["Clause"],
    }),
    deleteClause: builder.mutation<void, number>({
      query: (id) => ({ url: `/lawyer/clauses/${id}`, method: "DELETE" }),
      invalidatesTags: ["Clause"],
    }),
  }),
});

export const {
  useGetClausesQuery,
  useGetClauseByIdQuery,
  useCreateClauseMutation,
  useUpdateClauseMutation,
  useDeleteClauseMutation,
} = lawyerClauseApi;
