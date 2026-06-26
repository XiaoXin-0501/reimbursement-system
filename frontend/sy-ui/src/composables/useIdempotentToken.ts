import { ref } from 'vue'
import { generateUUID } from '@/utils/idGenerator'

const USER_ID = 'default_user'

export function useIdempotentToken() {
  const token = ref<string>('')

  function generate(): string {
    const t = `sub_${USER_ID}_${Date.now()}_${generateUUID()}`
    token.value = t
    return t
  }

  function refresh(): string {
    return generate()
  }

  function destroy(): void {
    token.value = ''
  }

  function current(): string {
    return token.value
  }

  return { token, generate, refresh, destroy, current }
}
