import http from './request'
import type { ApiResponse } from '@/types/common'
import type { SubsidyInfo } from '@/types/subsidy'

/** GET /api/subsidy/get/{reimId} */
export async function getSubsidiesByReimId(reimId: string): Promise<SubsidyInfo[]> {
  const res = await http.get<ApiResponse<SubsidyInfo[]>>(`/subsidy/get/${reimId}`)
  return res.data.data
}
