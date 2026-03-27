import { Modal, Button } from "antd";
import { Lock, Unlock } from "lucide-react";

interface Props {
  open: boolean;
  blocking: boolean;
  loading: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function BlockConfirmModal({ open, blocking, loading, onConfirm, onCancel }: Props) {
  return (
    <Modal open={open} footer={null} onCancel={onCancel} centered width={400} closable={false}>
      <div style={{ textAlign: "center", padding: "8px 0 4px" }}>
        <div
          style={{
            width: 56, height: 56, borderRadius: "50%",
            background: blocking ? "#fff1f2" : "#f0fdf4",
            display: "flex", alignItems: "center", justifyContent: "center",
            margin: "0 auto 16px",
          }}
        >
          {blocking
            ? <Lock size={24} color="#ef4444" />
            : <Unlock size={24} color="#16a34a" />
          }
        </div>

        <div style={{ fontSize: 18, fontWeight: 700, color: "#0b1c30", marginBottom: 8 }}>
          {blocking ? "Заблокировать пользователя?" : "Разблокировать пользователя?"}
        </div>
        <div style={{ fontSize: 13, color: "#64748b", marginBottom: 28, lineHeight: 1.6 }}>
          {blocking
            ? "Пользователь потеряет доступ к системе. Это действие можно отменить."
            : "Пользователь снова получит доступ к системе."}
        </div>

        <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>
          <Button onClick={onCancel} style={{ borderRadius: 8, minWidth: 100 }}>
            Отмена
          </Button>
          <Button
            type="primary"
            danger={blocking}
            loading={loading}
            onClick={onConfirm}
            style={{
              borderRadius: 8, minWidth: 100,
              background: blocking ? "#ef4444" : "#059669",
              borderColor: blocking ? "#ef4444" : "#059669",
            }}
          >
            {blocking ? "Заблокировать" : "Разблокировать"}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
