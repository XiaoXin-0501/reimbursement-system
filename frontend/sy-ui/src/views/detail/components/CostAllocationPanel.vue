<script setup lang="ts">
import { ref, computed } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useReferenceStore } from '@/stores/reference'
import { useToastStore } from '@/stores/toast'
import { formatAmount } from '@/utils/format'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const store = useReimbursementStore()
const refStore = useReferenceStore()
const toastStore = useToastStore()

const deleteConfirmVisible = ref(false)
const deleteTargetId = ref('')

function addRow(): void { store.addAllocationRow() }
function removeRow(localId: string): void {
  if (store.allocations.length <= 1) { toastStore.warning('至少保留一条分摊信息'); return }
  deleteTargetId.value = localId
  deleteConfirmVisible.value = true
}
function confirmDelete(): void {
  store.removeAllocationRow(deleteTargetId.value)
  deleteConfirmVisible.value = false
}
function equalSplit(): void { store.equalSplitAllocations() }
function getProportionDisplay(proportion: number): string { return (proportion * 100).toFixed(2) + '%' }

function handleCostOwnerChange(rowLocalId: string, val: string): void {
  const row = store.allocations.find((a) => a.localId === rowLocalId)
  if (!row) return
  const company = refStore.getCompanyById(val)
  if (company) { row.costOwnerName = company.reimCompanyName; row.costOwnerId = company.reimCompanyId; return }
  const dept = refStore.getDepartmentById(val)
  if (dept) { row.costOwnerName = dept.reimDepartmentName; row.costOwnerId = dept.reimDepartmentId }
}
function handleProjectChange(rowLocalId: string, val: string): void {
  const row = store.allocations.find((a) => a.localId === rowLocalId)
  if (!row) return
  const p = refStore.getProjectById(val)
  if (p) { row.projectName = p.projectName; row.projectId = p.projectId }
}

const costOwnerOptions = computed(() => [...refStore.companyOptions, ...refStore.departmentOptions])
const proportionSum = computed(() => store.allocations.reduce((s, r) => s + r.proportion, 0))
const amountSum = computed(() => store.allocations.reduce((s, r) => s + r.amount, 0))

function onProportionInput(row: any, val: string): void {
  const num = parseFloat(val)
  if (isNaN(num) || num < 0 || num > 100) return
  store.updateAllocationProportion(row.localId, num / 100)
}
function onAmountInput(row: any, val: string): void {
  const num = parseFloat(val)
  if (isNaN(num) || num < 0) return
  store.updateAllocationAmount(row.localId, num)
}

defineExpose({ addRow, equalSplit })
</script>

<template>
  <div class="allocation-panel">
    <el-table :data="store.allocations" border size="default" style="width: 100%">
      <el-table-column label="序号" width="55" align="center">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column label="费用归属 *" min-width="220">
        <template #default="{ row }">
          <el-select
            :model-value="row.costOwnerId"
            @update:model-value="handleCostOwnerChange(row.localId, $event)"
            placeholder="请选择" size="default"
            :disabled="store.isAudit || (!row.editable && store.allocations.length > 1)"
            style="width:100%"
          >
            <el-option v-for="opt in costOwnerOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="项目 *" min-width="180">
        <template #default="{ row }">
          <el-select
            :model-value="row.projectId"
            @update:model-value="handleProjectChange(row.localId, $event)"
            placeholder="请选择" size="default" :disabled="store.isAudit"
            style="width:100%"
          >
            <el-option v-for="p in refStore.projectOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column width="170" align="right" v-if="!store.isAudit">
        <template #header>
          <div class="col-header">
            <span class="col-header-label">分摊比例 *</span>
            <el-tooltip content="均摊" placement="top" :show-delay="300">
              <i class="iconfont icon-BAI-xunhuan col-header-icon" @click="equalSplit"></i>
            </el-tooltip>
          </div>
        </template>
        <template #default="{ row }">
          <el-input
            :model-value="row.proportion > 0 ? (row.proportion * 100).toFixed(2) : ''"
            @update:model-value="onProportionInput(row, $event)"
            size="default" placeholder="0.00"
            style="width:120px"
          >
            <template #suffix>%</template>
          </el-input>
        </template>
      </el-table-column>
      <el-table-column label="分摊金额 *" width="140" align="right">
        <template #default="{ row }">
          <el-input
            v-if="row.editable && !store.isAudit"
            :model-value="row.amount > 0 ? formatAmount(row.amount) : ''"
            @update:model-value="onAmountInput(row, $event)"
            size="default" placeholder="0.00"
            style="width:110px"
          />
          <span v-else>{{ formatAmount(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70" align="center" v-if="!store.isAudit">
        <template #default="{ row }">
          <i class="iconfont icon-shanchu action-icon" title="删除" @click="removeRow(row.localId)"></i>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add Row Row -->
    <div class="allocation-add-row" v-if="!store.isAudit">
      <span class="add-row-btn" @click="addRow">
        <i class="iconfont icon-a-zengjiatianjiajiahaoduo"></i> 添加一行
      </span>
    </div>

    <!-- Totals Row -->
    <div class="allocation-total-row">
      <span class="total-label">合计</span>
      <span class="total-proportion">{{ getProportionDisplay(proportionSum) }}</span>
      <span class="total-amount">CNY {{ formatAmount(amountSum) }}</span>
    </div>

    <ConfirmDialog :visible="deleteConfirmVisible" message="确认删除？" @confirm="confirmDelete" @cancel="deleteConfirmVisible = false" />
  </div>
</template>

<style scoped>
.allocation-panel { padding: 0; }

.col-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding-right: 12px;
}
.col-header-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}
.col-header-icon {
  font-size: 16px;
  color: #409eff;
  cursor: pointer;
  transition: color 0.2s;
}
.col-header-icon:hover {
  color: #66b1ff;
}

.allocation-add-row {
  display: flex;
  justify-content: center;
  padding: 10px 12px;
  background: #fafcff;
  border-left: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px dashed #ebeef5;
}
.add-row-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}
.add-row-btn:hover {
  background: #ecf5ff;
}
.add-row-btn .iconfont {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
}

.allocation-total-row {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 10px 16px;
  background: #fdf6ec;
  border: 1px solid #ebeef5;
  border-top: none;
  font-size: 14px;
}
.total-label { color: #666; margin-right: auto; }
.total-proportion { color: #e6a23c; font-weight: 600; }
.total-amount { color: #e6a23c; font-weight: 600; }

.action-icon { font-size: 18px; color: #409eff; cursor: pointer; }
.action-icon:hover { opacity: 0.7; }

:deep(.el-table th.el-table__cell) {
  background: #fafcff;
}
</style>
