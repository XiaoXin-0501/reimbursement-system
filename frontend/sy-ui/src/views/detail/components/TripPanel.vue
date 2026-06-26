<script setup lang="ts">
import { ref } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useReferenceStore } from '@/stores/reference'
import { formatDate } from '@/utils/format'
import type { TripFormData } from '@/types/trip'
import TripDialog from './TripDialog.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const store = useReimbursementStore()
const refStore = useReferenceStore()

const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit' | 'copy'>('add')
const editingTrip = ref<TripFormData | null>(null)
const deleteConfirmVisible = ref(false)
const deleteTargetId = ref('')

function openAddDialog(): void {
  dialogMode.value = 'add'
  editingTrip.value = null
  dialogVisible.value = true
}
function openEditDialog(trip: TripFormData): void {
  dialogMode.value = 'edit'
  editingTrip.value = { ...trip }
  dialogVisible.value = true
}
function openCopyDialog(trip: TripFormData): void {
  const copied = store.copyTrip(trip.localId)
  if (copied) {
    dialogMode.value = 'copy'
    editingTrip.value = copied
    dialogVisible.value = true
  }
}
function handleDelete(localId: string): void {
  deleteTargetId.value = localId
  deleteConfirmVisible.value = true
}
function confirmDelete(): void {
  store.deleteTrip(deleteTargetId.value)
  deleteConfirmVisible.value = false
}
function handleDialogSave(trip: TripFormData): void {
  store.saveTrip(trip)
  dialogVisible.value = false
}
function getEmployeeName(id: string): string {
  return refStore.getEmployeeById(id)?.reimburserName || id
}
function getEmployeeNo(id: string): string {
  return refStore.getEmployeeById(id)?.reimburserNo || ''
}
function getCityName(id: string, name?: string): string {
  if (name) return name
  return refStore.getCityByNo(id)?.cityName || id
}
defineExpose({ openAddDialog })
</script>

<template>
  <div class="trip-panel">
    <el-table :data="store.trips" border size="default" style="width: 100%">
      <el-table-column label="序号" width="60" align="center">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column label="出行人员" width="160">
        <template #default="{ row }">
          {{ getEmployeeName(row.travelerId) || row.travelerName }}/{{ getEmployeeNo(row.travelerId) }}
        </template>
      </el-table-column>
      <el-table-column label="出差时间" width="340">
        <template #default="{ row }">
          {{ formatDate(row.departureDate) }} 至 {{ formatDate(row.arrivalDate) }}
        </template>
      </el-table-column>
      <el-table-column label="行程" width="140">
        <template #default="{ row }">{{ getCityName(row.departureCityId, row.departureCityName) }}-{{ getCityName(row.arrivalCityId, row.arrivalCityName) }}</template>
      </el-table-column>
      <el-table-column label="行程说明" min-width="200">
        <template #default="{ row }">{{ row.description }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" v-if="!store.isAudit">
        <template #default="{ row }">
          <span class="action-icons">
            <i class="iconfont icon-xiugai action-icon" title="编辑" @click="openEditDialog(row)"></i>
            <i class="iconfont icon-shanchu action-icon" title="删除" @click="handleDelete(row.localId)"></i>
            <i class="iconfont icon-fuzhi action-icon" title="复制" @click="openCopyDialog(row)"></i>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <TripDialog
      :visible="dialogVisible"
      :mode="dialogMode"
      :trip-data="editingTrip"
      @save="handleDialogSave"
      @cancel="dialogVisible = false"
    />

    <ConfirmDialog
      :visible="deleteConfirmVisible"
      message="确认删除？"
      @confirm="confirmDelete"
      @cancel="deleteConfirmVisible = false"
    />
  </div>
</template>

<style scoped>
.trip-panel { padding: 0; }
.action-icons { display: flex; gap: 10px; justify-content: center; align-items: center; }
.action-icons .action-icon { font-size: 18px; color: #409eff; cursor: pointer; }
.action-icons .action-icon:hover { opacity: 0.7; }
</style>
