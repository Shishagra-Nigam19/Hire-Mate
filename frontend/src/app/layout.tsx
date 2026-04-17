import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "HireMate | AI Career Assistant",
  description: "Your personal AI career counselor powered by OpenAI and LLaMA.",
};

import { ClerkProvider } from "@clerk/nextjs";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <ClerkProvider>
      <html lang="en">
        <body>{children}</body>
      </html>
    </ClerkProvider>
  );
}
