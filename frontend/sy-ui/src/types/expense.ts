/** API expense detail record */
export interface ExpenseDetail {
  id: string
  reimId: string
  subsidyInfoId: string
  expenseDate: string
  week: number // 1-7, Monday-Sunday
  cityId: string
  cityName: string
  mealAllowance: number
  transportationAllowance: number
  communicationAllowance: number
}

/** A single day in the subsidy calendar (frontend model) */
export interface ExpenseCalendarDay {
  localId: string
  expenseDate: string // yyyy-MM-dd
  week: number // 1-7
  cityId: string
  cityName: string
  mealStandard: number // standard amount based on city type
  transportStandard: number // always 40
  commStandard: number // always 40
  mealAllowance: number // actual amount (editable if checked)
  transportationAllowance: number
  communicationAllowance: number
  mealChecked: boolean
  transportationChecked: boolean
  communicationChecked: boolean
}

/** Expense detail inside SubsidyInfoDTO */
export interface ExpenseDetailDTO {
  expenseDate: string
  week: number
  cityId: string
  cityName: string
  mealAllowance: number
  transportationAllowance: number
  communicationAllowance: number
}
