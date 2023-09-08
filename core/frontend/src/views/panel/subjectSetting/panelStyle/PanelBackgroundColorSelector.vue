<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <background-color-selector
          :chart="chart"
          @onChangeBackgroundForm="onChangeBackgroundForm"
        />
        <el-button
          slot="reference"
          size="mini"
          class="shape-item"
        >{{ $t('chart.background') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import BackgroundColorSelector from '@/views/chart/components/componentStyle/BackgroundColorSelector'

export default {
  components: { BackgroundColorSelector },
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  methods: {
    onChangeBackgroundForm(colorForm) {
      this.$emit('onChangeBackgroundForm', colorForm)
    }
  }

}
</script>

<style scoped>
  .avatar-uploader ::v-deep .el-upload {
    width: 100px;
    height: 60px;
    line-height: 70px;
  }
  .avatar-uploader ::v-deep .el-upload-list li{
    width: 100px !important;
    height: 60px !important;
  }
  .disabled ::v-deep .el-upload--picture-card {
    display: none;
  }
  .shape-item{
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .form-item-slider ::v-deep .el-form-item__label{
    font-size: 12px;
    line-height: 38px;
  }
  .form-item ::v-deep .el-form-item__label{
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
