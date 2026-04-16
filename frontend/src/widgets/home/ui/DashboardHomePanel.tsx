import { useNavigate } from "react-router-dom";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import { Empty, Table } from "antd";
import {
  FileText,
  ShieldCheck,
  User,
  ArrowRight,
  FolderOpen,
  Clock,
  CheckCircle2,
  PenLine,
  Plus,
} from "lucide-react";
import { useGetProfileQuery } from "../../../features/profile/api/profile-api";
import { useGetDocumentsQuery } from "../../../features/documents/api/document-api";
import type { DocumentListItem } from "../../../features/documents/api/document-api";
import StatCard from "../../../shared/ui/StatCard";
import { ROUTES } from "../../../app/router/router";
import editorialTableComponents from "../../../shared/ui/table-components";

function StatusPill({ status }: { status: string }) {
  const MAP: Record<string, { label: string; dot: string; bg: string; color: string }> = {
    DRAFT:      { label: "Черновик",    dot: "#f59e0b", bg: "#fffbeb", color: "#92400e" },
    PROCESSING: { label: "В обработке", dot: "#1677ff", bg: "#eff6ff", color: "#1d4ed8" },
    COMPLETED:  { label: "Завершён",    dot: "#059669", bg: "#f0fdf4", color: "#065f46" },
  };
  const s = MAP[status] ?? { label: status, dot: "#9ca3af", bg: "#f3f4f6", color: "#374151" };
  return (
    <span
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 6,
        background: s.bg,
        color: s.color,
        borderRadius: 20,
        padding: "3px 10px",
        fontSize: 12,
        fontWeight: 600,
        whiteSpace: "nowrap",
      }}
    >
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: s.dot, flexShrink: 0 }} />
      {s.label}
    </span>
  );
}

