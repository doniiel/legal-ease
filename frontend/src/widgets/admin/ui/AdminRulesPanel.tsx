import { useState } from "react";
import { Table, Switch, Tag, Typography, Input, Tooltip } from "antd";
import type { ColumnsType } from "antd/es/table";
import { Search } from "lucide-react";
import { useRules } from "../../../features/admin/model/use-rules";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import editorialTableComponents from "../../../shared/ui/table-components";
import type {
  AdminValidationRule,
  AdminRiskRule,
  AdminMatchingRule,
  AdminConditionalRule,
  AdminRequiredDocRule,
} from "../../../features/admin/api/admin-rules-api";

const { Text } = Typography;

type TabKey = "validation" | "risk" | "matching" | "conditional" | "required-docs";

const TABS: { key: TabKey; label: string }[] = [
  { key: "validation",    label: "Валидация" },
  { key: "risk",          label: "Риски" },
  { key: "matching",      label: "Матчинг" },
  { key: "conditional",   label: "Условные" },
  { key: "required-docs", label: "Обяз. документы" },
];

// ─── Reusable inline styles ───────────────────────────────────
const monoTag = (v: string) => (
  <span style={{ background: "#e5eeff", color: "#2d476f", padding: "2px 8px", borderRadius: 4, fontSize: 11, fontFamily: "monospace", fontWeight: 700, textTransform: "uppercase" }}>
    {v}
  </span>
);

const monoText = (v: string) => (
  <Text style={{ fontFamily: "monospace", fontSize: 12, color: "#475569" }}>{v}</Text>
);

// ─── Active toggle cell ───────────────────────────────────────
function ActiveCell({ active, onToggle }: { active: boolean; onToggle: () => void }) {
  return (
    <div style={{ display: "flex", justifyContent: "flex-end" }}>
      <Switch
        checked={active}
        size="small"
        onChange={onToggle}
        style={{ background: active ? "#059669" : undefined }}
      />
    </div>
  );
}

// ─── Template cell ────────────────────────────────────────────
function TemplateCell({ id, name }: { id: number; name?: string }) {
  return (
    <div>
      <Text strong style={{ display: "block", fontSize: 13, color: "#0F2A44", fontFamily: "Manrope, sans-serif" }}>
        {name ?? `Template #${id}`}
      </Text>
      <Text style={{ fontSize: 10, color: "#7790bd", fontWeight: 600 }}>ID: {id}</Text>
    </div>
  );
}

// ─── Per-tab columns ──────────────────────────────────────────
function validationColumns(onToggle: (id: number) => void): ColumnsType<AdminValidationRule> {
  return [
    { title: "Шаблон",          key: "tpl",    render: (_, r) => <TemplateCell id={r.templateId} name={r.label} /> },
    { title: "Поле",            key: "field",  render: (_, r) => monoText(r.fieldKey) },
    { title: "Оператор",        key: "op",     render: (_, r) => monoTag(r.ruleType) },
    { title: "Ожидаемое",       key: "val",    render: (_, r) => monoText(r.ruleValue) },
    { title: "Сообщение об ошибке", key: "err", ellipsis: true, render: (_, r) => <Text style={{ fontSize: 13, fontStyle: "italic", color: "#475569" }}>"{r.errorMessage}"</Text> },
    { title: "Активно", key: "active", width: 90, render: (_, r) => <ActiveCell active={r.active} onToggle={() => onToggle(r.id)} /> },
  ];
}

function riskColumns(onToggle: (id: number) => void): ColumnsType<AdminRiskRule> {
  return [
    { title: "Шаблон",    key: "tpl",   render: (_, r) => <TemplateCell id={r.templateId} /> },
    { title: "Код",       key: "code",  render: (_, r) => monoText(r.ruleCode) },
    { title: "Уровень",   key: "lvl",   width: 110, render: (_, r) => (
        <Tag color={r.level === "HIGH" ? "red" : r.level === "MEDIUM" ? "orange" : "blue"} style={{ fontWeight: 700, fontSize: 11 }}>{r.level}</Tag>
    )},
    { title: "Сообщение", key: "msg",   ellipsis: true, render: (_, r) => <Text style={{ fontSize: 13, fontStyle: "italic", color: "#475569" }}>"{r.message}"</Text> },
    { title: "Условие",   key: "cond",  ellipsis: true, render: (_, r) => monoText(r.condition) },
    { title: "Активно",   key: "active",width: 90, render: (_, r) => <ActiveCell active={r.active} onToggle={() => onToggle(r.id)} /> },
  ];
}

