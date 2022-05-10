<template>
  <div>
    <draggable
      v-model="sortList"
      group="drag"
      animation="300"
      :move="onMove"
      class="drag-list"
      @update="onUpdate"
    >
      <transition-group class="draggable-group">
        <span v-for="(item,index) in sortList" :key="index" class="item-dimension" :title="item">
          {{ item }}
        </span>
      </transition-group>
    </draggable>
    <p style="margin-top: 10px;color:#F56C6C;font-size: 12px;">{{ $t('chart.custom_sort_tip') }}</p>
  </div>
</template>

<script>
import { customSort } from '@/views/chart/chart/util'

export default {
  name: 'CustomSortEdit',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      sortList: []
    }
  },
  watch: {
    chart() {
      this.init()
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      console.log(this.chart)
      const chart = JSON.parse(JSON.stringify(this.chart))
      let customSortData
      const res = []
      if (Object.prototype.toString.call(chart.customSort) === '[object Array]') {
        customSortData = JSON.parse(JSON.stringify(chart.customSort))
      } else {
        customSortData = JSON.parse(chart.customSort)
      }
      if (!customSortData || customSortData.length === 0) {
        if (chart && chart.data) {
          const data = chart.data.datas
          data.forEach(ele => {
            res.push(ele.field)
          })
        }
      } else {
        if (chart && chart.data) {
          const data = chart.data.datas
          const cus = customSort(customSortData, data)
          cus.forEach(ele => {
            res.push(ele.field)
          })
        }
      }
      // 字段去重
      this.sortList = res.filter(function(item, index, arr) {
        return res.indexOf(item, 0) === index
      })
    },
    onMove() {
    },
    onUpdate() {
      this.$emit('onSortChange', this.sortList)
    }
  }
}
</script>

<style scoped>
.drag-list {
  overflow: auto;
  height: 50vh;
}

.item-dimension {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 6px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
  cursor: pointer;
}
</style>
