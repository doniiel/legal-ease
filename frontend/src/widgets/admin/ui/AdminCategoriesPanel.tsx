import { useState } from "react";
import {
  App,
  Table,
  Button,
  Modal,
  Form,
  Input,
  Switch,
  Tag,
  Popconfirm,
  Space,
  Typography,
  Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { FolderOpen, Search, Plus, Pencil, PowerOff } from "lucide-react";
import {
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  useUpdateCategoryMutation,
  useDeactivateCategoryMutation,
  type Category,
  type UpdateCategoryRequest,
} from "../../../features/admin/api/admin-category-api";

const { Title, Text } = Typography;
const { TextArea } = Input;

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export default function AdminCategoriesPanel() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [form] = Form.useForm();

  const { data, isLoading } = useGetCategoriesQuery({ page: page - 1, size: 10 });
  const [createCategory, { isLoading: isCreating }] = useCreateCategoryMutation();
  const [updateCategory, { isLoading: isUpdating }] = useUpdateCategoryMutation();
  const [deactivateCategory, { isLoading: isDeactivating }] = useDeactivateCategoryMutation();

  const filtered = (data?.content ?? []).filter((c) => {
    const q = search.toLowerCase();
    return (
      c.name.toLowerCase().includes(q) ||
      c.description?.toLowerCase().includes(q)
    );
  });

  const openCreate = () => {
    setEditingCategory(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (category: Category) => {
    setEditingCategory(category);
    form.setFieldsValue({
      name: category.name,
      description: category.description,
      active: category.active,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    try {
      if (editingCategory) {
        await updateCategory({
          id: editingCategory.id,
          body: values as UpdateCategoryRequest,
        }).unwrap();
        message.success("Категория обновлена");
      } else {
        await createCategory({ name: values.name, description: values.description }).unwrap();
        message.success("Категория создана");
      }
      setModalOpen(false);
      form.resetFields();
    } catch (e) {
      if (isFetchError(e) && e.status === 409) {
        message.error("Категория с таким названием уже существует");
      } else if (isFetchError(e) && e.status === 400) {
        message.error("Ошибка валидации данных");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const handleDeactivate = async (id: number) => {
    try {
      await deactivateCategory(id).unwrap();
      message.success("Категория деактивирована");
    } catch (e) {
      if (isFetchError(e) && e.status === 409) {
        message.error("Нельзя деактивировать: есть связанные шаблоны");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const columns: ColumnsType<Category> = [
    {
      title: "Название",
      dataIndex: "name",
      key: "name",
      render: (name: string) => (
        <Text strong style={{ fontSize: 13 }}>
          {name}
        </Text>
      ),
    },
    {
      title: "Описание",
      dataIndex: "description",
      key: "description",
      render: (desc: string) => (
        <Text type="secondary" style={{ fontSize: 13 }}>
          {desc || "—"}
        </Text>
      ),
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 120,
      align: "center",
      render: (active: boolean) =>
        active ? (
          <Tag color="success">Активна</Tag>
        ) : (
          <Tag color="default">Неактивна</Tag>
        ),
    },
    {
      title: "Действия",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Space size={6}>
          <Tooltip title="Редактировать">
            <Button
              size="small"
              icon={<Pencil size={14} />}
              onClick={() => openEdit(record)}
            />
          </Tooltip>
          <Tooltip title={record.active ? "Деактивировать" : "Уже неактивна"}>
            <Popconfirm
              title="Деактивировать категорию?"
              description="Нельзя применить, если есть связанные шаблоны."
              onConfirm={() => handleDeactivate(record.id)}
              okText="Да"
              cancelText="Отмена"
              disabled={!record.active}
            >
              <Button
                size="small"
                icon={<PowerOff size={14} />}
                danger
                disabled={!record.active}
                loading={isDeactivating}
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* Header banner */}
      <div
        style={{
          background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
          borderRadius: 16,
          padding: "28px 36px",
          marginBottom: 24,
          display: "flex",
          alignItems: "center",
          gap: 20,
        }}
      >
        <div
          style={{
            width: 52,
            height: 52,
            borderRadius: 14,
            background: "rgba(255,255,255,0.15)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flexShrink: 0,
          }}
        >
          <FolderOpen size={26} color="#fff" />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
            Управление категориями
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Создание, редактирование и деактивация категорий
          </Text>
        </div>
        {data && (
          <div style={{ marginLeft: "auto", display: "flex", gap: 12, alignItems: "center" }}>
            <div
              style={{
                background: "rgba(255,255,255,0.12)",
                borderRadius: 10,
                padding: "8px 18px",
                textAlign: "center",
              }}
            >
              <Text style={{ color: "#fff", fontSize: 22, fontWeight: 700, display: "block" }}>
                {data.totalElements}
              </Text>
              <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 12 }}>Всего</Text>
            </div>
            <Button
              icon={<Plus size={16} />}
              onClick={openCreate}
              style={{
                background: "rgba(255,255,255,0.15)",
                border: "1px solid rgba(255,255,255,0.3)",
                color: "#fff",
                borderRadius: 8,
              }}
            >
              Добавить
            </Button>
          </div>
        )}
      </div>

      {/* Search */}
      <div style={{ marginBottom: 16 }}>
        <Input
          prefix={<Search size={15} color="#9ca3af" />}
          placeholder="Поиск по названию или описанию..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: 360, borderRadius: 8 }}
        />
      </div>

      {/* Table */}
      <Table
        columns={columns}
        dataSource={filtered}
        rowKey="id"
        loading={isLoading}
        pagination={{
          current: page,
          total: data?.totalElements,
          pageSize: 10,
          onChange: (p) => setPage(p),
          showSizeChanger: false,
          showTotal: (total) => `Всего ${total} категорий`,
        }}
        style={{ background: "#fff", borderRadius: 12 }}
      />

      {/* Create / Edit Modal */}
      <Modal
        title={editingCategory ? "Редактировать категорию" : "Новая категория"}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false);
          form.resetFields();
        }}
        onOk={handleSubmit}
        okText={editingCategory ? "Сохранить" : "Создать"}
        cancelText="Отмена"
        confirmLoading={isCreating || isUpdating}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            label="Название"
            name="name"
            rules={[{ required: true, message: "Введите название категории" }]}
          >
            <Input placeholder="Например: Гражданское право" />
          </Form.Item>
          <Form.Item
            label="Описание"
            name="description"
            rules={[{ required: true, message: "Введите описание" }]}
          >
            <TextArea rows={3} placeholder="Краткое описание категории..." />
          </Form.Item>
          {editingCategory && (
            <Form.Item label="Активна" name="active" valuePropName="checked">
              <Switch checkedChildren="Да" unCheckedChildren="Нет" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
}
