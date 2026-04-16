import { useState } from "react";
import {
  App, Table, Button, Modal, Form, Input, Select,
  Space, Typography, Tabs, Switch, Badge, Empty, InputNumber,
} from "antd";
import { useIsMobile } from "../../../shared/hooks/use-is-mobile";
import type { ColumnsType } from "antd/es/table";
import {
  ShieldAlert, GitBranch, FileCheck2, FileBadge2,
  Plus, Pencil, Trash2, GitMerge,
} from "lucide-react";
import {
  useGetValidationRulesByTemplateQuery,
  useCreateValidationRuleMutation,
  useUpdateValidationRuleMutation,
  useDeleteValidationRuleMutation,
  type ValidationRule,
  type ValidationRuleRequest,
  type RuleConditionOperator,
} from "../../../features/lawyer/api/lawyer-validation-rules-api";
import {
  useGetRiskRulesByTemplateQuery,
  useCreateRiskRuleMutation,
  useUpdateRiskRuleMutation,
  useDeleteRiskRuleMutation,
  type RiskRule,
  type RiskRuleRequest,
} from "../../../features/lawyer/api/lawyer-risk-rules-api";
import {
  useGetConditionalRulesByTemplateQuery,
  useCreateConditionalRuleMutation,
  useUpdateConditionalRuleMutation,
  useDeleteConditionalRuleMutation,
  type ConditionalRule,
  type ConditionalRuleRequest,
} from "../../../features/lawyer/api/lawyer-conditional-rules-api";
import {
  useGetRequiredDocRulesByTemplateQuery,
  useCreateRequiredDocRuleMutation,
  useUpdateRequiredDocRuleMutation,
  useDeleteRequiredDocRuleMutation,
  type RequiredDocRule,
  type RequiredDocRuleRequest,
} from "../../../features/lawyer/api/lawyer-required-doc-rules-api";
import {
  useGetRulesByTemplateQuery,
  useCreateRuleMutation,
  useUpdateRuleMutation,
  useDeleteRuleMutation,
  type MatchingRule,
  type MatchingRuleRequest,
} from "../../../features/lawyer/api/lawyer-matching-rules-api";
import { useGetMyTemplatesQuery } from "../../../features/lawyer/api/lawyer-template-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";

const { Text } = Typography;

const isFetchError = (e: unknown): e is { status: number } =>
  typeof e === "object" && e !== null && "status" in e;

const OPERATORS: { value: RuleConditionOperator; label: string }[] = [
  { value: "EQUALS", label: "= Равно" },
  { value: "NOT_EQUALS", label: "≠ Не равно" },
  { value: "CONTAINS", label: "Содержит" },
  { value: "GREATER_THAN", label: "> Больше" },
  { value: "LESS_THAN", label: "< Меньше" },
  { value: "GREATER_OR_EQUAL", label: "≥ Больше или равно" },
  { value: "LESS_OR_EQUAL", label: "≤ Меньше или равно" },
  { value: "IS_EMPTY", label: "Пустое" },
  { value: "IS_NOT_EMPTY", label: "Не пустое" },
];

const RISK_LEVELS = [
  { value: "HIGH", label: "Высокий" },
  { value: "MEDIUM", label: "Средний" },
  { value: "LOW", label: "Низкий" },
];

// ─── Ghost icon button ────────────────────────────────────────
function IconBtn({ icon, tooltip, onClick, hoverBg, hoverColor }: {
  icon: React.ReactNode; tooltip: string; onClick?: () => void;
  hoverBg: string; hoverColor: string;
}) {
  return (
    <button title={tooltip} onClick={onClick}
      style={{ width: 30, height: 30, borderRadius: 7, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
      onMouseEnter={(e) => { e.currentTarget.style.background = hoverBg; e.currentTarget.style.color = hoverColor; }}
      onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
    >
      {icon}
    </button>
  );
}

// ─── Status pill ──────────────────────────────────────────────
function ActivePill({ active }: { active: boolean }) {
  const cfg = active
    ? { color: "#059669", bg: "rgba(5,150,105,0.08)", border: "rgba(5,150,105,0.2)", label: "Активно" }
    : { color: "#94a3b8", bg: "rgba(148,163,184,0.08)", border: "rgba(148,163,184,0.2)", label: "Выкл" };
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "2px 8px", borderRadius: 20, background: cfg.bg, border: `1px solid ${cfg.border}` }}>
      <span style={{ width: 5, height: 5, borderRadius: "50%", background: cfg.color }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: cfg.color }}>{cfg.label}</span>
    </div>
  );
}

