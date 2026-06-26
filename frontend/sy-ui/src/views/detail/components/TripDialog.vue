<script setup lang="ts">
import { reactive, watch, ref } from 'vue'
import { useReferenceStore } from '@/stores/reference'
import { useReimbursementStore } from '@/stores/reimbursement'
import { useToastStore } from '@/stores/toast'
import { generateUUID } from '@/utils/idGenerator'
import type { TripFormData } from '@/types/trip'

const props = defineProps<{
  visible: boolean
  mode: 'add' | 'edit' | 'copy'
  tripData: TripFormData | null
}>()

const emit = defineEmits<{
  save: [trip: TripFormData]
  cancel: []
}>()

const refStore = useReferenceStore()
const store = useReimbursementStore()
const toastStore = useToastStore()

const form = reactive<TripFormData>({
  localId: '',
  travelerId: '', travelerName: '',
  departureCityId: '', departureCityName: '',
  arrivalCityId: '', arrivalCityName: '',
  departureDate: '', arrivalDate: '',
  description: '',
})

const dialogKey = ref(0)

function resetForm(data: TripFormData | null) {
  if (data) {
    form.localId = data.localId
    form.travelerId = data.travelerId || ''
    form.travelerName = data.travelerName || ''
    form.departureCityId = data.departureCityId || ''
    form.departureCityName = data.departureCityName || refStore.getCityByNo(data.departureCityId)?.cityName || ''
    form.arrivalCityId = data.arrivalCityId || ''
    form.arrivalCityName = data.arrivalCityName || refStore.getCityByNo(data.arrivalCityId)?.cityName || ''
    form.departureDate = data.departureDate || ''
    form.arrivalDate = data.arrivalDate || ''
    form.description = data.description || ''
  } else {
    form.localId = generateUUID()
    form.travelerId = ''
    form.travelerName = ''
    form.departureCityId = ''
    form.departureCityName = ''
    form.arrivalCityId = ''
    form.arrivalCityName = ''
    form.departureDate = ''
    form.arrivalDate = ''
    form.description = ''
  }
  dialogKey.value++
}

watch(() => props.visible, (val) => {
  if (val) resetForm(props.tripData)
})

function handleTravelerChange(val: string): void {
  const emp = refStore.getEmployeeById(val)
  form.travelerName = emp?.reimburserName || ''
}

function handleDepartureCityChange(val: string): void {
  const city = refStore.cities.find((c) => c.cityNo === val)
  form.departureCityName = city?.cityName || ''
}

function handleArrivalCityChange(val: string): void {
  const city = refStore.cities.find((c) => c.cityNo === val)
  form.arrivalCityName = city?.cityName || ''
}

function validate(): boolean {
  if (!form.travelerId) { toastStore.warning('请选择出行人'); return false }
  if (!form.departureCityId) { toastStore.warning('请选择出发城市'); return false }
  if (!form.arrivalCityId) { toastStore.warning('请选择到达城市'); return false }
  if (!form.departureDate) { toastStore.warning('请选择出发日期'); return false }
  if (!form.arrivalDate) { toastStore.warning('请选择到达日期'); return false }
  if (!form.description.trim()) { toastStore.warning('请输入行程说明'); return false }
  if (form.description.length > 500) { toastStore.warning('行程说明不能超过500字'); return false }
  if (new Date(form.arrivalDate).getTime() <= new Date(form.departureDate).getTime()) {
    toastStore.warning('到达时间必须晚于出发时间'); return false
  }
  if (store.checkTripOverlap(form.travelerId, form.departureDate, form.arrivalDate, form.localId)) {
    toastStore.warning('该出行人在此时段已有行程，不可重复'); return false
  }
  return true
}

function handleSave(): void {
  if (!validate()) return
  emit('save', { ...form })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'edit' ? '编辑行程' : '补录行程'"
    width="640px"
    :close-on-click-modal="false"
    @close="emit('cancel')"
  >
    <el-alert class="trip-alert" type="warning" :closable="false" show-icon>
      <template #default>
        <div style="margin-bottom:4px">仅可补录未从申请单带入或未产生费用的行程信息；</div>
        <div>跨天跨城行程填写说明：出发城市-到达城市：武汉-北京; 出发日期-到达日期：1号-5号; 1号~5号补助按北京匹配;</div>
      </template>
    </el-alert>

    <el-form :key="dialogKey" label-width="84px" class="trip-form">
      <el-form-item label="出行人" required>
        <el-select v-model="form.travelerId" @change="handleTravelerChange" placeholder="请选择出行人" style="width:100%">
          <el-option v-for="e in refStore.employees" :key="e.reimburserId" :label="e.reimburserName" :value="e.reimburserId" />
        </el-select>
      </el-form-item>

      <div class="form-row">
        <el-form-item label="出发城市" required class="form-col">
          <el-select v-model="form.departureCityId" @change="handleDepartureCityChange" placeholder="请选择" style="width:100%">
            <el-option v-for="c in refStore.cities" :key="c.cityNo" :label="c.cityName" :value="c.cityNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="到达城市" required class="form-col">
          <el-select v-model="form.arrivalCityId" @change="handleArrivalCityChange" placeholder="请选择" style="width:100%">
            <el-option v-for="c in refStore.cities" :key="c.cityNo" :label="c.cityName" :value="c.cityNo" />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="出发时间" required class="form-col">
          <el-date-picker
            v-model="form.departureDate"
            type="datetime"
            placeholder="选择出发时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="到达时间" required class="form-col">
          <el-date-picker
            v-model="form.arrivalDate"
            type="datetime"
            placeholder="选择到达时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
      </div>

      <el-form-item label="行程说明" required>
        <el-input v-model="form.description" type="textarea" placeholder="请输入行程说明" maxlength="500" show-word-limit :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-cancel" @click="emit('cancel')">取消</el-button>
        <el-button class="btn-confirm" type="primary" @click="handleSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.trip-alert {
  margin-bottom: 16px;
}
.trip-form {
  padding: 4px 0;
}
.form-row {
  display: flex;
  gap: 16px;
  width: 100%;
}
.form-col {
  flex: 1;
  min-width: 0;
}
.form-col :deep(.el-form-item__content) {
  flex: 1;
}
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
}
.btn-cancel {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  min-width: 88px;
}
.btn-cancel:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background: #ecf5ff;
}
.btn-confirm {
  background: #1a3a5c;
  border-color: #1a3a5c;
  color: #fff;
  min-width: 88px;
}
.btn-confirm:hover {
  background: #2c5282;
  border-color: #2c5282;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>
