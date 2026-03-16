import { useState } from "react";
import {
  App,
  Table,
  Button,
  Drawer,
  Form,
  Input,
  Select,
  Switch,
  InputNumber,
  Tag,
  Popconfirm,
  Space,
  Typography,
  Tooltip,
  Divider,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  FileCode,
  Search,
  Plus,
  Pencil,
  Trash2,
  Send,
  PlusCircle,
  Minus,
} from "lucide-react";
import {
  useGetMyTemplatesQuery,
  useCreateTemplateMutation,
  useUpdateTemplateMutation,
  useDeleteTemplateMutation,
  usePublishTemplateMutation,
  type Template,
  type TemplateFieldRequest,
  type FieldType,
} from "../../../features/lawyer/api/lawyer-template-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";

const { Title, Text } = Typography;
const { TextArea } = Input;
const { Option } = Select;

const FIELD_TYPES: { value: FieldType; label: string }[] = [
  { value: "TEXT", label: "Текст" },
  { value: "NUMBER", label: "Число" },
  { value: "DATE", label: "Дата" },
  { value: "BOOLEAN", label: "Да/Нет" },
  { value: "SELECT", label: "Выбор" },
];

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export default function LawyerTemplatesPanel() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<Template | null>(null);
  const [form] = Form.useForm();

  const { data, isLoading } = useGetMyTemplatesQuery({ page: page - 1, size: 10 });
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const categories = categoriesData ?? [];
  const [createTemplate, { isLoading: isCreating }] = useCreateTemplateMutation();
  const [updateTemplate, { isLoading: isUpdating }] = useUpdateTemplateMutation();
  const [deleteTemplate, { isLoading: isDeleting }] = useDeleteTemplateMutation();
  const [publishTemplate, { isLoading: isPublishing }] = usePublishTemplateMutation();

  const filtered = (data?.content ?? []).filter((t) => {
    const q = search.toLowerCase();
    return (
      t.title.toLowerCase().includes(q) ||
      t.category?.name?.toLowerCase().includes(q)
    );
  });

  const openCreate = () => {
    setEditingTemplate(null);
    form.resetFields();
    form.setFieldsValue({ fields: [{ fieldKey: "", label: "", fieldType: "TEXT", required: true, orderNum: 1 }] });
    setDrawerOpen(true);
  };

  const openEdit = (template: Template) => {
    setEditingTemplate(template);
    form.setFieldsValue({
      title: template.title,
      description: template.description,
      categoryId: template.category?.id,
      fields: template.fields.map((f) => ({
        fieldKey: f.fieldKey,
        label: f.label,
        fieldType: f.fieldType,
        required: f.required,
        orderNum: f.orderNum,
      })),
    });
    setDrawerOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const fields: TemplateFieldRequest[] = (values.fields ?? []).map(
      (f: TemplateFieldRequest, i: number) => ({ ...f, orderNum: i + 1 })
    );
    try {
      if (editingTemplate) {
        await updateTemplate({ id: editingTemplate.id, body: { ...values, fields } }).unwrap();
        message.success("Шаблон обновлён");
      } else {
        await createTemplate({ ...values, fields }).unwrap();
        message.success("Шаблон создан");
      }
      setDrawerOpen(false);
      form.resetFields();
    } catch (e) {
      if (isFetchError(e) && e.status === 403) {
        message.error("Нет доступа или шаблон уже опубликован");
      } else if (isFetchError(e) && e.status === 400) {
        message.error("Ошибка валидации данных");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTemplate(id).unwrap();
      message.success("Шаблон удалён");
    } catch (e) {
      if (isFetchError(e) && e.status === 403) {
        message.error("Нельзя удалить опубликованный шаблон");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishTemplate(id).unwrap();
      message.success("Шаблон опубликован");
    } catch (e) {
      if (isFetchError(e) && e.status === 409) {
        message.error("Шаблон уже опубликован");
      } else if (isFetchError(e) && e.status === 403) {
        message.error("Нет доступа к этому шаблону");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const columns: ColumnsType<Template> = [
    {
      title: "Название",
      dataIndex: "title",
      key: "title",
      render: (title: string) => (
        <Text strong style={{ fontSize: 13 }}>
          {title}
        </Text>
      ),
    },
    {
      title: "Категория",
      dataIndex: "category",
      key: "category",
      render: (_: unknown, record: Template) => (
        <Text style={{ fontSize: 13 }}>{record.category?.name || "—"}</Text>
      ),
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      width: 130,
      align: "center",
      render: (status: string) =>
        status === "PUBLISHED" ? (
          <Tag color="success">Опубликован</Tag>
        ) : (
          <Tag color="orange">Черновик</Tag>
        ),
    },
    {
      title: "Полей",
      key: "fields",
      width: 80,
      align: "center",
      render: (_, record) => (
        <Tag color="blue">{record.fields?.length ?? 0}</Tag>
      ),
    },
    {
      title: "Дата создания",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 150,
      render: (date: string) => (
        <Text type="secondary" style={{ fontSize: 12 }}>
          {new Date(date).toLocaleDateString("ru-RU")}
        </Text>
      ),
    },
    {
      title: "Действия",
      key: "actions",
      width: 140,
      render: (_, record) => {
        const isDraft = record.status === "DRAFT";
        return (
          <Space size={4}>
            <Tooltip title={isDraft ? "Редактировать" : "Нельзя редактировать опубликованный"}>
              <Button
                size="small"
                icon={<Pencil size={14} />}
                onClick={() => openEdit(record)}
                disabled={!isDraft}
              />
            </Tooltip>
            <Tooltip title={isDraft ? "Опубликовать" : "Уже опубликован"}>
              <Popconfirm
                title="Опубликовать шаблон?"
                description="После публикации шаблон нельзя изменить или удалить."
                onConfirm={() => handlePublish(record.id)}
                okText="Да"
                cancelText="Отмена"
                disabled={!isDraft}
              >
                <Button
                  size="small"
                  icon={<Send size={14} />}
                  disabled={!isDraft}
                  loading={isPublishing}
                  style={isDraft ? { borderColor: "#1677ff", color: "#1677ff" } : {}}
                />
              </Popconfirm>
            </Tooltip>
            <Tooltip title={isDraft ? "Удалить" : "Нельзя удалить опубликованный"}>
              <Popconfirm
                title="Удалить шаблон?"
                onConfirm={() => handleDelete(record.id)}
                okText="Да"
                cancelText="Отмена"
                disabled={!isDraft}
              >
                <Button
                  size="small"
                  danger
                  icon={<Trash2 size={14} />}
                  disabled={!isDraft}
                  loading={isDeleting}
                />
              </Popconfirm>
            </Tooltip>
          </Space>
        );
      },
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
          <FileCode size={26} color="#fff" />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
            Мои шаблоны
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Создание и управление шаблонами документов
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
              Создать
            </Button>
          </div>
        )}
      </div>

      {/* Search */}
      <div style={{ marginBottom: 16 }}>
        <Input
          prefix={<Search size={15} color="#9ca3af" />}
          placeholder="Поиск по названию или категории..."
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
          showTotal: (total) => `Всего ${total} шаблонов`,
        }}
        style={{ background: "#fff", borderRadius: 12 }}
      />

      {/* Create / Edit Drawer */}
      <Drawer
        title={editingTemplate ? "Редактировать шаблон" : "Новый шаблон"}
        open={drawerOpen}
        onClose={() => {
          setDrawerOpen(false);
          form.resetFields();
        }}
        width={640}
        footer={
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <Button onClick={() => setDrawerOpen(false)}>Отмена</Button>
            <Button
              type="primary"
              onClick={handleSubmit}
              loading={isCreating || isUpdating}
            >
              {editingTemplate ? "Сохранить" : "Создать"}
            </Button>
          </div>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="Название"
            name="title"
            rules={[{ required: true, message: "Введите название шаблона" }]}
          >
            <Input placeholder="Например: Договор купли-продажи" />
          </Form.Item>

          <Form.Item
            label="Описание"
            name="description"
            rules={[{ required: true, message: "Введите описание" }]}
          >
            <TextArea rows={3} placeholder="Краткое описание шаблона..." />
          </Form.Item>

          <Form.Item
            label="Категория"
            name="categoryId"
            rules={[{ required: true, message: "Выберите категорию" }]}
          >
            <Select
              showSearch
              placeholder="Выберите категорию"
              optionFilterProp="label"
              options={categories.map((c) => ({ value: c.id, label: c.name }))}
            />
          </Form.Item>

          <Divider  style={{ fontSize: 13, color: "#6b7280" }}>
            Поля шаблона
          </Divider>

          <Form.List name="fields">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <div
                    key={key}
                    style={{
                      background: "#f8fafc",
                      borderRadius: 10,
                      padding: "16px 16px 4px",
                      marginBottom: 12,
                      border: "1px solid #e5e7eb",
                      position: "relative",
                    }}
                  >
                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
                      <Form.Item
                        {...restField}
                        label="Ключ поля"
                        name={[name, "fieldKey"]}
                        rules={[{ required: true, message: "Обязательное поле" }]}
                      >
                        <Input placeholder="party_name" style={{ fontFamily: "monospace" }} />
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        label="Название"
                        name={[name, "label"]}
                        rules={[{ required: true, message: "Обязательное поле" }]}
                      >
                        <Input placeholder="Имя стороны" />
                      </Form.Item>
                    </div>
                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
                      <Form.Item
                        {...restField}
                        label="Тип"
                        name={[name, "fieldType"]}
                        rules={[{ required: true, message: "Выберите тип" }]}
                      >
                        <Select placeholder="Тип поля">
                          {FIELD_TYPES.map((ft) => (
                            <Option key={ft.value} value={ft.value}>
                              {ft.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        label="Порядок"
                        name={[name, "orderNum"]}
                        rules={[{ required: true, message: "Укажите порядок" }]}
                      >
                        <InputNumber min={1} style={{ width: "100%" }} />
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        label="Обязательное"
                        name={[name, "required"]}
                        valuePropName="checked"
                      >
                        <Switch checkedChildren="Да" unCheckedChildren="Нет" />
                      </Form.Item>
                    </div>
                    {fields.length > 1 && (
                      <Button
                        type="text"
                        danger
                        size="small"
                        icon={<Minus size={14} />}
                        onClick={() => remove(name)}
                        style={{ position: "absolute", top: 10, right: 10 }}
                      />
                    )}
                  </div>
                ))}
                <Button
                  type="dashed"
                  onClick={() =>
                    add({ fieldKey: "", label: "", fieldType: "TEXT", required: true, orderNum: fields.length + 1 })
                  }
                  icon={<PlusCircle size={15} />}
                  block
                  style={{ marginTop: 4 }}
                >
                  Добавить поле
                </Button>
              </>
            )}
          </Form.List>
        </Form>
      </Drawer>
    </div>
  );
}
