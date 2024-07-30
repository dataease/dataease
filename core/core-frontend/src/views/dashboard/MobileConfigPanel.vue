<script lang="ts" setup>
import { ref, onMounted, unref, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import MobileBackgroundSelector from './MobileBackgroundSelector.vue'
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useEmbedded } from '@/store/modules/embedded'
import { canvasSave } from '@/utils/canvasUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { backCanvasData } from '@/utils/canvasUtils'
import { storeToRefs } from 'pinia'
import { debounce } from 'lodash-es'
import mobileHeader from '@/assets/img/mobile-header.png'

const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, canvasViewInfo, dvInfo } = storeToRefs(dvMainStore)
const mobileLoading = ref(true)
const mobileStyle = ref(null)
const emits = defineEmits(['pcMode'])
const snapshotStore = snapshotStoreWithOut()

const getComponentStyleDefault = () => {
  return {
    top: 0,
    left: 0,
    width: '190px',
    height: '190px'
  }
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
const embeddedStore = useEmbedded()

const iframeSrc = computed(() => {
  return embeddedStore.baseUrl
    ? `${embeddedStore.baseUrl}mobile.html#/panel`
    : './mobile.html#/panel'
})
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
        dvInfo: JSON.parse(JSON.stringify(unref(dvInfo))),
        isEmbedded: !!embeddedStore.baseUrl
      })
    )
  )
}

const componentDataNotInMobile = computed(() => {
  return componentData.value.filter(ele => !ele.inMobile)
})

const newWindow = ref()

const hanedleMessage = event => {
  if (
    event.data?.msgOrigin === 'de-fit2cloud' &&
    !!embeddedStore.token &&
    !!embeddedStore.baseUrl
  ) {
    const params = {
      embeddedToken: embeddedStore.token
    }
    params['de-embedded'] = true
    const contentWindow = newWindow.value.contentWindow
    contentWindow.postMessage(params, '*')
    return
  }
  if (event.data.type === 'panelInit') {
    loadCanvasData()
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

  if (['mobileSaveFromMobile', 'mobilePatchFromMobile'].includes(event.data.type)) {
    componentData.value.forEach(ele => {
      const com = event.data.value[ele.id]
      if (!!com) {
        const { x, y, sizeX, sizeY } = com
        ele.mx = x
        ele.my = y
        ele.mSizeX = sizeX
        ele.mSizeY = sizeY
        if (ele.component === 'DeTabs') {
          ele.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              const { x: tx, y: ty, sizeX: tSizeX, sizeY: tSizeY } = com.tab[tabComponent.id]
              tabComponent.mx = tx
              tabComponent.my = ty
              tabComponent.mSizeX = tSizeX
              tabComponent.mSizeY = tSizeY
            })
          })
        }
      }
    })
  }
  if (event.data.type === 'mobileSaveFromMobile') {
    saveCanvasWithCheckFromMobile()
  }

  if (event.data.type === 'mobilePatchFromMobile') {
    emits('pcMode')
  }
}

const saveCanvasWithCheckFromMobile = () => {
  snapshotStore.resetStyleChangeTimes()
  canvasSave(() => {
    ElMessage.success('保存成功')
  })
}
const loadCanvasData = () => {
  handleLoad()
  mobileLoading.value = false
}

const setMobileStyle = debounce(() => {
  const height = window.innerHeight
  if (height > 1032) {
    mobileStyle.value = {
      transform: 'translateY(-50%)'
    }
    return
  }
  const scale = height / 1032
  mobileStyle.value = {
    transform: `scale(${scale}) translateY(-50%)`,
    transformOrigin: '0 0'
  }
}, 100)
onMounted(() => {
  window.addEventListener('message', hanedleMessage)
  window.addEventListener('resize', setMobileStyle)
  dvMainStore.setMobileInPc(true)
  useEmitt({
    name: 'onMobileStatusChange',
    callback: ({ type, value }) => {
      mobileStatusChange(type, value)
    }
  })
  setMobileStyle()
})

onBeforeUnmount(() => {
  dvMainStore.setMobileInPc(false)
  window.removeEventListener('message', hanedleMessage)
  window.removeEventListener('resize', setMobileStyle)
})

const addToMobile = com => {
  if (mobileLoading.value) return
  com.inMobile = true
  changeTimes.value++
  mobileStatusChange('addToMobile', JSON.parse(JSON.stringify(unref(com))))
}

