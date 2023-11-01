<template>
  <div style="overflow: hidden;width: 100%;height: 100%;">
    <img
      v-if="!showLink"
      :style="imageAdapter"
      :src="imgUrl"
    >
    <a
      v-if="showLink"
      :title="element.hyperlinks.content "
      :target="element.hyperlinks.openMode "
      :href="element.hyperlinks.content "
    >
      <img
        :style="imageAdapter"
        :src="imgUrl"
      >
    </a>
  </div>
</template>

<script>
import { imgUrlTrans } from '@/components/canvas/utils/utils'

export default {
  props: {
    element: {
      type: Object,
      require: true
    },
    editMode: {
      type: String,
      require: false,
      default: 'preview'
    }
  },
  computed: {
    imgUrl() {
      return imgUrlTrans(this.element.propValue)
    },
    showLink() {
      return this.editMode === 'preview' && this.element && this.element.hyperlinks && this.element.hyperlinks.enable
    },
    imageAdapter() {
      const style = {
        position: 'relative',
        width: '100%',
        height: '100%'
      }
      if (this.element.style.adaptation === 'original') {
        style.width = 'auto'
        style.height = 'auto'
      } else if (this.element.style.adaptation === 'equiratio') {
        style.height = 'auto'
      }
      return style
    }
  }
}
</script>

<style lang="scss" scoped>
.pic-main{
  overflow: hidden;
  width: 100%;
  height: 100%;
}
</style>
