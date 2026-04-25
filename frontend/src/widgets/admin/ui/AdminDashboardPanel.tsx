import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import {
  Users,
  Scale,
  FileText,
  FileCode,
  ClipboardList,
  TrendingUp,
  Activity,
  CheckCircle2,
  Pencil,
  Trash2,
  ShieldOff,
  ChevronRight,
  Info,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useGetSystemMetricsQuery } from "../../../features/admin/api/admin-metrics-api";
import { useGetAuditLogsQuery, type AuditLog } from "../../../features/admin/api/admin-audit-api";
import { ROUTES } from "../../../app/router/router";
import SharedStatCard from "../../../shared/ui/StatCard";

// ─── Quick access bento card (matching mockup) ────────────────
function QuickCard({
  title,
  desc,
  icon,
  route,
}: {
  title: string;
  desc: string;
  icon: React.ReactNode;
  route: string;
}) {
  const navigate = useNavigate();

  return (
    <div
      style={{
        background: "#eff4ff",
        borderRadius: 12,
        padding: "32px",
        flex: 1,
        display: "flex",
        flexDirection: "column",
        borderBottom: "4px solid transparent",
        transition: "all 0.2s",
        cursor: "pointer",
      }}
      onMouseEnter={(e) => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.background = "#dce9ff";
        el.style.borderBottomColor = "#1a2744";
      }}
      onMouseLeave={(e) => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.background = "#eff4ff";
        el.style.borderBottomColor = "transparent";
      }}
    >
      {/* Icon */}
      <div
        style={{
          width: 56,
          height: 56,
          background: "#fff",
          borderRadius: 12,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          marginBottom: 24,
          boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
        }}
      >
        {icon}
      </div>

      {/* Title */}
      <div
        style={{
          fontSize: 18,
          fontWeight: 700,
          color: "#1a2744",
          marginBottom: 12,
          fontFamily: "Manrope, sans-serif",
        }}
      >
        {title}
      </div>

      {/* Desc */}
      <div
        style={{
          fontSize: 13,
          color: "#444650",
          lineHeight: 1.6,
          marginBottom: 32,
          flex: 1,
        }}
      >
        {desc}
      </div>

      {/* Button */}
      <button
        onClick={() => navigate(route)}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          width: "100%",
          background: "#1a2744",
          color: "#fff",
          padding: "12px 20px",
          borderRadius: 8,
          border: "none",
          fontWeight: 700,
          fontSize: 14,
          cursor: "pointer",
          transition: "opacity 0.15s",
        }}
        onMouseEnter={(e) => (e.currentTarget.style.opacity = "0.9")}
        onMouseLeave={(e) => (e.currentTarget.style.opacity = "1")}
      >
        Перейти
        <ChevronRight size={18} />
      </button>
    </div>
  );
}

// ─── Activity row config ──────────────────────────────────────
function getActivityConfig(action: string): {
  iconBg: string;
  iconColor: string;
  statusBg: string;
  statusColor: string;
  statusText: string;
  icon: React.ReactNode;
} {
  switch (action) {
    case "CREATE":
    case "APPROVE":
      return { iconBg: "#f0fdf4", iconColor: "#16a34a", statusBg: "#dcfce7", statusColor: "#15803d", statusText: action === "APPROVE" ? "Одобрено" : "Создано", icon: <CheckCircle2 size={16} /> };
    case "UPDATE":
      return { iconBg: "#eff6ff", iconColor: "#2563eb", statusBg: "#dbeafe", statusColor: "#1d4ed8", statusText: "Обновлено", icon: <Pencil size={16} /> };
    case "DELETE":
    case "BLOCK":
      return { iconBg: "#fff1f2", iconColor: "#dc2626", statusBg: "#fee2e2", statusColor: "#b91c1c", statusText: action === "BLOCK" ? "Заблокировано" : "Удалено", icon: <Trash2 size={16} /> };
    case "REJECT":
      return { iconBg: "#fffbeb", iconColor: "#d97706", statusBg: "#fef3c7", statusColor: "#b45309", statusText: "Отклонено", icon: <ShieldOff size={16} /> };
    case "UNBLOCK":
      return { iconBg: "#f0fdf4", iconColor: "#16a34a", statusBg: "#dcfce7", statusColor: "#15803d", statusText: "Разблокировано", icon: <CheckCircle2 size={16} /> };
    default:
      return { iconBg: "#f0f9ff", iconColor: "#0284c7", statusBg: "#e0f2fe", statusColor: "#0369a1", statusText: "Завершено", icon: <Info size={16} /> };
  }
}

