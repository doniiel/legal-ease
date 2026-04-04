import { useParams } from "react-router-dom";
import { Spin } from "antd";
import { CheckCircle2, XCircle, ShieldCheck, ShieldAlert, User, Calendar, Hash, FileText } from "lucide-react";
import { useVerifyDocumentQuery } from "../../features/documents/api/document-verification-api";

function InfoRow({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "14px 0", borderBottom: "1px solid #f0f0f0" }}>
      <div style={{ width: 32, height: 32, borderRadius: 8, background: "#f8fafc", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
        {icon}
      </div>
      <div>
        <div style={{ fontSize: 11, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 2 }}>{label}</div>
        <div style={{ fontSize: 14, fontWeight: 600, color: "#111827" }}>{value}</div>
      </div>
    </div>
  );
}

export default function DocumentVerify() {
  const { id } = useParams<{ id: string }>();
  const documentId = Number(id);

  const { data, isLoading, isError } = useVerifyDocumentQuery(documentId, { skip: isNaN(documentId) });

  if (isNaN(documentId)) {
    return (
      <div style={{ minHeight: "100vh", background: "#f5f7fa", display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
        <div style={{ background: "#fff", borderRadius: 20, padding: "48px 40px", textAlign: "center", maxWidth: 440, boxShadow: "0 8px 32px rgba(0,0,0,0.08)" }}>
          <XCircle size={48} color="#ef4444" style={{ marginBottom: 16 }} />
          <div style={{ fontWeight: 700, fontSize: 20, color: "#111827", marginBottom: 8 }}>Неверная ссылка</div>
          <p style={{ color: "#6b7280" }}>ID документа некорректен.</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: "100vh", background: "#f5f7fa" }}>
      {/* Header */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", padding: "28px 32px" }}>
        <div style={{ maxWidth: 560, margin: "0 auto", display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <ShieldCheck size={24} color="#fff" />
          </div>
          <div>
            <div style={{ color: "#fff", fontWeight: 800, fontSize: 18 }}>LegalEase</div>
            <div style={{ color: "rgba(255,255,255,0.65)", fontSize: 12 }}>Проверка подлинности документа</div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div style={{ maxWidth: 560, margin: "40px auto", padding: "0 16px" }}>
        {isLoading && (
          <div style={{ background: "#fff", borderRadius: 20, padding: "64px 24px", textAlign: "center", boxShadow: "0 8px 32px rgba(0,0,0,0.06)" }}>
            <Spin size="large" />
            <p style={{ color: "#6b7280", marginTop: 16 }}>Проверяем документ...</p>
          </div>
        )}

        {(isError || (!isLoading && !data)) && (
          <div style={{ background: "#fff", borderRadius: 20, padding: "48px 32px", textAlign: "center", boxShadow: "0 8px 32px rgba(0,0,0,0.06)" }}>
            <div style={{ width: 72, height: 72, borderRadius: "50%", background: "#fee2e2", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 20px" }}>
              <XCircle size={36} color="#ef4444" />
            </div>
            <div style={{ fontWeight: 700, fontSize: 20, color: "#111827", marginBottom: 8 }}>Документ не найден</div>
            <p style={{ color: "#6b7280", fontSize: 14 }}>Документ с данным ID не существует или был удалён.</p>
          </div>
        )}

        {!isLoading && data && (
          <div style={{ background: "#fff", borderRadius: 20, overflow: "hidden", boxShadow: "0 8px 32px rgba(0,0,0,0.06)" }}>
            {/* Status banner */}
            <div style={{
              padding: "28px 32px",
              background: data.status === "VALID" ? "linear-gradient(135deg, #059669 0%, #047857 100%)" : "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)",
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                <div style={{ width: 56, height: 56, borderRadius: "50%", background: "rgba(255,255,255,0.2)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  {data.status === "VALID"
                    ? <CheckCircle2 size={30} color="#fff" />
                    : <ShieldAlert size={30} color="#fff" />}
                </div>
                <div>
                  <div style={{ color: "#fff", fontWeight: 800, fontSize: 20, marginBottom: 4 }}>
                    {data.status === "VALID" ? "Документ подлинный" : "Документ не прошёл проверку"}
                  </div>
                  <div style={{ color: "rgba(255,255,255,0.8)", fontSize: 13 }}>
                    {data.status === "VALID"
                      ? "Документ создан и заверен через LegalEase"
                      : "Документ не найден или был изменён после создания"}
                  </div>
                </div>
              </div>
            </div>

            {/* Document details */}
            {data.status === "VALID" && (
              <div style={{ padding: "24px 32px" }}>
                {data.documentId && (
                  <InfoRow icon={<FileText size={16} color="#0F2A44" />} label="Идентификатор" value={data.documentId} />
                )}
                {data.createdBy && (
                  <InfoRow icon={<User size={16} color="#0F2A44" />} label="Составитель" value={data.createdBy} />
                )}
                {data.createdAt && (
                  <InfoRow icon={<Calendar size={16} color="#0F2A44" />} label="Дата создания"
                    value={new Date(data.createdAt).toLocaleDateString("ru-KZ", { day: "numeric", month: "long", year: "numeric" })} />
                )}
                <InfoRow icon={<Hash size={16} color={data.hashValid ? "#059669" : "#f59e0b"} />} label="Целостность файла"
                  value={data.hashValid ? "SHA-256 совпадает — файл не изменён" : "Хэш не проверен"} />

                <div style={{ marginTop: 20, background: "#f0fdf4", borderRadius: 10, padding: "14px 18px", border: "1px solid #bbf7d0", display: "flex", alignItems: "center", gap: 10 }}>
                  <CheckCircle2 size={16} color="#059669" />
                  <span style={{ fontSize: 13, color: "#065f46", fontWeight: 500 }}>
                    Этот документ был официально сформирован через платформу LegalEase
                  </span>
                </div>
              </div>
            )}

            {data.status === "INVALID" && (
              <div style={{ padding: "24px 32px" }}>
                <div style={{ background: "#fef2f2", borderRadius: 10, padding: "14px 18px", border: "1px solid #fecaca" }}>
                  <p style={{ color: "#991b1b", fontSize: 13, margin: 0 }}>
                    Документ не может быть верифицирован. Возможные причины: документ не существует, был удалён или содержимое было изменено после публикации.
                  </p>
                </div>
              </div>
            )}

            {/* Footer */}
            <div style={{ padding: "16px 32px", borderTop: "1px solid #f0f0f0", background: "#f8fafc" }}>
              <p style={{ fontSize: 11, color: "#9ca3af", margin: 0, textAlign: "center" }}>
                Проверка выполнена LegalEase · {new Date().toLocaleDateString("ru-KZ")}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
