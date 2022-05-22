<!-- TODO: 这个页面后续将用 JSX 重构 -->
<template>
  <div class="attr-list">
    <el-form>
      <el-form-item v-for="(key, index) in styleKeys.filter(item => item != 'rotate')" :key="index" :label="map[key]">
        <el-color-picker v-if="key == 'borderColor'" v-model="curComponent.style[key]" />
        <el-color-picker v-else-if="key == 'color'" v-model="curComponent.style[key]" />
        <el-color-picker v-else-if="key == 'backgroundColor'" v-model="curComponent.style[key]" />
        <el-select v-else-if="selectKey.includes(key)" v-model="curComponent.style[key]">
          <template v-if="key == 'textAlign'">
            <el-option
              v-for="item in textAlignOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </template>
          <template v-else-if="key == 'borderStyle'">
            <el-option
              v-for="item in borderStyleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </template>
          <template v-else>
            <el-option
              v-for="item in verticalAlignOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </template>
        </el-select>
        <el-input v-else v-model="curComponent.style[key]" type="number" />
      </el-form-item>
      <el-form-item v-if="curComponent && !excludes.includes(curComponent.component)" :label="$t('panel.content')">
        <el-input v-model="curComponent.propValue" type="textarea" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      excludes: ['Picture', 'Group', 'user-view'], // 这些组件不显示内容
      textAlignOptions: [
        {
          label: this.$t('panel.text_align_left'),
          value: 'left'
        },
        {
          label: this.$t('panel.text_align_center'),
          value: 'center'
        },
        {
          label: this.$t('panel.text_align_right'),
          value: 'right'
        }
      ],
      borderStyleOptions: [
        {
          label: this.$t('panel.border_style_solid'),
          value: 'solid'
        },
        {
          label: this.$t('panel.border_style_dashed'),
          value: 'dashed'
        }
      ],
      verticalAlignOptions: [
        {
          label: this.$t('panel.vertical_align_top'),
          value: 'top'
        },
        {
          label: this.$t('panel.vertical_align_middle'),
          value: 'middle'
        },
        {
          label: this.$t('panel.vertical_align_bottom'),
          value: 'bottom'
        }
      ],
      selectKey: ['textAlign', 'borderStyle', 'verticalAlign'],
      map: {
        left: this.$t('panel.left'),
        top: this.$t('panel.top'),
        height: this.$t('panel.height'),
        width: this.$t('panel.width'),
        color: this.$t('panel.color'),
        backgroundColor: this.$t('panel.backgroundColor'),
        borderStyle: this.$t('panel.borderStyle'),
        borderWidth: this.$t('panel.borderWidth'),
        borderColor: this.$t('panel.borderColor'),
        borderRadius: this.$t('panel.borderRadius'),
        fontSize: this.$t('panel.fontSize'),
        fontWeight: this.$t('panel.fontWeight'),
        lineHeight: this.$t('panel.lineHeight'),
        letterSpacing: this.$t('panel.letterSpacing'),
        textAlign: this.$t('panel.textAlign'),
        opacity: this.$t('panel.opacity'),
        verticalAlign: this.$t('panel.verticalAlign')
      }
    }
  },
  computed: {
    styleKeys() {
      return this.$store.state.curComponent ? Object.keys(this.$store.state.curComponent.style) : []
    },
    curComponent() {
      return this.$store.state.curComponent
    }
  }
}
</script>

<style lang="scss" scoped>
  .attr-list {
    overflow: auto;
    padding: 20px;
    padding-top: 0;
    height: 100%;
  }
</style>
