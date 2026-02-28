import type { FC } from "react";
import { Button, Form, Input, message } from "antd";
import { AUTH_CREDENTIALS } from "../../../../shared/auth/constants";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../../../../app/router/router";

type Props = {
  onSuccess: () => void;
}

type SignInValues = {
  username: string;
  password: string;
}

export const SignInForm: FC<Props> = ({ onSuccess }) => {
  const [form] = Form.useForm<SignInValues>();
  const navigate = useNavigate();
  const onFinish = (values: SignInValues) => {
    if (
      values.username === AUTH_CREDENTIALS.email &&
      values.password === AUTH_CREDENTIALS.password
    ) {
      message.success("Successfully signed in!");
      onSuccess();
    } else {
      message.error("Invalid email or password");
    }
  };
  const toForgotPassword = () => {
    navigate(ROUTES.FORGOT);
  }
  return(
    <Form form={form} className="flex flex-col w-full" onFinish={onFinish}>
      <div className="mb-12">
        <h1 className="mb-3 text-[25px]">Welcome back</h1>
        <p className="text-gray-500">Please enter your details to sign in</p>
      </div>
      <Form.Item
        label="Email"
        name="username"
        layout="vertical"
        rules={[{ required: true, message: "Please enter your email" }]}
      >
        <Input size="large" placeholder="name@example.com"/>
      </Form.Item>
      <Form.Item
        label="Password"
        name="password"
        layout="vertical"
        rules={[{ required: true, message: "Please enter your password" }]}
      >
        <Input.Password size="large" placeholder="Enter your password"/>
      </Form.Item>
      <Form.Item
        name="forgot"
      >
        <Button onClick={toForgotPassword} type="link" className="text-[var(--color-primary)] flex justify-end">Forgot password?</Button>
      </Form.Item>
      <Form.Item label={null}>
        <Button size="large" className="w-full" type="primary" htmlType="submit">
          Sign in
        </Button>
      </Form.Item>
    </Form>
  );
}
