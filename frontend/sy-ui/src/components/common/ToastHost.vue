<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { subscribe } from '@/stores/toast'
import type { ToastItem } from '@/stores/toast'

const toasts = ref<ToastItem[]>([])

let unsub: (() => void) | null = null
onMounted(() => {
  unsub = subscribe((items) => {
    toasts.value = items
  })
})
onBeforeUnmount(() => {
  if (unsub) unsub()
  unsub = null
})
</script>

<template>
  <Teleport to="body">
    <div class="app-toast-host">
      <transition-group name="toast-fade" tag="div" class="app-toast-stack">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="app-toast"
          :class="'app-toast--' + t.type"
          role="status"
        >
          <span class="app-toast__icon" aria-hidden="true" v-if="t.type === 'success'">✓</span>
          <span class="app-toast__icon" aria-hidden="true" v-else-if="t.type === 'warning'">!</span>
          <span class="app-toast__icon" aria-hidden="true" v-else-if="t.type === 'error'">×</span>
          <span class="app-toast__icon" aria-hidden="true" v-else>i</span>
          <span class="app-toast__msg">{{ t.message }}</span>
        </div>
      </transition-group>
    </div>
  </Teleport>
</template>

<style>
.app-toast-host {
  position: fixed;
  top: 22px;
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  justify-content: center;
  pointer-events: none;
}
.app-toast-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  pointer-events: none;
}
.app-toast {
  pointer-events: auto;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  border-radius: 6px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
  background: #fff;
  color: #333;
  font-size: 14px;
  min-width: 180px;
  max-width: 80vw;
  border: 1px solid #ebeef5;
  font-weight: 500;
}
.app-toast__icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  line-height: 1;
}
.app-toast--success { border-color: #e1f3d8; background: #f0f9eb; color: #529b2e; }
.app-toast--success .app-toast__icon { background: #67c23a; }
.app-toast--warning { border-color: #faecd8; background: #fdf6ec; color: #b88230; }
.app-toast--warning .app-toast__icon { background: #e6a23c; }
.app-toast--error { border-color: #fde2e2; background: #fef0f0; color: #c45656; }
.app-toast--error .app-toast__icon { background: #f56c6c; }
.app-toast--info { border-color: #ebeef5; background: #edf2fc; color: #3d71b8; }
.app-toast--info .app-toast__icon { background: #909399; }
.app-toast__msg { line-height: 1.4; }

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
