import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  App,
  Button,
  Card,
  Col,
  Divider,
  Form,
  Input,
  Row,
  Spin,
  Tag,
  Typography,
  Alert,
  Progress,
  Empty,
  Tooltip,
  Space,
} from "antd";
import {
  ArrowLeft,
  Save,
  CheckCircle2,
  AlertTriangle,
  Info,
  Lightbulb,
  FileSearch,
  Zap,
  ShieldAlert,
  FileCheck2,
  BrainCircuit,
} from "lucide-react";
import {
  useGetDocumentByIdQuery,
  useUpdateDocumentMutation,
  useCompleteDocumentMutation,
  useGetDocumentSuggestionsQuery,
  type AnalysisResult,
} from "../../../features/documents/api/document-api";
import { ROUTES } from "../../../app/router/router";

const { Title, Text, Paragraph } = Typography;

const STATUS_CONFIG = {
  DRAFT: { color: "orange", label: "Черновик" },
  COMPLETED: { color: "green", label: "Завершён" },
  PROCESSING: { color: "blue", label: "Обработка" },
};

const RISK_COLORS = {
  HIGH: { border: "#fecaca", bg: "#fef2f2", text: "#b91c1c", tag: "error" as const },
  MEDIUM: { border: "#fed7aa", bg: "#fff7ed", text: "#c2410c", tag: "warning" as const },
  LOW: { border: "#bbf7d0", bg: "#f0fdf4", text: "#15803d", tag: "success" as const },
};

