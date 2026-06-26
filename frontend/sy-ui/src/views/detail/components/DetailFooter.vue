<script setup lang="ts">
import { ref } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const props = defineProps<{
  isAudit: boolean
  isSubmitting: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: []
}>()

const store = useReimbursementStore()
const closeConfirmVisible = ref(false)

function handleClose(): void {
  if (props.isAudit) {
    emit('close')
  } else {
    closeConfirmVisible.value = true
  }
}
function confirmClose(): void { emit('close'); closeConfirmVisible.value = false }
</script>

<template>
  <div class="detail-footer">
    <div class="footer-inner">
      <el-button class="btn-close" :disabled="isSubmitting" @click="handleClose">关闭</el-button>
      <el-button
        v-if="!isAudit"
        class="btn-submit" type="primary"
        :disabled="isSubmitting" :loading="isSubmitting"
        @click="emit('submit')"
      >
        {{ isSubmitting ? '提交中...' : '提交' }}
      </el-button>
    </div>

    <ConfirmDialog
      :visible="closeConfirmVisible"
      title="提示" message="确认关闭？"
      @confirm="confirmClose" @cancel="closeConfirmVisible = false"
    />
  </div>
</template>

<style scoped>
.detail-footer {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  border-top: 1px solid #ebeef5; padding: 12px 0; z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
}
.footer-inner {
  width: 1200px; margin: 0 auto; display: flex; justify-content: center; gap: 24px;
}
.btn-close { background: #fff; border: 1px solid #dcdfe6; color: #606266; min-width: 120px; }
.btn-submit { background: #1a3a5c; border-color: #1a3a5c; color: #fff; min-width: 120px; }
</style>
