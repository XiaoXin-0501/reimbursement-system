/** Unified API response wrapper */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** Paginated query result */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** Reimbursement status enum */
export enum ReimStatus {
  DRAFT = 0,
  AUDIT = 1,
  APPROVED = 2,
  REJECTED = 3,
  ABOLISHED = 4,
}

/** City tier enum */
export enum CityType {
  FIRST_TIER = '1',
  SECOND_TIER = '2',
  THIRD_TIER = '3',
}
