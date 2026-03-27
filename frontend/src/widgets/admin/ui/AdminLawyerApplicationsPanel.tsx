import {
  App, Table, Modal, Form, Input, Select, Typography, Descriptions,
  Drawer, DatePicker, Avatar, Divider, Badge, Tooltip, Button, Space,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  Eye, UserCheck, UserX, Trash2, Clock, CheckCircle, XCircle,
  SlidersHorizontal, RotateCcw, Users, ShieldCheck, ShieldX, FileSearch,
  AlertTriangle,
} from "lucide-react";
import { useState } from "react";
import { useAdminApplications, EMPTY_FILTERS } from "../../../features/admin/model/use-admin-applications";
import type { AdminLawyerApplication } from "../../../features/admin/api/admin-lawyer-api";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

// ─── Status pill ──────────────────────────────────────────────
const STATUS_CONFIG = {
  PENDING:  { color: "#1d4ed8", bg: "rgba(29,78,216,0.08)",  border: "rgba(29,78,216,0.2)",  dot: "#1d4ed8", label: "На рассмотрении", icon: <Clock size={11} /> },
  APPROVED: { color: "#059669", bg: "rgba(5,150,105,0.08)",  border: "rgba(5,150,105,0.2)",  dot: "#059669", label: "Одобрено",         icon: <CheckCircle size={11} /> },
  REJECTED: { color: "#ef4444", bg: "rgba(239,68,68,0.08)",  border: "rgba(239,68,68,0.2)",  dot: "#ef4444", label: "Отклонено",        icon: <XCircle size={11} /> },
};

