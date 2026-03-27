import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const publicShareApi = createApi({
  reducerPath: "publicShareApi",
  baseQuery: fetchBaseQuery({ baseUrl: "/api" }),
  endpoints: (builder) => ({
    getSharedDocument: builder.query<Blob, string>({
      query: (token) => ({
        url: `/public/share/${token}`,
        responseHandler: (response) => response.blob(),
      }),
    }),
  }),
});

export const { useGetSharedDocumentQuery } = publicShareApi;
