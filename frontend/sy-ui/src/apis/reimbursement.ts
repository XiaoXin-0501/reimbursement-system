import http from './request'
import type { ApiResponse, PageResult } from '@/types/common'
import type { Reimbursement, ReimbursementPageQuery } from '@/types/reimbursement'
import type { TripDTO } from '@/types/trip'
import type { CostAllocationDTO } from '@/types/costAllocation'
import type { ReimbursementDTO } from '@/types/reimbursement'

/** Create reimbursement request body */
export interface CreateReimbursementParams {
  idempotentToken: string
  reimbursement: ReimbursementDTO
  trips: TripDTO[]
  allocations: CostAllocationDTO[]
}

/** POST /api/reimbursement/create */
export async function createReimbursement(params: CreateReimbursementParams): Promise<string> {
  const res = await http.post<ApiResponse<string>>('/reimbursement/create', params)
  return res.data.data
}

/** POST /api/reimbursement/page */
export async function pageQuery(query: ReimbursementPageQuery): Promise<PageResult<Reimbursement>> {
  const res = await http.post<ApiResponse<PageResult<Reimbursement>>>('/reimbursement/page', query)
  return res.data.data
}

/** DELETE /api/reimbursement/{reimId} */
export async function deleteReimbursement(reimId: string): Promise<void> {
  await http.delete(`/reimbursement/${reimId}`)
}