// ─── Modal header ─────────────────────────────────────────────
function ModalHeader({ icon, title, subtitle }: { icon: React.ReactNode; title: string; subtitle: string }) {
  return (
    <div style={{ background: "linear-gradient(135deg, #0F2A44, #1a4070)", borderRadius: "8px 8px 0 0", padding: "20px 28px 16px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ width: 38, height: 38, borderRadius: 10, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
          {icon}
        </div>
        <div>
          <Text style={{ color: "#fff", fontWeight: 700, fontSize: 15, display: "block" }}>{title}</Text>
          <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 12 }}>{subtitle}</Text>
        </div>
      </div>
    </div>
  );
}

// ─── Validation Tab ───────────────────────────
function ValidationTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ValidationRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetValidationRulesByTemplateQuery(templateId);
  const [create, { isLoading: isCreating }] = useCreateValidationRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateValidationRuleMutation();
  const [remove] = useDeleteValidationRuleMutation();

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true); };
  const openEdit = (r: ValidationRule) => {
    setEditing(r);
    form.setFieldsValue({ fieldKey: r.fieldKey, fieldLabel: r.fieldLabel, operator: r.operator, expectedValue: r.expectedValue, errorMessage: r.errorMessage });
    setOpen(true);
  };
  const handleDelete = (id: number) => remove(id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"));

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body: ValidationRuleRequest = { ...values, templateId };
    try {
      if (editing) { await update({ id: editing.id, body }).unwrap(); message.success("Обновлено"); }
      else { await create(body).unwrap(); message.success("Создано"); }
      setOpen(false);
    } catch (e) {
      message.error(isFetchError(e) && e.status === 400 ? "Ошибка валидации данных" : "Произошла ошибка");
    }
  };

  const columns: ColumnsType<ValidationRule> = [
    { title: "Поле", key: "field", render: (_, r) => (
        <div>
          <Text strong style={{ fontSize: 13, color: "#0b1c30" }}>{r.fieldLabel}</Text>
          <Text style={{ fontSize: 11, color: "#94a3b8", display: "block", fontFamily: "monospace" }}>{r.fieldKey}</Text>
        </div>
    ) },
    { title: "Оператор", dataIndex: "operator", key: "operator", width: 160,
      render: (v: string) => <span style={{ fontSize: 11, fontFamily: "monospace", color: "#1677ff", background: "rgba(22,119,255,0.08)", padding: "2px 8px", borderRadius: 6 }}>{OPERATORS.find(o => o.value === v)?.label ?? v}</span> },
    { title: "Ожидаемое значение", dataIndex: "expectedValue", key: "expectedValue",
      render: (v: string) => <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#475569" }}>{v || "—"}</Text> },
    { title: "Сообщение", dataIndex: "errorMessage", key: "errorMessage",
      render: (v: string) => <Text style={{ fontSize: 12, color: "#64748b" }}>{v}</Text> },
    { title: "Статус", dataIndex: "active", key: "active", width: 100, render: (v: boolean) => <ActivePill active={v} /> },
    { title: "", key: "actions", width: 80, align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2 }}>
          <IconBtn icon={<Pencil size={13} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(r)} />
          <IconBtn icon={<Trash2 size={13} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(r.id)} />
        </div>
      ) },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#1677ff", borderColor: "#1677ff", color: "#fff", borderRadius: 8 }}>Добавить правило</Button>
      </div>
      <Table components={editorialTableComponents} columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил валидации для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12, overflow: "hidden" }} />
      <Modal open={open} onCancel={() => setOpen(false)} title={null}
        footer={<div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}><Button onClick={() => setOpen(false)}>Отмена</Button><Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating} style={{ background: "#1677ff", borderColor: "#1677ff" }}>{editing ? "Сохранить" : "Создать"}</Button></div>}
        destroyOnHidden styles={{ body: { padding: 0 } }}>
        <ModalHeader icon={<FileCheck2 size={18} color="#fff" />} title={editing ? "Редактировать правило валидации" : "Новое правило валидации"} subtitle="Проверка значений полей документа" />
        <div style={{ padding: "20px 28px" }}>
          <Form form={form} layout="vertical">
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ключ поля</Text>} name="fieldKey" rules={[{ required: true, message: "Введите ключ поля" }]}>
              <Input placeholder="Например: contract_amount" style={{ fontFamily: "monospace" }} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Название поля</Text>} name="fieldLabel" rules={[{ required: true, message: "Введите название" }]}>
              <Input placeholder="Например: Сумма договора" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Оператор</Text>} name="operator" rules={[{ required: true, message: "Выберите оператор" }]}>
              <Select options={OPERATORS} placeholder="Выберите оператор" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ожидаемое значение</Text>} name="expectedValue">
              <Input placeholder="Оставьте пустым для IS_EMPTY / IS_NOT_EMPTY" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Сообщение об ошибке</Text>} name="errorMessage" rules={[{ required: true, message: "Введите сообщение" }]}>
              <Input placeholder="Например: Поле обязательно для заполнения" />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
}

