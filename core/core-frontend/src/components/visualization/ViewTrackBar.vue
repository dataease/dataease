<template>
  <div>
    <el-dropdown
      :id="'view-track-bar-' + chartId"
      :teleported="false"
      trigger="click"
      @visible-change="visibleChange"
    >
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
              @mousedown.stop
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
    enlarge: t('visualization.enlarge'),
    event_jump: t('visualization.jump'),
    event_download: t('visualization.download'),
    event_share: t('visualization.share'),
    event_fullScreen: t('visualization.fullscreen'),
    event_showHidden: t('visualization.pop_area'),
    event_refreshDataV: t('visualization.refresh'),
    event_refreshView: t('visualization.refresh_view')
  }
})
const visibleChange = () => {
  document.querySelectorAll('.g2-tooltip')?.forEach(tooltip => {
    if (tooltip.id?.includes(chartId.value)) {
      tooltip.classList.toggle('hidden-tooltip', true)
    }
  })
}
// 添加图表标识，用于区分不同图表的 tooltip
const chartId = ref(null)
const trackButtonClick = (id?: string) => {
  chartId.value = id
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
