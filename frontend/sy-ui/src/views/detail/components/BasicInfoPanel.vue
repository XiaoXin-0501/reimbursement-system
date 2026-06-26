<script setup lang="ts">
import { computed } from 'vue'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useReferenceStore } from '@/stores/reference'
import { buildBusinessTypeTree } from '@/utils/businessTypeTree'

const store = useReimbursementStore()
const refStore = useReferenceStore()

const businessTypeTreeData = computed(() => buildBusinessTypeTree(refStore.businessTypes))

const treeProps = {
  value: 'businessTypeId',
  label: 'businessTypeName',
  children: 'children',
  checkStrictly: false,
}
</script>

<template>
  <div class="basic-info-panel" v-if="store.basicInfo">
    <div class="form-grid">
      <!-- Row 1: Title -->
      <div class="form-row-inline">
        <label class="form-label required">报销标题</label>
        <el-input
          :model-value="store.basicInfo.title"
          @update:model-value="store.updateBasicField('title', $event)"
          placeholder="请输入报销标题"
          maxlength="500"
          show-word-limit
          :disabled="store.isAudit"
        />
      </div>

      <!-- Row 2 & 3: Shared 3-column grid -->
      <div class="form-cols">
        <!-- Row 2 -->
        <label class="form-label required col-label">报销人</label>
        <el-select
          class="form-col-input"
          :model-value="store.basicInfo.reimburserId"
          @update:model-value="store.updateBasicField('reimburserId', $event)"
          placeholder="请选择报销人"
          :disabled="store.isAudit"
        >
          <el-option
            v-for="e in refStore.employees"
            :key="e.reimburserId"
            :label="e.reimburserName"
            :value="e.reimburserId"
          />
        </el-select>

        <label class="form-label required col-label">报销部门</label>
        <el-select
          class="form-col-input"
          :model-value="store.basicInfo.reimDepartmentId"
          @update:model-value="store.updateBasicField('reimDepartmentId', $event)"
          placeholder="请选择报销部门"
          :disabled="store.isAudit"
        >
          <el-option
            v-for="d in refStore.departments"
            :key="d.reimDepartmentId"
            :label="d.reimDepartmentName"
            :value="d.reimDepartmentId"
          />
        </el-select>

        <label class="form-label required col-label">费用归属公司</label>
        <el-select
          class="form-col-input"
          :model-value="store.basicInfo.reimCompanyId"
          @update:model-value="store.updateBasicField('reimCompanyId', $event)"
          placeholder="请选择费用归属公司"
          :disabled="store.isAudit"
        >
          <el-option
            v-for="c in refStore.companies"
            :key="c.reimCompanyId"
            :label="c.reimCompanyName"
            :value="c.reimCompanyId"
          />
        </el-select>

        <!-- Row 3: Business Type in column 1 -->
        <label class="form-label required col-label col-label-row2">业务类型</label>
        <el-tree-select
          class="form-col-input col-input-row2"
          :model-value="store.basicInfo.businessTypeId"
          @update:model-value="store.updateBasicField('businessTypeId', $event)"
          :data="businessTypeTreeData"
          :props="treeProps"
          placeholder="请选择业务类型"
          :disabled="store.isAudit"
          check-strictly
        />
      </div>

      <!-- Row 4: Reason -->
      <div class="form-row-inline">
        <label class="form-label required">出差事由</label>
        <el-input
          :model-value="store.basicInfo.reason"
          @update:model-value="store.updateBasicField('reason', $event)"
          type="textarea"
          placeholder="请输入出差事由"
          maxlength="500"
          show-word-limit
          :rows="4"
          :disabled="store.isAudit"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.basic-info-panel {
  padding: 0;
}
.form-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.form-row-inline {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  min-width: 90px;
  padding-top: 6px;
  text-align: right;
}
.form-label.required::before {
  content: '*';
  color: #f56c6c;
  margin-right: 2px;
}

/* 3-column shared grid for rows 2 & 3 */
.form-cols {
  display: grid;
  grid-template-columns: 90px 1fr 90px 1fr 90px 1fr;
  column-gap: 10px;
  row-gap: 14px;
  align-items: center;
}
.form-cols .col-label {
  padding-top: 0;
}
.form-cols .col-label-row2 {
  grid-row: 2;
}
.form-cols .col-input-row2 {
  grid-row: 2;
}
.form-cols > :nth-child(1) { grid-column: 1; grid-row: 1; }
.form-cols > :nth-child(2) { grid-column: 2; grid-row: 1; }
.form-cols > :nth-child(3) { grid-column: 3; grid-row: 1; }
.form-cols > :nth-child(4) { grid-column: 4; grid-row: 1; }
.form-cols > :nth-child(5) { grid-column: 5; grid-row: 1; }
.form-cols > :nth-child(6) { grid-column: 6; grid-row: 1; }
.form-cols > :nth-child(7) { grid-column: 1; }
.form-cols > :nth-child(8) { grid-column: 2; }

.form-col-input {
  width: 100%;
}
</style>
