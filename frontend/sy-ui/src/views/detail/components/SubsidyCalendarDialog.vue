<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { SubsidyInfoLocal } from '@/types/subsidy'
import type { ExpenseCalendarDay } from '@/types/expense'
import { useSubsidyCalendar } from '@/composables/useSubsidyCalendar'
import { formatAmount, formatDate, getWeekDayName } from '@/utils/format'
import { useReferenceStore } from '@/stores/reference'

const props = defineProps<{
  visible: boolean
  subsidy: SubsidyInfoLocal | null
  businessTypeName?: string
}>()

const emit = defineEmits<{
  save: [expenses: ExpenseCalendarDay[]]
  cancel: []
}>()

const refStore = useReferenceStore()

const effectiveBusinessTypeName = computed(
  () => props.businessTypeName || '日常办公',
)

const {
  days,
  toggleAll,
  toggleType,
  toggleDay,
  toggleSingle,
  updateAllowance,
  standardTotal,
  subsidyTotal,
  isAllChecked,
  isRowChecked,
  isColChecked,
} = useSubsidyCalendar([])

watch(
  () => [props.visible, props.subsidy],
  () => {
    if (props.visible && props.subsidy) {
      days.value = props.subsidy.expenses.map((e) => ({ ...e }))
    }
  },
  { immediate: true },
)

function getCityDisplay(cityId: string, cityName?: string): string {
  if (cityName) return cityName
  if (!cityId) return '-'
  const byNo = refStore.getCityByNo(cityId)
  if (byNo) return byNo.cityName
  const found = refStore.cities.find((c) => c.cityName === cityId)
  if (found) return found.cityName
  return cityId
}

const startCity = computed(() => {
  if (!props.subsidy) return ''
  const parts = props.subsidy.itinerary?.split('-') || []
  return (parts[0] || '').trim()
})

const endCity = computed(() => {
  if (!props.subsidy) return ''
  const parts = props.subsidy.itinerary?.split('-') || []
  return (parts[1] || '').trim() || getCityDisplay(props.subsidy.subsidyCity)
})

const startDateText = computed(() => {
  if (!props.subsidy) return ''
  return formatDate(props.subsidy.travelStartDate)
})

const endDateText = computed(() => {
  if (!props.subsidy) return ''
  return formatDate(props.subsidy.travelEndDate)
})

const travelDaysText = computed(() => {
  if (!props.subsidy) return ''
  const s = startCity.value || endCity.value || ''
  const e = endCity.value || startCity.value || ''
  return `${s} - ${e}`
})

const travelDaysCount = computed(() => props.subsidy?.travelDays ?? 0)

const mealPrice = computed(() => days.value[0]?.mealStandard ?? 0)
const transportPrice = computed(() => days.value[0]?.transportStandard ?? 0)
const commPrice = computed(() => days.value[0]?.commStandard ?? 0)

function onAllowanceInput(dayIdx: number, type: 'meal' | 'transportation' | 'communication', raw: string | number): void {
  const num = typeof raw === 'string' ? Number(raw.replace(/[^\d.]/g, '')) : raw
  if (isNaN(num)) return
  updateAllowance(dayIdx, type, num)
}

function handleSave(): void {
  emit('save', days.value.map((d) => ({ ...d })))
}

function handleCancel(): void {
  emit('cancel')
}

</script>

