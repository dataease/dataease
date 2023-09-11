<template>

  <el-popover
    placement="right"
    title=""
    width="150"
    trigger="click"
  >
    <i
      slot="reference"
      :title="$t('chart.layer_controller')"
      class="icon iconfont icon-xuanfu"
      style="margin-left: 4px;cursor: pointer;font-size: 14px;"
    />
    <div>
      <ul
        class="de-ul"
        style="padding: 0px !important;"
      >
        <li
          v-for="(node, i) in layerOption"
          :key="node.id"
          :index="i"
          class="de-sort-field-span"
          :class="currentSeriesId === node.id ? 'de-active-li': ''"
          @click="changeSeries(node)"
        >
          <span :title="node.name">{{ node.name }}</span>
        </li>
      </ul>

    </div>
  </el-popover>

</template>

<script>
import bus from '@/utils/bus'
export default {
  name: 'MapLayerController',
  props: {
    chart: {
      type: Object,
      required: true
    },
    seriesIdMap: {
      type: Object,
      default: () => {
        return {
          id: ''
        }
      }
    }
  },
  data() {
    return {
      currentSeriesId: null
    }
  },
  computed: {
    yaxis() {
      return JSON.parse(this.chart.yaxis)
    },
    layerOption() {
      return this.yaxis.map(this.buildOption)
    },
    customAttr() {
      const attr = JSON.parse(this.chart.customAttr)
      if (!attr.currentSeriesId || !this.layerOption.some(item => item.id === attr.currentSeriesId)) {
        attr.currentSeriesId = this.layerOption[0].id
      }
      return attr
    }

  },
  created() {
    this.init()
  },
  methods: {
    buildOption({ id, name }) {
      return ({ id, name })
    },
    changeSeries(node) {
      if (node.id === this.currentSeriesId) return
      this.currentSeriesId = node.id
      const param = {
        id: this.chart.id,
        seriesId: this.currentSeriesId
      }
      this.seriesIdMap.id = this.currentSeriesId
      bus.$emit('change-series-id', param)
    },

    init() {
      this.currentSeriesId = this.seriesIdMap?.id || this.customAttr.currentSeriesId
    }
  }
}
</script>

<style scoped lang="scss">

.de-ul li {
  margin: 5px 2px;
  cursor: pointer;

  &:hover {
    color: #409EFF;
    border-color: rgb(198, 226, 255);
    background-color: rgb(236, 245, 255);
  }

  &:before {
    content: "";
    width: 6px;
    height: 6px;
    display: inline-block;
    border-radius: 50%;
    vertical-align: middle;
    margin-right: 5px;
  }
}

.de-active-li {
  &:before {
    background: #409EFF;
  }
}
.de-sort-field-span {
  display: inline-flexbox;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  span {
    font-size: 12px;
  }
}
</style>
