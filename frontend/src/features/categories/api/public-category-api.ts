import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { BASE_URL } from "../../../shared/api";

export interface PublicCategory {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export const publicCategoryApi = createApi({
  reducerPath: "publicCategoryApi",
  baseQuery: fetchBaseQuery({ baseUrl: BASE_URL }),
  tagTypes: ["PublicCategory"],
  endpoints: (builder) => ({
    getActiveCategories: builder.query<PublicCategory[], void>({
      query: () => "/categories",
      providesTags: ["PublicCategory"],
    }),
  }),
});

export const { useGetActiveCategoriesQuery } = publicCategoryApi;
