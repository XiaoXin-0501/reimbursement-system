import { CityType } from '@/types/common'

/**
 * Meal allowance by city tier (元/天)
 */
export const MEAL_ALLOWANCE_MAP: Record<CityType, number> = {
  [CityType.FIRST_TIER]: 100,
  [CityType.SECOND_TIER]: 80,
  [CityType.THIRD_TIER]: 50,
}

/** Transport allowance: all cities 40/day */
export const TRANSPORT_ALLOWANCE = 40

/** Communication allowance: all cities 40/day */
export const COMMUNICATION_ALLOWANCE = 40

/**
 * Get the meal allowance standard for a given city type
 */
export function getMealStandard(cityType: CityType): number {
  return MEAL_ALLOWANCE_MAP[cityType] || 0
}

/**
 * Get the city type from a city ID (lookup in city data)
 */
export function getCityTypeByCityId(
  cityId: string,
  cities: Array<{ cityNo: string; cityType: CityType }>,
): CityType {
  const city = cities.find((c) => c.cityNo === cityId)
  return city?.cityType || CityType.THIRD_TIER
}
