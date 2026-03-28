import { useState } from "react";
import { useGetAuditLogsQuery, type AuditLog } from "../api/admin-audit-api";

export function useAdminAudit() {
  const [page, setPage] = useState(1);
  const [emailSearch, setEmailSearch] = useState("");
  const [actionFilter, setActionFilter] = useState<string | undefined>(undefined);
  const [entityTypeFilter, setEntityTypeFilter] = useState<string | undefined>(undefined);
  const [detailDrawer, setDetailDrawer] = useState<{ open: boolean; record: AuditLog | null }>({
    open: false,
    record: null,
  });

  const { data, isLoading } = useGetAuditLogsQuery({
    page: page - 1,
    size: 20,
    sort: "createdAt,DESC",
  });

  const allLogs = data?.content ?? [];

  const actionOptions = [...new Set(allLogs.map((l) => l.action).filter(Boolean))];
  const entityTypeOptions = [...new Set(allLogs.map((l) => l.entityType).filter(Boolean))];

  const filtered = allLogs.filter((log) => {
    const emailMatch = emailSearch
      ? log.userEmail?.toLowerCase().includes(emailSearch.toLowerCase())
      : true;
    const actionMatch = actionFilter ? log.action === actionFilter : true;
    const entityMatch = entityTypeFilter ? log.entityType === entityTypeFilter : true;
    return emailMatch && actionMatch && entityMatch;
  });

  const uniqueUsers = new Set(allLogs.map((l) => l.userId).filter(Boolean)).size;

  const activeFilterCount = [emailSearch, actionFilter, entityTypeFilter].filter(Boolean).length;

  const resetFilters = () => {
    setEmailSearch("");
    setActionFilter(undefined);
    setEntityTypeFilter(undefined);
  };

  return {
    page, setPage,
    emailSearch, setEmailSearch,
    actionFilter, setActionFilter,
    entityTypeFilter, setEntityTypeFilter,
    detailDrawer, setDetailDrawer,
    data, isLoading,
    filtered, actionOptions, entityTypeOptions,
    uniqueUsers, activeFilterCount,
    resetFilters,
  };
}
