import {
  Table, Button, Modal, Form, Input, InputNumber, Select,
  Typography, Tooltip,
} from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import type { ColumnsType } from "antd/es/table";
import {
  GitMerge, Search, Plus, Pencil, Trash2, RotateCcw,
  Target, CheckCircle2, Hash, Tag as TagIcon,
} from "lucide-react";
import type { MatchingRule } from "../../../features/lawyer/api/lawyer-matching-rules-api";
import { useMatchingRules } from "../../../features/lawyer/model/use-matching-rules";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;

// ─── Status pill ──────────────────────────────────────────────
function ActivePill({ active }: { active: boolean }) {
  const cfg = active
    ? { color: "#059669", bg: "rgba(5,150,105,0.08)", border: "rgba(5,150,105,0.2)", label: "Активно" }
    : { color: "#94a3b8", bg: "rgba(148,163,184,0.08)", border: "rgba(148,163,184,0.2)", label: "Неактивно" };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: cfg.color, flexShrink: 0 }} />
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

// ─── Main panel ───────────────────────────────────────────────
export default function LawyerMatchingRulesPanel() {
  const isMobile = useIsMobile();
  const {
    form, search, setSearch, modalOpen,
    editingRule, isLoading, isCreating, isUpdating, isDeleting,
    rules, filtered, activeCount,
    categories, templates,
    openCreate, openEdit, closeModal,
    handleSubmit, handleDelete,
  } = useMatchingRules();

  const columns: ColumnsType<MatchingRule> = [
    {
      title: "Шаблон",
      key: "template",
      render: (_, r) => (
        <div>
          <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>{r.templateTitle || "—"}</Text>
          <Text style={{ fontSize: 11, color: "#94a3b8", display: "block", marginTop: 1, fontFamily: "monospace" }}>ID {r.templateId}</Text>
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      width: 180,
      render: (name: string) => (
        <div style={{ display: "inline-flex", alignItems: "center", padding: "3px 10px", borderRadius: 20, background: "rgba(15,42,68,0.06)", border: "1px solid rgba(15,42,68,0.12)" }}>
          <span style={{ fontSize: 11, fontWeight: 600, color: "#0F2A44" }}>{name || "—"}</span>
        </div>
      ),
    },
    {
      title: "Ключевые слова",
      dataIndex: "keywords",
      key: "keywords",
      render: (kw: string) => (
        <div style={{ display: "flex", flexWrap: "wrap", gap: 4 }}>
          {(kw || "").split(",").filter(Boolean).map((k, i) => (
            <span key={i} style={{ fontSize: 11, fontFamily: "monospace", color: "#1677ff", background: "rgba(22,119,255,0.08)", padding: "2px 8px", borderRadius: 6, border: "1px solid rgba(22,119,255,0.15)" }}>
              {k.trim()}
            </span>
          ))}
          {!kw && <Text style={{ color: "#94a3b8" }}>—</Text>}
        </div>
      ),
    },
    {
      title: "Базовый балл",
      dataIndex: "baseScore",
      key: "baseScore",
      width: 130,
      align: "center" as const,
      render: (score: number) => (
        <span style={{ fontFamily: "monospace", fontSize: 13, fontWeight: 700, color: "#7c3aed", background: "rgba(124,58,237,0.08)", padding: "3px 10px", borderRadius: 8, border: "1px solid rgba(124,58,237,0.15)" }}>
          {score}
        </span>
      ),
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 130,
      render: (active: boolean) => <ActivePill active={active} />,
    },
    {
      title: "Действия",
      key: "actions",
      width: 100,
      align: "center" as const,
      render: (_, record) => (
        <div style={{ display: "flex", gap: 2, justifyContent: "center" }}>
          <IconBtn icon={<Pencil size={14} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(record)} />
          <IconBtn icon={isDeleting ? <span style={{ fontSize: 10 }}>…</span> : <Trash2 size={14} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(record.id)} />
        </div>
      ),
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: isMobile ? "24px 16px 48px" : "40px 40px 56px", display: "flex", justifyContent: "space-between", alignItems: "flex-end", position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>Правила матчинга</h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>Настройка логики подбора шаблонов для клиентских запросов</p>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ display: "flex", alignItems: "center", background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)" }}>
              <div style={{ padding: "12px 20px" }}>
                <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Всего</div>
                <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{rules.length}</div>
              </div>
              <div style={{ width: 1, height: 40, background: "rgba(255,255,255,0.1)" }} />
              <div style={{ padding: "12px 20px" }}>
                <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Активных</div>
                <div style={{ fontSize: 22, fontWeight: 800, color: "#059669", fontFamily: "Manrope, sans-serif" }}>{activeCount}</div>
              </div>
            </div>
            <Button icon={<Plus size={15} />} onClick={openCreate}
              style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "#fff", borderRadius: 10, height: 42, fontWeight: 600, paddingLeft: 20, paddingRight: 20 }}>
              Создать правило
            </Button>
          </div>
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 12px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "flex", gap: 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего правил"   value={rules.length}      color="#0F2A44" icon={<GitMerge size={26} />} loading={isLoading} />
          <StatCard label="Активных"       value={activeCount}        color="#059669" icon={<CheckCircle2 size={26} />} />
          <StatCard label="Неактивных"     value={rules.length - activeCount} color="#94a3b8" icon={<Target size={26} />} />
          <StatCard label="Шаблонов"       value={[...new Set(rules.map(r => r.templateId))].length} color="#7c3aed" icon={<Hash size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "20px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <Input prefix={<Search size={15} color="#757682" />} placeholder="По ключевым словам, шаблону или категории..." allowClear value={search} onChange={e => setSearch(e.target.value)}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }} />
            </div>
            {search && (
              <Button icon={<RotateCcw size={13} />} type="text" onClick={() => setSearch("")} style={{ color: "#94a3b8", height: 40 }}>
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
            pagination={{ pageSize: 10, showSizeChanger: false, showTotal: t => `Всего ${t} правил`, style: { padding: "16px 32px", margin: 0 } }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Create / Edit Modal ── */}
      <Modal
        open={modalOpen}
        onCancel={closeModal}
        title={null}
        footer={
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <Button onClick={closeModal}>Отмена</Button>
            <Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating}
              style={{ background: "#0F2A44", borderColor: "#0F2A44" }}>
              {editingRule ? "Сохранить" : "Создать"}
            </Button>
          </div>
        }
        width={560} destroyOnHidden styles={{ body: { padding: 0 } }}
      >
        <div style={{ background: "linear-gradient(135deg, #0F2A44, #1a4070)", borderRadius: "8px 8px 0 0", padding: "20px 28px 16px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <GitMerge size={20} color="#fff" />
            </div>
            <div>
              <Text style={{ color: "#fff", fontWeight: 700, fontSize: 16, display: "block" }}>{editingRule ? "Редактировать правило" : "Новое правило матчинга"}</Text>
              <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>Настройка логики подбора шаблона для клиентов</Text>
            </div>
          </div>
        </div>
        <div style={{ padding: "24px 28px" }}>
          <Form form={form} layout="vertical">
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Шаблон</Text>} name="templateId" rules={[{ required: true, message: "Выберите шаблон" }]}>
              <Select showSearch placeholder="Выберите шаблон" size="large"
                filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
                options={templates.map(t => ({ value: t.id, label: t.title }))} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Категория</Text>} name="categoryId" rules={[{ required: true, message: "Выберите категорию" }]}>
              <Select showSearch placeholder="Выберите категорию" size="large"
                filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
                options={categories.map(c => ({ value: c.id, label: c.name }))} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ключевые слова</Text>} name="keywords" rules={[{ required: true, message: "Введите ключевые слова" }]}
              extra={<span style={{ fontSize: 11, color: "#94a3b8" }}>Через запятую: купля, продажа, договор</span>}>
              <Input placeholder="купля, продажа, аренда" prefix={<TagIcon size={14} color="#9ca3af" />} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Базовый балл</Text>} name="baseScore" rules={[{ required: true, message: "Укажите базовый балл" }]}>
              <InputNumber style={{ width: "100%" }} size="large" min={0} placeholder="100" />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </div>
  );
}
