<template>
  <div>
    <div style="width: 100%">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.color')" class="form-item">
              <colorPicker v-model="colorForm.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeBackgroundStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.alpha')" class="form-item">
              <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.background') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_BACKGROUND_COLOR } from '../../chart/chart'

export default {
  name: 'BackgroundColorSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      colorForm: JSON.parse(JSON.stringify(DEFAULT_BACKGROUND_COLOR))
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customStyle) {
          const customStyle = JSON.parse(chart.customStyle)
          if (customStyle.background) {
            this.colorForm = customStyle.background
          }
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    changeBackgroundStyle() {
      this.$emit('onChangeBackgroundForm', this.colorForm)
    }
  }
}
</script>

<style scoped lang="scss">
.shape-item{
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider>>>.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item>>>.el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{
    font-size: 12px
  }
  .el-form-item{
    margin-bottom: 6px;
  }
</style>
