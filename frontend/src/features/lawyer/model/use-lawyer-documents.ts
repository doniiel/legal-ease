import { useState } from "react";
import { App, Form } from "antd";
import { useGetLawyerDocumentsQuery, useGetLawyerDocumentByIdQuery } from "../api/lawyer-document-api";
import { useCreateReviewMutation } from "../api/lawyer-document-reviews-api";
import type { DocumentReviewRequest } from "../api/lawyer-document-reviews-api";
import type { DocumentListItem } from "../../documents/api/document-api";

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export function useLawyerDocuments() {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [reviewModal, setReviewModal] = useState<{ open: boolean; documentId: number | null }>({ open: false, documentId: null });
  const [detailDrawer, setDetailDrawer] = useState<{ open: boolean; record: DocumentListItem | null }>({ open: false, record: null });
  const [selectedDocId, setSelectedDocId] = useState<number | null>(null);

  const { data, isLoading } = useGetLawyerDocumentsQuery({ page: page - 1, size: 10, sort: "createdDate,DESC" });
  const { data: allData } = useGetLawyerDocumentsQuery({ page: 0, size: 1000 });
  const { data: docDetail } = useGetLawyerDocumentByIdQuery(selectedDocId!, { skip: selectedDocId === null });
  const [createReview, { isLoading: isSubmitting }] = useCreateReviewMutation();

  const allDocs = allData?.content ?? [];
  const draftCount = allDocs.filter((d) => d.status === "DRAFT").length;
  const completedCount = allDocs.filter((d) => d.status === "COMPLETED").length;
  const processingCount = allDocs.filter((d) => d.status === "VALIDATED").length;

  const filtered = (data?.content ?? []).filter((d) => {
    const q = search.toLowerCase();
    const matchSearch = !search || d.title.toLowerCase().includes(q) || d.templateTitle?.toLowerCase().includes(q);
    const matchStatus = !statusFilter || d.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const openReview = (id: number) => {
    form.resetFields();
    setReviewModal({ open: true, documentId: id });
  };
  const closeReview = () => { setReviewModal({ open: false, documentId: null }); form.resetFields(); };

  const openDetail = (record: DocumentListItem) => {
    setSelectedDocId(record.id);
    setDetailDrawer({ open: true, record });
  };
  const closeDetail = () => { setDetailDrawer({ open: false, record: null }); };

  const handleSubmitReview = async () => {
    if (!reviewModal.documentId) return;
    try {
      const values = await form.validateFields();
      await createReview({ documentId: reviewModal.documentId, body: values as DocumentReviewRequest }).unwrap();
      message.success("Ревью добавлено");
      closeReview();
    } catch (e) {
      if (isFetchError(e) && e.status === 409) message.error("Ревью уже существует для этого документа");
      else message.error("Произошла ошибка");
    }
  };

  return {
    form, page, setPage,
    search, setSearch,
    statusFilter, setStatusFilter,
    reviewModal, detailDrawer,
    data, isLoading, isSubmitting,
    docDetail,
    filtered, draftCount, completedCount, processingCount,
    openReview, closeReview,
    openDetail, closeDetail,
    handleSubmitReview,
  };
}
