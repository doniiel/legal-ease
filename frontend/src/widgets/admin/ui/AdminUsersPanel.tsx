import { Table, Tag, Space, Typography, Input, Avatar, Tooltip, Popconfirm, Select } from "antd";
import type { ColumnsType } from "antd/es/table";
import { Users, Search, ShieldOff, Eye, Scale, Activity, Lock, Unlock } from "lucide-react";
import { useAdminUsers } from "../../../features/admin/model/use-admin-users";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import { isAdmin, isLawyer, getRoleLabel, getRoleColor } from "../../../features/admin/lib/user-role";
import BlockConfirmModal from "../../../features/admin/ui/BlockConfirmModal";
import UserDetailDrawer from "../../../features/admin/ui/UserDetailDrawer";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";
import type { AdminUser } from "../../../features/admin/api/admin-user-api";

const { Text } = Typography;

export default function AdminUsersPanel() {
  const isMobile = useIsMobile();
  const {
    data, metrics, filtered,
    isLoading, isMetricsLoading, isBlocking, isUnblocking, isRevoking,
    page, setPage,
    search, setSearch, roleFilter, setRoleFilter,
    selectedUserId, setSelectedUserId,
    blockedUsers, confirmModal, setConfirmModal,
    handleConfirmToggle, handleRevoke,
  } = useAdminUsers();

  const columns: ColumnsType<AdminUser> = [
    {
      title: "Пользователь",
      key: "user",
      render: (_, record) => (
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <Avatar
            size={40}
            src={record.userProfileImageUrl || undefined}
            style={{ background: "#0F2A44", fontWeight: 700, flexShrink: 0, borderRadius: 10 }}
          >
            {!record.userProfileImageUrl && `${record.firstName?.[0] ?? ""}${record.lastName?.[0] ?? ""}`}
          </Avatar>
          <div>
            <Text strong style={{ display: "block", fontSize: 14, color: "#0b1c30" }}>
              {record.firstName} {record.lastName}
            </Text>
            <Text style={{ fontSize: 12, color: "#64748b" }}>{record.email}</Text>
          </div>
        </div>
      ),
    },
    {
      title: "Username",
      dataIndex: "username",
      key: "username",
      render: (v) => (
        <Text style={{ fontFamily: "monospace", fontSize: 13, color: "#475569", background: "#f8fafc", padding: "3px 8px", borderRadius: 6, border: "1px solid #e2e8f0" }}>
          @{v}
        </Text>
      ),
    },
    {
      title: "Телефон",
      dataIndex: "telephone",
      key: "telephone",
      render: (v) => <Text style={{ fontSize: 13, color: "#475569" }}>{v || "—"}</Text>,
    },
    {
      title: "Роль",
      key: "role",
      render: (_, record) => (
        <Tag color={getRoleColor(record)} style={{ margin: 0, fontWeight: 600, fontSize: 11 }}>
          {getRoleLabel(record)}
        </Tag>
      ),
    },
    {
      title: "Действия",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Space size={2}>
          {/* View */}
          <Tooltip title="Просмотр">
            <button
              onClick={() => setSelectedUserId(record.id)}
              style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
              onMouseEnter={(e) => { e.currentTarget.style.background = "#f1f5f9"; e.currentTarget.style.color = "#0F2A44"; }}
              onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
            >
              <Eye size={15} />
            </button>
          </Tooltip>

          {/* Block / Unblock */}
          <Tooltip title={isAdmin(record) ? "Нельзя заблокировать админа" : blockedUsers.has(record.id) ? "Разблокировать" : "Заблокировать"}>
            <button
              disabled={isAdmin(record)}
              onClick={() => !isAdmin(record) && setConfirmModal({ userId: record.id, block: !blockedUsers.has(record.id) })}
              style={{
                width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent",
                cursor: isAdmin(record) ? "not-allowed" : "pointer",
                color: isAdmin(record) ? "#cbd5e1" : blockedUsers.has(record.id) ? "#ef4444" : "#64748b",
                display: "flex", alignItems: "center", justifyContent: "center",
                transition: "background 0.15s, color 0.15s",
              }}
              onMouseEnter={(e) => {
                if (!isAdmin(record)) {
                  e.currentTarget.style.background = blockedUsers.has(record.id) ? "#f0fdf4" : "#fff1f2";
                  e.currentTarget.style.color = blockedUsers.has(record.id) ? "#16a34a" : "#ef4444";
                }
              }}
              onMouseLeave={(e) => {
                if (!isAdmin(record)) {
                  e.currentTarget.style.background = "transparent";
                  e.currentTarget.style.color = blockedUsers.has(record.id) ? "#ef4444" : "#64748b";
                }
              }}
            >
              {(isBlocking || isUnblocking) && confirmModal?.userId === record.id
                ? <span style={{ fontSize: 12 }}>…</span>
                : blockedUsers.has(record.id) ? <Lock size={15} /> : <Unlock size={15} />
              }
            </button>
          </Tooltip>

          {/* Revoke lawyer */}
          {isLawyer(record) && (
            <Tooltip title="Отозвать статус адвоката">
              <Popconfirm
                title="Отозвать статус адвоката?"
                description="Пользователь сохранит роль USER"
                onConfirm={() => handleRevoke(record.id)}
                okText="Да" cancelText="Отмена"
              >
                <button
                  style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
                  onMouseEnter={(e) => { e.currentTarget.style.background = "#fffbeb"; e.currentTarget.style.color = "#d97706"; }}
                  onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
                >
                  {isRevoking ? <span style={{ fontSize: 12 }}>…</span> : <ShieldOff size={15} />}
                </button>
              </Popconfirm>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ padding: isMobile ? "24px 16px 40px" : "40px 40px 56px", display: "flex", justifyContent: "space-between", alignItems: "flex-end", flexDirection: isMobile ? "column" : undefined }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
              Управление пользователями
            </h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>
              Блокировка, управление ролями и просмотр профилей пользователей системы
            </p>
          </div>
         
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(3, 1fr)", gap: 20, flexWrap: "wrap", marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего пользователей" value={data?.totalElements} color="#0F2A44" icon={<Users size={26} />} loading={isLoading} />
          <StatCard label="Юристов"              value={metrics?.totalLawyers}  color="#1677ff"  icon={<Scale size={26} />}    loading={isMetricsLoading} />
          <StatCard label="Активных"             value={metrics?.activeUsers}   color="#059669"  icon={<Activity size={26} />} loading={isMetricsLoading} />
        </div>

        {/* ── Filters ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : "1fr 200px", gap: 20, alignItems: "end" }}>
            <div>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <div style={{ position: "relative" }}>
                <span style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" }}>
                  <Search size={15} color="#757682" />
                </span>
                <Input
                  placeholder="Имя, email или username..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  style={{ paddingLeft: 36, borderRadius: 12, background: "#eff4ff", border: "none" }}
                  allowClear
                />
              </div>
            </div>
            <div>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Роль</div>
              <Select
                value={roleFilter}
                onChange={setRoleFilter}
                style={{ width: "100%" }}
                options={[
                  { value: "ALL",    label: "Все роли" },
                  { value: "USER",   label: "Пользователь" },
                  { value: "LAWYER", label: "Адвокат" },
                  { value: "ADMIN",  label: "Админ" },
                ]}
              />
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
            pagination={{
              current: page,
              total: data?.totalElements,
              pageSize: 10,
              onChange: (p) => setPage(p),
              showSizeChanger: false,
              showTotal: (total) => `Всего ${total} пользователей`,
              style: { padding: "16px 32px", margin: 0 },
            }}
            style={{ borderRadius: 0 }}
            showHeader
          />
        </div>

        {/* ── Drawer ── */}
        <UserDetailDrawer userId={selectedUserId} onClose={() => setSelectedUserId(null)} />

        {/* ── Block/Unblock modal ── */}
        <BlockConfirmModal
          open={confirmModal !== null}
          blocking={confirmModal?.block ?? true}
          onConfirm={handleConfirmToggle}
          onCancel={() => setConfirmModal(null)}
          loading={isBlocking || isUnblocking}
        />
      </div>
    </div>
  );
}