function matchingColumns(onToggle: (id: number) => void): ColumnsType<AdminMatchingRule> {
  return [
    { title: "Шаблон",       key: "tpl",  render: (_, r) => <TemplateCell id={r.templateId} name={r.templateTitle} /> },
    { title: "Категория",    key: "cat",  render: (_, r) => <Tag color="geekblue" style={{ fontWeight: 600, fontSize: 11 }}>{r.categoryName}</Tag> },
    { title: "Ключевые слова", key: "kw", ellipsis: true, render: (_, r) => monoText(r.keywords) },
    { title: "Базовый балл", key: "score",width: 110, align: "center" as const, render: (_, r) => <Text strong style={{ color: "#0F2A44" }}>{r.baseScore}</Text> },
    { title: "Активно",      key: "active",width: 90, render: (_, r) => <ActiveCell active={r.active} onToggle={() => onToggle(r.id)} /> },
  ];
}

function conditionalColumns(onToggle: (id: number) => void): ColumnsType<AdminConditionalRule> {
  return [
    { title: "Шаблон",     key: "tpl",   render: (_, r) => <TemplateCell id={r.templateId} /> },
    { title: "Если поле",  key: "cf",    render: (_, r) => monoText(r.conditionField) },
    { title: "Оператор",   key: "op",    width: 110, render: (_, r) => monoTag(r.conditionOperator) },
    { title: "Значение",   key: "cv",    render: (_, r) => monoText(r.conditionValue) },
    { title: "Тип действия",key: "at",   width: 130, render: (_, r) => <Tag color="purple" style={{ fontWeight: 600, fontSize: 11 }}>{r.actionType}</Tag> },
    { title: "Цель",       key: "atgt",  render: (_, r) => monoText(r.actionTarget) },
    { title: "Активно",    key: "active",width: 90, render: (_, r) => <ActiveCell active={r.active} onToggle={() => onToggle(r.id)} /> },
  ];
}

function requiredDocColumns(onToggle: (id: number) => void): ColumnsType<AdminRequiredDocRule> {
  return [
    { title: "Шаблон",      key: "tpl",  render: (_, r) => <TemplateCell id={r.templateId} /> },
    { title: "Документ",    key: "title",render: (_, r) => <Text strong style={{ fontSize: 13, color: "#0F2A44" }}>{r.title}</Text> },
    { title: "Причина",     key: "rsn",  ellipsis: true, render: (_, r) => <Text style={{ fontSize: 13, color: "#475569" }}>{r.reason}</Text> },
    { title: "Обязательный",key: "mand", width: 120, align: "center" as const, render: (_, r) => (
        <Tag color={r.mandatory ? "red" : "default"} style={{ fontWeight: 700, fontSize: 11 }}>{r.mandatory ? "Да" : "Нет"}</Tag>
    )},
    { title: "Активно",     key: "active",width: 90, render: (_, r) => <ActiveCell active={r.active} onToggle={() => onToggle(r.id)} /> },
  ];
}

// ─── Summary stat cards ───────────────────────────────────────
function SummaryCard({ label, value, total, color, description }: { label: string; value: number; total: number; color: string; description: string }) {
  return (
    <div style={{ padding: "20px 20px 18px", background: "#e5eeff", borderRadius: 12, borderLeft: `4px solid ${color}` }}>
      <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#7790bd", marginBottom: 10 }}>
        {label}
      </div>
      <div style={{ display: "flex", alignItems: "flex-end", gap: 6, marginBottom: 6 }}>
        <span style={{ fontSize: 28, fontWeight: 800, color, lineHeight: 1, fontFamily: "Manrope, sans-serif" }}>{value}</span>
        <span style={{ fontSize: 13, color: "#94a3b8", marginBottom: 2 }}>/ {total}</span>
      </div>
      <div style={{ height: 4, borderRadius: 4, background: "rgba(0,0,0,0.08)", marginBottom: 12, overflow: "hidden" }}>
        <div style={{ height: "100%", borderRadius: 4, background: color, width: total > 0 ? `${Math.round((value / total) * 100)}%` : "0%", transition: "width 0.4s" }} />
      </div>
      <p style={{ fontSize: 11, color: "#64748b", margin: 0, lineHeight: 1.5 }}>{description}</p>
    </div>
  );
}

