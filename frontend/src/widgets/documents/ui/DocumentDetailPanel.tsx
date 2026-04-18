import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import {
  App, Form, Input, Spin, Typography, Alert, Progress, Empty, Tooltip, Divider,
} from "antd";
import {
  ArrowLeft, Save, CheckCircle2, AlertTriangle, Lightbulb,
  FileSearch, Zap, ShieldAlert, FileCheck2, BrainCircuit,
  FileText, Clock, FilePen, Download, Share2, Archive,
  RotateCcw, ShieldCheck, Copy, Check, History,
} from "lucide-react";
import {
  useGetDocumentByIdQuery,
  useUpdateDocumentMutation,
  useCompleteDocumentMutation,
  useValidateDocumentMutation,
  useArchiveDocumentMutation,
  useRestoreDocumentMutation,
  useShareDocumentMutation,
  useLazyGetDocumentUrlQuery,
  useGetDocumentVersionsQuery,
  useGetDocumentSuggestionsQuery,
  type AnalysisResult,
  type DocumentVersion,
} from "../../../features/documents/api/document-api";
import { useGetTemplateByIdQuery } from "../../../features/templates/api/user-template-api";
import type { TemplateField } from "../../../features/lawyer/api/lawyer-template-api";
import { useExplainClauseMutation, type ClauseExplainResponse } from "../../../features/ai/api/ai-api";
import { ROUTES } from "../../../app/router/router";

const { Text, Paragraph } = Typography;

// ─── Status pill ──────────────────────────────────────────────
function StatusPill({ status }: { status: string }) {
  const cfg =
    status === "COMPLETED"  ? { color: "#059669", bg: "rgba(5,150,105,0.12)",   border: "rgba(5,150,105,0.3)",   dot: "#059669", label: "Завершён"     } :
    status === "VALIDATED"  ? { color: "#7c3aed", bg: "rgba(124,58,237,0.12)",  border: "rgba(124,58,237,0.3)",  dot: "#7c3aed", label: "Проверен"     } :
    status === "PROCESSING" ? { color: "#1677ff", bg: "rgba(22,119,255,0.12)",  border: "rgba(22,119,255,0.3)",  dot: "#1677ff", label: "В обработке"  } :
    status === "ARCHIVED"   ? { color: "#6b7280", bg: "rgba(107,114,128,0.12)", border: "rgba(107,114,128,0.3)", dot: "#6b7280", label: "Архив"         } :
                              { color: "#f59e0b", bg: "rgba(245,158,11,0.12)",  border: "rgba(245,158,11,0.3)",  dot: "#f59e0b", label: "Черновик"     };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "4px 12px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: cfg.dot }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color, letterSpacing: "0.04em" }}>{cfg.label}</span>
    </div>
  );
}

// ─── Risk pill ────────────────────────────────────────────────
function RiskPill({ level }: { level: string }) {
  const cfg =
    level === "HIGH"   ? { color: "#ef4444", bg: "rgba(239,68,68,0.08)",   label: "Высокий" } :
    level === "MEDIUM" ? { color: "#f59e0b", bg: "rgba(245,158,11,0.08)",  label: "Средний" } :
                         { color: "#059669", bg: "rgba(5,150,105,0.08)",   label: "Низкий"  };
  return <span style={{ fontSize: 10, fontWeight: 700, color: cfg.color, background: cfg.bg, padding: "2px 8px", borderRadius: 20 }}>{cfg.label}</span>;
}

// ─── Hero action button ───────────────────────────────────────
function HeroBtn({
  icon, label, onClick, disabled = false,
  variant = "ghost",
}: {
  icon: React.ReactNode; label: string; onClick?: () => void;
  disabled?: boolean; variant?: "ghost" | "success" | "danger" | "primary";
}) {
  const styles: Record<string, React.CSSProperties> = {
    ghost:   { background: "rgba(255,255,255,0.08)", border: "1px solid rgba(255,255,255,0.2)",  color: "rgba(255,255,255,0.85)" },
    success: { background: "rgba(5,150,105,0.7)",   border: "1px solid rgba(5,150,105,0.8)",    color: "#fff" },
    danger:  { background: "rgba(239,68,68,0.6)",   border: "1px solid rgba(239,68,68,0.8)",    color: "#fff" },
    primary: { background: "rgba(124,58,237,0.7)",  border: "1px solid rgba(124,58,237,0.8)",   color: "#fff" },
  };
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        display: "flex", alignItems: "center", gap: 6,
        ...styles[variant],
        borderRadius: 8, padding: "7px 14px",
        cursor: disabled ? "not-allowed" : "pointer",
        fontSize: 13, fontWeight: 600,
        opacity: disabled ? 0.6 : 1,
        transition: "opacity 0.15s",
      }}
    >
      {icon}{label}
    </button>
  );
}

