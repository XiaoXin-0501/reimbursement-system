<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReferenceStore } from '@/stores/reference'
import { useDraftStore } from '@/stores/draft'
import { useToastStore } from '@/stores/toast'
import { pageQuery, deleteReimbursement } from '@/apis/reimbursement'
import { ReimStatus } from '@/types/common'
import type { Reimbursement, ReimbursementListRow, ReimbursementPageQuery } from '@/types/reimbursement'
import { formatDate, formatAmount, truncateText } from '@/utils/format'
import { buildBusinessTypeTree } from '@/utils/businessTypeTree'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const router = useRouter()
const referenceStore = useReferenceStore()
const draftStore = useDraftStore()
const toastStore = useToastStore()

// ===== Dialog State =====
const deleteConfirmVisible = ref(false)
const draftLimitAlertVisible = ref(false)
const deleteTargetRow = ref<ReimbursementListRow | null>(null)

// ===== Filter State =====
const filterLine1 = reactive({
  reimbursementNo: '',
  title: '',
  reason: '',
  reimCompanyId: '',
})
const filterLine2 = reactive({
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: '',
})

// ===== Table State =====
const tableData = ref<ReimbursementListRow[]>([])
const loading = ref(false)

// ===== Pagination State =====
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)
const totalPages = ref(0)
const goToPage = ref('')

// ===== Business Type Tree Props =====
const businessTypeTreeData = computed(() => buildBusinessTypeTree(referenceStore.businessTypes))

const filterProps = {
  value: 'businessTypeId',
  label: 'businessTypeName',
  children: 'children',
  checkStrictly: false,
}

// ===== Lifecycle =====
onMounted(() => {
  draftStore.cleanExpired()
  handleSearch()
})

// ===== Handlers =====
function handleSearch(): void {
  currentPage.value = 1
  fetchData()
}

function handleClear(): void {
  filterLine1.reimbursementNo = ''
  filterLine1.title = ''
  filterLine1.reason = ''
  filterLine1.reimCompanyId = ''
  filterLine2.reimDepartmentId = ''
  filterLine2.reimburserId = ''
  filterLine2.businessTypeId = ''
}

function handlePageChange(page: number): void {
  currentPage.value = page
  applyFrontendPagination()
}

function handleSizeChange(size: number): void {
  pageSize.value = size
  currentPage.value = 1
  applyFrontendPagination()
}

function handleGoToPage(): void {
  const page = parseInt(goToPage.value, 10)
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    toastStore.warning('请输入有效的页码')
    return
  }
  currentPage.value = page
  goToPage.value = ''
  applyFrontendPagination()
}

// ===== Fetch Data (API + merge drafts, unified frontend pagination) =====
// Cache of all API records (refreshed on search / filter change)
const allApiRecords = ref<ReimbursementListRow[]>([])

function buildQueryForBackend(): ReimbursementPageQuery {
  return {
    current: 1,
    size: 100000,
    reimbursementNo: filterLine1.reimbursementNo || undefined,
    title: filterLine1.title || undefined,
    reason: filterLine1.reason || undefined,
    reimCompanyId: filterLine1.reimCompanyId || undefined,
    reimDepartmentId: filterLine2.reimDepartmentId || undefined,
    reimburserId: filterLine2.reimburserId || undefined,
    businessTypeId: filterLine2.businessTypeId || undefined,
  }
}

function matchDraftByQuery(draft: any, query: ReimbursementPageQuery): boolean {
  if (query.reimbursementNo && draft.reimbursementNo && !draft.reimbursementNo.includes(query.reimbursementNo)) return false
  if (query.title && draft.title && !draft.title.includes(query.title)) return false
  if (query.reason && draft.reason && !draft.reason.includes(query.reason)) return false
  if (query.reimCompanyId && draft.reimCompanyId !== query.reimCompanyId) return false
  if (query.reimDepartmentId && draft.reimDepartmentId !== query.reimDepartmentId) return false
  if (query.reimburserId && draft.reimburserId !== query.reimburserId) return false
  if (query.businessTypeId && draft.businessTypeId !== query.businessTypeId) return false
  return true
}

