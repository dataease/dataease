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
            @change="leftOnChange"
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
          prop="marginTop"
        >
          <el-input
            v-model="styleInfo.height"
            type="number"
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
    return {}
  },
  computed: {
    maxLeft() {
      return 1600 - this.styleInfo.width - this.componentGap
    },
    styleInfo() {
      return this.$store.state.curComponent.style
    },
    ...mapState([
      'componentGap'
    ])
  },
  watch: {},
  mounted() {
  },
  methods: {
    leftOnChange() {
      if (this.styleInfo.left > this.maxLeft) {
        this.styleInfo.left = this.maxLeft
      }
    }
  }
}
</script>

<style scoped>

</style>
