import { useState } from "react";
import { App } from "antd";
import {
  useGetMyTemplatesQuery,
  useDeleteTemplateMutation,
  usePublishTemplateMutation,
  type Template,
} from "../api/lawyer-template-api";

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export function useLawyerTemplates() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<Template | null>(null);

  const { data, isLoading } = useGetMyTemplatesQuery({ page: page - 1, size: 10, sort: "createdDate,DESC" });
  const { data: allData } = useGetMyTemplatesQuery({ page: 0, size: 1000 });
  const [deleteTemplate, { isLoading: isDeleting }] = useDeleteTemplateMutation();
  const [publishTemplate, { isLoading: isPublishing }] = usePublishTemplateMutation();

  const allTemplates = allData?.content ?? [];
  const draftCount = allTemplates.filter((t) => t.status === "DRAFT").length;
  const publishedCount = allTemplates.filter((t) => t.status === "PUBLISHED").length;
  const totalFields = allTemplates.reduce((sum, t) => sum + (t.fields?.length ?? 0), 0);

  const filtered = (data?.content ?? []).filter((t) => {
    const q = search.toLowerCase();
    const matchSearch = !search || t.title.toLowerCase().includes(q) || t.category?.name?.toLowerCase().includes(q);
    const matchStatus = !statusFilter || t.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const openCreate = () => { setEditingTemplate(null); setModalOpen(true); };
  const openEdit = (t: Template) => { setEditingTemplate(t); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditingTemplate(null); };

  const handleDelete = async (id: number) => {
    try {
      await deleteTemplate(id).unwrap();
      message.success("Шаблон удалён");
    } catch (e) {
      if (isFetchError(e) && e.status === 403) message.error("Нельзя удалить опубликованный шаблон");
      else message.error("Произошла ошибка");
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishTemplate(id).unwrap();
      message.success("Шаблон опубликован");
    } catch (e) {
      if (isFetchError(e) && e.status === 409) message.error("Шаблон уже опубликован");
      else message.error("Произошла ошибка");
    }
  };

  return {
    page, setPage,
    search, setSearch,
    statusFilter, setStatusFilter,
    modalOpen, editingTemplate,
    data, isLoading, isDeleting, isPublishing,
    filtered, draftCount, publishedCount, totalFields,
    openCreate, openEdit, closeModal,
    handleDelete, handlePublish,
  };
}
