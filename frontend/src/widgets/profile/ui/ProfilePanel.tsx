import { Form, Input, Button, Skeleton, Select, Divider, Tooltip } from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import {
  User, Mail, Phone, CreditCard,
  Shield, ShieldCheck, Crown, Pencil, X, Save, Lock,
  UserCircle,
} from "lucide-react";
import { useProfile } from "../../../features/profile/model/use-profile";

// ─── Role config ──────────────────────────────────────────────
const ROLE_CONFIG = {
  ADMIN: {
    label: "Администратор",
    color: "#ef4444",
    bg: "rgba(239,68,68,0.08)",
    border: "rgba(239,68,68,0.2)",
    icon: <Crown size={14} />,
    description: "Полный доступ к системе управления",
  },
  LAWYER: {
    label: "Адвокат",
    color: "#1677ff",
    bg: "rgba(22,119,255,0.08)",
    border: "rgba(22,119,255,0.2)",
    icon: <ShieldCheck size={14} />,
    description: "Верифицированный специалист платформы",
  },
  USER: {
    label: "Пользователь",
    color: "#64748b",
    bg: "rgba(100,116,139,0.06)",
    border: "rgba(100,116,139,0.15)",
    icon: <Shield size={14} />,
    description: "Стандартный аккаунт",
  },
};

function getInitials(fio: string) {
  return fio.trim().split(/\s+/).slice(0, 2).map((w) => w[0]).join("").toUpperCase();
}


// ─── Field label ──────────────────────────────────────────────
function FieldLabel({ children }: { children: React.ReactNode }) {
  return (
    <span style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#94a3b8" }}>
      {children}
    </span>
  );
}

const inputStyle = (editable: boolean): React.CSSProperties => ({
  borderRadius: 10,
  background: editable ? "#fff" : "#f8fafc",
  border: editable ? "1px solid #d1d5db" : "1px solid #f1f5f9",
  height: 44,
  color: editable ? "#0b1c30" : "#64748b",
});


