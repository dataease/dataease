<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes" :element="curComponent">
      <el-collapse-item
        :effect="themes"
        title="流媒体信息"
        name="streamLinks"
        v-if="curComponent && curComponent.streamMediaLinks"
      >
        <stream-media-links
          :link-info="curComponent.streamMediaLinks"
          :themes="themes"
        ></stream-media-links>
      </el-collapse-item>
    </CommonAttr>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { storeToRefs } from 'pinia'
import VideoLinks from '@/custom-component/de-video/VideoLinks.vue'
import StreamMediaLinks from '@/custom-component/de-stream-media/StreamMediaLinks.vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)
</script>
