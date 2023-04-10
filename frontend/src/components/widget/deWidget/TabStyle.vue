<template>

  <el-popover
    placement="right"
    trigger="click"
  >
    <el-form
      ref="tabsStyleForm"
      :model="styleInfo"
      size="small"
      class="de-form-item"
    >
      <el-form-item
        label="头部字体颜色"
        prop="headFrontColor"
      >
        <div
          class="picker-color-div"
          @click="triggerTheme('headFontColor')"
        >
          <el-input
            v-model="styleInfo.headFontColor"
            readonly
            class="theme-input"
          >
            <el-color-picker
              ref="headFontColorPicker"
              slot="prefix"
              v-model="styleInfo.headFontColor"
              class="theme-picker"
              @change="styleChange"
            />
          </el-input>
        </div>
      </el-form-item>
      <el-form-item
        label="头部字体选中颜色"
        prop="headFontActiveColor"
      >
        <div
          class="picker-color-div"
          @click="triggerTheme('headFontActiveColor')"
        >
          <el-input
            v-model="styleInfo.headFontActiveColor"
            readonly
            class="theme-input"
          >
            <el-color-picker
              ref="headFontActiveColorPicker"
              slot="prefix"
              v-model="styleInfo.headFontActiveColor"
              class="theme-picker"
              @change="styleChange"
            />
          </el-input>
        </div>
      </el-form-item>
      <el-form-item
        label="头部边框颜色"
        prop="headBorderColor"
      >
        <div
          class="picker-color-div"
          @click="triggerTheme('headBorderColor')"
        >
          <el-input
            v-model="styleInfo.headBorderColor"
            readonly
            class="theme-input"
          >
            <el-color-picker
              ref="headBorderColorPicker"
              slot="prefix"
              v-model="styleInfo.headBorderColor"
              class="theme-picker"
              @change="styleChange"
            />
          </el-input>
        </div>
      </el-form-item>
      <el-form-item
        label="头部边框选中颜色"
        prop="headBorderActiveColor"
      >
        <div
          class="picker-color-div"
          @click="triggerTheme('headBorderActiveColor')"
        >
          <el-input
            v-model="styleInfo.headBorderActiveColor"
            readonly
            class="theme-input"
          >
            <el-color-picker
              ref="headBorderActiveColorPicker"
              slot="prefix"
              v-model="styleInfo.headBorderActiveColor"
              class="theme-picker"
              @change="styleChange"
            />
          </el-input>
        </div>
      </el-form-item>
      <el-form-item :label="$t('detabs.head_position')">
        <el-radio-group
          v-model="styleInfo.headPosition"
          size="mini"
        >
          <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
          <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
          <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <i
      slot="reference"
      class="iconfont icon-tabs"
    />
  </el-popover>

</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'TabStyle',
  props: {
    styleInfo: {
      type: Object,
      default: null
    }
  },
  data() {
    return {}
  },
  computed: {

    ...mapState([
      'componentData',
      'curComponent'
    ])
  },
  methods: {
    triggerTheme(key) {
      const pickKey = key + 'Picker'
      const current = this.$refs[pickKey]
      current && (current.showPicker = true)
    },
    styleChange() {
      this.$store.commit('canvasChange')
    }
  }

}
</script>

<style lang="scss" scoped>
::v-deep.number-padding {
  .el-input__inner {
    padding-right: 0;
  }
  .el-input-group__append {
    padding: 0 10px;
  }
}
</style>
