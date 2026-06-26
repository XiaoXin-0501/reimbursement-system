<script setup lang="ts">
import { computed } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import { formatDate } from '@/utils/format'

const store = useReimbursementStore()

const displayDate = computed(() => {
  const t = store.createTime
  return t ? formatDate(t) : ''
})
</script>

<template>
  <div class="detail-header">
    <div class="header-placeholder"></div>
    <h1 class="header-title">差旅费费用报销单</h1>
    <span class="header-date">提单日 {{ displayDate }}</span>
  </div>
</template>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-placeholder {
  width: 150px;
}
.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0;
  text-align: center;
  flex: 1;
}
.header-date {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  width: 150px;
  text-align: right;
}
</style>
