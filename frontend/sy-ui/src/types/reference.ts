import type { CityType } from './common'

export interface Company {
  reimCompanyId: string
  reimCompanyNo: string
  reimCompanyName: string
}

export interface Department {
  reimDepartmentId: string
  reimDepartmentNo: string
  reimDepartmentName: string
}

export interface Employee {
  reimburserId: string
  reimburserNo: string
  reimburserName: string
}

export interface BusinessTypeItem {
  businessTypeId: string
  businessTypeNo: string
  businessTypeName: string
  thereSubordinateNode: string // "1" | "0"
  superiorId: string
}

export interface BusinessTypeTreeNode extends BusinessTypeItem {
  children?: BusinessTypeTreeNode[]
}

export interface City {
  cityNo: string
  cityName: string
  cityType: CityType
}

export interface Project {
  projectId: string
  projectNo: string
  projectName: string
}
