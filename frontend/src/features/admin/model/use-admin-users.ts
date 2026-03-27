import { useState } from "react";
import { App } from "antd";
import {
  useGetUsersQuery,
  useBlockUserMutation,
  useUnblockUserMutation,
  useRevokeLawyerMutation,
} from "../api/admin-user-api";
import { useGetSystemMetricsQuery } from "../api/admin-metrics-api";
import { isAdmin, isLawyer } from "../lib/user-role";

export interface ConfirmModal {
  userId: number;
  block: boolean;
}

export function useAdminUsers() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [blockedUsers, setBlockedUsers] = useState<Set<number>>(new Set());
  const [confirmModal, setConfirmModal] = useState<ConfirmModal | null>(null);

  const { data, isLoading } = useGetUsersQuery({ page: page - 1, size: 10 });
  const { data: metrics, isLoading: isMetricsLoading } = useGetSystemMetricsQuery();
  const [blockUser,    { isLoading: isBlocking  }] = useBlockUserMutation();
  const [unblockUser,  { isLoading: isUnblocking }] = useUnblockUserMutation();
  const [revokeLawyer, { isLoading: isRevoking  }] = useRevokeLawyerMutation();

  const filtered = (data?.content ?? []).filter((u) => {
    const q = search.toLowerCase();
    const textMatch =
      u.email.toLowerCase().includes(q) ||
      u.firstName.toLowerCase().includes(q) ||
      u.lastName.toLowerCase().includes(q) ||
      u.username?.toLowerCase().includes(q);

    const roleMatch =
      roleFilter === "ALL" ||
      (roleFilter === "ADMIN"  && isAdmin(u)) ||
      (roleFilter === "LAWYER" && isLawyer(u)) ||
      (roleFilter === "USER"   && !isAdmin(u) && !isLawyer(u));

    return textMatch && roleMatch;
  });

  const handleConfirmToggle = async () => {
    if (!confirmModal) return;
    const { userId, block } = confirmModal;
    try {
      if (block) {
        await blockUser(userId).unwrap();
        setBlockedUsers((prev) => new Set(prev).add(userId));
        message.success("Пользователь заблокирован");
      } else {
        await unblockUser(userId).unwrap();
        setBlockedUsers((prev) => {
          const next = new Set(prev);
          next.delete(userId);
          return next;
        });
        message.success("Пользователь разблокирован");
      }
    } catch {
      message.error(block ? "Ошибка при блокировке" : "Ошибка при разблокировке");
    } finally {
      setConfirmModal(null);
    }
  };

  const handleRevoke = async (id: number) => {
    try {
      await revokeLawyer(id).unwrap();
      message.success("Статус адвоката отозван");
    } catch {
      message.error("Ошибка при отзыве статуса");
    }
  };

  return {
    // data
    data, metrics, filtered,
    // loading states
    isLoading, isMetricsLoading, isBlocking, isUnblocking, isRevoking,
    // pagination
    page, setPage,
    // filters
    search, setSearch, roleFilter, setRoleFilter,
    // drawer
    selectedUserId, setSelectedUserId,
    // block modal
    blockedUsers, confirmModal, setConfirmModal,
    // handlers
    handleConfirmToggle, handleRevoke,
  };
}
