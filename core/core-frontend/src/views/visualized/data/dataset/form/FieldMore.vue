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
      label: '删除',
      value: 'delete',
      icon: 'icon_delete-trash_outlined'
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
        <Icon name="more_v"></Icon>
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
