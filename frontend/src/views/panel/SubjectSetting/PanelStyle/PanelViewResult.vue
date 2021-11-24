<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-row>
          <el-col :span="16">
            <el-radio-group v-model="panel.resultMode" class="radio-span" size="mini" @change="onChangePanelStyle">
              <el-radio label="all"><span>{{ $t('panel.view') }}</span></el-radio>
              <el-radio label="custom">
                <span>{{ $t('panel.panel') }} </span>
              </el-radio>
            </el-radio-group>
          </el-col>
          <el-col :span="8" class="slider-area">
            <el-slider
              v-model="panel.resultCount"
              :disabled="panel.resultMode==='all'"
              style="margin-left: 5px"
              show-input
              :show-input-controls="false"
              :show-tooltip="false"
              input-size="mini"
              :min="1"
              :max="10000"
              @change="onChangePanelStyle"
            />
          </el-col>
        </el-row>
        <el-row>
          <span style="color: #909399; font-size: 8px;margin-left: 3px">
            Tips: {{ $t('panel.panel_view_result_tips') }}
          </span>
        </el-row>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('panel.panel_view_result_show') }}<i
          class="el-icon-setting el-icon--right"
        /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PanelViewResult',
  props: {},
  data() {
    return {
      panel: null
    }
  },
  computed: {
    canvasStyleData() {
      return this.$store.state.canvasStyleData
    }
  },

  created() {
    // 初始化赋值
    this.panel = this.canvasStyleData.panel
  },
  methods: {
    onChangePanelStyle() {
      this.$store.state.styleChangeTimes++
    }
  }
}
</script>

<style scoped>
  .avatar-uploader >>> .el-upload {
    width: 100px;
    height: 60px;
    line-height: 70px;
  }

  .avatar-uploader >>> .el-upload-list li {
    width: 100px !important;
    height: 60px !important;
  }

  .disabled >>> .el-upload--picture-card {
    display: none;
  }

  .shape-item {
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .form-item-slider >>> .el-form-item__label {
    font-size: 12px;
    line-height: 38px;
  }

  .form-item >>> .el-form-item__label {
    font-size: 12px;
  }

  .el-select-dropdown__item {
    padding: 0 20px;
  }

  span {
    font-size: 12px
  }

  .el-form-item {
    margin-bottom: 6px;
  }

  .radio-span {
    margin-top: 10px;
  }

  .radio-span >>> .el-radio__label {
    margin-left: 4px;
  }

  .slider-area >>> .el-slider__runway {
    display: none;
  }

  .result-count {
    width: 80px;
  }

</style>
