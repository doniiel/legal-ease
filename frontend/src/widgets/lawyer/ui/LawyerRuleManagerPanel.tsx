import { useState } from "react";
import {
  App, Table, Button, Modal, Form, Input, Select, Tag,
  Popconfirm, Space, Typography, Tabs, Switch, Badge, Empty, InputNumber,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  ShieldAlert, GitBranch, FileCheck2, FileBadge2,
  Plus, Pencil, Trash2, Search, GitMerge,
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

const { Text, Title } = Typography;

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
  { value: "HIGH", label: "Высокий", color: "error" },
  { value: "MEDIUM", label: "Средний", color: "warning" },
  { value: "LOW", label: "Низкий", color: "success" },
];

// ─── Stat Cards ───────────────────────────────
function StatBadge({ label, value }: { label: string; value: number }) {
  return (
    <div style={{
      background: "rgba(255,255,255,0.12)", borderRadius: 10,
      padding: "8px 16px", textAlign: "center", minWidth: 72,
    }}>
      <Text style={{ color: "#fff", fontSize: 20, fontWeight: 700, display: "block", lineHeight: 1 }}>{value}</Text>
      <Text style={{ color: "rgba(255,255,255,0.6)", fontSize: 11 }}>{label}</Text>
    </div>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div style={{
      background: "#fff", borderRadius: 12, padding: "18px 22px", flex: 1,
      borderTop: `3px solid ${color}`, boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
    }}>
      <Text style={{ fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", color: "#8c8fa3" }}>
        {label}
      </Text>
      <div style={{ fontSize: 32, fontWeight: 800, color, marginTop: 6, lineHeight: 1 }}>{value}</div>
    </div>
  );
}

// ─── Validation Tab ───────────────────────────
function ValidationTab({ templateId }: { templateId: number }) {
  const { message } = App.useApp();
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ValidationRule | null>(null);
  const [form] = Form.useForm();

  const { data: rules = [], isLoading } = useGetValidationRulesByTemplateQuery(templateId);
  const [create, { isLoading: isCreating }] = useCreateValidationRuleMutation();
  const [update, { isLoading: isUpdating }] = useUpdateValidationRuleMutation();
  const [remove] = useDeleteValidationRuleMutation();

  const filtered = rules.filter((r) =>
    r.fieldKey?.toLowerCase().includes(search.toLowerCase()) ||
    r.fieldLabel?.toLowerCase().includes(search.toLowerCase())
  );

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true); };
  const openEdit = (r: ValidationRule) => {
    setEditing(r);
    form.setFieldsValue({ fieldKey: r.fieldKey, fieldLabel: r.fieldLabel, operator: r.operator, expectedValue: r.expectedValue, errorMessage: r.errorMessage });
    setOpen(true);
  };

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
    {
      title: "Поле",
      key: "field",
      render: (_, r) => (
        <div>
          <Text strong style={{ fontSize: 13 }}>{r.fieldLabel}</Text>
          <Text type="secondary" style={{ fontSize: 11, display: "block", fontFamily: "monospace" }}>{r.fieldKey}</Text>
        </div>
      ),
    },
    {
      title: "Оператор",
      dataIndex: "operator",
      key: "operator",
      render: (v: string) => <Tag color="blue">{OPERATORS.find(o => o.value === v)?.label ?? v}</Tag>,
    },
    {
      title: "Ожидаемое значение",
      dataIndex: "expectedValue",
      key: "expectedValue",
      render: (v: string) => <Text code style={{ fontSize: 12 }}>{v || "—"}</Text>,
    },
    {
      title: "Сообщение об ошибке",
      dataIndex: "errorMessage",
      key: "errorMessage",
      render: (v: string) => <Text type="secondary" style={{ fontSize: 12 }}>{v}</Text>,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 100,
      align: "center",
      render: (v: boolean) => <Tag color={v ? "success" : "default"}>{v ? "Активно" : "Выкл"}</Tag>,
    },
    {
      title: "",
      key: "actions",
      width: 90,
      align: "center",
      render: (_, r) => (
        <Space size={4}>
          <Button size="small" icon={<Pencil size={13} />} onClick={() => openEdit(r)} />
          <Popconfirm title="Удалить правило?" onConfirm={() => remove(r.id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"))} okText="Да" cancelText="Нет">
            <Button size="small" danger icon={<Trash2 size={13} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <Input prefix={<Search size={14} color="#9ca3af" />} placeholder="Поиск по ключу или названию поля..."
          value={search} onChange={(e) => setSearch(e.target.value)} style={{ maxWidth: 340, borderRadius: 8 }} />
        <Button type="primary" icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#1677ff", borderRadius: 8 }}>Добавить правило</Button>
      </div>
      <Table columns={columns} dataSource={filtered} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил валидации для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12 }} />

      <Modal title={editing ? "Редактировать правило валидации" : "Новое правило валидации"}
        open={open} onCancel={() => setOpen(false)} onOk={handleSubmit}
        okText={editing ? "Сохранить" : "Создать"} cancelText="Отмена"
        confirmLoading={isCreating || isUpdating} destroyOnClose>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Ключ поля" name="fieldKey" rules={[{ required: true, message: "Введите ключ поля" }]}>
            <Input placeholder="Например: contract_amount" />
          </Form.Item>
          <Form.Item label="Название поля" name="fieldLabel" rules={[{ required: true, message: "Введите название" }]}>
            <Input placeholder="Например: Сумма договора" />
          </Form.Item>
          <Form.Item label="Оператор" name="operator" rules={[{ required: true, message: "Выберите оператор" }]}>
            <Select options={OPERATORS} placeholder="Выберите оператор" />
          </Form.Item>
          <Form.Item label="Ожидаемое значение" name="expectedValue">
            <Input placeholder="Оставьте пустым для IS_EMPTY / IS_NOT_EMPTY" />
          </Form.Item>
          <Form.Item label="Сообщение об ошибке" name="errorMessage" rules={[{ required: true, message: "Введите сообщение" }]}>
            <Input placeholder="Например: Поле обязательно для заполнения" />
          </Form.Item>
        </Form>
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

  const levelColor: Record<string, string> = { HIGH: "error", MEDIUM: "warning", LOW: "success" };
  const levelLabel: Record<string, string> = { HIGH: "Высокий", MEDIUM: "Средний", LOW: "Низкий" };

  const columns: ColumnsType<RiskRule> = [
    {
      title: "Код",
      dataIndex: "ruleCode",
      key: "ruleCode",
      render: (v: string) => <Text code style={{ fontSize: 11, color: "#c0392b" }}>{v}</Text>,
    },
    {
      title: "Поле / Условие",
      key: "condition",
      render: (_, r) => (
        <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#555" }}>
          {r.fieldKey} {OPERATORS.find(o => o.value === r.operator)?.label} {r.expectedValue || ""}
        </Text>
      ),
    },
    {
      title: "Сообщение",
      dataIndex: "riskMessage",
      key: "riskMessage",
      render: (v: string) => <Text style={{ fontSize: 12 }}>{v}</Text>,
    },
    {
      title: "Уровень",
      dataIndex: "riskLevel",
      key: "riskLevel",
      width: 110,
      align: "center",
      render: (v: string) => <Tag color={levelColor[v]}>{levelLabel[v] ?? v}</Tag>,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 100,
      align: "center",
      render: (v: boolean) => <Tag color={v ? "success" : "default"}>{v ? "Активно" : "Выкл"}</Tag>,
    },
    {
      title: "",
      key: "actions",
      width: 90,
      align: "center",
      render: (_, r) => (
        <Space size={4}>
          <Button size="small" icon={<Pencil size={13} />} onClick={() => openEdit(r)} />
          <Popconfirm title="Удалить правило риска?" onConfirm={() => remove(r.id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"))} okText="Да" cancelText="Нет">
            <Button size="small" danger icon={<Trash2 size={13} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#ef4444", borderColor: "#ef4444", borderRadius: 8 }}>Добавить риск</Button>
      </div>
      <Table columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил риска для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12 }} />

      <Modal title={editing ? "Редактировать правило риска" : "Новое правило риска"}
        open={open} onCancel={() => setOpen(false)} onOk={handleSubmit}
        okText={editing ? "Сохранить" : "Создать"} cancelText="Отмена"
        confirmLoading={isCreating || isUpdating} destroyOnClose>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Код правила" name="ruleCode"
            rules={[{ required: true, message: "Введите код" }, { pattern: /^[A-Z0-9_]+$/, message: "Только A-Z, 0-9, _" }]}>
            <Input placeholder="Например: HIGH_LIABILITY_RISK" style={{ textTransform: "uppercase" }} />
          </Form.Item>
          <Form.Item label="Ключ поля" name="fieldKey" rules={[{ required: true, message: "Введите ключ поля" }]}>
            <Input placeholder="Например: contract_amount" />
          </Form.Item>
          <Form.Item label="Оператор" name="operator" rules={[{ required: true, message: "Выберите оператор" }]}>
            <Select options={OPERATORS} placeholder="Выберите оператор" />
          </Form.Item>
          <Form.Item label="Ожидаемое значение" name="expectedValue">
            <Input placeholder="Например: 1000000" />
          </Form.Item>
          <Form.Item label="Уровень риска" name="riskLevel" rules={[{ required: true, message: "Выберите уровень" }]}>
            <Select options={RISK_LEVELS.map(r => ({ value: r.value, label: r.label }))} placeholder="Выберите уровень" />
          </Form.Item>
          <Form.Item label="Сообщение о риске" name="riskMessage" rules={[{ required: true, message: "Введите сообщение" }]}>
            <Input.TextArea rows={2} placeholder="Описание риска для пользователя" />
          </Form.Item>
        </Form>
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
    {
      title: "Условие (IF)",
      key: "condition",
      render: (_, r) => (
        <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#1677ff" }}>
          {r.conditionFieldKey} {OPERATORS.find(o => o.value === r.operator)?.label} "{r.conditionValue}"
        </Text>
      ),
    },
    {
      title: "Целевое поле (THEN показать)",
      dataIndex: "targetFieldKey",
      key: "targetFieldKey",
      render: (v: string) => <Text code style={{ fontSize: 12 }}>{v}</Text>,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 100,
      align: "center",
      render: (v: boolean) => <Tag color={v ? "success" : "default"}>{v ? "Активно" : "Выкл"}</Tag>,
    },
    {
      title: "",
      key: "actions",
      width: 90,
      align: "center",
      render: (_, r) => (
        <Space size={4}>
          <Button size="small" icon={<Pencil size={13} />} onClick={() => openEdit(r)} />
          <Popconfirm title="Удалить условное правило?" onConfirm={() => remove(r.id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"))} okText="Да" cancelText="Нет">
            <Button size="small" danger icon={<Trash2 size={13} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#7c3aed", borderColor: "#7c3aed", borderRadius: 8 }}>Добавить условие</Button>
      </div>
      <Table columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет условных правил для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12 }} />

      <Modal title={editing ? "Редактировать IF/THEN правило" : "Новое IF/THEN правило"}
        open={open} onCancel={() => setOpen(false)} onOk={handleSubmit}
        okText={editing ? "Сохранить" : "Создать"} cancelText="Отмена"
        confirmLoading={isCreating || isUpdating} destroyOnClose width={540}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <div style={{ background: "#eff6ff", borderRadius: 8, padding: "10px 14px", marginBottom: 12 }}>
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
          <div style={{ background: "#f5f3ff", borderRadius: 8, padding: "10px 14px", marginBottom: 12 }}>
            <Text strong style={{ color: "#7c3aed", fontSize: 12 }}>THEN — Показать поле</Text>
          </div>
          <Form.Item label="Ключ целевого поля" name="targetFieldKey" rules={[{ required: true, message: "Обязательно" }]}>
            <Input placeholder="Ключ поля которое нужно показать" />
          </Form.Item>
        </Form>
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
    {
      title: "Документ",
      dataIndex: "requiredDocTitle",
      key: "requiredDocTitle",
      render: (v: string) => <Text strong style={{ fontSize: 13 }}>{v}</Text>,
    },
    {
      title: "Причина",
      dataIndex: "reason",
      key: "reason",
      render: (v: string) => <Text type="secondary" style={{ fontSize: 12 }}>{v}</Text>,
    },
    {
      title: "Условие",
      key: "condition",
      render: (_, r) => r.conditionFieldKey
        ? <Text style={{ fontSize: 11, fontFamily: "monospace", color: "#7c3aed" }}>{r.conditionFieldKey} {r.conditionOperator} {r.conditionValue}</Text>
        : <Text type="secondary" style={{ fontSize: 11 }}>Всегда</Text>,
    },
    {
      title: "Обязательный",
      dataIndex: "mandatory",
      key: "mandatory",
      width: 110,
      align: "center",
      render: (v: boolean) => <Switch checked={v} disabled size="small" />,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 100,
      align: "center",
      render: (v: boolean) => <Tag color={v ? "success" : "default"}>{v ? "Активно" : "Выкл"}</Tag>,
    },
    {
      title: "",
      key: "actions",
      width: 90,
      align: "center",
      render: (_, r) => (
        <Space size={4}>
          <Button size="small" icon={<Pencil size={13} />} onClick={() => openEdit(r)} />
          <Popconfirm title="Удалить требование?" onConfirm={() => remove(r.id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"))} okText="Да" cancelText="Нет">
            <Button size="small" danger icon={<Trash2 size={13} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#059669", borderColor: "#059669", borderRadius: 8 }}>Добавить документ</Button>
      </div>
      <Table columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет требований к документам" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12 }} />

      <Modal title={editing ? "Редактировать требование" : "Новое требование к документу"}
        open={open} onCancel={() => setOpen(false)} onOk={handleSubmit}
        okText={editing ? "Сохранить" : "Создать"} cancelText="Отмена"
        confirmLoading={isCreating || isUpdating} destroyOnClose width={520}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Название документа" name="requiredDocTitle" rules={[{ required: true, message: "Введите название" }]}>
            <Input placeholder="Например: Паспорт гражданина РК" />
          </Form.Item>
          <Form.Item label="Причина требования" name="reason" rules={[{ required: true, message: "Введите причину" }]}>
            <Input.TextArea rows={2} placeholder="Почему этот документ необходим" />
          </Form.Item>
          <Form.Item label="Обязательный" name="mandatory" valuePropName="checked">
            <Switch checkedChildren="Да" unCheckedChildren="Нет" />
          </Form.Item>
          <div style={{ background: "#f5f3ff", borderRadius: 8, padding: "10px 14px", marginBottom: 12 }}>
            <Text type="secondary" style={{ fontSize: 12 }}>Условие (опционально) — если пусто, документ требуется всегда</Text>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
            <Form.Item label="Поле" name="conditionFieldKey">
              <Input placeholder="Ключ поля" />
            </Form.Item>
            <Form.Item label="Оператор" name="conditionOperator">
              <Select options={OPERATORS} placeholder="Оператор" allowClear />
            </Form.Item>
            <Form.Item label="Значение" name="conditionValue">
              <Input placeholder="Значение" />
            </Form.Item>
          </div>
        </Form>
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
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      render: (v: string) => <Tag color="geekblue">{v || "—"}</Tag>,
    },
    {
      title: "Ключевые слова",
      dataIndex: "keywords",
      key: "keywords",
      render: (v: string) => <Text style={{ fontSize: 12, fontFamily: "monospace", color: "#1677ff" }}>{v}</Text>,
    },
    {
      title: "Базовый балл",
      dataIndex: "baseScore",
      key: "baseScore",
      width: 120,
      align: "center",
      render: (v: number) => <Tag color="blue" style={{ fontWeight: 600 }}>{v}</Tag>,
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 100,
      align: "center",
      render: (v: boolean) => <Tag color={v ? "success" : "default"}>{v ? "Активно" : "Выкл"}</Tag>,
    },
    {
      title: "",
      key: "actions",
      width: 90,
      align: "center",
      render: (_, r) => (
        <Space size={4}>
          <Button size="small" icon={<Pencil size={13} />} onClick={() => openEdit(r)} />
          <Popconfirm title="Удалить правило?" onConfirm={() => remove(r.id).unwrap().then(() => message.success("Удалено")).catch(() => message.error("Ошибка"))} okText="Да" cancelText="Нет">
            <Button size="small" danger icon={<Trash2 size={13} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
        <Button type="primary" icon={<Plus size={14} />} onClick={openCreate}
          style={{ background: "#0F2A44", borderColor: "#0F2A44", borderRadius: 8 }}>Добавить правило</Button>
      </div>
      <Table columns={columns} dataSource={rules} rowKey="id" loading={isLoading}
        locale={{ emptyText: <Empty description="Нет правил матчинга для этого шаблона" /> }}
        pagination={{ pageSize: 8, showTotal: (t) => `Всего ${t}` }} style={{ borderRadius: 12 }} />

      <Modal title={editing ? "Редактировать правило матчинга" : "Новое правило матчинга"}
        open={open} onCancel={() => setOpen(false)} onOk={handleSubmit}
        okText={editing ? "Сохранить" : "Создать"} cancelText="Отмена"
        confirmLoading={isCreating || isUpdating} destroyOnClose>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="Категория" name="categoryId" rules={[{ required: true, message: "Выберите категорию" }]}>
            <Select showSearch placeholder="Выберите категорию" optionFilterProp="label"
              options={categories.map((c) => ({ value: c.id, label: c.name }))} />
          </Form.Item>
          <Form.Item label="Ключевые слова" name="keywords" rules={[{ required: true, message: "Введите ключевые слова" }]}>
            <Input placeholder="Например: купля, продажа, договор" />
          </Form.Item>
          <Form.Item label="Базовый балл (0–100)" name="baseScore" rules={[{ required: true, message: "Укажите балл" }]}>
            <InputNumber style={{ width: "100%" }} min={0} max={100} placeholder="50" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

// ─── Main Panel ───────────────────────────────
export default function LawyerRuleManagerPanel() {
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
    <div>
      {/* Header */}
      <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", borderRadius: 16, padding: "28px 36px", marginBottom: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ width: 52, height: 52, borderRadius: 14, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <ShieldAlert size={26} color="#fff" />
          </div>
          <div style={{ flex: 1 }}>
            <Title level={4} style={{ margin: 0, color: "#fff", fontWeight: 800 }}>Rule Engine</Title>
            <Text style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>
              Управление правилами валидации, рисков, условиями, матчинга и требованиями к документам
            </Text>
          </div>
          {selectedTemplateId && (
            <div style={{ display: "flex", gap: 10 }}>
              <StatBadge label="Валидация" value={validationRules.length} />
              <StatBadge label="Риски" value={riskRules.length} />
              <StatBadge label="IF/THEN" value={conditionalRules.length} />
              <StatBadge label="Документы" value={requiredDocRules.length} />
              <StatBadge label="Матчинг" value={matchingRules.length} />
            </div>
          )}
        </div>
      </div>

      {/* Template Selector */}
      <div style={{ background: "#fff", borderRadius: 12, padding: "18px 24px", marginBottom: 20, boxShadow: "0 1px 4px rgba(0,0,0,0.06)", display: "flex", alignItems: "center", gap: 16 }}>
        <Text strong style={{ color: "#0F2A44", whiteSpace: "nowrap", fontSize: 14 }}>Шаблон:</Text>
        <Select
          showSearch style={{ flex: 1, maxWidth: 500 }}
          placeholder="Выберите шаблон для управления правилами..."
          optionFilterProp="label" value={selectedTemplateId}
          onChange={(v) => setSelectedTemplateId(v)}
          options={templates.map((t) => ({ value: t.id, label: `${t.title} · ${t.category?.name ?? "—"}` }))}
          allowClear onClear={() => setSelectedTemplateId(null)}
        />
        {selectedTemplateId && (
          <Text type="secondary" style={{ fontSize: 12, whiteSpace: "nowrap" }}>
            Всего правил: <Text strong>{totalRules}</Text>
          </Text>
        )}
      </div>

      {/* Stats */}
      {selectedTemplateId && (
        <div style={{ display: "flex", gap: 14, marginBottom: 20 }}>
          <StatCard label="Валидация" value={validationRules.length} color="#1677ff" />
          <StatCard label="Риски" value={riskRules.length} color="#ef4444" />
          <StatCard label="IF/THEN" value={conditionalRules.length} color="#7c3aed" />
          <StatCard label="Доп. документы" value={requiredDocRules.length} color="#059669" />
          <StatCard label="Матчинг" value={matchingRules.length} color="#0F2A44" />
        </div>
      )}

      {/* Tabs */}
      <div style={{ background: "#fff", borderRadius: 12, padding: "0 24px 24px", boxShadow: "0 1px 4px rgba(0,0,0,0.06)", minHeight: 300 }}>
        {!selectedTemplateId ? (
          <div style={{ padding: "60px 0", textAlign: "center" }}>
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE}
              description={<span style={{ color: "#8c8fa3" }}>Выберите шаблон выше для управления правилами</span>} />
          </div>
        ) : (
          <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} style={{ paddingTop: 8 }} />
        )}
      </div>
    </div>
  );
}
