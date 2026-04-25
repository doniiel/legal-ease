import { useNavigate } from "react-router-dom";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import { Empty, Table } from "antd";
import {
  FileText, ShieldCheck, User, ArrowRight,
  FolderOpen, Clock, CheckCircle2, PenLine, Plus,
} from "lucide-react";
import { useGetProfileQuery } from "../../../features/profile/api/profile-api";
import { useGetDocumentsQuery } from "../../../features/documents/api/document-api";
import type { DocumentListItem } from "../../../features/documents/api/document-api";
import StatCard from "../../../shared/ui/StatCard";
import { ROUTES } from "../../../app/router/router";
import editorialTableComponents from "../../../shared/ui/table-components";

function StatusPill({ status }: { status: string }) {
  const MAP: Record<string, { label: string; color: string; bg: string }> = {
    DRAFT:     { label: "Черновик", color: "#92400e", bg: "rgba(245,158,11,0.1)"  },
    VALIDATED: { label: "Проверен", color: "#1d4ed8", bg: "rgba(22,119,255,0.08)" },
    COMPLETED: { label: "Завершён", color: "#065f46", bg: "rgba(5,150,105,0.08)"  },
    ARCHIVED:  { label: "Архив",    color: "#6b7280", bg: "rgba(107,114,128,0.08)"},
  };
  const s = MAP[status] ?? { label: status, color: "#6b7280", bg: "#f0f2f7" };
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 5, background: s.bg, color: s.color, borderRadius: 20, padding: "3px 10px", fontSize: 12, fontWeight: 600, whiteSpace: "nowrap" }}>
      <span style={{ width: 5, height: 5, borderRadius: "50%", background: s.color, flexShrink: 0 }} />
      {s.label}
    </span>
  );
}

function getInitials(fio: string) {
  const parts = fio.trim().split(/\s+/);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return fio.slice(0, 2).toUpperCase();
}

