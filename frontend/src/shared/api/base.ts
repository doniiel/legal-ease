import { fetchBaseQuery } from "@reduxjs/toolkit/query/react";

// In Docker: nginx proxies /open-api/ → backend:9191/open-api/
// In local dev: set VITE_API_URL=http://localhost:9191/open-api in .env
export const BASE_URL = import.meta.env.VITE_API_URL ?? "/open-api";

export const baseQuery = fetchBaseQuery({
  baseUrl: BASE_URL,
  prepareHeaders: (headers) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    return headers;
  },
});
