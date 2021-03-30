<template>

  <div>
    <el-card class="filter-card-class">
      <div slot="header">
        <span>卡片名称</span>
      </div>
      <div style="position: relative;margin-left: 5px;" @dragstart="handleDragStart">
        <div
          v-for="(item, index) in componentList"
          :key="index"
          :data-id="item.id"
          draggable
          :data-index="index"
        >
          <span class="iconfont" :class="'icon-' + item.icon" />
          <span>{{ item.label }}</span>
        </div>
      </div>

    </el-card>
  </div>

</template>

<script>import componentList from '@/components/canvas/custom-component/component-list'
import { ApplicationContext } from '@/utils/ApplicationContext'
export default {
  name: 'FilterGroup',
  data() {
    return {
      componentList
    }
  },
  mounted() {
    const wid = ApplicationContext.getService('testWidget')
    console.log(wid)
  },

  methods: {
    handleDragStart(ev) {
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'other',
        id: ev.target.dataset.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
    }
  }
}
</script>

<style lang="scss" scoped>

  .item {
    font-size: 12px;
    width: 100px;
    height: 36px;
    position: relative;
    float: left;
    margin-top: 10px;
    margin-left: 10px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

  .box-card {
    width: 480px;
  }
</style>
