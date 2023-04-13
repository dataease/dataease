<template>
  <div
    v-if="loadFinish"
    class="chart-type"
  >
    <div
      v-for="(renderItem, category) in renderMap[chart.render]"
      :key="category"
    >
      <el-divider class="chart-type-divider">{{ $t(category) }}</el-divider>

      <div
        v-for="(container, i) in renderItem"
        :key="i"
        style="display: block;"
      >
        <div class="radio-row">
          <div
            v-for="(item, idx) in container"
            :key="idx"
            style="position: relative;display: block;"
          >
            <el-radio
              v-if="item.placeholder"
              value=""
              label=""
              disabled
              class="disabled-none-cursor"
            ><svg-icon
              icon-class=""
              class="chart-icon"
            /></el-radio>
            <plugin-com
              v-else-if="item.isPlugin"
              :component-name="item.value + '-type'"
            />
            <el-radio
              v-else
              :value="item.value"
              :label="item.value"
              border
              class="radio-style"
            >
              <span :title="$t(item.title)">
                <svg-icon
                  :icon-class="item.icon"
                  class="chart-icon"
                />
              </span>
              <p class="radio-label">{{ $t(item.title) }}</p>
            </el-radio>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { TYPE_CONFIGS } from '@/views/chart/chart/util'
import { pluginTypes } from '@/api/chart/chart'
import PluginCom from '@/views/system/plugin/PluginCom'
export default {
  name: 'ChartType',
  components: { PluginCom },
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      defaultTypes: TYPE_CONFIGS,
      renderMap: {},
      loadFinish: false
    }
  },
  created() {
    const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views'))
    if (plugins) {
      this.initTypes(plugins)
    } else {
      pluginTypes().then(res => {
        const plugins = res.data
        localStorage.setItem('plugin-views', JSON.stringify(plugins))
        this.initTypes(plugins)
      }).catch(e => {
        localStorage.setItem('plugin-views', null)
        this.initTypes([])
      })
    }
  },
  methods: {
    currentIsPlugin(type) {
      const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views')) || []
      return plugins.some(plugin => plugin.value === type)
    },
    initTypes(plugins) {
      plugins.forEach(plugin => {
        plugin.isPlugin = true
      })
      this.pluginTypes = [...this.defaultTypes, ...plugins]
      this.formatTypes()
      this.loadFinish = true
    },
    formatTypes() {
      this.pluginTypes.forEach(item => {
        const { render, category } = item
        this.renderMap[render] = this.renderMap[render] || {}
        const renderItem = this.renderMap[render]
        renderItem[category] = renderItem[category] || []

        const len = renderItem[category].length
        if (len === 0 || renderItem[category][len - 1].length === 3) {
          renderItem[category][len] = []
          renderItem[category][len].push(item)
        } else {
          renderItem[category][len - 1].push(item)
        }
      })

      // 填充占位符

      Object.keys(this.renderMap).forEach(key => {
        Object.keys(this.renderMap[key]).forEach(category => {
          this.renderMap[key][category].forEach(container => {
            const len = container.length
            let reduc = 3 - len
            while (reduc--) {
              container.push({ placeholder: true })
            }
          })
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.chart-type{
  >div{
    width: 100%;
  }
  padding: 4px;
  display: -webkit-flex;
  flex-wrap: wrap;
  justify-content: space-between;
  overflow-y: auto;
  width: 100%;
}

.chart-icon{
  width: 80px !important;
  height: 40px !important;
}

.el-radio{
  margin:5px;
}

.radio-row{
  width: 100%;
  display: flex;
  display: -webkit-flex;
  justify-content: space-between;
  flex-direction: row;
  flex-wrap: wrap;
}

.chart-type-divider{
  width: auto;
}

.el-divider--horizontal{
  margin: 30px 0 14px 0;
}

.el-divider__text{
  font-size: 12px;
  font-weight: 400;
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
