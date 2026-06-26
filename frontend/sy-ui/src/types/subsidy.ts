import type { ExpenseCalendarDay, ExpenseDetailDTO } from './expense'

/** API subsidy info record */
export interface SubsidyInfo {
  id: string
  reimId: string
  tripId: string
  travelerId: string
  travelerName: string
  travelStartDate: string
  travelEndDate: string
  travelDays: number
  itinerary: string
  subsidyCity: string
  applyAmount: number
  subsidyAmount: number
}

/** Local subsidy info with linked expenses */
export interface SubsidyInfoLocal extends SubsidyInfo {
  localTripId: string // references TripFormData.localId
  expenses: ExpenseCalendarDay[]
}

/** Subsidy info nested inside TripDTO */
export interface SubsidyInfoDTO {
  travelerId: string
  travelerName: string
  travelStartDate: string
  travelEndDate: string
  travelDays: number
  itinerary: string
  subsidyCity: string
  expenses: ExpenseDetailDTO[]
}
