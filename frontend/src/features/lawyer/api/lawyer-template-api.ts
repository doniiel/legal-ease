import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export type TemplateStatus = "DRAFT" | "PUBLISHED";
export type FieldType = "TEXT" | "NUMBER" | "DATE" | "BOOLEAN" | "SELECT";

export interface TemplateField {
  id: number;
  fieldKey: string;
  label: string;
  fieldType: FieldType;
  required: boolean;
  orderNum: number;
}

export interface TemplateLawyer {
  id: number;
  email: string;
  fio: string;
  iin: string;
}

export interface TemplateCategory {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface Template {
  id: number;
  title: string;
  description: string;
  category: TemplateCategory;
  status: TemplateStatus;
  lawyer: TemplateLawyer;
  fields: TemplateField[];
  createdDate: string;
  updatedDate: string;
}

export interface TemplatesPage {
  content: Template[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GetTemplatesParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface TemplateFieldRequest {
  fieldKey: string;
  label: string;
  fieldType: FieldType;
  required: boolean;
  orderNum: number;
}

export interface CreateTemplateRequest {
  title: string;
  description: string;
  categoryId: number;
  fields: TemplateFieldRequest[];
}

export interface UpdateTemplateRequest {
  title: string;
  description: string;
  categoryId: number;
  fields: TemplateFieldRequest[];
}

export const lawyerTemplateApi = createApi({
  reducerPath: "lawyerTemplateApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    prepareHeaders: (headers) => {
      const token = localStorage.getItem("accessToken");
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ["Template"],
  endpoints: (builder) => ({
    getMyTemplates: builder.query<TemplatesPage, GetTemplatesParams>({
      query: ({ page = 0, size = 10, sort = "createdDate,DESC" } = {}) => ({
        url: "/my-templates",
        params: { page, size, sort },
      }),
      providesTags: ["Template"],
    }),
    getTemplateById: builder.query<Template, number>({
      query: (id) => `/my-templates/${id}`,
      providesTags: (_result, _error, id) => [{ type: "Template", id }],
    }),
    createTemplate: builder.mutation<Template, CreateTemplateRequest>({
      query: (body) => ({
        url: "/my-templates",
        method: "POST",
        body,
      }),
      invalidatesTags: ["Template"],
    }),
    updateTemplate: builder.mutation<Template, { id: number; body: UpdateTemplateRequest }>({
      query: ({ id, body }) => ({
        url: `/my-templates/${id}`,
        method: "PUT",
        body,
      }),
      invalidatesTags: ["Template"],
    }),
    deleteTemplate: builder.mutation<void, number>({
      query: (id) => ({
        url: `/my-templates/${id}`,
        method: "DELETE",
      }),
      invalidatesTags: ["Template"],
    }),
    publishTemplate: builder.mutation<Template, number>({
      query: (id) => ({
        url: `/my-templates/${id}/publish`,
        method: "POST",
      }),
      invalidatesTags: ["Template"],
    }),
  }),
});

export const {
  useGetMyTemplatesQuery,
  useGetTemplateByIdQuery,
  useCreateTemplateMutation,
  useUpdateTemplateMutation,
  useDeleteTemplateMutation,
  usePublishTemplateMutation,
} = lawyerTemplateApi;
