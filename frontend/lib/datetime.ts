export function parseVietnamDate(value?: string | null): Date | null {
  if (!value) return null;
  const hasZone = /[zZ]|[+-]\d{2}:?\d{2}$/.test(value);
  const date = new Date(hasZone ? value : `${value}+07:00`);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatVietnamDateTime(value?: string | null): string {
  const date = parseVietnamDate(value);
  if (!date) return "";
  return date.toLocaleString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" });
}

export function formatVietnamDate(value?: string | null): string {
  const date = parseVietnamDate(value);
  if (!date) return "";
  return date.toLocaleDateString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" });
}

export function canReturnOrder(createdAt?: string | null, status?: string | null, returnStatus?: string | null) {
  if (status !== "DELIVERED" || returnStatus) return false;
  const created = parseVietnamDate(createdAt);
  if (!created) return false;
  return Date.now() <= created.getTime() + 3 * 24 * 60 * 60 * 1000;
}
