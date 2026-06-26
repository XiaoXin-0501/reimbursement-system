import http from './request'
import type { ApiResponse } from '@/types/common'
import type { CostAllocation } from '@/types/costAllocation'

/** GET /api/cost/allocation/get/{reimId} */
export async function getCostAllocationsByReimId(reimId: string): Promise<CostAllocation[]> {
  const res = await http.get<ApiResponse<CostAllocation[]>>(`/cost/allocation/get/${reimId}`)
  return res.data.data
}