function StatusPill({ status }: { status: string }) {
  const s = STATUS_CONFIG[status as keyof typeof STATUS_CONFIG] ?? STATUS_CONFIG.PENDING;
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 6, padding: "4px 12px", borderRadius: 20, background: s.bg, border: `1px solid ${s.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: s.dot, flexShrink: 0 }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: s.color, letterSpacing: "0.04em" }}>{s.label}</span>
    </div>
  );
}

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({ icon, tooltip, onClick, hoverBg, hoverColor, disabled = false }: {
  icon: React.ReactNode; tooltip: string; onClick?: () => void;
  hoverBg: string; hoverColor: string; disabled?: boolean;
}) {
  return (
    <Tooltip title={tooltip}>
      <button
        disabled={disabled}
        onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: disabled ? "not-allowed" : "pointer", color: disabled ? "#cbd5e1" : "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={(e) => { if (!disabled) { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; } }}
        onMouseLeave={(e) => { if (!disabled) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; } }}
      >
        {icon}
      </button>
    </Tooltip>
  );
}

// ─── Delete confirm modal ─────────────────────────────────────
function DeleteModal({
  open, loading, onConfirm, onCancel,
}: { open: boolean; loading: boolean; onConfirm: () => void; onCancel: () => void }) {
  return (
    <Modal open={open} footer={null} onCancel={onCancel} centered width={420} closable={false}>
      <div style={{ textAlign: "center", padding: "8px 0 4px" }}>
        <div style={{ width: 60, height: 60, borderRadius: "50%", background: "#fff1f2", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 20px", border: "2px solid rgba(239,68,68,0.2)" }}>
          <AlertTriangle size={26} color="#ef4444" />
        </div>
        <div style={{ fontSize: 18, fontWeight: 800, color: "#0b1c30", marginBottom: 8, fontFamily: "Manrope, sans-serif" }}>
          Удалить заявку?
        </div>
        <div style={{ fontSize: 13, color: "#64748b", marginBottom: 6, lineHeight: 1.6 }}>
          Заявка будет удалена без возможности восстановления.
        </div>
        <div style={{ fontSize: 12, color: "#991b1b", background: "#fff1f2", border: "1px solid rgba(239,68,68,0.25)", borderRadius: 8, padding: "10px 16px", marginBottom: 28, lineHeight: 1.6 }}>
          ⚠ Это действие необратимо. История заявки будет потеряна.
        </div>
        <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>
          <Button onClick={onCancel} style={{ borderRadius: 8, minWidth: 110 }}>Отмена</Button>
          <Button loading={loading} onClick={onConfirm} style={{ borderRadius: 8, minWidth: 110, background: "#ef4444", borderColor: "#ef4444", color: "#fff", fontWeight: 600 }}>
            Удалить
          </Button>
        </div>
      </div>
    </Modal>
  );
}

function getInitials(fio: string) {
  return fio.split(" ").slice(0, 2).map((w) => w[0]).join("").toUpperCase();
}
const AVATAR_COLORS = ["#0F2A44", "#1d4ed8", "#7c3aed", "#b45309", "#0369a1"];
const fmtDate = (v: string) => new Date(v).toLocaleDateString("ru-KZ", { day: "2-digit", month: "short", year: "numeric" });

// ─── Main panel ───────────────────────────────────────────────
export default function AdminLawyerApplicationsPanel() {
  const { message } = App.useApp();
  void message;

  const [rejectForm] = Form.useForm();
  const [filterForm] = Form.useForm();
  const [deleteModal, setDeleteModal] = useState<{ open: boolean; id: number | null }>({ open: false, id: null });

  const {
    filters, setFilters,
    isLoading, isFetching,
    stats, filtered, paginatedData,
    loadingIds, activeFilterCount,
    rejectModal, setRejectModal,
    detailDrawer, setDetailDrawer,
    filterModalOpen, setFilterModalOpen,
    handleApprove, handleRejectSubmit, handleDelete,
  } = useAdminApplications();

  const openFilterModal = () => {
    filterForm.setFieldsValue({ status: filters.status ?? null, licenseNumber: filters.licenseNumber ?? "", userId: filters.userId ?? null, createdRange: filters.createdRange ?? null, reviewedRange: filters.reviewedRange ?? null });
    setFilterModalOpen(true);
  };
  const applyFilters = () => {
    const v = filterForm.getFieldsValue();
    setFilters({ ...EMPTY_FILTERS, reviewerFio: filters.reviewerFio, status: v.status || undefined, licenseNumber: v.licenseNumber || undefined, userId: v.userId || undefined, createdRange: v.createdRange || null, reviewedRange: v.reviewedRange || null });
    setFilterModalOpen(false);
  };

  const columns: ColumnsType<AdminLawyerApplication> = [
    {
      title: "Заявитель",
      key: "lawyer",
      render: (_, r) => (
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <Avatar size={40} style={{ background: AVATAR_COLORS[r.id % AVATAR_COLORS.length], fontWeight: 700, flexShrink: 0, borderRadius: 10, fontSize: 13 }}>
            {getInitials(r.lawyerInfo.fio)}
          </Avatar>
          <div>
            <Text strong style={{ display: "block", fontSize: 14, color: "#0b1c30" }}>{r.lawyerInfo.fio}</Text>
            <Text style={{ fontSize: 12, color: "#64748b" }}>{r.lawyerInfo.email}</Text>
          </div>
        </div>
      ),
    },
    {
      title: "Лицензия",
      dataIndex: "licenseNumber",
      render: (val) => (
        <span style={{ fontFamily: "monospace", fontSize: 12, color: "#475569", background: "#f8fafc", padding: "3px 8px", borderRadius: 6, border: "1px solid #e2e8f0" }}>
          {val}
        </span>
      ),
    },
    {
      title: "Статус",
      dataIndex: "status",
      render: (status) => <StatusPill status={status} />,
    },
    {
      title: "Дата подачи",
      dataIndex: "submittedAt",
      render: (val) => <Text style={{ fontSize: 13, color: "#64748b" }}>{fmtDate(val)}</Text>,
    },
    {
      title: "Действия",
      key: "actions",
      width: 140,
      render: (_, record) => {
        const loading = loadingIds.includes(record.id);
        return (
          <Space size={2}>
            <IconBtn icon={<Eye size={15} />}       tooltip="Просмотр"  hoverBg="#f1f5f9" hoverColor="#0F2A44" onClick={() => setDetailDrawer({ open: true, record })} />
            {record.status === "PENDING" && <>
              <IconBtn icon={loading ? <span style={{ fontSize: 11 }}>…</span> : <UserCheck size={15} />} tooltip="Одобрить"   hoverBg="#f0fdf4" hoverColor="#059669"
                onClick={() => handleApprove(record.id)} disabled={loading} />
              <IconBtn icon={<UserX size={15} />}    tooltip="Отклонить" hoverBg="#fff1f2" hoverColor="#ef4444"
                onClick={() => setRejectModal({ open: true, id: record.id })} disabled={loading} />
            </>}
            <IconBtn icon={loading ? <span style={{ fontSize: 11 }}>…</span> : <Trash2 size={15} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" disabled={loading}
              onClick={() => setDeleteModal({ open: true, id: record.id })} />
          </Space>
        );
      },
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: "40px 40px 56px", display: "flex", justifyContent: "space-between", alignItems: "flex-end", position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
              Заявки на статус адвоката
            </h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>
              Рассмотрение, одобрение и отклонение заявок пользователей
            </p>
          </div>
          <div style={{ display: "flex", alignItems: "center", background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)" }}>
            <div style={{ padding: "12px 20px" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Всего</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{stats.total}</div>
            </div>
            <div style={{ width: 1, height: 40, background: "rgba(255,255,255,0.1)" }} />
            <div style={{ padding: "12px 20px", textAlign: "right" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Ожидают</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#f59e0b", fontFamily: "Manrope, sans-serif" }}>{stats.pending}</div>
            </div>
          </div>
        </div>
      </div>

      <div style={{ padding: "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "flex", gap: 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего заявок"       value={stats.total}    color="#0F2A44" icon={<Users size={26} />} />
          <StatCard label="На рассмотрении"    value={stats.pending}  color="#1d4ed8" icon={<Clock size={26} />} />
          <StatCard label="Одобрено"           value={stats.approved} color="#059669" icon={<ShieldCheck size={26} />} />
          <StatCard label="Отклонено"          value={stats.rejected} color="#ef4444" icon={<ShieldX size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
            {/* Search reviewer */}
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск по проверяющему</div>
              <Input
                prefix={<FileSearch size={15} color="#757682" />}
                placeholder="ФИО проверяющего..."
                allowClear
                value={filters.reviewerFio ?? ""}
                onChange={(e) => setFilters((f) => ({ ...f, reviewerFio: e.target.value || undefined, page: 0 }))}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }}
              />
            </div>

            {/* Quick status filter */}
            <div style={{ minWidth: 180 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Статус</div>
              <Select
                allowClear placeholder="Все статусы"
                value={filters.status}
                onChange={(v) => setFilters((f) => ({ ...f, status: v, page: 0 }))}
                style={{ width: "100%" }}
              >
                <Option value="PENDING"><Space size={6}><Clock size={12} color="#1d4ed8" />На рассмотрении</Space></Option>
                <Option value="APPROVED"><Space size={6}><CheckCircle size={12} color="#059669" />Одобрено</Space></Option>
                <Option value="REJECTED"><Space size={6}><XCircle size={12} color="#ef4444" />Отклонено</Space></Option>
              </Select>
            </div>

            {/* Filter modal btn */}
            <Badge count={activeFilterCount} size="small" color="#1d4ed8" offset={[-4, 4]}>
              <Button
                icon={<SlidersHorizontal size={15} />}
                onClick={openFilterModal}
                style={{ borderRadius: 12, height: 40, paddingLeft: 16, paddingRight: 16, background: activeFilterCount > 0 ? "#eff4ff" : "transparent", borderColor: activeFilterCount > 0 ? "#1d4ed8" : "#e2e8f0", color: activeFilterCount > 0 ? "#1d4ed8" : "#64748b", fontWeight: 600 }}
              >
                Фильтры
              </Button>
            </Badge>

            {/* Reset */}
            {(activeFilterCount > 0 || filters.reviewerFio) && (
              <Button icon={<RotateCcw size={13} />} type="text" onClick={() => setFilters(EMPTY_FILTERS)} style={{ color: "#94a3b8", height: 40 }}>
                Сбросить
              </Button>
            )}
          </div>
        </div>

        {/* ── Table ── */}
        <div style={{ background: "#fff", borderRadius: 16, boxShadow: "0 8px 32px rgba(11,28,48,0.04)", overflow: "hidden" }}>
          <Table
            components={editorialTableComponents}
            columns={columns}
            dataSource={paginatedData}
            rowKey="id"
            loading={isLoading || isFetching}
            scroll={{ x: "max-content" }}
            pagination={{
              current: filters.page + 1,
              pageSize: filters.size,
              total: filtered.length,
              onChange: (p, ps) => setFilters((f) => ({ ...f, page: p - 1, size: ps })),
              showSizeChanger: false,
              showTotal: (total, range) => `${range[0]}–${range[1]} из ${total} заявок`,
              style: { padding: "16px 32px", margin: 0 },
            }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Filter modal ── */}
      <Modal
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#eff4ff", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <SlidersHorizontal size={16} color="#0F2A44" />
            </div>
            <span style={{ fontWeight: 700, color: "#0b1c30" }}>Расширенные фильтры</span>
          </div>
        }
        open={filterModalOpen}
        onCancel={() => setFilterModalOpen(false)}
        width={520}
        footer={
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "4px 0" }}>
            <Button icon={<RotateCcw size={13} />} type="text" onClick={() => filterForm.resetFields()} style={{ color: "#94a3b8" }}>
              Сбросить всё
            </Button>
            <Space>
              <Button style={{ borderRadius: 8 }} onClick={() => setFilterModalOpen(false)}>Отмена</Button>
              <Button style={{ borderRadius: 8, background: "#0F2A44", borderColor: "#0F2A44", color: "#fff", fontWeight: 600 }} onClick={applyFilters}>
                Применить
              </Button>
            </Space>
          </div>
        }
      >
        <Form form={filterForm} layout="vertical" style={{ padding: "8px 0 0" }}>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
            <Form.Item name="status" label={<span style={{ fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#757682" }}>Статус</span>}>
              <Select placeholder="Все статусы" allowClear style={{ borderRadius: 8 }}>
                <Option value="PENDING"><Space size={6}><Clock size={12} color="#1d4ed8" />На рассмотрении</Space></Option>
                <Option value="APPROVED"><Space size={6}><CheckCircle size={12} color="#059669" />Одобрено</Space></Option>
                <Option value="REJECTED"><Space size={6}><XCircle size={12} color="#ef4444" />Отклонено</Space></Option>
              </Select>
            </Form.Item>
            <Form.Item name="licenseNumber" label={<span style={{ fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#757682" }}>Номер лицензии</span>}>
              <Input placeholder="KZ-ADV-2024-..." allowClear style={{ borderRadius: 8, fontFamily: "monospace" }} />
            </Form.Item>
          </div>
          <Form.Item name="createdRange" label={<span style={{ fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#757682" }}>Период подачи</span>}>
            <RangePicker style={{ width: "100%", borderRadius: 8 }} placeholder={["От", "До"]} />
          </Form.Item>
          <Form.Item name="reviewedRange" label={<span style={{ fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#757682" }}>Период рассмотрения</span>} style={{ marginBottom: 0 }}>
            <RangePicker style={{ width: "100%", borderRadius: 8 }} placeholder={["От", "До"]} />
          </Form.Item>
        </Form>
      </Modal>

      {/* ── Reject modal ── */}
      <Modal
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#fff1f2", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <UserX size={16} color="#ef4444" />
            </div>
            <span style={{ fontWeight: 700, color: "#0b1c30" }}>Отклонить заявку</span>
          </div>
        }
        open={rejectModal.open}
        onCancel={() => { setRejectModal({ open: false, id: null }); rejectForm.resetFields(); }}
        footer={null}
        destroyOnHidden
        width={480}
      >
        <p style={{ fontSize: 13, color: "#64748b", margin: "8px 0 20px", lineHeight: 1.6 }}>
          Укажите причину отклонения — она будет показана заявителю.
        </p>
        <Form form={rejectForm} layout="vertical" onFinish={(v) => { handleRejectSubmit(v); rejectForm.resetFields(); }}>
          <Form.Item name="reason" label={<span style={{ fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#757682" }}>Причина отклонения</span>}
            rules={[{ required: true, message: "Укажите причину" }]}>
            <Input.TextArea rows={4} placeholder="Например: Лицензия не найдена в реестре адвокатов РК..." style={{ borderRadius: 8 }} />
          </Form.Item>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <Button style={{ borderRadius: 8 }} onClick={() => { setRejectModal({ open: false, id: null }); rejectForm.resetFields(); }}>Отмена</Button>
            <Button htmlType="submit" style={{ borderRadius: 8, background: "#ef4444", borderColor: "#ef4444", color: "#fff", fontWeight: 600 }}>
              Отклонить заявку
            </Button>
          </div>
        </Form>
      </Modal>

      {/* ── Delete modal ── */}
      <DeleteModal
        open={deleteModal.open}
        loading={deleteModal.id !== null && loadingIds.includes(deleteModal.id)}
        onConfirm={() => {
          if (deleteModal.id !== null) {
            handleDelete(deleteModal.id);
            setDeleteModal({ open: false, id: null });
          }
        }}
        onCancel={() => setDeleteModal({ open: false, id: null })}
      />

      {/* ── Detail drawer ── */}
      <Drawer
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#eff4ff", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <Eye size={15} color="#0F2A44" />
            </div>
            <span style={{ fontWeight: 700, color: "#0b1c30" }}>Заявка #{detailDrawer.record?.id}</span>
          </div>
        }
        open={detailDrawer.open}
        onClose={() => setDetailDrawer({ open: false, record: null })}
        width={440}
        styles={{ body: { padding: 24 } }}
      >
        {detailDrawer.record && (() => {
          const r = detailDrawer.record;
          const s = STATUS_CONFIG[r.status as keyof typeof STATUS_CONFIG];
          return (
            <>
              {/* Status banner */}
              <div style={{ background: s.bg, border: `1px solid ${s.border}`, borderRadius: 10, padding: "14px 18px", marginBottom: 24, display: "flex", alignItems: "center", gap: 10 }}>
                <span style={{ width: 8, height: 8, borderRadius: "50%", background: s.dot, flexShrink: 0 }} />
                <Text style={{ color: s.color, fontWeight: 700, fontSize: 13 }}>{s.label}</Text>
              </div>

              {/* Applicant */}
              <Text style={{ fontSize: 11, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.08em", fontWeight: 700 }}>Заявитель</Text>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 10, marginBottom: 20 }}>
                <Avatar size={44} style={{ background: AVATAR_COLORS[r.id % AVATAR_COLORS.length], fontWeight: 700, borderRadius: 10 }}>
                  {getInitials(r.lawyerInfo.fio)}
                </Avatar>
                <div>
                  <Text strong style={{ display: "block", color: "#0b1c30" }}>{r.lawyerInfo.fio}</Text>
                  <Text style={{ fontSize: 12, color: "#64748b" }}>{r.lawyerInfo.email}</Text>
                  <Text style={{ fontSize: 12, color: "#94a3b8", display: "block" }}>ИИН: {r.lawyerInfo.iin}</Text>
                </div>
              </div>

              <Divider style={{ margin: "0 0 20px" }} />

              <Descriptions column={1} size="small" colon={false} styles={{ label: { color: "#94a3b8", fontSize: 12 }, content: { fontSize: 13 } }}>
                <Descriptions.Item label="Номер лицензии">
                  <span style={{ fontFamily: "monospace", background: "#f8fafc", padding: "2px 8px", borderRadius: 6, border: "1px solid #e2e8f0", fontSize: 12 }}>{r.licenseNumber}</span>
                </Descriptions.Item>
                <Descriptions.Item label="Дата подачи">{fmtDate(r.submittedAt)}</Descriptions.Item>
                {r.reviewerInfo && <Descriptions.Item label="Проверил">{r.reviewerInfo.fio}</Descriptions.Item>}
                {r.reviewedAt && <Descriptions.Item label="Дата рассмотрения">{new Date(r.reviewedAt).toLocaleString("ru-KZ")}</Descriptions.Item>}
              </Descriptions>

              {r.rejectionReason && (
                <>
                  <Divider style={{ margin: "16px 0" }} />
                  <Text style={{ fontSize: 11, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.08em", fontWeight: 700, display: "block", marginBottom: 8 }}>Причина отклонения</Text>
                  <div style={{ background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.2)", borderRadius: 8, padding: "12px 16px" }}>
                    <Text style={{ fontSize: 13, color: "#ef4444" }}>{r.rejectionReason}</Text>
                  </div>
                </>
              )}

              {r.status === "PENDING" && (
                <>
                  <Divider style={{ margin: "20px 0" }} />
                  <div style={{ display: "flex", flexDirection: "column", width: "100%", gap: 8 }}>
                    <Button block icon={<UserCheck size={14} />} onClick={() => { handleApprove(r.id); setDetailDrawer({ open: false, record: null }); }}
                      style={{ borderRadius: 8, height: 40, background: "rgba(5,150,105,0.08)", borderColor: "rgba(5,150,105,0.2)", color: "#059669", fontWeight: 600 }}>
                      Одобрить заявку
                    </Button>
                    <Button block icon={<UserX size={14} />} onClick={() => { setDetailDrawer({ open: false, record: null }); setRejectModal({ open: true, id: r.id }); }}
                      style={{ borderRadius: 8, height: 40, background: "rgba(239,68,68,0.06)", borderColor: "rgba(239,68,68,0.2)", color: "#ef4444", fontWeight: 600 }}>
                      Отклонить заявку
                    </Button>
                  </div>
                </>
              )}
            </>
          );
        })()}
      </Drawer>
    </div>
  );
}
