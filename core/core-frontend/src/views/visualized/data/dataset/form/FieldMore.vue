<script lang="ts" setup>
import custom_sort from '@/assets/svg/custom_sort.svg'
import dvRename from '@/assets/svg/dv-rename.svg'
import icon_calendar_outlined from '@/assets/svg/icon_calendar_outlined.svg'
import icon_copy_outlined from '@/assets/svg/icon_copy_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_local_outlined from '@/assets/svg/icon_local_outlined.svg'
import icon_number_outlined from '@/assets/svg/icon_number_outlined.svg'
import icon_url_outlined from '@/assets/svg/icon_url_outlined.svg'
import icon_switch_outlined from '@/assets/svg/icon_switch_outlined.svg'
import icon_text_outlined from '@/assets/svg/icon_text_outlined.svg'
import more_v from '@/assets/svg/more_v.svg'
import { ref, computed, nextTick } from 'vue'
import { ElCascaderPanel } from 'element-plus-secondary'
import { timeTypes } from './util'
import { fieldType } from '@/utils/attr'
import { useI18n } from '@/hooks/web/useI18n'

export interface Menu {
  svgName: string
  label?: string
  command: string
  divided?: boolean
}
const { t } = useI18n()

const props = defineProps({
  extField: {
    type: Number,
    default: 0
  },
  showTime: {
    type: Boolean,
    default: false
  },
  transType: {
    type: String,
    default: ''
  }
})
const timeTypesChildren = timeTypes.map(ele => {
  return {
    label: ele === 'custom' ? t('data_set.customize') : ele,
    value: ele
  }
})

const options = computed(() => {
  const optionArr = [
    {
      label: props.transType,
      value: 'translate',
      icon: icon_switch_outlined
    },
    {
      label: t('data_set.change_field_type'),
      value: 'translateType',
      icon: custom_sort,
      children: [
        {
          label: t('data_set.text'),
          icon: icon_text_outlined,
          value: 'text'
        },
        {
          label: t('data_set.time'),
          icon: icon_calendar_outlined,
          value: 'time',
          children: props.showTime ? timeTypesChildren : []
        },
        {
          label: t('data_set.geographical_location'),
          icon: icon_local_outlined,
          value: 'location'
        },
        {
          label: t('data_set.numerical_value'),
          icon: icon_number_outlined,
          value: 'value'
        },
        {
          label: t('data_set.numeric_value_decimal'),
          icon: icon_number_outlined,
          value: 'float'
        },
        {
          label: 'URL',
          icon: icon_url_outlined,
          value: 'url'
        }
      ]
    },
    {
      label: t('data_set.edit'),
      value: 'editor',
      icon: icon_edit_outlined
    },
    {
      label: t('data_set.rename'),
      value: 'rename',
      icon: dvRename
    },
    {
      label: t('data_set.copy'),
      value: 'copy',
      icon: icon_copy_outlined
    },
    {
      label: t('data_set.delete'),
      value: 'delete',
      icon: icon_deleteTrash_outlined
    }
  ]
  if (props.extField !== 2) {
    optionArr.splice(2, 1)
  }
  return optionArr
})
const deTypeArr = ref([])
const popover = ref()
const level = ref(1)
const cascaderPanel = ref()
const handleExpand = val => {
  level.value = val.left + 1
}
const emit = defineEmits(['handleCommand'])
const handleCommand = () => {
  const [translate, fieldType, timeType] = deTypeArr.value
  popover.value.hide()
  emit('handleCommand', timeType || fieldType || translate)
  nextTick(() => {
    deTypeArr.value = []
  })
}
const handleChange = () => {
  handleCommand()
}
</script>

<template>
  <el-popover
    :popper-class="
      options.length === 6 ? 'menu-more_popper_one menu-more_popper_six' : 'menu-more_popper_one'
    "
    :persistent="false"
    ref="popover"
    placement="right"
    :width="level * 175"
    trigger="click"
  >
    <template #reference>
      <el-icon class="menu-more">
        <Icon name="more_v"><more_v class="svg-icon" /></Icon>
      </el-icon>
    </template>
    <ElCascaderPanel
      v-model="deTypeArr"
      @expand-change="handleExpand"
      ref="cascaderPanel"
      :border="false"
      :options="options"
      @change="handleChange"
    >
      <template #default="{ data }">
        <div class="flex-align-center icon">
          <el-icon v-if="data.icon">
            <Icon
              ><component
                class="svg-icon"
                :class="
                  ['text', 'location', 'value', 'float', 'time', 'url'].includes(data.value) &&
                  `field-icon-${fieldType[['float', 'value'].includes(data.value) ? 2 : 0]}`
                "
                :is="data.icon"
              ></component
            ></Icon>
          </el-icon>
          <span>
            {{ data.label }}
          </span>
        </div>
      </template>
    </ElCascaderPanel>
  </el-popover>
</template>
<style lang="less" scoped>
.menu-more {
  cursor: pointer;
  height: 24px;
  width: 24px;
  border-radius: 4px;
  font-size: 16px;
  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}
</style>

<style lang="less">
.menu-more_popper_six > :first-child > :first-child > :first-child {
  height: 210px;
}
.menu-more_popper_one {
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;

  .ed-cascader-node.in-active-path {
    color: #1f2329;
    font-weight: 400;
  }
  .ed-cascader-panel {
    .ed-cascader-menu {
      border-radius: 4px;
      border: 1px solid #dee0e3;
      background: #fff;
      box-shadow: 0px 4px 8px 0px rgba(31, 35, 41, 0.1);
      border-right: none;
      &:nth-child(2) {
        margin-top: 32px;
        > div {
          height: 210px;
        }
      }
      &:nth-child(3) {
        margin-top: 64px;
      }
      .arrow-right {
        position: absolute;
        top: 50%;
        right: 11px;
        transform: translateY(-50%);
      }
    }
  }
}
</style>
