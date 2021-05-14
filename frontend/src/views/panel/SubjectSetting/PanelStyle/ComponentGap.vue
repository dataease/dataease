<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="250"
        trigger="click"
      >
        <el-col>
          <el-radio v-model="panelStyleForm.gap" label="yes" @change="onChangePanelStyle">{{ $t('panel.gap') }} </el-radio>
          <el-radio v-model="panelStyleForm.gap" label="no" @change="onChangePanelStyle">{{ $t('panel.no_gap') }}</el-radio>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('panel.component_gap') }} <i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_PANEL_STYLE } from '@/views/panel/panel'
export default {
  name: 'BackgroundSelector',
  props: {
  },
  data() {
    return {
      panelStyleForm: JSON.parse(JSON.stringify(DEFAULT_PANEL_STYLE))
    }
  },
  methods: {
    onChangePanelStyle() {
      this.$emit('onChangePanelStyle', this.panelStyleForm)
    }
  }
}
</script>

<style scoped>
  .avatar-uploader>>>.el-upload {
    width: 100px;
    height: 60px;
    line-height: 70px;
  }
  .avatar-uploader>>>.el-upload-list li{
    width: 100px !important;
    height: 60px !important;
  }
  .disabled>>>.el-upload--picture-card {
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
