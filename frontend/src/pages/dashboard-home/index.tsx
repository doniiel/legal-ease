import MainLayout from "../../widgets/layout/ui/MainLayout";

export default function Home() {
  return (
    <MainLayout>
      <h2 style={{ marginBottom: 8, color: "#0F2A44", fontWeight: 600 }}>
        Добро пожаловать в LegalEase
      </h2>
      <p style={{ color: "#6b7280" }}>
        Выберите раздел в боковом меню для начала работы.
      </p>
    </MainLayout>
  );
}
