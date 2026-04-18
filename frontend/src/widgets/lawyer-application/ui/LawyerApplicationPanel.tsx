import { useState } from "react";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import {
  App,
  Card,
  Form,
  Input,
  Button,
  Typography,
  Steps,
  Alert,
  Row,
  Col,
  Timeline,
  Divider,
  Spin,
} from "antd";
import {
  FileText,
  CheckCircle2,
  Clock3,
  XCircle,
  Send,
  ShieldCheck,
  AlertCircle,
  BadgeCheck,
  Info,
  CalendarDays,
  MessageSquare,
  CircleCheck,
} from "lucide-react";
import {
  useGetMyApplicationQuery,
  useSubmitApplicationMutation,
} from "../../../features/lawyer-application/api/lawyer-application-api";

const { Title, Text, Paragraph } = Typography;

const STATUS_CONFIG = {
  PENDING: {
    color: "#1d4ed8",
    bg: "#eff6ff",
    border: "#bfdbfe",
    icon: <Clock3 size={28} color="#1d4ed8" />,
    step: 1,
    title: "Заявка на рассмотрении",
    description: "Администратор рассмотрит вашу заявку в ближайшее время.",
    pillBg: "#dbeafe",
    pillColor: "#1d4ed8",
    pillLabel: "На рассмотрении",
  },
  APPROVED: {
    color: "#059669",
    bg: "#f0fdf4",
    border: "#bbf7d0",
    icon: <BadgeCheck size={28} color="#059669" />,
    step: 2,
    title: "Заявка одобрена!",
    description: "Поздравляем! Вам присвоен статус адвоката в системе LegalEase.",
    pillBg: "#d1fae5",
    pillColor: "#059669",
    pillLabel: "Одобрена",
  },
  REJECTED: {
    color: "#b91c1c",
    bg: "#fef2f2",
    border: "#fecaca",
    icon: <XCircle size={28} color="#b91c1c" />,
    step: 2,
    title: "Заявка отклонена",
    description: "К сожалению, ваша заявка была отклонена.",
    pillBg: "#fee2e2",
    pillColor: "#b91c1c",
    pillLabel: "Отклонена",
  },
};

const FAQ = [
  {
    q: "Сколько времени занимает рассмотрение?",
    a: "Обычно 1–3 рабочих дня. В период высокой нагрузки — до 5 дней.",
  },
  {
    q: "Что такое номер адвокатской лицензии?",
    a: "Это уникальный идентификатор, выданный вам адвокатской коллегией РК. Формат: KZ-ADV-ГГГГ-НННNN.",
  },
  {
    q: "Что если заявку отклонят?",
    a: "Вы получите причину отклонения и сможете исправить данные, подав заявку повторно.",
  },
];

