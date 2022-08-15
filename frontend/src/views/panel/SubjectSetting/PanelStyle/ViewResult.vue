<template>
  <div>
    <div style="width: 100%;">
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
      this.$store.commit('canvasChange')
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

  .avatar-uploader ::v-deep .el-upload-list li {
    width: 100px !important;
    height: 60px !important;
  }

  .disabled ::v-deep .el-upload--picture-card {
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

  .form-item-slider ::v-deep .el-form-item__label {
    font-size: 12px;
    line-height: 38px;
  }

  .form-item ::v-deep .el-form-item__label {
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

  .radio-span ::v-deep .el-radio__label {
    margin-left: 4px;
  }

  .slider-area ::v-deep .el-slider__runway {
    display: none;
  }

  .result-count {
    width: 80px;
  }

</style>
