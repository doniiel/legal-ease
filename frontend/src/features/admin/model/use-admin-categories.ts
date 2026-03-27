import { useState } from "react";
import { App } from "antd";
import {
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeactivateCategoryMutation,
  type Category,
  type UpdateCategoryRequest,
} from "../api/admin-category-api";

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export function useAdminCategories() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  const { data, isLoading } = useGetCategoriesQuery({ page: page - 1, size: 10 });
  const [createCategory, { isLoading: isCreating }] = useCreateCategoryMutation();
  const [updateCategory, { isLoading: isUpdating }] = useUpdateCategoryMutation();
  const [deactivateCategory, { isLoading: isDeactivating }] = useDeactivateCategoryMutation();

  const filtered = (data?.content ?? []).filter((c) => {
    const q = search.toLowerCase();
    return c.name.toLowerCase().includes(q) || c.description?.toLowerCase().includes(q);
  });

  const activeCount   = (data?.content ?? []).filter((c) => c.active).length;
  const inactiveCount = (data?.content ?? []).filter((c) => !c.active).length;

  const openCreate = (resetFields: () => void) => {
    setEditingCategory(null);
    resetFields();
    setModalOpen(true);
  };

  const openEdit = (category: Category, setFieldsValue: (v: object) => void) => {
    setEditingCategory(category);
    setFieldsValue({ name: category.name, description: category.description, active: category.active });
    setModalOpen(true);
  };

  const closeModal = (resetFields: () => void) => {
    setModalOpen(false);
    resetFields();
  };

  const handleSubmit = async (values: UpdateCategoryRequest, resetFields: () => void) => {
    try {
      if (editingCategory) {
        await updateCategory({ id: editingCategory.id, body: values }).unwrap();
        message.success("Категория обновлена");
      } else {
        await createCategory({ name: values.name, description: values.description }).unwrap();
        message.success("Категория создана");
      }
      setModalOpen(false);
      resetFields();
    } catch (e) {
      if (isFetchError(e) && e.status === 409) message.error("Категория с таким названием уже существует");
      else if (isFetchError(e) && e.status === 400) message.error("Ошибка валидации данных");
      else message.error("Произошла ошибка");
    }
  };

  const handleDeactivate = async (id: number) => {
    try {
      await deactivateCategory(id).unwrap();
      message.success("Категория деактивирована");
    } catch (e) {
      if (isFetchError(e) && e.status === 409) message.error("Нельзя деактивировать: есть связанные шаблоны");
      else message.error("Произошла ошибка");
    }
  };

  const handleActivate = async (category: Category) => {
    try {
      await updateCategory({ id: category.id, body: { name: category.name, description: category.description ?? "", active: true } }).unwrap();
      message.success("Категория активирована");
    } catch {
      message.error("Произошла ошибка");
    }
  };

  return {
    data, isLoading, filtered,
    activeCount, inactiveCount,
    page, setPage,
    search, setSearch,
    modalOpen, editingCategory,
    isCreating, isUpdating, isDeactivating,
    openCreate, openEdit, closeModal, handleSubmit, handleDeactivate, handleActivate,
  };
}