function applyFrontendPagination(): void {
  const allDrafts = draftStore.loadAllDrafts()
  const query = buildQueryForBackend()
  const filteredDrafts = allDrafts.filter((d) => matchDraftByQuery(d, query))

  // 由于后端 create_time 精度为「日」，同日记录无法按时间排序
  // 采用雪花 ID 倒序作为可靠的创建先后依据（ID 按时间单调递增）
  const sortedApi = [...allApiRecords.value].sort((a, b) => {
    const idA = a.id || ''
    const idB = b.id || ''
    if (idA !== idB) return idB.localeCompare(idA)
    const ta = a.createTime ? new Date(a.createTime).getTime() : 0
    const tb = b.createTime ? new Date(b.createTime).getTime() : 0
    return tb - ta
  })
  const allRecords = [...filteredDrafts, ...sortedApi]
  totalRecords.value = allRecords.length
  totalPages.value = Math.max(1, Math.ceil(totalRecords.value / pageSize.value))

  const start = (currentPage.value - 1) * pageSize.value
  tableData.value = allRecords.slice(start, start + pageSize.value)
}

async function fetchData(): Promise<void> {
  loading.value = true
  try {
    const query = buildQueryForBackend()
    const result = await pageQuery(query)
    allApiRecords.value = (result.records || []).map((r) => ({
      ...r,
      isDraft: false,
      isLocal: false,
    })) as ReimbursementListRow[]

    applyFrontendPagination()
  } catch (err: any) {
    toastStore.error(err.message || '查询失败')
    allApiRecords.value = []
    const allDrafts = draftStore.loadAllDrafts()
    const query = buildQueryForBackend()
    const filteredDrafts = allDrafts.filter((d) => matchDraftByQuery(d, query))
    totalRecords.value = filteredDrafts.length
    totalPages.value = Math.max(1, Math.ceil(totalRecords.value / pageSize.value))
    const start = (currentPage.value - 1) * pageSize.value
    tableData.value = filteredDrafts.slice(start, start + pageSize.value)
  } finally {
    loading.value = false
  }
}

// ===== Row Actions =====
function handleCreate(): void {
  if (!draftStore.canCreateDraft()) {
    draftLimitAlertVisible.value = true
    return
  }
  router.push('/detail')
}

function handleEdit(row: ReimbursementListRow): void {
  if (row.isLocal) {
    router.push(`/detail/${row.draftKey}`)
  } else if (row.status === ReimStatus.DRAFT) {
    // API record in draft status — can still edit
    router.push(`/detail/${row.id}`)
  }
}

function handleCopy(row: ReimbursementListRow): void {
  if (!draftStore.canCreateDraft()) {
    draftLimitAlertVisible.value = true
    return
  }
  if (row.isLocal) {
    router.push({ path: '/detail', query: { copyFrom: row.draftKey } })
  } else {
    router.push({ path: '/detail', query: { copyFrom: row.id } })
  }
}

function closeDraftLimitAlert(): void {
  draftLimitAlertVisible.value = false
}

async function handleDelete(row: ReimbursementListRow): Promise<void> {
  deleteTargetRow.value = row
  deleteConfirmVisible.value = true
}

async function confirmDelete(): Promise<void> {
  const row = deleteTargetRow.value
  if (!row) return
  deleteConfirmVisible.value = false
  deleteTargetRow.value = null
  try {
    if (row.isLocal) {
      draftStore.deleteDraft(row.draftKey!)
      toastStore.success('删除成功')
    } else {
      await deleteReimbursement(row.id)
      toastStore.success('删除成功')
    }
    fetchData()
  } catch (e: any) {
    toastStore.error(e?.message || '删除失败')
  }
}

function cancelDelete(): void {
  deleteConfirmVisible.value = false
  deleteTargetRow.value = null
}

function handleViewDetail(row: ReimbursementListRow): void {
  if (row.isLocal) {
    router.push(`/detail/${row.draftKey}`)
  } else {
    router.push(`/detail/${row.id}`)
  }
}

