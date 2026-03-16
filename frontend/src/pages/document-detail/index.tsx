import { useParams, Navigate } from "react-router-dom";
import MainLayout from "../../widgets/layout/ui/MainLayout";
import DocumentDetailPanel from "../../widgets/documents/ui/DocumentDetailPanel";
import { ROUTES } from "../../app/router/router";

export default function DocumentDetail() {
  const { id } = useParams<{ id: string }>();
  const docId = Number(id);

  if (!id || isNaN(docId)) {
    return <Navigate to={ROUTES.DOCUMENTS} replace />;
  }

  return (
    <MainLayout>
      <DocumentDetailPanel documentId={docId} />
    </MainLayout>
  );
}
