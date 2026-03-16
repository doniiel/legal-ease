import { useState } from "react";
import {
  App,
  Table,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  Tag,
  Popconfirm,
  Space,
  Typography,
  Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { GitMerge, Search, Plus, Pencil, Trash2 } from "lucide-react";
import {
  useGetRulesQuery,
  useCreateRuleMutation,
  useUpdateRuleMutation,
  useDeleteRuleMutation,
  type MatchingRule,
  type MatchingRuleRequest,
} from "../../../features/lawyer/api/lawyer-matching-rules-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";
import { useGetMyTemplatesQuery } from "../../../features/lawyer/api/lawyer-template-api";

const { Title, Text } = Typography;

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

export default function LawyerMatchingRulesPanel() {
  const { message } = App.useApp();
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<MatchingRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetRulesQuery();
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const { data: templatesData } = useGetMyTemplatesQuery({ page: 0, size: 100 });
  const [createRule, { isLoading: isCreating }] = useCreateRuleMutation();
  const [updateRule, { isLoading: isUpdating }] = useUpdateRuleMutation();
  const [deleteRule, { isLoading: isDeleting }] = useDeleteRuleMutation();

  const categories = categoriesData ?? [];
  const templates = templatesData?.content ?? [];

  const filtered = rules.filter((r) => {
    const q = search.toLowerCase();
    return (
      r.keywords?.toLowerCase().includes(q) ||
      r.templateTitle?.toLowerCase().includes(q) ||
      r.categoryName?.toLowerCase().includes(q)
    );
  });

  const openCreate = () => {
    setEditingRule(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (rule: MatchingRule) => {
    setEditingRule(rule);
    form.setFieldsValue({
      templateId: rule.templateId,
      categoryId: rule.categoryId,
      keywords: rule.keywords,
      baseScore: rule.baseScore,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body = values as MatchingRuleRequest;
    try {
      if (editingRule) {
        await updateRule({ id: editingRule.id, body }).unwrap();
        message.success("Правило обновлено");
      } else {
        await createRule(body).unwrap();
        message.success("Правило создано");
      }
      setModalOpen(false);
      form.resetFields();
    } catch (e) {
      if (isFetchError(e) && e.status === 400) {
        message.error("Ошибка валидации данных");
      } else if (isFetchError(e) && e.status === 404) {
        message.error("Шаблон или категория не найдены");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRule(id).unwrap();
      message.success("Правило удалено");
    } catch (e) {
      if (isFetchError(e) && e.status === 404) {
        message.error("Правило не найдено");
      } else {
        message.error("Произошла ошибка");
      }
    }
  };

  const columns: ColumnsType<MatchingRule> = [
    {
      title: "Шаблон",
      dataIndex: "templateTitle",
      key: "templateTitle",
      render: (title: string, record) => (
        <div>
          <Text strong style={{ fontSize: 13 }}>
            {title}
          </Text>
          <Text type="secondary" style={{ fontSize: 11, display: "block" }}>
            ID: {record.templateId}
          </Text>
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      render: (name: string) => (
        <Text style={{ fontSize: 13 }}>{name || "—"}</Text>
      ),
    },
    {
      title: "Ключевые слова",
      dataIndex: "keywords",
      key: "keywords",
      render: (kw: string) => (
        <Text style={{ fontSize: 13, fontFamily: "monospace", color: "#1677ff" }}>
          {kw || "—"}
        </Text>
      ),
    },
    {
      title: "Базовый балл",
      dataIndex: "baseScore",
      key: "baseScore",
      width: 130,
      align: "center",
      render: (score: number) => (
        <Tag color="blue" style={{ fontWeight: 600 }}>
          {score}
        </Tag>
      ),
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 110,
      align: "center",
      render: (active: boolean) =>
        active ? (
          <Tag color="success">Активно</Tag>
        ) : (
          <Tag color="default">Неактивно</Tag>
        ),
    },
    {
      title: "Действия",
      key: "actions",
      width: 100,
      align: "center",
      render: (_, record) => (
        <Space size={6}>
          <Tooltip title="Редактировать">
            <Button
              size="small"
              icon={<Pencil size={14} />}
              onClick={() => openEdit(record)}
            />
          </Tooltip>
          <Tooltip title="Удалить">
            <Popconfirm
              title="Удалить правило?"
              onConfirm={() => handleDelete(record.id)}
              okText="Да"
              cancelText="Отмена"
            >
              <Button
                size="small"
                danger
                icon={<Trash2 size={14} />}
                loading={isDeleting}
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
          <GitMerge size={26} color="#fff" />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
            подходящее правило
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Управление правилами сопоставления документов с шаблонами
          </Text>
        </div>
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
              {rules.length}
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
      </div>

      {/* Search */}
      <div style={{ marginBottom: 16 }}>
        <Input
          prefix={<Search size={15} color="#9ca3af" />}
          placeholder="Поиск по ключевым словам, шаблону или категории..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: 400, borderRadius: 8 }}
        />
      </div>

      {/* Table */}
      <Table
        columns={columns}
        dataSource={filtered}
        rowKey="id"
        loading={isLoading}
        pagination={{
          pageSize: 10,
          showSizeChanger: false,
          showTotal: (total) => `Всего ${total} правил`,
        }}
        style={{ background: "#fff", borderRadius: 12 }}
      />

      {/* Create / Edit Modal */}
      <Modal
        title={editingRule ? "Редактировать правило" : "Новое подходящее правило"}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false);
          form.resetFields();
        }}
        onOk={handleSubmit}
        okText={editingRule ? "Сохранить" : "Создать"}
        cancelText="Отмена"
        confirmLoading={isCreating || isUpdating}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            label="Шаблон"
            name="templateId"
            rules={[{ required: true, message: "Выберите шаблон" }]}
          >
            <Select
              showSearch
              placeholder="Выберите шаблон"
              optionFilterProp="label"
              options={templates.map((t) => ({ value: t.id, label: t.title }))}
            />
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
          <Form.Item
            label="Ключевые слова"
            name="keywords"
            rules={[{ required: true, message: "Введите ключевые слова" }]}
          >
            <Input placeholder="Например: купля, продажа, договор" />
          </Form.Item>
          <Form.Item
            label="Базовый балл"
            name="baseScore"
            rules={[{ required: true, message: "Укажите базовый балл" }]}
          >
            <InputNumber style={{ width: "100%" }} min={0} placeholder="100" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
