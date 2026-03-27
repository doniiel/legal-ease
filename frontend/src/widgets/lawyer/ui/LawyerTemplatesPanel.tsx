import { useState, useRef } from "react";
import {
  App, Table, Button, Modal, Form, Input, Select, Switch,
  Tag, Popconfirm, Space, Typography, Tooltip, Badge, Steps,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  FileCode, Search, Plus, Pencil, Trash2, Send,
  PlusCircle, Minus, FileText, CheckCircle2,
  Type, Hash, Calendar, ToggleLeft, List,
  ArrowRight, ArrowLeft, RefreshCw,
} from "lucide-react";
import {
  useGetMyTemplatesQuery,
  useCreateTemplateMutation,
  useUpdateTemplateMutation,
  useDeleteTemplateMutation,
  usePublishTemplateMutation,
  type Template,
  type TemplateFieldRequest,
  type FieldType,
} from "../../../features/lawyer/api/lawyer-template-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";

const { Title, Text } = Typography;
const { TextArea } = Input;

// ─── Transliteration: Russian label → snake_case fieldKey ───
const RU_MAP: Record<string, string> = {
  а:"a",б:"b",в:"v",г:"g",д:"d",е:"e",ё:"e",ж:"zh",з:"z",и:"i",
  й:"y",к:"k",л:"l",м:"m",н:"n",о:"o",п:"p",р:"r",с:"s",т:"t",
  у:"u",ф:"f",х:"kh",ц:"ts",ч:"ch",ш:"sh",щ:"sch",ъ:"",ы:"y",ь:"",
  э:"e",ю:"yu",я:"ya",
};

function toFieldKey(label: string, index: number): string {
  const result = label
    .toLowerCase()
    .split("")
    .map(c => RU_MAP[c] ?? (c.match(/[a-z0-9]/) ? c : "_"))
    .join("")
    .replace(/_+/g, "_")
    .replace(/^[^a-z]+/, "")
    .replace(/[^a-z0-9_]/g, "")
    .slice(0, 60);
  return result.length >= 2 ? result : `field_${index + 1}`;
}

// ─── Field type config ────────────────────────────────────────
const FIELD_TYPES: { value: FieldType; label: string; color: string; bg: string; icon: React.ReactNode }[] = [
  { value: "TEXT",    label: "Текст",   color: "#1677ff", bg: "#eff6ff", icon: <Type size={14} /> },
  { value: "NUMBER",  label: "Число",   color: "#7c3aed", bg: "#f5f3ff", icon: <Hash size={14} /> },
  { value: "DATE",    label: "Дата",    color: "#059669", bg: "#f0fdf4", icon: <Calendar size={14} /> },
  { value: "BOOLEAN", label: "Да/Нет", color: "#f59e0b", bg: "#fffbeb", icon: <ToggleLeft size={14} /> },
  { value: "SELECT",  label: "Список", color: "#ef4444", bg: "#fff1f2", icon: <List size={14} /> },
];

const FIELD_KEY_PATTERN = /^[a-z][a-z0-9_]*$/;

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

// ─── Stat card ────────────────────────────────────────────────
function StatCard({ label, value, color, icon }: {
  label: string; value: number; color: string; icon: React.ReactNode;
}) {
  return (
    <div style={{
      background: "#fff", borderRadius: 12, padding: "18px 22px", flex: 1,
      borderTop: `3px solid ${color}`, boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
      display: "flex", alignItems: "center", gap: 14,
    }}>
      <div style={{ width: 40, height: 40, borderRadius: 10, background: `${color}18`, display: "flex", alignItems: "center", justifyContent: "center" }}>
        {icon}
      </div>
      <div>
        <div style={{ fontSize: 28, fontWeight: 800, color, lineHeight: 1 }}>{value}</div>
        <Text style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#8c8fa3" }}>
          {label}
        </Text>
      </div>
    </div>
  );
}

