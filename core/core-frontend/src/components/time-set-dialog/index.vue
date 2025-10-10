<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import dayjs from 'dayjs'

const { t } = useI18n()
const dialogFormVisible = ref(false)
const form = reactive({
  type: '',
  value: ''
})
const init = (type, value) => {
  dialogFormVisible.value = true
  form.type = type || 'year'
  form.value = type ? value : ''
}

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

const formatMap = {
  datetime: 'YYYY/MM/DD HH:mm:ss',
  date: 'YYYY/MM/DD',
  month: 'YYYY/MM',
  year: 'YYYY'
}

const beforeClose = () => {
  form.type = ''
  form.value = ''
  dialogFormVisible.value = false
}
const emits = defineEmits(['saveTime'])
const formatValue = (val: any) => {
  if (!val) return ''
  return dayjs(val).format(formatMap[form.type])
}
const confirm = () => {
  const value = form.value
  if (value) emits('saveTime', form.type, formatValue(form.value))
  beforeClose()
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    :before-close="beforeClose"
    v-model="dialogFormVisible"
    :title="$t('data_set.time')"
    width="600"
    append-to-body
  >
    <el-form label-position="top">
      <el-form-item :label="$t('v_query.time_granularity')">
        <el-select
          :placeholder="$t('v_query.the_time_granularity')"
          v-model="form.type"
          style="width: 58%"
        >
          <el-option
            v-for="ele in timeList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          />
        </el-select>
        <el-date-picker style="margin-left: auto" v-model="form.value" :type="form.type" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="beforeClose">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirm">
          {{ t('dataset.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped></style>