// ===== Format Helpers =====
function getStatusText(status: ReimStatus): string {
  const map: Record<number, string> = {
    [ReimStatus.DRAFT]: '草稿',
    [ReimStatus.AUDIT]: '审批中',
    [ReimStatus.APPROVED]: '审批通过',
    [ReimStatus.REJECTED]: '审批不通过',
    [ReimStatus.ABOLISHED]: '已废除',
  }
  return map[status] || '未知'
}

function getStatusType(status: ReimStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    [ReimStatus.DRAFT]: 'info',
    [ReimStatus.AUDIT]: 'warning',
    [ReimStatus.APPROVED]: 'success',
    [ReimStatus.REJECTED]: 'danger',
    [ReimStatus.ABOLISHED]: 'danger',
  }
  return map[status] || 'info'
}

function getEmployeeDisplay(row: ReimbursementListRow): string {
  const emp = referenceStore.getEmployeeById(row.reimburserId)
  if (emp) return emp.reimburserName
  if (row.reimburserName) return row.reimburserName
  return row.reimburserId || ''
}

function getDepartmentDisplay(row: ReimbursementListRow): string {
  const dept = referenceStore.getDepartmentById(row.reimDepartmentId)
  if (dept) return dept.reimDepartmentName
  if (row.reimDepartmentName) return row.reimDepartmentName
  return row.reimDepartmentId || ''
}

function getCompanyDisplay(row: ReimbursementListRow): string {
  const comp = referenceStore.getCompanyById(row.reimCompanyId)
  if (comp) return comp.reimCompanyName
  return row.reimCompanyName || row.reimCompanyId || ''
}

function getBusinessTypeDisplay(row: ReimbursementListRow): string {
  const bt = referenceStore.getBusinessTypeById(row.businessTypeId)
  if (bt) return bt.businessTypeName
  return row.businessTypeName || row.businessTypeId || ''
}

function handleMoreCommand(command: string, row: ReimbursementListRow): void {
  if (command === 'delete') handleDelete(row)
  else if (command === 'push') {
    if (!row.isLocal) return
    router.push(`/detail/${row.draftKey}`)
  }
  else if (command === 'copy') handleCopy(row)
}
</script>

