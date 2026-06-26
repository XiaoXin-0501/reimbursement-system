<script setup lang="ts">
import { ref, computed } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  message?: string
  showCancel?: boolean
}>(), {
  showCancel: true,
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const localVisible = computed({
  get: () => props.visible,
  set: (v) => { if (!v) emit('cancel') },
})

function handleConfirm(): void {
  emit('confirm')
}
function handleCancel(): void {
  emit('cancel')
}
</script>

<template>
  <el-dialog
    v-model="localVisible"
    :title="title || '提示'"
    :show-close="true"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    align-center
    @update:model-value="(v: boolean) => { if (!v) emit('cancel') }"
  >
    <div class="confirm-content">
      <div class="confirm-icon-wrap">
        <el-icon class="confirm-icon"><WarningFilled /></el-icon>
      </div>
      <span class="confirm-text">{{ message || '确认删除？' }}</span>
    </div>
    <template #footer>
      <div class="confirm-footer">
        <el-button v-if="showCancel" class="btn-cancel" @click="handleCancel">取消</el-button>
        <el-button class="btn-confirm" type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.confirm-content {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 8px 8px;
}
.confirm-icon-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.confirm-icon {
  font-size: 28px;
  color: #e6a23c;
}
.confirm-text {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
  line-height: 1.5;
}
.confirm-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 12px;
}
.btn-cancel {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  min-width: 88px;
}
.btn-cancel:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background: #ecf5ff;
}
.btn-confirm {
  background: #1a3a5c;
  border-color: #1a3a5c;
  color: #fff;
  min-width: 88px;
}
.btn-confirm:hover {
  background: #2c5282;
  border-color: #2c5282;
}

:deep(.el-dialog__header) {
  padding: 16px 20px 10px;
  border-bottom: none;
  margin-right: 0;
}
:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
:deep(.el-dialog__body) {
  padding: 8px 20px 4px;
}
:deep(.el-dialog__footer) {
  padding: 10px 20px 18px;
}
</style>
