<template>
  <span style="position: relative;display: inline-block;">
    <i
      class="el-icon-arrow-down el-icon-delete"
      style="position: absolute;top: 6px;right: 24px;color: #878d9f;cursor: pointer;z-index: 1;"
      @click="removeItem"
    />
    <el-dropdown
      trigger="click"
      size="mini"
      @command="clickItem"
    >
      <span class="el-dropdown-link">
        <el-tag
          size="small"
          class="item-axis"
          :type="tagType"
        >
          <span style="float: left">
            <svg-icon
              v-if="item.deType === 0"
              icon-class="field_text"
              class="field-icon-text"
            />
            <svg-icon
              v-if="item.deType === 1"
              icon-class="field_time"
              class="field-icon-time"
            />
            <svg-icon
              v-if="item.deType === 2 || item.deType === 3"
              icon-class="field_value"
              class="field-icon-value"
            />
            <svg-icon
              v-if="item.deType === 5"
              icon-class="field_location"
              class="field-icon-location"
            />
            <svg-icon
              v-if="item.sort === 'asc'"
              icon-class="sort-asc"
              class-name="field-icon-sort"
            />
            <svg-icon
              v-if="item.sort === 'desc'"
              icon-class="sort-desc"
              class-name="field-icon-sort"
            />
          </span>
          <span
            class="item-span-style"
            :title="item.name"
          >{{ item.name }}</span>
          <field-error-tips v-if="tagType === 'danger'" />
          <i
            class="el-icon-arrow-down el-icon--right"
            style="position: absolute;top: 6px;right: 10px;"
          />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            icon="el-icon-delete"
            :command="beforeClickItem('remove')"
          >
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
import { getItemType } from '@/views/chart/components/dragItem/utils'
import FieldErrorTips from '@/views/chart/components/dragItem/components/FieldErrorTips'
import bus from '@/utils/bus'

export default {
  name: 'DrillItem',
  components: { FieldErrorTips },
  props: {
    param: {
      type: Object,
      required: true
    },
    item: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      tagType: 'success'
    }
  },
  watch: {
    dimensionData: function() {
      this.getItemTagType()
    },
    quotaData: function() {
      this.getItemTagType()
    },
    item: function() {
      this.getItemTagType()
    }
  },
  mounted() {
    bus.$on('reset-change-table', this.getItemTagType)
  },
  beforeDestroy() {
    bus.$off('reset-change-table', this.getItemTagType)
  },
  methods: {
    clickItem(param) {
      if (!param) {
        return
      }
      switch (param.type) {
        case 'remove':
          this.removeItem()
          break
        default:
          break
      }
    },
    beforeClickItem(type) {
      return {
        type: type
      }
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('onDimensionItemRemove', this.item)
    },
    getItemTagType() {
      this.tagType = getItemType(this.dimensionData, this.quotaData, this.item)
    }
  }
}
</script>

<style scoped>
  .item-axis {
    padding: 1px 6px;
    margin: 0 3px 2px 3px;
    text-align: left;
    height: 24px;
    line-height: 22px;
    display: flex;
    border-radius: 4px;
    box-sizing: border-box;
    white-space: nowrap;
    width: 159px;
  }

  .item-axis:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  span {
    font-size: 12px;
  }

  .summary-span{
    margin-left: 4px;
    color: #878d9f;;
  }

  .inner-dropdown-menu{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%
  }

  .item-span-style{
    display: inline-block;
    width: 115px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
</style>
