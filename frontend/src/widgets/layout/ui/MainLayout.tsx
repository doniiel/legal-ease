import { type ReactNode, useState } from "react";
import { Layout, Avatar } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  Home,
  FileText,
  User,
  Settings,
  ShieldCheck,
  ClipboardList,
  Users,
  FolderOpen,
  FileCode,
  ShieldAlert,
  LogOut,
  PanelLeftClose,
  PanelLeftOpen,
  LayoutDashboard,
  ScrollText,
  Scale,
  Bell,
  HelpCircle,
} from "lucide-react";
import { ROUTES } from "../../../app/router/router";
import { useAuth } from "../../../features/auth/model/use-auth";

const { Sider, Content, Header } = Layout;

const NAV_ITEMS = [
  { key: ROUTES.HOME,                icon: <Home size={18} />,           label: "Главная",          roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.DOCUMENTS,           icon: <FileText size={18} />,       label: "Документы",        roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.PROFILE,             icon: <User size={18} />,           label: "Профиль",          roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.LAWYER_APPLICATION,  icon: <ShieldCheck size={18} />,    label: "Заявка адвоката",  roles: ["USER"] },
  { key: ROUTES.SETTINGS,            icon: <Settings size={18} />,       label: "Настройки",        roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.ADMIN_DASHBOARD,     icon: <LayoutDashboard size={18} />,label: "Дашборд",          roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_APPLICATIONS,  icon: <ClipboardList size={18} />,  label: "Заявки",           roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_USERS,         icon: <Users size={18} />,          label: "Пользователи",     roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_CATEGORIES,    icon: <FolderOpen size={18} />,     label: "Категории",        roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_RULES,         icon: <ShieldCheck size={18} />,    label: "Правила",          roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_AUDIT,         icon: <ScrollText size={18} />,     label: "Аудит логи",       roles: ["ADMIN"] },
  { key: ROUTES.LAWYER_TEMPLATES,    icon: <FileCode size={18} />,       label: "Мои шаблоны",      roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_RULE_MANAGER, icon: <ShieldAlert size={18} />,    label: "Rule Manager",     roles: ["LAWYER"] },
];

const ROLE_LABELS: Record<string, string> = {
  ADMIN: "Super Admin",
  LAWYER: "Lawyer",
  USER: "User",
};

// ─── Nav Item ─────────────────────────────────────────────────
function NavItem({
  icon, label, isActive, collapsed, onClick, danger = false,
}: {
  icon: React.ReactNode;
  label: string;
  isActive: boolean;
  collapsed: boolean;
  onClick: () => void;
  danger?: boolean;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "10px 14px",
        borderRadius: 8,
        marginBottom: 2,
        cursor: "pointer",
        background: isActive ? "#0F2A44" : "transparent",
        color: isActive ? "#fff" : danger ? "#ef4444" : "#64748b",
        fontWeight: isActive ? 600 : 500,
        fontSize: 14,
        transition: "all 0.15s",
        boxShadow: isActive ? "0 4px 12px rgba(15,42,68,0.25)" : "none",
        whiteSpace: "nowrap",
        overflow: "hidden",
      }}
      onMouseEnter={(e) => {
        if (!isActive) {
          const el = e.currentTarget as HTMLDivElement;
          el.style.background = danger ? "#fff1f2" : "#eff6ff";
          el.style.color = danger ? "#ef4444" : "#0F2A44";
        }
      }}
      onMouseLeave={(e) => {
        if (!isActive) {
          const el = e.currentTarget as HTMLDivElement;
          el.style.background = "transparent";
          el.style.color = danger ? "#ef4444" : "#64748b";
        }
      }}
    >
      <span style={{ flexShrink: 0, display: "flex" }}>{icon}</span>
      {!collapsed && <span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{label}</span>}
    </div>
  );
}

// ─── Icon Button (header) ─────────────────────────────────────
function IconBtn({ icon, onClick }: { icon: React.ReactNode; onClick?: () => void }) {
  return (
    <button
      onClick={onClick}
      style={{
        width: 36,
        height: 36,
        borderRadius: "50%",
        border: "none",
        background: "transparent",
        cursor: "pointer",
        color: "#64748b",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        transition: "background 0.2s",
      }}
      onMouseEnter={(e) => (e.currentTarget.style.background = "#f1f5f9")}
      onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
    >
      {icon}
    </button>
  );
}

