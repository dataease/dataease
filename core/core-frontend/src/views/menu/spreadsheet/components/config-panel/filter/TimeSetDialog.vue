<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

const dialogVisible = ref(false)
const form = reactive({
  type: 'year',
  value: null as Date | string | null
})

const timeList = [
  {
    label: t('dynamic_time.year'),
    value: 'year'
  },
  {
    label: t('chart.y_M'),
    value: 'month'
  },
  {
    label: t('chart.y_M_d'),
    value: 'date'
  },
  {
    label: t('chart.y_M_d_H_m_s'),
    value: 'datetime'
  }
]

const pad = (num: number) => `${num}`.padStart(2, '0')

const formatValue = (val: Date | string | null) => {
  if (!val) return ''
  const date = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())
  const formatMap = {
    year: `${year}`,
    month: `${year}/${month}`,
    date: `${year}/${month}/${day}`,
    datetime: `${year}/${month}/${day} ${hour}:${minute}:${second}`
  }
  return formatMap[form.type] || ''
}

const init = (type?: string, value?: Date | string) => {
  dialogVisible.value = true
  form.type = type || 'year'
  form.value = value || null
}

const close = () => {
  form.type = 'year'
  form.value = null
  dialogVisible.value = false
}

const emits = defineEmits<{
  saveTime: [type: string, value: string]
}>()

const confirm = () => {
  const value = formatValue(form.value)
  if (value) {
    emits('saveTime', form.type, value)
  }
  close()
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :before-close="close"
    :title="$t('data_set.time')"
    width="600"
    append-to-body
  >
    <el-form label-position="top">
      <el-form-item :label="$t('v_query.time_granularity')">
        <el-select
          v-model="form.type"
          :placeholder="$t('v_query.the_time_granularity')"
          style="width: 58%"
        >
          <el-option
            v-for="ele in timeList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          />
        </el-select>
        <el-date-picker
          v-model="form.value"
          style="margin-left: auto"
          :type="form.type"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="close">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirm">
          {{ t('dataset.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>
