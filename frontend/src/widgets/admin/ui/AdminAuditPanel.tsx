import {
  Table, Select, Button, Typography, Tooltip, Drawer, Descriptions,
  Badge, Divider, Input,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  ScrollText, Search, RotateCcw, Eye,
  PlusCircle, RefreshCw, Trash2, CheckCircle, XCircle,
  ShieldCheck, ShieldX, Lock, Unlock, FileSearch, Users,
} from "lucide-react";
import { useAdminAudit } from "../../../features/admin/model/use-admin-audit";
import type { AuditLog } from "../../../features/admin/api/admin-audit-api";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;

// ─── Action config ────────────────────────────────────────────
const ACTION_CONFIG: Record<string, { color: string; bg: string; border: string; icon: React.ReactNode; label: string }> = {
  CREATE:   { color: "#059669", bg: "rgba(5,150,105,0.08)",   border: "rgba(5,150,105,0.2)",   icon: <PlusCircle size={11} />,  label: "Создание"     },
  UPDATE:   { color: "#1677ff", bg: "rgba(22,119,255,0.08)",  border: "rgba(22,119,255,0.2)",  icon: <RefreshCw size={11} />,   label: "Обновление"   },
  DELETE:   { color: "#ef4444", bg: "rgba(239,68,68,0.08)",   border: "rgba(239,68,68,0.2)",   icon: <Trash2 size={11} />,      label: "Удаление"     },
  APPROVE:  { color: "#059669", bg: "rgba(5,150,105,0.08)",   border: "rgba(5,150,105,0.2)",   icon: <CheckCircle size={11} />, label: "Одобрение"    },
  REJECT:   { color: "#ef4444", bg: "rgba(239,68,68,0.08)",   border: "rgba(239,68,68,0.2)",   icon: <XCircle size={11} />,     label: "Отклонение"   },
  BLOCK:    { color: "#f59e0b", bg: "rgba(245,158,11,0.08)",  border: "rgba(245,158,11,0.2)",  icon: <Lock size={11} />,        label: "Блокировка"   },
  UNBLOCK:  { color: "#0891b2", bg: "rgba(8,145,178,0.08)",   border: "rgba(8,145,178,0.2)",   icon: <Unlock size={11} />,      label: "Разблокировка"},
  LOGIN:    { color: "#7c3aed", bg: "rgba(124,58,237,0.08)",  border: "rgba(124,58,237,0.2)",  icon: <ShieldCheck size={11} />, label: "Вход"         },
  LOGOUT:   { color: "#64748b", bg: "rgba(100,116,139,0.08)", border: "rgba(100,116,139,0.2)", icon: <ShieldX size={11} />,     label: "Выход"        },
};

function getActionCfg(action: string) {
  return ACTION_CONFIG[action] ?? {
    color: "#64748b", bg: "rgba(100,116,139,0.08)", border: "rgba(100,116,139,0.2)",
    icon: <FileSearch size={11} />, label: action,
  };
}

// ─── Action pill ──────────────────────────────────────────────
function ActionPill({ action }: { action: string }) {
  const cfg = getActionCfg(action);
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ display: "flex", color: cfg.color }}>{cfg.icon}</span>
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color, letterSpacing: "0.04em" }}>{cfg.label}</span>
    </div>
  );
}

// ─── Entity type pill ─────────────────────────────────────────
function EntityPill({ type }: { type: string }) {
  return (
    <div style={{ display: "inline-flex", alignItems: "center", padding: "3px 10px", borderRadius: 20, background: "rgba(15,42,68,0.06)", border: "1px solid rgba(15,42,68,0.12)" }}>
      <span style={{ fontSize: 11, fontWeight: 600, color: "#0F2A44", letterSpacing: "0.04em" }}>{type || "—"}</span>
    </div>
  );
}

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({ icon, tooltip, onClick }: { icon: React.ReactNode; tooltip: string; onClick?: () => void }) {
  return (
    <Tooltip title={tooltip}>
      <button
        onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={(e) => { e.currentTarget.style.background = "#f1f5f9"; e.currentTarget.style.color = "#0F2A44"; }}
        onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
      >
        {icon}
      </button>
    </Tooltip>
  );
}

const fmtDate = (v: string) =>
  new Date(v).toLocaleString("ru-KZ", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });

