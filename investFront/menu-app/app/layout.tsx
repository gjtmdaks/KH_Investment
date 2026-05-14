import type { Metadata } from "next";
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
        <WatchlistProvider>{children}</WatchlistProvider>
      </body>
    </html>
  );
}
    