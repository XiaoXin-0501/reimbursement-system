<script setup lang="ts">
import { ref } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useReferenceStore } from '@/stores/reference'
import { formatAmount, formatDate } from '@/utils/format'
import type { SubsidyInfoLocal } from '@/types/subsidy'
import type { ExpenseCalendarDay } from '@/types/expense'
import SubsidyCalendarDialog from './SubsidyCalendarDialog.vue'

const store = useReimbursementStore()
const refStore = useReferenceStore()

const calendarVisible = ref(false)
const editingSubsidy = ref<SubsidyInfoLocal | null>(null)

function openCalendar(sub: SubsidyInfoLocal): void {
  editingSubsidy.value = sub
  calendarVisible.value = true
}
function handleCalendarSave(expenses: ExpenseCalendarDay[]): void {
  if (editingSubsidy.value) {
    store.updateSubsidyExpenses((editingSubsidy.value as any).localId, expenses)
  }
  calendarVisible.value = false
  editingSubsidy.value = null
}
function getCityName(no: string): string {
  if (!no) return '-'
  if (!refStore.cities || refStore.cities.length === 0) return no
  const byNo = refStore.getCityByNo(no)
  if (byNo) return byNo.cityName
  const found = refStore.cities.find((c) => c.cityName === no)
  if (found) return found.cityName
  return no
}
function getEmployeeName(id: string, name?: string): string {
  if (name) return name
  return refStore.getEmployeeById(id)?.reimburserName || id
}
</script>

<template>
  <div class="subsidy-panel">
    <el-alert
      title="补助规则：餐费补助按城市等级（一线城市100元/天、二线城市80元/天、三线城市50元/天），交通补助统一40元/天，通讯补助统一40元/天。补助城市按到达城市核算。"
      type="warning" :closable="false" show-icon style="margin-bottom:12px"
    />

    <el-table :data="store.subsidies" border size="default" style="width:100%">
      <el-table-column label="序号" width="50" align="center">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column label="出行人" width="80">
        <template #default="{ row }">{{ getEmployeeName(row.travelerId, row.travelerName) }}</template>
      </el-table-column>
      <el-table-column label="出差时间" width="340">
        <template #default="{ row }">{{ formatDate(row.travelStartDate) }} 至 {{ formatDate(row.travelEndDate) }}</template>
      </el-table-column>
      <el-table-column label="补助天数" width="80" align="center">
        <template #default="{ row }">{{ row.travelDays }}</template>
      </el-table-column>
      <el-table-column label="行程" min-width="120">
        <template #default="{ row }">{{ row.itinerary || (row.itinerary === '' ? '' : '-') }}</template>
      </el-table-column>
      <el-table-column label="补助城市" width="80">
        <template #default="{ row }">{{ getCityName(row.subsidyCity) }}</template>
      </el-table-column>
      <el-table-column label="申请金额" width="110" align="right">
        <template #default="{ row }">{{ formatAmount(row.applyAmount) }}</template>
      </el-table-column>
      <el-table-column label="补助金额" width="110" align="right">
        <template #default="{ row }">{{ formatAmount(row.subsidyAmount) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="70" align="center" v-if="!store.isAudit">
        <template #default="{ row }">
          <i class="iconfont icon-xiugai action-icon" title="编辑" @click="openCalendar(row as SubsidyInfoLocal)"></i>
        </template>
      </el-table-column>
    </el-table>

    <SubsidyCalendarDialog
      :visible="calendarVisible"
      :subsidy="editingSubsidy"
      :business-type-name="store.basicInfo.businessTypeName"
      @save="handleCalendarSave"
      @cancel="calendarVisible = false; editingSubsidy = null"
    />
  </div>
</template>

<style scoped>
.subsidy-panel { padding: 0; }
.action-icon { font-size: 18px; color: #409eff; cursor: pointer; }
.action-icon:hover { opacity: 0.7; }
</style>
