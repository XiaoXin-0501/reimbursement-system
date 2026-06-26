import type { SubsidyInfoDTO } from './subsidy'

/** API trip record */
export interface Trip {
  id: string
  reimId: string
  travelerId: string
  travelerName: string
  departureCityId: string
  departureCityName: string
  arrivalCityId: string
  arrivalCityName: string
  departureDate: string // "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd"
  arrivalDate: string
  description: string
}

/** Local form model for trips (before submission) */
export interface TripFormData {
  localId: string // temp UUID for frontend tracking
  travelerId: string
  travelerName: string
  departureCityId: string
  departureCityName: string
  arrivalCityId: string
  arrivalCityName: string
  departureDate: string // "yyyy-MM-dd HH:mm:ss"
  arrivalDate: string // "yyyy-MM-dd HH:mm:ss"
  description: string
}

/** Trip nested inside create DTO */
export interface TripDTO {
  travelerId: string
  travelerName: string
  departureCityId: string
  departureCityName: string
  arrivalCityId: string
  arrivalCityName: string
  departureDate: string
  arrivalDate: string
  description: string
  subsidies: SubsidyInfoDTO[]
}
