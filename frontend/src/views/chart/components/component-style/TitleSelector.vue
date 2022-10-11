<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="titleForm"
        :model="titleForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          v-show="showProperty('show')"
          :label="$t('chart.show')"
          class="form-item"
        >
          <el-checkbox
            v-model="titleForm.show"
            @change="changeTitleStyle('show')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="showProperty('show') && titleForm.show">
          <el-form-item
            v-show="showProperty('title')"
            v-if="!batchOptStatus"
            :label="$t('chart.title')"
            class="form-item"
          >
            <el-input
              v-model="titleForm.title"
              size="mini"
              :placeholder="$t('chart.title')"
              clearable
              @blur="changeTitleStyle('title')"
              @input="inputOnInput($event)"
            />
          </el-form-item>
          <el-form-item
            v-show="showProperty('fontFamily')"
            :label="$t('chart.font_family')"
            class="form-item"
          >
            <el-select
              v-model="titleForm.fontFamily"
              :placeholder="$t('chart.font_family')"
              @change="changeTitleStyle('fontFamily')"
            >
              <el-option
                v-for="option in fontFamily"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('fontSize')"
            :label="$t('chart.text_fontsize')"
            class="form-item"
          >
            <el-select
              v-model="titleForm.fontSize"
              :placeholder="$t('chart.text_fontsize')"
              size="mini"
              @change="changeTitleStyle('fontSize')"
            >
              <el-option
                v-for="option in fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('color')"
            :label="$t('chart.text_color')"
            class="form-item"
          >
            <el-color-picker
              v-model="titleForm.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeTitleStyle('color')"
            />
          </el-form-item>
          <el-form-item
            v-show="showProperty('hPosition')"
            :label="$t('chart.text_h_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="titleForm.hPosition"
              size="mini"
              @change="changeTitleStyle('hPosition')"
            >
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="showProperty('vPosition')"
            :label="$t('chart.text_v_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="titleForm.vPosition"
              size="mini"
              @change="changeTitleStyle('vPosition')"
            >
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="showProperty('isItalic') || showProperty('isBolder')"
            :label="$t('chart.text_style')"
            class="form-item"
          >
            <el-checkbox
              v-show="showProperty('isItalic')"
              v-model="titleForm.isItalic"
              @change="changeTitleStyle('isItalic')"
            >{{ $t('chart.italic') }}</el-checkbox>
            <el-checkbox
              v-show="showProperty('isBolder')"
              v-model="titleForm.isBolder"
              @change="changeTitleStyle('isBolder')"
            >{{ $t('chart.bolder') }}</el-checkbox>
          </el-form-item>
          <el-form-item
            v-show="showProperty('letterSpace')"
            :label="$t('chart.letter_space')"
            class="form-item"
          >
            <el-select
              v-model="titleForm.letterSpace"
              :placeholder="$t('chart.quota_letter_space')"
              @change="changeTitleStyle('letterSpace')"
            >
              <el-option
                v-for="option in fontLetterSpace"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('fontShadow')"
            :label="$t('chart.font_shadow')"
            class="form-item"
          >
            <el-checkbox
              v-model="titleForm.fontShadow"
              @change="changeTitleStyle('fontShadow')"
            >{{ $t('chart.font_shadow') }}</el-checkbox>
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { CHART_FONT_FAMILY, CHART_FONT_LETTER_SPACE, COLOR_PANEL, DEFAULT_TITLE_STYLE } from '../../chart/chart'
import { checkViewTitle } from '@/components/canvas/utils/utils'
import { mapState } from 'vuex'

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
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL,
      fontFamily: CHART_FONT_FAMILY,
      fontLetterSpace: CHART_FONT_LETTER_SPACE
    }
  },
  computed: {
    ...mapState([
      'batchOptStatus'
    ])
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

          this.titleForm.fontFamily = this.titleForm.fontFamily ? this.titleForm.fontFamily : DEFAULT_TITLE_STYLE.fontFamily
          this.titleForm.letterSpace = this.titleForm.letterSpace ? this.titleForm.letterSpace : DEFAULT_TITLE_STYLE.letterSpace
          this.titleForm.fontShadow = this.titleForm.fontShadow ? this.titleForm.fontShadow : DEFAULT_TITLE_STYLE.fontShadow
        }
        if (!this.batchOptStatus) {
          this.titleForm.title = this.chart.title
        }
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
    changeTitleStyle(modifyName) {
      if (!this.batchOptStatus) {
        if (this.titleForm.title.length < 1) {
          this.$error(this.$t('chart.title_cannot_empty'))
          this.titleForm.title = this.chart.title
          return
        }
        if (checkViewTitle('update', this.chart.id, this.titleForm.title)) {
          this.$error(this.$t('chart.title_repeat'))
          this.titleForm.title = this.chart.title
          return
        }
      }
      if (!this.titleForm.show) {
        this.isSetting = false
      }
      this.titleForm['modifyName'] = modifyName
      this.$emit('onTextChange', this.titleForm)
    },
    inputOnInput: function(e) {
      this.$forceUpdate()
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
