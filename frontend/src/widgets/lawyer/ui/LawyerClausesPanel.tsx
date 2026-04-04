import { useState } from "react";
import {
  App, Table, Input, Select, Tooltip, Form,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  BookOpen, Plus, Search, RotateCcw, Pencil, Trash2, Tag, FolderOpen,
} from "lucide-react";
import {
  useGetClausesQuery,
  useCreateClauseMutation,
  useUpdateClauseMutation,
  useDeleteClauseMutation,
  type LegalClause,
} from "../../../features/lawyer/api/lawyer-clause-api";
import { useGetActiveCategoriesQuery } from "../../../features/categories/api/public-category-api";
import StatCard from "../../../shared/ui/StatCard";
import editorialTableComponents from "../../../shared/ui/table-components";


function ActivePill({ active }: { active: boolean }) {
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 5, padding: "3px 10px", borderRadius: 20, background: active ? "rgba(5,150,105,0.08)" : "rgba(107,114,128,0.08)", border: `1px solid ${active ? "rgba(5,150,105,0.2)" : "rgba(107,114,128,0.2)"}` }}>
      <span style={{ width: 6, height: 6, borderRadius: "50%", background: active ? "#059669" : "#9ca3af" }} />
      <span style={{ fontSize: 11, fontWeight: 700, color: active ? "#059669" : "#6b7280" }}>{active ? "Активна" : "Неактивна"}</span>
    </div>
  );
}

function IconBtn({ icon, tooltip, onClick, hoverColor }: { icon: React.ReactNode; tooltip: string; onClick?: () => void; hoverColor: string }) {
  return (
    <Tooltip title={tooltip}>
      <button onClick={onClick}
        style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "transparent", cursor: "pointer", color: "#64748b", display: "flex", alignItems: "center", justifyContent: "center", transition: "background 0.15s, color 0.15s" }}
        onMouseEnter={e => { e.currentTarget.style.background = hoverColor + "15"; e.currentTarget.style.color = hoverColor; }}
        onMouseLeave={e => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#64748b"; }}
      >{icon}</button>
    </Tooltip>
  );
}

