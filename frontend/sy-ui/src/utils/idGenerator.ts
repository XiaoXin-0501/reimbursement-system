/** Generate a UUID v4 string using the browser crypto API */
export function generateUUID(): string {
  return crypto.randomUUID()
}
