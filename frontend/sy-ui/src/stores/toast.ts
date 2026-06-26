import { defineStore } from 'pinia'

export type ToastType = 'success' | 'warning' | 'info' | 'error'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
  duration: number
}

type Listener = (items: ToastItem[]) => void

const listeners = new Set<Listener>()
let seq = 0
let items: ToastItem[] = []

function emit() {
  listeners.forEach((l) => l(items.slice()))
}

export function subscribe(listener: Listener): () => void {
  listeners.add(listener)
  listener(items.slice())
  return () => {
    listeners.delete(listener)
  }
}

function show(type: ToastType, message: string, duration = 2500): void {
  const id = ++seq
  items = [...items, { id, type, message, duration }]
  emit()
  window.setTimeout(() => {
    const idx = items.findIndex((t) => t.id === id)
    if (idx >= 0) {
      const next = items.slice()
      next.splice(idx, 1)
      items = next
      emit()
    }
  }, duration + 250)
}

export const useToastStore = defineStore('appToast', {
  actions: {
    success(msg: string, duration?: number) {
      show('success', msg, duration)
    },
    warning(msg: string, duration?: number) {
      show('warning', msg, duration)
    },
    info(msg: string, duration?: number) {
      show('info', msg, duration)
    },
    error(msg: string, duration?: number) {
      show('error', msg, duration)
    },
  },
})
