import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { App, Select, Empty, Progress, Spin } from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import {
  Sparkles, Search, FileText, FolderOpen, ArrowRight, Zap,
  BrainCircuit, AlertTriangle, ShieldAlert,
} from "lucide-react";
import { useMatchTemplateMutation } from "../../../features/matching/api/matching-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";
import { useCreateDocumentMutation } from "../../../features/documents/api/document-api";
import type { AnalysisResult, MatchedTemplate } from "../../../features/documents/api/document-api";
import { ROUTES } from "../../../app/router/router";

export default function MatchingPanel() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [creatingFor, setCreatingFor] = useState<number | null>(null);

  const { data: categories = [] } = useGetActiveCategoriesQuery();
  const [matchTemplate, { isLoading }] = useMatchTemplateMutation();
  const [createDocument] = useCreateDocumentMutation();

  const handleMatch = async () => {
    if (!query.trim()) { message.warning("Опишите вашу юридическую ситуацию"); return; }
    try {
      const res = await matchTemplate({
        fieldValues: { query },
        categoryId,
      }).unwrap();
      setResult(res);
    } catch { message.error("Ошибка при поиске шаблонов"); }
  };

  const handleUseTemplate = async (template: MatchedTemplate) => {
    setCreatingFor(template.templateId);
    try {
      const doc = await createDocument({
        templateId: template.templateId,
        title: `Документ по шаблону: ${template.title}`,
        fieldValues: {},
      }).unwrap();
      navigate(`${ROUTES.DOCUMENTS}/${doc.id}`);
    } catch {
      message.error("Ошибка при создании документа");
      setCreatingFor(null);
    }
  };

  const matched = result?.matchedTemplates ?? [];
  const hasErrors = (result?.validationErrors?.length ?? 0) > 0;
  const hasRisks = (result?.risks?.length ?? 0) > 0;

  return (
    <div style={{ padding: "0 32px 32px" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{
        background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
        padding: "40px 40px 56px",
        marginLeft: -32, marginRight: -32,
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20, marginBottom: 32 }}>
          <div style={{ width: 52, height: 52, borderRadius: 14, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <Sparkles size={28} color="#fff" />
          </div>
          <div>
            <div style={{ color: "#fff", fontSize: 22, fontWeight: 800, marginBottom: 4 }}>Подбор шаблона</div>
            <div style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
              Опишите вашу ситуацию — AI подберёт подходящие юридические шаблоны
            </div>
          </div>
        </div>

        {/* Search area inside hero */}
        <div style={{ background: "rgba(255,255,255,0.1)", backdropFilter: "blur(10px)", borderRadius: 14, padding: "20px 24px", border: "1px solid rgba(255,255,255,0.15)" }}>
          <div style={{ marginBottom: 14 }}>
            <textarea
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Например: Мне нужен договор на оказание консультационных услуг с ИП, срок 3 месяца, с условиями конфиденциальности..."
              rows={3}
              style={{
                width: "100%", boxSizing: "border-box",
                background: "rgba(255,255,255,0.15)",
                border: "1px solid rgba(255,255,255,0.25)",
                borderRadius: 10, padding: "12px 16px",
                color: "#fff", fontSize: 14, resize: "vertical",
                outline: "none", fontFamily: "inherit",
              }}
            />
          </div>
          <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
            <Select
              placeholder="Категория (необязательно)"
              allowClear
              value={categoryId}
              onChange={v => setCategoryId(v)}
              style={{ width: 220 }}
              options={categories.map(c => ({ value: c.id, label: c.name }))}
              styles={{ popup: { root: { zIndex: 1100 } } }}
            />
            <button
              onClick={handleMatch}
              disabled={isLoading || !query.trim()}
              style={{
                background: isLoading || !query.trim() ? "rgba(255,255,255,0.3)" : "#fff",
                color: "#0F2A44", border: "none", borderRadius: 10,
                padding: "10px 24px", fontWeight: 700, fontSize: 14,
                cursor: isLoading || !query.trim() ? "not-allowed" : "pointer",
                display: "flex", alignItems: "center", gap: 8,
                transition: "background 0.2s",
              }}
            >
              {isLoading ? <><Spin size="small" /> Поиск...</> : <><Search size={15} /> Найти шаблоны</>}
            </button>
          </div>
        </div>
      </div>

      {/* ── Results ── */}
      <div style={{ marginTop: 28 }}>
        {!result && !isLoading && (
          <div style={{ background: "#fff", borderRadius: 14, border: "1px solid #e5e7eb", padding: "64px 24px", textAlign: "center" }}>
            <div style={{ width: 64, height: 64, borderRadius: "50%", background: "rgba(15,42,68,0.06)", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 16px" }}>
              <BrainCircuit size={28} color="#0F2A44" />
            </div>
            <div style={{ fontWeight: 700, fontSize: 16, color: "#111827", marginBottom: 8 }}>Начните поиск</div>
            <p style={{ color: "#6b7280", fontSize: 14, maxWidth: 420, margin: "0 auto" }}>
              Введите описание вашей юридической ситуации выше, и AI подберёт наиболее подходящие шаблоны из базы
            </p>
          </div>
        )}

        {result && (
          <div style={{ display: "grid", gridTemplateColumns: matched.length ? "1fr 340px" : "1fr", gap: 20, alignItems: "start" }}>
            {/* Matched templates */}
            <div>
              {matched.length === 0 ? (
                <div style={{ background: "#fff", borderRadius: 14, border: "1px solid #e5e7eb", padding: "48px 24px" }}>
                  <Empty description={<span style={{ color: "#6b7280" }}>Подходящих шаблонов не найдено. Попробуйте другой запрос.</span>} />
                </div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                  {matched.map((t) => (
                    <div key={t.templateId} style={{ background: "#fff", borderRadius: 14, border: "1px solid #e5e7eb", padding: "20px 24px", display: "flex", alignItems: "flex-start", gap: 16 }}>
                      <div style={{ width: 44, height: 44, borderRadius: 12, background: "rgba(15,42,68,0.07)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                        <FileText size={20} color="#0F2A44" />
                      </div>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 4 }}>
                          <span style={{ fontWeight: 700, fontSize: 15, color: "#111827" }}>{t.title}</span>
                          <span style={{ fontSize: 12, fontWeight: 700, color: "#1677ff", background: "rgba(22,119,255,0.08)", padding: "2px 10px", borderRadius: 20 }}>
                            {Math.round(t.score * 100)}%
                          </span>
                        </div>
                        <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
                          <FolderOpen size={12} color="#9ca3af" />
                          <span style={{ fontSize: 12, color: "#9ca3af" }}>{t.categoryName}</span>
                        </div>
                        <Progress
                          percent={Math.round(t.score * 100)}
                          size="small"
                          showInfo={false}
                          strokeColor={t.score >= 0.7 ? "#059669" : t.score >= 0.4 ? "#f59e0b" : "#ef4444"}
                          style={{ marginBottom: 8 }}
                        />
                        {t.aiNote && <p style={{ fontSize: 12, color: "#64748b", margin: "0 0 12px" }}>{t.aiNote}</p>}
                        <button
                          onClick={() => handleUseTemplate(t)}
                          disabled={creatingFor === t.templateId}
                          style={{
                            background: "#0F2A44", color: "#fff", border: "none", borderRadius: 8,
                            padding: "8px 18px", cursor: creatingFor === t.templateId ? "not-allowed" : "pointer",
                            fontWeight: 600, fontSize: 13, display: "flex", alignItems: "center", gap: 6,
                            opacity: creatingFor === t.templateId ? 0.7 : 1,
                          }}
                        >
                          {creatingFor === t.templateId ? <><Spin size="small" /> Создание...</> : <><Zap size={13} /> Использовать шаблон<ArrowRight size={13} /></>}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* AI analysis sidebar */}
            {(result.aiSummary || hasErrors || hasRisks) && (
              <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                {result.aiSummary && (
                  <div style={{ background: "#fff", borderRadius: 14, border: "1px solid rgba(22,119,255,0.15)", padding: "18px 20px" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                      <BrainCircuit size={15} color="#1677ff" />
                      <span style={{ fontWeight: 700, fontSize: 13, color: "#1677ff" }}>AI Анализ</span>
                    </div>
                    <p style={{ fontSize: 13, color: "#1e3a8a", lineHeight: 1.6, margin: 0 }}>{result.aiSummary}</p>
                    {result.aiRecommendation && (
                      <p style={{ fontSize: 12, color: "#1e40af", marginTop: 8, marginBottom: 0 }}>
                        <strong>Рекомендация:</strong> {result.aiRecommendation}
                      </p>
                    )}
                  </div>
                )}
                {hasErrors && (
                  <div style={{ background: "#fff", borderRadius: 14, border: "1px solid rgba(239,68,68,0.2)", padding: "18px 20px" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                      <AlertTriangle size={15} color="#ef4444" />
                      <span style={{ fontWeight: 700, fontSize: 13, color: "#ef4444" }}>Предупреждения ({result.validationErrors.length})</span>
                    </div>
                    {result.validationErrors.map((e, i) => (
                      <div key={i} style={{ fontSize: 12, color: "#dc2626", marginBottom: 4 }}>• {e.message}</div>
                    ))}
                  </div>
                )}
                {hasRisks && (
                  <div style={{ background: "#fff", borderRadius: 14, border: "1px solid rgba(245,158,11,0.2)", padding: "18px 20px" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
                      <ShieldAlert size={15} color="#f59e0b" />
                      <span style={{ fontWeight: 700, fontSize: 13, color: "#92400e" }}>Риски ({result.risks.length})</span>
                    </div>
                    {result.risks.map((r, i) => (
                      <div key={i} style={{ fontSize: 12, color: "#92400e", marginBottom: 4 }}>• {r.message}</div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
