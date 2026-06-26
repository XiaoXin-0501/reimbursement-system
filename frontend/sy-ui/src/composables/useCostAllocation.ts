import { ref, computed } from 'vue'
import type { AllocationFormRow } from '@/types/costAllocation'
import { generateUUID } from '@/utils/idGenerator'

export function useCostAllocation(totalAmount: () => number) {
  const rows = ref<AllocationFormRow[]>([
    {
      localId: generateUUID(),
      costOwnerId: '',
      costOwnerName: '',
      projectId: '',
      projectName: '',
      proportion: 1,
      amount: 0,
      isFirstRow: true,
      editable: false,
    },
  ])

  function recalculate(): void {
    const total = totalAmount()
    const otherSum = rows.value
      .filter((r) => !r.isFirstRow)
      .reduce((s, r) => s + r.proportion, 0)

    if (otherSum > 1) {
      // Invalid state — clear non-first rows
      for (const r of rows.value) {
        if (!r.isFirstRow) {
          r.proportion = 0
          r.amount = 0
        }
      }
    }

    // Set first row proportion
    if (rows.value.length > 0 && rows.value[0]!.isFirstRow) {
      const firstProportion = Math.max(0, Math.round((1 - otherSum) * 10000) / 10000)
      rows.value[0]!.proportion = firstProportion
      rows.value[0]!.amount = Math.round(total * firstProportion * 100) / 100
    }

    // Update non-first row amounts
    for (const r of rows.value) {
      if (!r.isFirstRow) {
        r.amount = Math.round(total * r.proportion * 100) / 100
      }
    }
  }

  function addRow(): void {
    rows.value.push({
      localId: generateUUID(),
      costOwnerId: '',
      costOwnerName: '',
      projectId: '',
      projectName: '',
      proportion: 0,
      amount: 0,
      isFirstRow: false,
      editable: true,
    })
    recalculate()
  }

  function removeRow(localId: string): boolean {
    if (rows.value.length <= 1) return false
    rows.value = rows.value.filter((r) => r.localId !== localId)
    if (rows.value.length === 1) {
      rows.value[0]!.isFirstRow = true
      rows.value[0]!.editable = false
    }
    recalculate()
    return true
  }

  function updateProportion(localId: string, proportion: number): void {
    const row = rows.value.find((r) => r.localId === localId)
    if (!row || !row.editable) return
    row.proportion = proportion
    recalculate()
  }

  function equalSplit(): void {
    const n = rows.value.length
    if (n === 0) return
    const equalShare = Math.floor((1 / n) * 10000) / 10000
    const remainder = Math.round((1 - equalShare * (n - 1)) * 10000) / 10000
    rows.value.forEach((row, i) => {
      row.proportion = i === 0 ? remainder : equalShare
    })
    recalculate()
  }

  const proportionSum = computed(() => rows.value.reduce((s, r) => s + r.proportion, 0))
  const amountSum = computed(() => rows.value.reduce((s, r) => s + r.amount, 0))

  function reset(initialRows?: AllocationFormRow[]): void {
    if (initialRows && initialRows.length > 0) {
      rows.value = initialRows
    } else {
      rows.value = [
        {
          localId: generateUUID(),
          costOwnerId: '',
          costOwnerName: '',
          projectId: '',
          projectName: '',
          proportion: 1,
          amount: 0,
          isFirstRow: true,
          editable: false,
        },
      ]
    }
    recalculate()
  }

  return {
    rows,
    recalculate,
    addRow,
    removeRow,
    updateProportion,
    equalSplit,
    proportionSum,
    amountSum,
    reset,
  }
}
