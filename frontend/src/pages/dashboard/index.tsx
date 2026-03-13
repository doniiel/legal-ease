import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Layout, Menu, Button, Avatar, Dropdown } from "antd";
import {
  FileText,
  LayoutDashboard,
  User,
  Settings,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Briefcase,
  ShieldCheck,
} from "lucide-react";
import { useAuth } from "../../features/auth";
import LawyerApplicationPanel from "../../widgets/lawyer-application/ui/LawyerApplicationPanel";
import AdminLawyerApplicationsPanel from "../../widgets/admin/ui/AdminLawyerApplicationsPanel";

const { Header, Sider, Content } = Layout;

function getRoleFromToken(token: string | null): string | null {
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.role ?? payload.roles?.[0] ?? null;
  } catch {
    return null;
  }
}

export default function Dashboard() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [selectedKey, setSelectedKey] = useState("dashboard");

  const role = useMemo(() => getRoleFromToken(localStorage.getItem("accessToken")), []);
  const isAdmin = role === "ADMIN" || role === "ROLE_ADMIN" || true;

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const menuItems = [
    {
      key: "dashboard",
      icon: <LayoutDashboard size={18} />,
      label: "Главная",
    },
    {
      key: "documents",
      icon: <FileText size={18} />,
      label: "Документы",
    },
    {
      key: "profile",
      icon: <User size={18} />,
      label: "Профиль",
    },
    {
      key: "lawyer-application",
      icon: <Briefcase size={18} />,
      label: "Заявка адвоката",
    },
    {
      key: "settings",
      icon: <Settings size={18} />,
      label: "Настройки",
    },
    ...(isAdmin
      ? [
          {
            key: "admin-lawyer-applications",
            icon: <ShieldCheck size={18} />,
            label: "Заявки (Админ)",
          },
        ]
      : []),
  ];

  const dropdownItems = [
    {
      key: "logout",
      icon: <LogOut size={16} />,
      label: "Выйти",
      onClick: handleLogout,
    },
  ];

  function renderContent() {
    switch (selectedKey) {
      case "lawyer-application":
        return <LawyerApplicationPanel />;
      case "admin-lawyer-applications":
        return <AdminLawyerApplicationsPanel />;
      default:
        return (
          <>
            <h2 style={{ marginBottom: 8, color: "#0F2A44", fontWeight: 600 }}>
              Добро пожаловать в LegalEase
            </h2>
            <p style={{ color: "#6b7280" }}>
              Выберите раздел в боковом меню для начала работы.
            </p>
          </>
        );
    }
  }

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={240}
        style={{ background: "#0F2A44" }}
      >
        <div
          style={{
            height: 64,
            display: "flex",
            alignItems: "center",
            justifyContent: collapsed ? "center" : "flex-start",
            padding: collapsed ? 0 : "0 24px",
            borderBottom: "1px solid rgba(255,255,255,0.08)",
          }}
        >
          {!collapsed && (
            <span style={{ color: "#fff", fontWeight: 600, fontSize: 18 }}>
              LegalEase
            </span>
          )}
          {collapsed && (
            <span style={{ color: "#fff", fontWeight: 700, fontSize: 20 }}>L</span>
          )}
        </div>

        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => setSelectedKey(key)}
          style={{
            background: "#0F2A44",
            border: "none",
            marginTop: 8,
          }}
          theme="dark"
        />
      </Sider>

      <Layout>
        <Header
          style={{
            padding: "0 24px",
            background: "#fff",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
            position: "sticky",
            top: 0,
            zIndex: 10,
          }}
        >
          <Button
            type="text"
            onClick={() => setCollapsed(!collapsed)}
            icon={
              collapsed
                ? <ChevronRight size={20} />
                : <ChevronLeft size={20} />
            }
            style={{ width: 40, height: 40, display: "flex", alignItems: "center", justifyContent: "center" }}
          />

          <Dropdown menu={{ items: dropdownItems }} placement="bottomRight" trigger={["click"]}>
            <Button
              type="text"
              style={{
                height: 40,
                padding: "0 8px",
                display: "flex",
                alignItems: "center",
                gap: 8,
              }}
            >
              <Avatar
                size={32}
                icon={<User size={16} />}
                style={{ background: "#0F2A44", display: "flex", alignItems: "center", justifyContent: "center" }}
              />
            </Button>
          </Dropdown>
        </Header>

        <Content
          style={{
            margin: 24,
            padding: 32,
            background: "#fff",
            borderRadius: 10,
            minHeight: "calc(100vh - 64px - 48px)",
          }}
        >
          {renderContent()}
        </Content>
      </Layout>
    </Layout>
  );
}
