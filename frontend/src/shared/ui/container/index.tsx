import type { FC } from "react";

type Props = {
  children: React.ReactNode;
  className?: string;
}

export const Container: FC<Props> = ({ children, className }) => {
  return (
    <div className={`w-[90%] mx-auto ${className}`}>
      {children}
    </div>
  );
}
