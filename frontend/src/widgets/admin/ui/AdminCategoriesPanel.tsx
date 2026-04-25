import { useState } from "react";
import {
  App, Table, Modal, Form, Input, Switch, Popconfirm, Space, Typography, Tooltip, Button,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  FolderOpen, Search, Plus, FilePen, CircleSlash, CircleCheck,
  FolderCheck, FolderX, AlertTriangle,
} from "lucide-react";
import { useAdminCategories } from "../../../features/admin/model/use-admin-categories";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";
import type { Category, UpdateCategoryRequest } from "../../../features/admin/api/admin-category-api";

const { Text } = Typography;
const { TextArea } = Input;

// ─── Status pill ──────────────────────────────────────────────
function StatusPill({ active }: { active: boolean }) {
  return (
    <div
      style={{
        display: "inline-flex", alignItems: "center", gap: 6,
        padding: "4px 12px", borderRadius: 20,
        background: active ? "rgba(5,150,105,0.08)" : "rgba(148,163,184,0.12)",
        border: `1px solid ${active ? "rgba(5,150,105,0.2)" : "rgba(148,163,184,0.25)"}`,
      }}
    >
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: active ? "#059669" : "#94a3b8", flexShrink: 0 }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: active ? "#059669" : "#94a3b8", letterSpacing: "0.04em" }}>
        {active ? "Активна" : "Неактивна"}
      </span>
    </div>
  );
}

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({
  icon, tooltip, onClick, hoverBg, hoverColor, disabled = false,
}: {
  icon: React.ReactNode; tooltip: string; onClick?: () => void;
  hoverBg: string; hoverColor: string; disabled?: boolean;
}) {
  return (
    <Tooltip title={tooltip}>
      <button
        disabled={disabled}
        onClick={onClick}
        style={{
          width: 32, height: 32, borderRadius: 8, border: "none",
          background: "transparent", cursor: disabled ? "not-allowed" : "pointer",
          color: disabled ? "#cbd5e1" : "#64748b",
          display: "flex", alignItems: "center", justifyContent: "center",
          transition: "background 0.15s, color 0.15s",
        }}
        onMouseEnter={(e) => { if (!disabled) { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; } }}
        onMouseLeave={(e) => { if (!disabled) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; } }}
      >
        {icon}
      </button>
    </Tooltip>
  );
}

// ─── Deactivate confirm modal ─────────────────────────────────
function DeactivateModal({
  category,
  loading,
  onConfirm,
  onCancel,
}: {
  category: Category | null;
  loading: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <Modal open={category !== null} footer={null} onCancel={onCancel} centered width={420} closable={false}>
      <div style={{ textAlign: "center", padding: "8px 0 4px" }}>
        <div
          style={{
            width: 60, height: 60, borderRadius: "50%",
            background: "#fff7ed",
            display: "flex", alignItems: "center", justifyContent: "center",
            margin: "0 auto 20px",
            border: "2px solid rgba(245,158,11,0.2)",
          }}
        >
          <AlertTriangle size={26} color="#f59e0b" />
        </div>

        <div style={{ fontSize: 18, fontWeight: 800, color: "#0b1c30", marginBottom: 8, fontFamily: "Manrope, sans-serif" }}>
          Деактивировать категорию?
        </div>
        <div style={{ fontSize: 13, color: "#64748b", marginBottom: 6, lineHeight: 1.6 }}>
          Категория <strong style={{ color: "#1a2744" }}>«{category?.name}»</strong> будет скрыта из системы.
        </div>
        <div
          style={{
            fontSize: 12, color: "#92400e", background: "#fffbeb",
            border: "1px solid rgba(245,158,11,0.25)", borderRadius: 8,
            padding: "10px 16px", marginBottom: 28, lineHeight: 1.6,
          }}
        >
          ⚠ Нельзя деактивировать категорию, если к ней привязаны активные шаблоны.
        </div>

        <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>
          <Button onClick={onCancel} style={{ borderRadius: 8, minWidth: 110 }}>
            Отмена
          </Button>
          <Button
            loading={loading}
            onClick={onConfirm}
            style={{
              borderRadius: 8, minWidth: 110,
              background: "#f59e0b", borderColor: "#f59e0b", color: "#fff", fontWeight: 600,
            }}
          >
            Деактивировать
          </Button>
        </div>
      </div>
    </Modal>
  );
}

