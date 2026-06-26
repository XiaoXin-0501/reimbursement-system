import http from './request'
import type { ApiResponse } from '@/types/common'
import type { ExpenseDetail } from '@/types/expense'

/** GET /api/expense/get/{subsidyInfoId} */
export async function getExpensesBySubsidyInfoId(subsidyInfoId: string): Promise<ExpenseDetail[]> {
  const res = await http.get<ApiResponse<ExpenseDetail[]>>(`/expense/get/${subsidyInfoId}`)
  return res.data.data
}
