import { useState, useEffect } from "react";
import { Form, message } from "antd";
import { useGetProfileQuery, useUpdateProfileMutation } from "../api/profile-api";

export function useProfile() {
  const [form] = Form.useForm();
  const [editing, setEditing] = useState(false);

  const { data: profile, isLoading } = useGetProfileQuery();
  const [updateProfile, { isLoading: isSaving }] = useUpdateProfileMutation();

  useEffect(() => {
    if (profile) {
      form.setFieldsValue({ fio: profile.fio, phone: profile.phone, gender: profile.gender });
    }
  }, [profile, form]);

  const startEdit = () => setEditing(true);
  const cancelEdit = () => {
    if (profile) form.setFieldsValue({ fio: profile.fio, phone: profile.phone });
    setEditing(false);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      await updateProfile(values).unwrap();
      message.success("Профиль обновлён");
      setEditing(false);
    } catch {
      message.error("Не удалось сохранить профиль");
    }
  };

  return { form, profile, isLoading, isSaving, editing, startEdit, cancelEdit, handleSave };
}
