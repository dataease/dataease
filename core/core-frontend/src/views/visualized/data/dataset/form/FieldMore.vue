<script lang="ts" setup>
import { ref, computed, nextTick } from 'vue'
import { ElCascaderPanel } from 'element-plus-secondary'
import { timeTypes } from './util'
import { fieldType } from '@/utils/attr'

export interface Menu {
  svgName: string
  label?: string
  command: string
  divided?: boolean
}

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
    label: ele === 'custom' ? '自定义' : ele,
    value: ele
  }
})
const options = computed(() => {
  const optionArr = [
    {
      label: props.transType,
      value: 'translate',
      icon: 'icon_switch_outlined'
    },
    {
      label: '更换字段类型',
      value: 'translateType',
      icon: 'custom_sort',
      children: [
        {
          label: '文本',
          icon: 'icon_text_outlined',
          value: 'text'
        },
        {
          label: '时间',
          icon: 'icon_calendar_outlined',
          value: 'time',
          children: props.showTime ? timeTypesChildren : []
        },
        {
          label: '地理位置',
          icon: 'icon_local_outlined',
          value: 'location'
        },
        {
          label: '数值',
          icon: 'icon_number_outlined',
          value: 'value'
        },
        {
          label: '数值 (小数)',
          icon: 'icon_number_outlined',
          value: 'float'
        }
      ]
    },
    {
      label: '编辑',
      value: 'editor',
      icon: 'icon_edit_outlined'
    },
    {
      label: '重命名',
      value: 'rename',
      icon: 'dv-rename'
    },
    {
      label: '复制',
      value: 'copy',
      icon: 'icon_copy_outlined'
    },
    {
      label: 'icon_delete-trash_outlined',
      value: 'delete',
      icon: '删除'
    }
  ]
  if (props.extField === 2) {
    optionArr.splice(3, 1)
  }
  return optionArr
})
const deTypeArr = ref([])
const popover = ref()
const level = ref(1)
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
    popper-class="menu-more_popper_one"
    show-arrow
    ref="popover"
    placement="right"
    :width="level * 175"
    trigger="click"
  >
    <template #reference>
      <el-icon class="menu-more">
        <Icon name="more_v"></Icon>
      </el-icon>
    </template>
    <ElCascaderPanel
      v-model="deTypeArr"
      @expand-change="handleExpand"
      :border="false"
      :options="options"
      @change="handleChange"
    >
      <template #default="{ data }">
        <div class="flex-align-center icon">
          <el-icon v-if="data.icon">
            <icon
              :className="
                ['text', 'location', 'value', 'float', 'time'].includes(data.value) &&
                `field-icon-${fieldType[['float', 'value'].includes(data.value) ? 2 : 0]}`
              "
              :name="data.icon"
            ></icon>
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
.menu-more_popper_one {
  padding-left: 0 !important;
  padding-right: 0 !important;
  .ed-cascader-menu__wrap.ed-scrollbar__wrap {
  }
}
</style>
