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
        <span
          v-for="(item) in sortList"
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
        </span>
      </transition-group>
    </draggable>
  </div>
</template>

<script>
import { linkMultFieldValues, multFieldValues } from '@/api/dataset/dataset'
import { getLinkToken, getToken } from '@/utils/auth'
import { mergeCustomSortOption } from '@/utils'
export default {
  name: 'FilterCustomSort',
  props: {
    fieldId: {
      type: String,
      default: null
    },

    customSortList: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      sortList: []
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    fieldId() {
      this.init()
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.fieldId.split(',') }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }

      method(param).then(res => {
        this.sortList = this.optionData(res.data)
      })
    },
    optionData(data) {
      if (!data) return null
      return mergeCustomSortOption(this.customSortList, data.filter(item => !!item))
    },
    onMove() {
    },
    onUpdate() {
      this.$emit('on-filter-sort-change', this.sortList)
    }
  }
}
</script>

<style scoped>
.drag-list {
  overflow: auto;
  height: 30vh;
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