// ─── Main panel ───────────────────────────────────────────────
export default function AdminCategoriesPanel() {
  const isMobile = useIsMobile();
  const { message } = App.useApp();
  void message;
  const [form] = Form.useForm();
  const [deactivateTarget, setDeactivateTarget] = useState<Category | null>(null);

  const {
    data, isLoading, filtered,
    activeCount, inactiveCount,
    page, setPage,
    search, setSearch,
    modalOpen, editingCategory,
    isCreating, isUpdating, isDeactivating,
    openCreate, openEdit, closeModal, handleSubmit, handleDeactivate, handleActivate,
  } = useAdminCategories();

  const columns: ColumnsType<Category> = [
    {
      title: "Название",
      key: "name",
      render: (_, record) => (
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div
            style={{
              width: 38, height: 38, borderRadius: 10, flexShrink: 0,
              background: record.active ? "#eff4ff" : "#f8fafc",
              display: "flex", alignItems: "center", justifyContent: "center",
              border: `1px solid ${record.active ? "#dce9ff" : "#e2e8f0"}`,
            }}
          >
            <FolderOpen size={18} color={record.active ? "#1a2744" : "#94a3b8"} />
          </div>
          <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>{record.name}</Text>
        </div>
      ),
    },
    {
      title: "Описание",
      dataIndex: "description",
      key: "description",
      render: (v) => <Text style={{ fontSize: 13, color: "#64748b" }}>{v || "—"}</Text>,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 140,
      render: (active: boolean) => <StatusPill active={active} />,
    },
    {
      title: "Действия",
      key: "actions",
      width: 110,
      render: (_, record) => (
        <Space size={2}>
          {/* Edit */}
          <IconBtn
            icon={<FilePen size={15} />}
            tooltip="Редактировать"
            hoverBg="#f1f5f9" hoverColor="#1a2744"
            onClick={() => openEdit(record, form.setFieldsValue)}
          />

          {/* Deactivate → custom modal */}
          {record.active && (
            <IconBtn
              icon={isDeactivating && deactivateTarget?.id === record.id
                ? <span style={{ fontSize: 12 }}>…</span>
                : <CircleSlash size={15} />
              }
              tooltip="Деактивировать"
              hoverBg="#fff7ed" hoverColor="#f59e0b"
              onClick={() => setDeactivateTarget(record)}
            />
          )}

          {/* Activate → keep Popconfirm (low risk action) */}
          {!record.active && (
            <Popconfirm
              title="Активировать категорию?"
              onConfirm={() => handleActivate(record)}
              okText="Да" cancelText="Отмена"
            >
              <span>
                <IconBtn
                  icon={isUpdating ? <span style={{ fontSize: 12 }}>…</span> : <CircleCheck size={15} />}
                  tooltip="Активировать"
                  hoverBg="#f0fdf4" hoverColor="#059669"
                />
              </span>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Hero ── */}
      <div style={{ background: "#1a2744", padding: isMobile ? "24px 16px 40px" : "40px 40px 56px" }}>
        <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
          Управление категориями
        </h1>
        <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>
          Создание, редактирование и управление статусом категорий документов
        </p>
      </div>

      <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(3, 1fr)", gap: isMobile ? 12 : 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего категорий" value={data?.totalElements} color="#1a2744" icon={<FolderOpen size={26} />} loading={isLoading} />
          <StatCard label="Активных"        value={activeCount}         color="#059669"  icon={<FolderCheck size={26} />} />
          <StatCard label="Неактивных"      value={inactiveCount}       color="#94a3b8"  icon={<FolderX size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 16 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <div style={{ position: "relative" }}>
                <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" }}>
                  <Search size={15} color="#757682" />
                </span>
                <Input
                  placeholder="Название или описание категории..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  style={{ paddingLeft: 36, borderRadius: 12, background: "#eff4ff", border: "none" }}
                  allowClear
                />
              </div>
            </div>
            <Button
              icon={<Plus size={15} />}
              onClick={() => openCreate(form.resetFields)}
              style={{ borderRadius: 12, background: "#1a2744", borderColor: "#1a2744", color: "#fff", height: 40, paddingLeft: 20, paddingRight: 20, fontWeight: 600 }}
            >
              Добавить
            </Button>
          </div>
        </div>

        {/* ── Table ── */}
        <div style={{ background: "#fff", borderRadius: 16, boxShadow: "0 8px 32px rgba(11,28,48,0.04)", overflow: "hidden" }}>
          <Table
            components={editorialTableComponents}
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
              style: { padding: "16px 32px", margin: 0 },
            }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Deactivate confirm modal ── */}
      <DeactivateModal
        category={deactivateTarget}
        loading={isDeactivating}
        onConfirm={async () => {
          if (deactivateTarget) {
            await handleDeactivate(deactivateTarget.id);
            setDeactivateTarget(null);
          }
        }}
        onCancel={() => setDeactivateTarget(null)}
      />

      {/* ── Create / Edit modal ── */}
      <Modal
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#eff4ff", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <FolderOpen size={16} color="#1a2744" />
            </div>
            <span style={{ fontWeight: 700, color: "#0b1c30" }}>
              {editingCategory ? "Редактировать категорию" : "Новая категория"}
            </span>
          </div>
        }
        open={modalOpen}
        onCancel={() => closeModal(form.resetFields)}
        onOk={async () => {
          const values = await form.validateFields();
          await handleSubmit(values as UpdateCategoryRequest, form.resetFields);
        }}
        okText={editingCategory ? "Сохранить" : "Создать"}
        cancelText="Отмена"
        confirmLoading={isCreating || isUpdating}
        okButtonProps={{ style: { background: "#1a2744", borderColor: "#1a2744", borderRadius: 8 } }}
        cancelButtonProps={{ style: { borderRadius: 8 } }}
        destroyOnHidden
        width={480}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Название" name="name" rules={[{ required: true, message: "Введите название категории" }]}>
            <Input placeholder="Например: Гражданское право" style={{ borderRadius: 8 }} />
          </Form.Item>
          <Form.Item label="Описание" name="description" rules={[{ required: true, message: "Введите описание" }]}>
            <TextArea rows={3} placeholder="Краткое описание категории..." style={{ borderRadius: 8 }} />
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