export default function LawyerClausesPanel() {
  const { message } = App.useApp();
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<number | undefined>(undefined);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<LegalClause | null>(null);
  const [deleteModal, setDeleteModal] = useState<{ open: boolean; clause: LegalClause | null }>({ open: false, clause: null });
  const [form] = Form.useForm();

  const { data, isLoading } = useGetClausesQuery({ page: page - 1, size: 10, keyword: keyword || undefined, categoryId: categoryFilter });
  const { data: allData } = useGetClausesQuery({ page: 0, size: 1000 });
  const { data: categories = [] } = useGetActiveCategoriesQuery();

  const [createClause, { isLoading: isCreating }] = useCreateClauseMutation();
  const [updateClause, { isLoading: isUpdating }] = useUpdateClauseMutation();
  const [deleteClause, { isLoading: isDeleting }] = useDeleteClauseMutation();

  const allClauses = allData?.content ?? [];
  const totalCount = allData?.totalElements ?? 0;
  const activeCount = allClauses.filter(c => c.active).length;
  const categoryCount = new Set(allClauses.map(c => c.categoryId).filter(Boolean)).size;

  const openCreate = () => { setEditing(null); form.resetFields(); setModalOpen(true); };
  const openEdit = (clause: LegalClause) => {
    setEditing(clause);
    form.setFieldsValue({ title: clause.title, content: clause.content, categoryId: clause.categoryId, tags: clause.tags ?? "" });
    setModalOpen(true);
  };

  const handleSubmit = async (values: { title: string; content: string; categoryId?: number; tags?: string }) => {
    try {
      if (editing) {
        await updateClause({ id: editing.id, body: values }).unwrap();
        message.success("Клауза обновлена");
      } else {
        await createClause(values).unwrap();
        message.success("Клауза создана");
      }
      setModalOpen(false);
      form.resetFields();
    } catch { message.error("Ошибка при сохранении клаузы"); }
  };

  const handleDelete = async () => {
    if (!deleteModal.clause) return;
    try {
      await deleteClause(deleteModal.clause.id).unwrap();
      message.success("Клауза удалена");
      setDeleteModal({ open: false, clause: null });
    } catch { message.error("Ошибка при удалении"); }
  };

  const columns: ColumnsType<LegalClause> = [
    {
      title: "Название",
      dataIndex: "title",
      key: "title",
      render: (v: string, row) => (
        <div>
          <div style={{ fontWeight: 600, color: "#111827", fontSize: 13 }}>{v}</div>
          {row.tags && (
            <div style={{ display: "flex", gap: 4, flexWrap: "wrap", marginTop: 4 }}>
              {row.tags.split(",").map(t => t.trim()).filter(Boolean).map(t => (
                <span key={t} style={{ fontSize: 10, padding: "1px 7px", borderRadius: 20, background: "rgba(15,42,68,0.07)", color: "#0F2A44", fontWeight: 600 }}>
                  {t}
                </span>
              ))}
            </div>
          )}
        </div>
      ),
    },
    {
      title: "Категория",
      dataIndex: "categoryName",
      key: "categoryName",
      render: (v: string | null) => v ? (
        <div style={{ display: "inline-flex", alignItems: "center", gap: 5 }}>
          <FolderOpen size={12} color="#64748b" />
          <span style={{ fontSize: 12, color: "#374151" }}>{v}</span>
        </div>
      ) : <span style={{ color: "#9ca3af", fontSize: 12 }}>—</span>,
    },
    {
      title: "Содержание",
      dataIndex: "content",
      key: "content",
      ellipsis: true,
      render: (v: string) => (
        <Tooltip title={v}>
          <span style={{ fontSize: 12, color: "#6b7280" }}>{v.length > 80 ? v.slice(0, 80) + "..." : v}</span>
        </Tooltip>
      ),
    },
    {
      title: "Статус",
      dataIndex: "active",
      key: "active",
      width: 110,
      render: (v: boolean) => <ActivePill active={v} />,
    },
    {
      title: "Дата",
      dataIndex: "createdDate",
      key: "createdDate",
      width: 110,
      render: (v: string) => <span style={{ fontSize: 12, color: "#9ca3af" }}>{new Date(v).toLocaleDateString("ru-KZ")}</span>,
    },
    {
      title: "",
      key: "actions",
      width: 80,
      render: (_: unknown, row: LegalClause) => (
        <div style={{ display: "flex", gap: 4 }}>
          <IconBtn icon={<Pencil size={14} />} tooltip="Редактировать" onClick={() => openEdit(row)} hoverColor="#0F2A44" />
          <IconBtn icon={<Trash2 size={14} />} tooltip="Удалить" onClick={() => setDeleteModal({ open: true, clause: row })} hoverColor="#ef4444" />
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: "0 32px 32px" }}>
      {/* ── Full-bleed hero ── */}
      <div style={{
        background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)",
        padding: "40px 40px 56px",
        marginLeft: -32, marginRight: -32,
        display: "flex", alignItems: "center", justifyContent: "space-between", gap: 20,
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ width: 52, height: 52, borderRadius: 14, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <BookOpen size={28} color="#fff" />
          </div>
          <div>
            <div style={{ color: "#fff", fontSize: 22, fontWeight: 800, marginBottom: 4 }}>Библиотека клауз</div>
            <div style={{ color: "rgba(255,255,255,0.65)", fontSize: 13 }}>Управление переиспользуемыми юридическими формулировками</div>
          </div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ background: "rgba(255,255,255,0.12)", border: "1px solid rgba(255,255,255,0.2)", borderRadius: 20, padding: "6px 16px", color: "#fff", fontSize: 12, fontWeight: 600 }}>
            {totalCount} клауз
          </div>
          <button onClick={openCreate}
            style={{ background: "#fff", color: "#0F2A44", border: "none", borderRadius: 10, padding: "10px 20px", fontWeight: 700, fontSize: 13, cursor: "pointer", display: "flex", alignItems: "center", gap: 8 }}>
            <Plus size={15} /> Создать клаузу
          </button>
        </div>
      </div>

      {/* ── Stat cards ── */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 16, marginTop: -28, position: "relative", zIndex: 2, marginBottom: 24 }}>
        <StatCard label="Всего клауз" value={totalCount} color="#0F2A44" icon={<BookOpen size={18} />} loading={isLoading} />
        <StatCard label="Активных" value={activeCount} color="#059669" icon={<Tag size={18} />} loading={isLoading} />
        <StatCard label="Категорий" value={categoryCount} color="#1677ff" icon={<FolderOpen size={18} />} loading={isLoading} />
      </div>

      {/* ── Filter toolbar ── */}
      <div style={{ background: "rgba(255,255,255,0.8)", backdropFilter: "blur(20px)", border: "1px solid #e5e7eb", borderRadius: 12, padding: "16px 20px", marginBottom: 20, display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
        <Input prefix={<Search size={14} color="#9ca3af" />} placeholder="Поиск по ключевому слову..." value={keyword}
          onChange={e => { setKeyword(e.target.value); setPage(1); }}
          style={{ width: 260, borderRadius: 8, height: 36 }} />
        <Select placeholder="Категория" allowClear value={categoryFilter}
          onChange={v => { setCategoryFilter(v); setPage(1); }}
          style={{ width: 200 }}
          options={categories.map(c => ({ value: c.id, label: c.name }))} />
        <button onClick={() => { setKeyword(""); setCategoryFilter(undefined); setPage(1); }}
          style={{ display: "flex", alignItems: "center", gap: 6, background: "transparent", border: "1px solid #e5e7eb", borderRadius: 8, padding: "7px 14px", cursor: "pointer", color: "#6b7280", fontSize: 13 }}>
          <RotateCcw size={13} /> Сбросить
        </button>
      </div>

      {/* ── Table ── */}
      <div style={{ background: "#fff", borderRadius: 14, border: "1px solid #e5e7eb", overflow: "hidden" }}>
        <Table<LegalClause>
          columns={columns}
          dataSource={data?.content ?? []}
          rowKey="id"
          loading={isLoading}
          components={editorialTableComponents}
          pagination={{
            current: page, pageSize: 10,
            total: data?.totalElements ?? 0,
            onChange: setPage,
            showSizeChanger: false,
            showTotal: (total) => `Всего: ${total}`,
            style: { padding: "16px 20px" },
          }}
        />
      </div>

      {/* ── Create/Edit Modal ── */}
      {modalOpen && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.45)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ background: "#fff", borderRadius: 16, width: 560, maxHeight: "90vh", overflow: "auto", boxShadow: "0 24px 64px rgba(0,0,0,0.18)" }}>
            <div style={{ background: "linear-gradient(135deg, #0F2A44 0%, #1a4070 100%)", padding: "20px 28px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <BookOpen size={18} color="#fff" />
                <span style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>{editing ? "Редактировать клаузу" : "Создать клаузу"}</span>
              </div>
              <p style={{ color: "rgba(255,255,255,0.65)", fontSize: 12, margin: "6px 0 0" }}>
                {editing ? "Измените данные клаузы" : "Добавьте новую юридическую формулировку в библиотеку"}
              </p>
            </div>
            <div style={{ padding: "24px 28px" }}>
              <Form form={form} layout="vertical" onFinish={handleSubmit}>
                <Form.Item name="title" label="Название" rules={[{ required: true, message: "Введите название" }]}>
                  <Input style={{ borderRadius: 8 }} placeholder="Например: Клауза о конфиденциальности" />
                </Form.Item>
                <Form.Item name="content" label="Содержание клаузы" rules={[{ required: true, message: "Введите текст клаузы" }]}>
                  <Input.TextArea rows={5} style={{ borderRadius: 8 }} placeholder="Полный текст юридической формулировки..." />
                </Form.Item>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
                  <Form.Item name="categoryId" label="Категория">
                    <Select placeholder="Выберите категорию" allowClear style={{ borderRadius: 8 }}
                      options={categories.map(c => ({ value: c.id, label: c.name }))} />
                  </Form.Item>
                  <Form.Item name="tags" label="Теги (через запятую)">
                    <Input style={{ borderRadius: 8 }} placeholder="договор, NDA, конфиденциальность" />
                  </Form.Item>
                </div>
                <div style={{ display: "flex", gap: 10, justifyContent: "flex-end", marginTop: 8 }}>
                  <button type="button" onClick={() => { setModalOpen(false); form.resetFields(); }}
                    style={{ background: "transparent", border: "1px solid #e5e7eb", borderRadius: 8, padding: "9px 20px", cursor: "pointer", color: "#6b7280", fontSize: 13 }}>
                    Отмена
                  </button>
                  <button type="submit" disabled={isCreating || isUpdating}
                    style={{ background: "#0F2A44", color: "#fff", border: "none", borderRadius: 8, padding: "9px 20px", cursor: "pointer", fontWeight: 700, fontSize: 13, opacity: (isCreating || isUpdating) ? 0.7 : 1 }}>
                    {(isCreating || isUpdating) ? "Сохранение..." : (editing ? "Сохранить" : "Создать")}
                  </button>
                </div>
              </Form>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete Modal ── */}
      {deleteModal.open && deleteModal.clause && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.45)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ background: "#fff", borderRadius: 16, width: 440, boxShadow: "0 24px 64px rgba(0,0,0,0.18)", overflow: "hidden" }}>
            <div style={{ background: "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)", padding: "20px 28px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <Trash2 size={18} color="#fff" />
                <span style={{ color: "#fff", fontWeight: 700, fontSize: 15 }}>Удалить клаузу</span>
              </div>
            </div>
            <div style={{ padding: "24px 28px" }}>
              <p style={{ color: "#374151", fontSize: 14, marginBottom: 20 }}>
                Вы уверены, что хотите удалить клаузу <strong>«{deleteModal.clause.title}»</strong>? Это действие необратимо.
              </p>
              <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
                <button onClick={() => setDeleteModal({ open: false, clause: null })}
                  style={{ background: "transparent", border: "1px solid #e5e7eb", borderRadius: 8, padding: "9px 20px", cursor: "pointer", color: "#6b7280", fontSize: 13 }}>
                  Отмена
                </button>
                <button onClick={handleDelete} disabled={isDeleting}
                  style={{ background: "#ef4444", color: "#fff", border: "none", borderRadius: 8, padding: "9px 20px", cursor: "pointer", fontWeight: 700, fontSize: 13, opacity: isDeleting ? 0.7 : 1 }}>
                  {isDeleting ? "Удаление..." : "Удалить"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
