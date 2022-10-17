<template>
  <div>
    <div class="nav_calss">
      <el-select v-model="linkOpen" @change="linkChange" style="width: 100%;">
        <el-option v-for="(item,index) in jumpArr" :key="index" :value="item.jumpLink" :label="item.jumpName">
        </el-option>
      </el-select>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'DeJump',
  props: {
    propValue: {
      type: String,
      require: true
    },
    element: {
      type: Object
    },
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    jumpArr() {
      return this.element.options.jumpList
    }
  },
  data() {
    return {
      linkOpen: '',
    }
  },
  mounted() {
    console.log('de-jump',this.curComponent,this.element)
  },
  methods: {
    linkChange() {
      console.log(this.linkOpen)

      window.location.href = this.linkOpen
    }
  },
}
</script>
<style lang="scss">
.nav_calss{
  display:flex;
  height: 100%;
}
</style>