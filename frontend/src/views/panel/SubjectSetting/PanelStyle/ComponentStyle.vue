<template>
  <div style="width: 100%">
    <el-row>
      <el-col class="custom-item el-form-item">
        <el-checkbox v-model="componentStyleForm.backgroundColorSelect" style="margin-right: 10px;float: right" @change="themeChange('backgroundColorSelect')">
          <span style="font-size: 12px">{{ $t('panel.color') }}</span>
        </el-checkbox>
      </el-col>
      <el-col :span="10">
        <el-color-picker v-model="componentStyleForm.color" :disabled="!componentStyleForm.backgroundColorSelect" size="mini" class="color-picker-style" :predefine="predefineColors" @change="themeChange('color')" />
      </el-col>
    </el-row>
    <el-form ref="componentStyleForm" :model="componentStyleForm" label-width="70px" size="mini">
      <el-form-item :label="$t('panel.opacity')" class="form-item">
        <el-slider v-model="componentStyleForm.alpha" :disabled="!componentStyleForm.backgroundColorSelect" show-input :show-input-controls="false" input-size="mini" @change="themeChange('alpha')" />
      </el-form-item>
      <el-form-item :label="$t('panel.board_radio')" class="form-item">
        <el-slider v-model="componentStyleForm.borderRadius" show-input :show-input-controls="false" input-size="mini" @change="themeChange('borderRadius')" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import bus from '@/utils/bus'
export default {
  name: 'ComponentStyle',
  data() {
    return {
      predefineColors: COLOR_PANEL,
      componentStyleForm: {}
    }
  },
  computed: mapState([
    'canvasStyleData',
    'componentData'
  ]),
  created() {
    this.initForm()
    bus.$on('onThemeColorChange', this.initForm)
  },
  beforeDestroy() {
    bus.$off('onThemeColorChange', this.initForm)
  },
  methods: {
    initForm() {
      this.componentStyleForm = this.canvasStyleData.chartInfo.chartCommonStyle
    },
    themeChange(modifyName) {
      this.componentData.forEach((item, index) => {
        item.commonBackground[modifyName] = this.componentStyleForm[modifyName]
      })
      this.$store.commit('recordSnapshot')
    }
  }
}
</script>

<style scoped>

.form-item  ::v-deep .el-form-item__label{
  font-size: 12px;
}
.custom-item{
  width: 70px;
}
.custom-item-value{
  width: calc(100% - 70px);;
}

</style>
