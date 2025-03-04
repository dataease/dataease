<template>
  <div>
    <el-dropdown :teleported="false" trigger="click" @visible-change="visibleChange">
      <input id="input" ref="trackButton" type="button" hidden />
      <template #dropdown>
        <div :class="{ 'data-mobile': isDataVMobile }">
          <el-dropdown-menu
            class="track-menu"
            :style="{ 'font-family': fontFamily }"
            :append-to-body="false"
          >
            <el-dropdown-item
              v-for="(item, key) in trackMenu"
              :key="key"
              @click="trackMenuClick(item)"
              ><span class="menu-item">{{ state.i18n_map[item] }}</span></el-dropdown-item
            >
          </el-dropdown-menu>
        </div>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const trackButton = ref(null)
const emits = defineEmits(['trackClick'])

const props = defineProps({
  trackMenu: {
    type: Array,
    required: true
  },
  isDataVMobile: {
    type: Boolean,
    required: false,
    default: false
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  }
})
const { trackMenu } = toRefs(props)
const state = reactive({
  i18n_map: {
    drill: t('visualization.drill'),
    linkage: t('visualization.linkage'),
    linkageAndDrill: t('visualization.linkage_and_drill'),
    jump: t('visualization.jump'),
    enlarge: t('visualization.enlarge')
  }
})
const visibleChange = isVisible => {
  const tooltips = document.querySelectorAll('.g2-tooltip')
  if (tooltips) {
    tooltips.forEach(tooltip => {
      if (isVisible) {
        // 当下拉菜单显示时，添加隐藏样式
        tooltip.classList.add('hidden-tooltip')
      } else {
        // 当下拉菜单隐藏时，移除隐藏样式
        tooltip.classList.remove('hidden-tooltip')
      }
    })
  }
}

const trackButtonClick = () => {
  setTimeout(() => {
    trackButton.value.click()
  }, 50)
}

const trackMenuClick = menu => {
  emits('trackClick', menu)
}

defineExpose({
  trackButtonClick
})
</script>

<style lang="less" scoped>
.menu-item {
  font-size: 12px;
}

:deep(.ed-dropdown__popper) {
  position: static !important;
}

.ed-popper[x-placement^='bottom'] .popper__arrow {
  display: none;
}

:deep(.ed-popper[x-placement^='bottom']) {
  margin-top: -80px !important;
}

.data-mobile {
  zoom: 0.3;
}
</style>
