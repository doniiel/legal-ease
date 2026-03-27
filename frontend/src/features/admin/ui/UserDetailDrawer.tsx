import { Avatar, Divider, Drawer, Spin, Tag, Typography } from "antd";
import { User } from "lucide-react";
import { useGetUserByIdQuery } from "../api/admin-user-api";
import { getRoleColor, getRoleLabel } from "../lib/user-role";

const { Text } = Typography;

interface Props {
  userId: number | null;
  onClose: () => void;
}

export default function UserDetailDrawer({ userId, onClose }: Props) {
  const { data: user, isLoading } = useGetUserByIdQuery(userId!, {
    skip: userId === null,
  });

  return (
    <Drawer
      title="Информация о пользователе"
      open={userId !== null}
      onClose={onClose}
      width={380}
    >
      {isLoading && <Spin style={{ display: "block", margin: "40px auto" }} />}
      {user && (
        <div>
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", paddingBottom: 24 }}>
            <Avatar
              size={72}
              src={user.userProfileImageUrl || undefined}
              style={{ background: "#0F2A44", fontWeight: 700, fontSize: 24, marginBottom: 12, borderRadius: 16 }}
              icon={!user.userProfileImageUrl ? <User size={32} /> : undefined}
            >
              {!user.userProfileImageUrl && `${user.firstName?.[0] ?? ""}${user.lastName?.[0] ?? ""}`}
            </Avatar>
            <Text strong style={{ fontSize: 17 }}>
              {user.firstName} {user.lastName}
            </Text>
            <Tag color={getRoleColor(user)} style={{ marginTop: 6 }}>
              {getRoleLabel(user)}
            </Tag>
          </div>

          <Divider style={{ margin: "0 0 16px" }} />

          {[
            { label: "ID",       value: user.id },
            { label: "Email",    value: user.email },
            { label: "Username", value: `@${user.username}` },
            { label: "Телефон",  value: user.telephone || "—" },
          ].map(({ label, value }) => (
            <div
              key={label}
              style={{ display: "flex", justifyContent: "space-between", marginBottom: 14, alignItems: "center" }}
            >
              <Text type="secondary" style={{ fontSize: 13 }}>{label}</Text>
              <Text style={{ fontSize: 13, fontFamily: label === "Username" || label === "ID" ? "monospace" : undefined }}>
                {String(value)}
              </Text>
            </div>
          ))}
        </div>
      )}
    </Drawer>
  );
}