function AnalysisResultBlock({ result }: { result: AnalysisResult }) {
  if (!result) return null;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      {/* AI Summary */}
      {result.aiSummary && (
        <Card
          size="small"
          style={{ borderRadius: 10, border: "1px solid #bfdbfe", background: "#eff6ff" }}
          styles={{ body: { padding: "16px 20px" } }}
        >
          <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
            <BrainCircuit size={18} color="#1d4ed8" style={{ flexShrink: 0, marginTop: 2 }} />
            <div>
              <Text strong style={{ color: "#1d4ed8", fontSize: 13, display: "block", marginBottom: 4 }}>
                AI Анализ
              </Text>
              <Paragraph style={{ margin: 0, fontSize: 13, color: "#1e3a8a" }}>
                {result.aiSummary}
              </Paragraph>
              {result.aiRecommendation && (
                <Paragraph style={{ margin: "8px 0 0", fontSize: 12, color: "#1e40af" }}>
                  <strong>Рекомендация:</strong> {result.aiRecommendation}
                </Paragraph>
              )}
              {result.aiIntentLabel && (
                <div style={{ marginTop: 8 }}>
                  <Tag color="blue">{result.aiIntentLabel}</Tag>
                  <Text style={{ fontSize: 11, color: "#6b7280", marginLeft: 6 }}>
                    Уверенность: {Math.round(result.aiIntentConfidence * 100)}%
                  </Text>
                  <Progress
                    percent={Math.round(result.aiIntentConfidence * 100)}
                    size="small"
                    style={{ marginTop: 4 }}
                    strokeColor="#1d4ed8"
                  />
                </div>
              )}
            </div>
          </div>
        </Card>
      )}

      {/* Aborted */}
      {result.aborted && (
        <Alert
          type="warning"
          showIcon
          message="Анализ прерван"
          description={result.abortReason}
          style={{ borderRadius: 10 }}
        />
      )}

      {/* Validation errors */}
      {result.validationErrors?.length > 0 && (
        <Card
          size="small"
          title={
            <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <AlertTriangle size={15} color="#b91c1c" />
              <Text style={{ fontSize: 13, color: "#b91c1c" }}>
                Ошибки валидации ({result.validationErrors.length})
              </Text>
            </span>
          }
          style={{ borderRadius: 10, borderColor: "#fecaca" }}
          styles={{ body: { padding: "12px 16px" } }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {result.validationErrors.map((e, i) => (
              <div
                key={i}
                style={{
                  padding: "8px 12px",
                  borderRadius: 6,
                  background: "#fef2f2",
                  border: "1px solid #fecaca",
                }}
              >
                <Text strong style={{ fontSize: 12, color: "#991b1b", display: "block" }}>
                  {e.label || e.fieldKey}
                </Text>
                <Text style={{ fontSize: 12, color: "#b91c1c" }}>{e.message}</Text>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Risks */}
      {result.risks?.length > 0 && (
        <Card
          size="small"
          title={
            <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <ShieldAlert size={15} color="#d97706" />
              <Text style={{ fontSize: 13, color: "#92400e" }}>
                Риски ({result.risks.length})
              </Text>
            </span>
          }
          style={{ borderRadius: 10, borderColor: "#fed7aa" }}
          styles={{ body: { padding: "12px 16px" } }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {result.risks.map((r, i) => {
              const cfg = RISK_COLORS[r.level] ?? RISK_COLORS.LOW;
              return (
                <div
                  key={i}
                  style={{
                    padding: "10px 14px",
                    borderRadius: 8,
                    background: cfg.bg,
                    border: `1px solid ${cfg.border}`,
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                    <Tag color={cfg.tag} style={{ margin: 0 }}>
                      {r.level}
                    </Tag>
                    <Text strong style={{ fontSize: 12, color: cfg.text }}>
                      {r.message}
                    </Text>
                  </div>
                  {r.aiExplanation && (
                    <Text style={{ fontSize: 11, color: "#6b7280" }}>{r.aiExplanation}</Text>
                  )}
                </div>
              );
            })}
          </div>
        </Card>
      )}

      {/* Field suggestions */}
      {result.fieldSuggestions?.length > 0 && (
        <Card
          size="small"
          title={
            <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <Lightbulb size={15} color="#0F2A44" />
              <Text style={{ fontSize: 13 }}>Предложения по полям</Text>
            </span>
          }
          style={{ borderRadius: 10 }}
          styles={{ body: { padding: "12px 16px" } }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {result.fieldSuggestions.map((s, i) => (
              <div
                key={i}
                style={{
                  padding: "8px 12px",
                  borderRadius: 6,
                  background: "#f8fafc",
                  border: "1px solid #e2e8f0",
                }}
              >
                <Text strong style={{ fontSize: 12, color: "#0F2A44", display: "block" }}>
                  {s.label || s.fieldKey}
                </Text>
                <Text style={{ fontSize: 12, color: "#374151" }}>
                  Предлагается: <code style={{ background: "#e2e8f0", padding: "1px 5px", borderRadius: 4 }}>{s.suggestedValue}</code>
                </Text>
                {s.reason && (
                  <Text style={{ fontSize: 11, color: "#6b7280", display: "block", marginTop: 2 }}>
                    {s.reason}
                  </Text>
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Matched templates */}
      {result.matchedTemplates?.length > 0 && (
        <Card
          size="small"
          title={
            <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <FileSearch size={15} color="#0F2A44" />
              <Text style={{ fontSize: 13 }}>Похожие шаблоны</Text>
            </span>
          }
          style={{ borderRadius: 10 }}
          styles={{ body: { padding: "12px 16px" } }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {result.matchedTemplates.map((t, i) => (
              <div
                key={i}
                style={{
                  padding: "8px 12px",
                  borderRadius: 6,
                  background: "#f8fafc",
                  border: "1px solid #e2e8f0",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <div>
                  <Text strong style={{ fontSize: 13, color: "#0F2A44" }}>{t.title}</Text>
                  <Text style={{ fontSize: 11, color: "#6b7280", display: "block" }}>
                    {t.categoryName}
                  </Text>
                  {t.aiNote && (
                    <Text style={{ fontSize: 11, color: "#374151" }}>{t.aiNote}</Text>
                  )}
                </div>
                <Tag color="blue" style={{ flexShrink: 0 }}>
                  {t.score}%
                </Tag>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Required documents */}
      {result.requiredDocuments?.length > 0 && (
        <Card
          size="small"
          title={
            <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <FileCheck2 size={15} color="#0F2A44" />
              <Text style={{ fontSize: 13 }}>Требуемые документы</Text>
            </span>
          }
          style={{ borderRadius: 10 }}
          styles={{ body: { padding: "12px 16px" } }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {result.requiredDocuments.map((d, i) => (
              <div
                key={i}
                style={{
                  padding: "8px 12px",
                  borderRadius: 6,
                  background: "#f8fafc",
                  border: "1px solid #e2e8f0",
                  display: "flex",
                  alignItems: "center",
                  gap: 10,
                }}
              >
                {d.mandatory && <Tag color="red" style={{ margin: 0, flexShrink: 0 }}>Обязательно</Tag>}
                <div>
                  <Text strong style={{ fontSize: 12, color: "#0F2A44" }}>{d.title}</Text>
                  <Text style={{ fontSize: 11, color: "#6b7280", display: "block" }}>{d.reason}</Text>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {result.valid && !result.aborted && result.risks?.length === 0 && result.validationErrors?.length === 0 && (
        <Alert
          type="success"
          showIcon
          icon={<CheckCircle2 size={16} />}
          message="Документ прошёл проверку без замечаний"
          style={{ borderRadius: 10 }}
        />
      )}
    </div>
  );
}

interface Props {
  documentId: number;
}

export default function DocumentDetailPanel({ documentId }: Props) {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const { data: doc, isLoading } = useGetDocumentByIdQuery(documentId);
  const { data: suggestions, isFetching: isFetchingSuggestions } =
    useGetDocumentSuggestionsQuery(documentId, { skip: !showSuggestions });
  const [updateDocument, { isLoading: isUpdating }] = useUpdateDocumentMutation();
  const [completeDocument, { isLoading: isCompleting }] = useCompleteDocumentMutation();

  useEffect(() => {
    if (doc) {
      const fieldMap: Record<string, string> = {};
      doc.fieldValues?.forEach((f) => { fieldMap[f.fieldKey] = f.fieldValue; });
      form.setFieldsValue({ title: doc.title, ...fieldMap });
    }
  }, [doc, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const { title, ...rest } = values;
      await updateDocument({ id: documentId, title, fieldValues: rest }).unwrap();
      message.success("Документ сохранён");
    } catch {
      message.error("Ошибка при сохранении");
    }
  };

  const handleComplete = async () => {
    try {
      const values = await form.validateFields();
      const { title, ...rest } = values;
      await updateDocument({ id: documentId, title, fieldValues: rest }).unwrap();
      const response = await completeDocument(documentId).unwrap();
      setAnalysisResult(response.result);
      message.success("Анализ завершён");
    } catch {
      message.error("Ошибка при завершении");
    }
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: "center", padding: "80px 0" }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!doc) return null;

  const statusCfg = STATUS_CONFIG[doc.status] ?? { color: "default", label: doc.status };

  return (
    <div>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
        <Button
          icon={<ArrowLeft size={16} />}
          onClick={() => navigate(ROUTES.DOCUMENTS)}
          style={{ borderRadius: 8 }}
        >
          Назад
        </Button>
        <div style={{ flex: 1 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <Title level={4} style={{ margin: 0, color: "#0F2A44" }}>
              {doc.title}
            </Title>
            <Tag color={statusCfg.color}>{statusCfg.label}</Tag>
          </div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {doc.templateTitle} · {doc.categoryName} · Обновлён{" "}
            {new Date(doc.updatedDate).toLocaleString("ru-KZ")}
          </Text>
        </div>
        <Space>
          <Button
            icon={<Info size={15} />}
            onClick={() => setShowSuggestions(!showSuggestions)}
            loading={isFetchingSuggestions}
            style={{ borderRadius: 8 }}
          >
            Предложения
          </Button>
          <Button
            icon={<Save size={15} />}
            onClick={handleSave}
            loading={isUpdating}
            style={{ borderRadius: 8 }}
          >
            Сохранить
          </Button>
          <Button
            type="primary"
            icon={<Zap size={15} />}
            onClick={handleComplete}
            loading={isCompleting}
            style={{ background: "#0F2A44", borderRadius: 8 }}
            disabled={doc.status === "COMPLETED"}
          >
            Завершить анализ
          </Button>
        </Space>
      </div>

      <Row gutter={24}>
        {/* Left: Form */}
        <Col xs={24} lg={analysisResult || showSuggestions ? 13 : 24}>
          <Card
            style={{ borderRadius: 12, border: "1px solid #e5e7eb" }}
            styles={{ body: { padding: "24px 28px" } }}
          >
            <Form form={form} layout="vertical">
              <Form.Item
                name="title"
                label={<Text strong style={{ color: "#374151" }}>Название документа</Text>}
                rules={[{ required: true, message: "Введите название" }]}
              >
                <Input style={{ borderRadius: 8 }} />
              </Form.Item>

              {doc.fieldValues?.length > 0 && (
                <>
                  <Divider style={{ margin: "8px 0 20px" }}>
                    <Text type="secondary" style={{ fontSize: 12 }}>Поля документа</Text>
                  </Divider>
                  {doc.fieldValues.map((field) => (
                    <Form.Item
                      key={field.id}
                      name={field.fieldKey}
                      label={
                        <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
                          <Text style={{ color: "#374151", fontSize: 13 }}>{field.fieldKey}</Text>
                          {doc.missingRequiredFields?.includes(field.fieldKey) && (
                            <Tooltip title="Обязательное поле не заполнено">
                              <AlertTriangle size={13} color="#d97706" />
                            </Tooltip>
                          )}
                        </span>
                      }
                    >
                      <Input style={{ borderRadius: 8, fontFamily: "inherit" }} />
                    </Form.Item>
                  ))}
                </>
              )}

              {doc.missingRequiredFields?.length > 0 && (
                <Alert
                  type="warning"
                  showIcon
                  message={`Незаполненные обязательные поля: ${doc.missingRequiredFields.join(", ")}`}
                  style={{ borderRadius: 8, marginTop: 8 }}
                />
              )}

              {(!doc.fieldValues || doc.fieldValues.length === 0) && (
                <Empty
                  description="Поля документа не заданы"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              )}
            </Form>
          </Card>
        </Col>

        {/* Right: Analysis / Suggestions */}
        {(analysisResult || showSuggestions) && (
          <Col xs={24} lg={11}>
            {analysisResult && (
              <div style={{ marginBottom: showSuggestions ? 16 : 0 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12 }}>
                  <Zap size={16} color="#0F2A44" />
                  <Text strong style={{ color: "#0F2A44" }}>Результат анализа</Text>
                </div>
                <AnalysisResultBlock result={analysisResult} />
              </div>
            )}

            {showSuggestions && suggestions && (
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12 }}>
                  <Lightbulb size={16} color="#0F2A44" />
                  <Text strong style={{ color: "#0F2A44" }}>Предложения</Text>
                </div>
                <AnalysisResultBlock result={suggestions} />
              </div>
            )}

            {showSuggestions && isFetchingSuggestions && (
              <div style={{ textAlign: "center", padding: "40px 0" }}>
                <Spin />
              </div>
            )}
          </Col>
        )}
      </Row>
    </div>
  );
}
