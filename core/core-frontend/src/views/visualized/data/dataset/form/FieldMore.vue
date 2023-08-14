<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import { ref } from 'vue'
import { timeTypes } from './util.js'

export interface Menu {
  svgName: string
  label?: string
  command: string
  divided?: boolean
}

defineProps({
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
const handleCommand = (command: string | number | object) => {
  if (typeof command === 'object') return
  if ('custom' === command) {
    replaceType.value.handleClose()
    translate.value.handleClose()
  }
  if (['text', 'time', 'location', 'number', 'float'].includes(command as string)) {
    replaceType.value.handleClose()
    setTimeout(() => {
      emit('handleCommand', command)
    }, 100)
    return
  }

  if (['copy', 'editor'].includes(command as string)) {
    translate.value.handleClose()
  }
  emit('handleCommand', command)
}

const replaceType = ref()
const translate = ref()

const emit = defineEmits(['handleCommand'])
</script>

<template>
  <el-dropdown
    popper-class="menu-more_popper_one"
    placement="bottom-start"
    ref="translate"
    :hide-on-click="false"
    trigger="click"
    @command="handleCommand"
  >
    <el-icon class="menu-more">
      <Icon name="more_v"></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="translate">
          <el-icon>
            <Icon name="icon_switch_outlined"></Icon>
          </el-icon>
          {{ transType }}
        </el-dropdown-item>
        <el-dropdown-item>
          <el-dropdown
            popper-class="menu-more_popper_two"
            placement="bottom-start"
            :hide-on-click="false"
            ref="replaceType"
            trigger="click"
            @command="handleCommand"
          >
            <div class="field-type">
              <el-icon>
                <Icon name="custom_sort"></Icon>
              </el-icon>
              更换字段类型
            </div>
            <template #dropdown>
              <el-dropdown-item command="text">
                <el-icon>
                  <Icon class-name="primary-color" name="icon_text_outlined"></Icon>
                </el-icon>
                文本
              </el-dropdown-item>
              <el-dropdown-menu class="time-col">
                <el-dropdown-item command="time">
                  <el-dropdown
                    popper-class="menu-more_popper_three"
                    placement="bottom-start"
                    trigger="hover"
                    v-if="showTime"
                    @command="handleCommand"
                  >
                    <div class="field-type">
                      <el-icon>
                        <Icon class-name="primary-color" name="icon_calendar_outlined"></Icon>
                      </el-icon>
                      时间
                    </div>
                    <template #dropdown>
                      <el-dropdown-menu class="time-col">
                        <template v-for="ele in timeTypes" :key="ele">
                          <el-dropdown-item :command="ele">
                            {{ ele === 'custom' ? '自定义' : ele }}
                          </el-dropdown-item>
                        </template>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <template v-else>
                    <el-icon>
                      <Icon class-name="primary-color" name="icon_calendar_outlined"></Icon>
                    </el-icon>
                    时间
                  </template>
                </el-dropdown-item>
              </el-dropdown-menu>
              <el-dropdown-item command="location">
                <el-icon>
                  <Icon class-name="primary-color" name="icon_local_outlined"></Icon>
                </el-icon>
                地理位置
              </el-dropdown-item>
              <el-dropdown-item command="number">
                <el-icon>
                  <Icon class-name="primary-color" name="icon_number_outlined"></Icon>
                </el-icon>
                数值
              </el-dropdown-item>
              <el-dropdown-item command="float">
                <el-icon>
                  <Icon class-name="primary-color" name="icon_number_outlined"></Icon>
                </el-icon>
                数值 (小数)
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item v-if="extField === 2" command="editor">
          <el-icon>
            <Icon name="icon_edit_outlined"></Icon>
          </el-icon>
          编辑
        </el-dropdown-item>
        <el-dropdown-item command="copy">
          <el-icon>
            <Icon name="icon_copy_outlined"></Icon>
          </el-icon>
          复制
        </el-dropdown-item>
        <el-dropdown-item command="delete">
          <el-icon>
            <Icon name="icon_delete-trash_outlined"></Icon>
          </el-icon>
          删除
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="less" scoped>
.menu-more {
  cursor: pointer;
  height: 24px;
  width: 24px;
  border-radius: 4px;
  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}
</style>
<style lang="less">
.menu-more_popper_one,
.menu-more_popper_two,
.menu-more_popper_three {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}
.menu-more_popper_one,
.menu-more_popper_two {
  .field-type {
    font-weight: 400;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    white-space: nowrap;
    padding: 5px 0;
    width: 100%;
    margin: 0;
    font-size: var(--el-font-size-base);
    color: var(--el-text-color-regular);
    cursor: pointer;
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.menu-more_popper_two,
.menu-more_popper_three {
  margin: -30px 0 0 105px !important;
  .time-col {
    padding: 0;

    .ed-dropdown {
      width: 100%;
    }
  }
}
</style>
