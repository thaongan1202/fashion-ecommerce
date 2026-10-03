export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(message: string, status: number, code?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

export type ApiRequestOptions = RequestInit & { requireAuth?: boolean }

export function getAccessToken(): string | null {
  return localStorage.getItem('fashionAccessToken')
}

export function hasAccessToken(): boolean {
  return Boolean(getAccessToken())
}

export async function apiRequest(path: string, options: ApiRequestOptions = {}): Promise<Response> {
  const { requireAuth = false, ...init } = options
  const token = getAccessToken()
  if (requireAuth && !token) {
    throw new ApiError('Vui lòng đăng nhập để tiếp tục.', 401, 'AUTH_REQUIRED')
  }

  const headers = new Headers(init.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (typeof init.body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  return fetch(path, { ...init, credentials: 'include', headers })
}

export async function apiJson<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const response = await apiRequest(path, options)
  const body = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(
      body?.message ?? body?.detail ?? 'Không thể thực hiện yêu cầu. Vui lòng thử lại.',
      response.status,
      body?.code,
    )
  }
  return body as T
}
