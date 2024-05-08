<template>
  <div>
    <el-row style="margin-bottom: 6px">
      <el-col>
        <el-input
          v-model="searchText"
          :placeholder="$t('commons.search')"
          size="mini"
        />
      </el-col>
    </el-row>
    <draggable
      v-model="sortList"
      group="drag"
      animation="300"
      class="drag-list"
      :disabled="searchText"
      @update="onUpdate"
    >
      <transition-group class="draggable-group">
        <span
          v-for="(item, index) in filterList"
          :key="item"
          class="item-dimension"
          :title="item"
        >
          <svg-icon
            icon-class="drag"
            class="item-icon"
          />
          <span class="item-span">
            {{ item }}
          </span>
          <span
            :title="$t('panel.to_top')"
            @click="moveToTop(index, item)"
          >
            <svg-icon
              icon-class="to-top"
              class="item-icon to-top"
            />
          </span>
        </span>
      </transition-group>
    </draggable>
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'

export default {
  name: 'CustomSortEdit',
  props: {
    chart: {
      type: Object,
      required: true
    },
    field: {
      type: Object,
      required: true
    },
    fieldType: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      sortList: [],
      searchText: ''
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    filterList() {
      if (!this.searchText) {
        return this.sortList
      }
      return this.sortList.filter(item => item?.includes(this.searchText))
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
      if (this.fieldType === 'drillFields') {
        post('/chart/view/getDrillFieldData/' + this.chart.id + '/' + this.panelInfo.id + '/' + this.field.id, {}).then(response => {
          this.sortList = response.data
          this.onUpdate()
        })
      } else {
        post('/chart/view/getFieldData/' + this.chart.id + '/' + this.panelInfo.id + '/' + this.field.id + '/' + this.fieldType, {}).then(response => {
          this.sortList = response.data
          this.onUpdate()
        })
      }
    },
    moveToTop(index, item) {
      let targetIndex = index
      if (this.searchText) {
        targetIndex = this.sortList.findIndex(i => i === item)
      }
      const [target] = this.sortList.splice(targetIndex, 1)
      this.sortList.unshift(target)
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
.item-dimension .to-top {
  display: none;
  cursor: pointer;
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
.item-dimension:hover .to-top {
  display: block;
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
  cursor: pointer;
}
</style>
