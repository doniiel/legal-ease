import { useState, useMemo } from "react";
import {
  App,
  Card,
  Table,
  Tag,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Typography,
  Popconfirm,
  Descriptions,
  Drawer,
  DatePicker,
  Row,
  Col,
  Avatar,
  Badge,
  Statistic,
  Divider,
} from "antd";
import {
  CheckCircle,
  XCircle,
  Clock,
  Trash2,
  Eye,
  RotateCcw,
  Users,
  ShieldCheck,
  ShieldX,
  Filter,
  SlidersHorizontal,
} from "lucide-react";
import type { ColumnsType } from "antd/es/table";
import type { Dayjs } from "dayjs";
import type { AdminLawyerApplication } from "../../../features/admin/api/admin-lawyer-api";
import {
  useGetApplicationsQuery,
  useApproveApplicationMutation,
  useRejectApplicationMutation,
  useDeleteApplicationMutation,
} from "../../../features/admin/api/admin-lawyer-api";

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface Filters {
  status?: "PENDING" | "APPROVED" | "REJECTED";
  licenseNumber?: string;
  userId?: number;
  reviewerFio?: string;
  createdRange?: [Dayjs, Dayjs] | null;
  reviewedRange?: [Dayjs, Dayjs] | null;
  page: number;
  size: number;
}

const EMPTY_FILTERS: Filters = { page: 0, size: 10 };

const STATUS_STYLE: Record<string, { color: string; bg: string; label: string }> = {
  PENDING:  { color: "#1d4ed8", bg: "#eff6ff", label: "На рассмотрении" },
  APPROVED: { color: "#15803d", bg: "#f0fdf4", label: "Одобрено" },
  REJECTED: { color: "#b91c1c", bg: "#fef2f2", label: "Отклонено" },
};

function StatusBadge({ status }: { status: string }) {
  const s = STATUS_STYLE[status] ?? STATUS_STYLE.PENDING;
  const icons: Record<string, React.ReactNode> = {
    APPROVED: <CheckCircle size={11} />,
    REJECTED: <XCircle size={11} />,
    PENDING:  <Clock size={11} />,
  };
  return (
    <Tag
      icon={icons[status]}
      style={{
        color: s.color,
        background: s.bg,
        border: "none",
        fontWeight: 500,
        borderRadius: 20,
        padding: "2px 10px",
        display: "inline-flex",
        alignItems: "center",
        gap: 4,
      }}
    >
      {s.label}
    </Tag>
  );
}

function getInitials(fio: string) {
  return fio.split(" ").slice(0, 2).map((w) => w[0]).join("").toUpperCase();
}

const AVATAR_COLORS = ["#0F2A44", "#1d4ed8", "#7c3aed", "#b45309", "#0369a1"];