// ─── Risk Tab ─────────────────────────────────
function RiskTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<RiskRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetRiskRulesByTemplateQuery(templateId);
  const [create, { isLoading: isCreating }] = useCreateRiskRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateRiskRuleMutation();
  const [remove] = useDeleteRiskRuleMutation();

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true); };
  const openEdit = (r: RiskRule) => {
    setEditing(r);
    form.setFieldsValue({ ruleCode: r.ruleCode, fieldKey: r.fieldKey, operator: r.operator, expectedValue: r.expectedValue, riskMessage: r.riskMessage, riskLevel: r.riskLevel });
    setOpen(true);
  };
  const handleDelete = (id: number) => remove(id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"));

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body: RiskRuleRequest = { ...values, templateId };
    try {
      if (editing) { await update({ id: editing.id, body }).unwrap(); message.success("Обновлено"); }
      else { await create(body).unwrap(); message.success("Создано"); }
      setOpen(false);
    } catch (e) {
      message.error(isFetchError(e) && e.status === 400 ? "Ошибка: проверьте формат кода правила (A-Z, 0-9, _)" : "Произошла ошибка");
    }
  };

  const levelStyle: Record<string, { color: string; bg: string }> = {
    HIGH:   { color: "#ef4444", bg: "rgba(239,68,68,0.08)" },
    MEDIUM: { color: "#f59e0b", bg: "rgba(245,158,11,0.08)" },
    LOW:    { color: "#059669", bg: "rgba(5,150,105,0.08)" },
  };
  const levelLabel: Record<string, string> = { HIGH: "Высокий", MEDIUM: "Средний", LOW: "Низкий" };

  const columns: ColumnsType<RiskRule> = [
    { title: "Код", dataIndex: "ruleCode", key: "ruleCode",
      render: (v: string) => <span style={{ fontSize: 11, fontFamily: "monospace", fontWeight: 700, color: "#ef4444", background: "rgba(239,68,68,0.06)", padding: "2px 8px", borderRadius: 6 }}>{v}</span> },
    { title: "Поле / Условие", key: "condition",
      render: (_, r) => <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#475569" }}>{r.fieldKey} {OPERATORS.find(o => o.value === r.operator)?.label} {r.expectedValue || ""}</Text> },
    { title: "Сообщение", dataIndex: "riskMessage", key: "riskMessage",
      render: (v: string) => <Text style={{ fontSize: 12, color: "#64748b" }}>{v}</Text> },
    { title: "Уровень", dataIndex: "riskLevel", key: "riskLevel", width: 110, align: "center" as const,
      render: (v: string) => {
        const s = levelStyle[v] ?? { color: "#64748b", bg: "rgba(100,116,139,0.08)" };
        return <span style={{ fontSize: 11, fontWeight: 700, color: s.color, background: s.bg, padding: "2px 10px", borderRadius: 20 }}>{levelLabel[v] ?? v}</span>;
      } },
    { title: "Статус", dataIndex: "active", key: "active", width: 100, render: (v: boolean) => <ActivePill active={v} /> },
    { title: "", key: "actions", width: 80, align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2 }}>
          <IconBtn icon={<Pencil size={13} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(r)} />
          <IconBtn icon={<Trash2 size={13} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(r.id)} />
        </div>
      ) },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#ef4444", borderColor: "#ef4444", color: "#fff", borderRadius: 8 }}>Добавить риск</Button>
      </div>
      <Table components={editorialTableComponents} columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил риска для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12, overflow: "hidden" }} />
      <Modal open={open} onCancel={() => setOpen(false)} title={null}
        footer={<div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}><Button onClick={() => setOpen(false)}>Отмена</Button><Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating} style={{ background: "#ef4444", borderColor: "#ef4444" }}>{editing ? "Сохранить" : "Создать"}</Button></div>}
        destroyOnHidden styles={{ body: { padding: 0 } }}>
        <ModalHeader icon={<ShieldAlert size={18} color="#fff" />} title={editing ? "Редактировать правило риска" : "Новое правило риска"} subtitle="Определение уровня риска по условию поля" />
        <div style={{ padding: "20px 28px" }}>
          <Form form={form} layout="vertical">
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Код правила</Text>} name="ruleCode"
              rules={[{ required: true, message: "Введите код" }, { pattern: /^[A-Z0-9_]+$/, message: "Только A-Z, 0-9, _" }]}>
              <Input placeholder="Например: HIGH_LIABILITY_RISK" style={{ textTransform: "uppercase", fontFamily: "monospace" }} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ключ поля</Text>} name="fieldKey" rules={[{ required: true, message: "Введите ключ поля" }]}>
              <Input placeholder="Например: contract_amount" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Оператор</Text>} name="operator" rules={[{ required: true, message: "Выберите оператор" }]}>
              <Select options={OPERATORS} placeholder="Выберите оператор" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ожидаемое значение</Text>} name="expectedValue">
              <Input placeholder="Например: 1000000" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Уровень риска</Text>} name="riskLevel" rules={[{ required: true, message: "Выберите уровень" }]}>
              <Select options={RISK_LEVELS} placeholder="Выберите уровень" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Сообщение о риске</Text>} name="riskMessage" rules={[{ required: true, message: "Введите сообщение" }]}>
              <Input.TextArea rows={2} placeholder="Описание риска для пользователя" />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
}