<template>
  <div class="list-container">
    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-wrap">
        <!-- Line 1 -->
        <div class="filter-row filter-row--line1">
          <div class="filter-item">
            <label>报销单号</label>
            <el-input v-model="filterLine1.reimbursementNo" placeholder="输入报销单号" clearable />
          </div>
          <div class="filter-item">
            <label>标题</label>
            <el-input v-model="filterLine1.title" placeholder="输入标题" clearable />
          </div>
          <div class="filter-item">
            <label>事由</label>
            <el-input v-model="filterLine1.reason" placeholder="输入事由" clearable />
          </div>
          <div class="filter-item">
            <label>费用归属公司</label>
            <el-select v-model="filterLine1.reimCompanyId" placeholder="请选择" clearable>
              <el-option v-for="c in referenceStore.companies" :key="c.reimCompanyId" :label="c.reimCompanyName" :value="c.reimCompanyId" />
            </el-select>
          </div>
        </div>
        <!-- Line 2 -->
        <div class="filter-row filter-row--line2">
          <div class="filter-item">
            <label>报销部门</label>
            <el-select v-model="filterLine2.reimDepartmentId" placeholder="请选择" clearable>
              <el-option v-for="d in referenceStore.departments" :key="d.reimDepartmentId" :label="d.reimDepartmentName" :value="d.reimDepartmentId" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>报销人</label>
            <el-select v-model="filterLine2.reimburserId" placeholder="请选择" clearable>
              <el-option v-for="e in referenceStore.employees" :key="e.reimburserId" :label="e.reimburserName" :value="e.reimburserId" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>业务类型</label>
            <el-tree-select v-model="filterLine2.businessTypeId" :data="businessTypeTreeData" :props="filterProps" placeholder="请选择" clearable check-strictly />
          </div>
          <div class="filter-buttons">
            <el-button class="btn-create" @click="handleCreate">新增</el-button>
            <el-button class="btn-clear" @click="handleClear">清除</el-button>
            <el-button class="btn-search" type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table Section -->
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" border stripe size="default" style="width: 100%">
        <el-table-column width="60" align="center">
          <template #header>
            <i class="iconfont icon-liebiao list-header-icon"></i>
          </template>
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <span class="action-icons">
              <i class="iconfont icon-fuzhi action-icon" title="复制" @click="handleCopy(row)"></i>
              <i
                class="iconfont icon-xiugai action-icon"
                :class="{ disabled: !row.isLocal && row.status !== ReimStatus.DRAFT }"
                title="编辑"
                @click="(!row.isLocal && row.status !== ReimStatus.DRAFT) ? null : handleEdit(row)"
              ></i>
              <el-dropdown trigger="click" @command="(cmd: string) => handleMoreCommand(cmd, row)">
                <i class="iconfont icon-more-br action-icon" title="更多" style="cursor:pointer"></i>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                    <el-dropdown-item
                      command="push"
                      :disabled="!row.isLocal && row.status !== ReimStatus.DRAFT"
                      :class="{ 'is-disabled-item': !row.isLocal && row.status !== ReimStatus.DRAFT }"
                    >手工推送</el-dropdown-item>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </span>
          </template>
        </el-table-column>

        <el-table-column label="报销单号" width="180">
          <template #default="{ row }">
            <span
              class="link-text"
              @click="handleViewDetail(row)"
            >
              {{ row.isLocal ? '草稿' : row.reimbursementNo }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="单据状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="单据类型" width="120">
          <template #default="{ row }">
            {{ row.businessTypeName || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="报销人" width="160">
          <template #default="{ row }">
            {{ getEmployeeDisplay(row) }}
          </template>
        </el-table-column>

        <el-table-column label="报销部门" width="180">
          <template #default="{ row }">
            {{ getDepartmentDisplay(row) }}
          </template>
        </el-table-column>

        <el-table-column label="费用归属公司" width="160">
          <template #default="{ row }">
            {{ truncateText(getCompanyDisplay(row), 15) }}
          </template>
        </el-table-column>

        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">
            {{ getBusinessTypeDisplay(row) }}
          </template>
        </el-table-column>

        <el-table-column label="报销标题" width="160">
          <template #default="{ row }">
            <span class="link-text" @click="handleViewDetail(row)">
              {{ truncateText(row.title || '未命名报销单', 15) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="报销事由" width="160">
          <template #default="{ row }">
            {{ truncateText(row.reason, 15) }}
          </template>
        </el-table-column>

        <el-table-column label="补助金额" width="120" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.subsidyTotalAmount) }}
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination Section -->
    <div class="pagination-section">
      <div class="page-controls">
        <span class="total-info">共{{ totalRecords }}条</span>
        <el-select
          :model-value="pageSize"
          @update:model-value="handleSizeChange"
          style="width: 110px"
        >
          <el-option :value="10" label="10条/页" />
          <el-option :value="20" label="20条/页" />
          <el-option :value="50" label="50条/页" />
        </el-select>
        <div class="page-nav">
          <el-button
            :disabled="currentPage <= 1"
            size="default"
            @click="handlePageChange(currentPage - 1)"
          >
            <i class="iconfont icon-zuo"></i>
          </el-button>
          <template v-for="page in totalPages" :key="page">
            <el-button
              v-if="page === 1 || page === totalPages || Math.abs(page - currentPage) <= 1"
              :type="page === currentPage ? 'primary' : 'default'"
              size="default"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </el-button>
            <span v-else-if="page === 2 || page === totalPages - 1">...</span>
          </template>
          <el-button
            :disabled="currentPage >= totalPages"
            size="default"
            @click="handlePageChange(currentPage + 1)"
          >
            <i class="iconfont icon-you-copy"></i>
          </el-button>
        </div>
        <div class="go-to-page">
          前往
          <el-input v-model="goToPage" size="default" style="width: 50px" @keyup.enter="handleGoToPage" />
          页
        </div>
      </div>
    </div>

    <ConfirmDialog
      :visible="deleteConfirmVisible"
      title="提示"
      message="确认删除？"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />

    <ConfirmDialog
      :visible="draftLimitAlertVisible"
      title="提示"
      :message="`草稿已达上限（${draftStore.MAX_DRAFTS}个），请先删除草稿`"
      :show-cancel="false"
      @confirm="closeDraftLimitAlert"
      @cancel="closeDraftLimitAlert"
    />
  </div>
</template>

<style scoped>
.list-container {
  padding: 16px;
  background: #fff;
  min-height: 100vh;
  min-width: 900px;
}

/* Filter — responsive grid */
.filter-section {
  background: #f5f7fa;
  padding: 20px 24px 16px;
  border-radius: 6px;
  margin-bottom: 18px;
}
.filter-wrap {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  column-gap: 50px;
  row-gap: 16px;
  align-items: center;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  height: 36px;
}
.filter-item label {
  white-space: nowrap;
  font-size: 13px;
  color: #333;
  flex-shrink: 0;
}
.filter-item :deep(.el-input),
.filter-item :deep(.el-select) {
  flex: 1;
  min-width: 0;
}
.filter-item :deep(.el-tree-select) {
  flex: 1;
  min-width: 0;
}
.filter-row {
  display: contents;
}
.filter-buttons {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  justify-content: flex-end;
  height: 36px;
}
.filter-wrap > .filter-row--line1 > .filter-item:nth-child(1) { grid-column: 1; grid-row: 1; }
.filter-wrap > .filter-row--line1 > .filter-item:nth-child(2) { grid-column: 2; grid-row: 1; }
.filter-wrap > .filter-row--line1 > .filter-item:nth-child(3) { grid-column: 3; grid-row: 1; }
.filter-wrap > .filter-row--line1 > .filter-item:nth-child(4) { grid-column: 4; grid-row: 1; }

.filter-wrap > .filter-row--line2 > .filter-item:nth-child(1) { grid-column: 1; grid-row: 2; }
.filter-wrap > .filter-row--line2 > .filter-item:nth-child(2) { grid-column: 2; grid-row: 2; }
.filter-wrap > .filter-row--line2 > .filter-item:nth-child(3) { grid-column: 3; grid-row: 2; }
.filter-wrap > .filter-row--line2 > .filter-buttons { grid-column: 4; grid-row: 2; }

/* Table */
.table-section {
  margin-bottom: 16px;
  overflow-x: auto;
}
.action-icons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}
.action-icons .action-icon {
  font-size: 18px;
  color: #409eff;
  cursor: pointer;
  transition: opacity 0.2s;
}
.action-icons .action-icon:hover {
  opacity: 0.7;
}
.action-icons .action-icon.disabled {
  color: #ccc;
  cursor: not-allowed;
}
.action-icons .action-icon.disabled:hover {
  opacity: 1;
}
.list-header-icon {
  font-size: 16px;
  color: #409eff;
  font-weight: normal;
  font-style: normal;
}
.link-text {
  color: #409eff;
  cursor: pointer;
}
.link-text:hover {
  text-decoration: underline;
}

:deep(.el-dropdown-menu__item.is-disabled-item),
:deep(.el-dropdown-menu__item.is-disabled-item:not(.is-disabled)) {
  color: #c0c4cc !important;
  cursor: not-allowed !important;
  background-color: #f5f7fa !important;
}

/* Pagination */
.pagination-section {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 0;
  flex-wrap: wrap;
  gap: 8px;
}
.total-info {
  font-size: 14px;
  color: #666;
}
.page-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.page-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.go-to-page {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

/* Buttons */
.btn-create {
  background: #fff;
  border: 1px solid #409eff;
  color: #409eff;
}
.btn-clear {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}
.btn-search {
  background: #1a3a5c;
  border-color: #1a3a5c;
}
</style>
