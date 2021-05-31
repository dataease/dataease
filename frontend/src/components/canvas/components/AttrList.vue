<template>
  <div class="attr-list">
    <el-form>
      <el-form-item v-for="(key, index) in styleKeys.filter(item => (item != 'rotate' && item != 'borderWidth'))" :key="index" :label="map[key]">
        <el-color-picker v-if="key == 'borderColor'" v-model="curComponent.style[key]" />
        <el-color-picker v-else-if="key == 'color'" v-model="curComponent.style[key]" />
        <el-color-picker v-else-if="key == 'backgroundColor'" v-model="curComponent.style[key]" />
        <el-select v-else-if="key == 'textAlign'" v-model="curComponent.style[key]">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-input v-if="key ==='opacity'" v-model="curComponent.style[key]" type="number" :min="0" :step="0.1" :max="1" />
        <el-input v-else v-model="curComponent.style[key]" type="number" />
      </el-form-item>
      <!--      <el-form-item v-if="curComponent && !excludes.includes(curComponent.component)" label="内容">-->
      <!--        <el-input v-model="curComponent.propValue" type="textarea" />-->
      <!--      </el-form-item>-->
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      excludes: ['Picture', 'Group'], // 这些组件不显示内容
      options: [
        {
          label: this.$t('panel.aline_left'),
          value: 'left'
        },
        {
          label: this.$t('panel.aline_center'),
          value: 'center'
        },
        {
          label: this.$t('panel.aline_right'),
          value: 'right'
        }
      ],
      map: {
        left: this.$t('panel.left'),
        top: this.$t('panel.top'),
        height: this.$t('panel.height'),
        width: this.$t('panel.width'),
        color: this.$t('panel.color'),
        backgroundColor: this.$t('panel.backgroundColor'),
        borderWidth: this.$t('panel.borderWidth'),
        borderColor: this.$t('panel.borderColor'),
        borderRadius: this.$t('panel.borderRadius'),
        fontSize: this.$t('panel.fontSize'),
        fontWeight: this.$t('panel.fontWeight'),
        lineHeight: this.$t('panel.lineHeight'),
        letterSpacing: this.$t('panel.letterSpacing'),
        textAlign: this.$t('panel.textAlign'),
        opacity: this.$t('panel.opacity')
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
    padding: 0px;
    padding-top: 0;
    height: 100%;
}
</style>
