import type { ReimStatus } from './common'

/** API reimbursement record */
export interface Reimbursement {
  id: string
  reimbursementNo: string
  title: string
  reimburserId: string
  reimburserName: string
  reimDepartmentId: string
  reimDepartmentName: string
  reimCompanyId: string
  reimCompanyName: string
  businessTypeId: string
  businessTypeName: string
  reason: string
  remarks: string
  status: ReimStatus
  subsidyTotalAmount: number
  mealAllowanceTotal: number
  transportationAllowanceTotal: number
  communicationAllowanceTotal: number
  createTime: string
}

/** Used for the list page (API row + draft row merged) */
export interface ReimbursementListRow extends Reimbursement {
  isDraft: boolean
  draftKey?: string // localStorage key for drafts
  isLocal?: boolean // whether this is a local draft not yet submitted
}

export interface ReimbursementPageQuery {
  current: number
  size: number
  reimbursementNo?: string
  title?: string
  reason?: string
  reimCompanyId?: string
  reimDepartmentId?: string
  reimburserId?: string
  businessTypeId?: string
}

/** Basic info part of reimbursement */
export interface ReimbursementBasicInfo {
  title: string
  reimburserId: string
  reimburserName: string
  reimDepartmentId: string
  reimDepartmentName: string
  reimCompanyId: string
  reimCompanyName: string
  businessTypeId: string
  businessTypeName: string
  reason: string
  remarks: string
}

/** Create request reimbursement part */
export interface ReimbursementDTO {
  title: string
  reimburserId: string
  reimburserName: string
  reimDepartmentId: string
  reimDepartmentName: string
  reimCompanyId: string
  reimCompanyName: string
  businessTypeId: string
  businessTypeName: string
  reason: string
  remarks: string
}
