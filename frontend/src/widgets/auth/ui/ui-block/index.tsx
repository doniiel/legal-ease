import type { FC } from "react";
import { Button } from "antd";

type Props = {
  isSignUp: boolean;
  onToggle: () => void;
}

export const UIBlock: FC<Props> = ({ isSignUp, onToggle }) => {
  return(
    <div
      className={`w-[50%] 
        min-h-screen 
        flex flex-col 
        text-center gap-5 
        justify-center 
        bg-[var(--color-primary)] text-white absolute 
        top-0 z-10 transition-all duration-500
        overflow-hidden
        ${
        isSignUp ? "left-0" : "left-[50%]"
      }`}
    >
      <div className={`
        w-[400%] flex
        transition-transform duration-500 ease-in-out
        ${isSignUp ? "translate-x-0" : "-translate-x-1/2"}
      `}>
        <div className="w-1/2">
          <div className="w-1/2 flex items-center justify-center">
            <div className="flex flex-col gap-[15px] px-8">
              <div className="text-[30px] font-bold">
                Welcome Back!
              </div>
              <div className="text-white/80">
                To keep connected with us please login with your personal info
              </div>
              <Button type="default" onClick={onToggle} className="mt-4 bg-transparent text-white">
                Sign In
              </Button>
            </div>
          </div>
        </div>
        <div className="w-1/2">
          <div className="w-1/2 flex items-center justify-center">
            <div className="flex flex-col gap-[15px] px-8">
              <div className="text-[30px] font-bold">
                Hello, Friend!
              </div>
              <div className="text-white/80">
                Enter your personal details and start your journey with us
              </div>
              <Button onClick={onToggle} className="mt-4 bg-transparent text-white">
                Sign Up
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
