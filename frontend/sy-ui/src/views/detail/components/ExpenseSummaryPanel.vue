<script setup lang="ts">
import { useReimbursementStore } from '@/stores/reimbursement'
import { formatAmount } from '@/utils/format'

const store = useReimbursementStore()
</script>

<template>
  <div class="expense-summary-panel">
    <div class="summary-grid">
      <div class="summary-item">
        <span class="summary-label">补助总金额</span>
        <span class="summary-value">{{ formatAmount(store.subsidyTotalAmount) }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">餐费补助</span>
        <span class="summary-value">{{ formatAmount(store.mealAllowanceTotal) }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">交通补助</span>
        <span class="summary-value">{{ formatAmount(store.transportationAllowanceTotal) }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">通讯补助</span>
        <span class="summary-value">{{ formatAmount(store.communicationAllowanceTotal) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.summary-item { display: flex; flex-direction: column; gap: 8px; padding: 16px; background: #f5f7fa; border-radius: 4px; text-align: center; }
.summary-label { font-size: 14px; color: #666; }
.summary-value { font-size: 18px; font-weight: 600; color: #333; }
</style>