const ROLE_LABEL: Record<string, string> = {
  USER: "Пользователь",
  LAWYER: "Адвокат",
  ADMIN: "Администратор",
};

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
  const drafts = docs.filter((d) => d.status === "DRAFT").length;
  const processing = docs.filter((d) => d.status === "PROCESSING").length;
  const completed = docs.filter((d) => d.status === "COMPLETED").length;

  const recentDocs = [...docs]
    .sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime())
    .slice(0, 5);

  const today = new Date().toLocaleDateString("ru-KZ", { day: "numeric", month: "long", year: "numeric" });

  const quickCards = [
    {
      icon: <FileText size={22} color="#0F2A44" />,
      iconBg: "#e0e7ff",
      title: "Мои документы",
      desc: "Создавайте и управляйте юридическими документами по шаблонам",
      route: ROUTES.DOCUMENTS,
      show: true,
    },
    {
      icon: <ShieldCheck size={22} color="#059669" />,
      iconBg: "#d1fae5",
      title: "Заявка адвоката",
      desc: "Подайте заявку на получение статуса адвоката в системе",
      route: ROUTES.LAWYER_APPLICATION,
      show: profile?.role === "USER",
    },
    {
      icon: <User size={22} color="#7c3aed" />,
      iconBg: "#ede9fe",
      title: "Мой профиль",
      desc: "Просматривайте и редактируйте личные данные",
      route: ROUTES.PROFILE,
      show: true,
    },
  ].filter((c) => c.show);

  const columns = [
    {
      title: "Название",
      dataIndex: "title",
      key: "title",
      ellipsis: true,
      render: (v: string) => <span style={{ fontWeight: 500, color: "#111827" }}>{v}</span>,
    },
    {
      title: "Шаблон",
      dataIndex: "templateTitle",
      key: "templateTitle",
      ellipsis: true,
      render: (v: string) => <span style={{ color: "#6b7280", fontSize: 13 }}>{v}</span>,
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      render: (v: string) => <StatusPill status={v} />,
    },
    {
      title: "Дата",
      dataIndex: "createdDate",
      key: "createdDate",
      render: (v: string) => (
        <span style={{ color: "#6b7280", fontSize: 13 }}>
          {new Date(v).toLocaleDateString("ru-KZ")}
        </span>
      ),
    },
  ];

  return (
    <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>
      {/* ── Full-bleed hero ── */}
      <div
        style={{
          background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
          padding: isMobile ? "24px 20px 48px" : "40px 40px 56px",
          marginLeft: isMobile ? -16 : -32,
          marginRight: isMobile ? -16 : -32,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 20,
          flexWrap: "wrap",
        }}
      >
        {/* Left: avatar + greeting */}
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: "50%",
              background: "rgba(255,255,255,0.2)",
              border: "2px solid rgba(255,255,255,0.35)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 22,
              fontWeight: 800,
              color: "#fff",
              flexShrink: 0,
              letterSpacing: 1,
            }}
          >
            {profile?.fio ? getInitials(profile.fio) : "U"}
          </div>
          <div>
            <div style={{ color: "rgba(255,255,255,0.7)", fontSize: 13, marginBottom: 4 }}>
              Добрый день,
            </div>
            <div style={{ color: "#fff", fontSize: 22, fontWeight: 800, lineHeight: 1.2, marginBottom: 4 }}>
              {profileLoading ? "..." : (profile?.fio ?? "Пользователь")}
            </div>
            <div style={{ color: "rgba(255,255,255,0.6)", fontSize: 13 }}>
              Добро пожаловать в LegalEase
            </div>
          </div>
        </div>

        {/* Right: pills + action */}
        <div style={{ display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
          <div
            style={{
              background: "rgba(255,255,255,0.15)",
              border: "1px solid rgba(255,255,255,0.25)",
              borderRadius: 20,
              padding: "6px 14px",
              color: "#fff",
              fontSize: 12,
              fontWeight: 600,
            }}
          >
            {ROLE_LABEL[profile?.role ?? "USER"] ?? "Пользователь"}
          </div>
          <div
            style={{
              background: "rgba(255,255,255,0.15)",
              border: "1px solid rgba(255,255,255,0.25)",
              borderRadius: 20,
              padding: "6px 14px",
              color: "rgba(255,255,255,0.85)",
              fontSize: 12,
            }}
          >
            {today}
          </div>
          <button
            onClick={() => navigate(ROUTES.DOCUMENTS)}
            style={{
              background: "#fff",
              color: "#0F2A44",
              border: "none",
              borderRadius: 20,
              padding: "8px 18px",
              fontSize: 13,
              fontWeight: 700,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              gap: 6,
            }}
          >
            <Plus size={14} />
            Создать документ
          </button>
        </div>
      </div>

      {/* ── Stat cards (overlap hero) ── */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)",
          gap: isMobile ? 10 : 16,
          marginTop: -28,
          position: "relative",
          zIndex: 2,
          marginBottom: 28,
        }}
      >
        <StatCard label="Всего документов" value={totalDocs} color="#0F2A44" icon={<FolderOpen size={18} />} loading={docsLoading} />
        <StatCard label="Черновики" value={drafts} color="#f59e0b" icon={<PenLine size={18} />} loading={docsLoading} />
        <StatCard label="В обработке" value={processing} color="#1677ff" icon={<Clock size={18} />} loading={docsLoading} />
        <StatCard label="Завершённые" value={completed} color="#059669" icon={<CheckCircle2 size={18} />} loading={docsLoading} />
      </div>

      {/* ── Quick access cards ── */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: isMobile ? "1fr" : `repeat(${quickCards.length}, 1fr)`,
          gap: isMobile ? 10 : 16,
          marginBottom: 28,
        }}
      >
        {quickCards.map((card) => (
          <div
            key={card.route}
            style={{
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 14,
              padding: "24px 24px 20px",
              display: "flex",
              flexDirection: "column",
              gap: 12,
              transition: "box-shadow 0.2s, transform 0.15s",
              cursor: "pointer",
            }}
            onClick={() => navigate(card.route)}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLDivElement).style.boxShadow = "0 8px 24px rgba(15,42,68,0.12)";
              (e.currentTarget as HTMLDivElement).style.transform = "translateY(-2px)";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLDivElement).style.boxShadow = "none";
              (e.currentTarget as HTMLDivElement).style.transform = "translateY(0)";
            }}
          >
            <div
              style={{
                width: 48,
                height: 48,
                borderRadius: 12,
                background: card.iconBg,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              {card.icon}
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 15, color: "#111827", marginBottom: 4 }}>
                {card.title}
              </div>
              <div style={{ fontSize: 13, color: "#6b7280", lineHeight: 1.5 }}>
                {card.desc}
              </div>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 4, color: "#0F2A44", fontSize: 13, fontWeight: 600, marginTop: 4 }}>
              Перейти <ArrowRight size={14} />
            </div>
          </div>
        ))}
      </div>

      {/* ── Recent documents ── */}
      <div
        style={{
          background: "#fff",
          border: "1px solid #e5e7eb",
          borderRadius: 14,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "20px 24px 16px",
            borderBottom: "1px solid #f0f0f0",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <FileText size={16} color="#0F2A44" />
            <span style={{ fontWeight: 700, fontSize: 15, color: "#111827" }}>Последние документы</span>
          </div>
          <button
            onClick={() => navigate(ROUTES.DOCUMENTS)}
            style={{
              background: "transparent",
              border: "none",
              color: "#0F2A44",
              fontSize: 13,
              fontWeight: 600,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              gap: 4,
              padding: "4px 8px",
              borderRadius: 6,
            }}
          >
            Все документы <ArrowRight size={13} />
          </button>
        </div>

        {recentDocs.length === 0 && !docsLoading ? (
          <div style={{ padding: "48px 24px", textAlign: "center" }}>
            <Empty
              description={<span style={{ color: "#6b7280", fontSize: 14 }}>Документы пока не созданы</span>}
            >
              <button
                onClick={() => navigate(ROUTES.DOCUMENTS)}
                style={{
                  background: "#0F2A44",
                  color: "#fff",
                  border: "none",
                  borderRadius: 8,
                  padding: "8px 20px",
                  fontSize: 13,
                  fontWeight: 600,
                  cursor: "pointer",
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 6,
                  marginTop: 8,
                }}
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
            onRow={(record) => ({ onClick: () => navigate(`${ROUTES.DOCUMENTS}/${record.id}`) })}
            style={{ cursor: "pointer" }}
          />
        )}
      </div>
    </div>
  );
}
