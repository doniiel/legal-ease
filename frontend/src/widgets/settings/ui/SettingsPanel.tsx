import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { App, Card, Input, Col, Row, Spin } from "antd";
import {
  Settings,
  KeyRound,
  User,
  Mail,
  CheckCircle2,
  ChevronRight,
} from "lucide-react";
import { useGetProfileQuery } from "../../../features/profile/api/profile-api";
import {
  useSendResetCodeMutation,
  useVerifyResetCodeMutation,
  useResetPasswordMutation,
} from "../../../features/auth/api/auth-api";
import { useAuth } from "../../../features/auth/model/use-auth";
import { ROUTES } from "../../../app/router/router";

const ROLE_LABEL: Record<string, string> = {
  USER: "Пользователь",
  LAWYER: "Адвокат",
  ADMIN: "Администратор",
};

const GENDER_LABEL: Record<string, string> = {
  MALE: "Мужской",
  FEMALE: "Женский",
};

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "12px 0", borderBottom: "1px solid #f0f0f0" }}>
      <span style={{ fontSize: 11, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.05em" }}>{label}</span>
      <span style={{ fontSize: 14, color: "#111827", fontWeight: 500 }}>{value || "—"}</span>
    </div>
  );
}

function StepDots({ step }: { step: number }) {
  const labels = ["Отправка кода", "Проверка", "Новый пароль"];
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 0, marginBottom: 28 }}>
      {labels.map((label, i) => {
        const done = i < step;
        const active = i === step;
        return (
          <div key={i} style={{ display: "flex", alignItems: "center", flex: i < labels.length - 1 ? 1 : "none" }}>
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4, minWidth: 72 }}>
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: "50%",
                  background: done ? "#059669" : active ? "#0F2A44" : "#e5e7eb",
                  color: done || active ? "#fff" : "#9ca3af",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontWeight: 700,
                  fontSize: 13,
                  transition: "background 0.2s",
                }}
              >
                {done ? <CheckCircle2 size={16} /> : i + 1}
              </div>
              <span style={{ fontSize: 11, color: active ? "#0F2A44" : done ? "#059669" : "#9ca3af", fontWeight: active ? 700 : 500, whiteSpace: "nowrap" }}>
                {label}
              </span>
            </div>
            {i < labels.length - 1 && (
              <div style={{ flex: 1, height: 2, background: done ? "#059669" : "#e5e7eb", marginBottom: 20, transition: "background 0.2s" }} />
            )}
          </div>
        );
      })}
    </div>
  );
}