export default function DashboardHomePanel() {
  const navigate = useNavigate();
  const isMobile = useIsMobile();
  const { data: profile, isLoading: profileLoading } = useGetProfileQuery();
  const { data: docsPage, isLoading: docsLoading } = useGetDocumentsQuery({ page: 0, size: 1000 });

  const docs = docsPage?.content ?? [];
  const totalDocs = docsPage?.totalElements ?? 0;
  const drafts     = docs.filter(d => d.status === "DRAFT").length;
  const validated  = docs.filter(d => d.status === "VALIDATED").length;
  const completed  = docs.filter(d => d.status === "COMPLETED").length;

  const recentDocs = [...docs]
    .sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime())
    .slice(0, 5);

  const quickCards = [
    { icon: <FileText size={22} />, title: "Мои документы",   desc: "Создавайте и управляйте юридическими документами по шаблонам", route: ROUTES.DOCUMENTS,          show: true },
    { icon: <ShieldCheck size={22} />, title: "Заявка адвоката", desc: "Подайте заявку на получение статуса адвоката в системе",       route: ROUTES.LAWYER_APPLICATION, show: profile?.role === "USER" },
    { icon: <User size={22} />,     title: "Мой профиль",     desc: "Просматривайте и редактируйте личные данные",                    route: ROUTES.PROFILE,            show: true },
  ].filter(c => c.show);

  const columns = [
    {
      title: "Название", dataIndex: "title", key: "title", ellipsis: true,
      render: (v: string) => <span style={{ fontWeight: 500, color: "#1a2744", fontSize: 13 }}>{v}</span>,
    },
    {
      title: "Шаблон", dataIndex: "templateTitle", key: "templateTitle", ellipsis: true,
      render: (v: string) => <span style={{ color: "#8a92a6", fontSize: 13 }}>{v}</span>,
    },
    {
      title: "Статус", dataIndex: "status", key: "status",
      render: (v: string) => <StatusPill status={v} />,
    },
    {
      title: "Дата", dataIndex: "createdDate", key: "createdDate",
      render: (v: string) => <span style={{ color: "#8a92a6", fontSize: 13 }}>{new Date(v).toLocaleDateString("ru-KZ")}</span>,
    },
  ];

  return (
    <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>

      {/* ── Topbar ── */}
      <div style={{ background: "#1a2744", padding: isMobile ? "24px 20px 48px" : "24px 32px 48px", marginLeft: isMobile ? -16 : -32, marginRight: isMobile ? -16 : -32, position: "relative" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: 12 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div style={{ width: 48, height: 48, borderRadius: "50%", background: "rgba(255,255,255,0.12)", border: "1px solid rgba(255,255,255,0.2)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, fontWeight: 700, color: "#fff", flexShrink: 0 }}>
              {profile?.fio ? getInitials(profile.fio) : "U"}
            </div>
            <div>
              <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 12, marginBottom: 2 }}>Добрый день,</div>
              <div style={{ color: "#fff", fontSize: 20, fontWeight: 700, lineHeight: 1.2 }}>
                {profileLoading ? "..." : (profile?.fio ?? "Пользователь")}
              </div>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>

            <button
              onClick={() => navigate(ROUTES.DOCUMENTS)}
              style={{ background: "#fff", color: "#1a2744", border: "none", borderRadius: 8, padding: "10px 18px", fontSize: 13, fontWeight: 600, cursor: "pointer", display: "flex", alignItems: "center", gap: 6, transition: "opacity 0.15s" }}
              onMouseEnter={e => (e.currentTarget.style.opacity = "0.9")}
              onMouseLeave={e => (e.currentTarget.style.opacity = "1")}
            >
              <Plus size={14} /> Новый документ
            </button>
          </div>
        </div>
      </div>

      {/* ── Stat cards ── */}
      <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)", gap: isMobile ? 10 : 14, marginTop: -24, position: "relative", zIndex: 2, marginBottom: 14 }}>
        <StatCard label="Всего документов" value={totalDocs}  icon={<FolderOpen size={16} />}    loading={docsLoading} />
        <StatCard label="Черновики"         value={drafts}     icon={<PenLine size={16} />}       loading={docsLoading} />
        <StatCard label="Проверено"          value={validated}  icon={<Clock size={16} />}         loading={docsLoading} />
        <StatCard label="Завершённые"       value={completed}  icon={<CheckCircle2 size={16} />}  loading={docsLoading} />
      </div>

      {/* ── Quick access ── */}
      <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : `repeat(${quickCards.length}, 1fr)`, gap: isMobile ? 10 : 14, marginBottom: 28 }}>
        {quickCards.map(card => (
          <div
            key={card.route}
            onClick={() => navigate(card.route)}
            style={{ background: "#fff", border: "1px solid #e8eaf0", borderRadius: 14, padding: 24, display: "flex", flexDirection: "column", cursor: "pointer", transition: "box-shadow 0.15s, transform 0.15s" }}
            onMouseEnter={e => { const el = e.currentTarget as HTMLDivElement; el.style.boxShadow = "0 6px 24px rgba(26,39,68,0.09)"; el.style.transform = "translateY(-1px)"; }}
            onMouseLeave={e => { const el = e.currentTarget as HTMLDivElement; el.style.boxShadow = "none"; el.style.transform = "translateY(0)"; }}
          >
            <div style={{ width: 44, height: 44, borderRadius: 10, background: "#f0f2f7", display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 16, color: "#6b7490" }}>
              {card.icon}
            </div>
            <div style={{ fontWeight: 700, fontSize: 15, color: "#1a2744", marginBottom: 6 }}>{card.title}</div>
            <div style={{ fontSize: 13, color: "#8a92a6", lineHeight: 1.5, flex: 1, marginBottom: 20 }}>{card.desc}</div>
            <button
              style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: "#1a2744", color: "#fff", border: "none", borderRadius: 8, padding: "11px 16px", fontSize: 13, fontWeight: 600, fontFamily: "inherit", cursor: "pointer", transition: "opacity 0.15s" }}
              onMouseEnter={e => (e.currentTarget.style.opacity = "0.85")}
              onMouseLeave={e => (e.currentTarget.style.opacity = "1")}
            >
              Перейти <ArrowRight size={14} />
            </button>
          </div>
        ))}
      </div>

      {/* ── Recent documents ── */}
      <div style={{ background: "#fff", border: "1px solid #e8eaf0", borderRadius: 14, overflow: "hidden" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "20px 24px 16px", borderBottom: "1px solid #e8eaf0" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <div style={{ width: 28, height: 28, borderRadius: 6, background: "#f0f2f7", display: "flex", alignItems: "center", justifyContent: "center", color: "#6b7490" }}>
              <FileText size={14} />
            </div>
            <span style={{ fontWeight: 700, fontSize: 15, color: "#1a2744" }}>Последние документы</span>
          </div>
          <button
            onClick={() => navigate(ROUTES.DOCUMENTS)}
            style={{ background: "transparent", border: "none", color: "#8a92a6", fontSize: 12, fontWeight: 500, cursor: "pointer", display: "flex", alignItems: "center", gap: 4, transition: "color 0.15s" }}
            onMouseEnter={e => (e.currentTarget.style.color = "#1a2744")}
            onMouseLeave={e => (e.currentTarget.style.color = "#8a92a6")}
          >
            Смотреть все <ArrowRight size={12} />
          </button>
        </div>

        {recentDocs.length === 0 && !docsLoading ? (
          <div style={{ padding: "48px 24px", textAlign: "center" }}>
            <Empty description={<span style={{ color: "#8a92a6", fontSize: 14 }}>Документы пока не созданы</span>}>
              <button
                onClick={() => navigate(ROUTES.DOCUMENTS)}
                style={{ background: "#1a2744", color: "#fff", border: "none", borderRadius: 8, padding: "9px 20px", fontSize: 13, fontWeight: 600, cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 6, marginTop: 8 }}
              >
                <Plus size={14} /> Создать первый документ
              </button>
            </Empty>
          </div>
        ) : (
          <Table<DocumentListItem>
            columns={columns}
            dataSource={recentDocs}
            rowKey="id"
            loading={docsLoading}
            pagination={false}
            components={editorialTableComponents}
            onRow={record => ({ onClick: () => navigate(`${ROUTES.DOCUMENTS}/${record.id}`) })}
            style={{ cursor: "pointer" }}
          />
        )}
      </div>
    </div>
  );
}
