/** API cost allocation record */
export interface CostAllocation {
  id: string
  reimId: string
  costOwnerId: string
  costOwnerName: string
  projectId: string
  projectName: string
  proportion: number // 0~1
  amount: number
}

/** Frontend allocation row (may not have id yet) */
export interface AllocationFormRow {
  localId: string
  costOwnerId: string
  costOwnerName: string
  projectId: string
  projectName: string
  proportion: number // displayed as percentage value (0-1 for calc, display as %)
  amount: number
  isFirstRow: boolean
  editable: boolean
}

/** Cost allocation inside create DTO */
export interface CostAllocationDTO {
  costOwnerId: string
  costOwnerName: string
  projectId: string
  projectName: string
  proportion: number
  amount: number
}
