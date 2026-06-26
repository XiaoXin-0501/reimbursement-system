<script setup lang="ts">
import { useReimbursementStore } from '@/stores/reimbursement'

const store = useReimbursementStore()
</script>

<template>
  <div class="remarks-panel">
    <el-input
      :model-value="store.basicInfo.remarks"
      @update:model-value="store.updateBasicField('remarks', $event)"
      type="textarea"
      placeholder="请输入"
      maxlength="1000"
      show-word-limit
      :rows="5"
      :disabled="store.isAudit"
    />
  </div>
</template>

<style scoped>
.remarks-panel { padding: 0; }
</style>
