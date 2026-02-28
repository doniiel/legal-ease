import type { FC } from "react";
import { Button } from "antd";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../../../app/router/router";


export const DashboardPage: FC = () => {
  const navigate = useNavigate();
  const onLogout = () => { 
    navigate(ROUTES.AUTH);
  }
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-6">
      <h1 className="text-3xl font-bold text-[var(--color-primary)]">
        Welcome to LegalEase
      </h1>
      <p className="text-gray-500">You are successfully authenticated</p>
      <Button type="primary" size="large" onClick={onLogout}>
        Log out
      </Button>
    </div>
  );
}
