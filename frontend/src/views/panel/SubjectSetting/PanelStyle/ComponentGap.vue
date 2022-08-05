<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="250"
        trigger="click"
      >
        <el-col>
          <el-radio v-model="panel.gap" label="yes" @change="onChangePanelStyle">{{ $t('panel.gap') }}</el-radio>
          <el-radio v-model="panel.gap" label="no" @change="onChangePanelStyle">{{ $t('panel.no_gap') }}</el-radio>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('panel.component_gap') }} <i
          class="el-icon-setting el-icon--right"
        /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  name: 'ComponentGap',
  props: {},
  data() {
    return {
      panel: null
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),

  created() {
    // 初始化赋值
    this.panel = this.canvasStyleData.panel
  },
  methods: {
    onChangePanelStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'onChangePanelStyle')
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
</style>
