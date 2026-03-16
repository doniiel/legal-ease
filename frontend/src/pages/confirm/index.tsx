import { useLocation, useNavigate, Link } from "react-router-dom";
import { Button, Input, Form, Card, notification } from "antd";
import { Mail, KeyRound } from "lucide-react";
import { useConfirmAccountMutation } from "../../features/auth";
import { ROUTES } from "../../app/router/router";

type FormValues = {
  email: string;
  code: string;
};

export default function Confirm() {
  const navigate = useNavigate();
  const location = useLocation();
  const prefillEmail = (location.state as { email?: string } | null)?.email ?? "";

  const [confirmAccount, { isLoading }] = useConfirmAccountMutation();

  const handleSubmit = async (values: FormValues) => {
    try {
      await confirmAccount({ email: values.email, code: values.code }).unwrap();
      notification.success({ message: "Аккаунт подтверждён! Войдите в систему." });
      navigate(ROUTES.LOGIN);
    } catch {
      notification.error({ message: "Неверный или истёкший код подтверждения" });
    }
  };

  return (
    <div className="min-h-screen bg-primary flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl text-primary-foreground mb-2">LegalEase</h1>
          <p className="text-white/60">Подтверждение аккаунта</p>
        </div>

        <Card title="Подтверждение регистрации">
          <p className="text-sm text-gray-500 mb-6">
            Код подтверждения был отправлен на ваш email. Введите его ниже.
          </p>

          <Form
            onFinish={handleSubmit}
            layout="vertical"
            initialValues={{ email: prefillEmail }}
          >
            <Form.Item
              label="Email"
              name="email"
              rules={[
                { required: true, message: "Введите email" },
                { type: "email", message: "Некорректный email" },
              ]}
            >
              <Input
                type="email"
                placeholder="example@mail.com"
                size="large"
                prefix={<Mail size={16} className="text-gray-400" />}
              />
            </Form.Item>

            <Form.Item
              label="Код подтверждения"
              name="code"
              rules={[{ required: true, message: "Введите код из письма" }]}
            >
              <Input
                placeholder="000000"
                size="large"
                maxLength={6}
                className="tracking-widest text-center"
                prefix={<KeyRound size={16} className="text-gray-400" />}
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                className="w-full"
                size="large"
                loading={isLoading}
              >
                Подтвердить аккаунт
              </Button>
            </Form.Item>
          </Form>

          <div className="text-center text-sm mt-2">
            <Link to={ROUTES.LOGIN} className="text-blue-600 hover:text-blue-700 hover:underline">
              Вернуться ко входу
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