// ─── Main panel ───────────────────────────────────────────────
export default function ProfilePanel() {
  const isMobile = useIsMobile();
  const { form, profile, isLoading, isSaving, editing, startEdit, cancelEdit, handleSave } = useProfile();

  if (isLoading) {
    return (
      <div style={{ maxWidth: 1000, margin: "0 auto", padding: "32px 0" }}>
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  if (!profile) return null;

  const roleCfg = ROLE_CONFIG[profile.role] ?? ROLE_CONFIG.USER;
  const avatarColors = ["#1a2744", "#1d4ed8", "#7c3aed", "#b45309", "#0369a1"];
  const avatarBg = avatarColors[profile.id % avatarColors.length];

  const ACCESS_ITEMS: Record<string, { label: string; desc: string }[]> = {
    ADMIN: [
      { label: "Управление пользователями", desc: "Блокировка, назначение ролей" },
      { label: "Управление правилами", desc: "Валидация, риски, матчинг" },
      { label: "Аудит системы", desc: "Полный журнал событий" },
      { label: "Категории документов", desc: "Создание и редактирование" },
      { label: "Заявки адвокатов", desc: "Рассмотрение и подтверждение" },
      { label: "Метрики платформы", desc: "Статистика и отчёты" },
    ],
    LAWYER: [
      { label: "Шаблоны документов", desc: "Создание и управление шаблонами" },
      { label: "Правила матчинга", desc: "Настройка логики подбора клиентов" },
      { label: "Правила валидации", desc: "Проверка корректности данных" },
      { label: "Условные правила", desc: "Гибкие сценарии документов" },
    ],
  };

  return (
    <div style={{ maxWidth: 1000, margin: "0 auto", padding: isMobile ? "0 16px" : 0 }}>
      {/* ── Page header ── */}
      <div style={{ marginBottom: isMobile ? 20 : 32 }}>
        <h1 style={{ fontSize: 32, fontWeight: 800, color: "#1a2744", margin: "0 0 6px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>
          Настройки профиля
        </h1>
        <p style={{ color: "#64748b", fontSize: 14, margin: 0 }}>
          Управляйте личными данными вашего аккаунта
        </p>
      </div>

      {/* ── Hero grid ── */}
      <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : "280px 1fr", gap: 24, marginBottom: 20 }}>
        {/* Avatar col */}
        <div style={{ background: "linear-gradient(135deg, #f8fafc 0%, #eff4ff 100%)", borderRadius: 20, padding: "36px 24px", display: "flex", flexDirection: "column", alignItems: "center", border: "1px solid rgba(197,198,210,0.25)", boxShadow: "0 4px 24px rgba(11,28,48,0.04)" }}>
          <div style={{ width: 96, height: 96, borderRadius: "50%", background: avatarBg, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 30, fontWeight: 800, color: "#fff", fontFamily: "Manrope, sans-serif", boxShadow: "0 8px 32px rgba(11,28,48,0.18)", marginBottom: 18, border: "4px solid #fff" }}>
            {getInitials(profile.fio)}
          </div>
          <div style={{ fontSize: 16, fontWeight: 800, color: "#0b1c30", fontFamily: "Manrope, sans-serif", textAlign: "center", marginBottom: 4, lineHeight: 1.3 }}>
            {profile.fio}
          </div>
          <div style={{ fontSize: 12, color: "#94a3b8", marginBottom: 20, textAlign: "center" }}>{profile.email}</div>

          <Divider style={{ margin: "0 0 20px", borderColor: "rgba(197,198,210,0.4)" }} />

          {/* Role badge */}
          <div style={{ display: "inline-flex", alignItems: "center", gap: 6, padding: "7px 16px", borderRadius: 20, background: roleCfg.bg, border: `1px solid ${roleCfg.border}`, marginBottom: 6, width: "100%", justifyContent: "center" }}>
            <span style={{ color: roleCfg.color, display: "flex" }}>{roleCfg.icon}</span>
            <span style={{ fontSize: 12, fontWeight: 700, color: roleCfg.color, letterSpacing: "0.04em" }}>{roleCfg.label}</span>
          </div>
          <div style={{ fontSize: 11, color: "#94a3b8", textAlign: "center", marginBottom: 20 }}>{roleCfg.description}</div>

          <Divider style={{ margin: "0 0 20px", borderColor: "rgba(197,198,210,0.4)" }} />

          <div style={{ marginTop: 20, display: "flex", alignItems: "center", gap: 6 }}>
            <span style={{ width: 7, height: 7, borderRadius: "50%", background: profile.active ? "#059669" : "#ef4444" }} />
            <span style={{ fontSize: 11, color: "#64748b", fontWeight: 600 }}>
              {profile.active ? "Аккаунт активен" : "Аккаунт неактивен"}
            </span>
          </div>
        </div>

        {/* Form col */}
        <div style={{ background: "#fff", borderRadius: 20, padding: "32px", border: "1px solid rgba(197,198,210,0.2)", boxShadow: "0 4px 24px rgba(11,28,48,0.04)" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 28 }}>
            <div>
              <div style={{ fontSize: 16, fontWeight: 700, color: "#0b1c30", fontFamily: "Manrope, sans-serif" }}>Личные данные</div>
              <div style={{ fontSize: 12, color: "#94a3b8", marginTop: 2 }}>Редактируемые поля: ФИО, телефон и пол</div>
            </div>
            {!editing ? (
              <Button icon={<Pencil size={14} />} onClick={startEdit} style={{ borderRadius: 10, fontWeight: 600, height: 38, paddingLeft: 16, paddingRight: 16, borderColor: "#e2e8f0", color: "#1a2744" }}>
                Редактировать
              </Button>
            ) : (
              <div style={{ display: "flex", gap: 8 }}>
                <Button icon={<X size={14} />} onClick={cancelEdit} style={{ borderRadius: 10, height: 38 }}>
                  Отмена
                </Button>
                <Button icon={<Save size={14} />} loading={isSaving} onClick={handleSave} style={{ borderRadius: 10, height: 38, background: "#1a2744", borderColor: "#1a2744", color: "#fff", fontWeight: 600 }}>
                  Сохранить
                </Button>
              </div>
            )}
          </div>

          <Form form={form} layout="vertical">
            {/* FIO — full row */}
            <Form.Item
              name="fio"
              label={<FieldLabel>ФИО</FieldLabel>}
              rules={[{ required: true, message: "Введите ФИО" }]}
            >
              <Input
                prefix={<User size={14} color="#94a3b8" />}
                disabled={!editing}
                style={inputStyle(editing)}
                placeholder="Фамилия Имя Отчество"
              />
            </Form.Item>

            <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr", gap: isMobile ? 0 : "0 24px" }}>
              {/* Phone */}
              <Form.Item
                name="phone"
                label={<FieldLabel>Телефон</FieldLabel>}
                rules={[{ pattern: /^(\+7|8)7\d{9}$/, message: "Формат: +77001234567" }]}
              >
                <Input
                  prefix={<Phone size={14} color="#94a3b8" />}
                  disabled={!editing}
                  style={inputStyle(editing)}
                  placeholder="+77001234567"
                />
              </Form.Item>

              {/* Gender — editable Select */}
              <Form.Item name="gender" label={<FieldLabel>Пол</FieldLabel>}>
                <Select
                  disabled={!editing}
                  placeholder="Выберите пол"
                  allowClear
                  style={{ height: 44 }}
                  options={[
                    { value: "MALE",   label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><UserCircle size={14} />Мужской</span> },
                    { value: "FEMALE", label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><UserCircle size={14} />Женский</span> },
                  ]}
                  styles={{ popup: { root: { borderRadius: 10 } } }}
                />
              </Form.Item>

              {/* Email — read-only */}
              <Form.Item label={<FieldLabel>Email</FieldLabel>}>
                <Tooltip title="Email нельзя изменить">
                  <Input
                    prefix={<Mail size={14} color="#94a3b8" />}
                    suffix={<Lock size={13} color="#cbd5e1" />}
                    value={profile.email}
                    disabled
                    style={{ ...inputStyle(false), fontFamily: "inherit" }}
                  />
                </Tooltip>
              </Form.Item>

              {/* IIN — read-only */}
              <Form.Item label={<FieldLabel>ИИН</FieldLabel>}>
                <Tooltip title="ИИН нельзя изменить">
                  <Input
                    prefix={<CreditCard size={14} color="#94a3b8" />}
                    suffix={<Lock size={13} color="#cbd5e1" />}
                    value={profile.iin}
                    disabled
                    style={{ ...inputStyle(false), fontFamily: "monospace", letterSpacing: "0.05em" }}
                  />
                </Tooltip>
              </Form.Item>
            </div>
          </Form>
        </div>
      </div>

      {/* ── Role-specific access section ── */}
      {profile.role !== "USER" && (
        <div style={{ background: "#fff", borderRadius: 20, padding: "28px 32px", border: "1px solid rgba(197,198,210,0.2)", boxShadow: "0 2px 12px rgba(11,28,48,0.03)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 20 }}>
            <div style={{ width: 40, height: 40, borderRadius: 10, background: roleCfg.bg, border: `1px solid ${roleCfg.border}`, display: "flex", alignItems: "center", justifyContent: "center", color: roleCfg.color }}>
              {roleCfg.icon}
            </div>
            <div>
              <div style={{ fontSize: 15, fontWeight: 700, color: "#0b1c30", fontFamily: "Manrope, sans-serif" }}>
                {profile.role === "ADMIN" ? "Доступ администратора" : "Доступ адвоката"}
              </div>
              <div style={{ fontSize: 12, color: "#94a3b8" }}>{roleCfg.description}</div>
            </div>
          </div>
          <Divider style={{ margin: "0 0 20px" }} />
          <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : `repeat(${profile.role === "ADMIN" ? 3 : 2}, 1fr)`, gap: 12 }}>
            {(ACCESS_ITEMS[profile.role] ?? []).map((item) => (
              <div key={item.label} style={{ padding: "14px 18px", borderRadius: 12, background: "#f8fafc", border: "1px solid #f1f5f9", borderLeft: `3px solid ${roleCfg.color}` }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: "#0b1c30", marginBottom: 3 }}>{item.label}</div>
                <div style={{ fontSize: 11, color: "#94a3b8" }}>{item.desc}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
