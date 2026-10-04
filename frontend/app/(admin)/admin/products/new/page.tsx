'use client';

import { useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ProductForm } from '@/components/features/admin';
import { useAuth } from '@/lib/auth-context';

export default function NewAdminProductPage() {
  const router = useRouter();
  const { user, isLoading } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    if (isLoading) return;
    if (!user) router.replace('/login');
    else if (!isAdmin) router.replace('/user');
  }, [isAdmin, isLoading, router, user]);

  if (isLoading || !isAdmin) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-muted-foreground">Đang tải...</p>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-secondary px-4 py-8">
      <div className="mx-auto max-w-5xl space-y-6">
        <Button variant="ghost" asChild>
          <Link href="/admin?tab=products">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại quản lý sản phẩm
          </Link>
        </Button>
        <header>
          <h1 className="text-2xl font-semibold">Thêm sản phẩm</h1>
          <p className="text-sm text-muted-foreground">Nhập thông tin sản phẩm và biến thể để thêm vào cửa hàng.</p>
        </header>
        <ProductForm onSuccess={() => router.push('/admin?tab=products')} />
      </div>
    </main>
  );
}
