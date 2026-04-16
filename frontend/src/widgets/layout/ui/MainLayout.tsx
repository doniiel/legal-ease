import { type ReactNode, useState } from "react";
import { Layout, Avatar, Dropdown, type MenuProps } from "antd";
import { useNavigate, useLocation, type NavigateFunction } from "react-router-dom";
import {
  Home,
  FileText,
  ShieldCheck,
  ClipboardList,
  Users,
  FolderOpen,
  FileCode,
  ShieldAlert,
  PanelLeftClose,
  PanelLeftOpen,
  LayoutDashboard,
  ScrollText,
  Scale,
  BookOpen,
  Sparkles,
  Menu,
  X,
} from "lucide-react";
import { ROUTES } from "../../../app/router/router";
import { useAuth } from "../../../features/auth/model/use-auth";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";

const { Sider, Content, Header } = Layout;

const NAV_ITEMS = [
  { key: ROUTES.HOME,                icon: <Home size={18} />,           label: "Главная",          roles: ["USER"] },
  { key: ROUTES.DOCUMENTS,           icon: <FileText size={18} />,       label: "Документы",        roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.LAWYER_APPLICATION,  icon: <ShieldCheck size={18} />,    label: "Заявка адвоката",  roles: ["USER"] },
  { key: ROUTES.ADMIN_DASHBOARD,     icon: <LayoutDashboard size={18} />,label: "Дашборд",          roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_APPLICATIONS,  icon: <ClipboardList size={18} />,  label: "Заявки",           roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_USERS,         icon: <Users size={18} />,          label: "Пользователи",     roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_CATEGORIES,    icon: <FolderOpen size={18} />,     label: "Категории",        roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_RULES,         icon: <ShieldCheck size={18} />,    label: "Правила",          roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_AUDIT,         icon: <ScrollText size={18} />,     label: "Аудит логи",       roles: ["ADMIN"] },
  { key: ROUTES.LAWYER_TEMPLATES,      icon: <FileCode size={18} />,       label: "Мои шаблоны",      roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_MATCHING_RULES, icon: <Scale size={18} />,          label: "Матчинг",          roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_RULE_MANAGER,   icon: <ShieldAlert size={18} />,    label: "Rule Manager",     roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_DOCUMENTS,      icon: <FileText size={18} />,       label: "Документы клиентов", roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_CLAUSES,        icon: <BookOpen size={18} />,       label: "Библиотека клауз", roles: ["LAWYER"] },
  { key: ROUTES.MATCHING,              icon: <Sparkles size={18} />,       label: "Подбор шаблона",   roles: ["USER", "LAWYER", "ADMIN"] },
];

const getDropdownItems = (navigate: NavigateFunction, logout: () => void): MenuProps["items"] => [
  { key: "profile",   label: "Профиль",   onClick: () => navigate(ROUTES.PROFILE) },
  { key: "settings",  label: "Настройки", onClick: () => navigate(ROUTES.SETTINGS) },
  { type: "divider" },
  { key: "logout",    label: "Выйти",     danger: true, onClick: logout },
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

// ─── Main Layout ──────────────────────────────────────────────
interface MainLayoutProps {
  children: ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { role, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const isMobile = useIsMobile();

  const visibleNavItems = NAV_ITEMS.filter((item) =>
    role ? item.roles.includes(role) : false
  );

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const dropdownItems = getDropdownItems(navigate, handleLogout);

  const isFullBleed =
    pathname.startsWith(ROUTES.DOCUMENTS) ||
    [
      ROUTES.HOME,
      ROUTES.LAWYER_APPLICATION,
      ROUTES.SETTINGS,
      ROUTES.ADMIN_DASHBOARD, ROUTES.ADMIN_USERS, ROUTES.ADMIN_RULES,
      ROUTES.ADMIN_CATEGORIES, ROUTES.ADMIN_APPLICATIONS, ROUTES.ADMIN_AUDIT,
      ROUTES.LAWYER_TEMPLATES, ROUTES.LAWYER_MATCHING_RULES,
      ROUTES.LAWYER_RULE_MANAGER, ROUTES.LAWYER_DOCUMENTS,
      ROUTES.LAWYER_CLAUSES, ROUTES.MATCHING,
    ].includes(pathname);

  const siderWidth = 260;
  const collapsedWidth = 72;

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* ── Mobile backdrop ── */}
      {isMobile && mobileOpen && (
        <div
          onClick={() => setMobileOpen(false)}
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0,0,0,0.5)",
            zIndex: 150,
            backdropFilter: "blur(2px)",
          }}
        />
      )}

      {/* ── Sidebar ── */}
      <Sider
        collapsed={isMobile ? false : collapsed}
        width={siderWidth}
        collapsedWidth={collapsedWidth}
        trigger={null}
        style={{
          background: "#f8fafc",
          borderRight: "1px solid #e2e8f0",
          position: "fixed",
          height: "100vh",
          overflow: "hidden",
          top: 0,
          bottom: 0,
          zIndex: 200,
          display: "flex",
          flexDirection: "column",
          left: isMobile ? (mobileOpen ? 0 : -siderWidth) : 0,
          transition: "left 0.3s ease",
          boxShadow: isMobile && mobileOpen ? "4px 0 24px rgba(0,0,0,0.15)" : "none",
        }}
      >
        {/* Logo + close button on mobile */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 12,
            padding: "28px 20px 24px",
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
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 16, fontWeight: 800, color: "#0F2A44", lineHeight: 1.2, fontFamily: "Manrope, sans-serif" }}>
              LegalEase
            </div>
            <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#7790bd" }}>
              {ROLE_LABELS[role ?? ""] ?? "Portal"}
            </div>
          </div>
          {isMobile && (
            <button
              onClick={() => setMobileOpen(false)}
              style={{ background: "transparent", border: "none", cursor: "pointer", color: "#64748b", display: "flex", padding: 4 }}
            >
              <X size={20} />
            </button>
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
              collapsed={!isMobile && collapsed}
              onClick={() => {
                navigate(item.key);
                if (isMobile) setMobileOpen(false);
              }}
            />
          ))}
        </nav>
      </Sider>

      {/* ── Main area ── */}
      <Layout
        style={{
          marginLeft: isMobile ? 0 : (collapsed ? collapsedWidth : siderWidth),
          transition: "margin-left 0.2s",
        }}
      >
        {/* Header */}
        <Header
          style={{
            background: "#fff",
            padding: isMobile ? "0 16px" : "0 32px",
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
          {/* Left: toggle */}
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <button
              onClick={() => isMobile ? setMobileOpen(true) : setCollapsed(!collapsed)}
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
              {isMobile
                ? <Menu size={18} />
                : collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />
              }
            </button>
          </div>

          {/* Right: user */}
          <div style={{ display: "flex", alignItems: "center", gap: 2 }}>
            <div style={{ width: 1, height: 32, background: "#e2e8f0", margin: "0 12px" }} />
            <Dropdown trigger={["click"]} menu={{ items: dropdownItems }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10, cursor: "pointer" }}>
                <Avatar
                  size={34}
                  style={{ background: "#0F2A44", fontWeight: 700, borderRadius: 8, fontSize: 13 }}
                >
                  {role?.[0] ?? "U"}
                </Avatar>
              </div>
            </Dropdown>
          </div>
        </Header>

        {/* Content */}
        <Content
          style={{
            padding: isFullBleed ? 0 : (isMobile ? 16 : 32),
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