<template>
  <el-dialog
    :model-value="visible"
    title="补助日历"
    width="980px"
    :close-on-click-modal="false"
    :show-close="false"
    class="subsidy-calendar-dialog"
    @close="handleCancel"
  >
    <template #header>
      <div class="dialog-header">
        <span class="dialog-title">补助日历</span>
        <span class="dialog-close" @click="handleCancel">×</span>
      </div>
    </template>

    <div class="calendar-layout" v-if="subsidy">
      <!-- Left Pane -->
      <div class="calendar-left">
        <!-- Module 1: Business Type -->
        <div class="left-module biz-type-row">
          <span class="module-label-inline">出差类型</span>
          <span class="type-tab active">{{ effectiveBusinessTypeName }}</span>
        </div>

        <!-- Module 2: Timeline -->
        <div class="left-module">
          <div class="timeline">
            <div class="tl-row">
              <span class="tl-dot tl-dot-blue"></span>
              <div class="tl-content">
                <div class="tl-label">开始日期</div>
                <div class="tl-value">{{ startDateText }}</div>
              </div>
            </div>

            <div class="tl-line"></div>

            <div class="tl-row tl-row-highlight">
              <span class="tl-dot tl-dot-blue tl-dot-inline"></span>
              <div class="tl-content tl-content-highlight">
                <div class="tl-highlight-left">
                  <span class="tl-label tl-label-white">行程天数</span>
                </div>
                <span class="tl-value tl-value-cities">{{ travelDaysText }}</span>
                <span class="tl-days-badge">{{ travelDaysCount }}天</span>
              </div>
            </div>

            <div class="tl-line tl-line-bottom"></div>

            <div class="tl-row">
              <span class="tl-dot tl-dot-blue"></span>
              <div class="tl-content">
                <div class="tl-label">结束日期</div>
                <div class="tl-value">{{ endDateText }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Module 3: Amount Summary -->
        <div class="left-module amount-module">
          <div class="amount-row">
            <span class="amount-label">补助金额</span>
            <span class="amount-cny">CNY</span>
            <span class="amount-value amount-value-orange">{{ formatAmount(subsidyTotal) }}</span>
          </div>
          <div class="amount-row">
            <span class="amount-label">标准总额</span>
            <span class="amount-cny">CNY</span>
            <span class="amount-value">{{ formatAmount(standardTotal) }}</span>
          </div>
          <div class="amount-row">
            <span class="amount-label">补助金额</span>
            <span class="amount-cny">CNY</span>
            <span class="amount-value amount-value-orange">{{ formatAmount(subsidyTotal) }}</span>
          </div>
        </div>
      </div>

      <!-- Right Pane: Horizontal Table (rows = dates, cols = allowance types) -->
      <div class="calendar-right">
        <!-- Right Title Bar -->
        <div class="right-title-bar">
          <span class="right-title">出差补助</span>
          <div class="right-title-actions">
            <el-checkbox :model-value="isAllChecked" size="small" @click.stop @change="toggleAll" />
            <span class="col-all-text">全选</span>
          </div>
        </div>

        <div class="table-scroll-wrap">
          <div class="table-wrapper">
            <!-- Header Row -->
            <div class="table-row table-header">
              <div class="cell cell-col-date">出差日期</div>
              <div class="cell cell-col-city">补助城市</div>
              <div class="cell cell-col-allowance">
                <div class="col-title-row">
                  <span class="col-title">餐费补助</span>
                  <el-checkbox :model-value="isRowChecked('meal')" size="small" @click.stop @change="toggleType('meal')" />
                </div>
                <div class="col-price">CNY {{ formatAmount(mealPrice) }} / 天</div>
              </div>
              <div class="cell cell-col-allowance">
                <div class="col-title-row">
                  <span class="col-title">交通补助</span>
                  <el-checkbox :model-value="isRowChecked('transportation')" size="small" @click.stop @change="toggleType('transportation')" />
                </div>
                <div class="col-price">CNY {{ formatAmount(transportPrice) }} / 天</div>
              </div>
              <div class="cell cell-col-allowance">
                <div class="col-title-row">
                  <span class="col-title">通讯补助</span>
                  <el-checkbox :model-value="isRowChecked('communication')" size="small" @click.stop @change="toggleType('communication')" />
                </div>
                <div class="col-price">CNY {{ formatAmount(commPrice) }} / 天</div>
              </div>
            </div>

            <!-- Data Rows -->
            <div
              v-for="(day, di) in days"
              :key="'row-' + day.localId"
              class="table-row table-body"
            >
              <div class="cell cell-row-date">
                <span class="date-pin">
                  <svg width="10" height="14" viewBox="0 0 10 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M5 0C2.24 0 0 2.24 0 5c0 3.75 5 9 5 9s5-5.25 5-9c0-2.76-2.24-5-5-5zm0 6.75c-.97 0-1.75-.78-1.75-1.75S4.03 3.25 5 3.25s1.75.78 1.75 1.75S5.97 6.75 5 6.75z" fill="#ff8a1f"/>
                  </svg>
                </span>
                <div class="date-texts">
                  <div class="date-main">{{ day.expenseDate }}</div>
                  <div class="date-week">{{ getWeekDayName(day.week) }}</div>
                </div>
              </div>
              <div class="cell cell-row-city">{{ getCityDisplay(day.cityId, day.cityName) }}</div>
              <div class="cell cell-row-allowance">
                <div class="allowance-price">CNY {{ formatAmount(day.mealStandard) }} / 天</div>
                <div class="allowance-controls">
                  <el-checkbox
                    :model-value="day.mealChecked"
                    size="small"
                    @click.stop
                    @change="toggleSingle(di, 'meal')"
                  />
                  <el-input
                    :model-value="day.mealAllowance"
                    size="small"
                    :disabled="!day.mealChecked"
                    class="allowance-input"
                    @blur="onAllowanceInput(di, 'meal', day.mealAllowance)"
                  />
                </div>
              </div>
              <div class="cell cell-row-allowance">
                <div class="allowance-price">CNY {{ formatAmount(day.transportStandard) }} / 天</div>
                <div class="allowance-controls">
                  <el-checkbox
                    :model-value="day.transportationChecked"
                    size="small"
                    @click.stop
                    @change="toggleSingle(di, 'transportation')"
                  />
                  <el-input
                    :model-value="day.transportationAllowance"
                    size="small"
                    :disabled="!day.transportationChecked"
                    class="allowance-input"
                    @blur="onAllowanceInput(di, 'transportation', day.transportationAllowance)"
                  />
                </div>
              </div>
              <div class="cell cell-row-allowance">
                <div class="allowance-price">CNY {{ formatAmount(day.commStandard) }} / 天</div>
                <div class="allowance-controls">
                  <el-checkbox
                    :model-value="day.communicationChecked"
                    size="small"
                    @click.stop
                    @change="toggleSingle(di, 'communication')"
                  />
                  <el-input
                    :model-value="day.communicationAllowance"
                    size="small"
                    :disabled="!day.communicationChecked"
                    class="allowance-input"
                    @blur="onAllowanceInput(di, 'communication', day.communicationAllowance)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-cancel" @click="handleCancel">取消</el-button>
        <el-button class="btn-confirm" type="primary" @click="handleSave">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 4px;
}
.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}
.dialog-close {
  width: 24px;
  height: 24px;
  line-height: 22px;
  text-align: center;
  font-size: 20px;
  color: #9ca3af;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  cursor: pointer;
  font-family: Arial, sans-serif;
  transition: all 0.15s;
  padding: 0;
  user-select: none;
}
.dialog-close:hover {
  color: #409eff;
  border-color: #409eff;
}

