<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import { ref } from 'vue'

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
  }
})
const handleCommand = (command: string | number | object) => {
  if (typeof command === 'object') return
  emit('handleCommand', command)
  if (['text', 'location', 'number', 'float'].includes(command as string)) {
    replaceType.value.handleClose()
    return
  }

  if (['copy', 'editor'].includes(command as string)) {
    translate.value.handleClose()
  }
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
      <Icon name="icon_more_outlined"></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="translate">
          <el-icon>
            <Icon name="icon_more_outlined"></Icon>
          </el-icon>
          转换为指标
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
                <Icon name="icon_more_outlined"></Icon>
              </el-icon>
              更换字段类型
            </div>
            <template #dropdown>
              <el-dropdown-item command="text">
                <el-icon>
                  <Icon name="icon_more_outlined"></Icon>
                </el-icon>
                文本
              </el-dropdown-item>
              <el-dropdown-menu class="time-col">
                <el-dropdown-item>
                  <el-dropdown
                    popper-class="menu-more_popper_three"
                    placement="bottom-start"
                    trigger="hover"
                    @command="handleCommand"
                  >
                    <div class="field-type">
                      <el-icon>
                        <Icon name="icon_more_outlined"></Icon>
                      </el-icon>
                      时间
                    </div>
                    <template #dropdown>
                      <el-dropdown-menu class="time-col">
                        <el-dropdown-item command="yy-mm">
                          <el-icon>
                            <Icon name="icon_more_outlined"></Icon>
                          </el-icon>
                          YY-MM
                        </el-dropdown-item>
                        <el-dropdown-item command="cmdk">
                          <el-icon>
                            <Icon name="icon_more_outlined"></Icon>
                          </el-icon>
                          YY-MM-HH
                        </el-dropdown-item>
                        <el-dropdown-item command="cmdk">
                          <el-icon>
                            <Icon name="icon_more_outlined"></Icon>
                          </el-icon>
                          YY-MM-HH mm
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </el-dropdown-item>
              </el-dropdown-menu>
              <el-dropdown-item command="location">
                <el-icon>
                  <Icon name="icon_more_outlined"></Icon>
                </el-icon>
                地理位置
              </el-dropdown-item>
              <el-dropdown-item command="number">
                <el-icon>
                  <Icon name="icon_more_outlined"></Icon>
                </el-icon>
                数值
              </el-dropdown-item>
              <el-dropdown-item command="float">
                <el-icon>
                  <Icon name="icon_more_outlined"></Icon>
                </el-icon>
                数值 (小数)
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item v-if="extField === 2" command="editor">
          <el-icon>
            <Icon name="icon_more_outlined"></Icon>
          </el-icon>
          编辑
        </el-dropdown-item>
        <el-dropdown-item command="copy">
          <el-icon>
            <Icon name="icon_more_outlined"></Icon>
          </el-icon>
          复制
        </el-dropdown-item>
        <el-dropdown-item command="delete">
          <el-icon>
            <Icon name="icon_more_outlined"></Icon>
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
  .el-popper__arrow {
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

    .el-dropdown {
      width: 100%;
    }
  }
}
</style>