// ─── Conditional Tab ──────────────────────────
function ConditionalTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ConditionalRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetConditionalRulesByTemplateQuery(templateId);
  const [create, { isLoading: isCreating }] = useCreateConditionalRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateConditionalRuleMutation();
  const [remove] = useDeleteConditionalRuleMutation();

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true); };
  const openEdit = (r: ConditionalRule) => {
    setEditing(r);
    form.setFieldsValue({ conditionFieldKey: r.conditionFieldKey, operator: r.operator, conditionValue: r.conditionValue, targetFieldKey: r.targetFieldKey });
    setOpen(true);
  };
  const handleDelete = (id: number) => remove(id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"));

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body: ConditionalRuleRequest = { ...values, templateId };
    try {
      if (editing) { await update({ id: editing.id, body }).unwrap(); message.success("Обновлено"); }
      else { await create(body).unwrap(); message.success("Создано"); }
      setOpen(false);
    } catch { message.error("Произошла ошибка"); }
  };

  const columns: ColumnsType<ConditionalRule> = [
    { title: "Если (IF)", key: "condition",
      render: (_, r) => <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#1677ff" }}>{r.conditionFieldKey} {OPERATORS.find(o => o.value === r.operator)?.label} "{r.conditionValue}"</Text> },
    { title: "То показать (THEN)", dataIndex: "targetFieldKey", key: "targetFieldKey",
      render: (v: string) => <span style={{ fontSize: 12, fontFamily: "monospace", color: "#7c3aed", background: "rgba(124,58,237,0.08)", padding: "2px 8px", borderRadius: 6 }}>{v}</span> },
    { title: "Статус", dataIndex: "active", key: "active", width: 100, render: (v: boolean) => <ActivePill active={v} /> },
    { title: "", key: "actions", width: 80, align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2 }}>
          <IconBtn icon={<Pencil size={13} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(r)} />
          <IconBtn icon={<Trash2 size={13} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(r.id)} />
        </div>
      ) },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#7c3aed", borderColor: "#7c3aed", color: "#fff", borderRadius: 8 }}>Добавить условие</Button>
      </div>
      <Table components={editorialTableComponents} columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет условных правил для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12, overflow: "hidden" }} />
      <Modal open={open} onCancel={() => setOpen(false)} title={null} width={540}
        footer={<div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}><Button onClick={() => setOpen(false)}>Отмена</Button><Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating} style={{ background: "#7c3aed", borderColor: "#7c3aed" }}>{editing ? "Сохранить" : "Создать"}</Button></div>}
        destroyOnHidden styles={{ body: { padding: 0 } }}>
        <ModalHeader icon={<GitBranch size={18} color="#fff" />} title={editing ? "Редактировать IF/THEN правило" : "Новое IF/THEN правило"} subtitle="Условная видимость полей документа" />
        <div style={{ padding: "20px 28px" }}>
          <Form form={form} layout="vertical">
            <div style={{ background: "#eff6ff", borderRadius: 8, padding: "8px 14px", marginBottom: 12 }}>
              <Text strong style={{ color: "#1d4ed8", fontSize: 12 }}>IF — Условие</Text>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
              <Form.Item label="Поле" name="conditionFieldKey" rules={[{ required: true, message: "Обязательно" }]}>
                <Input placeholder="Ключ поля" />
              </Form.Item>
              <Form.Item label="Оператор" name="operator" rules={[{ required: true, message: "Обязательно" }]}>
                <Select options={OPERATORS} placeholder="Оператор" />
              </Form.Item>
              <Form.Item label="Значение" name="conditionValue" rules={[{ required: true, message: "Обязательно" }]}>
                <Input placeholder="Значение" />
              </Form.Item>
            </div>
            <div style={{ background: "#f5f3ff", borderRadius: 8, padding: "8px 14px", marginBottom: 12 }}>
              <Text strong style={{ color: "#7c3aed", fontSize: 12 }}>THEN — Показать поле</Text>
            </div>
            <Form.Item label="Ключ целевого поля" name="targetFieldKey" rules={[{ required: true, message: "Обязательно" }]}>
              <Input placeholder="Ключ поля которое нужно показать" />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
}

