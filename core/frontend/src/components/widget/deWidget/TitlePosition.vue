<template>

  <el-popover
    placement="right"
    trigger="click"
  >
    <el-col>
      <el-form
        ref="tabsStyleForm"
        :model="styleInfo"
        label-width="80px"
        size="mini"
        class="demo-form-inline"
      >
        <el-form-item
          :label="$t('chart.text_h_position')"
          class="form-item"
        >
          <el-radio-group
            v-model="styleInfo.horizontal"
            size="mini"
            @change="styleChange"
          >
            <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
            <el-radio-button
              :disabled="styleInfo.vertical === 'center' && elementType !== 'de-select-grid'"
              label="center"
            >{{ $t('chart.text_pos_center') }}
            </el-radio-button>
            <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="showVertical"
          :label="$t('chart.text_v_position')"
          class="form-item"
        >
          <el-radio-group
            v-model="styleInfo.vertical"
            size="mini"
            @change="styleChange"
          >
            <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
            <el-radio-button
              :disabled="styleInfo.horizontal === 'center'"
              label="center"
            >{{ $t('chart.text_pos_center') }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

      </el-form>
    </el-col>

    <i
      slot="reference"
      class="iconfont icon-tabs"
    />
  </el-popover>

</template>

<script>
export default {

  name: 'TitlePosition',
  props: {
    styleInfo: {
      type: Object,
      default: null
    },
    showVertical: {
      type: Boolean,
      default: false
    },
    elementType: {
      type: String,
      default: ''
    }
  },

  methods: {
    styleChange() {
      this.$store.commit('canvasChange')
    }
  }

}
</script>

<style lang="scss" scoped>

</style>
