import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  App, Table, Button, Form, Input, Select, Typography, Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  FileText, Plus, Search, Trash2, Eye, RotateCcw,
  FilePen, CheckCircle2, Clock
} from "lucide-react";
import {
  useGetDocumentsQuery,
  useCreateDocumentMutation,
  useDeleteDocumentMutation,
  type DocumentListItem,
} from "../../../features/documents/api/document-api";
import { useGetTemplatesQuery } from "../../../features/templates/api/user-template-api";
import { ROUTES } from "../../../app/router/router";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;

// ─── Status pill ──────────────────────────────────────────────
function StatusPill({ status }: { status: string }) {
  const cfg =
    status === "COMPLETED"  ? { color: "#059669", bg: "rgba(5,150,105,0.08)",   border: "rgba(5,150,105,0.2)",   dot: "#059669", label: "Завершён"  } :
    status === "PROCESSING" ? { color: "#1677ff", bg: "rgba(22,119,255,0.08)",  border: "rgba(22,119,255,0.2)",  dot: "#1677ff", label: "В обработке" } :
                              { color: "#f59e0b", bg: "rgba(245,158,11,0.08)",  border: "rgba(245,158,11,0.2)",  dot: "#f59e0b", label: "Черновик"  };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: cfg.dot, flexShrink: 0 }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color, letterSpacing: "0.04em" }}>{cfg.label}</span>
    </div>
  );
}

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({ icon, tooltip, onClick, hoverBg, hoverColor }: {
  icon: React.ReactNode; tooltip: string; onClick?: () => void; hoverBg: string; hoverColor: string;
}) {
  return (
    <Tooltip title={tooltip}>
      <button onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={e => { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; }}
        onMouseLeave={e => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
      >{icon}</button>
    </Tooltip>
  );
}

export default function DocumentsListPanel() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteModal, setDeleteModal] = useState<{ open: boolean; id: number | null; title: string }>({ open: false, id: null, title: "" });
  const [form] = Form.useForm();

  const { data, isLoading } = useGetDocumentsQuery({ page: page - 1, size: 10 });
  const { data: allData } = useGetDocumentsQuery({ page: 0, size: 1000 });
  const { data: templatesData } = useGetTemplatesQuery({ page: 0, size: 100 });
  const templates = templatesData?.content ?? [];
  const [createDocument, { isLoading: isCreating }] = useCreateDocumentMutation();
  const [deleteDocument, { isLoading: isDeleting }] = useDeleteDocumentMutation();

  const allDocs = allData?.content ?? [];
  const draftCount     = allDocs.filter(d => d.status === "DRAFT").length;
  const completedCount = allDocs.filter(d => d.status === "COMPLETED").length;
  const processingCount = allDocs.filter(d => d.status === "PROCESSING").length;

  const filtered = (data?.content ?? []).filter(d => {
    const q = search.toLowerCase();
    const matchSearch = !search || d.title.toLowerCase().includes(q) || d.templateTitle?.toLowerCase().includes(q) || d.categoryName?.toLowerCase().includes(q);
    const matchStatus = !statusFilter || d.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const handleCreate = async (values: { templateId: number; title: string }) => {
    try {
      const doc = await createDocument({ templateId: values.templateId, title: values.title, fieldValues: {} }).unwrap();
      message.success("Документ создан");
      setCreateOpen(false);
      form.resetFields();
      navigate(`${ROUTES.DOCUMENTS}/${doc.id}`);
    } catch { message.error("Ошибка при создании документа"); }
  };

  const handleDelete = async () => {
    if (!deleteModal.id) return;
    try {
      await deleteDocument(deleteModal.id).unwrap();
      message.success("Документ удалён");
      setDeleteModal({ open: false, id: null, title: "" });
    } catch { message.error("Ошибка при удалении"); }
  };

  const columns: ColumnsType<DocumentListItem> = [
    {
      title: "Документ",
      key: "title",
      render: (_, r) => (
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ width: 36, height: 36, borderRadius: 8, background: "rgba(15,42,68,0.06)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <FilePen size={16} color="#0F2A44" />
          </div>
          <div>
            <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>{r.title}</Text>
            <Text style={{ fontSize: 11, color: "#94a3b8", display: "block", marginTop: 1 }}>{r.templateTitle || "—"}</Text>
          </div>
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      width: 160,
      render: (name: string) => (
        <div style={{ display: "inline-flex", padding: "3px 10px", borderRadius: 20, background: "rgba(15,42,68,0.06)", border: "1px solid rgba(15,42,68,0.12)" }}>
          <span style={{ fontSize: 11, fontWeight: 600, color: "#0F2A44" }}>{name || "—"}</span>
        </div>
      ),
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      width: 140,
      render: (status: string) => <StatusPill status={status} />,
    },
    {
      title: "Создан",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 120,
      render: (date: string) => <Text style={{ fontSize: 12, color: "#64748b" }}>{new Date(date).toLocaleDateString("ru-KZ", { day: "2-digit", month: "short", year: "numeric" })}</Text>,
    },
    {
      title: "Действия",
      key: "actions",
      width: 100,
      align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2, justifyContent: "center" }}>
          <IconBtn icon={<Eye size={14} />} tooltip="Открыть" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => navigate(`${ROUTES.DOCUMENTS}/${r.id}`)} />
          <IconBtn icon={<Trash2 size={14} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => setDeleteModal({ open: true, id: r.id, title: r.title })} />
        </div>
      ),
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: "40px 40px 56px", display: "flex", justifyContent: "space-between", alignItems: "flex-end", position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>Мои документы</h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>Создавайте, редактируйте и анализируйте юридические документы с помощью AI</p>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ display: "flex", alignItems: "center", background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)" }}>
              <div style={{ padding: "12px 20px" }}>
                <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Всего</div>
                <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{data?.totalElements ?? 0}</div>
              </div>
              <div style={{ width: 1, height: 40, background: "rgba(255,255,255,0.1)" }} />
              <div style={{ padding: "12px 20px" }}>
                <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Завершённых</div>
                <div style={{ fontSize: 22, fontWeight: 800, color: "#34d399", fontFamily: "Manrope, sans-serif" }}>{completedCount}</div>
              </div>
            </div>
            <Button icon={<Plus size={15} />} onClick={() => setCreateOpen(true)}
              style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "#fff", borderRadius: 10, height: 42, fontWeight: 600, paddingLeft: 20, paddingRight: 20 }}>
              Создать документ
            </Button>
          </div>
        </div>
      </div>

      <div style={{ padding: "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "flex", gap: 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего документов" value={data?.totalElements ?? 0} color="#0F2A44" icon={<FileText size={26} />} loading={isLoading} />
          <StatCard label="Черновики"         value={draftCount}              color="#f59e0b" icon={<FilePen size={26} />} />
          <StatCard label="Завершённые"       value={completedCount}          color="#059669" icon={<CheckCircle2 size={26} />} />
          <StatCard label="В обработке"       value={processingCount}         color="#1677ff" icon={<Clock size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "20px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <Input prefix={<Search size={15} color="#757682" />} placeholder="По названию, шаблону или категории..." allowClear value={search} onChange={e => setSearch(e.target.value)}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }} />
            </div>
            <div style={{ minWidth: 200 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Статус</div>
              <Select allowClear placeholder="Все статусы" value={statusFilter} onChange={setStatusFilter} style={{ width: "100%" }}
                options={[
                  { value: "DRAFT",      label: "Черновик" },
                  { value: "PROCESSING", label: "В обработке" },
                  { value: "COMPLETED",  label: "Завершён" },
                ]} />
            </div>
            {(search || statusFilter) && (
              <Button icon={<RotateCcw size={13} />} type="text" onClick={() => { setSearch(""); setStatusFilter(undefined); }} style={{ color: "#94a3b8", height: 40 }}>
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
            dataSource={filtered}
            rowKey="id"
            loading={isLoading}
            scroll={{ x: "max-content" }}
            onRow={r => ({ style: { cursor: "pointer" }, onClick: () => navigate(`${ROUTES.DOCUMENTS}/${r.id}`) })}
            pagination={{ current: page, total: data?.totalElements, pageSize: 10, onChange: p => setPage(p), showSizeChanger: false, showTotal: t => `Всего ${t} документов`, style: { padding: "16px 32px", margin: 0 } }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Create modal ── */}
      {createOpen && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.45)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ background: "#fff", borderRadius: 16, width: 500, overflow: "hidden", boxShadow: "0 24px 80px rgba(0,0,0,0.18)" }}>
            <div style={{ background: "linear-gradient(135deg, #0F2A44, #1a4070)", padding: "20px 28px 16px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 38, height: 38, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <FilePen size={18} color="#fff" />
                </div>
                <div>
                  <Text style={{ color: "#fff", fontWeight: 700, fontSize: 15, display: "block" }}>Создать документ</Text>
                  <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>Выберите шаблон и введите название</Text>
                </div>
              </div>
            </div>
            <div style={{ padding: "24px 28px" }}>
              <Form form={form} layout="vertical" onFinish={handleCreate}>
                <Form.Item name="templateId" label={<Text style={{ fontWeight: 600 }}>Шаблон документа</Text>} rules={[{ required: true, message: "Выберите шаблон" }]}>
                  <Select size="large" showSearch placeholder="Выберите шаблон"
                    filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
                    options={templates.map(t => ({ value: t.id, label: t.title }))} />
                </Form.Item>
                <Form.Item name="title" label={<Text style={{ fontWeight: 600 }}>Название документа</Text>} rules={[{ required: true, message: "Укажите название" }]}>
                  <Input size="large" placeholder="Например: Договор аренды офиса 2025" />
                </Form.Item>
                <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 8 }}>
                  <Button onClick={() => { setCreateOpen(false); form.resetFields(); }}>Отмена</Button>
                  <Button type="primary" htmlType="submit" loading={isCreating}
                    style={{ background: "#0F2A44", borderColor: "#0F2A44" }}>
                    Создать и открыть
                  </Button>
                </div>
              </Form>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete modal ── */}
      {deleteModal.open && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.45)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ background: "#fff", borderRadius: 16, width: 420, overflow: "hidden", boxShadow: "0 24px 80px rgba(0,0,0,0.18)" }}>
            <div style={{ background: "linear-gradient(135deg, #ef4444, #dc2626)", padding: "20px 28px 16px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 38, height: 38, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <Trash2 size={18} color="#fff" />
                </div>
                <div>
                  <Text style={{ color: "#fff", fontWeight: 700, fontSize: 15, display: "block" }}>Удалить документ</Text>
                  <Text style={{ color: "rgba(255,255,255,0.7)", fontSize: 12 }}>Это действие нельзя отменить</Text>
                </div>
              </div>
            </div>
            <div style={{ padding: "24px 28px" }}>
              <Text style={{ fontSize: 14, color: "#374151" }}>Вы уверены, что хотите удалить документ </Text>
              <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>«{deleteModal.title}»</Text>
              <Text style={{ fontSize: 14, color: "#374151" }}>?</Text>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
                <Button onClick={() => setDeleteModal({ open: false, id: null, title: "" })}>Отмена</Button>
                <Button danger loading={isDeleting} onClick={handleDelete}
                  style={{ background: "#ef4444", borderColor: "#ef4444", color: "#fff" }}>
                  Удалить
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
