import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { ReimStatus } from '@/types/common'
import type { Reimbursement, ReimbursementListRow, ReimbursementBasicInfo } from '@/types/reimbursement'
import type { TripFormData } from '@/types/trip'
import type { SubsidyInfoLocal } from '@/types/subsidy'
import type { ExpenseCalendarDay } from '@/types/expense'
import type { AllocationFormRow } from '@/types/costAllocation'
import { useReferenceStore } from './reference'
import { useDraftStore } from './draft'
import { generateUUID } from '@/utils/idGenerator'
import { formatDateTime, today, daysBetween, dateRange, getWeekDay, formatAmount } from '@/utils/format'
import { getMealStandard, getCityTypeByCityId, TRANSPORT_ALLOWANCE, COMMUNICATION_ALLOWANCE } from '@/utils/subsidyRules'
import { createReimbursement, type CreateReimbursementParams } from '@/apis/reimbursement'

export const useReimbursementStore = defineStore('reimbursement', () => {
  const referenceStore = useReferenceStore()
  const draftStore = useDraftStore()

  // ===== Core State =====
  const reimId = ref<string | null>(null)
  const draftKey = ref<string | null>(null)
  const isDraft = ref(true)
  const isAudit = ref(false)
  const isSubmitting = ref(false)
  const dirty = ref(false)
  const draftBlocked = ref(false) // true when saveAsDraft rejects due to MAX_DRAFTS

  const basicInfo = ref<ReimbursementBasicInfo>({
    title: '', reimburserId: '', reimburserName: '',
    reimDepartmentId: '', reimDepartmentName: '',
    reimCompanyId: '', reimCompanyName: '',
    businessTypeId: '', businessTypeName: '',
    reason: '', remarks: '',
  })

  const createTime = ref<string>(today())

  const trips = ref<TripFormData[]>([])
  const subsidies = ref<SubsidyInfoLocal[]>([])
  const allocations = ref<AllocationFormRow[]>([])
  const totalSubsidyAmount = ref(0)

  // ===== Computed =====
  const mealAllowanceTotal = computed(() => {
    let total = 0
    for (const sub of subsidies.value) {
      for (const exp of sub.expenses) {
        if (exp.mealChecked) total += exp.mealAllowance
      }
    }
    return total
  })
  const transportationAllowanceTotal = computed(() => {
    let total = 0
    for (const sub of subsidies.value) {
      for (const exp of sub.expenses) {
        if (exp.transportationChecked) total += exp.transportationAllowance
      }
    }
    return total
  })
  const communicationAllowanceTotal = computed(() => {
    let total = 0
    for (const sub of subsidies.value) {
      for (const exp of sub.expenses) {
        if (exp.communicationChecked) total += exp.communicationAllowance
      }
    }
    return total
  })
  const subsidyTotalAmount = computed(() =>
    mealAllowanceTotal.value + transportationAllowanceTotal.value + communicationAllowanceTotal.value,
  )

  watch(
    [basicInfo, trips, subsidies, allocations],
    () => { if (isDraft.value && !isAudit.value) dirty.value = true },
    { deep: true },
  )

  // ===== Init =====
  function initNew(): void {
    reimId.value = null
    draftKey.value = 'sy_reim_draft_' + generateUUID()
    isDraft.value = true; isAudit.value = false; isSubmitting.value = false; dirty.value = false
    basicInfo.value = { title: '', reimburserId: '', reimburserName: '', reimDepartmentId: '', reimDepartmentName: '', reimCompanyId: '', reimCompanyName: '', businessTypeId: '', businessTypeName: '', reason: '', remarks: '' }
    trips.value = []
    subsidies.value = []
    allocations.value = _createDefaultAllocations()
    totalSubsidyAmount.value = 0
    createTime.value = today()
  }

  function initFromDraft(draftKeyParam: string): void {
    const draft = draftStore.loadDraft(draftKeyParam)
    if (!draft) throw new Error('草稿不存在或已过期')
    reimId.value = draft.id || null
    draftKey.value = draftKeyParam
    isDraft.value = true; isAudit.value = false; isSubmitting.value = false; dirty.value = false
    basicInfo.value = {
      title: draft.title || '', reimburserId: draft.reimburserId || '', reimburserName: draft.reimburserName || '',
      reimDepartmentId: draft.reimDepartmentId || '', reimDepartmentName: draft.reimDepartmentName || '',
      reimCompanyId: draft.reimCompanyId || '', reimCompanyName: draft.reimCompanyName || '',
      businessTypeId: draft.businessTypeId || '', businessTypeName: draft.businessTypeName || '',
      reason: draft.reason || '', remarks: draft.remarks || '',
    }
    createTime.value = draft.createTime || today()
  }

  // ===== Trip Management =====
  function saveTrip(trip: TripFormData): void {
    const idx = trips.value.findIndex((t) => t.localId === trip.localId)
    if (idx >= 0) trips.value[idx] = { ...trip }
    else trips.value.push({ ...trip })
    _regenerateSubsidyForTrip(trip.localId)
    _updateSubsidyTotal()
    _recalculateAllocations()
    dirty.value = true
  }

  function deleteTrip(localId: string): void {
    trips.value = trips.value.filter((t) => t.localId !== localId)
    subsidies.value = subsidies.value.filter((s) => s.localTripId !== localId)
    _updateSubsidyTotal()
    _recalculateAllocations()
    dirty.value = true
  }

  function copyTrip(localId: string): TripFormData | null {
    const trip = trips.value.find((t) => t.localId === localId)
    if (!trip) return null
    return { ...trip, localId: generateUUID() }
  }

  // ===== Subsidy =====
  function updateSubsidyExpenses(subLocalId: string, expenses: ExpenseCalendarDay[]): void {
    const sub = subsidies.value.find((s) => (s as any).localId === subLocalId)
    if (sub) {
      sub.expenses = expenses
      sub.applyAmount = _calcApplyAmount(expenses)
      sub.subsidyAmount = _calcSubsidyAmount(expenses)
      _updateSubsidyTotal()
      _recalculateAllocations()
      dirty.value = true
    }
  }

  // ===== Allocation =====
  function addAllocationRow(): void {
    allocations.value.push({
      localId: generateUUID(), costOwnerId: '', costOwnerName: '',
      projectId: '', projectName: '', proportion: 0, amount: 0,
      isFirstRow: false, editable: true,
    })
    _recalculateAllocations()
    dirty.value = true
  }

  function removeAllocationRow(localId: string): boolean {
    if (allocations.value.length <= 1) return false
    allocations.value = allocations.value.filter((a) => a.localId !== localId)
    if (allocations.value.length === 1) {
      allocations.value[0]!.isFirstRow = true
      allocations.value[0]!.editable = false
    }
    _recalculateAllocations()
    dirty.value = true
    return true
  }

  /** Update a row's proportion — auto-calc amount & recalc row 1 */
  function updateAllocationProportion(localId: string, proportion: number): void {
    const row = allocations.value.find((a) => a.localId === localId)
    if (!row || !row.editable) return
    const otherSum = allocations.value.filter((a) => a.localId !== localId && a.editable).reduce((s, a) => s + a.proportion, 0)
    if (otherSum + proportion > 1.0001) { row.proportion = 0; row.amount = 0; return }
    row.proportion = proportion
    row.amount = Math.round(totalSubsidyAmount.value * proportion * 100) / 100
    _recalculateAllocations()
    dirty.value = true
  }

  /** Update a row's amount — auto-calc proportion & recalc row 1 */
  function updateAllocationAmount(localId: string, amount: number): void {
    const row = allocations.value.find((a) => a.localId === localId)
    if (!row || !row.editable) return
    if (totalSubsidyAmount.value <= 0) return
    const newProp = amount / totalSubsidyAmount.value
    const otherSum = allocations.value.filter((a) => a.localId !== localId && a.editable).reduce((s, a) => s + a.proportion, 0)
    if (otherSum + newProp > 1.0001) { row.amount = 0; row.proportion = 0; return }
    row.amount = amount
    row.proportion = Math.round(newProp * 10000) / 10000
    _recalculateAllocations()
    dirty.value = true
  }

  function equalSplitAllocations(): void {
    const n = allocations.value.length
    if (n === 0) return
    const perProp = Math.floor((1 / n) * 10000) / 10000
    const rem = Math.round((1 - perProp * (n - 1)) * 10000) / 10000
    allocations.value.forEach((r, i) => {
      r.proportion = i === 0 ? rem : perProp
      r.amount = Math.round(totalSubsidyAmount.value * r.proportion * 100) / 100
    })
    dirty.value = true
  }

  // ===== Submit =====
  function validateForm(): string[] {
    const e: string[] = []
    if (!basicInfo.value.title.trim()) e.push('请输入报销标题')
    if (!basicInfo.value.reimburserId) e.push('请选择报销人')
    if (!basicInfo.value.reimDepartmentId) e.push('请选择报销部门')
    if (!basicInfo.value.reimCompanyId) e.push('请选择费用归属公司')
    if (!basicInfo.value.businessTypeId) e.push('请选择业务类型')
    if (!basicInfo.value.reason.trim()) e.push('请输入出差事由')
    if (trips.value.length === 0) e.push('请至少添加一条补录行程')
    if (allocations.value.length === 0) {
      e.push('请至少添加一条费用分摊')
    } else {
      allocations.value.forEach((a, i) => {
        if (!a.costOwnerId) e.push(`第 ${i + 1} 条费用分摊请选择费用归属`)
        if (!a.projectId) e.push(`第 ${i + 1} 条费用分摊请选择项目`)
      })
    }
    const pSum = allocations.value.reduce((s, a) => s + a.proportion, 0)
    if (Math.abs(pSum - 1) > 0.001) e.push('分摊比例合计必须为100%')
    const aSum = allocations.value.reduce((s, a) => s + a.amount, 0)
    if (Math.abs(aSum - totalSubsidyAmount.value) > 0.01) e.push('分摊金额合计必须等于补助总金额')
    return e
  }

  function buildCreateDTO(token: string): CreateReimbursementParams {
    return {
      idempotentToken: token,
      reimbursement: {
        title: basicInfo.value.title, reimburserId: basicInfo.value.reimburserId, reimburserName: basicInfo.value.reimburserName,
        reimDepartmentId: basicInfo.value.reimDepartmentId, reimDepartmentName: basicInfo.value.reimDepartmentName,
        reimCompanyId: basicInfo.value.reimCompanyId, reimCompanyName: basicInfo.value.reimCompanyName,
        businessTypeId: basicInfo.value.businessTypeId, businessTypeName: basicInfo.value.businessTypeName,
        reason: basicInfo.value.reason, remarks: basicInfo.value.remarks,
      },
      trips: trips.value.map((t) => ({
        travelerId: t.travelerId, travelerName: t.travelerName,
        departureCityId: t.departureCityId, departureCityName: t.departureCityName,
        arrivalCityId: t.arrivalCityId, arrivalCityName: t.arrivalCityName,
        departureDate: t.departureDate, arrivalDate: t.arrivalDate, description: t.description,
        subsidies: subsidies.value.filter((s) => s.localTripId === t.localId).map((s) => ({
          travelerId: s.travelerId, travelerName: s.travelerName,
          travelStartDate: s.travelStartDate, travelEndDate: s.travelEndDate,
          travelDays: s.travelDays, itinerary: s.itinerary, subsidyCity: s.subsidyCity,
          expenses: s.expenses.map((ex) => ({
            expenseDate: ex.expenseDate, week: ex.week, cityId: ex.cityId, cityName: ex.cityName,
            mealAllowance: ex.mealChecked ? ex.mealAllowance : 0,
            transportationAllowance: ex.transportationChecked ? ex.transportationAllowance : 0,
            communicationAllowance: ex.communicationChecked ? ex.communicationAllowance : 0,
          })),
        })),
      })),
      allocations: allocations.value.map((a) => ({
        costOwnerId: a.costOwnerId, costOwnerName: a.costOwnerName,
        projectId: a.projectId, projectName: a.projectName,
        proportion: a.proportion, amount: a.amount,
      })),
    }
  }

  async function submit(token: string): Promise<{ success: boolean; reimId?: string; error?: string }> {
    if (isSubmitting.value) return { success: false, error: '正在提交中...' }
    const errors = validateForm()
    if (errors.length > 0) return { success: false, error: errors.join('; ') }
    isSubmitting.value = true
    try {
      const dto = buildCreateDTO(token)
      const newReimId = await createReimbursement(dto)
      isDraft.value = false; isAudit.value = true; reimId.value = newReimId
      if (draftKey.value) { draftStore.deleteDraft(draftKey.value); draftKey.value = null }
      return { success: true, reimId: newReimId }
    } catch (err: any) {
      return { success: false, error: err.message || '提交失败' }
    } finally { isSubmitting.value = false }
  }

  function saveAsDraft(): boolean {
    if (!isDraft.value || isAudit.value) return false
    const row: ReimbursementListRow = {
      id: reimId.value || '', reimbursementNo: '', title: basicInfo.value.title || '未命名报销单',
      reimburserId: basicInfo.value.reimburserId, reimburserName: basicInfo.value.reimburserName,
      reimDepartmentId: basicInfo.value.reimDepartmentId, reimDepartmentName: basicInfo.value.reimDepartmentName,
      reimCompanyId: basicInfo.value.reimCompanyId, reimCompanyName: basicInfo.value.reimCompanyName,
      businessTypeId: basicInfo.value.businessTypeId, businessTypeName: basicInfo.value.businessTypeName,
      reason: basicInfo.value.reason, remarks: basicInfo.value.remarks,
      status: ReimStatus.DRAFT,
      subsidyTotalAmount: subsidyTotalAmount.value, mealAllowanceTotal: mealAllowanceTotal.value,
      transportationAllowanceTotal: transportationAllowanceTotal.value, communicationAllowanceTotal: communicationAllowanceTotal.value,
      createTime: createTime.value, isDraft: true, draftKey: draftKey.value || undefined, isLocal: true,
    }
    const key = draftStore.saveDraft(row)
    if (key === null) {
      draftBlocked.value = true
      return false
    }
    draftBlocked.value = false
    if (!draftKey.value) draftKey.value = key
    dirty.value = false
    return true
  }

  function updateBasicField(field: keyof ReimbursementBasicInfo, value: string): void {
    ;(basicInfo.value as any)[field] = value
    if (field === 'reimburserId') { const emp = referenceStore.getEmployeeById(value); basicInfo.value.reimburserName = emp?.reimburserName || '' }
    if (field === 'reimDepartmentId') { const dept = referenceStore.getDepartmentById(value); basicInfo.value.reimDepartmentName = dept?.reimDepartmentName || '' }
    if (field === 'reimCompanyId') { const company = referenceStore.getCompanyById(value); basicInfo.value.reimCompanyName = company?.reimCompanyName || '' }
    if (field === 'businessTypeId') { const bt = referenceStore.getBusinessTypeById(value); basicInfo.value.businessTypeName = bt?.businessTypeName || '' }
    dirty.value = true
  }

  function checkTripOverlap(travelerId: string, startDate: string, endDate: string, excludeLocalId?: string): boolean {
    return trips.value.some((t) => {
      if (excludeLocalId && t.localId === excludeLocalId) return false
      if (t.travelerId !== travelerId) return false
      return new Date(startDate).getTime() <= new Date(t.arrivalDate).getTime() && new Date(endDate).getTime() >= new Date(t.departureDate).getTime()
    })
  }

  // ===== Private =====
  function _createDefaultAllocations(): AllocationFormRow[] {
    return [{ localId: generateUUID(), costOwnerId: '', costOwnerName: '', projectId: '', projectName: '', proportion: 1, amount: 0, isFirstRow: true, editable: false }]
  }

  function _regenerateSubsidyForTrip(localTripId: string): void {
    const trip = trips.value.find((t) => t.localId === localTripId)
    if (!trip) return
    subsidies.value = subsidies.value.filter((s) => s.localTripId !== localTripId)
    const startDate = trip.departureDate.substring(0, 10)
    const endDate = trip.arrivalDate.substring(0, 10)
    const travelDays = daysBetween(startDate, endDate)
    const itinerary = `${trip.departureCityName}-${trip.arrivalCityName}`
    const subsidyCity = trip.arrivalCityName
    const cityId = trip.arrivalCityId
    const cityType = getCityTypeByCityId(cityId, referenceStore.cities)
    const mealStandard = getMealStandard(cityType)
    const dates = dateRange(startDate, endDate)
    const expenses: ExpenseCalendarDay[] = dates.map((d) => ({
      localId: generateUUID(), expenseDate: d, week: getWeekDay(d),
      cityId, cityName: subsidyCity, mealStandard, transportStandard: TRANSPORT_ALLOWANCE, commStandard: COMMUNICATION_ALLOWANCE,
      mealAllowance: mealStandard, transportationAllowance: TRANSPORT_ALLOWANCE, communicationAllowance: COMMUNICATION_ALLOWANCE,
      mealChecked: true, transportationChecked: true, communicationChecked: true,
    }))
    const applyAmount = expenses.reduce((sum, e) => sum + e.mealStandard + e.transportStandard + e.commStandard, 0)
    const locId = generateUUID()
    subsidies.value.push({
      id: '', reimId: '', tripId: '', localTripId, localId: locId as any,
      travelerId: trip.travelerId, travelerName: trip.travelerName,
      travelStartDate: startDate, travelEndDate: endDate, travelDays,
      itinerary, subsidyCity, applyAmount, subsidyAmount: applyAmount, expenses,
    } as any)
  }

  function _calcApplyAmount(expenses: ExpenseCalendarDay[]): number {
    return expenses.reduce((sum, e) => sum + e.mealStandard + e.transportStandard + e.commStandard, 0)
  }
  function _calcSubsidyAmount(expenses: ExpenseCalendarDay[]): number {
    return expenses.reduce((sum, e) => sum + (e.mealChecked ? e.mealAllowance : 0) + (e.transportationChecked ? e.transportationAllowance : 0) + (e.communicationChecked ? e.communicationAllowance : 0), 0)
  }
  function _updateSubsidyTotal(): void { totalSubsidyAmount.value = subsidyTotalAmount.value }

  function _recalculateAllocations(): void {
    if (allocations.value.length === 0) return
    const fr = allocations.value[0]!
    if (!fr.isFirstRow) return
    const otherSum = allocations.value.filter((a) => !a.isFirstRow).reduce((s, a) => s + a.proportion, 0)
    fr.proportion = Math.max(0, Math.round((1 - otherSum) * 10000) / 10000)
    fr.amount = Math.round(totalSubsidyAmount.value * fr.proportion * 100) / 100
    for (const r of allocations.value) {
      if (!r.isFirstRow) r.amount = Math.round(totalSubsidyAmount.value * r.proportion * 100) / 100
    }
  }

  function setCreateTime(time: string): void {
    createTime.value = time || today()
  }

  return {
    reimId, draftKey, isDraft, isAudit, isSubmitting, dirty, draftBlocked, basicInfo, trips, subsidies, allocations, totalSubsidyAmount,
    mealAllowanceTotal, transportationAllowanceTotal, communicationAllowanceTotal, subsidyTotalAmount,
    createTime,
    initNew, initFromDraft, setCreateTime, saveTrip, deleteTrip, copyTrip, checkTripOverlap,
    updateSubsidyExpenses,
    addAllocationRow, removeAllocationRow, updateAllocationProportion, updateAllocationAmount, equalSplitAllocations,
    validateForm, buildCreateDTO, submit, saveAsDraft, updateBasicField,
    _formatAmount: formatAmount, _updateSubsidyTotal, _createDefaultAllocations,
    referenceStore,
  }
})
