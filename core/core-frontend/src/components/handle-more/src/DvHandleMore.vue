<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import { propTypes } from '@/utils/propTypes'
import type { Placement } from 'element-plus-secondary'
import { ref, PropType } from 'vue'
import { XpackComponent } from '@/components/plugin'
export interface Menu {
  svgName?: string
  label?: string
  command: string
  divided?: boolean
  disabled?: boolean
  hidden?: boolean
}

const props = defineProps({
  menuList: {
    type: Array as PropType<Menu[]>
  },
  placement: {
    type: String as () => Placement,
    default: 'bottom-end'
  },
  iconName: propTypes.string.def('icon_more_outlined'),
  inTable: propTypes.bool.def(false),
  node: {
    type: Object,
    default() {
      return {}
    }
  },
  anyManage: propTypes.bool.def(false)
})

const shareComponent = ref(null)
const menus = ref([
  ...props.menuList.map(item => {
    if (!props.anyManage && (item.command === 'copy' || item.command === 'move')) {
      item.hidden = true
    }
    return item
  })
])
const handleCommand = (command: string | number | object) => {
  if (command === 'share') {
    shareComponent.value.invokeMethod({ methodName: 'execute' })
    return
  }
  emit('handleCommand', command)
}
const callBack = param => {
  if (props.node.leaf && props.node?.weight >= 7) {
    menus.value.splice(2, 0, param)
  }
}
const emit = defineEmits(['handleCommand'])
</script>

<template>
  <el-dropdown
    popper-class="menu-more-dv_popper"
    :placement="placement"
    trigger="click"
    @command="handleCommand"
  >
    <el-icon class="hover-icon" :class="inTable && 'hover-icon-in-table'" @click.stop>
      <Icon :name="iconName"></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          :divided="ele.divided"
          :command="ele.command"
          v-for="ele in menus"
          :key="ele.label"
          :disabled="ele.disabled"
          :class="{ 'de-hidden-drop-item': ele.hidden }"
        >
          <el-icon class="handle-icon" v-if="ele.svgName">
            <Icon :name="ele.svgName"></Icon>
          </el-icon>
          {{ ele.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  <XpackComponent
    ref="shareComponent"
    jsname="c2hhcmUtaGFuZGxlcg=="
    :resource-id="props.node.id"
    :weight="node.weight"
    @loaded="callBack"
  />
</template>

<style lang="less">
.de-hidden-drop-item {
  display: none;
}
.menu-more-dv_popper {
  width: 120px;
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}

.handle-icon {
  font-size: 16px;
}
</style>
