<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useDraftStore } from '@/stores/draft'
import { useToastStore } from '@/stores/toast'
import { useIdempotentToken } from '@/composables/useIdempotentToken'
import { pageQuery } from '@/apis/reimbursement'
import { getTripsByReimId } from '@/apis/trip'
import { getSubsidiesByReimId } from '@/apis/subsidy'
import { getExpensesBySubsidyInfoId } from '@/apis/expense'
import { getCostAllocationsByReimId } from '@/apis/costAllocation'
import { generateUUID } from '@/utils/idGenerator'
import { getMealStandard, getCityTypeByCityId, TRANSPORT_ALLOWANCE, COMMUNICATION_ALLOWANCE } from '@/utils/subsidyRules'
import DetailHeader from './components/DetailHeader.vue'
import DetailFooter from './components/DetailFooter.vue'
import BasicInfoPanel from './components/BasicInfoPanel.vue'
import TripPanel from './components/TripPanel.vue'
import SubsidyPanel from './components/SubsidyPanel.vue'
import ExpenseSummaryPanel from './components/ExpenseSummaryPanel.vue'
import CostAllocationPanel from './components/CostAllocationPanel.vue'
import RemarksPanel from './components/RemarksPanel.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const route = useRoute()
const router = useRouter()
const store = useReimbursementStore()
const toastStore = useToastStore()
const draftStore = useDraftStore()
const { token, generate, refresh, destroy } = useIdempotentToken()

const activeNames = ref(['basic', 'trip', 'subsidy', 'expense', 'allocation', 'remarks'])
const tripPanelRef = ref<InstanceType<typeof TripPanel> | null>(null)
const deleteRemarksConfirm = ref(false)

function openTripDialog(): void { tripPanelRef.value?.openAddDialog() }
function confirmDeleteRemarks(): void { store.basicInfo.remarks = ''; store.dirty = true; deleteRemarksConfirm.value = false }

