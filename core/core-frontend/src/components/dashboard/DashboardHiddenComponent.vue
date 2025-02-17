<script setup lang="ts">
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import dvHidden from '@/assets/svg/dv-hidden.svg'
import { computed } from 'vue'
import Icon from '../icon-custom/src/Icon.vue'
import EmptyBackground from '../empty-background/src/EmptyBackground.vue'
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, canvasViewInfo, dvInfo } = storeToRefs(dvMainStore)
const emit = defineEmits(['cancelHidden'])
const componentsHidden = computed(() => {
  return componentData.value.filter(ele => ele.dashboardHidden)
})

const addToDashboard = item => {
  item.dashboardHidden = false
  emit('cancelHidden', item)
}
</script>

<template>
  <div class="config-hidden">
    <template v-if="componentsHidden?.length > 0">
      <div
        :style="{ height: '180px', width: '260px' }"
        class="wrapper-inner-adaptor"
        v-for="item in componentsHidden"
        :key="item.id"
      >
        <div class="component-outer">
          <ComponentWrapper
            canvas-id="canvas-main"
            :canvas-style-data="canvasStyleData"
            :dv-info="dvInfo"
            :canvas-view-info="canvasViewInfo"
            :view-info="canvasViewInfo[item.id]"
            :config="item"
            class="wrapper-design"
            show-position="viewDialog"
            :search-count="0"
            :scale="canvasStyleData.scale"
          />
        </div>
        <div class="select-to-dashboard" @click="addToDashboard(item)">
          <el-tooltip effect="dark" :content="$t('visualization.cancel_hidden')" placement="bottom">
            <el-icon style="font-size: 16px">
              <Icon name="dvHidden"><dvHidden class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
        </div>
      </div>
    </template>
    <template v-else>
      <empty-background
        :description="$t('visualization.no_hidden_components')"
        img-type="noneWhite"
      />
    </template>
  </div>
</template>

<style scoped lang="less">
.config-hidden {
  position: relative;
  padding: 16px 0;
  .wrapper-inner-adaptor {
    position: relative;
    margin-left: 8px;
    margin-bottom: 8px;
    float: left;
    background: #fff;
    padding: 4px;
    border-radius: 4px;
    border: 1px solid #dee0e3;
    &:nth-child(2n) {
      margin-right: -1px;
    }

    .select-to-dashboard {
      position: absolute;
      width: 16px;
      height: 16px;
      top: 4px;
      right: 8px;
      z-index: 24;
      cursor: pointer;
      &:hover {
        color: var(--ed-color-primary-99, #3370ff99);
      }
    }

    .component-outer {
      position: relative;
      width: 100%;
      height: 100%;
      .wrapper-design {
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
      }
    }

    &:hover {
      border-color: var(--ed-color-primary-99, #3370ff99);
    }
  }
}
</style>
