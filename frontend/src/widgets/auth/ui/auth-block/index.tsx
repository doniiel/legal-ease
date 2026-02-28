import type { FC } from "react";
import { SignInForm } from "../sign_in";
import { SignUpForm } from "../sign_up";

type Props = {
  isSignUp: boolean;
  hasToggled: boolean;
  onAuthSuccess: () => void;
}

export const AuthBlock: FC<Props> = ({ isSignUp, hasToggled, onAuthSuccess }) => {
  const signInClass = hasToggled
    ? (!isSignUp ? "block_up" : "block_down")
    : (!isSignUp ? "" : "invisible opacity-0");

  const signUpClass = hasToggled
    ? (!isSignUp ? "block_down" : "block_up")
    : (!isSignUp ? "invisible opacity-0" : "");

  return(
    <div className={`flex w-full min-h-screen relative transition-all duration-500 ${isSignUp ? "translate-x-1/2" : "translate-x-0"}`}>
      <div className={`w-[50%] min-h-screen flex items-center justify-center px-10 absolute ${signInClass}`}>
        <div className="w-full max-w-md">
          <SignInForm onSuccess={onAuthSuccess}/>
        </div>
      </div>
      <div className={`w-[50%] min-h-screen flex items-center justify-center px-10 absolute ${signUpClass}`}>
        <div className="w-full max-w-md">
          <SignUpForm onSuccess={onAuthSuccess}/>
        </div>
      </div>
    </div>
  );
}
