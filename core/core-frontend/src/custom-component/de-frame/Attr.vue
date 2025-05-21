<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes" :element="curComponent">
      <el-collapse-item
        :effect="themes"
        :title="t('visualization.link_info')"
        name="frameLinks"
        v-if="curComponent && curComponent.frameLinks && !mobileInPc"
      >
        <frame-links :frame-links="curComponent.frameLinks" :themes="themes"></frame-links>
      </el-collapse-item>
    </CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
import FrameLinks from '@/custom-component/de-frame/FrameLinks.vue'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, mobileInPc } = storeToRefs(dvMainStore)
const { t } = useI18n()
withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)
</script>
