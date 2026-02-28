import type { FC } from "react";
import { Button, Form, Input, message } from "antd";
import { AUTH_CREDENTIALS } from "../../../../shared/auth/constants";

type Props = {
  onSuccess: () => void;
}

type SignUpValues = {
  nickname: string;
  email: string;
  password: string;
  confirm_password: string;
}

export const SignUpForm: FC<Props> = ({ onSuccess }) => {
  const [form] = Form.useForm<SignUpValues>();

  const onFinish = (values: SignUpValues) => {
    if (values.password !== values.confirm_password) {
      message.error("Passwords do not match");
      return;
    }

    if (
      values.email === AUTH_CREDENTIALS.email &&
      values.nickname === AUTH_CREDENTIALS.nickname &&
      values.password === AUTH_CREDENTIALS.password
    ) {
      message.success("Account created successfully!");
      onSuccess();
    } else {
      message.error("Invalid registration data. Use the preset credentials.");
    }
  };

  return(
    <Form form={form} className="flex flex-col w-full" onFinish={onFinish}>
      <div className="mb-12">
        <h1 className="mb-3 text-[25px]">Create Account</h1>
        <p className="text-gray-500">Fill in your details to get started</p>
      </div>
      <Form.Item
        label="Nickname"
        name="nickname"
        layout="vertical"
        rules={[{ required: true, message: "Please enter your nickname" }]}
      >
        <Input size="large" placeholder="Your nickname"/>
      </Form.Item>
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
      <Form.Item
        label="Password"
        name="password"
        layout="vertical"
        rules={[{ required: true, message: "Please enter your password" }]}
      >
        <Input.Password size="large" placeholder="Create a password"/>
      </Form.Item>
      <Form.Item
        label="Confirm Password"
        name="confirm_password"
        layout="vertical"
        rules={[{ required: true, message: "Please confirm your password" }]}
      >
        <Input.Password size="large" placeholder="Confirm your password"/>
      </Form.Item>
      <Form.Item label={null}>
        <Button size="large" className="w-full" type="primary" htmlType="submit">
          Sign up
        </Button>
      </Form.Item>
    </Form>
  );
}
