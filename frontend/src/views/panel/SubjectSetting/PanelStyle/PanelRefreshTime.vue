<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="colorForm" label-width="110px" size="mini">
            <el-form-item :label="'视图加载提示'" class="form-item form-item-slider">
              <el-checkbox v-model="canvasStyleData.refreshViewLoading" @change="onChangePanelStyle" />
            </el-form-item>
            <el-form-item :label="'刷新时间单位'" class="form-item form-item-slider">
              <el-radio-group v-model="canvasStyleData.refreshUnit" @change="onChangePanelStyle">
                <el-radio label="second">秒</el-radio>
                <el-radio label="minute">分</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="'刷新时间频率'" class="form-item form-item-slider">
              <el-slider
                v-model="canvasStyleData.refreshTime"
                show-input
                :show-input-controls="false"
                input-size="mini"
                :min="1"
                :max="3600"
                @change="onChangePanelStyle"
              />
            </el-form-item>
          </el-form>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('panel.refresh_time') }} <i
          class="el-icon-setting el-icon--right"
        /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>

export default {
  name: 'PanelRefreshTime',
  props: {},
  computed: {
    canvasStyleData() {
      return this.$store.state.canvasStyleData
    }
  },
  created() {
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
</style>