// ─── Required Doc Tab ─────────────────────────
function RequiredDocTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<RequiredDocRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetRequiredDocRulesByTemplateQuery(templateId);
  const [create, { isLoading: isCreating }] = useCreateRequiredDocRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateRequiredDocRuleMutation();
  const [remove] = useDeleteRequiredDocRuleMutation();

  const openCreate = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ mandatory: true }); setOpen(true); };
  const openEdit = (r: RequiredDocRule) => {
    setEditing(r);
    form.setFieldsValue({ requiredDocTitle: r.requiredDocTitle, reason: r.reason, mandatory: r.mandatory, conditionFieldKey: r.conditionFieldKey, conditionOperator: r.conditionOperator, conditionValue: r.conditionValue });
    setOpen(true);
  };
  const handleDelete = (id: number) => remove(id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"));

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body: RequiredDocRuleRequest = { ...values, templateId };
    try {
      if (editing) { await update({ id: editing.id, body }).unwrap(); message.success("Обновлено"); }
      else { await create(body).unwrap(); message.success("Создано"); }
      setOpen(false);
    } catch { message.error("Произошла ошибка"); }
  };

  const columns: ColumnsType<RequiredDocRule> = [
    { title: "Документ", dataIndex: "requiredDocTitle", key: "requiredDocTitle",
      render: (v: string) => <Text strong style={{ fontSize: 13, color: "#0b1c30" }}>{v}</Text> },
    { title: "Причина", dataIndex: "reason", key: "reason",
      render: (v: string) => <Text style={{ fontSize: 12, color: "#64748b" }}>{v}</Text> },
    { title: "Условие", key: "condition",
      render: (_, r) => r.conditionFieldKey
        ? <Text style={{ fontSize: 11, fontFamily: "monospace", color: "#7c3aed" }}>{r.conditionFieldKey} {r.conditionOperator} {r.conditionValue}</Text>
        : <span style={{ fontSize: 11, color: "#94a3b8", fontStyle: "italic" }}>Всегда</span> },
    { title: "Обяз.", dataIndex: "mandatory", key: "mandatory", width: 70, align: "center" as const,
      render: (v: boolean) => <Switch checked={v} disabled size="small" /> },
    { title: "Статус", dataIndex: "active", key: "active", width: 100, render: (v: boolean) => <ActivePill active={v} /> },
    { title: "", key: "actions", width: 80, align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2 }}>
          <IconBtn icon={<Pencil size={13} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(r)} />
          <IconBtn icon={<Trash2 size={13} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(r.id)} />
        </div>
      ) },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#059669", borderColor: "#059669", color: "#fff", borderRadius: 8 }}>Добавить документ</Button>
      </div>
      <Table components={editorialTableComponents} columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет требований к документам" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12, overflow: "hidden" }} />
      <Modal open={open} onCancel={() => setOpen(false)} title={null} width={520}
        footer={<div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}><Button onClick={() => setOpen(false)}>Отмена</Button><Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating} style={{ background: "#059669", borderColor: "#059669" }}>{editing ? "Сохранить" : "Создать"}</Button></div>}
        destroyOnHidden styles={{ body: { padding: 0 } }}>
        <ModalHeader icon={<FileBadge2 size={18} color="#fff" />} title={editing ? "Редактировать требование" : "Новое требование к документу"} subtitle="Документы необходимые для данного шаблона" />
        <div style={{ padding: "20px 28px" }}>
          <Form form={form} layout="vertical">
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Название документа</Text>} name="requiredDocTitle" rules={[{ required: true, message: "Введите название" }]}>
              <Input placeholder="Например: Паспорт гражданина РК" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Причина требования</Text>} name="reason" rules={[{ required: true, message: "Введите причину" }]}>
              <Input.TextArea rows={2} placeholder="Почему этот документ необходим" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Обязательный</Text>} name="mandatory" valuePropName="checked">
              <Switch checkedChildren="Да" unCheckedChildren="Нет" />
            </Form.Item>
            <div style={{ background: "#f5f3ff", borderRadius: 8, padding: "8px 14px", marginBottom: 12 }}>
              <Text style={{ fontSize: 12, color: "#7c3aed" }}>Условие (опционально) — если пусто, документ требуется всегда</Text>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
              <Form.Item label="Поле" name="conditionFieldKey"><Input placeholder="Ключ поля" /></Form.Item>
              <Form.Item label="Оператор" name="conditionOperator"><Select options={OPERATORS} placeholder="Оператор" allowClear /></Form.Item>
              <Form.Item label="Значение" name="conditionValue"><Input placeholder="Значение" /></Form.Item>
            </div>
          </Form>
        </div>
      </Modal>
    </>
  );
}