// ─── Main Panel ───────────────────────────────────────────────
export default function AdminRulesPanel() {
  const isMobile = useIsMobile();
  const [activeTab, setActiveTab] = useState<TabKey>("validation");
  const [search, setSearch] = useState("");

  const {
    validationRules, vLoading,
    riskRules,       rLoading,
    matchingRules,   mLoading,
    conditionalRules,cLoading,
    requiredDocRules,dLoading,
    totalActive, totalAll,
    handleToggleValidation, handleToggleRisk, handleToggleMatching,
    handleToggleConditional, handleToggleRequiredDoc,
  } = useRules();

  // ── Filter by search per tab ──────────────────────────────
  const q = search.toLowerCase();
  const filteredValidation  = validationRules.filter((r)  => r.fieldKey.toLowerCase().includes(q) || r.label?.toLowerCase().includes(q));
  const filteredRisk        = riskRules.filter((r)        => r.ruleCode?.toLowerCase().includes(q) || r.message?.toLowerCase().includes(q));
  const filteredMatching    = matchingRules.filter((r)    => r.templateTitle?.toLowerCase().includes(q) || r.categoryName?.toLowerCase().includes(q));
  const filteredConditional = conditionalRules.filter((r) => r.conditionField?.toLowerCase().includes(q) || r.actionTarget?.toLowerCase().includes(q));
  const filteredRequired    = requiredDocRules.filter((r) => r.title?.toLowerCase().includes(q) || r.reason?.toLowerCase().includes(q));

  // ── Render the right table ────────────────────────────────
  const tableProps = (() => {
    switch (activeTab) {
      case "validation":    return { columns: validationColumns(handleToggleValidation),   dataSource: filteredValidation,  loading: vLoading };
      case "risk":          return { columns: riskColumns(handleToggleRisk),                dataSource: filteredRisk,         loading: rLoading };
      case "matching":      return { columns: matchingColumns(handleToggleMatching),        dataSource: filteredMatching,     loading: mLoading };
      case "conditional":   return { columns: conditionalColumns(handleToggleConditional),  dataSource: filteredConditional,  loading: cLoading };
      case "required-docs": return { columns: requiredDocColumns(handleToggleRequiredDoc),  dataSource: filteredRequired,     loading: dLoading };
    }
  })();

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: isMobile ? "24px 16px 80px" : "48px 48px 96px", display: "flex", justifyContent: "space-between", flexDirection: isMobile ? "column" : ("row" as React.CSSProperties["flexDirection"]), alignItems: isMobile ? "flex-start" : "flex-end", position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: 0, fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
              Управление правилами
            </h1>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: 0, background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)" }}>
            <div style={{ padding: "12px 20px" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Total Active Rules</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{totalActive.toLocaleString()}</div>
            </div>
            <div style={{ width: 1, height: 40, background: "rgba(255,255,255,0.1)" }} />
            <div style={{ padding: "12px 20px", textAlign: "right" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Total Rules</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{totalAll.toLocaleString()}</div>
            </div>
          </div>
        </div>
      </div>

      {/* ── Overlapping content card ── */}
      <div style={{ padding: isMobile ? "0 16px 24px" : "0 48px 48px", marginTop: -48, position: "relative", zIndex: 10 }}>
        <div style={{ background: "#fff", borderRadius: 12, boxShadow: "0 20px 60px rgba(11,28,48,0.12)", overflow: "hidden" }}>

          {/* Filter bar */}
          <div style={{ padding: "20px 24px", background: "#eff4ff", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 20, borderBottom: "1px solid rgba(197,198,210,0.2)", flexWrap: "wrap" }}>
            {/* Search */}
            <div style={{ position: "relative", minWidth: 280, maxWidth: 400, flex: 1 }}>
              <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" }}>
                <Search size={15} color="#757682" />
              </span>
              <Input
                placeholder="Фильтр по названию шаблона..."
                value={search}
                onChange={(e) => { setSearch(e.target.value); }}
                style={{ paddingLeft: 36, borderRadius: 8, background: "#fff", border: "1px solid rgba(197,198,210,0.4)", height: 40 }}
                allowClear
              />
            </div>

            {/* Tab pills */}
            <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
              {TABS.map((tab) => {
                const isActive = activeTab === tab.key;
                return (
                  <Tooltip key={tab.key} title={`${tab.label}: ${getTabCount(tab.key, { validationRules, riskRules, matchingRules, conditionalRules, requiredDocRules })} правил`}>
                    <button
                      onClick={() => { setActiveTab(tab.key); setSearch(""); }}
                      style={{
                        padding: "8px 20px", borderRadius: 8, border: "none",
                        background: isActive ? "#0F2A44" : "transparent",
                        color: isActive ? "#fff" : "#64748b",
                        fontWeight: isActive ? 700 : 500, fontSize: 13,
                        cursor: "pointer", whiteSpace: "nowrap",
                        boxShadow: isActive ? "0 4px 12px rgba(15,42,68,0.25)" : "none",
                        transition: "all 0.15s",
                      }}
                      onMouseEnter={(e) => { if (!isActive) e.currentTarget.style.background = "#dce9ff"; }}
                      onMouseLeave={(e) => { if (!isActive) e.currentTarget.style.background = "transparent"; }}
                    >
                      {tab.label}
                    </button>
                  </Tooltip>
                );
              })}
            </div>
          </div>

          {/* Table */}
          <div style={{ overflowX: "auto" }}>
            <Table
              components={editorialTableComponents}
              columns={tableProps.columns as ColumnsType<object>}
              dataSource={tableProps.dataSource as object[]}
              rowKey="id"
              loading={tableProps.loading}
              pagination={{
                pageSize: 10,
                showSizeChanger: false,
                showTotal: (total) => `Показано ${Math.min(10, total)} из ${total} правил`,
                style: { padding: "12px 24px", margin: 0, background: "#eff4ff", borderTop: "1px solid rgba(197,198,210,0.2)" },
              }}
              scroll={{ x: "max-content" }}
              style={{ borderRadius: 0 }}
            />
          </div>

          {/* Summary stat cards */}
          <div style={{ padding: "28px 24px", borderTop: "1px solid #f1f5f9" }}>
            <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)", gap: 16 }}>
              <SummaryCard
                label="Валидация"
                value={validationRules.filter((r) => r.active).length}
                total={validationRules.length}
                color="#059669"
                description="Базовые технические проверки форматов данных и обязательности полей."
              />
              <SummaryCard
                label="Риск"
                value={riskRules.filter((r) => r.active).length}
                total={riskRules.length}
                color="#ef4444"
                description="Критически важные правила проверки санкционных списков и комплаенса."
              />
              <SummaryCard
                label="Матчинг"
                value={matchingRules.filter((r) => r.active).length}
                total={matchingRules.length}
                color="#1677ff"
                description="Правила сопоставления шаблонов с категориями по ключевым словам."
              />
              <SummaryCard
                label="Условные"
                value={conditionalRules.filter((r) => r.active).length}
                total={conditionalRules.length}
                color="#7c3aed"
                description="Условные правила и обязательные документы по шаблонам."
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Helper ───────────────────────────────────────────────────
function getTabCount(
  key: TabKey,
  data: {
    validationRules: { active: boolean }[];
    riskRules: { active: boolean }[];
    matchingRules: { active: boolean }[];
    conditionalRules: { active: boolean }[];
    requiredDocRules: { active: boolean }[];
  }
): string {
  const map: Record<TabKey, { active: boolean }[]> = {
    "validation":    data.validationRules,
    "risk":          data.riskRules,
    "matching":      data.matchingRules,
    "conditional":   data.conditionalRules,
    "required-docs": data.requiredDocRules,
  };
  const arr = map[key];
  return `${arr.filter((r) => r.active).length}/${arr.length}`;
}
