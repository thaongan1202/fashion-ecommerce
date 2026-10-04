import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth-context";
import { Toaster } from "@/components/ui/sonner";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0a0a0a" },
  ],
};

export const metadata: Metadata = {
  title: {
    default: "UTE Fashion Hub - Cửa hàng thời trang",
    template: "%s | UTE Fashion Hub",
  },
  description: "Cửa hàng thời trang trực tuyến. Quần áo, giày dép, túi xách và phụ kiện với giá rõ ràng, giao hàng toàn quốc.",
  keywords: ["thời trang", "quần áo", "giày", "túi xách", "phụ kiện", "UTE Fashion Hub"],
  authors: [{ name: "UTE Fashion Hub" }],
  creator: "UTE Fashion Hub",
  icons: {
    icon: [
      { url: "/favicon.ico", sizes: "any" },
      { url: "/icon.svg", type: "image/svg+xml" },
    ],
    apple: "/apple-touch-icon.png",
  },
  openGraph: {
    title: "UTE Fashion Hub - Cửa hàng thời trang",
    description: "Quần áo, giày dép, túi xách và phụ kiện thời trang",
    url: "https://utefashionhub.com",
    siteName: "UTE Fashion Hub",
    locale: "vi_VN",
    type: "website",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <AuthProvider>
          {children}
          <Toaster position="top-right" richColors closeButton />
        </AuthProvider>
      </body>
    </html>
  );
}
