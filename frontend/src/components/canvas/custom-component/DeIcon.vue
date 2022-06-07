<template>
  <div style="overflow: hidden;width: 100%;height: 100%;" class="icon_img">
    <img v-if="!showLink" :src="require('@/assets/icon_lib/'+element.propValue)">
    <a v-if="showLink" :title="element.hyperlinks.content " :target="element.hyperlinks.openMode " :href="element.hyperlinks.content ">
      <img :src="require('@/assets/icon_lib/'+element.propValue)">
    </a>
  </div>
</template>

<script>
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
    showLink() {
      return this.editMode === 'preview' && this.element && this.element.hyperlinks && this.element.hyperlinks.enable
    }
  },
  mounted(){
    console.log('DeIcon::::',this.element,this.editMode)
  }
}
</script>

<style lang="scss" scoped>
img {
    width: 100%;
    height: 100%;
}
// .icon_img {
//   width:100%;
//   height:100%;
//   display:flex;
//   align-items:center;
//   justify-content:center;
//   img {
//     // object-fit: cover;
//     width: auto;
//     height:auto;
//   }
// }
</style>
