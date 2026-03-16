import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  App,
  Table,
  Tag,
  Button,
  Space,
  Typography,
  Input,
  Modal,
  Form,
  Select,
  Popconfirm,
  Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  FileText,
  Plus,
  Search,
  Trash2,
  Eye,
  FilePen,
} from "lucide-react";
import {
  useGetDocumentsQuery,
  useCreateDocumentMutation,
  useDeleteDocumentMutation,
  type DocumentListItem,
} from "../../../features/documents/api/document-api";
import { useGetMyTemplatesQuery } from "../../../features/lawyer/api/lawyer-template-api";
import { ROUTES } from "../../../app/router/router";

const { Title, Text } = Typography;

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  DRAFT: { color: "orange", label: "Черновик" },
  COMPLETED: { color: "green", label: "Завершён" },
  PROCESSING: { color: "blue", label: "Обработка" },
};

export default function DocumentsListPanel() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const { data, isLoading } = useGetDocumentsQuery({ page: page - 1, size: 10 });
  const { data: templatesData } = useGetMyTemplatesQuery({ page: 0, size: 100 });
  const templates = (templatesData?.content ?? []).filter((t) => t.status === "PUBLISHED");
  const [createDocument, { isLoading: isCreating }] = useCreateDocumentMutation();
  const [deleteDocument, { isLoading: isDeleting }] = useDeleteDocumentMutation();

  const handleCreate = async (values: { templateId: number; title: string }) => {
    try {
      const doc = await createDocument({
        templateId: values.templateId,
        title: values.title,
        fieldValues: {},
      }).unwrap();
      message.success("Документ создан");
      setCreateOpen(false);
      form.resetFields();
      navigate(`${ROUTES.DOCUMENTS}/${doc.id}`);
    } catch {
      message.error("Ошибка при создании документа");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteDocument(id).unwrap();
      message.success("Документ удалён");
    } catch {
      message.error("Ошибка при удалении");
    }
  };

  const filtered = (data?.content ?? []).filter((d) => {
    const q = search.toLowerCase();
    return (
      d.title.toLowerCase().includes(q) ||
      d.templateTitle?.toLowerCase().includes(q) ||
      d.categoryName?.toLowerCase().includes(q)
    );
  });

  const columns: ColumnsType<DocumentListItem> = [
    {
      title: "Название",
      key: "title",
      render: (_, record) => (
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div
            style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              background: "#eff6ff",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
            }}
          >
            <FilePen size={16} color="#1d4ed8" />
          </div>
          <div>
            <Text strong style={{ display: "block", fontSize: 13 }}>
              {record.title}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.templateTitle}
            </Text>
          </div>
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      render: (v) => <Text style={{ fontSize: 13 }}>{v || "—"}</Text>,
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      render: (status) => {
        const cfg = STATUS_CONFIG[status] ?? { color: "default", label: status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    {
      title: "Создан",
      dataIndex: "createdDate",
      key: "createdDate",
      render: (v) => (
        <Text style={{ fontSize: 12, color: "#6b7280" }}>
          {new Date(v).toLocaleDateString("ru-KZ")}
        </Text>
      ),
    },
    {
      title: "Действия",
      key: "actions",
      width: 100,
      render: (_, record) => (
        <Space size={6}>
          <Tooltip title="Открыть">
            <Button
              size="small"
              icon={<Eye size={14} />}
              onClick={() => navigate(`${ROUTES.DOCUMENTS}/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="Удалить">
            <Popconfirm
              title="Удалить документ?"
              onConfirm={() => handleDelete(record.id)}
              okText="Да"
              cancelText="Отмена"
            >
              <Button
                size="small"
                icon={<Trash2 size={14} />}
                danger
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
      {/* Header */}
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
          <FileText size={26} color="#fff" />
        </div>
        <div style={{ flex: 1 }}>
          <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
            Мои документы
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Создавайте, редактируйте и анализируйте юридические документы
          </Text>
        </div>
        {data && (
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
        )}
      </div>

      {/* Toolbar */}
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, gap: 12 }}>
        <Input
          prefix={<Search size={15} color="#9ca3af" />}
          placeholder="Поиск по названию или категории..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: 360, borderRadius: 8 }}
        />
        <Button
          type="primary"
          icon={<Plus size={15} />}
          style={{ background: "#0F2A44", borderRadius: 8 }}
          onClick={() => setCreateOpen(true)}
        >
          Создать документ
        </Button>
      </div>

      {/* Table */}
      <Table
        columns={columns}
        dataSource={filtered}
        rowKey="id"
        loading={isLoading}
        onRow={(record) => ({
          style: { cursor: "pointer" },
          onClick: () => navigate(`${ROUTES.DOCUMENTS}/${record.id}`),
        })}
        pagination={{
          current: page,
          total: data?.totalElements,
          pageSize: 10,
          onChange: (p) => setPage(p),
          showSizeChanger: false,
          showTotal: (total) => `Всего ${total} документов`,
        }}
        style={{ background: "#fff", borderRadius: 12 }}
      />

      {/* Create Modal */}
      <Modal
        title="Создать документ"
        open={createOpen}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        onOk={() => form.submit()}
        okText="Создать"
        cancelText="Отмена"
        confirmLoading={isCreating}
        okButtonProps={{ style: { background: "#0F2A44" } }}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate} style={{ marginTop: 16 }}>
          <Form.Item
            name="templateId"
            label="Шаблон"
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
            name="title"
            label="Название документа"
            rules={[{ required: true, message: "Укажите название" }]}
          >
            <Input placeholder="Введите название" style={{ borderRadius: 8 }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
