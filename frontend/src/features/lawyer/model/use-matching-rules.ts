import { useState } from "react";
import { App, Form } from "antd";
import {
  useGetRulesQuery,
  useCreateRuleMutation,
  useUpdateRuleMutation,
  useDeleteRuleMutation,
  type MatchingRule,
  type MatchingRuleRequest,
} from "../api/lawyer-matching-rules-api";
import { useGetActiveCategoriesQuery } from "../../categories/api/public-category-api";
import { useGetMyTemplatesQuery } from "../api/lawyer-template-api";

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export function useMatchingRules() {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<MatchingRule | null>(null);

  const { data: rules = [], isLoading } = useGetRulesQuery();
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const { data: templatesData } = useGetMyTemplatesQuery({ page: 0, size: 100 });
  const [createRule, { isLoading: isCreating }] = useCreateRuleMutation();
  const [updateRule, { isLoading: isUpdating }] = useUpdateRuleMutation();
  const [deleteRule, { isLoading: isDeleting }] = useDeleteRuleMutation();

  const categories = categoriesData ?? [];
  const templates = templatesData?.content ?? [];
  const activeCount = rules.filter((r) => r.active).length;

  const filtered = rules.filter((r) => {
    const q = search.toLowerCase();
    return !search || r.keywords?.toLowerCase().includes(q) || r.templateTitle?.toLowerCase().includes(q) || r.categoryName?.toLowerCase().includes(q);
  });

  const openCreate = () => {
    setEditingRule(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (rule: MatchingRule) => {
    setEditingRule(rule);
    form.setFieldsValue({ templateId: rule.templateId, categoryId: rule.categoryId, keywords: rule.keywords, baseScore: rule.baseScore });
    setModalOpen(true);
  };

  const closeModal = () => { setModalOpen(false); form.resetFields(); };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    try {
      if (editingRule) {
        await updateRule({ id: editingRule.id, body: values as MatchingRuleRequest }).unwrap();
        message.success("Правило обновлено");
      } else {
        await createRule(values as MatchingRuleRequest).unwrap();
        message.success("Правило создано");
      }
      closeModal();
    } catch (e) {
      if (isFetchError(e) && e.status === 400) message.error("Ошибка валидации данных");
      else if (isFetchError(e) && e.status === 404) message.error("Шаблон или категория не найдены");
      else message.error("Произошла ошибка");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRule(id).unwrap();
      message.success("Правило удалено");
    } catch {
      message.error("Произошла ошибка");
    }
  };

  return {
    form, search, setSearch, modalOpen,
    editingRule, isLoading, isCreating, isUpdating, isDeleting,
    rules, filtered, activeCount,
    categories, templates,
    openCreate, openEdit, closeModal,
    handleSubmit, handleDelete,
  };
}