// ─── Field card (inside Form.List) ────────────────────────────
function FieldCard({
  name, index, onRemove, canRemove, form,
}: {
  name: number; index: number; onRemove: () => void; canRemove: boolean; form: ReturnType<typeof Form.useForm>[0];
}) {
  const keyTouched = useRef(false);
  const currentType: FieldType = Form.useWatch(["fields", name, "fieldType"], form) ?? "TEXT";
  const typeConfig = FIELD_TYPES.find(t => t.value === currentType) ?? FIELD_TYPES[0];

  const handleLabelChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!keyTouched.current) {
      const generated = toFieldKey(e.target.value, index);
      form.setFields([{ name: ["fields", name, "fieldKey"], value: generated }]);
    }
  };

  const handleKeyChange = () => { keyTouched.current = true; };

  const regenerateKey = () => {
    const label: string = form.getFieldValue(["fields", name, "label"]) ?? "";
    const generated = toFieldKey(label, index);
    form.setFields([{ name: ["fields", name, "fieldKey"], value: generated }]);
    keyTouched.current = false;
  };

  return (
    <div style={{
      background: "#fff", borderRadius: 12,
      border: `1.5px solid ${typeConfig.color}30`,
      padding: "16px 18px", marginBottom: 12,
      position: "relative",
      boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
    }}>
      {/* Field number badge */}
      <div style={{
        position: "absolute", top: -10, left: 16,
        background: typeConfig.color, color: "#fff",
        fontSize: 11, fontWeight: 700, padding: "1px 10px",
        borderRadius: 20, letterSpacing: "0.05em",
      }}>
        Поле {index + 1}
      </div>

      {/* Row 1: Label + fieldKey */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginTop: 6 }}>
        <Form.Item
          label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Название поля</Text>}
          name={[name, "label"]}
          rules={[
            { required: true, message: "Обязательное" },
            { max: 255, message: "Максимум 255" },
          ]}
          style={{ marginBottom: 10 }}
        >
          <Input
            placeholder="Например: Сумма договора"
            onChange={handleLabelChange}
          />
        </Form.Item>

        <Form.Item
          label={
            <Space size={4}>
              <Text style={{ fontSize: 12, fontWeight: 600 }}>Ключ поля</Text>
              <Tooltip title="Уникальный идентификатор поля для правил. Автогенерируется из названия. Используйте латиницу и _ (напр. contract_amount)">
                <Text type="secondary" style={{ fontSize: 11, cursor: "help" }}>(?)</Text>
              </Tooltip>
            </Space>
          }
          name={[name, "fieldKey"]}
          rules={[
            { required: true, message: "Обязательное" },
            { pattern: FIELD_KEY_PATTERN, message: "Только: a-z, 0-9, _ (начинается с буквы)" },
            { min: 2, message: "Минимум 2 символа" },
            { max: 100, message: "Максимум 100" },
          ]}
          style={{ marginBottom: 10 }}
        >
          <Input
            placeholder="contract_amount"
            style={{ fontFamily: "monospace", fontSize: 12, color: "#1677ff" }}
            onChange={handleKeyChange}
            addonAfter={
              <Tooltip title="Перегенерировать из названия">
                <RefreshCw
                  size={13}
                  style={{ cursor: "pointer", color: "#6b7280" }}
                  onClick={regenerateKey}
                />
              </Tooltip>
            }
          />
        </Form.Item>
      </div>

      {/* Row 2: Type + Required */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 12, alignItems: "end" }}>
        <Form.Item
          label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Тип поля</Text>}
          name={[name, "fieldType"]}
          rules={[{ required: true, message: "Выберите тип" }]}
          style={{ marginBottom: 0 }}
        >
          <Select>
            {FIELD_TYPES.map(ft => (
              <Select.Option key={ft.value} value={ft.value}>
                <Space size={6}>
                  <span style={{ color: ft.color }}>{ft.icon}</span>
                  <span style={{ fontSize: 13 }}>{ft.label}</span>
                </Space>
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <div style={{ display: "flex", alignItems: "center", gap: 16, paddingBottom: 4 }}>
          <Form.Item
            label={<Text style={{ fontSize: 12, fontWeight: 600 }}>Обязательное</Text>}
            name={[name, "required"]}
            valuePropName="checked"
            style={{ marginBottom: 0 }}
          >
            <Switch checkedChildren="Да" unCheckedChildren="Нет" size="small" />
          </Form.Item>

          {canRemove && (
            <Tooltip title="Удалить поле">
              <Button
                type="text" danger size="small"
                icon={<Minus size={13} />}
                onClick={onRemove}
                style={{ marginTop: 18 }}
              />
            </Tooltip>
          )}
        </div>
      </div>

      {/* Type badge at top-right */}
      <div style={{ position: "absolute", top: 12, right: 14 }}>
        <Tag style={{
          background: typeConfig.bg, color: typeConfig.color,
          border: `1px solid ${typeConfig.color}40`,
          fontSize: 11, fontWeight: 600, display: "flex", alignItems: "center", gap: 4,
        }}>
          {typeConfig.icon} {typeConfig.label}
        </Tag>
      </div>
    </div>
  );
}

// ─── Template Form Modal ──────────────────────────────────────
function TemplateFormModal({
  open, onClose, editingTemplate, onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  editingTemplate: Template | null;
  onSuccess: () => void;
}) {
  const { message } = App.useApp();
  const [step, setStep] = useState(0);
  const [form] = Form.useForm();
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const categories = categoriesData ?? [];

  const [createTemplate, { isLoading: isCreating }] = useCreateTemplateMutation();
  const [updateTemplate, { isLoading: isUpdating }] = useUpdateTemplateMutation();
  const isLoading = isCreating || isUpdating;

  const handleOpen = () => {
    setStep(0);
    if (editingTemplate) {
      form.setFieldsValue({
        title: editingTemplate.title,
        description: editingTemplate.description,
        categoryId: editingTemplate.category?.id,
        fields: editingTemplate.fields.map(f => ({
          label: f.label,
          fieldKey: f.fieldKey,
          fieldType: f.fieldType,
          required: f.required,
        })),
      });
    } else {
      form.resetFields();
      form.setFieldsValue({
        fields: [{ label: "", fieldKey: "", fieldType: "TEXT", required: false }],
      });
    }
  };

  const goNext = async () => {
    await form.validateFields(["title", "categoryId"]);
    setStep(1);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const fields: TemplateFieldRequest[] = (values.fields ?? []).map(
      (f: Omit<TemplateFieldRequest, "orderNum">, i: number) => ({ ...f, orderNum: i + 1 })
    );
    try {
      if (editingTemplate) {
        await updateTemplate({ id: editingTemplate.id, body: { ...values, fields } }).unwrap();
        message.success("Шаблон обновлён");
      } else {
        await createTemplate({ ...values, fields }).unwrap();
        message.success("Шаблон создан");
      }
      onClose();
      onSuccess();
    } catch (e) {
      if (isFetchError(e) && e.status === 403) message.error("Нет доступа или шаблон уже опубликован");
      else if (isFetchError(e) && e.status === 400) message.error("Ошибка: проверьте данные (ключи полей должны быть уникальны)");
      else message.error("Произошла ошибка");
    }
  };

  const footer = (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Button onClick={onClose} disabled={isLoading}>Отмена</Button>
      <Space>
        {step === 1 && (
          <Button icon={<ArrowLeft size={14} />} onClick={() => setStep(0)} disabled={isLoading}>
            Назад
          </Button>
        )}
        {step === 0 && (
          <Button type="primary" icon={<ArrowRight size={14} />}
            onClick={goNext}
            style={{ background: "#0F2A44", borderColor: "#0F2A44" }}>
            Далее: Поля
          </Button>
        )}
        {step === 1 && (
          <Button type="primary" onClick={handleSubmit} loading={isLoading}
            style={{ background: "#059669", borderColor: "#059669" }}>
            {editingTemplate ? "Сохранить изменения" : "Создать шаблон"}
          </Button>
        )}
      </Space>
    </div>
  );

  return (
    <Modal
      open={open}
      onCancel={onClose}
      afterOpenChange={(v) => v && handleOpen()}
      title={null}
      footer={footer}
      width={760}
      destroyOnClose
      styles={{ body: { padding: 0 } }}
    >
      {/* Modal header */}
      <div style={{
        background: "linear-gradient(135deg, #0F2A44, #1a4070)",
        borderRadius: "8px 8px 0 0",
        padding: "20px 28px 16px",
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
          <div style={{ width: 40, height: 40, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <FileCode size={20} color="#fff" />
          </div>
          <div>
            <Text style={{ color: "#fff", fontWeight: 700, fontSize: 16, display: "block" }}>
              {editingTemplate ? "Редактировать шаблон" : "Создать шаблон"}
            </Text>
            <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>
              {step === 0 ? "Шаг 1 из 2: Основная информация" : "Шаг 2 из 2: Поля документа"}
            </Text>
          </div>
        </div>
        <Steps
          current={step}
          size="small"
          style={{ maxWidth: 400 }}
          items={[
            { title: <Text style={{ color: step === 0 ? "#fff" : "rgba(255,255,255,0.5)", fontSize: 12 }}>Информация</Text> },
            { title: <Text style={{ color: step === 1 ? "#fff" : "rgba(255,255,255,0.5)", fontSize: 12 }}>Поля шаблона</Text> },
          ]}
        />
      </div>

      {/* Form content */}
      <div style={{ padding: "24px 28px", maxHeight: 520, overflowY: "auto" }}>
        <Form form={form} layout="vertical">
          {/* Step 0: Basic info */}
          <div style={{ display: step === 0 ? "block" : "none" }}>
            <Form.Item
              label={<Text style={{ fontWeight: 600 }}>Название шаблона</Text>}
              name="title"
              rules={[
                { required: true, message: "Введите название" },
                { min: 3, message: "Минимум 3 символа" },
                { max: 255, message: "Максимум 255 символов" },
              ]}
            >
              <Input
                size="large"
                placeholder="Например: Договор купли-продажи недвижимости"
                prefix={<FileText size={16} color="#9ca3af" />}
              />
            </Form.Item>

            <Form.Item
              label={
                <Space>
                  <Text style={{ fontWeight: 600 }}>Описание</Text>
                  <Tag style={{ fontSize: 10 }}>Необязательно</Tag>
                </Space>
              }
              name="description"
              rules={[{ max: 2000, message: "Максимум 2000 символов" }]}
            >
              <TextArea
                rows={3}
                placeholder="Краткое описание — для каких случаев подходит этот шаблон..."
                showCount maxLength={2000}
              />
            </Form.Item>

            <Form.Item
              label={<Text style={{ fontWeight: 600 }}>Категория</Text>}
              name="categoryId"
              rules={[{ required: true, message: "Выберите категорию" }]}
            >
              <Select
                size="large"
                showSearch
                placeholder="Выберите категорию документа"
                optionFilterProp="label"
                options={categories.map(c => ({ value: c.id, label: c.name }))}
              />
            </Form.Item>

            {/* Tip */}
            <div style={{ background: "#f0f9ff", borderRadius: 10, padding: "12px 16px", border: "1px solid #bae6fd" }}>
              <Text style={{ fontSize: 12, color: "#0369a1" }}>
                💡 На следующем шаге вы добавите поля, которые пользователь будет заполнять при создании документа.
              </Text>
            </div>
          </div>

          {/* Step 1: Fields */}
          <div style={{ display: step === 1 ? "block" : "none" }}>
            <div style={{ background: "#f0f9ff", borderRadius: 10, padding: "10px 14px", marginBottom: 18, border: "1px solid #bae6fd" }}>
              <Text style={{ fontSize: 12, color: "#0369a1" }}>
                💡 <strong>Ключ поля</strong> — это внутренний идентификатор, используемый в правилах и условиях. Автогенерируется из названия. Можно изменить вручную или нажать <RefreshCw size={10} style={{ verticalAlign: "middle" }} />.
              </Text>
            </div>

            <Form.List name="fields">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name: fieldName }, i) => (
                    <FieldCard
                      key={key}
                      name={fieldName}
                      index={i}
                      form={form}
                      canRemove={fields.length > 1}
                      onRemove={() => remove(fieldName)}
                    />
                  ))}
                  <Button
                    type="dashed"
                    onClick={() => add({ label: "", fieldKey: "", fieldType: "TEXT", required: false })}
                    icon={<PlusCircle size={14} />}
                    block
                    style={{ borderColor: "#0F2A44", color: "#0F2A44", borderRadius: 8, height: 40 }}
                  >
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

// ─── Main Panel ───────────────────────────────────────────────
export default function LawyerTemplatesPanel() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<Template | null>(null);

  const { data, isLoading } = useGetMyTemplatesQuery({ page: page - 1, size: 10, sort: "createdDate,DESC" });
  const { data: allData } = useGetMyTemplatesQuery({ page: 0, size: 1000 });

  const [deleteTemplate, { isLoading: isDeleting }] = useDeleteTemplateMutation();
  const [publishTemplate, { isLoading: isPublishing }] = usePublishTemplateMutation();

  const allTemplates = allData?.content ?? [];
  const draftCount = allTemplates.filter(t => t.status === "DRAFT").length;
  const publishedCount = allTemplates.filter(t => t.status === "PUBLISHED").length;
  const totalFields = allTemplates.reduce((sum, t) => sum + (t.fields?.length ?? 0), 0);

  const filtered = (data?.content ?? []).filter(t => {
    const q = search.toLowerCase();
    return t.title.toLowerCase().includes(q) || t.category?.name?.toLowerCase().includes(q);
  });

  const openCreate = () => { setEditingTemplate(null); setModalOpen(true); };
  const openEdit = (t: Template) => { setEditingTemplate(t); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditingTemplate(null); };

  const handleDelete = async (id: number) => {
    try {
      await deleteTemplate(id).unwrap();
      message.success("Шаблон удалён");
    } catch (e) {
      if (isFetchError(e) && e.status === 403) message.error("Нельзя удалить опубликованный шаблон");
      else message.error("Произошла ошибка");
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishTemplate(id).unwrap();
      message.success("Шаблон опубликован");
    } catch (e) {
      if (isFetchError(e) && e.status === 409) message.error("Шаблон уже опубликован");
      else if (isFetchError(e) && e.status === 403) message.error("Нет доступа");
      else message.error("Произошла ошибка");
    }
  };

  const columns: ColumnsType<Template> = [
    {
      title: "Шаблон",
      key: "title",
      render: (_, record) => (
        <div>
          <Text strong style={{ fontSize: 13 }}>{record.title}</Text>
          {record.description && (
            <Text type="secondary" style={{ fontSize: 11, display: "block", marginTop: 2 }}>
              {record.description.length > 80 ? record.description.slice(0, 80) + "…" : record.description}
            </Text>
          )}
        </div>
      ),
    },
    {
      title: "Категория",
      key: "category",
      width: 160,
      render: (_, record) => (
        <Tag color="geekblue" style={{ fontSize: 11 }}>{record.category?.name || "—"}</Tag>
      ),
    },
    {
      title: "Поля",
      key: "fields",
      width: 90,
      align: "center",
      render: (_, record) => {
        const types = record.fields?.map(f => f.fieldType) ?? [];
        return (
          <Tooltip title={record.fields?.map(f => `${f.label} (${f.fieldKey})`).join(", ") || "Нет полей"}>
            <Badge
              count={types.length}
              style={{ backgroundColor: "#1677ff", cursor: "help" }}
            />
          </Tooltip>
        );
      },
    },
    {
      title: "Статус",
      dataIndex: "status",
      key: "status",
      width: 130,
      align: "center",
      render: (status: string) =>
        status === "PUBLISHED"
          ? <Tag color="success" icon={<CheckCircle2 size={11} />} style={{ gap: 4 }}>Опубликован</Tag>
          : <Tag color="orange">Черновик</Tag>,
    },
    {
      title: "Создан",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 120,
      render: (date: string) => (
        <Text type="secondary" style={{ fontSize: 12 }}>
          {new Date(date).toLocaleDateString("ru-RU")}
        </Text>
      ),
    },
    {
      title: "",
      key: "actions",
      width: 120,
      align: "center",
      render: (_, record) => {
        const isDraft = record.status === "DRAFT";
        return (
          <Space size={4}>
            <Tooltip title={isDraft ? "Редактировать" : "Только черновик можно редактировать"}>
              <Button size="small" icon={<Pencil size={13} />}
                onClick={() => openEdit(record)} disabled={!isDraft} />
            </Tooltip>
            <Tooltip title={isDraft ? "Опубликовать" : "Уже опубликован"}>
              <Popconfirm
                title="Опубликовать шаблон?"
                description="После публикации шаблон нельзя изменить или удалить."
                onConfirm={() => handlePublish(record.id)}
                okText="Да" cancelText="Отмена" disabled={!isDraft}
              >
                <Button size="small" icon={<Send size={13} />} disabled={!isDraft}
                  loading={isPublishing}
                  style={isDraft ? { borderColor: "#1677ff", color: "#1677ff" } : {}} />
              </Popconfirm>
            </Tooltip>
            <Tooltip title={isDraft ? "Удалить" : "Нельзя удалить опубликованный"}>
              <Popconfirm
                title="Удалить шаблон?"
                onConfirm={() => handleDelete(record.id)}
                okText="Да" cancelText="Отмена" disabled={!isDraft}
              >
                <Button size="small" danger icon={<Trash2 size={13} />}
                  disabled={!isDraft} loading={isDeleting} />
              </Popconfirm>
            </Tooltip>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      {/* Header */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", borderRadius: 16, padding: "28px 36px", marginBottom: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ width: 52, height: 52, borderRadius: 14, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <FileCode size={26} color="#fff" />
          </div>
          <div style={{ flex: 1 }}>
            <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 800 }}>Мои шаблоны</Title>
            <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
              Создание и управление шаблонами юридических документов
            </Text>
          </div>
          <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
            {[
              { label: "Всего", value: data?.totalElements ?? 0 },
              { label: "Черновик", value: draftCount },
              { label: "Опубликован", value: publishedCount },
            ].map(s => (
              <div key={s.label} style={{ background: "rgba(255,255,255,0.12)", borderRadius: 10, padding: "8px 16px", textAlign: "center", minWidth: 72 }}>
                <Text style={{ color: "#fff", fontSize: 20, fontWeight: 700, display: "block", lineHeight: 1 }}>{s.value}</Text>
                <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 11 }}>{s.label}</Text>
              </div>
            ))}
            <Button icon={<Plus size={15} />} onClick={openCreate}
              style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "#fff", borderRadius: 8 }}>
              Создать
            </Button>
          </div>
        </div>
      </div>

      {/* Stat Cards */}
      <div style={{ display: "flex", gap: 14, marginBottom: 20 }}>
        <StatCard label="Всего шаблонов" value={data?.totalElements ?? 0} color="#0F2A44" icon={<FileCode size={18} color="#0F2A44" />} />
        <StatCard label="Черновики" value={draftCount} color="#f59e0b" icon={<FileText size={18} color="#f59e0b" />} />
        <StatCard label="Опубликованы" value={publishedCount} color="#059669" icon={<CheckCircle2 size={18} color="#059669" />} />
        <StatCard label="Всего полей" value={totalFields} color="#1677ff" icon={<List size={18} color="#1677ff" />} />
      </div>

      {/* Search + Table */}
      <div style={{ background: "#fff", borderRadius: 12, boxShadow: "0 1px 4px rgba(0,0,0,0.06)" }}>
        <div style={{ padding: "14px 20px", borderBottom: "1px solid #f0f0f0", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Input
            prefix={<Search size={14} color="#9ca3af" />}
            placeholder="Поиск по названию или категории..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ maxWidth: 340, borderRadius: 8 }}
            allowClear
          />
          <Text type="secondary" style={{ fontSize: 12 }}>
            Показано: <strong>{filtered.length}</strong> из <strong>{data?.totalElements ?? 0}</strong>
          </Text>
        </div>
        <Table
          columns={columns}
          dataSource={filtered}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: page,
            total: data?.totalElements,
            pageSize: 10,
            onChange: p => setPage(p),
            showSizeChanger: false,
            showTotal: total => `Всего ${total} шаблонов`,
          }}
          style={{ borderRadius: "0 0 12px 12px" }}
        />
      </div>

      {/* Form modal */}
      <TemplateFormModal
        open={modalOpen}
        onClose={closeModal}
        editingTemplate={editingTemplate}
        onSuccess={closeModal}
      />
    </div>
  );
}