export default function SettingsPanel() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { logout } = useAuth();

  const { data: profile, isLoading: profileLoading } = useGetProfileQuery();
  const [sendCode, { isLoading: isSending }] = useSendResetCodeMutation();
  const [verifyCode, { isLoading: isVerifying }] = useVerifyResetCodeMutation();
  const [resetPassword, { isLoading: isResetting }] = useResetPasswordMutation();

  const [step, setStep] = useState(0); // 0 = send code, 1 = verify, 2 = new password, 3 = success
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [codeError, setCodeError] = useState("");
  const [passwordError, setPasswordError] = useState("");

  const email = profile?.email ?? "";

  const handleSendCode = async () => {
    if (!email) return;
    try {
      await sendCode({ email }).unwrap();
      message.success("Код отправлен на " + email);
      setStep(1);
    } catch {
      message.error("Не удалось отправить код");
    }
  };

  const handleVerifyCode = async () => {
    if (!code.trim()) {
      setCodeError("Введите код");
      return;
    }
    setCodeError("");
    try {
      await verifyCode({ email, code }).unwrap();
      setStep(2);
    } catch {
      setCodeError("Неверный код. Попробуйте ещё раз.");
    }
  };

  const handleResetPassword = async () => {
    if (newPassword.length < 6) {
      setPasswordError("Минимум 6 символов");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError("Пароли не совпадают");
      return;
    }
    setPasswordError("");
    try {
      await resetPassword({ email, code, newPassword }).unwrap();
      setStep(3);
    } catch {
      message.error("Не удалось изменить пароль");
    }
  };

  const handleLogoutAndLogin = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  return (
    <div style={{ padding: "0 32px 32px" }}>
      {/* ── Full-bleed hero ── */}
      <div
        style={{
          background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
          padding: "40px 40px 56px",
          marginLeft: -32,
          marginRight: -32,
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
          <Settings size={28} color="#fff" />
        </div>
        <div>
          <div style={{ color: "#fff", fontSize: 22, fontWeight: 800, lineHeight: 1.2, marginBottom: 4 }}>
            Настройки
          </div>
          <div style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
            Управление паролем и просмотр данных аккаунта
          </div>
        </div>
      </div>

      {/* ── Content (no stat cards for settings) ── */}
      <div style={{ marginTop: 28 }}>
        {profileLoading ? (
          <div style={{ textAlign: "center", padding: "60px 0" }}>
            <Spin size="large" />
          </div>
        ) : (
          <Row gutter={24}>
            {/* ── LEFT: password change ── */}
            <Col xs={24} lg={14}>
              <Card
                style={{ borderRadius: 14, border: "1px solid #e5e7eb", marginBottom: 20 }}
                styles={{ body: { padding: "28px 32px" } }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6 }}>
                  <KeyRound size={18} color="#0F2A44" />
                  <span style={{ fontWeight: 700, fontSize: 16, color: "#111827" }}>Изменение пароля</span>
                </div>
                <p style={{ color: "#6b7280", fontSize: 13, marginBottom: 28, marginTop: 4 }}>
                  Для смены пароля мы отправим код подтверждения на ваш email.
                </p>

                {step < 3 && <StepDots step={step} />}

                {/* Step 0: send code */}
                {step === 0 && (
                  <div>
                    <div
                      style={{
                        background: "#f8fafc",
                        border: "1px solid #e2e8f0",
                        borderRadius: 10,
                        padding: "14px 18px",
                        display: "flex",
                        alignItems: "center",
                        gap: 10,
                        marginBottom: 20,
                      }}
                    >
                      <Mail size={15} color="#64748b" />
                      <div>
                        <div style={{ fontSize: 11, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                          Код будет отправлен на
                        </div>
                        <div style={{ fontSize: 14, fontWeight: 600, color: "#111827" }}>{email}</div>
                      </div>
                    </div>
                    <button
                      onClick={handleSendCode}
                      disabled={isSending || !email}
                      style={{
                        background: "#0F2A44",
                        color: "#fff",
                        border: "none",
                        borderRadius: 8,
                        padding: "10px 24px",
                        fontSize: 14,
                        fontWeight: 600,
                        cursor: isSending || !email ? "not-allowed" : "pointer",
                        opacity: isSending || !email ? 0.7 : 1,
                        display: "flex",
                        alignItems: "center",
                        gap: 8,
                      }}
                    >
                      {isSending ? (
                        <span>Отправка...</span>
                      ) : (
                        <>
                          <Mail size={14} />
                          Отправить код
                        </>
                      )}
                    </button>
                  </div>
                )}

                {/* Step 1: verify code */}
                {step === 1 && (
                  <div>
                    <p style={{ fontSize: 13, color: "#6b7280", marginBottom: 12 }}>
                      Введите 6-значный код, отправленный на <strong>{email}</strong>
                    </p>
                    <Input
                      value={code}
                      onChange={(e) => { setCode(e.target.value); setCodeError(""); }}
                      placeholder="000000"
                      maxLength={6}
                      size="large"
                      style={{
                        fontFamily: "monospace",
                        fontSize: 20,
                        letterSpacing: 8,
                        textAlign: "center",
                        borderRadius: 8,
                        marginBottom: 8,
                        borderColor: codeError ? "#ef4444" : undefined,
                      }}
                    />
                    {codeError && <p style={{ color: "#ef4444", fontSize: 12, marginBottom: 8 }}>{codeError}</p>}
                    <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
                      <button
                        onClick={handleVerifyCode}
                        disabled={isVerifying}
                        style={{
                          background: "#0F2A44",
                          color: "#fff",
                          border: "none",
                          borderRadius: 8,
                          padding: "10px 24px",
                          fontSize: 14,
                          fontWeight: 600,
                          cursor: isVerifying ? "not-allowed" : "pointer",
                          opacity: isVerifying ? 0.7 : 1,
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        {isVerifying ? "Проверка..." : (<><ChevronRight size={14} />Проверить код</>)}
                      </button>
                      <button
                        onClick={() => { setStep(0); setCode(""); setCodeError(""); }}
                        style={{
                          background: "transparent",
                          color: "#6b7280",
                          border: "1px solid #e5e7eb",
                          borderRadius: 8,
                          padding: "10px 16px",
                          fontSize: 13,
                          cursor: "pointer",
                        }}
                      >
                        Назад
                      </button>
                    </div>
                  </div>
                )}

                {/* Step 2: new password */}
                {step === 2 && (
                  <div>
                    <div style={{ marginBottom: 16 }}>
                      <label style={{ fontSize: 13, fontWeight: 500, color: "#374151", display: "block", marginBottom: 6 }}>
                        Новый пароль
                      </label>
                      <Input.Password
                        value={newPassword}
                        onChange={(e) => { setNewPassword(e.target.value); setPasswordError(""); }}
                        placeholder="Минимум 6 символов"
                        size="large"
                        style={{ borderRadius: 8 }}
                      />
                    </div>
                    <div style={{ marginBottom: 20 }}>
                      <label style={{ fontSize: 13, fontWeight: 500, color: "#374151", display: "block", marginBottom: 6 }}>
                        Подтвердите пароль
                      </label>
                      <Input.Password
                        value={confirmPassword}
                        onChange={(e) => { setConfirmPassword(e.target.value); setPasswordError(""); }}
                        placeholder="Повторите новый пароль"
                        size="large"
                        style={{
                          borderRadius: 8,
                          borderColor: passwordError ? "#ef4444" : undefined,
                        }}
                      />
                    </div>
                    {passwordError && <p style={{ color: "#ef4444", fontSize: 12, marginBottom: 12 }}>{passwordError}</p>}
                    <div style={{ display: "flex", gap: 10 }}>
                      <button
                        onClick={handleResetPassword}
                        disabled={isResetting}
                        style={{
                          background: "#059669",
                          color: "#fff",
                          border: "none",
                          borderRadius: 8,
                          padding: "10px 24px",
                          fontSize: 14,
                          fontWeight: 600,
                          cursor: isResetting ? "not-allowed" : "pointer",
                          opacity: isResetting ? 0.7 : 1,
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        {isResetting ? "Сохранение..." : (<><CheckCircle2 size={14} />Сохранить пароль</>)}
                      </button>
                      <button
                        onClick={() => { setStep(1); setPasswordError(""); }}
                        style={{
                          background: "transparent",
                          color: "#6b7280",
                          border: "1px solid #e5e7eb",
                          borderRadius: 8,
                          padding: "10px 16px",
                          fontSize: 13,
                          cursor: "pointer",
                        }}
                      >
                        Назад
                      </button>
                    </div>
                  </div>
                )}

                {/* Step 3: success */}
                {step === 3 && (
                  <div
                    style={{
                      background: "#f0fdf4",
                      border: "1px solid #bbf7d0",
                      borderRadius: 12,
                      padding: "28px 24px",
                      textAlign: "center",
                    }}
                  >
                    <div
                      style={{
                        width: 56,
                        height: 56,
                        borderRadius: "50%",
                        background: "#d1fae5",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        margin: "0 auto 16px",
                      }}
                    >
                      <CheckCircle2 size={28} color="#059669" />
                    </div>
                    <div style={{ fontWeight: 700, fontSize: 16, color: "#065f46", marginBottom: 8 }}>
                      Пароль успешно изменён
                    </div>
                    <p style={{ color: "#6b7280", fontSize: 13, marginBottom: 20 }}>
                      Для продолжения работы необходимо войти заново.
                    </p>
                    <button
                      onClick={handleLogoutAndLogin}
                      style={{
                        background: "#0F2A44",
                        color: "#fff",
                        border: "none",
                        borderRadius: 8,
                        padding: "10px 24px",
                        fontSize: 14,
                        fontWeight: 600,
                        cursor: "pointer",
                      }}
                    >
                      Войти снова
                    </button>
                  </div>
                )}
              </Card>
            </Col>

            {/* ── RIGHT: account info ── */}
            <Col xs={24} lg={10}>
              <Card
                style={{ borderRadius: 14, border: "1px solid #e5e7eb" }}
                styles={{ body: { padding: "28px 28px" } }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 4 }}>
                  <User size={18} color="#0F2A44" />
                  <span style={{ fontWeight: 700, fontSize: 16, color: "#111827" }}>Данные аккаунта</span>
                </div>
                <p style={{ color: "#6b7280", fontSize: 12, marginBottom: 16, marginTop: 2 }}>
                  Для редактирования перейдите в{" "}
                  <span
                    onClick={() => navigate(ROUTES.PROFILE)}
                    style={{ color: "#0F2A44", fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}
                  >
                    Профиль
                  </span>
                </p>

                <InfoRow label="ФИО" value={profile?.fio ?? ""} />
                <InfoRow label="Email" value={profile?.email ?? ""} />
                <InfoRow label="Телефон" value={profile?.phone ?? ""} />
                <InfoRow label="Роль" value={ROLE_LABEL[profile?.role ?? "USER"] ?? ""} />
                <InfoRow label="ИИН" value={profile?.iin ?? ""} />
                <InfoRow
                  label="Дата рождения"
                  value={profile?.dateOfBirth ? new Date(profile.dateOfBirth).toLocaleDateString("ru-KZ") : ""}
                />
                <InfoRow
                  label="Пол"
                  value={profile?.gender ? GENDER_LABEL[profile.gender] ?? "" : ""}
                />

                <div style={{ marginTop: 20 }}>
                  <div
                    style={{
                      display: "inline-flex",
                      alignItems: "center",
                      gap: 4,
                      borderRadius: 20,
                      padding: "4px 12px",
                      background: profile?.active ? "#d1fae5" : "#fee2e2",
                      color: profile?.active ? "#059669" : "#b91c1c",
                      fontSize: 12,
                      fontWeight: 600,
                    }}
                  >
                    <span
                      style={{
                        width: 6,
                        height: 6,
                        borderRadius: "50%",
                        background: profile?.active ? "#059669" : "#b91c1c",
                      }}
                    />
                    {profile?.active ? "Активен" : "Неактивен"}
                  </div>
                </div>
              </Card>
            </Col>
          </Row>
        )}
      </div>
    </div>
  );
}
