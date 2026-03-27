import { useState, useMemo } from "react";
import { App } from "antd";
import type { Dayjs } from "dayjs";
import {
  useGetApplicationsQuery,
  useApproveApplicationMutation,
  useRejectApplicationMutation,
  useDeleteApplicationMutation,
  type AdminLawyerApplication,
} from "../api/admin-lawyer-api";

export interface AppFilters {
  status?: "PENDING" | "APPROVED" | "REJECTED";
  licenseNumber?: string;
  userId?: number;
  reviewerFio?: string;
  createdRange?: [Dayjs, Dayjs] | null;
  reviewedRange?: [Dayjs, Dayjs] | null;
  page: number;
  size: number;
}

export const EMPTY_FILTERS: AppFilters = { page: 0, size: 10 };

export function useAdminApplications() {
  const { message } = App.useApp();
  const [filters, setFilters] = useState<AppFilters>(EMPTY_FILTERS);
  const [loadingIds, setLoadingIds] = useState<number[]>([]);

  const [rejectModal,   setRejectModal]   = useState<{ open: boolean; id: number | null }>({ open: false, id: null });
  const [detailDrawer,  setDetailDrawer]  = useState<{ open: boolean; record: AdminLawyerApplication | null }>({ open: false, record: null });
  const [filterModalOpen, setFilterModalOpen] = useState(false);

  const { data, isLoading, isFetching } = useGetApplicationsQuery({
    status: filters.status,
    licenseNumber: filters.licenseNumber,
    userId: filters.userId,
    page: 0,
    size: 200,
  });

  const { data: statsAll }      = useGetApplicationsQuery({ page: 0, size: 1 });
  const { data: statsPending }  = useGetApplicationsQuery({ status: "PENDING",  page: 0, size: 1 });
  const { data: statsApproved } = useGetApplicationsQuery({ status: "APPROVED", page: 0, size: 1 });
  const { data: statsRejected } = useGetApplicationsQuery({ status: "REJECTED", page: 0, size: 1 });

  const [approveApplication] = useApproveApplicationMutation();
  const [rejectApplication]  = useRejectApplicationMutation();
  const [deleteApplication]  = useDeleteApplicationMutation();

  const stats = {
    total:    statsAll?.totalElements      ?? 0,
    pending:  statsPending?.totalElements  ?? 0,
    approved: statsApproved?.totalElements ?? 0,
    rejected: statsRejected?.totalElements ?? 0,
  };

  const filtered = useMemo(() => {
    const content = data?.content ?? [];
    return content.filter((a) => {
      if (filters.reviewerFio && !a.reviewerInfo?.fio.toLowerCase().includes(filters.reviewerFio.toLowerCase())) return false;
      if (filters.createdRange) {
        const t = new Date(a.submittedAt).getTime();
        if (t < filters.createdRange[0].startOf("day").valueOf()) return false;
        if (t > filters.createdRange[1].endOf("day").valueOf()) return false;
      }
      if (filters.reviewedRange && a.reviewedAt) {
        const t = new Date(a.reviewedAt).getTime();
        if (t < filters.reviewedRange[0].startOf("day").valueOf()) return false;
        if (t > filters.reviewedRange[1].endOf("day").valueOf()) return false;
      }
      return true;
    });
  }, [data, filters]);

  const paginatedData = filtered.slice(
    filters.page * filters.size,
    filters.page * filters.size + filters.size,
  );

  const activeFilterCount = Object.keys(filters).filter(
    (k) => !["page", "size"].includes(k) && filters[k as keyof AppFilters] != null,
  ).length;

  const withLoading = async (id: number, fn: () => Promise<void>) => {
    setLoadingIds((ids) => [...ids, id]);
    try { await fn(); }
    finally { setLoadingIds((ids) => ids.filter((i) => i !== id)); }
  };

  const handleApprove = (id: number) =>
    withLoading(id, async () => {
      await approveApplication(id).unwrap();
      message.success("Заявка успешно одобрена");
    }).catch(() => message.error("Ошибка при одобрении заявки"));

  const handleRejectSubmit = (values: { reason: string }) => {
    if (!rejectModal.id) return;
    const id = rejectModal.id;
    withLoading(id, async () => {
      await rejectApplication({ id, reason: values.reason }).unwrap();
      message.success("Заявка отклонена");
      setRejectModal({ open: false, id: null });
    }).catch(() => message.error("Ошибка при отклонении заявки"));
  };

  const handleDelete = (id: number) =>
    withLoading(id, async () => {
      await deleteApplication(id).unwrap();
      message.success("Заявка удалена");
    }).catch(() => message.error("Ошибка при удалении заявки"));

  return {
    filters, setFilters,
    data, isLoading, isFetching,
    stats, filtered, paginatedData,
    loadingIds, activeFilterCount,
    rejectModal, setRejectModal,
    detailDrawer, setDetailDrawer,
    filterModalOpen, setFilterModalOpen,
    handleApprove, handleRejectSubmit, handleDelete,
  };
}