// ─── Main Layout ──────────────────────────────────────────────
interface MainLayoutProps {
  children: ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { role, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  const visibleNavItems = NAV_ITEMS.filter((item) =>
    role ? item.roles.includes(role) : false
  );

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  // Dashboard gets zero padding — hero section goes edge-to-edge
  const isFullBleed = [ROUTES.ADMIN_DASHBOARD, ROUTES.ADMIN_USERS, ROUTES.ADMIN_RULES, ROUTES.ADMIN_CATEGORIES, ROUTES.ADMIN_APPLICATIONS, ROUTES.ADMIN_AUDIT].includes(pathname);

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* ── Sidebar ── */}
      <Sider
        collapsed={collapsed}
        width={260}
        collapsedWidth={72}
        trigger={null}
        style={{
          background: "#f8fafc",
          borderRight: "1px solid #e2e8f0",
          position: "fixed",
          height: "100vh",
          overflow: "hidden",
          left: 0,
          top: 0,
          bottom: 0,
          zIndex: 100,
          display: "flex",
          flexDirection: "column",
        }}
      >
        {/* Logo */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 12,
            padding: collapsed ? "28px 16px 24px" : "28px 20px 24px",
            borderBottom: "1px solid #e2e8f0",
            marginBottom: 8,
          }}
        >
          <div
            style={{
              width: 40,
              height: 40,
              background: "#0F2A44",
              borderRadius: 12,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
              boxShadow: "0 4px 12px rgba(15,42,68,0.3)",
            }}
          >
            <Scale size={20} color="#fff" />
          </div>
          {!collapsed && (
            <div>
              <div
                style={{
                  fontSize: 16,
                  fontWeight: 800,
                  color: "#0F2A44",
                  lineHeight: 1.2,
                  fontFamily: "Manrope, sans-serif",
                }}
              >
                LegalEase
              </div>
              <div
                style={{
                  fontSize: 10,
                  fontWeight: 700,
                  textTransform: "uppercase",
                  letterSpacing: "0.08em",
                  color: "#7790bd",
                }}
              >
                {ROLE_LABELS[role ?? ""] ?? "Portal"}
              </div>
            </div>
          )}
        </div>

        {/* Nav items */}
        <nav style={{ padding: "8px 12px", flex: 1, overflowY: "auto", overflowX: "hidden" }}>
          {visibleNavItems.map((item) => (
            <NavItem
              key={item.key}
              icon={item.icon}
              label={item.label}
              isActive={pathname === item.key}
              collapsed={collapsed}
              onClick={() => navigate(item.key)}
            />
          ))}
        </nav>

        {/* Bottom: logout */}
        <div style={{ padding: "12px 12px 24px" }}>
          <NavItem
            icon={<LogOut size={18} />}
            label="Выйти"
            isActive={false}
            collapsed={collapsed}
            onClick={handleLogout}
            danger
          />
        </div>
      </Sider>

      {/* ── Main area ── */}
      <Layout
        style={{
          marginLeft: collapsed ? 72 : 260,
          transition: "margin-left 0.2s",
        }}
      >
        {/* Header */}
        <Header
          style={{
            background: "#fff",
            padding: "0 32px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            borderBottom: "1px solid #f0f0f0",
            height: 64,
            position: "sticky",
            top: 0,
            zIndex: 99,
            boxShadow: "0 1px 4px rgba(0,0,0,0.04)",
          }}
        >
          {/* Left: toggle + search */}
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <button
              onClick={() => setCollapsed(!collapsed)}
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                width: 36,
                height: 36,
                borderRadius: 8,
                border: "1px solid #e5e7eb",
                background: "transparent",
                cursor: "pointer",
                color: "#0F2A44",
                transition: "background 0.2s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = "#f5f7fa")}
              onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
            >
              {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
            </button>
          </div>

          {/* Right: icons + user */}
          <div style={{ display: "flex", alignItems: "center", gap: 2 }}>
            <IconBtn icon={<Bell size={18} />} />
            <IconBtn icon={<HelpCircle size={18} />} />
            <IconBtn icon={<Settings size={18} />} onClick={() => navigate(ROUTES.SETTINGS)} />

            <div style={{ width: 1, height: 32, background: "#e2e8f0", margin: "0 12px" }} />

            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 10,
                cursor: "pointer",
              }}
              onClick={() => navigate(ROUTES.PROFILE)}
            >
             
              <Avatar
                size={34}
                style={{
                  background: "#0F2A44",
                  fontWeight: 700,
                  borderRadius: 8,
                  fontSize: 13,
                }}
              >
                {role?.[0] ?? "U"}
              </Avatar>
            </div>
          </div>
        </Header>

        {/* Content */}
        <Content
          style={{
            padding: isFullBleed ? 0 : 32,
            background: "#f8f9ff",
            minHeight: "calc(100vh - 64px)",
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
