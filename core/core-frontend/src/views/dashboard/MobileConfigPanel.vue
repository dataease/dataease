<script lang="ts" setup>
import { ref, onMounted, unref, onBeforeUnmount, computed } from 'vue'
import eventBus from '@/utils/eventBus'
import MobileBackgroundSelector from './MobileBackgroundSelector.vue'
import { ElMessageBox } from 'element-plus-secondary'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { getStyle } from '@/utils/style'
import findComponent from '@/utils/components'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, canvasViewInfo, dvInfo } = storeToRefs(dvMainStore)
const mobileLoading = ref(true)
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
  mobileStatusChange(
    'panelInit',
    JSON.parse(
      JSON.stringify({
        componentData: JSON.parse(
          JSON.stringify(unref(componentData.value.filter(ele => !!ele.inMobile)))
        ),
        canvasStyleData: JSON.parse(JSON.stringify(unref(canvasStyleData))),
        canvasViewInfo: JSON.parse(JSON.stringify(unref(canvasViewInfo))),
        dvInfo: JSON.parse(JSON.stringify(unref(dvInfo)))
      })
    )
  )
}

const componentDataNotInMobile = computed(() => {
  return componentData.value.filter(ele => !ele.inMobile)
})

const hanedleMessage = event => {
  if (event.data.type === 'panelInit') {
    mobileLoading.value = false
    handleLoad()
  }

  if (event.data.type === 'delFromMobile') {
    changeTimes.value++
    componentData.value.some(ele => {
      if (ele.id === event.data.value) {
        ele.inMobile = false
        return true
      }
      return false
    })
  }

  if (event.data.type === 'mobileSaveFromMobile') {
    componentData.value.forEach(ele => {
      const com = event.data.value[ele.id]
      if (!!com) {
        const { x, y, sizeX, sizeY } = com
        ele.mx = x
        ele.my = y
        ele.mSizeX = sizeX
        ele.mSizeY = sizeY
      }
    })

    eventBus.emit('saveFromMobile')
  }
}
onMounted(() => {
  window.addEventListener('message', hanedleMessage)
  dvMainStore.setMobileInPc(true)
  useEmitt({
    name: 'onMobileStatusChange',
    callback: ({ type, value }) => {
      mobileStatusChange(type, value)
    }
  })
})

onBeforeUnmount(() => {
  dvMainStore.setMobileInPc(false)
  window.removeEventListener('message', hanedleMessage)
})

const addToMobile = com => {
  com.inMobile = true
  changeTimes.value++
  mobileStatusChange('addToMobile', JSON.parse(JSON.stringify(unref(com))))
}

const changeTimes = ref(0)

const handleBack = () => {
  if (!changeTimes.value) {
    emits('pcMode')
    return
  }
  ElMessageBox.confirm('当前的更改尚未保存，确定退出吗？', {
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    emits('pcMode')
  })
}

const save = () => {
  changeTimes.value = 0
  mobileStatusChange('mobileSave', undefined)
}
</script>

<template>
  <div class="mobile-config-panel">
    <div class="top-bar">
      <div class="mobile-to-pc">
        <el-icon @click="handleBack">
          <Icon name="icon_left_outlined" />
        </el-icon>
        {{ dvInfo.name }}
      </div>
      <div class="mobile-save">
        <el-icon @click="handleBack">
          <Icon name="icon_pc_outlined" />
        </el-icon>
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </div>
    <div class="mobile-canvas">
      <div class="config-panel-title">
        <el-icon>
          <Icon name="icon_left_outlined" />
        </el-icon>
        {{ dvInfo.name }}
      </div>
      <div class="config-panel-content" v-loading="mobileLoading">
        <iframe src="./mobile.html#/panel" frameborder="0" width="375" height="640" />
      </div>
      <div class="config-panel-foot"></div>
    </div>
    <div class="mobile-com-list">
      <div class="config-mobile-sidebar">移动端配置</div>
      <el-collapse>
        <el-collapse-item title="样式设置" name="style" class="content-no-padding-bottom">
          <MobileBackgroundSelector @styleChange="changeTimes++"></MobileBackgroundSelector>
        </el-collapse-item>
        <el-collapse-item title="可视化组件" name="com" class="content-no-padding-bottom">
          <div
            :style="{ height: '198px', width: '198px' }"
            class="mobile-wrapper-inner-adaptor"
            v-for="config in componentDataNotInMobile"
            :key="config.id"
          >
            <div class="component-outer">
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
            </div>
            <div class="mobile-com-mask"></div>
            <div
              class="pc-select-to-mobile"
              v-if="!mobileLoading"
              @click="addToMobile(config)"
            ></div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<style lang="less" scoped>
.mobile-config-panel {
  height: 100vh;
  width: 100vw;
  position: relative;
  background: #f5f6f7;
  overflow-y: auto;

  .top-bar {
    position: sticky;
    top: 0;
    left: 0;
    z-index: 10;
    background: #050e21;
    box-shadow: 0px 2px 4px 0px #1f23291f;
    height: 64px;
    padding: 0 24px 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .mobile-save {
      display: flex;
      align-items: center;
      .ed-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 20px;
        color: #ffffff99;
      }
    }

    .mobile-to-pc {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 400;
      line-height: 24px;
      color: #ffffffe5;
      .ed-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 12px;
        color: #ffffffcc;
      }
    }
  }

  .mobile-canvas {
    border-radius: 30px;
    width: 419px;
    height: 854px;
    overflow: hidden;
    background-size: 100% 100% !important;
    height: 777px;
    position: absolute;
    top: 104px;
    left: calc(50% - 419px);
    background-image: url(../../assets/img/mobile-bg-pc.png);
    padding: 0 22px;

    .config-panel-title {
      margin-top: 64px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      position: relative;

      .ed-icon {
        font-size: 20px;
        position: absolute;
        left: 16px;
        top: 12px;
      }
    }

    .config-panel-content {
      width: 100%;
      border-bottom-left-radius: 45px;
      border-bottom-right-radius: 45px;
      overflow: hidden;
    }
  }

  .mobile-com-list {
    float: right;
    width: 420px;
    height: 100%;
    background-color: #fff;
    position: relative;
    border-left: rgba(31, 35, 41, 0.15) 1px solid;
    height: 100%;

    .config-mobile-sidebar {
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      padding: 8px;
    }
    .mobile-wrapper-inner-adaptor {
      position: relative;
      margin-right: 8px;
      margin-bottom: 8px;
      float: left;
      background: #fff;
      padding: 31px 4px 36px 4px;
      border-radius: 4px;
      border: 1px solid #dee0e3;
      &:nth-child(2n) {
        margin-right: -1px;
      }

      .component-outer {
        position: relative;
        width: 100%;
        height: 100%;
      }

      &:hover {
        border-color: var(--ed-color-primary);
      }
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
      top: 12px;
      right: 12px;
      border: 2px solid #8f959e;
      border-radius: 4px;
      z-index: 20;
      cursor: pointer;
    }
  }
}
</style>
