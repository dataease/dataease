<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col v-if="aidedDesign">
          <el-form ref="aidedForm" label-width="110px" size="mini">
            <el-form-item :label="'辅助网格'" class="form-item form-item-slider">
              <el-checkbox v-model="aidedDesign.showGrid" size="mini" @change="onChangePanelStyle" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">辅助设计 <i
          class="el-icon-setting el-icon--right"
        /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>

export default {
  name: 'PanelAidedDesign',
  props: {},
  computed: {
    aidedDesign() {
      return this.$store.state.canvasStyleData.aidedDesign
    }
  },
  created() {
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

  /*.form-item-slider ::v-deep .el-form-item__label {*/
  /*  font-size: 12px;*/
  /*  line-height: 38px;*/
  /*}*/

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
</style>
