<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="titleForm" :model="titleForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="titleForm.show" @change="changeTitleStyle('show')">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="titleForm.show">
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="titleForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTitleStyle('fontSize')">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="titleForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeTitleStyle('color')" />
          </el-form-item>
          <el-form-item :label="$t('chart.text_h_position')" class="form-item">
            <el-radio-group v-model="titleForm.hPosition" size="mini" @change="changeTitleStyle('hPosition')">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_v_position')" class="form-item">
            <el-radio-group v-model="titleForm.vPosition" size="mini" @change="changeTitleStyle('vPosition')">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_style')" class="form-item">
            <el-checkbox v-model="titleForm.isItalic" @change="changeTitleStyle('isItalic')">{{ $t('chart.italic') }}</el-checkbox>
            <el-checkbox v-model="titleForm.isBolder" @change="changeTitleStyle('isBolder')">{{ $t('chart.bolder') }}</el-checkbox>
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  name: 'TitleSelector',
  props: {
  },
  data() {
    return {
      titleForm: {},
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  created() {
    this.initForm()
    bus.$on('onThemeColorChange',this.initForm)
  },
  beforeDestroy() {
    bus.$off('onThemeColorChange',this.initForm)
  },
  mounted() {
    this.init()
  },
  methods: {
    initForm() {
      this.titleForm = this.canvasStyleData.chartInfo.chartTitle
    },
    init() {
      const arr = []
      for (let i = 10; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeTitleStyle(modifyName) {
      this.titleForm['modifyName'] = modifyName
      this.$emit('onTextChange', this.titleForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    }
  }
}
</script>

<style scoped>
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

.switch-style{
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
</style>
