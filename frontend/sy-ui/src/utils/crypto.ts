import CryptoJS from 'crypto-js'

const SECRET_KEY = 'sy-reim-draft-key-2026'

export function encrypt(data: string): string {
  return CryptoJS.AES.encrypt(data, SECRET_KEY).toString()
}

export function decrypt(cipher: string): string {
  const bytes = CryptoJS.AES.decrypt(cipher, SECRET_KEY)
  return bytes.toString(CryptoJS.enc.Utf8)
}

/** Encrypt an object by JSON stringifying first */
export function encryptObject<T>(obj: T): string {
  return encrypt(JSON.stringify(obj))
}

/** Decrypt a string and parse back to object */
export function decryptObject<T>(cipher: string): T | null {
  try {
    const json = decrypt(cipher)
    if (!json) return null
    return JSON.parse(json) as T
  } catch {
    return null
  }
}
