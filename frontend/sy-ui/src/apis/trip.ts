import http from './request'
import type { ApiResponse } from '@/types/common'
import type { Trip } from '@/types/trip'

/** GET /api/trip/get/{reimId} */
export async function getTripsByReimId(reimId: string): Promise<Trip[]> {
  const res = await http.get<ApiResponse<Trip[]>>(`/trip/get/${reimId}`)
  return res.data.data
}
