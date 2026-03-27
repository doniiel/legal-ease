import { Spin } from "antd";
import type { ReactNode } from "react";

interface Props {
  label: string;
  value: number | undefined;
  color: string;
  icon: ReactNode;
  loading?: boolean;
}

export default function StatCard({ label, value, color, icon, loading }: Props) {
  return (
    <div
      style={{
        background: "#fff",
        borderRadius: 12,
        padding: "24px",
        flex: 1,
        borderLeft: `4px solid ${color}`,
        boxShadow: "0 8px 32px rgba(11,28,48,0.04)",
        height: 120,
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <span
          style={{
            fontSize: 11, fontWeight: 700, textTransform: "uppercase",
            letterSpacing: "0.08em", color: "#7790bd",
          }}
        >
          {label}
        </span>
        <span style={{ color: `${color}25`, display: "flex" }}>{icon}</span>
      </div>
      {loading ? (
        <Spin size="small" />
      ) : (
        <div style={{ fontSize: 30, fontWeight: 800, color, lineHeight: 1, fontFamily: "Manrope, sans-serif" }}>
          {value ?? 0}
        </div>
      )}
    </div>
  );
}