export default function AdminLawyerApplicationsPanel() {
  const { message } = App.useApp();
  const [filters, setFilters] = useState<Filters>(EMPTY_FILTERS);
  const [filterModalOpen, setFilterModalOpen] = useState(false);
  const [rejectModal, setRejectModal] = useState<{ open: boolean; id: number | null }>({ open: false, id: null });
  const [detailDrawer, setDetailDrawer] = useState<{ open: boolean; record: AdminLawyerApplication | null }>({ open: false, record: null });
  const [rejectForm] = Form.useForm();
  const [filterForm] = Form.useForm();
  const [loadingIds, setLoadingIds] = useState<number[]>([]);

  // API queries
  const { data, isLoading, isFetching } = useGetApplicationsQuery({
    status: filters.status,
    licenseNumber: filters.licenseNumber,
    userId: filters.userId,
    page: 0,
    size: 200,
  });

  // Stats queries (size=1 to minimize payload, we only need totalElements)
  const { data: statsAll }      = useGetApplicationsQuery({ page: 0, size: 1 });
  const { data: statsPending }  = useGetApplicationsQuery({ status: "PENDING",  page: 0, size: 1 });
  const { data: statsApproved } = useGetApplicationsQuery({ status: "APPROVED", page: 0, size: 1 });
  const { data: statsRejected } = useGetApplicationsQuery({ status: "REJECTED", page: 0, size: 1 });

  const [approveApplication] = useApproveApplicationMutation();
  const [rejectApplication]  = useRejectApplicationMutation();
  const [deleteApplication]  = useDeleteApplicationMutation();

  const stats = {
    total:    statsAll?.totalElements      ?? 0,
    pending:  statsPending?.totalElements  ?? 0,
    approved: statsApproved?.totalElements ?? 0,
    rejected: statsRejected?.totalElements ?? 0,
  };

  const openFilterModal = () => {
    filterForm.setFieldsValue({
      status:        filters.status ?? null,
      licenseNumber: filters.licenseNumber ?? "",
      userId:        filters.userId ?? null,
      createdRange:  filters.createdRange ?? null,
      reviewedRange: filters.reviewedRange ?? null,
    });
    setFilterModalOpen(true);
  };
  const applyFilters = () => {
    const v = filterForm.getFieldsValue();
    setFilters((f) => ({
      ...EMPTY_FILTERS,
      reviewerFio:   f.reviewerFio,
      status:        v.status || undefined,
      licenseNumber: v.licenseNumber || undefined,
      userId:        v.userId || undefined,
      createdRange:  v.createdRange || null,
      reviewedRange: v.reviewedRange || null,
    }));
    setFilterModalOpen(false);
  };
  const resetFilters = () => filterForm.resetFields();
  const resetAndClose = () => { setFilters(EMPTY_FILTERS); filterForm.resetFields(); setFilterModalOpen(false); };

  // Client-side filtering for fields not supported by the API
  const filtered = useMemo(() => {
    const content = data?.content ?? [];
    return content.filter((a) => {
      if (filters.reviewerFio && !a.reviewerInfo?.fio.toLowerCase().includes(filters.reviewerFio.toLowerCase())) return false;
      if (filters.createdRange) {
        const t = new Date(a.submittedAt).getTime();
        if (t < filters.createdRange[0].startOf("day").valueOf()) return false;
        if (t > filters.createdRange[1].endOf("day").valueOf()) return false;
      }
      if (filters.reviewedRange && a.reviewedAt) {
        const t = new Date(a.reviewedAt).getTime();
        if (t < filters.reviewedRange[0].startOf("day").valueOf()) return false;
        if (t > filters.reviewedRange[1].endOf("day").valueOf()) return false;
      }
      return true;
    });
  }, [data, filters]);

  const paginatedData = filtered.slice(filters.page * filters.size, filters.page * filters.size + filters.size);

  const withLoading = async (id: number, fn: () => Promise<void>) => {
    setLoadingIds((ids) => [...ids, id]);
    try {
      await fn();
    } finally {
      setLoadingIds((ids) => ids.filter((i) => i !== id));
    }
  };

  const handleApprove = (id: number) => {
    withLoading(id, async () => {
      await approveApplication(id).unwrap();
      message.success("Заявка успешно одобрена");
    }).catch(() => message.error("Ошибка при одобрении заявки"));
  };

  const handleRejectSubmit = (values: { reason: string }) => {
    if (!rejectModal.id) return;
    const id = rejectModal.id;
    withLoading(id, async () => {
      await rejectApplication({ id, reason: values.reason }).unwrap();
      message.success("Заявка отклонена");
      setRejectModal({ open: false, id: null });
      rejectForm.resetFields();
    }).catch(() => message.error("Ошибка при отклонении заявки"));
  };

  const handleDelete = (id: number) => {
    withLoading(id, async () => {
      await deleteApplication(id).unwrap();
      message.success("Заявка удалена");
    }).catch(() => message.error("Ошибка при удалении заявки"));
  };

  const columns: ColumnsType<AdminLawyerApplication> = [
    {
      title: "Заявитель",
      key: "lawyer",
      render: (_, r) => (
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <Avatar
            size={36}
            style={{ background: AVATAR_COLORS[r.id % AVATAR_COLORS.length], flexShrink: 0, fontSize: 13, fontWeight: 600 }}
          >
            {getInitials(r.lawyerInfo.fio)}
          </Avatar>
          <div>
            <div style={{ fontWeight: 500, fontSize: 13, color: "#111827", lineHeight: 1.3 }}>
              {r.lawyerInfo.fio}
            </div>
            <div style={{ fontSize: 12, color: "#6b7280" }}>{r.lawyerInfo.email}</div>
          </div>
        </div>
      ),
    },
    {
      title: "Лицензия",
      dataIndex: "licenseNumber",
      render: (val) => (
        <Tag style={{ fontFamily: "monospace", fontSize: 12, borderRadius: 6, padding: "1px 8px" }}>
          {val}
        </Tag>
      ),
    },
    {
      title: "Статус",
      dataIndex: "status",
      render: (status) => <StatusBadge status={status} />,
    },
    {
      title: "Дата подачи",
      dataIndex: "submittedAt",
      render: (val) => (
        <Text style={{ fontSize: 12, color: "#6b7280" }}>
          {new Date(val).toLocaleDateString("ru-KZ", { day: "2-digit", month: "short", year: "numeric" })}
        </Text>
      ),
    },
    {
      title: "Действия",
      key: "actions",
      align: "right",
      render: (_, record) => {
        const loading = loadingIds.includes(record.id);
        return (
          <Space size={4}>
            <Button
              size="small"
              icon={<Eye size={13} />}
              style={{ borderRadius: 6 }}
              onClick={() => setDetailDrawer({ open: true, record })}
            />
            {record.status === "PENDING" && (
              <>
                <Popconfirm
                  title="Одобрить заявку?"
                  description="Пользователь получит статус адвоката."
                  onConfirm={() => handleApprove(record.id)}
                  okText="Одобрить"
                  cancelText="Отмена"
                  okButtonProps={{ style: { background: "#16a34a" } }}
                >
                  <Button
                    size="small"
                    loading={loading}
                    icon={<CheckCircle size={13} />}
                    style={{ borderRadius: 6, background: "#f0fdf4", color: "#15803d", border: "1px solid #bbf7d0", fontWeight: 500 }}
                  >
                    Одобрить
                  </Button>
                </Popconfirm>
                <Button
                  size="small"
                  loading={loading}
                  icon={<XCircle size={13} />}
                  style={{ borderRadius: 6, background: "#fef2f2", color: "#b91c1c", border: "1px solid #fecaca", fontWeight: 500 }}
                  onClick={() => setRejectModal({ open: true, id: record.id })}
                >
                  Отклонить
                </Button>
              </>
            )}
            <Popconfirm
              title="Удалить заявку?"
              description="Это действие необратимо."
              onConfirm={() => handleDelete(record.id)}
              okText="Удалить"
              cancelText="Отмена"
              okButtonProps={{ danger: true }}
            >
              <Button
                size="small"
                danger
                icon={<Trash2 size={13} />}
                style={{ borderRadius: 6 }}
                loading={loading}
              />
            </Popconfirm>
          </Space>
        );
      },
    },
  ];

  const activeFilterCount = Object.keys(filters).filter(
    (k) => !["page", "size"].includes(k) && filters[k as keyof Filters] != null
  ).length;

  return (
    <div>
      {/* Page header */}
      <div style={{ marginBottom: 24 }}>
        <Title level={4} style={{ margin: 0, color: "#0F2A44" }}>
          Заявки на статус адвоката
        </Title>
        <Text type="secondary" style={{ fontSize: 13 }}>
          Рассмотрите и управляйте заявками пользователей
        </Text>
      </div>

      {/* Stats row */}
      <Row gutter={12} style={{ marginBottom: 20 }}>
        {[
          { label: "Всего",            value: stats.total,    icon: <Users size={18} />,      color: "#0F2A44", bg: "#f0f4f8" },
          { label: "На рассмотрении",  value: stats.pending,  icon: <Clock size={18} />,      color: "#1d4ed8", bg: "#eff6ff" },
          { label: "Одобрено",         value: stats.approved, icon: <ShieldCheck size={18} />,color: "#15803d", bg: "#f0fdf4" },
          { label: "Отклонено",        value: stats.rejected, icon: <ShieldX size={18} />,    color: "#b91c1c", bg: "#fef2f2" },
        ].map((s) => (
          <Col xs={12} sm={6} key={s.label}>
            <Card
              style={{ borderRadius: 12, border: "1px solid #e5e7eb", cursor: "default" }}
              styles={{ body: { padding: "16px 20px" } }}
            >
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 8 }}>
                <Text style={{ fontSize: 12, color: "#6b7280", fontWeight: 500 }}>{s.label}</Text>
                <div style={{ width: 32, height: 32, borderRadius: 8, background: s.bg, display: "flex", alignItems: "center", justifyContent: "center", color: s.color }}>
                  {s.icon}
                </div>
              </div>
              <Statistic
                value={s.value}
                valueStyle={{ fontSize: 26, fontWeight: 700, color: s.color, lineHeight: 1 }}
              />
            </Card>
          </Col>
        ))}
      </Row>

      {/* Toolbar */}
      <Card
        style={{ borderRadius: 12, border: "1px solid #e5e7eb", marginBottom: 12 }}
        styles={{ body: { padding: "12px 16px" } }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <Input.Search
            placeholder="Поиск по ФИО проверяющего..."
            allowClear
            size="large"
            style={{ flex: 1, maxWidth: 380 }}
            value={filters.reviewerFio ?? ""}
            onChange={(e) =>
              setFilters((f) => ({ ...f, reviewerFio: e.target.value || undefined, page: 0 }))
            }
            onSearch={(val) =>
              setFilters((f) => ({ ...f, reviewerFio: val || undefined, page: 0 }))
            }
          />

          <div style={{ flex: 1 }} />

          <Text style={{ fontSize: 12, color: "#9ca3af", whiteSpace: "nowrap" }}>
            Найдено: <strong style={{ color: "#374151" }}>{filtered.length}</strong>
          </Text>

          {(activeFilterCount > 0 || filters.reviewerFio) && (
            <Button
              type="text"
              size="small"
              icon={<RotateCcw size={13} />}
              onClick={() => { resetAndClose(); setFilters(EMPTY_FILTERS); }}
              style={{ color: "#9ca3af", whiteSpace: "nowrap" }}
            >
              Сбросить
            </Button>
          )}

          <Badge count={activeFilterCount} size="small" color="#1d4ed8" offset={[-4, 4]}>
            <Button
              icon={<SlidersHorizontal size={15} />}
              onClick={openFilterModal}
              type={activeFilterCount > 0 ? "primary" : "default"}
              ghost={activeFilterCount > 0}
              style={{ borderRadius: 8, fontWeight: 500 }}
            >
              Фильтры
            </Button>
          </Badge>
        </div>
      </Card>

      {/* Table */}
      <Card style={{ borderRadius: 12, border: "1px solid #e5e7eb" }} styles={{ body: { padding: 0 } }}>
        <Table
          columns={columns}
          dataSource={paginatedData}
          rowKey="id"
          loading={isLoading || isFetching}
          style={{ borderRadius: 12, overflow: "hidden" }}
          rowClassName={() => "table-row-hover"}
          pagination={{
            current: filters.page + 1,
            pageSize: filters.size,
            total: filtered.length,
            onChange: (p, ps) => setFilters((f) => ({ ...f, page: p - 1, size: ps })),
            showTotal: (total, range) => (
              <Text style={{ fontSize: 12, color: "#6b7280" }}>
                {range[0]}–{range[1]} из {total} заявок
              </Text>
            ),
            style: { padding: "12px 20px" },
          }}
          scroll={{ x: 700 }}
        />
      </Card>

      {/* Filter Modal */}
      <Modal
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#eff6ff", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <Filter size={16} color="#1d4ed8" />
            </div>
            <span>Фильтры</span>
          </div>
        }
        open={filterModalOpen}
        onCancel={() => setFilterModalOpen(false)}
        width={560}
        footer={
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <Button
              icon={<RotateCcw size={13} />}
              onClick={resetFilters}
              style={{ color: "#6b7280" }}
            >
              Сбросить всё
            </Button>
            <Space>
              <Button style={{ borderRadius: 8 }} onClick={() => setFilterModalOpen(false)}>
                Отмена
              </Button>
              <Button
                type="primary"
                style={{ borderRadius: 8, background: "#0F2A44" }}
                onClick={applyFilters}
              >
                Применить
              </Button>
            </Space>
          </div>
        }
        destroyOnClose={false}
      >
        <Form form={filterForm} layout="vertical" style={{ padding: "4px 0" }}>
          <Row gutter={16}>
            <Col xs={24} sm={12}>
              <Form.Item name="status" label="Статус">
                <Select placeholder="Все статусы" allowClear size="large" style={{ width: "100%" }}>
                  <Option value="PENDING">
                    <Space size={6}><Clock size={12} color="#1d4ed8" />На рассмотрении</Space>
                  </Option>
                  <Option value="APPROVED">
                    <Space size={6}><CheckCircle size={12} color="#15803d" />Одобрено</Space>
                  </Option>
                  <Option value="REJECTED">
                    <Space size={6}><XCircle size={12} color="#b91c1c" />Отклонено</Space>
                  </Option>
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} sm={12}>
              <Form.Item name="licenseNumber" label="Номер лицензии">
                <Input
                  placeholder="KZ-ADV-2024-00123"
                  allowClear
                  size="large"
                  style={{ fontFamily: "monospace" }}
                />
              </Form.Item>
            </Col>

            <Col xs={24}>
              <Form.Item name="createdRange" label="Период подачи">
                <RangePicker style={{ width: "100%" }} size="large" placeholder={["От", "До"]} />
              </Form.Item>
            </Col>

            <Col xs={24}>
              <Form.Item name="reviewedRange" label="Период рассмотрения" style={{ marginBottom: 0 }}>
                <RangePicker style={{ width: "100%" }} size="large" placeholder={["От", "До"]} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* Reject Modal */}
      <Modal
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, borderRadius: 8, background: "#fef2f2", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <XCircle size={16} color="#b91c1c" />
            </div>
            <span>Отклонить заявку</span>
          </div>
        }
        open={rejectModal.open}
        onCancel={() => { setRejectModal({ open: false, id: null }); rejectForm.resetFields(); }}
        footer={null}
        destroyOnClose
        width={480}
      >
        <Text type="secondary" style={{ fontSize: 13, display: "block", marginBottom: 20 }}>
          Укажите причину отклонения — она будет показана заявителю.
        </Text>
        <Form form={rejectForm} layout="vertical" onFinish={handleRejectSubmit}>
          <Form.Item
            name="reason"
            label="Причина отклонения"
            rules={[{ required: true, message: "Укажите причину" }]}
          >
            <Input.TextArea
              rows={4}
              placeholder="Например: Лицензия не найдена в реестре адвокатов РК..."
              style={{ borderRadius: 8 }}
            />
          </Form.Item>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <Button style={{ borderRadius: 8 }} onClick={() => { setRejectModal({ open: false, id: null }); rejectForm.resetFields(); }}>
              Отмена
            </Button>
            <Button type="primary" danger htmlType="submit" style={{ borderRadius: 8 }}>
              Отклонить заявку
            </Button>
          </div>
        </Form>
      </Modal>

      {/* Detail Drawer */}
      <Drawer
        title={
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <Eye size={16} color="#6b7280" />
            <span>Детали заявки #{detailDrawer.record?.id}</span>
          </div>
        }
        open={detailDrawer.open}
        onClose={() => setDetailDrawer({ open: false, record: null })}
        width={440}
        styles={{ body: { padding: "24px" } }}
      >
        {detailDrawer.record && (() => {
          const r = detailDrawer.record;
          const s = STATUS_STYLE[r.status];
          return (
            <>
              {/* Status banner */}
              <div
                style={{
                  background: s.bg,
                  border: `1px solid`,
                  borderColor: r.status === "APPROVED" ? "#bbf7d0" : r.status === "REJECTED" ? "#fecaca" : "#bfdbfe",
                  borderRadius: 10,
                  padding: "14px 18px",
                  marginBottom: 24,
                  display: "flex",
                  alignItems: "center",
                  gap: 10,
                }}
              >
                {r.status === "APPROVED" && <CheckCircle size={18} color={s.color} />}
                {r.status === "REJECTED" && <XCircle size={18} color={s.color} />}
                {r.status === "PENDING"  && <Clock size={18} color={s.color} />}
                <Text style={{ color: s.color, fontWeight: 600, fontSize: 13 }}>{s.label}</Text>
              </div>

              {/* Applicant */}
              <Text style={{ fontSize: 11, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                Заявитель
              </Text>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 8, marginBottom: 20 }}>
                <Avatar size={44} style={{ background: AVATAR_COLORS[r.id % AVATAR_COLORS.length], fontWeight: 600 }}>
                  {getInitials(r.lawyerInfo.fio)}
                </Avatar>
                <div>
                  <div style={{ fontWeight: 600, color: "#111827" }}>{r.lawyerInfo.fio}</div>
                  <div style={{ fontSize: 12, color: "#6b7280" }}>{r.lawyerInfo.email}</div>
                  <div style={{ fontSize: 12, color: "#9ca3af" }}>ИИН: {r.lawyerInfo.iin}</div>
                </div>
              </div>

              <Divider style={{ margin: "0 0 20px" }} />

              <Descriptions column={1} size="small" colon={false}>
                <Descriptions.Item
                  label={<Text style={{ fontSize: 12, color: "#9ca3af" }}>Номер лицензии</Text>}
                >
                  <Tag style={{ fontFamily: "monospace", borderRadius: 6 }}>{r.licenseNumber}</Tag>
                </Descriptions.Item>
                <Descriptions.Item
                  label={<Text style={{ fontSize: 12, color: "#9ca3af" }}>Дата подачи</Text>}
                >
                  <Text style={{ fontSize: 13 }}>{new Date(r.submittedAt).toLocaleString("ru-KZ")}</Text>
                </Descriptions.Item>
                {r.reviewerInfo && (
                  <Descriptions.Item
                    label={<Text style={{ fontSize: 12, color: "#9ca3af" }}>Проверил</Text>}
                  >
                    <Text style={{ fontSize: 13 }}>{r.reviewerInfo.fio}</Text>
                  </Descriptions.Item>
                )}
                {r.reviewedAt && (
                  <Descriptions.Item
                    label={<Text style={{ fontSize: 12, color: "#9ca3af" }}>Дата рассмотрения</Text>}
                  >
                    <Text style={{ fontSize: 13 }}>{new Date(r.reviewedAt).toLocaleString("ru-KZ")}</Text>
                  </Descriptions.Item>
                )}
              </Descriptions>

              {r.rejectionReason && (
                <>
                  <Divider style={{ margin: "16px 0" }} />
                  <Text style={{ fontSize: 11, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.05em", display: "block", marginBottom: 8 }}>
                    Причина отклонения
                  </Text>
                  <div style={{ background: "#fef2f2", border: "1px solid #fecaca", borderRadius: 8, padding: "12px 16px" }}>
                    <Text style={{ fontSize: 13, color: "#b91c1c" }}>{r.rejectionReason}</Text>
                  </div>
                </>
              )}

              {r.status === "PENDING" && (
                <>
                  <Divider style={{ margin: "20px 0" }} />
                  <Space style={{ width: "100%" }} direction="vertical" size={8}>
                    <Popconfirm
                      title="Одобрить заявку?"
                      onConfirm={() => { handleApprove(r.id); setDetailDrawer({ open: false, record: null }); }}
                      okText="Да"
                      cancelText="Нет"
                    >
                      <Button block style={{ borderRadius: 8, background: "#f0fdf4", color: "#15803d", border: "1px solid #bbf7d0", fontWeight: 500, height: 40 }} icon={<CheckCircle size={14} />}>
                        Одобрить заявку
                      </Button>
                    </Popconfirm>
                    <Button
                      block
                      style={{ borderRadius: 8, background: "#fef2f2", color: "#b91c1c", border: "1px solid #fecaca", fontWeight: 500, height: 40 }}
                      icon={<XCircle size={14} />}
                      onClick={() => { setDetailDrawer({ open: false, record: null }); setRejectModal({ open: true, id: r.id }); }}
                    >
                      Отклонить заявку
                    </Button>
                  </Space>
                </>
              )}
            </>
          );
        })()}
      </Drawer>
    </div>
  );
}
