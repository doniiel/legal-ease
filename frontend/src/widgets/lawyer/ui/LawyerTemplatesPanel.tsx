import { useRef } from "react";
import {
  App, Table, Button, Modal, Form, Input, Select,
  Typography, Tooltip, Steps, Switch,
} from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import type { ColumnsType } from "antd/es/table";
import {
  FileCode, Search, Plus, Pencil, Trash2, Send,
  PlusCircle, Minus, FileText, CheckCircle2,
  Type, Hash, Calendar, ToggleLeft, List,
  ArrowRight, ArrowLeft, RefreshCw, RotateCcw,
} from "lucide-react";
import {
  useCreateTemplateMutation,
  useUpdateTemplateMutation,
  type Template,
  type TemplateFieldRequest,
  type FieldType,
} from "../../../features/lawyer/api/lawyer-template-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";
import { useLawyerTemplates } from "../../../features/lawyer/model/use-lawyer-templates";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;
const { TextArea } = Input;

// ─── Transliteration ─────────────────────────────────────────
const RU_MAP: Record<string, string> = {
  а:"a",б:"b",в:"v",г:"g",д:"d",е:"e",ё:"e",ж:"zh",з:"z",и:"i",
  й:"y",к:"k",л:"l",м:"m",н:"n",о:"o",п:"p",р:"r",с:"s",т:"t",
  у:"u",ф:"f",х:"kh",ц:"ts",ч:"ch",ш:"sh",щ:"sch",ъ:"",ы:"y",ь:"",
  э:"e",ю:"yu",я:"ya",
};
function toFieldKey(label: string, index: number): string {
  const result = label.toLowerCase().split("").map(c => RU_MAP[c] ?? (c.match(/[a-z0-9]/) ? c : "_")).join("")
    .replace(/_+/g, "_").replace(/^[^a-z]+/, "").replace(/[^a-z0-9_]/g, "").slice(0, 60);
  return result.length >= 2 ? result : `field_${index + 1}`;
}

// ─── Field type config ────────────────────────────────────────
const FIELD_TYPES: { value: FieldType; label: string; color: string; bg: string; icon: React.ReactNode }[] = [
  { value: "TEXT",    label: "Текст",   color: "#1677ff", bg: "#eff6ff", icon: <Type size={14} /> },
  { value: "NUMBER",  label: "Число",   color: "#7c3aed", bg: "#f5f3ff", icon: <Hash size={14} /> },
  { value: "DATE",    label: "Дата",    color: "#059669", bg: "#f0fdf4", icon: <Calendar size={14} /> },
  { value: "BOOLEAN", label: "Да/Нет", color: "#f59e0b", bg: "#fffbeb", icon: <ToggleLeft size={14} /> },
  { value: "SELECT",  label: "Список",  color: "#ef4444", bg: "#fff1f2", icon: <List size={14} /> },
];
const FIELD_KEY_PATTERN = /^[a-z][a-z0-9_]*$/;
const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

// ─── Status pill ──────────────────────────────────────────────
function StatusPill({ status }: { status: string }) {
  const cfg = status === "PUBLISHED"
    ? { color: "#059669", bg: "rgba(5,150,105,0.08)", border: "rgba(5,150,105,0.2)", dot: "#059669", label: "Опубликован" }
    : { color: "#f59e0b", bg: "rgba(245,158,11,0.08)", border: "rgba(245,158,11,0.2)", dot: "#f59e0b", label: "Черновик" };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: cfg.dot, flexShrink: 0 }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color, letterSpacing: "0.04em" }}>{cfg.label}</span>
    </div>
  );
}

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({ icon, tooltip, onClick, hoverBg, hoverColor, disabled = false }: {
  icon: React.ReactNode; tooltip: string; onClick?: () => void;
  hoverBg: string; hoverColor: string; disabled?: boolean;
}) {
  return (
    <Tooltip title={tooltip}>
      <button
        disabled={disabled}
        onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: disabled ? "not-allowed" : "pointer", color: disabled ? "#cbd5e1" : "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={(e) => { if (!disabled) { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; } }}
        onMouseLeave={(e) => { if (!disabled) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; } }}
      >
        {icon}
      </button>
    </Tooltip>
  );
}

