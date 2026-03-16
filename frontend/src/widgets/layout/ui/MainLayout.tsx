import { type ReactNode, useState } from "react";
import { Layout, Menu, Typography, Avatar, Dropdown } from "antd";
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
  GitMerge,
  LogOut,
  PanelLeftClose,
  PanelLeftOpen,
} from "lucide-react";
import { ROUTES } from "../../../app/router/router";
import { useAuth } from "../../../features/auth/model/use-auth";

const { Sider, Content, Header } = Layout;
const { Text } = Typography;

const NAV_ITEMS = [
  { key: ROUTES.HOME,                  icon: <Home size={16} />,          label: "Главная",          roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.DOCUMENTS,             icon: <FileText size={16} />,      label: "Документы",        roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.PROFILE,               icon: <User size={16} />,          label: "Профиль",          roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.LAWYER_APPLICATION,    icon: <ShieldCheck size={16} />,   label: "Заявка адвоката",  roles: ["USER"] },
  { key: ROUTES.SETTINGS,              icon: <Settings size={16} />,      label: "Настройки",        roles: ["USER", "LAWYER", "ADMIN"] },
  { key: ROUTES.ADMIN_APPLICATIONS,    icon: <ClipboardList size={16} />, label: "Заявки (Админ)",   roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_USERS,           icon: <Users size={16} />,         label: "Пользователи",     roles: ["ADMIN"] },
  { key: ROUTES.ADMIN_CATEGORIES,      icon: <FolderOpen size={16} />,    label: "Категории",        roles: ["ADMIN"] },
  { key: ROUTES.LAWYER_TEMPLATES,      icon: <FileCode size={16} />,      label: "Мои шаблоны",      roles: ["LAWYER"] },
  { key: ROUTES.LAWYER_MATCHING_RULES, icon: <GitMerge size={16} />,      label: "Правила матчинга", roles: ["LAWYER"] },
];

interface MainLayoutProps {
  children: ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { role } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  const visibleNavItems = NAV_ITEMS.filter((item) =>
    role ? item.roles.includes(role) : false
  );

  const handleLogout = () => {
    navigate(ROUTES.LOGIN);
  };

  const profileMenu = {
    items: [
      {
        key: "profile",
        icon: <User size={14} />,
        label: "Профиль",
        onClick: () => navigate(ROUTES.PROFILE),
      },
      { type: "divider" as const },
      {
        key: "logout",
        icon: <LogOut size={14} />,
        label: "Выйти",
        danger: true,
        onClick: handleLogout,
      },
    ],
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider
        collapsed={collapsed}
        style={{ background: "#0F2A44" }}
        width={220}
        trigger={null}
      >
        {/* Logo */}
        <div
          style={{
            height: 64,
            display: "flex",
            alignItems: "center",
            justifyContent: collapsed ? "center" : "flex-start",
            padding: collapsed ? 0 : "0 20px",
            borderBottom: "1px solid rgba(255,255,255,0.08)",
          }}
        >
          {!collapsed && (
            <Text style={{ color: "#fff", fontWeight: 700, fontSize: 18 }}>
              LegalEase
            </Text>
          )}
        </div>

        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[pathname]}
          style={{ background: "#0F2A44", marginTop: 8 }}
          items={visibleNavItems.map((item) => ({
            key: item.key,
            icon: item.icon,
            label: item.label,
            onClick: () => navigate(item.key),
          }))}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            background: "#fff",
            padding: "0 24px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            borderBottom: "1px solid #f0f0f0",
            height: 64,
          }}
        >
          {/* Collapse button */}
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
            {collapsed
              ? <PanelLeftOpen size={18} />
              : <PanelLeftClose size={18} />
            }
          </button>

          {/* Profile dropdown */}
          <Dropdown menu={profileMenu} trigger={["click"]} placement="bottomRight">
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 10,
                cursor: "pointer",
                padding: "4px 8px",
                borderRadius: 8,
                transition: "background 0.2s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = "#f5f7fa")}
              onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
            >
              <Avatar
                size={36}
                style={{ background: "#0F2A44", fontSize: 14, fontWeight: 600 }}
                icon={<User size={18} />}
              />
              <Text style={{ fontWeight: 500, color: "#0F2A44" }}>Профиль</Text>
            </div>
          </Dropdown>
        </Header>

        <Content style={{ padding: 32, background: "#f5f7fa" }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