// ─── Analysis block ───────────────────────────────────────────
function AnalysisBlock({ result }: { result: AnalysisResult }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {result.aiSummary && (
        <div style={{ background: "rgba(22,119,255,0.06)", borderRadius: 12, padding: "16px 18px", border: "1px solid rgba(22,119,255,0.15)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
            <BrainCircuit size={16} color="#1677ff" />
            <Text strong style={{ color: "#1677ff", fontSize: 12 }}>AI Анализ</Text>
            {result.aiIntentLabel && (
              <span style={{ fontSize: 10, fontWeight: 700, color: "#1677ff", background: "rgba(22,119,255,0.12)", padding: "1px 8px", borderRadius: 20, marginLeft: "auto" }}>
                {result.aiIntentLabel}
              </span>
            )}
          </div>
          <Paragraph style={{ margin: 0, fontSize: 13, color: "#1e3a8a", lineHeight: 1.6 }}>{result.aiSummary}</Paragraph>
          {result.aiRecommendation && (
            <Paragraph style={{ margin: "8px 0 0", fontSize: 12, color: "#1e40af" }}>
              <strong>Рекомендация:</strong> {result.aiRecommendation}
            </Paragraph>
          )}
          {result.aiIntentConfidence > 0 && (
            <div style={{ marginTop: 10 }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
                <Text style={{ fontSize: 11, color: "#94a3b8" }}>Уверенность AI</Text>
                <Text style={{ fontSize: 11, fontWeight: 700, color: "#1677ff" }}>{Math.round(result.aiIntentConfidence * 100)}%</Text>
              </div>
              <Progress percent={Math.round(result.aiIntentConfidence * 100)} size="small" showInfo={false} strokeColor="#1677ff" />
            </div>
          )}
        </div>
      )}

      {result.aborted && (
        <Alert type="warning" showIcon message="Анализ прерван" description={result.abortReason} style={{ borderRadius: 10 }} />
      )}

      {result.valid && !result.aborted && !result.risks?.length && !result.validationErrors?.length && (
        <div style={{ background: "rgba(5,150,105,0.06)", borderRadius: 12, padding: "14px 18px", border: "1px solid rgba(5,150,105,0.2)", display: "flex", alignItems: "center", gap: 10 }}>
          <CheckCircle2 size={18} color="#059669" />
          <Text style={{ fontSize: 13, color: "#059669", fontWeight: 600 }}>Документ прошёл проверку без замечаний</Text>
        </div>
      )}

      {result.validationErrors?.length > 0 && (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid rgba(239,68,68,0.2)", overflow: "hidden" }}>
          <div style={{ background: "rgba(239,68,68,0.05)", padding: "10px 16px", borderBottom: "1px solid rgba(239,68,68,0.12)", display: "flex", alignItems: "center", gap: 8 }}>
            <AlertTriangle size={14} color="#ef4444" />
            <Text style={{ fontSize: 12, fontWeight: 700, color: "#ef4444" }}>Ошибки валидации ({result.validationErrors.length})</Text>
          </div>
          <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 6 }}>
            {result.validationErrors.map((e, i) => (
              <div key={i} style={{ padding: "8px 12px", borderRadius: 8, background: "rgba(239,68,68,0.04)", border: "1px solid rgba(239,68,68,0.12)" }}>
                <Text strong style={{ fontSize: 12, color: "#991b1b", display: "block" }}>{e.label || e.fieldKey}</Text>
                <Text style={{ fontSize: 12, color: "#dc2626" }}>{e.message}</Text>
              </div>
            ))}
          </div>
        </div>
      )}

      {result.risks?.length > 0 && (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid rgba(245,158,11,0.2)", overflow: "hidden" }}>
          <div style={{ background: "rgba(245,158,11,0.05)", padding: "10px 16px", borderBottom: "1px solid rgba(245,158,11,0.12)", display: "flex", alignItems: "center", gap: 8 }}>
            <ShieldAlert size={14} color="#f59e0b" />
            <Text style={{ fontSize: 12, fontWeight: 700, color: "#92400e" }}>Правовые риски ({result.risks.length})</Text>
          </div>
          <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 8 }}>
            {result.risks.map((r, i) => (
              <div key={i} style={{ padding: "10px 14px", borderRadius: 8, background: "#f8fafc", border: "1px solid #f1f5f9" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                  <RiskPill level={r.level} />
                  <Text strong style={{ fontSize: 12, color: "#0b1c30" }}>{r.message}</Text>
                </div>
                {r.aiExplanation && <Text style={{ fontSize: 11, color: "#64748b" }}>{r.aiExplanation}</Text>}
              </div>
            ))}
          </div>
        </div>
      )}

      {result.fieldSuggestions?.length > 0 && (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid rgba(15,42,68,0.12)", overflow: "hidden" }}>
          <div style={{ background: "rgba(15,42,68,0.03)", padding: "10px 16px", borderBottom: "1px solid rgba(15,42,68,0.08)", display: "flex", alignItems: "center", gap: 8 }}>
            <Lightbulb size={14} color="#0F2A44" />
            <Text style={{ fontSize: 12, fontWeight: 700, color: "#0F2A44" }}>Предложения по полям ({result.fieldSuggestions.length})</Text>
          </div>
          <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 6 }}>
            {result.fieldSuggestions.map((s, i) => (
              <div key={i} style={{ padding: "8px 12px", borderRadius: 8, background: "#f8fafc", border: "1px solid #f1f5f9" }}>
                <Text strong style={{ fontSize: 12, color: "#0F2A44", display: "block" }}>{s.label || s.fieldKey}</Text>
                <Text style={{ fontSize: 12, color: "#374151" }}>
                  Предлагается: <span style={{ fontFamily: "monospace", background: "#e2e8f0", padding: "1px 5px", borderRadius: 4, fontSize: 11 }}>{s.suggestedValue}</span>
                </Text>
                {s.reason && <Text style={{ fontSize: 11, color: "#64748b", display: "block", marginTop: 2 }}>{s.reason}</Text>}
              </div>
            ))}
          </div>
        </div>
      )}

      {result.matchedTemplates?.length > 0 && (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid rgba(15,42,68,0.12)", overflow: "hidden" }}>
          <div style={{ background: "rgba(15,42,68,0.03)", padding: "10px 16px", borderBottom: "1px solid rgba(15,42,68,0.08)", display: "flex", alignItems: "center", gap: 8 }}>
            <FileSearch size={14} color="#0F2A44" />
            <Text style={{ fontSize: 12, fontWeight: 700, color: "#0F2A44" }}>Похожие шаблоны</Text>
          </div>
          <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 6 }}>
            {result.matchedTemplates.map((t, i) => (
              <div key={i} style={{ padding: "8px 12px", borderRadius: 8, background: "#f8fafc", border: "1px solid #f1f5f9", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <Text strong style={{ fontSize: 12, color: "#0F2A44" }}>{t.title}</Text>
                  <Text style={{ fontSize: 11, color: "#94a3b8", display: "block" }}>{t.categoryName}</Text>
                  {t.aiNote && <Text style={{ fontSize: 11, color: "#64748b" }}>{t.aiNote}</Text>}
                </div>
                <span style={{ fontSize: 12, fontWeight: 700, color: "#1677ff", background: "rgba(22,119,255,0.08)", padding: "3px 10px", borderRadius: 20, flexShrink: 0 }}>{t.score}%</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {result.requiredDocuments?.length > 0 && (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid rgba(15,42,68,0.12)", overflow: "hidden" }}>
          <div style={{ background: "rgba(15,42,68,0.03)", padding: "10px 16px", borderBottom: "1px solid rgba(15,42,68,0.08)", display: "flex", alignItems: "center", gap: 8 }}>
            <FileCheck2 size={14} color="#0F2A44" />
            <Text style={{ fontSize: 12, fontWeight: 700, color: "#0F2A44" }}>Требуемые документы</Text>
          </div>
          <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 6 }}>
            {result.requiredDocuments.map((d, i) => (
              <div key={i} style={{ padding: "8px 12px", borderRadius: 8, background: "#f8fafc", border: "1px solid #f1f5f9", display: "flex", alignItems: "flex-start", gap: 10 }}>
                {d.mandatory && <span style={{ fontSize: 10, fontWeight: 700, color: "#ef4444", background: "rgba(239,68,68,0.08)", padding: "2px 8px", borderRadius: 20, flexShrink: 0, marginTop: 2 }}>Обязательно</span>}
                <div>
                  <Text strong style={{ fontSize: 12, color: "#0F2A44" }}>{d.title}</Text>
                  <Text style={{ fontSize: 11, color: "#94a3b8", display: "block" }}>{d.reason}</Text>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Share modal ─────────────────────────────────────────────
function ShareModal({ url, onClose }: { url: string; onClose: () => void }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(url).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  return (
    <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
      <div style={{ background: "#fff", borderRadius: 16, width: 480, boxShadow: "0 24px 64px rgba(0,0,0,0.2)", overflow: "hidden" }}>
        {/* Header */}
        <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", padding: "20px 24px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <Share2 size={18} color="#fff" />
            <span style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>Поделиться документом</span>
          </div>
          <p style={{ color: "rgba(255,255,255,0.65)", fontSize: 12, margin: "6px 0 0" }}>
            Ссылка действительна ограниченное время. Получатель сможет скачать PDF без авторизации.
          </p>
        </div>
        {/* Body */}
        <div style={{ padding: "24px" }}>
          <div style={{ display: "flex", gap: 8 }}>
            <div style={{
              flex: 1, background: "#f8fafc", border: "1px solid #e2e8f0", borderRadius: 8,
              padding: "10px 14px", fontSize: 12, color: "#374151",
              overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap",
            }}>
              {url}
            </div>
            <button
              onClick={handleCopy}
              style={{
                background: copied ? "#059669" : "#0F2A44",
                color: "#fff", border: "none", borderRadius: 8,
                padding: "10px 16px", cursor: "pointer", fontSize: 13,
                fontWeight: 600, display: "flex", alignItems: "center", gap: 6,
                transition: "background 0.2s", flexShrink: 0,
              }}
            >
              {copied ? <><Check size={14} />Скопировано</> : <><Copy size={14} />Копировать</>}
            </button>
          </div>
          <div style={{ marginTop: 20, display: "flex", justifyContent: "flex-end" }}>
            <button onClick={onClose} style={{ background: "transparent", border: "1px solid #e5e7eb", borderRadius: 8, padding: "8px 20px", cursor: "pointer", color: "#6b7280", fontSize: 13 }}>
              Закрыть
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── AI Clause Explain modal ──────────────────────────────────
function ClauseExplainModal({ onClose }: { onClose: () => void }) {
  const { message } = App.useApp();
  const [clause, setClause] = useState("");
  const [context, setContext] = useState("");
  const [result, setResult] = useState<ClauseExplainResponse | null>(null);
  const [explainClause, { isLoading }] = useExplainClauseMutation();

  const handleExplain = async () => {
    if (!clause.trim()) { message.warning("Введите текст клаузы"); return; }
    try {
      const res = await explainClause({ clause, context: context || undefined }).unwrap();
      setResult(res);
    } catch { message.error("Ошибка при объяснении клаузы"); }
  };

  return (
    <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
      <div style={{ background: "#fff", borderRadius: 16, width: 580, maxHeight: "85vh", overflowY: "auto", boxShadow: "0 24px 64px rgba(0,0,0,0.2)", overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {/* Header */}
        <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", padding: "20px 24px", flexShrink: 0 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <BrainCircuit size={18} color="#fff" />
            <span style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>AI Объяснение клаузы</span>
          </div>
          <p style={{ color: "rgba(255,255,255,0.65)", fontSize: 12, margin: "6px 0 0" }}>
            Вставьте юридический текст — AI объяснит простыми словами и выявит риски
          </p>
        </div>
        {/* Body */}
        <div style={{ padding: "24px", overflowY: "auto", flex: 1 }}>
          <div style={{ marginBottom: 14 }}>
            <div style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#94a3b8", marginBottom: 6 }}>Текст клаузы *</div>
            <textarea
              value={clause}
              onChange={e => setClause(e.target.value)}
              placeholder="Вставьте текст юридической клаузы или пункта договора..."
              rows={5}
              style={{ width: "100%", boxSizing: "border-box", border: "1px solid #e2e8f0", borderRadius: 10, padding: "10px 14px", fontSize: 13, resize: "vertical", outline: "none", fontFamily: "inherit", color: "#111827" }}
            />
          </div>
          <div style={{ marginBottom: 20 }}>
            <div style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#94a3b8", marginBottom: 6 }}>Контекст (необязательно)</div>
            <input
              value={context}
              onChange={e => setContext(e.target.value)}
              placeholder="Например: договор аренды, Казахстан, 2024 год"
              style={{ width: "100%", boxSizing: "border-box", border: "1px solid #e2e8f0", borderRadius: 10, padding: "10px 14px", fontSize: 13, outline: "none", fontFamily: "inherit", color: "#111827" }}
            />
          </div>

          {result && (
            <div style={{ display: "flex", flexDirection: "column", gap: 12, marginBottom: 20 }}>
              <div style={{ background: "rgba(22,119,255,0.05)", borderRadius: 12, padding: "16px 18px", border: "1px solid rgba(22,119,255,0.15)" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
                  <BrainCircuit size={14} color="#1677ff" />
                  <span style={{ fontSize: 12, fontWeight: 700, color: "#1677ff" }}>Объяснение</span>
                </div>
                <p style={{ fontSize: 13, color: "#1e3a8a", lineHeight: 1.6, margin: 0 }}>{result.explanation}</p>
              </div>

              <div style={{ background: "rgba(5,150,105,0.05)", borderRadius: 12, padding: "16px 18px", border: "1px solid rgba(5,150,105,0.15)" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>
                  <CheckCircle2 size={14} color="#059669" />
                  <span style={{ fontSize: 12, fontWeight: 700, color: "#059669" }}>Простыми словами</span>
                </div>
                <p style={{ fontSize: 13, color: "#065f46", lineHeight: 1.6, margin: 0 }}>{result.simplifiedText}</p>
              </div>

              {result.risks.length > 0 && (
                <div style={{ background: "rgba(245,158,11,0.05)", borderRadius: 12, padding: "16px 18px", border: "1px solid rgba(245,158,11,0.2)" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                    <ShieldAlert size={14} color="#f59e0b" />
                    <span style={{ fontSize: 12, fontWeight: 700, color: "#92400e" }}>Риски ({result.risks.length})</span>
                  </div>
                  {result.risks.map((r, i) => (
                    <div key={i} style={{ fontSize: 12, color: "#92400e", marginBottom: 4 }}>• {r}</div>
                  ))}
                </div>
              )}

              {result.recommendations.length > 0 && (
                <div style={{ background: "rgba(15,42,68,0.04)", borderRadius: 12, padding: "16px 18px", border: "1px solid rgba(15,42,68,0.1)" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                    <Lightbulb size={14} color="#0F2A44" />
                    <span style={{ fontSize: 12, fontWeight: 700, color: "#0F2A44" }}>Рекомендации</span>
                  </div>
                  {result.recommendations.map((r, i) => (
                    <div key={i} style={{ fontSize: 12, color: "#374151", marginBottom: 4 }}>• {r}</div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
            <button onClick={onClose} style={{ background: "transparent", border: "1px solid #e5e7eb", borderRadius: 8, padding: "9px 20px", cursor: "pointer", color: "#6b7280", fontSize: 13 }}>
              Закрыть
            </button>
            <button onClick={handleExplain} disabled={isLoading || !clause.trim()}
              style={{ display: "flex", alignItems: "center", gap: 6, background: isLoading || !clause.trim() ? "#94a3b8" : "#0F2A44", color: "#fff", border: "none", borderRadius: 8, padding: "9px 20px", cursor: isLoading || !clause.trim() ? "not-allowed" : "pointer", fontSize: 13, fontWeight: 700 }}>
              <BrainCircuit size={14} />{isLoading ? "Анализ..." : "Объяснить"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Versions panel ───────────────────────────────────────────
function VersionsPanel({ versions, isLoading }: { versions: DocumentVersion[]; isLoading: boolean }) {
  if (isLoading) return <div style={{ textAlign: "center", padding: "20px 0" }}><Spin size="small" /></div>;
  if (!versions.length) return <Text style={{ fontSize: 13, color: "#94a3b8" }}>Версии недоступны</Text>;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      {versions.map((v) => (
        <div key={v.version} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "10px 14px", borderRadius: 8, background: "#f8fafc", border: "1px solid #f1f5f9" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ fontSize: 11, fontWeight: 700, color: "#0F2A44", background: "rgba(15,42,68,0.08)", padding: "2px 8px", borderRadius: 20 }}>v{v.version}</span>
            <Text style={{ fontSize: 12, color: "#64748b" }}>{new Date(v.createdDate).toLocaleString("ru-KZ")}</Text>
          </div>
        </div>
      ))}
    </div>
  );
}

// ─── Main panel ───────────────────────────────────────────────
interface Props { documentId: number }

export default function DocumentDetailPanel({ documentId }: Props) {
  const isMobile = useIsMobile();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [showVersions, setShowVersions] = useState(false);
  const [shareUrl, setShareUrl] = useState<string | null>(null);
  const [showExplainModal, setShowExplainModal] = useState(false);

  const { data: doc, isLoading } = useGetDocumentByIdQuery(documentId);
  const { data: template } = useGetTemplateByIdQuery(doc?.templateId ?? 0, { skip: !doc?.templateId });
  const { data: suggestions, isFetching: isFetchingSuggestions } = useGetDocumentSuggestionsQuery(documentId, { skip: !showSuggestions });
  const { data: versions = [], isLoading: versionsLoading } = useGetDocumentVersionsQuery(documentId, { skip: !showVersions });

  const [updateDocument,   { isLoading: isUpdating }]   = useUpdateDocumentMutation();
  const [completeDocument, { isLoading: isCompleting }] = useCompleteDocumentMutation();
  const [validateDocument, { isLoading: isValidating }] = useValidateDocumentMutation();
  const [archiveDocument,  { isLoading: isArchiving }]  = useArchiveDocumentMutation();
  const [restoreDocument,  { isLoading: isRestoring }]  = useRestoreDocumentMutation();
  const [shareDocument,    { isLoading: isSharing }]    = useShareDocumentMutation();
  const [triggerGetUrl] = useLazyGetDocumentUrlQuery();

  // Template fields sorted by orderNum; fall back to fields derived from doc.fieldValues
  const templateFields: TemplateField[] = template?.fields
    ? [...template.fields].sort((a, b) => a.orderNum - b.orderNum)
    : (doc?.fieldValues ?? []).map((fv) => ({
        id: fv.id,
        fieldKey: fv.fieldKey,
        label: fv.fieldKey,
        fieldType: "TEXT" as const,
        required: false,
        orderNum: fv.id,
      }));

  useEffect(() => {
    if (doc) {
      const fieldMap: Record<string, string> = {};
      doc.fieldValues?.forEach(f => { fieldMap[f.fieldKey] = f.fieldValue; });
      form.setFieldsValue({ title: doc.title, ...fieldMap });
    }
  }, [doc, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const { title, ...rest } = values;
      await updateDocument({ id: documentId, title, fieldValues: rest }).unwrap();
      message.success("Документ сохранён");
    } catch { message.error("Ошибка при сохранении"); }
  };

  const handleValidate = async () => {
    try {
      const values = await form.validateFields();
      const { title, ...rest } = values;
      await updateDocument({ id: documentId, title, fieldValues: rest }).unwrap();
      const result = await validateDocument(documentId).unwrap();
      setAnalysisResult(result);
      if (result.valid) message.success("Документ прошёл валидацию");
      else message.warning("Найдены ошибки валидации");
    } catch { message.error("Ошибка при валидации"); }
  };

  const handleComplete = async () => {
    try {
      const values = await form.validateFields();
      const { title, ...rest } = values;
      await updateDocument({ id: documentId, title, fieldValues: rest }).unwrap();
      const response = await completeDocument(documentId).unwrap();
      setAnalysisResult(response.result);
      message.success("Документ завершён, PDF создан");
    } catch { message.error("Ошибка при завершении"); }
  };

  const handleDownload = async () => {
    try {
      const result = await triggerGetUrl(documentId).unwrap();
      window.open(result.url, "_blank");
    } catch { message.error("Не удалось получить ссылку для скачивания"); }
  };

  const handleShare = async () => {
    try {
      const result = await shareDocument(documentId).unwrap();
      setShareUrl(result.shareUrl);
    } catch { message.error("Не удалось создать ссылку для шаринга"); }
  };

  const handleArchive = async () => {
    try {
      await archiveDocument(documentId).unwrap();
      message.success("Документ архивирован");
    } catch { message.error("Ошибка при архивировании"); }
  };

  const handleRestore = async () => {
    try {
      await restoreDocument(documentId).unwrap();
      message.success("Документ восстановлен в черновик");
    } catch { message.error("Ошибка при восстановлении"); }
  };

  if (isLoading) {
    return (
      <div style={{ overflowX: "hidden" }}>
        <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", height: 140 }} />
        <div style={{ padding: "32px" }}>
          <Spin size="large" style={{ display: "block", margin: "80px auto" }} />
        </div>
      </div>
    );
  }

  if (!doc) return null;

  const isDraft     = doc.status === "DRAFT" || doc.status === "VALIDATED";
  const isCompleted = doc.status === "COMPLETED";
  const isArchived  = doc.status === "ARCHIVED";
  const hasRightPanel = !!(analysisResult || showSuggestions || showVersions);

  return (
    <div style={{ overflowX: "hidden" }}>
      {shareUrl && <ShareModal url={shareUrl} onClose={() => setShareUrl(null)} />}
      {showExplainModal && <ClauseExplainModal onClose={() => setShowExplainModal(false)} />}

      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: isMobile ? "20px 16px 40px" : "28px 40px 48px", position: "relative", zIndex: 1 }}>
          {/* Back + actions row */}
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 20, flexWrap: "wrap" }}>
            <HeroBtn icon={<ArrowLeft size={14} />} label="Назад" onClick={() => navigate(ROUTES.DOCUMENTS)} />
            <div style={{ flex: 1 }} />

            {/* DRAFT / VALIDATED */}
            {isDraft && <>
              <HeroBtn icon={<Lightbulb size={14} />} label={isFetchingSuggestions ? "Загрузка..." : "Предложения AI"} onClick={() => setShowSuggestions(!showSuggestions)} />
              <HeroBtn icon={<Save size={14} />} label={isUpdating ? "Сохранение..." : "Сохранить"} onClick={handleSave} disabled={isUpdating} />
              <HeroBtn icon={<ShieldCheck size={14} />} label={isValidating ? "Проверка..." : "Валидировать"} onClick={handleValidate} disabled={isValidating} variant="primary" />
              <HeroBtn icon={<Zap size={14} />} label={isCompleting ? "Анализ..." : "Завершить"} onClick={handleComplete} disabled={isCompleting} variant="success" />
            </>}

            {/* COMPLETED */}
            {isCompleted && <>
              <HeroBtn icon={<BrainCircuit size={14} />} label="AI Объяснение" onClick={() => setShowExplainModal(true)} variant="primary" />
              <HeroBtn icon={<History size={14} />} label="Версии" onClick={() => setShowVersions(!showVersions)} />
              <HeroBtn icon={<Download size={14} />} label="Скачать PDF" onClick={handleDownload} />
              <HeroBtn icon={<Share2 size={14} />} label={isSharing ? "Создание..." : "Поделиться"} onClick={handleShare} disabled={isSharing} variant="primary" />
              <HeroBtn icon={<Archive size={14} />} label={isArchiving ? "Архивирование..." : "Архивировать"} onClick={handleArchive} disabled={isArchiving} variant="danger" />
            </>}

            {/* ARCHIVED */}
            {isArchived && <>
              <HeroBtn icon={<Download size={14} />} label="Скачать PDF" onClick={handleDownload} />
              <HeroBtn icon={<RotateCcw size={14} />} label={isRestoring ? "Восстановление..." : "Восстановить"} onClick={handleRestore} disabled={isRestoring} variant="success" />
            </>}
          </div>

          {/* Title + meta */}
          <div style={{ display: "flex", alignItems: "flex-end", gap: 16 }}>
            <div style={{ width: 48, height: 48, borderRadius: 12, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
              <FileText size={22} color="#fff" />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <h1 style={{ fontSize: 28, fontWeight: 800, color: "#fff", margin: "0 0 6px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.01em", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                {doc.title}
              </h1>
              <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
                <StatusPill status={doc.status} />
                <span style={{ fontSize: 12, color: "rgba(186,213,255,0.7)" }}>{doc.templateTitle}</span>
                {doc.categoryName && <span style={{ fontSize: 12, color: "rgba(186,213,255,0.5)" }}>· {doc.categoryName}</span>}
                {doc.updatedDate && <span style={{ fontSize: 12, color: "rgba(186,213,255,0.5)" }}>· обновлён {new Date(doc.updatedDate).toLocaleDateString("ru-KZ")}</span>}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 16px 24px" : "0 32px 32px" }}>
        {/* ── Stat pills ── */}
        <div style={{ display: "flex", gap: 16, marginTop: -20, marginBottom: 24, position: "relative", zIndex: 2, flexWrap: "wrap" }}>
          {[
            { icon: <FilePen size={16} />,       label: "Полей",         value: templateFields.length,                                                    color: "#0F2A44" },
            { icon: <AlertTriangle size={16} />, label: "Незаполненных", value: doc.missingRequiredFields?.length ?? 0,                                    color: doc.missingRequiredFields?.length ? "#f59e0b" : "#059669" },
            { icon: <Clock size={16} />,         label: "Обновлён",      value: doc.updatedDate ? new Date(doc.updatedDate).toLocaleDateString("ru-KZ") : "—", color: "#64748b" },
          ].map(item => (
            <div key={item.label} style={{ background: "#fff", borderRadius: 10, padding: "12px 18px", boxShadow: "0 4px 16px rgba(11,28,48,0.06)", display: "flex", alignItems: "center", gap: 10 }}>
              <span style={{ color: item.color }}>{item.icon}</span>
              <div>
                <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#94a3b8" }}>{item.label}</div>
                <div style={{ fontSize: 16, fontWeight: 800, color: item.color, lineHeight: 1.2, fontFamily: "Manrope, sans-serif" }}>{item.value}</div>
              </div>
            </div>
          ))}
        </div>

        {/* ── Main content grid ── */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : (hasRightPanel ? "1fr 420px" : "1fr"), gap: 24, alignItems: "start" }}>
          {/* Left: Form */}
          <div style={{ background: "#fff", borderRadius: 16, padding: "28px 32px", boxShadow: "0 8px 32px rgba(11,28,48,0.04)", border: "1px solid rgba(197,198,210,0.15)" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
              <div style={{ width: 32, height: 32, borderRadius: 8, background: "rgba(15,42,68,0.06)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <FilePen size={15} color="#0F2A44" />
              </div>
              <Text strong style={{ fontSize: 15, color: "#0b1c30", fontFamily: "Manrope, sans-serif" }}>Поля документа</Text>
              {!isDraft && (
                <span style={{ marginLeft: "auto", fontSize: 11, color: "#94a3b8", fontStyle: "italic" }}>
                  {isArchived ? "Документ в архиве — только чтение" : "Документ завершён — только чтение"}
                </span>
              )}
            </div>
            <Form form={form} layout="vertical">
              <Form.Item name="title"
                label={<span style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#94a3b8" }}>Название документа</span>}
                rules={[{ required: true, message: "Введите название" }]}>
                <Input disabled={!isDraft} style={{ borderRadius: 10, height: 44, background: isDraft ? "#fff" : "#f8fafc", border: isDraft ? "1px solid #d1d5db" : "1px solid #f1f5f9", color: isDraft ? "#0b1c30" : "#64748b" }} />
              </Form.Item>

              {templateFields.length > 0 && (
                <>
                  <Divider style={{ margin: "8px 0 20px" }}>
                    <span style={{ fontSize: 11, color: "#94a3b8", fontWeight: 600 }}>Поля шаблона</span>
                  </Divider>
                  <div style={{ display: "grid", gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr", gap: isMobile ? 0 : "0 24px" }}>
                    {templateFields.map(field => {
                      const isMissing = doc.missingRequiredFields?.includes(field.fieldKey);
                      return (
                        <Form.Item
                          key={field.fieldKey}
                          name={field.fieldKey}
                          rules={field.required ? [{ required: true, message: `Заполните "${field.label}"` }] : undefined}
                          label={
                            <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
                              <span style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: isMissing ? "#f59e0b" : "#94a3b8" }}>
                                {field.label || field.fieldKey}
                              </span>
                              {field.required && <span style={{ color: "#ef4444", fontSize: 11 }}>*</span>}
                              {isMissing && <Tooltip title="Обязательное поле не заполнено"><AlertTriangle size={12} color="#f59e0b" /></Tooltip>}
                            </span>
                          }
                        >
                          <Input
                            disabled={!isDraft}
                            type={field.fieldType === "NUMBER" ? "number" : "text"}
                            placeholder={field.fieldType === "DATE" ? "ГГГГ-ММ-ДД" : undefined}
                            style={{ borderRadius: 10, height: 40, background: isDraft ? "#fff" : "#f8fafc", border: isMissing ? "1px solid #f59e0b" : isDraft ? "1px solid #d1d5db" : "1px solid #f1f5f9" }}
                          />
                        </Form.Item>
                      );
                    })}
                  </div>
                </>
              )}

              {templateFields.length === 0 && (
                <Empty description="Поля шаблона не определены" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}

              {doc.missingRequiredFields?.length > 0 && (
                <div style={{ marginTop: 8, background: "rgba(245,158,11,0.06)", borderRadius: 10, padding: "10px 16px", border: "1px solid rgba(245,158,11,0.2)", display: "flex", alignItems: "center", gap: 10 }}>
                  <AlertTriangle size={14} color="#f59e0b" />
                  <Text style={{ fontSize: 12, color: "#92400e" }}>
                    Незаполненные обязательные поля: <strong>{doc.missingRequiredFields.join(", ")}</strong>
                  </Text>
                </div>
              )}

              {isDraft && (
                <div style={{ display: "flex", gap: 10, marginTop: 24, paddingTop: 20, borderTop: "1px solid #f1f5f9", flexWrap: "wrap" }}>
                  <button onClick={handleSave} disabled={isUpdating}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "transparent", border: "1px solid #e2e8f0", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#0F2A44", fontSize: 13, fontWeight: 600, height: 40, opacity: isUpdating ? 0.6 : 1 }}>
                    <Save size={14} />{isUpdating ? "Сохранение..." : "Сохранить"}
                  </button>
                  <button onClick={handleValidate} disabled={isValidating}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "rgba(124,58,237,0.08)", border: "1px solid rgba(124,58,237,0.3)", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#7c3aed", fontSize: 13, fontWeight: 700, height: 40, opacity: isValidating ? 0.6 : 1 }}>
                    <ShieldCheck size={14} />{isValidating ? "Проверка..." : "Валидировать"}
                  </button>
                  <button onClick={handleComplete} disabled={isCompleting}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "#0F2A44", border: "1px solid #0F2A44", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#fff", fontSize: 13, fontWeight: 700, height: 40, opacity: isCompleting ? 0.6 : 1 }}>
                    <Zap size={14} />{isCompleting ? "Анализ..." : "Завершить"}
                  </button>
                </div>
              )}

              {isCompleted && (
                <div style={{ display: "flex", gap: 10, marginTop: 24, paddingTop: 20, borderTop: "1px solid #f1f5f9", flexWrap: "wrap" }}>
                  <button onClick={handleDownload}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "#0F2A44", border: "none", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#fff", fontSize: 13, fontWeight: 600, height: 40 }}>
                    <Download size={14} />Скачать PDF
                  </button>
                  <button onClick={handleShare} disabled={isSharing}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "transparent", border: "1px solid #e2e8f0", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#0F2A44", fontSize: 13, fontWeight: 600, height: 40, opacity: isSharing ? 0.6 : 1 }}>
                    <Share2 size={14} />{isSharing ? "Создание..." : "Поделиться"}
                  </button>
                  <button onClick={handleArchive} disabled={isArchiving}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "transparent", border: "1px solid #fecaca", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#ef4444", fontSize: 13, fontWeight: 600, height: 40, opacity: isArchiving ? 0.6 : 1 }}>
                    <Archive size={14} />{isArchiving ? "Архивирование..." : "Архивировать"}
                  </button>
                </div>
              )}

              {isArchived && (
                <div style={{ display: "flex", gap: 10, marginTop: 24, paddingTop: 20, borderTop: "1px solid #f1f5f9" }}>
                  <button onClick={handleDownload}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "#0F2A44", border: "none", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#fff", fontSize: 13, fontWeight: 600, height: 40 }}>
                    <Download size={14} />Скачать PDF
                  </button>
                  <button onClick={handleRestore} disabled={isRestoring}
                    style={{ display: "flex", alignItems: "center", gap: 6, background: "transparent", border: "1px solid #bbf7d0", borderRadius: 10, padding: "9px 18px", cursor: "pointer", color: "#059669", fontSize: 13, fontWeight: 600, height: 40, opacity: isRestoring ? 0.6 : 1 }}>
                    <RotateCcw size={14} />{isRestoring ? "Восстановление..." : "Восстановить"}
                  </button>
                </div>
              )}
            </Form>
          </div>

          {/* Right: Analysis / Suggestions / Versions */}
          {hasRightPanel && (
            <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              {analysisResult && (
                <div style={{ background: "#fff", borderRadius: 16, padding: "20px 24px", boxShadow: "0 8px 32px rgba(11,28,48,0.04)", border: "1px solid rgba(197,198,210,0.15)" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
                    <Zap size={16} color="#0F2A44" />
                    <Text strong style={{ color: "#0F2A44", fontSize: 14 }}>Результат анализа</Text>
                  </div>
                  <AnalysisBlock result={analysisResult} />
                </div>
              )}
              {showSuggestions && (
                <div style={{ background: "#fff", borderRadius: 16, padding: "20px 24px", boxShadow: "0 8px 32px rgba(11,28,48,0.04)", border: "1px solid rgba(197,198,210,0.15)" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
                    <Lightbulb size={16} color="#0F2A44" />
                    <Text strong style={{ color: "#0F2A44", fontSize: 14 }}>Предложения AI</Text>
                  </div>
                  {isFetchingSuggestions ? <div style={{ textAlign: "center", padding: "32px 0" }}><Spin /></div>
                    : suggestions ? <AnalysisBlock result={suggestions} /> : null}
                </div>
              )}
              {showVersions && (
                <div style={{ background: "#fff", borderRadius: 16, padding: "20px 24px", boxShadow: "0 8px 32px rgba(11,28,48,0.04)", border: "1px solid rgba(197,198,210,0.15)" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
                    <History size={16} color="#0F2A44" />
                    <Text strong style={{ color: "#0F2A44", fontSize: 14 }}>История версий</Text>
                  </div>
                  <VersionsPanel versions={versions} isLoading={versionsLoading} />
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