// ─── Matching Tab ─────────────────────────────
function MatchingTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<MatchingRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetRulesByTemplateQuery(templateId);
  const { data: categoriesData } = useGetActiveCategoriesQuery();
  const categories = categoriesData ?? [];

  const [create, { isLoading: isCreating }] = useCreateRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateRuleMutation();
  const [remove] = useDeleteRuleMutation();

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true); };
  const openEdit = (r: MatchingRule) => {
    setEditing(r);
    form.setFieldsValue({ templateId: r.templateId, categoryId: r.categoryId, keywords: r.keywords, baseScore: r.baseScore });
    setOpen(true);
  };
  const handleDelete = (id: number) => remove(id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"));

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const body: MatchingRuleRequest = { ...values, templateId };
    try {
      if (editing) { await update({ id: editing.id, body }).unwrap(); message.success("Обновлено"); }
      else { await create(body).unwrap(); message.success("Создано"); }
      setOpen(false);
    } catch (e) {
      message.error(isFetchError(e) && e.status === 400 ? "Ошибка валидации" : "Произошла ошибка");
    }
  };

  const columns: ColumnsType<MatchingRule> = [
    { title: "Категория", dataIndex: "categoryName", key: "categoryName",
      render: (v: string) => <span style={{ fontSize: 11, fontWeight: 600, color: "#0F2A44", background: "rgba(15,42,68,0.06)", padding: "3px 10px", borderRadius: 20, border: "1px solid rgba(15,42,68,0.12)" }}>{v || "—"}</span> },
    { title: "Ключевые слова", dataIndex: "keywords", key: "keywords",
      render: (v: string) => <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#1677ff" }}>{v}</Text> },
    { title: "Базовый балл", dataIndex: "baseScore", key: "baseScore", width: 120, align: "center" as const,
      render: (v: number) => <span style={{ fontFamily: "monospace", fontWeight: 700, color: "#7c3aed" }}>{v}</span> },
    { title: "Статус", dataIndex: "active", key: "active", width: 100, render: (v: boolean) => <ActivePill active={v} /> },
    { title: "", key: "actions", width: 80, align: "center" as const,
      render: (_, r) => (
        <div style={{ display: "flex", gap: 2 }}>
          <IconBtn icon={<Pencil size={13} />} tooltip="Редактировать" hoverBg="#f0f9ff" hoverColor="#1677ff" onClick={() => openEdit(r)} />
          <IconBtn icon={<Trash2 size={13} />} tooltip="Удалить" hoverBg="#fff1f2" hoverColor="#ef4444" onClick={() => handleDelete(r.id)} />
        </div>
      ) },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#0F2A44", borderColor: "#0F2A44", color: "#fff", borderRadius: 8 }}>Добавить правило</Button>
      </div>
      <Table components={editorialTableComponents} columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил матчинга для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12, overflow: "hidden" }} />
      <Modal open={open} onCancel={() => setOpen(false)} title={null}
        footer={<div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}><Button onClick={() => setOpen(false)}>Отмена</Button><Button type="primary" onClick={handleSubmit} loading={isCreating || isUpdating} style={{ background: "#0F2A44", borderColor: "#0F2A44" }}>{editing ? "Сохранить" : "Создать"}</Button></div>}
        destroyOnHidden styles={{ body: { padding: 0 } }}>
        <ModalHeader icon={<GitMerge size={18} color="#fff" />} title={editing ? "Редактировать правило матчинга" : "Новое правило матчинга"} subtitle="Подбор шаблона для клиентских запросов" />
        <div style={{ padding: "20px 28px" }}>
          <Form form={form} layout="vertical">
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Категория</Text>} name="categoryId" rules={[{ required: true, message: "Выберите категорию" }]}>
              <Select showSearch placeholder="Выберите категорию"
                filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
                options={categories.map((c) => ({ value: c.id, label: c.name }))} />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Ключевые слова</Text>} name="keywords" rules={[{ required: true, message: "Введите ключевые слова" }]}>
              <Input placeholder="Например: купля, продажа, договор" />
            </Form.Item>
            <Form.Item label={<Text style={{ fontWeight: 600 }}>Базовый балл (0–100)</Text>} name="baseScore" rules={[{ required: true, message: "Укажите балл" }]}>
              <InputNumber style={{ width: "100%" }} min={0} max={100} placeholder="50" />
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
}

