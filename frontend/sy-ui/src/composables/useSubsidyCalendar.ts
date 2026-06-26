import { computed, ref } from 'vue'
import type { ExpenseCalendarDay } from '@/types/expense'

export function useSubsidyCalendar(initialDays: ExpenseCalendarDay[] = []) {
  const days = ref<ExpenseCalendarDay[]>([...initialDays])

  /** Check if all checkboxes of a given type are checked */
  const isAllChecked = computed(() => {
    if (days.value.length === 0) return false
    return days.value.every(
      (d) => d.mealChecked && d.transportationChecked && d.communicationChecked,
    )
  })

  /** Check if all days have a specific type checked */
  function isRowChecked(type: 'meal' | 'transportation' | 'communication'): boolean {
    if (days.value.length === 0) return false
    const key = (
      type === 'meal' ? 'mealChecked' : type === 'transportation' ? 'transportationChecked' : 'communicationChecked'
    ) as keyof ExpenseCalendarDay
    return days.value.every((d) => !!d[key])
  }

  /** Check if all types for a specific day index are checked */
  function isColChecked(dayIdx: number): boolean {
    const d = days.value[dayIdx]
    if (!d) return false
    return d.mealChecked && d.transportationChecked && d.communicationChecked
  }

  /** Toggle all checkboxes */
  function toggleAll(): void {
    const newVal = !isAllChecked.value
    for (const d of days.value) {
      d.mealChecked = newVal
      d.transportationChecked = newVal
      d.communicationChecked = newVal
      if (!newVal) {
        d.mealAllowance = 0
        d.transportationAllowance = 0
        d.communicationAllowance = 0
      } else {
        d.mealAllowance = d.mealStandard
        d.transportationAllowance = d.transportStandard
        d.communicationAllowance = d.commStandard
      }
    }
  }

  /** Toggle one type across all days */
  function toggleType(type: 'meal' | 'transportation' | 'communication'): void {
    const newVal = !isRowChecked(type)
    const checkKey = (
      type === 'meal' ? 'mealChecked' : type === 'transportation' ? 'transportationChecked' : 'communicationChecked'
    ) as keyof ExpenseCalendarDay
    const amountKey = (
      type === 'meal' ? 'mealAllowance' : type === 'transportation' ? 'transportationAllowance' : 'communicationAllowance'
    ) as keyof ExpenseCalendarDay
    const standardKey = (
      type === 'meal' ? 'mealStandard' : type === 'transportation' ? 'transportStandard' : 'commStandard'
    ) as keyof ExpenseCalendarDay

    for (const d of days.value) {
      ;(d as any)[checkKey] = newVal
      ;(d as any)[amountKey] = newVal ? (d as any)[standardKey] : 0
    }
  }

  /** Toggle a single day (all types) */
  function toggleDay(dayIdx: number): void {
    const d = days.value[dayIdx]
    if (!d) return
    const newVal = !(d.mealChecked && d.transportationChecked && d.communicationChecked)
    d.mealChecked = newVal
    d.transportationChecked = newVal
    d.communicationChecked = newVal
    if (!newVal) {
      d.mealAllowance = 0
      d.transportationAllowance = 0
      d.communicationAllowance = 0
    } else {
      d.mealAllowance = d.mealStandard
      d.transportationAllowance = d.transportStandard
      d.communicationAllowance = d.commStandard
    }
  }

  /** Toggle a single checkbox for a specific day and type */
  function toggleSingle(dayIdx: number, type: 'meal' | 'transportation' | 'communication'): void {
    const d = days.value[dayIdx]
    if (!d) return
    const checkKey = (
      type === 'meal' ? 'mealChecked' : type === 'transportation' ? 'transportationChecked' : 'communicationChecked'
    ) as keyof ExpenseCalendarDay
    const amountKey = (
      type === 'meal' ? 'mealAllowance' : type === 'transportation' ? 'transportationAllowance' : 'communicationAllowance'
    ) as keyof ExpenseCalendarDay
    const standardKey = (
      type === 'meal' ? 'mealStandard' : type === 'transportation' ? 'transportStandard' : 'commStandard'
    ) as keyof ExpenseCalendarDay

    const newVal = !(d as any)[checkKey]
    ;(d as any)[checkKey] = newVal
    ;(d as any)[amountKey] = newVal ? (d as any)[standardKey] : 0
  }

  /** Update an allowance amount (only if checked, and <= standard) */
  function updateAllowance(
    dayIdx: number,
    type: 'meal' | 'transportation' | 'communication',
    value: number,
  ): void {
    const d = days.value[dayIdx]
    if (!d) return
    const checkKey = (
      type === 'meal' ? 'mealChecked' : type === 'transportation' ? 'transportationChecked' : 'communicationChecked'
    ) as keyof ExpenseCalendarDay
    const amountKey = (
      type === 'meal' ? 'mealAllowance' : type === 'transportation' ? 'transportationAllowance' : 'communicationAllowance'
    ) as keyof ExpenseCalendarDay
    const standardKey = (
      type === 'meal' ? 'mealStandard' : type === 'transportation' ? 'transportStandard' : 'commStandard'
    ) as keyof ExpenseCalendarDay

    if (!(d as any)[checkKey]) return
    // Cap at standard amount
    const std = (d as any)[standardKey] as number
    ;(d as any)[amountKey] = Math.max(0, Math.min(value, std))
  }

  /** Standard total */
  const standardTotal = computed(() =>
    days.value.reduce(
      (sum, d) => sum + d.mealStandard + d.transportStandard + d.commStandard,
      0,
    ),
  )

  /** Actual subsidy total */
  const subsidyTotal = computed(() =>
    days.value.reduce(
      (sum, d) =>
        sum +
        (d.mealChecked ? d.mealAllowance : 0) +
        (d.transportationChecked ? d.transportationAllowance : 0) +
        (d.communicationChecked ? d.communicationAllowance : 0),
      0,
    ),
  )

  return {
    days,
    isAllChecked,
    isRowChecked,
    isColChecked,
    toggleAll,
    toggleType,
    toggleDay,
    toggleSingle,
    updateAllowance,
    standardTotal,
    subsidyTotal,
  }
}
