<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="filterForm"
        :model="filterForm"
        label-width="80px"
        size="mini"
      >
        <div>
          <el-form-item
            :label="$t('chart.text_h_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="filterForm.horizontal"
              size="mini"
              @change="themeChange('horizontal')"
            >
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button
                :disabled="filterForm.vertical === 'center'"
                label="center"
              >{{ $t('chart.text_pos_center') }}
              </el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('chart.text_v_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="filterForm.vertical"
              size="mini"
              @change="themeChange('vertical')"
            >
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button
                :disabled="filterForm.horizontal === 'center'"
                label="center"
              >{{ $t('chart.text_pos_center') }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('panel.title_color')"
            class="form-item"
          >
            <el-color-picker
              v-model="filterForm.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="themeChange('color')"
            />
          </el-form-item>
          <el-divider>{{ $t('panel.input_style') }}</el-divider>
          <el-row style="height: 40px;overflow: hidden;">
            <el-col
              :span="4"
              style="padding-left: 10px;padding-top: 8px"
            >
              {{ $t('panel.board') }}
            </el-col>
            <el-col
              :span="4"
              style="padding-top: 5px"
            >
              <el-color-picker
                v-model="filterForm.brColor"
                size="mini"
                class="color-picker-style"
                :predefine="predefineColors"
                @change="themeChange('brColor')"
              />
            </el-col>
            <el-col
              :span="4"
              style="padding-left: 10px;padding-top: 8px"
            >
              {{ $t('panel.text') }}
            </el-col>
            <el-col
              :span="4"
              style="padding-top: 5px"
            >
              <el-color-picker
                v-model="filterForm.wordColor"
                size="mini"
                class="color-picker-style"
                :predefine="predefineColors"
                @change="themeChange('wordColor')"
              />
            </el-col>
            <el-col
              :span="4"
              style="padding-left: 10px;padding-top: 8px"
            >
              {{ $t('panel.board_background') }}
            </el-col>
            <el-col
              :span="4"
              style="padding-top: 5px"
            >
              <el-color-picker
                v-model="filterForm.innerBgColor"
                size="mini"
                class="color-picker-style"
                :predefine="predefineColors"
                @change="themeChange('innerBgColor')"
              />
            </el-col>
          </el-row>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { adaptCurThemeFilterStyleAll } from '@/components/canvas/utils/style'
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  name: 'FilterStyleSelector',
  props: {},
  data() {
    return {
      filterForm: {},
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  created() {
    this.initForm()
    bus.$on('onThemeColorChange', this.initForm)
  },
  beforeDestroy() {
    bus.$off('onThemeColorChange', this.initForm)
  },
  mounted() {
  },
  methods: {
    initForm() {
      this.filterForm = this.canvasStyleData.chartInfo.filterStyle
    },
    themeChange(styleKey) {
      adaptCurThemeFilterStyleAll(styleKey)
      this.$store.commit('recordSnapshot')
    }
  }
}
</script>

<style scoped>
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

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.el-divider__text {
  font-size: 8px;
  font-weight: 400;
  color: rgb(144, 147, 153);
}
</style>
