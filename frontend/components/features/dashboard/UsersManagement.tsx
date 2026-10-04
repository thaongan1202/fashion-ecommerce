'use client';

import { useState, useEffect, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Plus, RefreshCw } from 'lucide-react';
import { adminUserAPI } from '@/lib/api';
import { toast } from 'sonner';
import {
  ConfirmDialog,
  CreateUserDialog,
  UserFiltersComponent,
  UserTable,
  UserDetailCard,
} from '@/components/features/users';
import type { User, UserFilters } from '@/types';

export function UsersManagement() {
  // State management
  const [users, setUsers] = useState<User[]>([]);
  const [searchedUser, setSearchedUser] = useState<User | null>(null); // For detail view
  const [isLoading, setIsLoading] = useState(true);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10,
  });
  const [filters, setFilters] = useState<UserFilters>({
    role: 'ALL',
    status: 'ALL',
    keyword: '',
  });

  // Dialog states
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    description: string;
    onConfirm: () => void;
  }>({
    open: false,
    title: '',
    description: '',
    onConfirm: () => {},
  });

  // Fetch users
  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setSearchedUser(null); // Reset searched user when fetching list
    try {
      // If keyword (ID) is provided, fetch single user by ID
      if (filters.keyword && filters.keyword.trim() !== '') {
        const userId = parseInt(filters.keyword);
        if (isNaN(userId)) {
          toast.error('ID không hợp lệ', {
            description: 'Vui lòng nhập số ID hợp lệ',
          });
          setIsLoading(false);
          return;
        }

        console.log('🔍 Fetching user by ID:', userId);
        const response = await adminUserAPI.getUserById(userId);
        
        if (response.success && response.data) {
          // Show detail card for searched user
          setSearchedUser(response.data);
          setUsers([]);
          toast.success('Tìm thấy người dùng', {
            description: `Hiển thị thông tin của ${response.data.fullName}`,
          });
        } else {
          setSearchedUser(null);
          setUsers([]);
          setPagination({
            currentPage: 0,
            totalPages: 0,
            totalElements: 0,
            pageSize: 10,
          });
          toast.error('Không tìm thấy người dùng với ID này');
        }
      } else {
        // Fetch all users with pagination and filters
        console.log('🔍 Fetching users with filters:', {
          page: pagination.currentPage,
          size: pagination.pageSize,
          role: filters.role,
          status: filters.status,
        });

        const response = await adminUserAPI.getUsers({
          page: pagination.currentPage,
          size: pagination.pageSize,
          role: filters.role,
          status: filters.status,
        });

        console.log('✅ API Response:', response);

        if (response.success && response.data) {
          setUsers(response.data.users || []);
          setPagination({
            currentPage: response.data.currentPage,
            totalPages: response.data.totalPages,
            totalElements: response.data.totalElements,
            pageSize: response.data.pageSize,
          });
        }
      }
    } catch (error: any) {
      console.error('❌ Fetch error:', error);
      toast.error('Lỗi', {
        description: error.message || 'Không thể tải danh sách người dùng',
      });
      setUsers([]);
    } finally {
      setIsLoading(false);
    }
  }, [pagination.currentPage, pagination.pageSize, filters.role, filters.status, filters.keyword]);

  // Initial load - only once on mount
  useEffect(() => {
    fetchUsers();
  }, [pagination.currentPage, filters.role, filters.status]);

  // Debounced search for keyword only
  useEffect(() => {
    if (filters.keyword === '') {
      return;
    }

    const timer = setTimeout(() => {
      fetchUsers();
    }, 500);

    return () => clearTimeout(timer);
  }, [filters.keyword, fetchUsers]);

  // Handle filter changes
  const handleFiltersChange = (newFilters: UserFilters) => {
    // Reset to first page when filters change
    setPagination((prev) => ({ ...prev, currentPage: 0 }));
    setFilters(newFilters);
  };

  // Handle lock user
  const handleLockUser = (user: User) => {
    setConfirmDialog({
      open: true,
      title: 'Khóa tài khoản',
      description: `Bạn có chắc chắn muốn khóa tài khoản "${user.fullName}" (${user.email})?`,
      onConfirm: async () => {
        try {
          const response = await adminUserAPI.lockUser(user.id);
          if (response.success) {
            toast.success('Thành công', {
              description: 'Đã khóa tài khoản',
            });
            fetchUsers();
          }
        } catch (error: any) {
          toast.error('Lỗi', {
            description: error.message || 'Không thể khóa tài khoản',
          });
        }
        setConfirmDialog({ ...confirmDialog, open: false });
      },
    });
  };

  // Handle unlock user
  const handleUnlockUser = (user: User) => {
    setConfirmDialog({
      open: true,
      title: 'Mở khóa tài khoản',
      description: `Bạn có chắc chắn muốn mở khóa tài khoản "${user.fullName}" (${user.email})?`,
      onConfirm: async () => {
        try {
          const response = await adminUserAPI.unlockUser(user.id);
          if (response.success) {
            toast.success('Thành công', {
              description: 'Đã mở khóa tài khoản',
            });
            fetchUsers();
          }
        } catch (error: any) {
          toast.error('Lỗi', {
            description: error.message || 'Không thể mở khóa tài khoản',
          });
        }
        setConfirmDialog({ ...confirmDialog, open: false });
      },
    });
  };

  const handleDeleteUser = (user: User) => {
    setConfirmDialog({
      open: true,
      title: 'Xóa tài khoản khách hàng',
      description: `Bạn có chắc chắn muốn xóa tài khoản "${user.fullName}" (${user.email})? Thông tin cá nhân sẽ bị ẩn danh; lịch sử đơn hàng và đánh giá được giữ lại.`,
      onConfirm: async () => {
        try {
          const response = await adminUserAPI.deleteUser(user.id);
          if (response.success) {
            toast.success('Đã xóa tài khoản khách hàng');
            if (users.length === 1 && pagination.currentPage > 0) {
              setPagination((current) => ({ ...current, currentPage: current.currentPage - 1 }));
            } else {
              void fetchUsers();
            }
          }
        } catch (error: any) {
          toast.error('Không thể xóa tài khoản', {
            description: error.message || 'Vui lòng thử lại',
          });
        } finally {
          setConfirmDialog({ ...confirmDialog, open: false });
        }
      },
    });
  };

  // Handle pagination
  const handlePageChange = (newPage: number) => {
    setPagination((prev) => ({ ...prev, currentPage: newPage }));
  };

  return (
    <div className="space-y-6">
      {/* Actions Bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <UserFiltersComponent filters={filters} onFiltersChange={handleFiltersChange} />
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={fetchUsers} disabled={isLoading}>
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
            <span className="ml-2 hidden sm:inline">Làm mới</span>
          </Button>
          <Button size="sm" onClick={() => setCreateDialogOpen(true)}>
            <Plus className="h-4 w-4" />
            <span className="ml-2">Tạo tài khoản</span>
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="rounded-lg border bg-card p-4">
        <p className="text-sm text-muted-foreground">Tổng số người dùng</p>
        <p className="text-2xl font-bold">{pagination.totalElements}</p>
      </div>

      {/* Show UserDetailCard if searched by ID, otherwise show table */}
      {searchedUser ? (
        <UserDetailCard user={searchedUser} onClose={() => {
          setSearchedUser(null);
          setFilters((prev) => ({ ...prev, keyword: '' }));
        }} />
      ) : (
        <>
          {/* User Table */}
          <UserTable
            users={users}
            isLoading={isLoading}
            onLockUser={handleLockUser}
            onUnlockUser={handleUnlockUser}
            onDeleteUser={handleDeleteUser}
          />

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Trang {pagination.currentPage + 1} / {pagination.totalPages} ({pagination.totalElements} người dùng)
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(pagination.currentPage - 1)}
                  disabled={pagination.currentPage === 0}
                >
                  Trước
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(pagination.currentPage + 1)}
                  disabled={pagination.currentPage >= pagination.totalPages - 1}
                >
                  Sau
                </Button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Dialogs */}
      <CreateUserDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={fetchUsers}
      />

      <ConfirmDialog
        open={confirmDialog.open}
        onOpenChange={(open) => setConfirmDialog({ ...confirmDialog, open })}
        onConfirm={confirmDialog.onConfirm}
        title={confirmDialog.title}
        description={confirmDialog.description}
        variant="destructive"
      />
    </div>
  );
}