// ─── Main panel ───────────────────────────────────────────────
export default function AdminAuditPanel() {
  const {
    page, setPage,
    emailSearch, setEmailSearch,
    actionFilter, setActionFilter,
    entityTypeFilter, setEntityTypeFilter,
    detailDrawer, setDetailDrawer,
    data, isLoading,
    filtered, actionOptions, entityTypeOptions,
    uniqueUsers, activeFilterCount,
    resetFilters,
  } = useAdminAudit();

  const columns: ColumnsType<AuditLog> = [
    {
      title: "Дата",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 170,
      render: (v: string) => (
        <Text style={{ fontSize: 12, color: "#64748b", fontVariantNumeric: "tabular-nums" }}>{fmtDate(v)}</Text>
      ),
    },
    {
      title: "Пользователь",
      key: "user",
      render: (_, r) => (
        <div>
          <Text style={{ fontSize: 13, color: "#0b1c30", display: "block" }}>{r.userEmail || "—"}</Text>
          <Text style={{ fontSize: 11, color: "#94a3b8" }}>ID: {r.userId}</Text>
        </div>
      ),
    },
    {
      title: "Действие",
      dataIndex: "action",
      key: "action",
      width: 150,
      render: (v: string) => <ActionPill action={v} />,
    },
    {
      title: "Тип сущности",
      dataIndex: "entityType",
      key: "entityType",
      width: 150,
      render: (v: string) => <EntityPill type={v} />,
    },
    {
      title: "ID сущности",
      dataIndex: "entityId",
      key: "entityId",
      width: 110,
      align: "center" as const,
      render: (v: number) => (
        <span style={{ fontFamily: "monospace", fontSize: 12, color: "#475569", background: "#f8fafc", padding: "2px 8px", borderRadius: 6, border: "1px solid #e2e8f0" }}>
          {v ?? "—"}
        </span>
      ),
    },
    {
      title: "Детали",
      dataIndex: "details",
      key: "details",
      render: (v: string) =>
        v ? (
          <Tooltip title={v}>
            <Text style={{ fontSize: 12, color: "#64748b", cursor: "help" }}>
              {v.length > 60 ? v.slice(0, 60) + "…" : v}
            </Text>
          </Tooltip>
        ) : (
          <Text style={{ color: "#cbd5e1", fontSize: 12 }}>—</Text>
        ),
    },
    {
      title: "Просмотр",
      key: "actions",
      width: 80,
      align: "center" as const,
      render: (_, record) => (
        <IconBtn icon={<Eye size={15} />} tooltip="Подробнее" onClick={() => setDetailDrawer({ open: true, record })} />
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
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
              Аудит логи
            </h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>
              История всех действий пользователей в системе
            </p>
          </div>
          <div style={{ display: "flex", alignItems: "center", background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)" }}>
            <div style={{ padding: "12px 20px" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Всего</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif" }}>{data?.totalElements ?? "—"}</div>
            </div>
            <div style={{ width: 1, height: 40, background: "rgba(255,255,255,0.1)" }} />
            <div style={{ padding: "12px 20px", textAlign: "right" }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.12em", color: "#7790bd", marginBottom: 4 }}>Страница</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: "#a5f3fc", fontFamily: "Manrope, sans-serif" }}>{page}</div>
            </div>
          </div>
        </div>
      </div>

      <div style={{ padding: "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "flex", gap: 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего записей"       value={data?.totalElements} color="#0F2A44" icon={<ScrollText size={26} />} loading={isLoading} />
          <StatCard label="На странице"         value={filtered.length}     color="#1677ff" icon={<FileSearch size={26} />} />
          <StatCard label="Уникальных польз."   value={uniqueUsers}         color="#7c3aed" icon={<Users size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12, flexWrap: "wrap" }}>
            {/* Email search */}
            <div style={{ flex: 1, minWidth: 200 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск по email</div>
              <Input
                prefix={<Search size={15} color="#757682" />}
                placeholder="user@example.com..."
                allowClear
                value={emailSearch}
                onChange={(e) => setEmailSearch(e.target.value)}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }}
              />
            </div>

            {/* Action filter */}
            <div style={{ minWidth: 180 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Действие</div>
              <Select
                allowClear
                placeholder="Все действия"
                value={actionFilter}
                onChange={setActionFilter}
                style={{ width: "100%" }}
                options={actionOptions.map((a) => ({ value: a, label: a }))}
                optionRender={(opt) => {
                  const cfg = getActionCfg(opt.value as string);
                  return (
                    <span style={{ display: "inline-flex", alignItems: "center", gap: 6 }}>
                      <span style={{ color: cfg.color, display: "flex" }}>{cfg.icon}</span>
                      <span>{cfg.label}</span>
                    </span>
                  );
                }}
                labelRender={(props) => {
                  const cfg = getActionCfg(props.value as string);
                  return (
                    <span style={{ display: "inline-flex", alignItems: "center", gap: 6 }}>
                      <span style={{ color: cfg.color, display: "flex" }}>{cfg.icon}</span>
                      <span>{cfg.label}</span>
                    </span>
                  );
                }}
              />
            </div>

            {/* Entity type filter */}
            <div style={{ minWidth: 180 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Тип сущности</div>
              <Select
                allowClear
                placeholder="Все типы"
                value={entityTypeFilter}
                onChange={setEntityTypeFilter}
                style={{ width: "100%" }}
                options={entityTypeOptions.map((e) => ({ value: e, label: e }))}
              />
            </div>

            {/* Reset */}
            {activeFilterCount > 0 && (
              <Button
                icon={<RotateCcw size={13} />}
                type="text"
                onClick={resetFilters}
                style={{ color: "#94a3b8", height: 40, alignSelf: "flex-end" }}
              >
                Сбросить
              </Button>
            )}

            <div style={{ marginLeft: "auto", alignSelf: "flex-end", paddingBottom: 4 }}>
              {activeFilterCount > 0 && (
                <Badge count={activeFilterCount} size="small" color="#1677ff" style={{ marginRight: 8 }} />
              )}
              <Text style={{ fontSize: 12, color: "#94a3b8" }}>
                Показано <strong style={{ color: "#0b1c30" }}>{filtered.length}</strong> из <strong style={{ color: "#0b1c30" }}>{data?.totalElements ?? 0}</strong>
              </Text>
            </div>
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
            pagination={{
              current: page,
              total: data?.totalElements,
              pageSize: 20,
              onChange: (p) => setPage(p),
              showSizeChanger: false,
              showTotal: (total) => `Всего ${total} записей`,
              style: { padding: "16px 32px", margin: 0 },
            }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      {/* ── Detail drawer ── */}
      <Drawer
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#eff4ff", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <ScrollText size={15} color="#0F2A44" />
            </div>
            <span style={{ fontWeight: 700, color: "#0b1c30" }}>Запись #{detailDrawer.record?.id}</span>
          </div>
        }
        open={detailDrawer.open}
        onClose={() => setDetailDrawer({ open: false, record: null })}
        styles={{ body: { padding: 24 }, wrapper: { width: 440 } }}
      >
        {detailDrawer.record && (() => {
          const r = detailDrawer.record;
          const cfg = getActionCfg(r.action);
          return (
            <>
              {/* Action banner */}
              <div style={{ background: cfg.bg, border: `1px solid ${cfg.border}`, borderRadius: 10, padding: "14px 18px", marginBottom: 24, display: "flex", alignItems: "center", gap: 10 }}>
                <span style={{ display: "flex", color: cfg.color }}>{cfg.icon}</span>
                <Text style={{ color: cfg.color, fontWeight: 700, fontSize: 13 }}>{cfg.label}</Text>
                <EntityPill type={r.entityType} />
              </div>

              {/* User */}
              <Text style={{ fontSize: 11, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.08em", fontWeight: 700, display: "block", marginBottom: 12 }}>Пользователь</Text>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 20, padding: "12px 16px", background: "#f8fafc", borderRadius: 10, border: "1px solid #e2e8f0" }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "#eff4ff", border: "1px solid #dce9ff", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                  <Text style={{ fontWeight: 800, color: "#0F2A44", fontSize: 14 }}>
                    {r.userEmail?.[0]?.toUpperCase() ?? "?"}
                  </Text>
                </div>
                <div>
                  <Text strong style={{ display: "block", color: "#0b1c30", fontSize: 14 }}>{r.userEmail || "—"}</Text>
                  <Text style={{ fontSize: 12, color: "#94a3b8" }}>ID: {r.userId}</Text>
                </div>
              </div>

              <Divider style={{ margin: "0 0 20px" }} />

              <Descriptions column={1} size="small" colon={false} styles={{ label: { color: "#94a3b8", fontSize: 12 }, content: { fontSize: 13 } }}>
                <Descriptions.Item label="Дата события">{fmtDate(r.createdDate)}</Descriptions.Item>
                <Descriptions.Item label="Действие"><ActionPill action={r.action} /></Descriptions.Item>
                <Descriptions.Item label="Тип сущности"><EntityPill type={r.entityType} /></Descriptions.Item>
                <Descriptions.Item label="ID сущности">
                  <span style={{ fontFamily: "monospace", background: "#f8fafc", padding: "2px 8px", borderRadius: 6, border: "1px solid #e2e8f0", fontSize: 12 }}>
                    {r.entityId ?? "—"}
                  </span>
                </Descriptions.Item>
              </Descriptions>

              {r.details && (
                <>
                  <Divider style={{ margin: "16px 0" }} />
                  <Text style={{ fontSize: 11, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.08em", fontWeight: 700, display: "block", marginBottom: 8 }}>Детали</Text>
                  <div style={{ background: "rgba(22,119,255,0.04)", border: "1px solid rgba(22,119,255,0.15)", borderRadius: 8, padding: "12px 16px" }}>
                    <Text style={{ fontSize: 13, color: "#374151", lineHeight: 1.6 }}>{r.details}</Text>
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
