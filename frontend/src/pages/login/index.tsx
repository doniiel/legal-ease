import { useNavigate, Link } from "react-router-dom";
import { Button, Input, Form, Card, Alert } from "antd";
import { Mail, Lock } from "lucide-react";
import { useLoginMutation } from "../../features/auth/api/auth-api";

export default function Login() {
  const navigate = useNavigate();
  const [loginMutation, { isLoading, error }] = useLoginMutation();

  const handleSubmit = async (values: { email: string; password: string }) => {
    const result = await loginMutation(values);
    if (!("error" in result)) {
      navigate("/dashboard");
    }
  };

  return (
    <div className="min-h-screen bg-primary flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl text-primary-foreground mb-2">LegalEase</h1>
          <p className="text-white/60">Платформа для работы с документами</p>
        </div>

        <Card title="Вход в систему">
          <Form onFinish={handleSubmit} layout="vertical">
            {error && (
              <Form.Item>
                <Alert type="error" showIcon message="Неверный email или пароль" />
              </Form.Item>
            )}

            <Form.Item
              label="Email"
              name="email"
              rules={[{ required: true, message: "Введите email" }]}
            >
              <Input type="email" placeholder="example@mail.com" size="large" prefix={<Mail size={16} className="text-gray-400" />} />
            </Form.Item>

            <Form.Item
              label="Пароль"
              name="password"
              rules={[{ required: true, message: "Введите пароль" }]}
            >
              <Input.Password placeholder="••••••••" size="large" prefix={<Lock size={16} className="text-gray-400" />} />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                className="w-full"
                size="large"
                loading={isLoading}
              >
                Войти
              </Button>
            </Form.Item>

            <div className="flex justify-between text-sm">
              <span>
                <span className="text-gray-600">Нет аккаунта? </span>
                <Link to="/register" className="text-blue-600 hover:text-blue-700 hover:underline">
                  Зарегистрироваться
                </Link>
              </span>
              <Link to="/forgot-password" className="text-blue-600 hover:text-blue-700 hover:underline">
                Забыли пароль?
              </Link>
            </div>
          </Form>
        </Card>
      </div>
    </div>
  );
}