.calendar-layout {
  display: flex;
  gap: 20px;
  min-height: 300px;
  max-height: 560px;
  overflow: hidden;
}

.calendar-left {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  padding-right: 4px;
}

.left-module {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
}

.module-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 10px;
}

.biz-type-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-label-inline {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.type-display {
  display: flex;
  gap: 8px;
}
.type-tab {
  padding: 4px 12px;
  font-size: 13px;
  border-radius: 4px;
  background: #f5f7fa;
  color: #606266;
  transition: all 0.2s;
}
.type-tab.active {
  background: transparent;
  color: #ff8a1f;
  font-weight: 500;
  padding: 4px 0;
}

.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.tl-row {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: 4px 0;
}

.tl-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
  margin-top: 2px;
  margin-right: 14px;
  box-shadow: 0 0 0 3px #e8f1ff;
}

.tl-dot-inline {
  margin-top: 10px;
}

.tl-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 2px 0;
}

.tl-content-highlight {
  flex-direction: row;
  align-items: center;
  gap: 10px;
  background: #e8f1ff;
  margin: 8px -16px 8px -30px;
  padding: 10px 16px 10px 30px;
  border-radius: 4px;
}

.tl-highlight-left {
  display: flex;
  flex-direction: column;
}

.tl-label {
  font-size: 12px;
  color: #909399;
}

