import type { DemoSession, DemoUser } from '../types/auth'

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, { ...init, headers: { 'Content-Type': 'application/json', ...init?.headers } })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Không thể thực hiện đăng nhập demo.')
  }
  return response.json() as Promise<T>
}

export const getDemoUsers = () => request<DemoUser[]>('/api/demo/users')
export const getDemoSession = () => request<DemoSession>('/api/demo/session')
export const loginDemo = (email: string) => request<DemoSession>('/api/demo/login', { method: 'POST', body: JSON.stringify({ email }) })
export async function logoutDemo() {
  const response = await fetch('/api/demo/logout', { method: 'POST' })
  if (!response.ok) throw new Error('Không thể đăng xuất phiên demo.')
}
