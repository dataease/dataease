<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'

export interface Menu {
  svgName: string
  label?: string
  command: string
  divided?: boolean
}
const handleCommand = (command: string | number | object) => {
  emit('handleCommand', command)
}

const emit = defineEmits(['handleCommand'])
</script>

<template>
  <el-dropdown
    :teleported="false"
    popper-class="menu-more_popper_one"
    placement="bottom-start"
    :hide-on-click="false"
    trigger="click"
    app
    @command="handleCommand"
  >
    <el-icon class="menu-more">
      <Icon name="icon_more_outlined"></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="cmd">
          <el-icon>
            <Icon name="icon_more_outlined"></Icon>
          </el-icon>
          转换为指标
        </el-dropdown-item>
        <el-dropdown-item command="cm">
          <el-dropdown
            popper-class="menu-more_popper_two"
            placement="bottom-start"
            :hide-on-click="false"
            trigger="click"
            @command="handleCommand"
          >
            <div class="field-type">
              <el-icon>
                <Icon name="icon_more_outlined"></Icon>
              </el-icon>
              转换为指标
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="cmdk">
                  <el-icon>
                    <Icon name="icon_more_outlined"></Icon>
                  </el-icon>
                  文本
                </el-dropdown-item>
              </el-dropdown-menu>
              <el-dropdown-menu>
                <el-dropdown-item command="cmdkp">
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
                      转换为指标
                    </div>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="cmdk">
                          <el-icon>
                            <Icon name="icon_more_outlined"></Icon>
                          </el-icon>
                          文本
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
}
</style>