const changeTimes = ref(0)
const activeCollapse = ref('com')
const handleBack = () => {
  if (!changeTimes.value) {
    mobileStatusChange('mobilePatch', undefined)
    return
  }
  ElMessageBox.confirm('当前的更改尚未保存，确定退出吗？', {
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    setTimeout(() => {
      backCanvasData(dvInfo.value.id, 'dashboard', () => {
        changeTimes.value = 0
        emits('pcMode')
      })
    }, 100)
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
        <span class="open-mobile">开启移动端</span>
        <el-switch size="small" v-model="dvInfo.mobileLayout" />
        <span class="open-mobile-line"></span>
        <el-tooltip :offset="14" effect="dark" content="切换至PC端布局" placement="bottom">
          <el-icon @click="handleBack" class="switch-pc">
            <Icon name="icon_pc_outlined" />
          </el-icon>
        </el-tooltip>
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </div>
    <div class="mobile-canvas" :style="mobileStyle" v-if="mobileStyle">
      <div class="mobile-header">
        <img :src="mobileHeader" alt="" srcset="" />
      </div>
      <div class="config-panel-title">
        <el-icon>
          <Icon name="icon_left_outlined" />
        </el-icon>
        {{ dvInfo.name }}
      </div>
      <div class="config-panel-content" v-loading="mobileLoading">
        <iframe ref="newWindow" :src="iframeSrc" frameborder="0" width="375" />
      </div>
      <div class="config-panel-foot"></div>
    </div>
    <div class="mobile-com-list">
      <div class="config-mobile-sidebar">移动端配置</div>
      <el-tabs size="small" v-model="activeCollapse">
        <el-tab-pane label="可视化组件" name="com"> </el-tab-pane>
        <el-tab-pane label="样式" name="style"> </el-tab-pane>
      </el-tabs>
      <div class="config-mobile-tab">
        <MobileBackgroundSelector
          v-if="activeCollapse === 'style'"
          @styleChange="changeTimes++"
        ></MobileBackgroundSelector>
        <template v-else>
          <div
            :style="{ height: '198px', width: '198px' }"
            class="mobile-wrapper-inner-adaptor"
            v-for="item in componentDataNotInMobile"
            :key="item.id"
          >
            <div class="component-outer">
              <ComponentWrapper
                v-show="item.isShow"
                canvas-id="canvas-main"
                :canvas-style-data="canvasStyleData"
                :dv-info="dvInfo"
                :canvas-view-info="canvasViewInfo"
                :view-info="canvasViewInfo[item.id]"
                :config="item"
                :style="getComponentStyleDefault()"
                show-position="preview"
                :search-count="0"
                :scale="80"
              />
            </div>
            <div class="mobile-com-mask" @click="addToMobile(item)">
              <span v-show="item.component === 'DeStreamMedia'" style="color: #909399"
                >IOS可能无法显示</span
              >
            </div>
            <div class="pc-select-to-mobile" @click="addToMobile(item)" v-if="!mobileLoading"></div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.mobile-config-panel {
  height: 100vh;
  width: 100vw;
  position: relative;
  background: #f5f6f7;

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
      .switch-pc {
        &::after {
          content: '';
          border-radius: 4px;
          display: none;
          position: absolute;
          width: calc(100% + 10px);
          height: calc(100% + 10px);
          top: -5px;
          left: -5px;
        }

        &:hover {
          &::after {
            display: block;
            background: rgba(255, 255, 255, 0.1);
          }
        }
        position: relative;
      }
      .open-mobile-line {
        background: #ffffff4d;
        width: 1px;
        height: 18px;
        margin: 0 20px;
      }

      .open-mobile {
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        color: #ffffff99;
        margin-right: 8px;
      }
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
    height: 852px;
    overflow: hidden;
    background-size: 100% 100% !important;
    position: absolute;
    top: calc(50% + 32px);
    left: calc(50% - 419px);
    background-image: url(../../assets/img/mobile-bg-pc.png);
    padding: 0 22px;

    .mobile-header {
      margin-top: 20px;
      height: 43px;
      display: flex;
      img {
        height: 100%;
        width: 100%;
      }
    }

    .config-panel-title {
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      position: relative;
      border-top-right-radius: 4px;
      border-top-left-radius: 4px;

      .ed-icon {
        font-size: 20px;
        position: absolute;
        left: 16px;
        top: 12px;
      }
    }

    .config-panel-content {
      width: 100%;
      height: calc(100% - 127px);
      border-bottom-left-radius: 45px;
      border-bottom-right-radius: 45px;
      overflow: hidden;
      iframe {
        height: 100%;
      }
    }
  }

  .mobile-com-list {
    float: right;
    width: 420px;
    background-color: #fff;
    position: relative;
    border-left: rgba(31, 35, 41, 0.15) 1px solid;
    height: calc(100% - 64px);
    overflow-y: auto;
    :deep(.ed-collapse) {
      --ed-collapse-header-font-size: 12px;
      --ed-collapse-content-font-size: 12px;
    }

    & > :deep(.ed-tabs) {
      --ed-tabs-header-height: 36px;
      border-top: 1px solid #1f232926;
      position: sticky;
      top: 38px;
      left: 0;
      background: #fff;
      z-index: 25;
      .ed-tabs__header {
        padding-left: 8px;
        &::before {
          content: '';
          width: 8px;
          height: 1px;
          position: absolute;
          bottom: 0;
          left: 0;
          background: #1f232926;
        }
      }
    }

    :deep(.ed-tabs__item) {
      font-size: 12px;
    }

    :deep(.ed-tabs__item):not(.is-active) {
      color: #646a73;
    }

    .config-mobile-sidebar {
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      padding: 8px;
      position: sticky;
      top: 0;
      left: 0;
      background: #fff;
      z-index: 25;
    }

    .config-mobile-tab {
      padding: 16px 8px;
    }
    .mobile-wrapper-inner-adaptor {
      position: relative;
      margin-right: 8px;
      margin-bottom: 8px;
      float: left;
      background: #fff;
      padding: 4px;
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
        border-color: var(--ed-color-primary-99, #3370ff99);
      }
    }

    .mobile-com-mask {
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
      z-index: 10;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .pc-select-to-mobile {
      position: absolute;
      width: 16px;
      height: 16px;
      top: 12px;
      right: 12px;
      border: 2px solid #8f959e;
      border-radius: 4px;
      z-index: 24;
      cursor: pointer;
      &:hover {
        border-color: var(--ed-color-primary-99, #3370ff99);
      }
    }
  }
}
</style>

<style lang="less">
.mobile-config-panel {
  .title-container {
    width: 90% !important;
  }
}
</style>
