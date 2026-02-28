import { Card } from "antd";
import { ForgotPasswordForm } from "../forgot-password";

export const ForgotPasswordBlock = () => {
  return(
    <div className="w-full min-h-screen flex justify-center items-center">
      <Card className="p-6 flex justify-center items-center">
        <ForgotPasswordForm/>
      </Card>
    </div>
  );
}