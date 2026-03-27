import type { AdminUser } from "../api/admin-user-api";

export const isLawyer = (user: AdminUser) => user.role === "LAWYER";
export const isAdmin  = (user: AdminUser) => user.role === "ADMIN";

export const getRoleLabel = (user: AdminUser): string =>
  isAdmin(user) ? "Админ" : isLawyer(user) ? "Адвокат" : "Пользователь";

export const getRoleColor = (user: AdminUser): string =>
  isAdmin(user) ? "red" : isLawyer(user) ? "blue" : "default";