function formatEventTitle(log: AuditLog): string {
  const entityMap: Record<string, string> = {
    USER: "пользователя",
    TEMPLATE: "шаблона",
    DOCUMENT: "документа",
    CATEGORY: "категории",
    APPLICATION: "заявки",
    RULE: "правила",
    CLAUSE: "клаузы",
  };
  const actionMap: Record<string, string> = {
    CREATE: "Создан",
    UPDATE: "Обновлён",
    DELETE: "Удалён",
    APPROVE: "Одобрена заявка",
    REJECT: "Отклонена заявка",
    BLOCK: "Заблокирован",
    UNBLOCK: "Разблокирован",
  };
  const actionLabel = actionMap[log.action] ?? log.action;
  const entityLabel = entityMap[log.entityType] ?? log.entityType?.toLowerCase() ?? "";
  return `${actionLabel} ${entityLabel}`.trim();
}

// ─── Main Panel ───────────────────────────────────────────────
export default function AdminDashboardPanel() {
  const navigate = useNavigate();
  const isMobile = useIsMobile();
  const { data: metrics, isLoading } = useGetSystemMetricsQuery();
  const { data: auditData } = useGetAuditLogsQuery({ page: 0, size: 5, sort: "createdDate,DESC" });
  const recentLogs = auditData?.content ?? [];

  return (
    <div>
      {/* ── Hero ── */}
      <section style={{ background: "#1a2744", padding: "40px 40px 48px" }}>
        <h1 style={{ fontSize: 40, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
          Дашборд
        </h1>
        <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0, maxWidth: 480 }}>
          Обзор ключевых показателей юридической системы LegalEase. Добро пожаловать в центр управления.
        </p>
      </section>

      {/* ── Stats + Rest of content ── */}
      <div style={{ padding: "0 40px 48px", marginTop: -32, position: "relative", zIndex: 2 }}>

        {/* Row 1: 4 main stats */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)", gap: isMobile ? 12 : 24, marginBottom: isMobile ? 12 : 24 }}>
          <SharedStatCard label="Всего пользователей" value={metrics?.totalUsers} icon={<Users size={22} />} loading={isLoading} />
          <SharedStatCard label="Юристов" value={metrics?.totalLawyers} icon={<Scale size={22} />} loading={isLoading} />
          <SharedStatCard label="Документов" value={metrics?.totalDocuments} icon={<FileText size={22} />} loading={isLoading} />
          <SharedStatCard label="Шаблонов" value={metrics?.totalTemplates} icon={<FileCode size={22} />} loading={isLoading} />
        </div>

        {/* Row 2: 3 action stats */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(1, 1fr)" : "repeat(3, 1fr)", gap: isMobile ? 12 : 24, marginBottom: isMobile ? 24 : 40 }}>
          <SharedStatCard label="Ожидают заявки" value={metrics?.pendingLawyerApplications} icon={<ClipboardList size={22} />} loading={isLoading} />
          <SharedStatCard label="Документов за месяц" value={metrics?.documentsThisMonth} icon={<TrendingUp size={22} />} loading={isLoading} />
          <SharedStatCard label="Активных пользователей" value={metrics?.activeUsers} icon={<Activity size={22} />} loading={isLoading} />
        </div>

        {/* Quick Access Bento */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : "repeat(3, 1fr)", gap: isMobile ? 12 : 24, marginBottom: isMobile ? 24 : 48 }}>
          <QuickCard
            title="Управление заявками"
            desc="Просмотр и модерация новых запросов от пользователей. Требует внимания."
            icon={<ClipboardList size={28} color="#1a2744" />}
            route={ROUTES.ADMIN_APPLICATIONS}
          />
          <QuickCard
            title="Пользователи"
            desc="Администрирование профилей, ролей и прав доступа для юристов и клиентов системы."
            icon={<Users size={28} color="#1a2744" />}
            route={ROUTES.ADMIN_USERS}
          />
          <QuickCard
            title="Категории"
            desc="Настройка структуры разделов права и классификация юридических документов."
            icon={<FileCode size={28} color="#1a2744" />}
            route={ROUTES.ADMIN_CATEGORIES}
          />
        </div>

        {/* Recent Activity */}
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
            <h2
              style={{
                fontSize: 24,
                fontWeight: 700,
                color: "#1a2744",
                margin: 0,
                fontFamily: "Manrope, sans-serif",
                letterSpacing: "-0.01em",
              }}
            >
              Последняя активность
            </h2>
            <button
              onClick={() => navigate(ROUTES.ADMIN_AUDIT)}
              style={{
                background: "none",
                border: "none",
                color: "#1a2744",
                fontWeight: 700,
                fontSize: 13,
                cursor: "pointer",
                padding: 0,
              }}
              onMouseEnter={(e) => (e.currentTarget.style.textDecoration = "underline")}
              onMouseLeave={(e) => (e.currentTarget.style.textDecoration = "none")}
            >
              Смотреть все логи
            </button>
          </div>

          <div
            style={{
              background: "#fff",
              borderRadius: 16,
              boxShadow: "0 8px 32px rgba(11,28,48,0.04)",
              overflow: "hidden",
            }}
          >
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr style={{ background: "#f8fafc", borderBottom: "1px solid #f1f5f9" }}>
                  {["Событие", "Пользователь", "Дата и время", "Статус"].map((h) => (
                    <th
                      key={h}
                      style={{
                        padding: "16px 32px",
                        textAlign: "left",
                        fontSize: 11,
                        fontWeight: 700,
                        textTransform: "uppercase",
                        letterSpacing: "0.08em",
                        color: "#94a3b8",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {recentLogs.length === 0 && (
                  <tr>
                    <td colSpan={4} style={{ padding: "32px", textAlign: "center", color: "#94a3b8", fontSize: 14 }}>
                      Нет данных об активности
                    </td>
                  </tr>
                )}
                {recentLogs.map((log, i) => {
                  const cfg = getActivityConfig(log.action);
                  const isLast = i === recentLogs.length - 1;
                  return (
                    <tr
                      key={log.id}
                      style={{
                        borderBottom: isLast ? "none" : "1px solid #f8fafc",
                        transition: "background 0.15s",
                      }}
                      onMouseEnter={(e) => (e.currentTarget.style.background = "rgba(239,246,255,0.4)")}
                      onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
                    >
                      {/* Event */}
                      <td style={{ padding: "20px 32px" }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                          <div
                            style={{
                              width: 36,
                              height: 36,
                              background: cfg.iconBg,
                              color: cfg.iconColor,
                              borderRadius: 8,
                              display: "flex",
                              alignItems: "center",
                              justifyContent: "center",
                              flexShrink: 0,
                            }}
                          >
                            {cfg.icon}
                          </div>
                          <span style={{ fontSize: 14, fontWeight: 500, color: "#1e293b" }}>
                            {formatEventTitle(log)}
                          </span>
                        </div>
                      </td>

                      {/* User */}
                      <td style={{ padding: "20px 32px", fontSize: 13, color: "#475569" }}>
                        {log.userEmail || "—"}
                      </td>

                      {/* Date */}
                      <td style={{ padding: "20px 32px", fontSize: 13, color: "#94a3b8" }}>
                        {new Date(log.createdAt).toLocaleString("ru-RU", {
                          hour: "2-digit",
                          minute: "2-digit",
                          day: "2-digit",
                          month: "short",
                          year: "numeric",
                        })}
                      </td>

                      {/* Status */}
                      <td style={{ padding: "20px 32px" }}>
                        <span
                          style={{
                            background: cfg.statusBg,
                            color: cfg.statusColor,
                            padding: "4px 12px",
                            borderRadius: 20,
                            fontSize: 11,
                            fontWeight: 700,
                            textTransform: "uppercase",
                            letterSpacing: "0.06em",
                          }}
                        >
                          {cfg.statusText}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
