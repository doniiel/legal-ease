import { useState } from "react";
import {
  App,
  Table,
  Tag,
  Button,
  Space,
  Typography,
  Input,
  Avatar,
  Tooltip,
  Popconfirm,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  Users,
  Search,
  ShieldOff,
  LockKeyhole,
  LockKeyholeOpen,
} from "lucide-react";
import {
  useGetUsersQuery,
  useBlockUserMutation,
  useUnblockUserMutation,
  useRevokeLawyerMutation,
  type AdminUser,
} from "../../../features/admin/api/admin-user-api";

const { Title, Text } = Typography;

const isLawyer = (user: AdminUser) =>
  user.role?.code?.toLowerCase().includes("lawyer");

const isAdmin = (user: AdminUser) =>
  user.role?.code?.toLowerCase().includes("admin");

export default function AdminUsersPanel() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");

  const { data, isLoading } = useGetUsersQuery({ page: page - 1, size: 10 });
  const [blockUser, { isLoading: isBlocking }] = useBlockUserMutation();
  const [unblockUser, { isLoading: isUnblocking }] = useUnblockUserMutation();
  const [revokeLawyer, { isLoading: isRevoking }] = useRevokeLawyerMutation();

  const handleBlock = async (id: number) => {
    try {
      await blockUser(id).unwrap();
      message.success("Пользователь заблокирован");
    } catch {
      message.error("Ошибка при блокировке");
    }
  };

  const handleUnblock = async (id: number) => {
    try {
      await unblockUser(id).unwrap();
      message.success("Пользователь разблокирован");
    } catch {
      message.error("Ошибка при разблокировке");
    }
  };

  const handleRevoke = async (id: number) => {
    try {
      await revokeLawyer(id).unwrap();
      message.success("Статус адвоката отозван");
    } catch {
      message.error("Ошибка при отзыве статуса");
    }
  };

  const filtered = (data?.content ?? []).filter((u) => {
    const q = search.toLowerCase();
    return (
      u.email.toLowerCase().includes(q) ||
      u.firstName.toLowerCase().includes(q) ||
      u.lastName.toLowerCase().includes(q) ||
      u.username?.toLowerCase().includes(q)
    );
  });

  const columns: ColumnsType<AdminUser> = [
    {
      title: "Пользователь",
      key: "user",
      render: (_, record) => (
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <Avatar
            size={36}
            src={record.userProfileImageUrl || undefined}
            style={{ background: "#0F2A44", fontWeight: 600, flexShrink: 0 }}
          >
            {!record.userProfileImageUrl &&
              `${record.firstName?.[0] ?? ""}${record.lastName?.[0] ?? ""}`}
          </Avatar>
          <div>
            <Text strong style={{ display: "block", fontSize: 13 }}>
              {record.firstName} {record.lastName}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.email}
            </Text>
          </div>
        </div>
      ),
    },
    {
      title: "Username",
      dataIndex: "username",
      key: "username",
      render: (v) => (
        <Text style={{ fontFamily: "monospace", fontSize: 13 }}>@{v}</Text>
      ),
    },
    {
      title: "Телефон",
      dataIndex: "telephone",
      key: "telephone",
      render: (v) => <Text style={{ fontSize: 13 }}>{v || "—"}</Text>,
    },
    {
      title: "Роль",
      key: "role",
      render: (_, record) => {
        const color = isAdmin(record)
          ? "red"
          : isLawyer(record)
          ? "blue"
          : "default";
        const label = isAdmin(record)
          ? "Админ"
          : isLawyer(record)
          ? "Адвокат"
          : "Пользователь";
        return (
          <Tag color={color} style={{ margin: 0 }}>
            {label}
          </Tag>
        );
      },
    },
    {
      title: "Действия",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Space size={6}>
          <Tooltip title="Заблокировать">
            <Popconfirm
              title="Заблокировать пользователя?"
              onConfirm={() => handleBlock(record.id)}
              okText="Да"
              cancelText="Отмена"
              disabled={isAdmin(record)}
            >
              <Button
                size="small"
                icon={<LockKeyhole size={14} />}
                loading={isBlocking}
                danger
                disabled={isAdmin(record)}
              />
            </Popconfirm>
          </Tooltip>

          <Tooltip title="Разблокировать">
            <Popconfirm
              title="Разблокировать пользователя?"
              onConfirm={() => handleUnblock(record.id)}
              okText="Да"
              cancelText="Отмена"
            >
              <Button
                size="small"
                icon={<LockKeyholeOpen size={14} />}
                loading={isUnblocking}
                style={{ borderColor: "#15803d", color: "#15803d" }}
              />
            </Popconfirm>
          </Tooltip>

          {isLawyer(record) && (
            <Tooltip title="Отозвать статус адвоката">
              <Popconfirm
                title="Отозвать статус адвоката?"
                description="Пользователь сохранит роль USER"
                onConfirm={() => handleRevoke(record.id)}
                okText="Да"
                cancelText="Отмена"
              >
                <Button
                  size="small"
                  icon={<ShieldOff size={14} />}
                  loading={isRevoking}
                  style={{ borderColor: "#d97706", color: "#d97706" }}
                />
              </Popconfirm>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* Header banner */}
      <div
        style={{
          background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
          borderRadius: 16,
          padding: "28px 36px",
          marginBottom: 24,
          display: "flex",
          alignItems: "center",
          gap: 20,
        }}
      >
        <div
          style={{
            width: 52,
            height: 52,
            borderRadius: 14,
            background: "rgba(255,255,255,0.15)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flexShrink: 0,
          }}
        >
          <Users size={26} color="#fff" />
        </div>
        <div>
          <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
            Управление пользователями
          </Title>
          <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Блокировка, разблокировка и управление ролями пользователей
          </Text>
        </div>
        {data && (
          <div style={{ marginLeft: "auto" }}>
            <div
              style={{
                background: "rgba(255,255,255,0.12)",
                borderRadius: 10,
                padding: "8px 18px",
                textAlign: "center",
              }}
            >
              <Text
                style={{
                  color: "#fff",
                  fontSize: 22,
                  fontWeight: 700,
                  display: "block",
                }}
              >
                {data.totalElements}
              </Text>
              <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 12 }}>
                Всего
              </Text>
            </div>
          </div>
        )}
      </div>

      {/* Search */}
      <div style={{ marginBottom: 16 }}>
        <Input
          prefix={<Search size={15} color="#9ca3af" />}
          placeholder="Поиск по имени, email или username..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: 360, borderRadius: 8 }}
        />
      </div>

      {/* Table */}
      <Table
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
        }}
        style={{ background: "#fff", borderRadius: 12 }}
      />
    </div>
  );
}