// ─── Field card ───────────────────────────────────────────────
function FieldCard({ name, index, onRemove, canRemove, form }: {
  name: number; index: number; onRemove: () => void; canRemove: boolean;
  form: ReturnType<typeof Form.useForm>[0];
}) {
  const keyTouched = useRef(false);
  const currentType: FieldType = Form.useWatch(["fields", name, "fieldType"], form) ?? "TEXT";
  const typeConfig = FIELD_TYPES.find(t => t.value === currentType) ?? FIELD_TYPES[0];
  const handleLabelChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!keyTouched.current) form.setFields([{ name: ["fields", name, "fieldKey"], value: toFieldKey(e.target.value, index) }]);
  };
  const regenerateKey = () => {
    const label: string = form.getFieldValue(["fields", name, "label"]) ?? "";
    form.setFields([{ name: ["fields", name, "fieldKey"], value: toFieldKey(label, index) }]);
    keyTouched.current = false;
  };
  return (
    <div style={{ background: "#fff", borderRadius: 12, border: `1.5px solid ${typeConfig.color}30`, padding: "16px 18px", marginBottom: 12, position: "relative" }}>
      <div style={{ position: "absolute", top: -10, left: 16, background: typeConfig.color, color: "#fff", fontSize: 11, fontWeight: 700, padding: "1px 10px", borderRadius: 20 }}>
        Поле {index + 1}
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginTop: 6 }}>
        <Form.Item label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Название поля</Text>} name={[name, "label"]}
          rules={[{ required: true, message: "Обязательное" }, { max: 255 }]} style={{ marginBottom: 10 }}>
          <Input placeholder="Например: Сумма договора" onChange={handleLabelChange} />
        </Form.Item>
        <Form.Item label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Ключ поля</Text>} name={[name, "fieldKey"]}
          rules={[{ required: true, message: "Обязательное" }, { pattern: FIELD_KEY_PATTERN, message: "Только: a-z, 0-9, _" }, { min: 2 }, { max: 100 }]}
          style={{ marginBottom: 10 }}>
          <Input placeholder="contract_amount" style={{ fontFamily: "monospace", fontSize: 12, color: "#1677ff" }}
            onChange={() => { keyTouched.current = true; }}
            addonAfter={<Tooltip title="Перегенерировать"><RefreshCw size={13} style={{ cursor: "pointer", color: "#6b7280" }} onClick={regenerateKey} /></Tooltip>} />
        </Form.Item>
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 12, alignItems: "end" }}>
        <Form.Item label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Тип поля</Text>} name={[name, "fieldType"]}
          rules={[{ required: true }]} style={{ marginBottom: 0 }}>
          <Select options={FIELD_TYPES.map(ft => ({
            value: ft.value,
            label: <span style={{ display: "flex", alignItems: "center", gap: 6 }}><span style={{ color: ft.color }}>{ft.icon}</span>{ft.label}</span>
          }))} />
        </Form.Item>
        <div style={{ display: "flex", alignItems: "center", gap: 16, paddingBottom: 4 }}>
          <Form.Item label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Обязательное</Text>} name={[name, "required"]}
            valuePropName="checked" style={{ marginBottom: 0 }}>
            <Switch checkedChildren="Да" unCheckedChildren="Нет" size="small" />
          </Form.Item>
          {canRemove && (
            <Tooltip title="Удалить поле">
              <Button type="text" danger size="small" icon={<Minus size={13} />} onClick={onRemove} style={{ marginTop: 18 }} />
            </Tooltip>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Template Form Modal ──────────────────────────────────────
function TemplateFormModal({ open, onClose, editingTemplate, onSuccess }: {
  open: boolean; onClose: () => void; editingTemplate: Template | null; onSuccess: () => void;
}) {
  const { message } = App.useApp();
  const [step, setStep] = useState(0);
  const [form] = Form.useForm();
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const [createTemplate, { isLoading: isCreating }] = useCreateTemplateMutation();
  const [updateTemplate, { isLoading: isUpdating }] = useUpdateTemplateMutation();
  const isLoading = isCreating || isUpdating;

  const handleOpen = () => {
    setStep(0);
    if (editingTemplate) {
      form.setFieldsValue({ title: editingTemplate.title, description: editingTemplate.description, categoryId: editingTemplate.category?.id,
        body: (editingTemplate as any).body ?? "",
        fields: editingTemplate.fields.map(f => ({ label: f.label, fieldKey: f.fieldKey, fieldType: f.fieldType, required: f.required })) });
    } else {
      form.resetFields();
      form.setFieldsValue({ fields: [{ label: "", fieldKey: "", fieldType: "TEXT", required: false }] });
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const fields: TemplateFieldRequest[] = (values.fields ?? []).map((f: Omit<TemplateFieldRequest, "orderNum">, i: number) => ({ ...f, orderNum: i + 1 }));
    try {
      if (editingTemplate) {
        await updateTemplate({ id: editingTemplate.id, body: { ...values, fields } }).unwrap();
        message.success("Шаблон обновлён");
      } else {
        await createTemplate({ ...values, fields }).unwrap();
        message.success("Шаблон создан");
      }
      onClose(); onSuccess();
    } catch (e) {
      if (isFetchError(e) && e.status === 403) message.error("Нет доступа или шаблон уже опубликован");
      else if (isFetchError(e) && e.status === 400) message.error("Проверьте данные (ключи полей должны быть уникальны)");
      else message.error("Произошла ошибка");
    }
  };

  return (
    <Modal open={open} onCancel={onClose} afterOpenChange={(v) => v && handleOpen()} title={null}
      footer={
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Button onClick={onClose} disabled={isLoading}>Отмена</Button>
          <div style={{ display: "flex", gap: 8 }}>
            {step === 1 && <Button icon={<ArrowLeft size={14} />} onClick={() => setStep(0)} disabled={isLoading}>Назад</Button>}
            {step === 0 && <Button type="primary" icon={<ArrowRight size={14} />} onClick={async () => { await form.validateFields(["title", "categoryId"]); setStep(1); }} style={{ background: "#1a2744", borderColor: "#1a2744" }}>Далее: Поля</Button>}
            {step === 1 && <Button type="primary" onClick={handleSubmit} loading={isLoading} style={{ background: "#059669", borderColor: "#059669" }}>{editingTemplate ? "Сохранить" : "Создать шаблон"}</Button>}
          </div>
        </div>
      }
      width={760} destroyOnHidden styles={{ body: { padding: 0 } }}>
      <div style={{ background: "#1a2744", borderRadius: "8px 8px 0 0", padding: "20px 28px 16px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
          <div style={{ width: 40, height: 40, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <FileCode size={20} color="#fff" />
          </div>
          <div>
            <Text style={{ color: "#fff", fontWeight: 700, fontSize: 16, display: "block" }}>{editingTemplate ? "Редактировать шаблон" : "Создать шаблон"}</Text>
            <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>{step === 0 ? "Шаг 1 из 2: Основная информация" : "Шаг 2 из 2: Поля документа"}</Text>
          </div>
        </div>
        <Steps current={step} size="small" style={{ maxWidth: 400 }} items={[
          { title: <Text style={{ color: step === 0 ? "#fff" : "rgba(255,255,255,0.5)", fontSize: 12 }}>Информация</Text> },
          { title: <Text style={{ color: step === 1 ? "#fff" : "rgba(255,255,255,0.5)", fontSize: 12 }}>Поля шаблона</Text> },
        ]} />
      </div>
      <div style={{ padding: "24px 28px", maxHeight: 520, overflowY: "auto" }}>
        <Form form={form} layout="vertical">
          <div style={{ display: step === 0 ? "block" : "none" }}>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Название шаблона</Text>} name="title"
              rules={[{ required: true, message: "Введите название" }, { min: 3 }, { max: 255 }]}>
              <Input size="large" placeholder="Например: Договор купли-продажи" prefix={<FileText size={16} color="#9ca3af" />} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Описание</Text>} name="description" rules={[{ max: 2000 }]}>
              <TextArea rows={2} placeholder="Краткое описание..." showCount maxLength={2000} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Категория</Text>} name="categoryId" rules={[{ required: true, message: "Выберите категорию" }]}>
              <Select size="large" showSearch placeholder="Выберите категорию" filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
                options={(categoriesData ?? []).map(c => ({ value: c.id, label: c.name }))} />
            </Form.Item>
            <Form.Item
              label={<Text style={{ fontWeight: 600 }}>Текст шаблона (тело документа)</Text>}
              name="body"
              rules={[{ max: 100000 }]}
              extra={
                <Text style={{ fontSize: 11, color: "#6b7280" }}>
                  Используйте <code style={{ background: "#f3f4f6", padding: "1px 4px", borderRadius: 3 }}>{"{{field_key}}"}</code> для подстановки значений полей.
                  Ключи должны совпадать с ключами полей на следующем шаге.
                  Пример: <code style={{ background: "#f3f4f6", padding: "1px 4px", borderRadius: 3 }}>{"Договор №{{contract_number}} от {{contract_date}}"}</code>
                </Text>
              }
            >
              <TextArea
                rows={10}
                placeholder={"Введите текст документа с плейсхолдерами...\n\nПример:\nДОГОВОР №{{contract_number}}\nг. {{city}}, {{contract_date}}\n\nСтороны заключили настоящий договор о следующем:"}
                showCount
                maxLength={100000}
                style={{ fontFamily: "monospace", fontSize: 13 }}
              />
            </Form.Item>
          </div>
          <div style={{ display: step === 1 ? "block" : "none" }}>
            <div style={{ background: "#f0f9ff", borderRadius: 10, padding: "10px 14px", marginBottom: 18, border: "1px solid #bae6fd" }}>
              <Text style={{ fontSize: 12, color: "#0369a1" }}>💡 <strong>Ключ поля</strong> — внутренний идентификатор для правил. Автогенерируется из названия.</Text>
            </div>
            <Form.List name="fields">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name: fieldName }, i) => (
                    <FieldCard key={key} name={fieldName} index={i} form={form} canRemove={fields.length > 1} onRemove={() => remove(fieldName)} />
                  ))}
                  <Button type="dashed" onClick={() => add({ label: "", fieldKey: "", fieldType: "TEXT", required: false })}
                    icon={<PlusCircle size={14} />} block style={{ borderColor: "#1a2744", color: "#1a2744", borderRadius: 8, height: 40 }}>
                    Добавить поле
                  </Button>
                </>
              )}
            </Form.List>
          </div>
        </Form>
      </div>
    </Modal>
  );
}

