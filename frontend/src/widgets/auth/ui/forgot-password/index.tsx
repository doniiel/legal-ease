import type { FC } from "react";
import { Button, Form, Input, message } from "antd";
import { AUTH_CREDENTIALS } from "../../../../shared/auth/constants";
import { useNavigate } from "react-router-dom";

export const ForgotPasswordForm: FC = () => {
  const [form] = Form.useForm<{ email: string }>();
  const navigate = useNavigate();
  const onFinish = (values: { email: string }) => {
    if (values.email === AUTH_CREDENTIALS.email) {
      message.success(`Your password: ${AUTH_CREDENTIALS.password}`);
    } else {
      message.error("Email not found");
    }
  };

  const onBack = () => {
    navigate(-1);
  }

  return (
    <Form form={form} className="flex flex-col w-full" onFinish={onFinish}>
      <div className="mb-5">
        <h1 className=" text-[25px]">Forgot Password?</h1>
        <p className="text-gray-500 text-[15px]">No worries, we'll send you reset instructions</p>
      </div>
      <Form.Item
        label="Email"
        name="email"
        layout="vertical"
        rules={[
          { required: true, message: "Please enter your email" },
          { type: "email", message: "Please enter a valid email" },
        ]}
      >
        <Input size="large" placeholder="name@example.com"/>
      </Form.Item>
      <Form.Item label={null}>
        <Button size="large" className="w-full" type="primary" htmlType="submit">
          Reset password
        </Button>
      </Form.Item>
      <div className="text-center">
        <a onClick={onBack} className="text-[var(--color-primary)] cursor-pointer hover:underline">
          &larr; Back to Sign in
        </a>
      </div>
    </Form>
  );
}
