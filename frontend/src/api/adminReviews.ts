export type AdminReview = {
  id: number; productId: number; productName: string; userName: string; rating: number
  comment: string; imageUrl: string | null; status: string; size: string | null; color: string | null
  heightCm: number | null; weightKg: number | null; createdAt: string
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, { ...init, credentials: 'include', headers: { 'Content-Type': 'application/json', ...init?.headers } })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Không thể xử lý review.')
  }
  return response.json() as Promise<T>
}

export const getPendingReviews = () => request<AdminReview[]>('/api/admin/reviews')
export const setReviewStatus = (id: number, status: 'APPROVED' | 'HIDDEN') =>
  request<AdminReview>(`/api/admin/reviews/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
