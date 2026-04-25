import {
  Table, Button, Drawer, Form, Select, Input, Typography, Tooltip, Divider,
} from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import type { ColumnsType } from "antd/es/table";
import {
  FileText, CheckCircle2, Clock, AlertCircle,
  Search, RotateCcw, MessageSquare, Eye,
  ThumbsUp, ThumbsDown, RefreshCw,
} from "lucide-react";
import type { DocumentListItem } from "../../../features/documents/api/document-api";
import { useLawyerDocuments } from "../../../features/lawyer/model/use-lawyer-documents";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;
const { TextArea } = Input;

// ─── Status pill ──────────────────────────────────────────────
function StatusPill({ status }: { status: string }) {
  const cfg =
    status === "COMPLETED"  ? { color: "#059669", bg: "rgba(5,150,105,0.08)",   border: "rgba(5,150,105,0.2)",   dot: "#059669", label: "Завершён"   } :
    status === "VALIDATED" ? { color: "#f59e0b", bg: "rgba(245,158,11,0.08)",  border: "rgba(245,158,11,0.2)",  dot: "#f59e0b", label: "Проверен"   } :
                              { color: "#94a3b8", bg: "rgba(148,163,184,0.08)", border: "rgba(148,163,184,0.2)", dot: "#94a3b8", label: "Черновик"   };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: cfg.dot, flexShrink: 0 }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color, letterSpacing: "0.04em" }}>{cfg.label}</span>
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
      <button disabled={disabled} onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: disabled ? "not-allowed" : "pointer", color: disabled ? "#cbd5e1" : "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={(e) => { if (!disabled) { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; } }}
        onMouseLeave={(e) => { if (!disabled) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; } }}
      >
        {icon}
      </button>
    </Tooltip>
  );
}