export default function LawyerApplicationPanel() {
  const isMobile = useIsMobile();
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [showForm, setShowForm] = useState(false);

  const { data: application, isLoading, isError } = useGetMyApplicationQuery();
  const [submitApplication, { isLoading: isSubmitting }] = useSubmitApplicationMutation();

  const hasApplication = !isError && application != null;
  const displayForm = !isLoading && (!hasApplication || showForm);
  const cfg = hasApplication && !showForm ? STATUS_CONFIG[application.status] : null;

  const handleSubmit = async (values: { licenseNum: string }) => {
    try {
      await submitApplication({ licenseNum: values.licenseNum }).unwrap();
      setShowForm(false);
      form.resetFields();
    } catch {
      message.error("Ошибка при отправке заявки");
    }
  };

  return (
    <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>
      {/* ── Full-bleed hero ── */}
      <div
        style={{
          background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
          padding: isMobile ? "24px 16px 40px" : "40px 40px 56px",
          marginLeft: isMobile ? -16 : -32,
          marginRight: isMobile ? -16 : -32,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 20,
          flexWrap: "wrap",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
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
            <ShieldCheck size={28} color="#fff" />
          </div>
          <div>
            <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 700 }}>
              Заявка на статус адвоката
            </Title>
            <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
              Подайте заявку с номером лицензии для верификации и получения расширенных возможностей
            </Text>
          </div>
        </div>

        {/* Steps inline in header */}
        <div style={{ flexShrink: 0, minWidth: 280, display: isMobile ? "none" : undefined }}>
          <Steps
            size="small"
            current={cfg?.step ?? 0}
            status={application?.status === "REJECTED" && !showForm ? "error" : undefined}
            style={{ "--ant-steps-finish-color": "#fff" } as React.CSSProperties}
            items={[
              { title: <span style={{ color: "rgba(255,255,255,0.85)", fontSize: 12 }}>Подача</span>, icon: <Send size={13} color="#fff" /> },
              { title: <span style={{ color: "rgba(255,255,255,0.85)", fontSize: 12 }}>Рассмотрение</span>, icon: <Clock3 size={13} color="#fff" /> },
              { title: <span style={{ color: "rgba(255,255,255,0.85)", fontSize: 12 }}>Решение</span>, icon: <CheckCircle2 size={13} color="#fff" /> },
            ]}
          />
        </div>
      </div>

      {/* ── Status/license pills row (overlaps hero) ── */}
      {hasApplication && cfg && (
        <div
          style={{
            display: "flex",
            gap: 12,
            flexWrap: "wrap",
            marginTop: -28,
            position: "relative",
            zIndex: 2,
            marginBottom: 24,
          }}
        >
          <div
            style={{
              background: cfg.pillBg,
              border: `1px solid ${cfg.border}`,
              borderRadius: 10,
              padding: "10px 20px",
              display: "flex",
              alignItems: "center",
              gap: 8,
              boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
            }}
          >
            {cfg.icon && <span style={{ display: "flex" }}>{cfg.icon}</span>}
            <div>
              <div style={{ fontSize: 11, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.05em" }}>Статус</div>
              <div style={{ fontWeight: 700, fontSize: 14, color: cfg.pillColor }}>{cfg.pillLabel}</div>
            </div>
          </div>
          <div
            style={{
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 10,
              padding: "10px 20px",
              display: "flex",
              alignItems: "center",
              gap: 8,
              boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
            }}
          >
            <FileText size={20} color="#0F2A44" />
            <div>
              <div style={{ fontSize: 11, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.05em" }}>Лицензия</div>
              <div style={{ fontWeight: 600, fontSize: 13, color: "#111827", fontFamily: "monospace" }}>{application.licenseNumber}</div>
            </div>
          </div>
          <div
            style={{
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 10,
              padding: "10px 20px",
              display: "flex",
              alignItems: "center",
              gap: 8,
              boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
            }}
          >
            <CalendarDays size={20} color="#64748b" />
            <div>
              <div style={{ fontSize: 11, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.05em" }}>Дата подачи</div>
              <div style={{ fontWeight: 600, fontSize: 13, color: "#111827" }}>
                {new Date(application.submittedAt).toLocaleDateString("ru-KZ")}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* No application yet — spacer */}
      {!hasApplication && !isLoading && (
        <div style={{ marginTop: 24 }} />
      )}

      {isLoading ? (
        <div style={{ textAlign: "center", padding: "60px 0" }}>
          <Spin size="large" />
        </div>
      ) : (
        <Row gutter={24}>
          {/* ─── LEFT column: form or status ─── */}
          <Col xs={24} lg={14}>
            {/* Status card */}
            {hasApplication && !showForm && cfg && (
              <Card
                style={{
                  borderRadius: 12,
                  border: `1px solid ${cfg.border}`,
                  background: cfg.bg,
                  marginBottom: 20,
                }}
                styles={{ body: { padding: "24px 28px" } }}
              >
                <div style={{ display: "flex", gap: 16, alignItems: "flex-start" }}>
                  <div
                    style={{
                      width: 52,
                      height: 52,
                      borderRadius: 14,
                      background: "#fff",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      flexShrink: 0,
                      boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
                    }}
                  >
                    {cfg.icon}
                  </div>
                  <div style={{ flex: 1 }}>
                    <Title level={5} style={{ margin: "0 0 4px", color: cfg.color }}>
                      {cfg.title}
                    </Title>
                    <Paragraph style={{ margin: 0, color: "#374151", fontSize: 13 }}>
                      {cfg.description}
                    </Paragraph>
                  </div>
                </div>

                {application.status === "REJECTED" && application.rejectionReason && (
                  <Alert
                    type="error"
                    showIcon
                    icon={<AlertCircle size={14} />}
                    message="Причина отклонения"
                    description={application.rejectionReason}
                    style={{ marginTop: 16, borderRadius: 8 }}
                  />
                )}

                {application.status === "REJECTED" && (
                  <Button
                    type="primary"
                    style={{ background: "#0F2A44", borderRadius: 8, marginTop: 20 }}
                    icon={<Send size={14} />}
                    onClick={() => setShowForm(true)}
                  >
                    Подать заявку повторно
                  </Button>
                )}
              </Card>
            )}

            {/* Submit form */}
            {displayForm && (
              <Card
                style={{ borderRadius: 12, border: "1px solid #e5e7eb" }}
                styles={{ body: { padding: "28px 32px" } }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6 }}>
                  <FileText size={18} color="#0F2A44" />
                  <Title level={5} style={{ margin: 0, color: "#0F2A44" }}>
                    Данные для заявки
                  </Title>
                </div>
                <Text type="secondary" style={{ fontSize: 13, display: "block", marginBottom: 24 }}>
                  Введите номер адвокатской лицензии. После подачи заявка будет рассмотрена администратором.
                </Text>

                <Form form={form} layout="vertical" onFinish={handleSubmit}>
                  <Form.Item
                    name="licenseNum"
                    label={<span style={{ fontWeight: 500, color: "#374151" }}>Номер лицензии</span>}
                    rules={[
                      { required: true, message: "Введите номер лицензии" },
                      { pattern: /^KZ-ADV-\d{4}-\d{5}$/, message: "Формат: KZ-ADV-2024-00123" },
                    ]}
                    extra={<Text style={{ fontSize: 12, color: "#9ca3af" }}>Пример: KZ-ADV-2024-00123</Text>}
                  >
                    <Input
                      placeholder="KZ-ADV-2024-00123"
                      size="large"
                      style={{ fontFamily: "monospace", borderRadius: 8 }}
                      prefix={<FileText size={15} color="#9ca3af" />}
                    />
                  </Form.Item>

                  <div
                    style={{
                      background: "#f8fafc",
                      borderRadius: 10,
                      padding: "14px 18px",
                      marginBottom: 20,
                      border: "1px solid #e2e8f0",
                      display: "flex",
                      gap: 10,
                      alignItems: "flex-start",
                    }}
                  >
                    <Info size={15} color="#64748b" style={{ marginTop: 1, flexShrink: 0 }} />
                    <Text style={{ fontSize: 12, color: "#64748b" }}>
                      После отправки вы сможете отслеживать статус на этой странице.
                      Среднее время рассмотрения: <strong>1–3 рабочих дня</strong>.
                    </Text>
                  </div>

                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={isSubmitting}
                    size="large"
                    icon={<Send size={15} />}
                    style={{ background: "#0F2A44", borderRadius: 8, height: 44, fontWeight: 500, paddingInline: 28 }}
                  >
                    Отправить заявку
                  </Button>
                </Form>
              </Card>
            )}
          </Col>

          {/* ─── RIGHT column: info ─── */}
          <Col xs={24} lg={10}>
            {/* Process timeline */}
            <Card
              style={{ borderRadius: 12, border: "1px solid #e5e7eb", marginBottom: 16 }}
              styles={{ body: { padding: "24px 28px" } }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 20 }}>
                <CalendarDays size={16} color="#0F2A44" />
                <Title level={5} style={{ margin: 0, color: "#0F2A44" }}>
                  Как проходит верификация
                </Title>
              </div>
              <Timeline
                items={[
                  {
                    color: hasApplication ? "#059669" : "#0F2A44",
                    dot: <CircleCheck size={16} color={hasApplication ? "#059669" : "#0F2A44"} />,
                    children: (
                      <div>
                        <Text style={{ fontWeight: 600, fontSize: 13, color: "#111827" }}>
                          Подача заявки
                        </Text>
                        <Paragraph style={{ margin: "2px 0 0", fontSize: 12, color: "#6b7280" }}>
                          Вы вводите номер лицензии и отправляете заявку
                        </Paragraph>
                      </div>
                    ),
                  },
                  {
                    color: hasApplication && application.status !== "PENDING" ? "#059669" : "#9ca3af",
                    dot: <Clock3 size={16} color={hasApplication && application.status !== "PENDING" ? "#059669" : "#9ca3af"} />,
                    children: (
                      <div>
                        <Text style={{ fontWeight: 600, fontSize: 13, color: "#111827" }}>
                          Проверка администратором
                        </Text>
                        <Paragraph style={{ margin: "2px 0 0", fontSize: 12, color: "#6b7280" }}>
                          Администратор верифицирует лицензию в реестре адвокатов РК
                        </Paragraph>
                      </div>
                    ),
                  },
                  {
                    color: application?.status === "APPROVED" ? "#059669" : application?.status === "REJECTED" ? "#b91c1c" : "#9ca3af",
                    dot: application?.status === "APPROVED"
                      ? <BadgeCheck size={16} color="#059669" />
                      : application?.status === "REJECTED"
                        ? <XCircle size={16} color="#b91c1c" />
                        : <CheckCircle2 size={16} color="#9ca3af" />,
                    children: (
                      <div>
                        <Text style={{ fontWeight: 600, fontSize: 13, color: "#111827" }}>
                          Решение по заявке
                        </Text>
                        <Paragraph style={{ margin: "2px 0 0", fontSize: 12, color: "#6b7280" }}>
                          При одобрении вы получите статус адвоката в системе
                        </Paragraph>
                      </div>
                    ),
                  },
                ]}
              />
            </Card>

            {/* FAQ */}
            <Card
              style={{ borderRadius: 12, border: "1px solid #e5e7eb" }}
              styles={{ body: { padding: "24px 28px" } }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 20 }}>
                <MessageSquare size={16} color="#0F2A44" />
                <Title level={5} style={{ margin: 0, color: "#0F2A44" }}>
                  Часто задаваемые вопросы
                </Title>
              </div>
              {FAQ.map((item, i) => (
                <div key={i}>
                  {i > 0 && <Divider style={{ margin: "14px 0" }} />}
                  <Text style={{ fontWeight: 600, fontSize: 13, color: "#111827", display: "block", marginBottom: 4 }}>
                    {item.q}
                  </Text>
                  <Text style={{ fontSize: 12, color: "#6b7280" }}>{item.a}</Text>
                </div>
              ))}
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
}