// needed for useState in TemplateFormModal
import { useState } from "react";

// ─── Main panel ───────────────────────────────────────────────
export default function LawyerTemplatesPanel() {
  const isMobile = useIsMobile();
  const {
    page, setPage, search, setSearch, statusFilter, setStatusFilter,
    modalOpen, editingTemplate, data, isLoading, isDeleting, isPublishing,
    filtered, draftCount, publishedCount, totalFields,
    openCreate, openEdit, closeModal, handleDelete, handlePublish,
  } = useLawyerTemplates();

  const columns: ColumnsType<Template> = [
    {
      title: "Шаблон",
      key: "title",
      render: (_, r) => (
        <div>
          <Text strong style={{ fontSize: 14, color: "#0b1c30" }}>{r.title}</Text>
          {r.description && <Text style={{ fontSize: 12, color: "#64748b", display: "block", marginTop: 2 }}>{r.description.length > 80 ? r.description.slice(0, 80) + "…" : r.description}</Text>}
        </div>
      ),
    },
    {
      title: "Категория",
      key: "category",
      width: 160,
      render: (_, r) => (
        <div style={{ display: "inline-flex", alignItems: "center", padding: "3px 10px", borderRadius: 20, background: "rgba(26,39,68,0.06)", border: "1px solid rgba(26,39,68,0.12)" }}>
          <span style={{ fontSize: 11, fontWeight: 600, color: "#1a2744" }}>{r.category?.name || "—"}</span>
        </div>
      ),
    },
    {
      title: "Поля",
      key: "fields",
      width: 80,
      align: "center" as const,
      render: (_, r) => (
        <Tooltip title={r.fields?.map(f => `${f.label} (${f.fieldKey})`).join(", ") || "Нет полей"}>
          <span style={{ fontFamily: "monospace", fontSize: 12, color: "#475569", background: "#f8fafc", padding: "2px 8px", borderRadius: 6, border: "1px solid #e2e8f0", cursor: "help" }}>
            {r.fields?.length ?? 0}
          </span>
        </Tooltip>
      ),
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      width: 140,
      render: (status: string) => <StatusPill status={status} />,
    },
    {
      title: "Создан",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 120,
      render: (date: string) => <Text style={{ fontSize: 12, color: "#64748b" }}>{new Date(date).toLocaleDateString("ru-KZ", { day: "2-digit", month: "short", year: "numeric" })}</Text>,
    },
    {
      title: "Действия",
      key: "actions",
      width: 120,
      align: "center" as const,
      render: (_, r) => {
        const isDraft = r.status === "DRAFT";
        return (
          <div style={{ display: "flex", gap: 2, justifyContent: "center" }}>
            <IconBtn icon={<Pencil size={14} />} tooltip={isDraft ? "Редактировать" : "Только черновик"} hoverBg="#f0f9ff" hoverColor="#1677ff" disabled={!isDraft} onClick={() => openEdit(r)} />
            <IconBtn icon={isPublishing ? <span style={{ fontSize: 10 }}>…</span> : <Send size={14} />} tooltip={isDraft ? "Опубликовать" : "Уже опубликован"} hoverBg="#f0fdf4" hoverColor="#059669" disabled={!isDraft}
              onClick={isDraft ? () => handlePublish(r.id) : undefined} />
            <IconBtn icon={isDeleting ? <span style={{ fontSize: 10 }}>…</span> : <Trash2 size={14} />} tooltip={isDraft ? "Удалить" : "Нельзя удалить опубликованный"} hoverBg="#fff1f2" hoverColor="#ef4444" disabled={!isDraft}
              onClick={isDraft ? () => handleDelete(r.id) : undefined} />
          </div>
        );
      },
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "#1a2744", position: "relative", overflow: "hidden" }}>
        <div style={{ padding: isMobile ? "24px 16px 48px" : "40px 40px 56px", display: "flex", flexDirection: isMobile ? "column" as React.CSSProperties["flexDirection"] : "row" as React.CSSProperties["flexDirection"], justifyContent: "space-between", alignItems: "flex-end", gap: isMobile ? 16 : undefined, position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>Мои шаблоны</h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>Создание и управление шаблонами юридических документов</p>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <Button icon={<Plus size={15} />} onClick={openCreate}
              style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "#fff", borderRadius: 10, height: 42, fontWeight: 600, paddingLeft: 20, paddingRight: 20 }}>
              Создать шаблон
            </Button>
          </div>
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 12px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "grid", gridTemplateColumns: isMobile ? "repeat(2, 1fr)" : "repeat(4, 1fr)", gap: isMobile ? 10 : 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Всего шаблонов"   value={data?.totalElements ?? 0} color="#1a2744" icon={<FileCode size={26} />} loading={isLoading} />
          <StatCard label="Черновики"         value={draftCount}              color="#f59e0b" icon={<FileText size={26} />} />
          <StatCard label="Опубликованы"      value={publishedCount}          color="#059669" icon={<CheckCircle2 size={26} />} />
          <StatCard label="Всего полей"       value={totalFields}             color="#1677ff" icon={<List size={26} />} />
        </div>

        {/* ── Filter toolbar ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "20px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Поиск</div>
              <Input prefix={<Search size={15} color="#757682" />} placeholder="По названию или категории..." allowClear value={search} onChange={e => setSearch(e.target.value)}
                style={{ borderRadius: 12, background: "#eff4ff", border: "none" }} />
            </div>
            <div style={{ minWidth: 180 }}>
              <div style={{ fontSize: 10, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.1em", color: "#757682", marginBottom: 8 }}>Статус</div>
              <Select allowClear placeholder="Все статусы" value={statusFilter} onChange={setStatusFilter} style={{ width: "100%" }}
                options={[{ value: "DRAFT", label: "Черновик" }, { value: "PUBLISHED", label: "Опубликован" }]} />
            </div>
            {(search || statusFilter) && (
              <Button icon={<RotateCcw size={13} />} type="text" onClick={() => { setSearch(""); setStatusFilter(undefined); }} style={{ color: "#94a3b8", height: 40 }}>
                Сбросить
              </Button>
            )}
          </div>
        </div>

        {/* ── Table ── */}
        <div style={{ background: "#fff", borderRadius: 16, boxShadow: "0 8px 32px rgba(11,28,48,0.04)", overflow: "hidden" }}>
          <Table
            components={editorialTableComponents}
            columns={columns}
            dataSource={filtered}
            rowKey="id"
            loading={isLoading}
            scroll={{ x: "max-content" }}
            pagination={{ current: page, total: data?.totalElements, pageSize: 10, onChange: p => setPage(p), showSizeChanger: false, showTotal: t => `Всего ${t} шаблонов`, style: { padding: "16px 32px", margin: 0 } }}
            style={{ borderRadius: 0 }}
          />
        </div>
      </div>

      <TemplateFormModal open={modalOpen} onClose={closeModal} editingTemplate={editingTemplate} onSuccess={closeModal} />
    </div>
  );
}
