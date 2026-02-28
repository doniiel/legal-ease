import { useNavigate } from "react-router-dom";
import { Container } from "../../../shared/container";
import { AuthBlock } from "../../../widgets/auth/ui/auth-block";
import { UIBlock } from "../../../widgets/auth/ui/ui-block";
import { useState } from "react";
import { ROUTES } from "../../../app/router/router";

export const AuthorizationPage = () => {
  const [isSignUp, setIsSignUp] = useState(false)
  const [hasToggled, setHasToggled] = useState(false)
  const [isAuth, setIsAuth] = useState(false)
  const navigate = useNavigate();
  if (isAuth) {
    navigate(ROUTES.DASHBOARD);
  }
  return(
    <Container className="w-[80%] relative">
      <AuthBlock
        isSignUp={isSignUp}
        hasToggled={hasToggled}
        onAuthSuccess={() => setIsAuth(true)}
      />
      <UIBlock isSignUp={isSignUp} onToggle={() => { if (!hasToggled) setHasToggled(true); setIsSignUp(prev => !prev); }} />
    </Container>
  );
}