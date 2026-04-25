import { type ReactNode, useState } from "react";
import { Layout, Avatar, Dropdown, type MenuProps } from "antd";
import { useNavigate, useLocation, type NavigateFunction } from "react-router-dom";
import {
  Home, FileText, ShieldCheck, ClipboardList, Users, FolderOpen,
  FileCode, ShieldAlert, LayoutDashboard, ScrollText, Scale,
  BookOpen, Sparkles, Menu, X, PanelLeftClose, PanelLeftOpen,
} from "lucide-react";
import { ROUTES } from "../../../app/router/router";
import { useAuth } from "../../../features/auth/model/use-auth";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import { useGetProfileQuery } from "../../../features/profile/api/profile-api";

const { Sider, Content, Header } = Layout;

const NAV_ITEMS = [
  { key: ROUTES.HOME,                  icon: <Home size={16} />,            label: "Главная",            roles: ["USER"] },
  { key: ROUTES.DOCUMENTS,             icon: <FileText size={16} />,        label: "Документы",          roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.LAWYER_APPLICATION,    icon: <ShieldCheck size={16} />,     label: "Заявка адвоката",    roles: ["USER"] },
  { key: ROUTES.ADMIN_DASHBOARD,       icon: <LayoutDashboard size={16} />, label: "Дашборд",            roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_APPLICATIONS,    icon: <ClipboardList size={16} />,   label: "Заявки",             roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_USERS,           icon: <Users size={16} />,           label: "Пользователи",       roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_CATEGORIES,      icon: <FolderOpen size={16} />,      label: "Категории",          roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_RULES,           icon: <ShieldCheck size={16} />,     label: "Правила",            roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_AUDIT,           icon: <ScrollText size={16} />,      label: "Аудит логи",         roles: ["ADMIN"] },
  { key: ROUTES.LAWYER_TEMPLATES,      icon: <FileCode size={16} />,        label: "Мои шаблоны",        roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_MATCHING_RULES, icon: <Scale size={16} />,           label: "Матчинг",            roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_RULE_MANAGER,   icon: <ShieldAlert size={16} />,     label: "Rule Manager",       roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_DOCUMENTS,      icon: <FileText size={16} />,        label: "Документы клиентов", roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_CLAUSES,        icon: <BookOpen size={16} />,        label: "Библиотека клауз",   roles: ["LAWYER"] },
  { key: ROUTES.MATCHING,              icon: <Sparkles size={16} />,        label: "Подбор шаблона",     roles: ["USER", "LAWYER", "ADMIN"] },
];

const getDropdownItems = (navigate: NavigateFunction, logout: () => void): MenuProps["items"] => [
  { key: "profile",  label: "Профиль",   onClick: () => navigate(ROUTES.PROFILE) },
  { key: "settings", label: "Настройки", onClick: () => navigate(ROUTES.SETTINGS) },
  { type: "divider" },
  { key: "logout",   label: "Выйти",     danger: true, onClick: logout },
];


// ─── Nav Item ─────────────────────────────────────────────────
function NavItem({
  icon, label, isActive, collapsed, onClick,
}: {
  icon: React.ReactNode;
  label: string;
  isActive: boolean;
  collapsed: boolean;
  onClick: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: 10,
        padding: "10px 12px",
        borderRadius: 8,
        marginBottom: 2,
        cursor: "pointer",
        background: isActive ? "rgba(255,255,255,0.12)" : "transparent",
        color: isActive ? "#fff" : "rgba(255,255,255,0.55)",
        fontWeight: isActive ? 600 : 500,
        fontSize: 13.5,
        transition: "all 0.2s",
        whiteSpace: "nowrap",
        overflow: "hidden",
      }}
      onMouseEnter={e => {
        if (!isActive) {
          const el = e.currentTarget as HTMLDivElement;
          el.style.background = "rgba(255,255,255,0.06)";
          el.style.color = "#fff";
        }
      }}
      onMouseLeave={e => {
        if (!isActive) {
          const el = e.currentTarget as HTMLDivElement;
          el.style.background = "transparent";
          el.style.color = "rgba(255,255,255,0.55)";
        }
      }}
    >
      <span style={{ flexShrink: 0, display: "flex", opacity: 0.8 }}>{icon}</span>
      {!collapsed && <span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{label}</span>}
    </div>
  );
}

// ─── Main Layout ──────────────────────────────────────────────
interface MainLayoutProps { children: ReactNode }

