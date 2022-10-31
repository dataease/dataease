<template>
  <div>
    <draggable
      v-model="sortList"
      group="drag"
      animation="300"
      class="drag-list"
    >
      <transition-group class="draggable-group">
        <span
          v-for="(item) in sortList"
          :key="item.name"
          class="item-dimension"
          :title="item.title"
        >
          <svg-icon
            icon-class="drag"
            class="item-icon"
          />
          <span class="item-span">
            {{ item.title }}
          </span>
        </span>
      </transition-group>
    </draggable>
  </div>
</template>

<script>

import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  name: 'CustomTabsSort',
  props: {
    element: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      sortList: []
    }
  },
  computed: {
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
      this.sortList = deepCopy(this.element.options.tabList)
    },
    save() {
      this.element.options.tabList = this.sortList
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
  padding: 2px;
  margin: 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: flex;
  align-items: center;
}

.item-icon{
  cursor: move;
  margin: 0 2px;
}

.item-span{
  display: inline-block;
  width: 100%;
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
