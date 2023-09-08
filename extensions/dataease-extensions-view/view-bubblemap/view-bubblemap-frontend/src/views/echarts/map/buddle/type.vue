<template>
    <el-radio :value="value" :label="value" border class="radio-style">
        <span :title="$t('plugin_view_buddle_map.type_title')">
            <!-- <svg-icon :icon-class="isDev ? svgName : dynamicSvgName" class="chart-icon" /> -->
            <svg-icon icon-class="buddle-map_mini" class="chart-icon" />
        </span>
        <p class="radio-label">{{$t('plugin_view_buddle_map.type_title')}}</p>
    </el-radio>
</template>

<script>
import messages from '@/de-base/lang/messages'
export default {
  data() {
    return {
      value: 'buddle-map'
    }
  },
  computed: {
    isDev() {
      return process.env.NODE_ENV === 'development'
    },
    dynamicSvgName() {
      return '/api/pluginCommon/staticInfo/' + this.value + '/svg'
    }
  },
  created() {
    this.$emit('on-add-languages', messages)
  }

}
</script>

<style lang="scss" scoped>
.radio-style {
  width: 80px;
  height: 60px;
  padding: 0;
}
.chart-icon{
  width: 80px;
  height: 40px;
}
.radio-label{
  display: block;
  position: absolute;
  bottom: 0;
  width: 100%;
  margin: 0;
  text-align: center;
  height: 20px;
  line-height: 20px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: pre;
  font-size: 12px;
}
</style>
