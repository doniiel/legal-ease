import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import type { TemplateField, TemplateCategory, TemplateLawyer } from "../../lawyer/api/lawyer-template-api";

export interface UserTemplate {
  id: number;
  title: string;
  description: string;
  category: TemplateCategory;
  status: string;
  lawyer: TemplateLawyer;
  fields: TemplateField[];
  createdDate: string;
  updatedDate: string;
}

export interface UserTemplatesPage {
  content: UserTemplate[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GetUserTemplatesParams {
  categoryId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export const userTemplateApi = createApi({
  reducerPath: "userTemplateApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) headers.set("Authorization", `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ["UserTemplate"],
  endpoints: (builder) => ({
    getTemplates: builder.query<UserTemplatesPage, GetUserTemplatesParams>({
      query: ({ categoryId, page = 0, size = 10, sort = "createdDate,DESC" } = {}) => ({
        url: "/user/templates",
        params: { ...(categoryId !== undefined && { categoryId }), page, size, sort },
      }),
      providesTags: ["UserTemplate"],
    }),
    getTemplateById: builder.query<UserTemplate, number>({
      query: (id) => `/user/templates/${id}`,
      providesTags: (_result, _error, id) => [{ type: "UserTemplate", id }],
    }),
  }),
});

export const {
  useGetTemplatesQuery,
  useGetTemplateByIdQuery,
} = userTemplateApi;
