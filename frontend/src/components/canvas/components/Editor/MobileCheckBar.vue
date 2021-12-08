<template>
  <div class="bar-main">
    <div style="width: 18px">
      <!--      <svg-icon icon-class="field_text" class="el-icon-close" />-->
      <el-checkbox v-model="element.mobileSelected" @change="onChange" />
    </div>
  </div>
</template>

<script>

import { mapState } from 'vuex'
export default {
  props: {
    element: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      timer: null
    }
  },
  computed: {
    ...mapState([
      'pcComponentData',
      'pcComponentGap'
    ])
  },
  mounted() {
  },
  beforeDestroy() {
  },
  methods: {
    onChange() {
      if (this.element.mobileSelected) {
        this.element.style.width = 1600
        this.element.style.height = 300
        this.element.style.left = 0
        this.element.style.top = 0
        this.element.sizex = 6
        this.element.sizey = 4
        this.element.x = 1
        this.element.y = 1
        this.element.auxiliaryMatrix = true
        this.$store.commit('addComponent', { component: this.element })
      } else {
        this.deleteComponent()
      }
      // this.updateMobileSelected(this.element.id)
    },
    deleteComponent() {
      this.$emit('amRemoveItem')
      this.$store.commit('deleteComponent')
      this.$store.commit('setCurComponent', { component: null, index: null })
    },
    updateMobileSelected(id, mobileSelected) {
      this.pcComponentData.forEach(item => {
        if (item.id === id) {
          item.mobileSelected = mobileSelected
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main{
    position: absolute;
    float:right;
    z-index: 2;
    border-radius:2px;
    padding-left: 1px;
    padding-right: 1px;
    cursor:pointer!important;
    background-color: #0a7be0;
  }
  .bar-main i{
    color: white;
    float: right;
    margin-right: 3px;
  }

</style>
