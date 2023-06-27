<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import { propTypes } from '@/utils/propTypes'
import type { Placement } from 'element-plus-secondary'
import { PropType } from 'vue'

export interface Menu {
  svgName?: string
  label?: string
  command: string
  divided?: boolean
}

defineProps({
  menuList: {
    type: Array as PropType<Menu[]>
  },
  placement: {
    type: String as () => Placement,
    default: 'bottom-end'
  },
  iconName: propTypes.string.def('icon_more_outlined')
})
const handleCommand = (command: string | number | object) => {
  emit('handleCommand', command)
}

const emit = defineEmits(['handleCommand'])
</script>

<template>
  <el-dropdown
    popper-class="menu-more_popper"
    :placement="placement"
    trigger="click"
    @command="handleCommand"
  >
    <el-icon class="hover-icon" @click.stop>
      <Icon :name="iconName"></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          :divided="ele.divided"
          :command="ele.command"
          v-for="ele in menuList"
          :key="ele.label"
        >
          <el-icon class="handle-icon" v-if="ele.svgName">
            <Icon :name="ele.svgName"></Icon>
          </el-icon>
          {{ ele.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="less">
.menu-more_popper {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}

.handle-icon {
  font-size: 20px;
}
</style>
