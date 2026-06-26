import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { CityType } from '@/types/common'
import type {
  Company,
  Department,
  Employee,
  BusinessTypeItem,
  BusinessTypeTreeNode,
  City,
  Project,
} from '@/types/reference'
import { buildBusinessTypeTree, getLeafBusinessTypeIds } from '@/utils/businessTypeTree'

export const useReferenceStore = defineStore('reference', () => {
  // ========== 5.3.1 费用归属公司控件数据 ==========
  const companies = ref<Company[]>([
    { reimCompanyId: '1C54557F1782E000', reimCompanyNo: '0407', reimCompanyName: '胜意科技北京分公司' },
    { reimCompanyId: '19218A262C976000', reimCompanyNo: '0408', reimCompanyName: '胜意科技上海分公司' },
    { reimCompanyId: '1C61686865DA8000', reimCompanyNo: '0409', reimCompanyName: '胜意科技武汉分公司' },
    { reimCompanyId: '1717271D1DA15000', reimCompanyNo: '0410', reimCompanyName: '胜意科技杭州分公司' },
    { reimCompanyId: '16AE93CC7EF92002', reimCompanyNo: '0411', reimCompanyName: '胜意科技荆州分公司' },
  ])

  // ========== 5.3.2 报销部门控件数据 ==========
  const departments = ref<Department[]>([
    { reimDepartmentId: '13AB8D7B52A9B002', reimDepartmentNo: '072001', reimDepartmentName: '客户成功事业部' },
    { reimDepartmentId: '13BFD31C6029A002', reimDepartmentNo: '072002', reimDepartmentName: '企业消费事业部' },
    { reimDepartmentId: '14515BB4BFB92003', reimDepartmentNo: '072003', reimDepartmentName: '企业费控事业部' },
    { reimDepartmentId: '19206611C47A6000', reimDepartmentNo: '072004', reimDepartmentName: '集采事业部' },
    { reimDepartmentId: '19D32F9FE9647000', reimDepartmentNo: '072005', reimDepartmentName: '航旅事业部' },
    { reimDepartmentId: '13C7E2BAE0393001', reimDepartmentNo: '072006', reimDepartmentName: '运营事业部' },
    { reimDepartmentId: '14055D22BB808001', reimDepartmentNo: '072007', reimDepartmentName: '营销事业部' },
  ])

  // ========== 5.3.3 员工控件数据 ==========
  const employees = ref<Employee[]>([
    { reimburserId: '13AB3A3F72409002', reimburserNo: '74541', reimburserName: '徐年年' },
    { reimburserId: '13AB498CC6409002', reimburserNo: '74008', reimburserName: '郑雨雪' },
    { reimburserId: '13AB4A56BB009002', reimburserNo: '21552', reimburserName: '邹薇' },
    { reimburserId: '13AB591FE8009002', reimburserNo: '80681', reimburserName: '王成军' },
    { reimburserId: '13AB77281A408001', reimburserNo: '89899', reimburserName: '潘展飞' },
    { reimburserId: '13AB7925EB808001', reimburserNo: '10503', reimburserName: '姜林' },
  ])

  // ========== 5.3.4 业务类型控件数据 ==========
  const businessTypes = ref<BusinessTypeItem[]>([
    { businessTypeId: '18F0916A8C2C4000', businessTypeNo: '1001001', businessTypeName: '员工差旅活动', thereSubordinateNode: '1', superiorId: 'none' },
    { businessTypeId: '18F091913EEC4000', businessTypeNo: '100100101', businessTypeName: '境内出差', thereSubordinateNode: '1', superiorId: '18F0916A8C2C4000' },
    { businessTypeId: '1B5FEB7DD4396000', businessTypeNo: '10010010101', businessTypeName: '项目出差', thereSubordinateNode: '0', superiorId: '18F091913EEC4000' },
    { businessTypeId: '1A92E43082EFC000', businessTypeNo: '10010010102', businessTypeName: '市场拓展出差', thereSubordinateNode: '0', superiorId: '18F091913EEC4000' },
    { businessTypeId: '13AB3A4138008001', businessTypeNo: '100100102', businessTypeName: '境外出差', thereSubordinateNode: '1', superiorId: '18F0916A8C2C4000' },
    { businessTypeId: '13AB3A4248008002', businessTypeNo: '10010010201', businessTypeName: '国外考察', thereSubordinateNode: '0', superiorId: '13AB3A4138008001' },
    { businessTypeId: '13AB3A4154008001', businessTypeNo: '10010010202', businessTypeName: '售后维护出差', thereSubordinateNode: '0', superiorId: '13AB3A4138008001' },
    { businessTypeId: '13AB3A4172008001', businessTypeNo: '1001002', businessTypeName: '人力资源', thereSubordinateNode: '1', superiorId: 'none' },
    { businessTypeId: '13AB3A418F808001', businessTypeNo: '100100201', businessTypeName: '个人团队培训', thereSubordinateNode: '0', superiorId: '13AB3A4172008001' },
    { businessTypeId: '13AB3A41AC408001', businessTypeNo: '100100202', businessTypeName: '招聘会', thereSubordinateNode: '0', superiorId: '13AB3A4172008001' },
    { businessTypeId: '13AB3A41CD808002', businessTypeNo: '1001003', businessTypeName: '员工福利', thereSubordinateNode: '1', superiorId: 'none' },
    { businessTypeId: '13AB3A41ED408002', businessTypeNo: '100100301', businessTypeName: '员工旅游', thereSubordinateNode: '0', superiorId: '13AB3A41CD808002' },
    { businessTypeId: '13AB3A420CC08002', businessTypeNo: '100100302', businessTypeName: '员工团建', thereSubordinateNode: '0', superiorId: '13AB3A41CD808002' },
    { businessTypeId: '13AB3A422A808001', businessTypeNo: '100100303', businessTypeName: '员工体检', thereSubordinateNode: '0', superiorId: '13AB3A41CD808002' },
  ])

  // ========== 5.3.5 城市控件数据 ==========
  const cities = ref<City[]>([
    { cityNo: '10119', cityName: '北京', cityType: '1' as CityType },
    { cityNo: '10621', cityName: '上海', cityType: '1' as CityType },
    { cityNo: '10458', cityName: '武汉', cityType: '2' as CityType },
    { cityNo: '10216', cityName: '杭州', cityType: '2' as CityType },
    { cityNo: '10455', cityName: '荆州', cityType: '3' as CityType },
  ])

  // ========== 5.3.6 项目控件数据 ==========
  const projects = ref<Project[]>([
    { projectId: '12BC248B25083001', projectNo: 'nonProjectRelated', projectName: '非项目类费用归集' },
    { projectId: '1C811ABF96195000', projectNo: 'centralChina', projectName: '华中客户定制化项目' },
    { projectId: '1C5931735AC4A000', projectNo: 'southChina', projectName: '华南客户定制化项目' },
    { projectId: '1771EC45F2443000', projectNo: 'northChina', projectName: '华北客户定制化项目' },
    { projectId: '1762792DB4E9A002', projectNo: 'eastChina', projectName: '华东客户定制化项目' },
    { projectId: '17071065FC29A002', projectNo: 'southWest', projectName: '西南客户定制化项目' },
    { projectId: '162664EBE9ABE001', projectNo: 'northWest', projectName: '西北客户定制化项目' },
    { projectId: '162664B8526BE002', projectNo: 'northEast', projectName: '东北客户定制化项目' },
  ])

  // ===== Getters =====

  /** Build the tree structure for business types */
  const businessTypeTree = computed<BusinessTypeTreeNode[]>(() => {
    return buildBusinessTypeTree(businessTypes.value)
  })

  /** Get only leaf nodes (no children) for selection */
  const businessTypeLeaves = computed<BusinessTypeItem[]>(() => {
    return businessTypes.value.filter((b) => b.thereSubordinateNode === '0')
  })

  /** Leaf node IDs for filter validation */
  const businessTypeLeafIds = computed<string[]>(() => {
    return getLeafBusinessTypeIds(businessTypeTree.value)
  })

  /** Companies formatted for select options */
  const companyOptions = computed(() =>
    companies.value.map((c) => ({ label: c.reimCompanyName, value: c.reimCompanyId })),
  )

  /** Departments formatted for select options */
  const departmentOptions = computed(() =>
    departments.value.map((d) => ({
      label: d.reimDepartmentName,
      value: d.reimDepartmentId,
    })),
  )

  /** Employees formatted for select options */
  const employeeOptions = computed(() =>
    employees.value.map((e) => ({
      label: e.reimburserName,
      value: e.reimburserId,
    })),
  )

  /** Cities formatted for select options */
  const cityOptions = computed(() =>
    cities.value.map((c) => ({ label: c.cityName, value: c.cityNo })),
  )

  /** Projects formatted for select options */
  const projectOptions = computed(() =>
    projects.value.map((p) => ({ label: p.projectName, value: p.projectId })),
  )

  // ===== Lookup helpers =====
  function getCompanyById(id: string): Company | undefined {
    return companies.value.find((c) => c.reimCompanyId === id)
  }

  function getDepartmentById(id: string): Department | undefined {
    return departments.value.find((d) => d.reimDepartmentId === id)
  }

  function getEmployeeById(id: string): Employee | undefined {
    return employees.value.find((e) => e.reimburserId === id)
  }

  function getCityByNo(no: string): City | undefined {
    return cities.value.find((c) => c.cityNo === no)
  }

  function getProjectById(id: string): Project | undefined {
    return projects.value.find((p) => p.projectId === id)
  }

  function getBusinessTypeById(id: string): BusinessTypeItem | undefined {
    return businessTypes.value.find((b) => b.businessTypeId === id)
  }

  return {
    // State
    companies,
    departments,
    employees,
    businessTypes,
    cities,
    projects,
    // Getters
    businessTypeTree,
    businessTypeLeaves,
    businessTypeLeafIds,
    companyOptions,
    departmentOptions,
    employeeOptions,
    cityOptions,
    projectOptions,
    // Helpers
    getCompanyById,
    getDepartmentById,
    getEmployeeById,
    getCityByNo,
    getProjectById,
    getBusinessTypeById,
  }
})
