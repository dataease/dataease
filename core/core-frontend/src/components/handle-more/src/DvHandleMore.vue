<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import { propTypes } from '@/utils/propTypes'
import type { Placement } from 'element-plus-secondary'
import { ref, PropType, computed } from 'vue'
import ShareHandler from '@/views/share/share/ShareHandler.vue'
import { useShareStoreWithOut } from '@/store/modules/share'
import { isDesktop } from '@/utils/ModelUtil'
import { useI18n } from '@/hooks/web/useI18n'
const shareStore = useShareStoreWithOut()
const { t } = useI18n()

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
  iconName: propTypes.string.def(''),
  inTable: propTypes.bool.def(false),
  resourceType: propTypes.string.def('dashboard'),
  node: {
    type: Object,
    default() {
      return {}
    }
  },
  anyManage: propTypes.bool.def(false)
})

const shareDisable = computed(() => {
  return shareStore.getShareDisable || isDesktop()
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
    // shareComponent.value.invokeMethod({ methodName: 'execute' })
    shareComponent.value.execute()
    return
  }
  emit('handleCommand', command)
}
const callBack = param => {
  if (shareDisable.value) {
    return
  }
  if (props.node.leaf && props.node?.weight >= 7) {
    menus.value.splice(0, 0, param)
  }
}
const emit = defineEmits(['handleCommand'])

const menuDisabledCheck = ele => {
  // do return
  return ele.disabled || (props.node.extraFlag1 === 0 && ['share', 'copy'].includes(ele.command))
}
</script>

<template>
  <el-dropdown
    popper-class="menu-more-dv_popper"
    :placement="placement"
    trigger="click"
    @command="handleCommand"
  >
    <el-icon class="hover-icon" :class="inTable && 'hover-icon-in-table'" @click.stop>
      <Icon><component class="svg-icon" :is="iconName || icon_more_outlined"></component></Icon>
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          :divided="ele.divided"
          :command="ele.command"
          v-for="ele in menus"
          :key="ele.label"
          :disabled="menuDisabledCheck(ele)"
          :class="{
            'de-hidden-drop-item':
              ele.hidden || (ele.command === 'cancelPublish' && node.extraFlag1 === 0)
          }"
        >
          <el-icon class="handle-icon" color="#646a73" size="16" v-if="ele.svgName">
            <Icon
              ><component
                class="svg-icon"
                :class="{ 'custom-disable': menuDisabledCheck(ele) }"
                :is="ele.svgName"
              ></component
            ></Icon>
          </el-icon>
          <el-tooltip
            class="box-item"
            effect="dark"
            :content="t('visualization.publish_tips2', [ele.label])"
            :disabled="!menuDisabledCheck(ele)"
            placement="top-start"
          >
            {{ ele.label }}
          </el-tooltip>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  <ShareHandler
    v-if="!shareDisable"
    ref="shareComponent"
    :resource-id="props.node.id"
    :resource-type="props.resourceType"
    :weight="node.weight"
    @loaded="callBack"
  />
</template>

<style lang="less">
.custom-disable {
  color: var(--ed-text-color-disabled) !important;
}
.de-hidden-drop-item {
  display: none !important;
}
.menu-more-dv_popper {
  min-width: 120px;
  margin-top: -2px !important;
}

.handle-icon {
  font-size: 16px;
  color: #646a73;
}
</style>