onMounted(async () => {
  const id = route.params.id as string | undefined
  const copyFrom = route.query.copyFrom as string | undefined

  if (copyFrom) {
    await loadCopyData(copyFrom)
    generate()
  } else if (!id) {
    store.initNew()
    generate()
  } else if (id.startsWith('sy_reim_draft_')) {
    try {
      store.initFromDraft(id)
      generate()
    } catch {
      toastStore.error('草稿不存在或已过期')
      router.push('/list')
    }
  } else {
    await loadApiDetail(id)
    generate()
  }

  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

let draftLimitWarned = false

function handleBeforeUnload(_e: BeforeUnloadEvent): void {
  if (store.isDraft && store.dirty) {
    const ok = store.saveAsDraft()
    if (!ok && !draftLimitWarned) {
      draftLimitWarned = true
      toastStore.warning(`草稿数量已达上限（${draftStore.MAX_DRAFTS}个），未保存为草稿，但您仍可直接提交`)
    }
  }
}

let autoSaveTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => {
  autoSaveTimer = setInterval(() => {
    if (store.isDraft && store.dirty && !store.isAudit) {
      const ok = store.saveAsDraft()
      if (!ok) {
        if (!draftLimitWarned) {
          draftLimitWarned = true
          toastStore.warning(`草稿数量已达上限（${draftStore.MAX_DRAFTS}个），未保存为草稿，但您仍可直接提交`)
        }
        if (autoSaveTimer) { clearInterval(autoSaveTimer); autoSaveTimer = null }
      }
    }
  }, 5000)
})
onBeforeUnmount(() => { if (autoSaveTimer) clearInterval(autoSaveTimer) })

async function loadCopyData(source: string): Promise<void> {
  store.initNew()
  store.draftKey = 'sy_reim_draft_' + generateUUID()
  store.isDraft = true

  if (source.startsWith('sy_reim_draft_')) {
    const draft = draftStore.loadDraft(source)
    if (draft) {
      store.basicInfo.title = draft.title || ''
      store.basicInfo.reimburserId = draft.reimburserId || ''
      store.basicInfo.reimburserName = draft.reimburserName || ''
      store.basicInfo.reimDepartmentId = draft.reimDepartmentId || ''
      store.basicInfo.reimDepartmentName = draft.reimDepartmentName || ''
      store.basicInfo.reimCompanyId = draft.reimCompanyId || ''
      store.basicInfo.reimCompanyName = draft.reimCompanyName || ''
      store.basicInfo.businessTypeId = draft.businessTypeId || ''
      store.basicInfo.businessTypeName = draft.businessTypeName || ''
      store.basicInfo.reason = draft.reason || ''
      store.basicInfo.remarks = draft.remarks || ''
    }
    return
  }

  await loadApiDetail(source)
  store.reimId = null
  store.draftKey = 'sy_reim_draft_' + generateUUID()
  store.isDraft = true
  store.isAudit = false
  store.trips.forEach((t) => { t.localId = generateUUID() })
}

async function loadApiDetail(apiId: string): Promise<void> {
  store.initNew()
  store.reimId = apiId
  store.draftKey = null

  try {
    const result = await pageQuery({ current: 1, size: 100 })
    const record = result.records?.find((r: any) => r.id === apiId)
    if (record) {
      store.basicInfo.title = record.title || ''
      store.basicInfo.reimburserId = record.reimburserId || ''
      store.basicInfo.reimburserName = record.reimburserName || ''
      store.basicInfo.reimDepartmentId = record.reimDepartmentId || ''
      store.basicInfo.reimDepartmentName = record.reimDepartmentName || ''
      store.basicInfo.reimCompanyId = record.reimCompanyId || ''
      store.basicInfo.reimCompanyName = record.reimCompanyName || ''
      store.basicInfo.businessTypeId = record.businessTypeId || ''
      store.basicInfo.businessTypeName = record.businessTypeName || ''
      store.basicInfo.reason = record.reason || ''
      store.basicInfo.remarks = record.remarks || ''
      store.isDraft = record.status === 0
      store.isAudit = record.status !== 0
      store.setCreateTime(record.createTime)
    }

    try {
      const tripsData = await getTripsByReimId(apiId)
      if (tripsData && tripsData.length > 0) {
        store.trips = tripsData.map((t: any) => ({
          localId: generateUUID(),
          travelerId: t.travelerId, travelerName: t.travelerName,
          departureCityId: t.departureCityId, departureCityName: t.departureCityName,
          arrivalCityId: t.arrivalCityId, arrivalCityName: t.arrivalCityName,
          departureDate: t.departureDate + ' 00:00:00', arrivalDate: t.arrivalDate + ' 00:00:00',
          description: t.description || '',
        }))
      }
    } catch { /* trips may not exist */ }

    try {
      const subsidiesData = await getSubsidiesByReimId(apiId)
      if (subsidiesData && subsidiesData.length > 0) {
        for (const s of subsidiesData) {
          const trip = store.trips.find((t) => t.travelerId === s.travelerId)
          if (!trip) continue
          let expenseDetails: any[] = []
          try { expenseDetails = await getExpensesBySubsidyInfoId(s.id) } catch { /* empty */ }
          const expenses = expenseDetails.map((e: any) => ({
            localId: generateUUID(), expenseDate: e.expenseDate, week: e.week,
            cityId: e.cityId, cityName: e.cityName,
            mealStandard: getMealStandard(getCityTypeByCityId(e.cityId, store.referenceStore.cities)),
            transportStandard: TRANSPORT_ALLOWANCE, commStandard: COMMUNICATION_ALLOWANCE,
            mealAllowance: e.mealAllowance || 0, transportationAllowance: e.transportationAllowance || 0,
            communicationAllowance: e.communicationAllowance || 0,
            mealChecked: (e.mealAllowance || 0) > 0,
            transportationChecked: (e.transportationAllowance || 0) > 0,
            communicationChecked: (e.communicationAllowance || 0) > 0,
          }))
          const applyAmount = expenses.reduce((sum, e) => sum + e.mealStandard + e.transportStandard + e.commStandard, 0)
          const subsidyAmount = expenses.reduce((sum, e) => sum + e.mealAllowance + e.transportationAllowance + e.communicationAllowance, 0)
          store.subsidies.push({
            id: s.id, reimId: s.reimId, tripId: s.tripId, localTripId: trip.localId,
            localId: generateUUID() as any,
            travelerId: s.travelerId, travelerName: s.travelerName,
            travelStartDate: s.travelStartDate, travelEndDate: s.travelEndDate,
            travelDays: s.travelDays, itinerary: s.itinerary, subsidyCity: s.subsidyCity,
            applyAmount, subsidyAmount, expenses,
          } as any)
        }
        store._updateSubsidyTotal()
      }
    } catch { /* subsidies may not exist */ }

    try {
      const allocationsData = await getCostAllocationsByReimId(apiId)
      if (allocationsData && allocationsData.length > 0) {
        store.allocations = allocationsData.map((a: any, idx: number) => ({
          localId: generateUUID(), costOwnerId: a.costOwnerId, costOwnerName: a.costOwnerName,
          projectId: a.projectId, projectName: a.projectName,
          proportion: a.proportion, amount: a.amount,
          isFirstRow: idx === 0, editable: idx !== 0,
        }))
      }
    } catch { /* allocations may not exist */ }
  } catch (err: any) {
    toastStore.error('加载报销单详情失败: ' + (err.message || '未知错误'))
  }
}

async function handleClose(): Promise<void> {
  if (store.dirty && store.isDraft && !store.isAudit) {
    const ok = store.saveAsDraft()
    if (!ok && !draftLimitWarned) {
      draftLimitWarned = true
      toastStore.warning(`草稿数量已达上限（${draftStore.MAX_DRAFTS}个），未保存为草稿，但您仍可直接提交`)
    }
  }
  router.push('/list')
}

async function handleSubmit(): Promise<void> {
  if (!token.value) generate()
  const result = await store.submit(token.value)
  if (result.success) {
    toastStore.success('提交成功')
    destroy()
    router.push('/list')
  } else {
    toastStore.error(result.error || '提交失败')
    refresh()
  }
}
</script>

<template>
  <div class="detail-page">
    <DetailHeader />

    <div class="detail-body">
      <el-collapse v-model="activeNames">
        <!-- Panel 1: Basic Info -->
        <el-collapse-item title="" name="basic">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">基础信息</span>
            </div>
          </template>
          <BasicInfoPanel />
        </el-collapse-item>

        <!-- Panel 2: Trip -->
        <el-collapse-item title="" name="trip">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">补录行程</span>
              <span v-if="!store.isAudit" class="panel-action-btn" @click.stop>
                <i class="iconfont icon-a-zengjiatianjiajiahaoduo" style="color:#409eff;font-size:14px;cursor:pointer" @click.stop="openTripDialog()"></i>
                <span style="color:#409eff;font-size:14px;cursor:pointer" @click.stop="openTripDialog()">补录行程</span>
              </span>
            </div>
          </template>
          <TripPanel ref="tripPanelRef" />
        </el-collapse-item>

        <!-- Panel 3: Subsidy -->
        <el-collapse-item title="" name="subsidy">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">补助信息</span>
            </div>
          </template>
          <SubsidyPanel />
        </el-collapse-item>

        <!-- Panel 4: Expense Summary -->
        <el-collapse-item title="" name="expense">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">费用合计</span>
            </div>
          </template>
          <ExpenseSummaryPanel />
        </el-collapse-item>

        <!-- Panel 5: Cost Allocation -->
        <el-collapse-item title="" name="allocation">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">费用归属及分摊</span>
              <span class="panel-subtitle">(分摊金额: {{ store._formatAmount(store.subsidyTotalAmount) }})</span>
            </div>
          </template>
          <CostAllocationPanel />
        </el-collapse-item>

        <!-- Panel 6: Remarks -->
        <el-collapse-item title="" name="remarks">
          <template #title>
            <div class="panel-title">
              <span class="title-bar"></span>
              <span class="title-text">备注信息</span>
              <span v-if="!store.isAudit && store.basicInfo.remarks" class="panel-action-btn" @click.stop="deleteRemarksConfirm = true">
                <span style="color:#409eff;font-size:13px;cursor:pointer">删除备注</span>
              </span>
            </div>
          </template>
          <RemarksPanel />
        </el-collapse-item>
      </el-collapse>
    </div>

    <DetailFooter
      :is-audit="store.isAudit"
      :is-submitting="store.isSubmitting"
      @close="handleClose"
      @submit="handleSubmit"
    />

    <!-- Delete Remarks Confirm -->
    <ConfirmDialog
      :visible="deleteRemarksConfirm"
      message="确认删除备注？"
      @confirm="confirmDeleteRemarks"
      @cancel="deleteRemarksConfirm = false"
    />
  </div>
</template>

<style scoped>
.detail-page {
  width: 1200px; margin: 0 auto; background: #fff; min-height: 100vh; padding-bottom: 80px;
}
.detail-body { padding: 20px; }
.panel-title {
  display: flex; align-items: center; height: 36px; width: 100%;
  background: #f5f7fa; padding: 0 16px; user-select: none; gap: 8px;
}
.title-bar { display: inline-block; width: 3px; height: 16px; background: #409eff; border-radius: 2px; flex-shrink: 0; }
.title-text { font-size: 16px; font-weight: 600; color: #333; }
.panel-subtitle { font-size: 12px; color: #999; font-weight: 400; }
.panel-action-btn { margin-left: auto; display: flex; align-items: center; gap: 8px; }

:deep(.el-collapse-item__header) { height: 36px; background: #f5f7fa; border: none; padding: 0; line-height: 36px; }
:deep(.el-collapse-item__wrap) { border: none; }
:deep(.el-collapse-item__content) { padding: 16px; }
</style>
