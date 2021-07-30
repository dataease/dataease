<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="titleForm" :model="titleForm" label-width="80px" size="mini" :disabled="!hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="titleForm.show" @change="changeTitleStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="titleForm.show">
          <el-form-item :label="$t('chart.title')" class="form-item">
            <el-input
              v-model="titleForm.title"
              size="mini"
              :placeholder="$t('chart.title')"
              clearable
              @blur="changeTitleStyle"
              @input="inputOnInput($event)"
            />
          </el-form-item>
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="titleForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTitleStyle">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="titleForm.color" class="color-picker-style" @change="changeTitleStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.text_h_position')" class="form-item">
            <el-radio-group v-model="titleForm.hPosition" size="mini" @change="changeTitleStyle">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-show="chart.type && !chart.type.includes('table')" :label="$t('chart.text_v_position')" class="form-item">
            <el-radio-group v-model="titleForm.vPosition" size="mini" @change="changeTitleStyle">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_style')" class="form-item">
            <el-checkbox v-model="titleForm.isItalic" @change="changeTitleStyle">{{ $t('chart.italic') }}</el-checkbox>
            <el-checkbox v-model="titleForm.isBolder" @change="changeTitleStyle">{{ $t('chart.bolder') }}</el-checkbox>
          </el-form-item>
        </div>
      </el-form>
    </el-col>
    <!--    <div style="width: 100%">-->
    <!--      <el-popover-->
    <!--        v-model="isSetting"-->
    <!--        placement="right"-->
    <!--        width="400"-->
    <!--        trigger="click"-->
    <!--      >-->
    <!--        <el-col>-->
    <!--          <el-form ref="titleForm" :model="titleForm" label-width="80px" size="mini">-->
    <!--            &lt;!&ndash;            <el-form-item :label="$t('chart.show')" class="form-item">&ndash;&gt;-->
    <!--            &lt;!&ndash;              <el-checkbox v-model="titleForm.show" @change="changeTitleStyle">{{ $t('chart.show') }}</el-checkbox>&ndash;&gt;-->
    <!--            &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--            <el-form-item :label="$t('chart.title')" class="form-item">-->
    <!--              <el-input-->
    <!--                v-model="titleForm.title"-->
    <!--                size="mini"-->
    <!--                :placeholder="$t('chart.title')"-->
    <!--                clearable-->
    <!--                @blur="changeTitleStyle"-->
    <!--                @input="inputOnInput($event)"-->
    <!--              />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">-->
    <!--              <el-select v-model="titleForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTitleStyle">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_color')" class="form-item">-->
    <!--              <colorPicker v-model="titleForm.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeTitleStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_h_position')" class="form-item">-->
    <!--              <el-radio-group v-model="titleForm.hPosition" size="mini" @change="changeTitleStyle">-->
    <!--                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>-->
    <!--                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>-->
    <!--                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item v-show="chart.type && !chart.type.includes('table')" :label="$t('chart.text_v_position')" class="form-item">-->
    <!--              <el-radio-group v-model="titleForm.vPosition" size="mini" @change="changeTitleStyle">-->
    <!--                <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>-->
    <!--                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>-->
    <!--                <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_style')" class="form-item">-->
    <!--              <el-checkbox v-model="titleForm.isItalic" @change="changeTitleStyle">{{ $t('chart.italic') }}</el-checkbox>-->
    <!--              <el-checkbox v-model="titleForm.isBolder" @change="changeTitleStyle">{{ $t('chart.bolder') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" size="mini" class="shape-item" :disabled="!titleForm.show || !hasDataPermission('manage',param.privileges)">-->
    <!--          {{ $t('chart.title') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--          <el-switch-->
    <!--            v-model="titleForm.show"-->
    <!--            :disabled="!hasDataPermission('manage',param.privileges)"-->
    <!--            class="switch-style"-->
    <!--            @click.stop.native-->
    <!--            @change="changeTitleStyle"-->
    <!--          />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_TITLE_STYLE } from '../../chart/chart'

export default {
  name: 'TitleSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
      fontSize: [],
      isSetting: false
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.text) {
          this.titleForm = customStyle.text
        }
        this.titleForm.title = this.chart.title
      }
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
    changeTitleStyle() {
      if (this.titleForm.title.length < 1) {
        this.$error(this.$t('chart.title_cannot_empty'))
        this.titleForm.title = this.chart.title
        return
      }
      if (!this.titleForm.show) {
        this.isSetting = false
      }
      this.$emit('onTextChange', this.titleForm)
    },
    inputOnInput: function(e) {
      this.$forceUpdate()
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
