<template>
  <div style="width: 100%;padding: 10px;">
    <el-col>
      <el-form
        label-width="40px"
        size="mini"
      >
        <el-form-item
          :label="$t('panel.space_top')"
          class="form-item"
          prop="marginTop"
        >
          <el-input
            v-model="styleInfo.top"
            type="number"
            :min="0"
            :max="maxTop"
            class="hide-icon-number"
          >
            <template slot="append">px</template>
          </el-input>
        </el-form-item>
        <el-form-item
          :label="$t('panel.space_left')"
          :min="0"
          class="form-item"
          prop="marginTop"
        >
          <el-input
            v-model="styleInfo.left"
            type="number"
            :min="0"
            :max="maxLeft"
            class="hide-icon-number"
          >
            <template slot="append">px</template>
          </el-input>
        </el-form-item>
        <el-form-item
          :label="$t('panel.space_width')"
          :min="0"
          class="form-item"
          prop="marginTop"
        >
          <el-input
            v-model="styleInfo.width"
            :min="0"
            :max="maxWidth"
            type="number"
            class="hide-icon-number"
          >
            <template slot="append">px</template>
          </el-input>
        </el-form-item>
        <el-form-item
          :label="$t('panel.space_height')"
          :min="0"
          class="form-item"
        >
          <el-input
            v-model="styleInfo.height"
            type="number"
            :min="0"
            :max="maxHeight"
            class="hide-icon-number"
          >
            <template slot="append">px</template>
          </el-input>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>

import { mapState } from 'vuex'

export default {
  name: 'PositionAdjust',
  props: {},
  data() {
    return {
      maxHeight: 2000,
      maxTop: 20000
    }
  },
  computed: {
    maxLeft() {
      return 1600 - this.styleInfo.width - this.componentGap
    },
    maxWidth() {
      return 1600 - this.styleInfo.left - this.componentGap
    },
    styleInfo() {
      return this.$store.state.curComponent.style
    },
    ...mapState([
      'componentGap'
    ])
  },
  watch: {
    'styleInfo.top': function() {
      this.topOnChange()
    },
    'styleInfo.left': function() {
      this.leftOnChange()
    },
    'styleInfo.width': function() {
      this.widthOnChange()
    },
    'styleInfo.height': function() {
      this.heightOnChange()
    }
  },
  mounted() {
  },
  methods: {
    leftOnChange() {
      if (this.styleInfo.left > this.maxLeft) {
        this.styleInfo.left = this.maxLeft
      } else if (this.styleInfo.left < 0) {
        this.styleInfo.left = 0
      }
      this.$store.commit('canvasChange')
    },
    widthOnChange() {
      if (this.styleInfo.width > this.maxWidth) {
        this.styleInfo.width = this.maxWidth
      } else if (this.styleInfo.width < 0) {
        this.styleInfo.left = 0
      }
      this.$store.commit('canvasChange')
    },
    heightOnChange() {
      if (this.styleInfo.height > this.maxHeight) {
        this.styleInfo.height = this.maxHeight
      } else if (this.styleInfo.height < 0) {
        this.styleInfo.height = 0
      }
      this.$store.commit('canvasChange')
    },
    topOnChange() {
      if (this.styleInfo.top > this.maxTop) {
        this.styleInfo.top = this.maxTop
      } else if (this.styleInfo.top < 0) {
        this.styleInfo.top = 0
      }
      this.$store.commit('canvasChange')
    }
  }
}
</script>

<style scoped>

</style>
