import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "./components/header/Header";
import Sideber from "./components/sidebar/Sidebar";
import Footer from "./components/footer/Footer";

export const metadata: Metadata = {
  title: "khinvest",
  description: "khinvest",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <Header />
        <Sideber />
        <Footer />
      </body>
    </html>
  );
}
