import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Button, Input, Form, Card, Steps, notification } from "antd";
import { Mail, KeyRound, Lock } from "lucide-react";
import {
  useSendResetCodeMutation,
  useVerifyResetCodeMutation,
  useResetPasswordMutation,
} from "../../features/auth";

type Step = "email" | "code" | "password";

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [verifiedCode, setVerifiedCode] = useState("");

  const [sendCode, { isLoading: isSending }] = useSendResetCodeMutation();
  const [verifyCode, { isLoading: isVerifying }] = useVerifyResetCodeMutation();
  const [resetPassword, { isLoading: isResetting }] = useResetPasswordMutation();

  const stepIndex = { email: 0, code: 1, password: 2 }[step];

  const handleEmailSubmit = async (values: { email: string }) => {
    const result = await sendCode({ email: values.email });
    if (!("error" in result)) {
      setEmail(values.email);
      setStep("code");
    } else {
      notification.error({ message: "Пользователь с таким email не найден" });
    }
  };

  const handleCodeSubmit = async (values: { code: string }) => {
    const result = await verifyCode({ email, code: values.code });
    if (!("errorCode" in result)) {
      setVerifiedCode(values.code);
      setStep("password");
    } else {
      notification.error({ message: "Неверный код подтверждения" });
    }
  };

  const handlePasswordSubmit = async (values: { password: string }) => {
    const result = await resetPassword({
      email,
      code: verifiedCode,
      newPassword: values.password,
    });
    if (!("error" in result)) {
      notification.success({ message: "Пароль успешно изменён" });
      navigate("/login");
    } else {
      notification.error({ message: "Не удалось сменить пароль" });
    }
  };

  return (
    <div className="min-h-screen bg-primary flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl text-primary-foreground mb-2">LegalEase</h1>
          <p className="text-white/60">Восстановление пароля</p>
        </div>

        <Card>
          <Steps
            current={stepIndex}
            className="mb-8"
            items={[
              { title: "Email" },
              { title: "Код" },
              { title: "Пароль" },
            ]}
          />

          {step === "email" && (
            <Form onFinish={handleEmailSubmit} layout="vertical">
              <Form.Item
                label="Введите ваш email"
                name="email"
                rules={[
                  { required: true, message: "Введите email" },
                  { type: "email", message: "Некорректный email" },
                ]}
              >
                <Input type="email" placeholder="example@mail.com" size="large" prefix={<Mail size={16} className="text-gray-400" />} />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  className="w-full"
                  size="large"
                  loading={isSending}
                >
                  Отправить код
                </Button>
              </Form.Item>
            </Form>
          )}

          {step === "code" && (
            <Form onFinish={handleCodeSubmit} layout="vertical">
              <p className="text-sm text-gray-500 mb-4">
                Код подтверждения отправлен на <strong>{email}</strong>
              </p>

              <Form.Item
                label="Код подтверждения"
                name="code"
                rules={[{ required: true, message: "Введите код" }]}
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
                  loading={isVerifying}
                >
                  Подтвердить
                </Button>
              </Form.Item>

              <Button type="link" className="w-full" onClick={() => setStep("email")}>
                Назад
              </Button>
            </Form>
          )}

          {step === "password" && (
            <Form onFinish={handlePasswordSubmit} layout="vertical">
              <Form.Item
                label="Новый пароль"
                name="password"
                rules={[
                  { required: true, message: "Введите пароль" },
                  { min: 8, message: "Минимум 8 символов" },
                  {
                    pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                    message: "Пароль должен содержать заглавную, строчную букву и цифру",
                  },
                ]}
              >
                <Input.Password placeholder="••••••••" size="large" prefix={<Lock size={16} className="text-gray-400" />} />
              </Form.Item>

              <Form.Item
                label="Подтвердите пароль"
                name="confirmPassword"
                dependencies={["password"]}
                rules={[
                  { required: true, message: "Подтвердите пароль" },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue("password") === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error("Пароли не совпадают"));
                    },
                  }),
                ]}
              >
                <Input.Password placeholder="••••••••" size="large" prefix={<Lock size={16} className="text-gray-400" />} />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  className="w-full"
                  size="large"
                  loading={isResetting}
                >
                  Сохранить пароль
                </Button>
              </Form.Item>
            </Form>
          )}

          <div className="text-center text-sm mt-2">
            <Link to="/login" className="text-blue-600 hover:text-blue-700 hover:underline">
              Вернуться ко входу
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
