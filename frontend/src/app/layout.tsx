import type { Metadata } from "next";
import { ClerkProvider } from "@clerk/nextjs";
import "./globals.css";

export const metadata: Metadata = {
  title: "HireMate | AI Career Assistant",
  description: "Your personal AI career counselor powered by OpenAI and LLaMA.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <ClerkProvider>
      <html lang="en" suppressHydrationWarning>
        <body 
          suppressHydrationWarning 
          style={{ backgroundColor: '#020617', color: '#f8fafc', minHeight: '100vh' }}
        >
          {children}
        </body>
      </html>
    </ClerkProvider>
  );
}
