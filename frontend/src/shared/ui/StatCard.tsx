import { Spin } from "antd";
import type { ReactNode } from "react";

interface Props {
  label: string;
  value: number | undefined;
  color?: string;
  icon: ReactNode;
  loading?: boolean;
}

export default function StatCard({ label, value, icon, loading }: Props) {
  return (
    <div
      style={{
        background: "#fff",
        borderRadius: 14,
        padding: "20px 22px 22px",
        border: "1px solid #e8eaf0",
        transition: "box-shadow 0.15s, transform 0.15s",
        cursor: "default",
      }}
      onMouseEnter={e => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.boxShadow = "0 6px 24px rgba(26,39,68,0.09)";
        el.style.transform = "translateY(-1px)";
      }}
      onMouseLeave={e => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.boxShadow = "none";
        el.style.transform = "translateY(0)";
      }}
    >
      <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 14 }}>
        <span style={{ fontSize: 11, fontWeight: 600, letterSpacing: "0.07em", textTransform: "uppercase", color: "#8a92a6" }}>
          {label}
        </span>
        <div style={{ width: 32, height: 32, borderRadius: 8, background: "#f0f2f7", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, color: "#6b7490" }}>
          {icon}
        </div>
      </div>
      {loading ? (
        <Spin size="small" />
      ) : (
        <div style={{ fontSize: 36, fontWeight: 700, color: "#1a2744", letterSpacing: "-1.5px", lineHeight: 1 }}>
          {value ?? 0}
        </div>
      )}
    </div>
  );
}
