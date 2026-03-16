import { useNavigate, Link } from "react-router-dom";
import { ROUTES } from "../../app/router/router";
import { Button, Input, Form, Card, Alert, Select } from "antd";
import { Mail, Phone, CreditCard, Lock } from "lucide-react";
import { useRegisterMutation } from "../../features/auth/api/auth-api";

const formatPhoneDisplay = (digits: string) => {
  const d = digits.slice(0, 10);
  if (d.length === 0) return "";
  if (d.length <= 3) return `(${d}`;
  if (d.length <= 6) return `(${d.slice(0, 3)}) ${d.slice(3)}`;
  if (d.length <= 8) return `(${d.slice(0, 3)}) ${d.slice(3, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 3)}) ${d.slice(3, 6)}-${d.slice(6, 8)}-${d.slice(8)}`;
};

type FormValues = {
  firstName: string;
  middleName: string;
  lastName: string;
  iin: string;
  gender: "MALE" | "FEMALE";
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
};

const genderOptions = [
  { value: "MALE", label: "Мужской" },
  { value: "FEMALE", label: "Женский" },
];

export default function Register() {
  const navigate = useNavigate();
  const [registerMutation, { isLoading, error }] = useRegisterMutation();

  const handleSubmit = async (values: FormValues) => {
    try {
      await registerMutation({
        firstName: values.firstName,
        middleName: values.middleName,
        lastName: values.lastName,
        iin: values.iin,
        gender: values.gender,
        email: values.email,
        phone: `+7${values.phone.replace(/\D/g, "")}`,
        password: values.password,
      }).unwrap();
      navigate(ROUTES.CONFIRM, { state: { email: values.email } });
    } catch {
      // error is shown via the `error` state from useRegisterMutation
    }
  };

  return (
    <div className="min-h-screen bg-primary flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">
        <div className="text-center mb-8">
          <h1 className="text-3xl text-primary-foreground mb-2">LegalEase</h1>
          <p className="text-white/60">Создайте аккаунт для начала работы</p>
        </div>

        <Card title="Регистрация">
          <Form onFinish={handleSubmit} layout="vertical">
            {error && (
              <Form.Item>
                <Alert type="error" showIcon message="Пользователь с таким email уже существует" />
              </Form.Item>
            )}

            <div className="grid grid-cols-3 gap-3">
              <Form.Item
                label="Фамилия"
                name="lastName"
                rules={[{ required: true, message: "Введите фамилию" }]}
              >
                <Input placeholder="Иванов" size="large" />
              </Form.Item>

              <Form.Item
                label="Имя"
                name="firstName"
                rules={[{ required: true, message: "Введите имя" }]}
              >
                <Input placeholder="Иван" size="large" />
              </Form.Item>

              <Form.Item
                label="Отчество"
                name="middleName"
                rules={[{ required: true, message: "Введите отчество" }]}
              >
                <Input placeholder="Иванович" size="large" />
              </Form.Item>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <Form.Item
                label="ИИН"
                name="iin"
                rules={[
                  { required: true, message: "Введите ИИН" },
                  { len: 12, message: "ИИН должен содержать 12 цифр" },
                  { pattern: /^\d{12}$/, message: "ИИН содержит только цифры" },
                ]}
              >
                <Input
                  placeholder="716987977608"
                  size="large"
                  maxLength={12}
                  prefix={<CreditCard size={16} className="text-gray-400" />}
                />
              </Form.Item>

              <Form.Item
                label="Пол"
                name="gender"
                rules={[{ required: true, message: "Выберите пол" }]}
              >
                <Select size="large" placeholder="Выберите пол" options={genderOptions} />
              </Form.Item>
            </div>

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
              label="Телефон"
              name="phone"
              getValueFromEvent={(e) => {
                const digits = e.target.value.replace(/\D/g, "");
                return formatPhoneDisplay(digits);
              }}
              rules={[
                { required: true, message: "Введите телефон" },
                {
                  validator(_, value) {
                    const digits = (value || "").replace(/\D/g, "");
                    if (digits.length === 10) return Promise.resolve();
                    return Promise.reject(new Error("Введите полный номер (10 цифр после +7)"));
                  },
                },
              ]}
            >
              <Input
                prefix={<span className="flex items-center gap-1 text-gray-500 pr-1"><Phone size={14} className="text-gray-400" />+7</span>}
                placeholder="(700) 005-91-31"
                size="large"
              />
            </Form.Item>

            <div className="grid grid-cols-2 gap-3">
              <Form.Item
                label="Пароль"
                name="password"
                rules={[
                  { required: true, message: "Введите пароль" },
                  { min: 6, message: "Минимум 6 символов" },
                ]}
              >
                <Input.Password
                  placeholder="••••••••"
                  size="large"
                  prefix={<Lock size={16} className="text-gray-400" />}
                />
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
                <Input.Password
                  placeholder="••••••••"
                  size="large"
                  prefix={<Lock size={16} className="text-gray-400" />}
                />
              </Form.Item>
            </div>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                className="w-full"
                size="large"
                loading={isLoading}
              >
                Зарегистрироваться
              </Button>
            </Form.Item>

            <div className="text-center text-sm">
              <span className="text-gray-600">Уже есть аккаунт? </span>
              <Link to="/login" className="text-blue-600 hover:text-blue-700 hover:underline">
                Войти
              </Link>
            </div>
          </Form>
        </Card>
      </div>
    </div>
  );
}
