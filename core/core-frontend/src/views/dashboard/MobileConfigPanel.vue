<script lang="ts" setup>
import { ref, onMounted, unref, onBeforeUnmount, computed } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { getStyle } from '@/utils/style'
import findComponent from '@/utils/components'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, canvasViewInfo, dvInfo } = storeToRefs(dvMainStore)
const mobileLoading = ref(true)
const activeName = ref('hide')

const emits = defineEmits(['pcMode'])

const getComponentStyleDefault = style => {
  return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
}
const mobileStatusChange = (type, value) => {
  const iframe = document.querySelector('iframe')
  if (iframe) {
    iframe.contentWindow.postMessage(
      {
        type,
        value
      },
      '*'
    )
  }
}

const handleLoad = () => {
  mobileLoading.value = false
  console.log('dvInfo', JSON.parse(JSON.stringify(unref(dvInfo))))

  setTimeout(() => {
    mobileStatusChange(
      'panelInit',
      JSON.parse(
        JSON.stringify({
          componentData: JSON.parse(JSON.stringify(unref(componentData))),
          canvasStyleData: JSON.parse(JSON.stringify(unref(canvasStyleData))),
          canvasViewInfo: JSON.parse(JSON.stringify(unref(canvasViewInfo))),
          dvInfo: JSON.parse(JSON.stringify(unref(dvInfo)))
        })
      )
    )
  }, 800)
}

const componentDataNotInMobile = computed(() => {
  return componentData.value.filter(ele => !ele.inMobile)
})

onMounted(() => {
  dvMainStore.setMobileInPc(true)
  useEmitt({
    name: 'onMobileStatusChange',
    callback: (type, value) => {
      mobileStatusChange(type, value)
    }
  })
})

onBeforeUnmount(() => {
  dvMainStore.setMobileInPc(false)
})

const addToMobile = com => {
  com.inMobile = true
  mobileStatusChange('addToMobile', JSON.parse(JSON.stringify(unref(com))))
}
</script>

<template>
  <div class="mobile-config-panel">
    <div class="mobile-to-pc">
      <el-icon class="custom-el-icon back-icon" @click="emits('pcMode')">
        <Icon class="toolbar-icon" name="icon_left_outlined" />
      </el-icon>
    </div>
    <div class="mobile-canvas">
      <div class="config-panel-title">{{ dvInfo.name }}</div>
      <div class="config-panel-content" v-loading="mobileLoading">
        <iframe
          src="./mobile.html#/panel"
          frameborder="0"
          width="360"
          height="570"
          @load="handleLoad"
        />
      </div>
      <div class="config-panel-foot"></div>
    </div>
    <div class="mobile-com-list">
      <el-tabs v-model="activeName">
        <el-tab-pane label="隐藏的组件" name="hide" />
        <el-tab-pane label="样式设置" name="style" />
      </el-tabs>
      <template v-if="activeName === 'hide'">
        <div
          :style="{ height: '200px', width: '31%' }"
          class="mobile-wrapper-inner-adaptor"
          v-for="config in componentDataNotInMobile"
          :key="config.id"
        >
          <component
            :is="findComponent(config['component'])"
            ref="component"
            class="component"
            :view="canvasViewInfo[config.id]"
            :canvas-style-data="canvasStyleData"
            :dv-info="dvInfo"
            :dv-type="dvInfo.type"
            :canvas-view-info="canvasViewInfo"
            :prop-value="config?.propValue"
            :element="config"
            :request="config?.request"
            :style="getComponentStyleDefault(config?.style)"
            :linkage="config?.linkage"
            show-position="preview"
            :disabled="true"
            :is-edit="false"
          />
          <div class="mobile-com-mask"></div>

          <div class="pc-select-to-mobile" @click="addToMobile(config)"></div>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="less" scoped>
.mobile-config-panel {
  height: 100vh;
  width: 100vw;
  position: relative;
  .mobile-canvas {
    border-radius: 30px;
    width: 370px;
    min-height: 600px;
    max-height: 700px;
    overflow: hidden;
    background-color: #000;
    background-size: 100% 100% !important;
    padding: 5px;
    height: 777px;
    position: absolute;
    top: 100px;
    left: 100px;

    .config-panel-title {
      margin-top: 30px;
      background: #fff;
      text-align: center;
    }
  }

  .mobile-com-list {
    position: absolute;
    top: 100px;
    left: 500px;
    width: calc(100% - 600px);
    max-height: 700px;
    display: flex;
    flex-wrap: wrap;
    background: #f5f6f7;
    padding: 16px;
    padding-top: 0;

    :deep(.ed-tabs) {
      width: 100%;
      margin-bottom: 16px;
    }

    .mobile-wrapper-inner-adaptor {
      position: relative;
      margin-right: 24px;
      margin-bottom: 24px;
      background: #fff;
    }

    .mobile-com-mask {
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
      z-index: 10;
    }

    .pc-select-to-mobile {
      position: absolute;
      width: 16px;
      height: 16px;
      top: 0;
      right: 0;
      border: 2px solid #ccc;
      border-radius: 4px;
      z-index: 11;
      cursor: pointer;
    }
  }
}
</style>
