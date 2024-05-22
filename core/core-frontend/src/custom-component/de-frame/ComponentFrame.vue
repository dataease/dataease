<template>
  <el-row class="main-frame">
    <div v-if="element.frameLinks.src" class="main-frame">
      <iframe
        v-if="state.frameShow"
        :id="'iframe-' + element.id"
        :src="element.frameLinks.src"
        scrolling="auto"
        frameborder="0"
        class="main-frame main-de-iframe"
      />
      <div v-if="isEdit" class="frame-mask edit-mask">
        <span style="opacity: 1">
          <span style="font-weight: bold; color: lawngreen">{{
            t('visualization.edit_web_tips')
          }}</span>
        </span>
      </div>
      <!--Here are three 15px wide masks(left top right) for easy clicking on the display jump button-->
      <div v-if="isEdit" class="frame-mask preview-top-mask" />
      <div v-if="isEdit" class="frame-mask preview-right-mask" />
      <div v-if="isEdit" class="frame-mask preview-left-mask" />
      <div v-if="screenShot" class="frame-mask" />
    </div>
    <div v-else class="info-class">
      <span>{{ t('visualization.link_add_tips_pre') }}</span>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const { t } = useI18n()

const props = defineProps({
  propValue: {
    type: String,
    require: true
  },
  element: {
    type: Object
  },
  isEdit: {
    type: Boolean,
    require: false,
    default: true
  },
  active: {
    type: Boolean,
    require: false,
    default: false
  },
  screenShot: {
    type: Boolean,
    default: false
  }
})

const { propValue, element, isEdit, active, screenShot } = toRefs(props)

const state = reactive({
  pOption: {},
  frameShow: true
})

const frameLinksChange = () => {
  state.frameShow = false
  nextTick(() => {
    state.frameShow = true
  })
}

onMounted(() => {
  useEmitt({
    name: 'frameLinksChange-' + element.value.id,
    callback: function () {
      frameLinksChange()
    }
  })
})
</script>

<style lang="less" scoped>
.info-class {
  text-align: center;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  color: #9ea6b2;
}

.main-frame {
  height: 100%;
  width: 100%;
}

.frame-mask {
  display: flex;
  opacity: 0.5;
  position: absolute;
  top: 0px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-mask {
  left: 0px;
  background-color: #5c5e61;
  height: 100% !important;
  width: 100% !important;
}

.preview-top-mask {
  left: 0px;
  height: 15px !important;
  width: 100% !important;
}

.preview-right-mask {
  right: 0px;
  height: 100% !important;
  width: 15px !important;
}

.preview-left-mask {
  left: 0px;
  height: 100% !important;
  width: 15px !important;
}
</style>
