import type { Metadata } from "next";
import { AuthProvider } from "@/app/context/AuthContext";
import { WatchlistProvider } from "@/app/context/WatchlistContext";
import "./globals.css";

export const metadata: Metadata = {
  title: "khinvest",
  description: "khinvest",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>
        <AuthProvider>
          <WatchlistProvider>{children}</WatchlistProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
    