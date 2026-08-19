<script setup lang="ts">
import VanPopup from "vant/es/popup";
import VanDatePicker from 'vant/es/date-picker'
import VanTimePicker from 'vant/es/time-picker'
import VanPickerGroup from 'vant/es/picker-group'
import {showToast} from 'vant'
import 'vant/es/popup/style'
import 'vant/es/date-picker/style'
import 'vant/es/picker-group/style'
import 'vant/es/time-picker/style'
import 'vant/es/toast/style'
import {useI18n} from "@/hooks/web/useI18n";
import {computed, onMounted, ref, watch} from "vue";
import dayjs from "dayjs";

const {t} = useI18n()

const props = withDefaults(defineProps<{
  show: boolean,
  dateType?: string,
  modelValue?: Date,
  minDate?: Date,
  maxDate?: Date,
  lessThan?: Date,
  greatThan?: Date
}>(), {
  show: false,
  modelValue: () => new Date(),
  minDate: () => new Date('1970/1/1'),
  maxDate: () => new Date('2100/1/1')
})

const emits = defineEmits('update:modelValue', 'update:show', 'change')

const columnsType = computed(() => {
  switch (props.dateType){
    case 'year':
      return ['year']
    case 'month':
    case 'monthrange':
      return ['year', 'month']
    case 'date':
    case 'daterange':
    case 'datetime':
    case 'datetimerange':
    default:
      return ['year', 'month', 'day']
  }
})
const showTimePick = computed(() => {
  return ['datetime', 'datetimerange'].includes(props.dateType)
})

const _show = computed({
  get() {
    return props.show
  },
  set(v) {
    emits('update:show', v)
  }
})

function onCancel() {
  _show.value = false
}

const currentDate = computed({
  get() {
    const temp = dayjs(value.value);
    switch (props.dateType) {
      case "year":
        return [temp.year()]
      case "month":
      case "monthrange":
        return [temp.year(), temp.month() + 1]
      case "date":
      case "daterange":
      default:
        return [temp.year(), temp.month() + 1, temp.date()]
    }
  },
  set(v) {
    const temp = dayjs(value.value);
    switch (props.dateType) {
      case "year":
        value.value = temp
            .year(v[0])
            .toDate()
        break
      case "month":
      case "monthrange":
        value.value = temp
            .year(v[0])
            .month(v[1] - 1)
            .toDate()
        break
      case "date":
      case "daterange":
      default:
        value.value = temp
            .year(v[0])
            .month(v[1] - 1)
            .date(v[2])
            .toDate()
        break
    }
  }
})
const currentTime = computed({
  get() {
    const temp = dayjs(value.value);
    return [temp.hour(), temp.minute(), temp.second()]
  },
  set(v) {
    const temp = dayjs(value.value);
    value.value = temp
        .hour(v[0])
        .minute(v[1])
        .second(v[2])
        .toDate()
  }
})

const _value = ref()

const value = computed<Date>({
  get() {
    return _value.value
  },
  set(v) {
    _value.value = v
  }
})

function onConfirm() {
  if (props.lessThan) {
    if (props.lessThan.getTime() <= value.value.getTime()) {
      showToast({
        duration: 2000,
        message: '开始时间必须小于结束时间',
        position: 'bottom',
        className: 'de-mobile-checker-error'
      })
      return
    }
  }
  if (props.greatThan) {
    if (props.greatThan.getTime() >= value.value.getTime()) {
      showToast({
        duration: 2000,
        message: '结束时间必须大于开始时间',
        position: 'bottom',
        className: 'de-mobile-checker-error'
      })
      return
    }
  }
  emits('update:modelValue', value.value)
  emits('change', value.value)
  onCancel()
}

watch(_show, (value) => {
  if (value) {
    _value.value = props.modelValue ? props.modelValue : new Date()
  }
})

onMounted(() => {
  _value.value = props.modelValue ? props.modelValue : new Date()
})


</script>

<template>
  <van-popup
      position="bottom"
      destroy-on-close
      teleport="body"
      v-model:show="_show"
      z-index="4000"
  >
    <van-picker-group
        @confirm="onConfirm"
        @cancel="onCancel"
        v-if="showTimePick"
        :title="t('v_query.time_selection')"
        :tabs="[t('dataset.select_date'), t('dataset.select_time')]"
        :next-step-text="t('sync_datasource.next')"
    >
      <van-date-picker
          :min-date="minDate"
          :max-date="maxDate"
          :columns-type="columnsType"
          v-model="currentDate"
      />
      <van-time-picker :columns-type="['hour', 'minute', 'second']" v-model="currentTime"/>
    </van-picker-group>
    <van-date-picker
        :title="t('dataset.select_date')"
        :columns-type="columnsType"
        @confirm="onConfirm"
        @cancel="onCancel"
        :min-date="minDate"
        :max-date="maxDate"
        v-else
        v-model="currentDate"
    />
  </van-popup>
</template>

<style lang="less">
.de-mobile-checker-error {
  z-index: 4003 !important;
}
</style>