.tl-label-white {
  color: #1f2937;
  font-weight: 500;
}

.tl-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.tl-value-cities {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  flex: 1;
}

.tl-days-badge {
  background: #ff8a1f;
  color: #fff;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.tl-line {
  width: 2px;
  height: 14px;
  background: #409eff;
  margin-left: 4px;
}
.tl-line-bottom {
  height: 12px;
}

.amount-module {
  margin-top: auto;
}
.amount-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f2f5;
  font-size: 13px;
}
.amount-row:last-child {
  border-bottom: none;
}
.amount-label {
  color: #606266;
  width: 72px;
}
.amount-cny {
  color: #909399;
  font-size: 12px;
}
.amount-value {
  margin-left: auto;
  color: #303133;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}
.amount-value-orange {
  color: #ff8a1f;
  font-weight: 700;
}

/* ---------- Right Pane: Horizontal Table ---------- */
.calendar-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.right-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fafcff;
  border: 1px solid #ebeef5;
  border-radius: 6px 6px 0 0;
  border-bottom: none;
}
.right-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}
.right-title-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.table-scroll-wrap {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  max-height: 480px;
}

.table-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 0 0 6px 6px;
  overflow: hidden;
  background: #fff;
}

.table-row {
  display: grid;
  grid-template-columns: 120px 110px 1fr 1fr 1fr;
  min-height: 80px;
  border-bottom: 1px solid #ebeef5;
}

.table-row:last-child {
  border-bottom: none;
}

.cell {
  padding: 10px 8px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: #303133;
  background: #fff;
  min-width: 0;
}

.cell:last-child {
  border-right: none;
}

.table-header .cell {
  background: #fafcff;
  font-weight: 600;
  color: #303133;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 8px 6px;
  gap: 4px;
}

.cell-col-date {
  align-items: center;
  justify-content: center;
}

.cell-col-city {
  align-items: center;
  justify-content: center;
}

.cell-col-allowance {
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.col-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.col-title {
  font-size: 13px;
  color: #303133;
  font-weight: 600;
}

.col-price {
  font-size: 11px;
  color: #ff8a1f;
  font-weight: 500;
}

.col-all-text {
  font-size: 12px;
  color: #606266;
}

/* Body cells */
.cell-row-date {
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  padding: 8px 10px;
}

.date-pin {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  padding-top: 2px;
}

.date-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date-main {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.date-week {
  font-size: 11px;
  color: #909399;
}

.cell-row-city {
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.cell-row-allowance {
  align-items: stretch;
  justify-content: center;
  gap: 4px;
  padding: 8px 6px;
}

.allowance-price {
  font-size: 11px;
  color: #ff8a1f;
  font-weight: 500;
  text-align: center;
}

.allowance-controls {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
}

.allowance-input {
  width: 80px;
}

.allowance-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 4px;
  height: 26px;
}

.allowance-input :deep(.el-input__inner) {
  height: 26px;
  line-height: 26px;
  font-size: 13px;
}

/* ---------- Footer ---------- */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-cancel {
  background: #fff;
  border: 1px solid #409eff;
  color: #409eff;
  min-width: 88px;
}
.btn-cancel:hover {
  background: #ecf5ff;
}
.btn-confirm {
  background: #1a3a5c;
  border-color: #1a3a5c;
  min-width: 88px;
}
.btn-confirm:hover {
  background: #2c5282;
  border-color: #2c5282;
}
</style>