// ─── Main panel ───────────────────────────────────────────────
export default function LawyerDocumentsPanel() {
  const isMobile = useIsMobile();
  const {
    form, page, setPage, search, setSearch, statusFilter, setStatusFilter,
    reviewModal, detailDrawer,
    data, isLoading, isSubmitting,
    docDetail,
    filtered, draftCount, completedCount, processingCount,
    openReview, closeReview, openDetail, closeDetail,
    handleSubmitReview,
  } = useLawyerDocuments();

  const columns: ColumnsType<DocumentListItem> = [
    {
      title: "Документ",
      key: "title",
      render: (_, r) => (
        <div>
          <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>{r.title}</Text>
          <Text style={{ fontSize: 11, color: "#94a3b8", display: "block", marginTop: 2 }}>{r.templateTitle || "—"}</Text>
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      width: 160,
      render: (name: string) => (
        <div style={{ display: "inline-flex", alignItems: "center", padding: "3px 10px", borderRadius: 20, background: "rgba(26,39,68,0.06)", border: "1px solid rgba(26,39,68,0.12)" }}>
          <span style={{ fontSize: 11, fontWeight: 600, color: "#1a2744" }}>{name || "—"}</span>
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
      width: 110,
      align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2, justifyContent: "center" }}>
          <IconBtn icon={<Eye size={14} />} tooltip="Детали" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openDetail(r)} />
          <IconBtn icon={<MessageSquare size={14} />} tooltip="Добавить ревью" hoverBg="#f0fdf4" hoverColor="#059669"
            onClick={() => openReview(r.id)} />
        </div>
      ),
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "#1a2744", position: "relative", overflow: "hidden" }}>
        <div style={{ padding: isMobile ? "24px 16px 48px" : "40px 40px 56px", display: "flex", flexDirection: isMobile ? "column" as React.CSSProperties["flexDirection"] : "row" as React.CSSProperties["flexDirection"], justifyContent: "space-between", alignItems: "flex-end", gap: isMobile ? 16 : undefined, position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>Документы клиентов</h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>Документы на основе ваших шаблонов — просмотр и добавление ревью</p>
          </div>
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 12px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)", gap: isMobile ? 10 : 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего документов" value={data?.totalElements ?? 0} color="#1a2744" icon={<FileText size={26} />} loading={isLoading} />
          <StatCard label="Проверен"          value={processingCount}          color="#f59e0b" icon={<Clock size={26} />} />
          <StatCard label="Завершённые"       value={completedCount}           color="#059669" icon={<CheckCircle2 size={26} />} />
          <StatCard label="Черновики"         value={draftCount}               color="#94a3b8" icon={<AlertCircle size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "20px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <Input prefix={<Search size={15} color="#757682" />} placeholder="По названию документа или шаблону..." allowClear value={search} onChange={e => setSearch(e.target.value)}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }} />
            </div>
            <div style={{ minWidth: 200 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Статус</div>
              <Select allowClear placeholder="Все статусы" value={statusFilter} onChange={setStatusFilter} style={{ width: "100%" }}
                options={[
                  { value: "DRAFT",      label: "Черновик" },
                  { value: "VALIDATED", label: "Проверен" },
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
            pagination={{ current: page, total: data?.totalElements, pageSize: 10, onChange: p => setPage(p), showSizeChanger: false, showTotal: t => `Всего ${t} документов`, style: { padding: "16px 32px", margin: 0 } }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Review Modal ── */}
      {reviewModal.open && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.45)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ background: "#fff", borderRadius: 16, width: 500, overflow: "hidden", boxShadow: "0 24px 80px rgba(0,0,0,0.18)" }}>
            <div style={{ background: "#1a2744", padding: "20px 28px 16px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 38, height: 38, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <MessageSquare size={18} color="#fff" />
                </div>
                <div>
                  <Text style={{ color: "#fff", fontWeight: 700, fontSize: 15, display: "block" }}>Добавить ревью</Text>
                  <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>Оценка документа клиента</Text>
                </div>
              </div>
            </div>
            <div style={{ padding: "24px 28px" }}>
              <Form form={form} layout="vertical">
                <Form.Item label={<Text style={{ fontWeight: 600 }}>Решение</Text>} name="status" rules={[{ required: true, message: "Выберите решение" }]}>
                  <Select size="large" placeholder="Выберите решение" options={[
                    { value: "APPROVED",       label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><ThumbsUp size={14} color="#059669" />Одобрить</span> },
                    { value: "REJECTED",       label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><ThumbsDown size={14} color="#ef4444" />Отклонить</span> },
                    { value: "NEEDS_REVISION", label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><RefreshCw size={14} color="#f59e0b" />Требует доработки</span> },
                  ]} />
                </Form.Item>
                <Form.Item label={<Text style={{ fontWeight: 600 }}>Комментарий</Text>} name="comment" rules={[{ required: true, message: "Добавьте комментарий" }]}>
                  <TextArea rows={4} placeholder="Опишите ваши замечания или рекомендации..." showCount maxLength={1000} />
                </Form.Item>
              </Form>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 8 }}>
                <Button onClick={closeReview}>Отмена</Button>
                <Button type="primary" onClick={handleSubmitReview} loading={isSubmitting}
                  style={{ background: "#1a2744", borderColor: "#1a2744" }}>
                  Отправить ревью
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Detail Drawer ── */}
      <Drawer
        open={detailDrawer.open}
        onClose={closeDetail}
        width={480}
        title={null}
        styles={{ body: { padding: 0 }, header: { display: "none" } }}
      >
        {detailDrawer.record && (
          <>
            <div style={{ background: "#1a2744", padding: "24px 28px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <FileText size={20} color="#fff" />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <Text style={{ color: "#fff", fontWeight: 700, fontSize: 15, display: "block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {detailDrawer.record.title}
                  </Text>
                  <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>{detailDrawer.record.templateTitle}</Text>
                </div>
              </div>
              <StatusPill status={detailDrawer.record.status} />
            </div>
            <div style={{ padding: "24px 28px" }}>
              <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
                {[
                  { label: "Категория", value: detailDrawer.record.categoryName || "—" },
                  { label: "ID документа", value: `#${detailDrawer.record.id}`, mono: true },
                  { label: "Создан", value: new Date(detailDrawer.record.createdDate).toLocaleString("ru-KZ") },
                ].map(item => (
                  <div key={item.label}>
                    <Text style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#94a3b8", display: "block", marginBottom: 4 }}>{item.label}</Text>
                    <Text style={{ fontSize: 14, color: "#0b1c30", fontFamily: item.mono ? "monospace" : "inherit" }}>{item.value}</Text>
                  </div>
                ))}
              </div>

              {docDetail && (
                <>
                  <Divider style={{ margin: "20px 0" }} />
                  <Text style={{ fontSize: 12, fontWeight: 700, color: "#0b1c30", display: "block", marginBottom: 12 }}>Значения полей</Text>
                  {docDetail.fieldValues.length === 0 ? (
                    <Text style={{ fontSize: 12, color: "#94a3b8" }}>Нет заполненных полей</Text>
                  ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                      {docDetail.fieldValues.map(fv => (
                        <div key={fv.id} style={{ padding: "10px 14px", borderRadius: 10, background: "#f8fafc", border: "1px solid #f1f5f9" }}>
                          <Text style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#94a3b8", display: "block", marginBottom: 3, fontFamily: "monospace" }}>{fv.fieldKey}</Text>
                          <Text style={{ fontSize: 13, color: "#0b1c30" }}>{fv.fieldValue || "—"}</Text>
                        </div>
                      ))}
                    </div>
                  )}

                  {(docDetail.missingRequiredFields?.length ?? 0) > 0 && (
                    <>
                      <Divider style={{ margin: "20px 0" }} />
                      <Text style={{ fontSize: 12, fontWeight: 700, color: "#ef4444", display: "block", marginBottom: 8 }}>Незаполненные обязательные поля</Text>
                      <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
                        {docDetail.missingRequiredFields?.map(f => (
                          <span key={f} style={{ fontSize: 11, fontFamily: "monospace", color: "#ef4444", background: "rgba(239,68,68,0.08)", padding: "2px 8px", borderRadius: 6, border: "1px solid rgba(239,68,68,0.2)" }}>{f}</span>
                        ))}
                      </div>
                    </>
                  )}
                </>
              )}

              <Divider style={{ margin: "20px 0" }} />
              <Button icon={<MessageSquare size={14} />} onClick={() => { closeDetail(); openReview(detailDrawer.record!.id); }}
                style={{ width: "100%", borderRadius: 10, borderColor: "#1a2744", color: "#1a2744", fontWeight: 600, height: 40 }}>
                Добавить ревью
              </Button>
            </div>
          </>
        )}
      </Drawer>
    </div>
  );
}
