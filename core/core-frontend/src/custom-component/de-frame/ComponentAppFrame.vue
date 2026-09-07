<template>
  <el-row class="main-frame">
    <div v-if="element.frameLinks.src" class="main-frame">
      <iframe
        v-if="state.frameShow"
        :id="'app-iframe-' + element.id"
        :srcdoc="srcDoc"
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
      <div v-if="screenShot" class="frame-mask" />
    </div>
    <div v-else class="info-class">
      <span>{{ t('visualization.link_add_tips_pre') }}</span>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'

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

const { element, isEdit, screenShot } = toRefs(props)

const state = reactive({
  frameShow: true
})

// 纯 JS 代码（如立即执行函数）需包裹成 script 才能执行；含 script 标签的直接放入
const buildBody = (src: string) => {
  if (/<script[\s>]/i.test(src)) {
    return src
  }
  return `<script>${src}<\/script>`
}

// 每个嵌入应用渲染在独立的 srcdoc iframe 中：相互隔离，删除组件即彻底销毁
const srcDoc = computed(() => {
  const src = element.value?.frameLinks?.src || ''
  return `<!DOCTYPE html><html><head><meta charset="utf-8"><style>html,body{margin:0;padding:0;height:100%;width:100%;overflow:auto;}.copilot{height:100%;width:100%;}</style></head><body><div class="copilot"></div>${buildBody(
    src
  )}</body></html>`
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
  position: absolute;
  top: 0px;
  z-index: 1;
  align-items: center;
  justify-content: center;
}

.edit-mask {
  left: 0px;
  background-color: rgba(92, 94, 97, 0.75);
  height: 100% !important;
  width: 100% !important;
}
</style>