// ─── Main Panel ───────────────────────────────
export default function LawyerRuleManagerPanel() {
  const isMobile = useIsMobile();
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
  const [activeTab, setActiveTab] = useState("validation");

  const { data: templatesData } = useGetMyTemplatesQuery({ page: 0, size: 100 });
  const templates = templatesData?.content ?? [];

  const { data: validationRules = [] } = useGetValidationRulesByTemplateQuery(selectedTemplateId!, { skip: !selectedTemplateId });
  const { data: riskRules = [] } = useGetRiskRulesByTemplateQuery(selectedTemplateId!, { skip: !selectedTemplateId });
  const { data: conditionalRules = [] } = useGetConditionalRulesByTemplateQuery(selectedTemplateId!, { skip: !selectedTemplateId });
  const { data: requiredDocRules = [] } = useGetRequiredDocRulesByTemplateQuery(selectedTemplateId!, { skip: !selectedTemplateId });
  const { data: matchingRules = [] } = useGetRulesByTemplateQuery(selectedTemplateId!, { skip: !selectedTemplateId });

  const totalRules = validationRules.length + riskRules.length + conditionalRules.length + requiredDocRules.length + matchingRules.length;

  const tabItems = [
    {
      key: "validation",
      label: <Space><FileCheck2 size={14} /><span>Валидация</span>{selectedTemplateId && <Badge count={validationRules.length} color="#1677ff" />}</Space>,
      children: selectedTemplateId ? <ValidationTab templateId={selectedTemplateId} /> : null,
    },
    {
      key: "risk",
      label: <Space><ShieldAlert size={14} /><span>Риски</span>{selectedTemplateId && <Badge count={riskRules.length} color="#ef4444" />}</Space>,
      children: selectedTemplateId ? <RiskTab templateId={selectedTemplateId} /> : null,
    },
    {
      key: "conditional",
      label: <Space><GitBranch size={14} /><span>IF / THEN</span>{selectedTemplateId && <Badge count={conditionalRules.length} color="#7c3aed" />}</Space>,
      children: selectedTemplateId ? <ConditionalTab templateId={selectedTemplateId} /> : null,
    },
    {
      key: "required-docs",
      label: <Space><FileBadge2 size={14} /><span>Доп. документы</span>{selectedTemplateId && <Badge count={requiredDocRules.length} color="#059669" />}</Space>,
      children: selectedTemplateId ? <RequiredDocTab templateId={selectedTemplateId} /> : null,
    },
    {
      key: "matching",
      label: <Space><GitMerge size={14} /><span>Матчинг</span>{selectedTemplateId && <Badge count={matchingRules.length} color="#0F2A44" />}</Space>,
      children: selectedTemplateId ? <MatchingTab templateId={selectedTemplateId} /> : null,
    },
  ];

  return (
    <div style={{ overflowX: "hidden" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", top: 0, right: 0, width: "50%", height: "100%", background: "linear-gradient(to left, rgba(173,199,247,0.07), transparent)", pointerEvents: "none" }} />
        <div style={{ padding: isMobile ? "24px 16px 48px" : "40px 40px 56px", display: "flex", justifyContent: "space-between", alignItems: "flex-end", position: "relative", zIndex: 1 }}>
          <div>
            <h1 style={{ fontSize: 36, fontWeight: 800, color: "#fff", margin: "0 0 8px", fontFamily: "Manrope, sans-serif", letterSpacing: "-0.02em" }}>Rule Engine</h1>
            <p style={{ color: "rgba(186,213,255,0.75)", fontSize: 14, margin: 0 }}>Управление правилами валидации, рисков, условий и матчинга</p>
          </div>
          {selectedTemplateId && (
            <div style={{ display: "flex", gap: 8, alignItems: "center", background: "rgba(255,255,255,0.05)", backdropFilter: "blur(12px)", borderRadius: 12, border: "1px solid rgba(255,255,255,0.1)", padding: "12px 20px" }}>
              {[
                { label: "Валидация", value: validationRules.length, color: "#60a5fa" },
                { label: "Риски", value: riskRules.length, color: "#f87171" },
                { label: "IF/THEN", value: conditionalRules.length, color: "#c084fc" },
                { label: "Документы", value: requiredDocRules.length, color: "#34d399" },
                { label: "Матчинг", value: matchingRules.length, color: "#94a3b8" },
              ].map((s, i, arr) => (
                <>
                  <div key={s.label} style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 18, fontWeight: 800, color: s.color, lineHeight: 1, fontFamily: "Manrope, sans-serif" }}>{s.value}</div>
                    <div style={{ fontSize: 10, fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.08em", color: "rgba(255,255,255,0.5)", marginTop: 3 }}>{s.label}</div>
                  </div>
                  {i < arr.length - 1 && <div key={`div-${i}`} style={{ width: 1, height: 32, background: "rgba(255,255,255,0.1)" }} />}
                </>
              ))}
            </div>
          )}
        </div>
      </div>

      <div style={{ padding: isMobile ? "0 12px 24px" : "0 32px 32px" }}>
        {/* ── Stat cards ── */}
        <div style={{ display: "flex", gap: 20, marginTop: -28, marginBottom: 28, position: "relative", zIndex: 2 }}>
          <StatCard label="Валидация"     value={validationRules.length}  color="#1677ff" icon={<FileCheck2 size={26} />} />
          <StatCard label="Риски"         value={riskRules.length}        color="#ef4444" icon={<ShieldAlert size={26} />} />
          <StatCard label="IF/THEN"       value={conditionalRules.length} color="#7c3aed" icon={<GitBranch size={26} />} />
          <StatCard label="Доп. документы" value={requiredDocRules.length} color="#059669" icon={<FileBadge2 size={26} />} />
          <StatCard label="Матчинг"       value={matchingRules.length}    color="#0F2A44" icon={<GitMerge size={26} />} />
        </div>

        {/* ── Template selector ── */}
        <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", borderRadius: 16, padding: "20px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(11,28,48,0.06)", border: "1px solid rgba(197,198,210,0.15)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <Text strong style={{ color: "#0F2A44", whiteSpace: "nowrap", fontSize: 13, fontWeight: 700 }}>Шаблон:</Text>
            <Select showSearch style={{ flex: 1, maxWidth: 520 }}
              placeholder="Выберите шаблон для управления правилами..."
              filterOption={(input, opt) => String(opt?.label ?? "").toLowerCase().includes(input.toLowerCase())}
              value={selectedTemplateId} onChange={(v) => setSelectedTemplateId(v)}
              options={templates.map((t) => ({ value: t.id, label: `${t.title} · ${t.category?.name ?? "—"}` }))}
              allowClear onClear={() => setSelectedTemplateId(null)} />
            {selectedTemplateId && (
              <Text style={{ fontSize: 12, color: "#94a3b8", whiteSpace: "nowrap" }}>
                Всего правил: <Text strong style={{ color: "#0b1c30" }}>{totalRules}</Text>
              </Text>
            )}
          </div>
        </div>

        {/* ── Tabs ── */}
        <div style={{ background: "#fff", borderRadius: 16, padding: "0 24px 24px", boxShadow: "0 8px 32px rgba(11,28,48,0.04)", minHeight: 300 }}>
          {!selectedTemplateId ? (
            <div style={{ padding: "80px 0", textAlign: "center" }}>
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE}
                description={<span style={{ color: "#94a3b8", fontSize: 14 }}>Выберите шаблон выше для управления правилами</span>} />
            </div>
          ) : (
            <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} style={{ paddingTop: 8 }} />
          )}
        </div>
      </div>
    </div>
  );
}