export default function MainLayout({ children }: MainLayoutProps) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { role, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const isMobile = useIsMobile();
  const { data: profile } = useGetProfileQuery();

  const avatarLetter = profile?.fio
    ? profile.fio.trim()[0].toUpperCase()
    : (role?.[0]?.toUpperCase() ?? "U");

  const visibleNavItems = NAV_ITEMS.filter(item => role ? item.roles.includes(role) : false);

  const handleLogout = () => { logout(); navigate(ROUTES.LOGIN); };
  const dropdownItems = getDropdownItems(navigate, handleLogout);

  const isFullBleed =
    pathname.startsWith(ROUTES.DOCUMENTS) ||
    [
      ROUTES.HOME, ROUTES.LAWYER_APPLICATION, ROUTES.SETTINGS,
      ROUTES.ADMIN_DASHBOARD, ROUTES.ADMIN_USERS, ROUTES.ADMIN_RULES,
      ROUTES.ADMIN_CATEGORIES, ROUTES.ADMIN_APPLICATIONS, ROUTES.ADMIN_AUDIT,
      ROUTES.LAWYER_TEMPLATES, ROUTES.LAWYER_MATCHING_RULES,
      ROUTES.LAWYER_RULE_MANAGER, ROUTES.LAWYER_DOCUMENTS,
      ROUTES.LAWYER_CLAUSES, ROUTES.MATCHING,
    ].includes(pathname);

  const siderWidth = 220;
  const collapsedWidth = 64;

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* ── Mobile backdrop ── */}
      {isMobile && mobileOpen && (
        <div onClick={() => setMobileOpen(false)} style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", zIndex: 150, backdropFilter: "blur(2px)" }} />
      )}

      {/* ── Sidebar ── */}
      <Sider
        collapsed={isMobile ? false : collapsed}
        width={siderWidth}
        collapsedWidth={collapsedWidth}
        trigger={null}
        style={{
          background: "#1a2744",
          position: "fixed",
          height: "100vh",
          overflow: "hidden",
          top: 0, bottom: 0,
          zIndex: 200,
          display: "flex",
          flexDirection: "column",
          left: isMobile ? (mobileOpen ? 0 : -siderWidth) : 0,
          transition: "all 0.2s",
          boxShadow: isMobile && mobileOpen ? "4px 0 24px rgba(0,0,0,0.25)" : "none",
        }}
      >
        {/* Logo */}
        <div style={{ padding: "28px 24px", borderBottom: "1px solid rgba(255,255,255,0.08)", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div style={{ fontSize: 18, fontWeight: 700, color: "#fff", letterSpacing: "-0.3px" }}>
            {!collapsed || isMobile ? <>Legal<span style={{ color: "rgba(130,170,255,0.9)" }}>Ease</span></> : <Scale size={20} color="#fff" />}
          </div>
          {isMobile && (
            <button onClick={() => setMobileOpen(false)} style={{ background: "transparent", border: "none", cursor: "pointer", color: "rgba(255,255,255,0.5)", display: "flex", padding: 4 }}>
              <X size={18} />
            </button>
          )}
        </div>

        {/* Nav */}
        <nav style={{ padding: "16px 12px", flex: 1, overflowY: "auto", overflowX: "hidden" }}>
          {visibleNavItems.map(item => (
            <NavItem
              key={item.key}
              icon={item.icon}
              label={item.label}
              isActive={pathname === item.key || (item.key !== ROUTES.HOME && pathname.startsWith(item.key))}
              collapsed={!isMobile && collapsed}
              onClick={() => { navigate(item.key); if (isMobile) setMobileOpen(false); }}
            />
          ))}
        </nav>

      </Sider>

      {/* ── Main area ── */}
      <Layout style={{ marginLeft: isMobile ? 0 : (collapsed ? collapsedWidth : siderWidth), transition: "all 0.2s" }}>
        {/* Header */}
        <Header
          style={{
            background: "#1a2744",
            padding: isMobile ? "0 16px" : "0 32px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            height: 56,
            position: "sticky",
            top: 0,
            zIndex: 99,
          }}
        >
          {/* Left: mobile burger / desktop collapse */}
          {isMobile ? (
            <button
              onClick={() => setMobileOpen(true)}
              style={{ display: "flex", alignItems: "center", justifyContent: "center", width: 32, height: 32, borderRadius: 6, border: "1px solid rgba(255,255,255,0.15)", background: "transparent", cursor: "pointer", color: "#fff" }}
            >
              <Menu size={16} />
            </button>
          ) : (
            <button
              onClick={() => setCollapsed(c => !c)}
              title={collapsed ? "Развернуть" : "Свернуть"}
              style={{ display: "flex", alignItems: "center", justifyContent: "center", width: 32, height: 32, borderRadius: 6, border: "none", background: "transparent", cursor: "pointer", color: "rgba(255,255,255,0.45)", transition: "color 0.әs" }}
              onMouseEnter={e => (e.currentTarget.style.color = "#fff")}
              onMouseLeave={e => (e.currentTarget.style.color = "rgba(255,255,255,0.45)")}
            >
              {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
            </button>
          )}

          {/* Right: user avatar */}
          <Dropdown trigger={["click"]} menu={{ items: dropdownItems }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer", padding: "4px 8px", borderRadius: 8, transition: "background 0.2s" }}
              onMouseEnter={e => (e.currentTarget.style.background = "rgba(255,255,255,0.08)")}
              onMouseLeave={e => (e.currentTarget.style.background = "transparent")}
            >
              <Avatar size={28} style={{ background: "rgba(255,255,255,0.15)", fontWeight: 700, borderRadius: 6, fontSize: 11, color: "#fff", border: "1px solid rgba(255,255,255,0.2)" }}>
                {avatarLetter}
              </Avatar>
            </div>
          </Dropdown>
        </Header>

        {/* Content */}
        <Content style={{ padding: isFullBleed ? 0 : (isMobile ? 16 : 32), background: "#f0f2f7", minHeight: "calc(100vh - 56px)" }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
