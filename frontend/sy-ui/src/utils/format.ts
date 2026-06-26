/**
 * Format a date string to yyyy-MM-dd
 */
export function formatDate(date: string | Date): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Format a datetime string to yyyy-MM-dd HH:mm:ss
 */
export function formatDateTime(date: string | Date): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''
  const ymd = formatDate(d)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${ymd} ${h}:${m}:${s}`
}

/**
 * Format a number to 2 decimal places
 */
export function formatAmount(amount: number): string {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

/**
 * Truncate text to maxLen, appending "..." if exceeded
 */
export function truncateText(text: string, maxLen: number): string {
  if (!text) return ''
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen - 1) + '...'
}

/**
 * Get the day of week for a given date (1=Monday, 7=Sunday)
 */
export function getWeekDay(date: string | Date): number {
  const d = typeof date === 'string' ? new Date(date) : date
  // JS getDay: 0=Sunday, so convert: 0→7, 1→1, 2→2, ..., 6→6
  const jsDay = d.getDay()
  return jsDay === 0 ? 7 : jsDay
}

/**
 * Get the Chinese weekday name
 */
export function getWeekDayName(week: number): string {
  const names: Record<number, string> = {
    1: '周一',
    2: '周二',
    3: '周三',
    4: '周四',
    5: '周五',
    6: '周六',
    7: '周日',
  }
  return names[week] || ''
}

/**
 * Get today's date as yyyy-MM-dd
 */
export function today(): string {
  return formatDate(new Date())
}

/**
 * Calculate number of days between two dates (inclusive)
 */
export function daysBetween(start: string, end: string): number {
  const s = new Date(start)
  const e = new Date(end)
  const diff = e.getTime() - s.getTime()
  return Math.floor(diff / (1000 * 60 * 60 * 24)) + 1
}

/**
 * Generate an array of date strings between start and end (inclusive)
 */
export function dateRange(start: string, end: string): string[] {
  const dates: string[] = []
  const s = new Date(start)
  const e = new Date(end)
  const current = new Date(s)
  while (current <= e) {
    dates.push(formatDate(current))
    current.setDate(current.getDate() + 1)
  }
  return dates
}
