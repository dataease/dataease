<template>
  <span>
    <el-tag v-if="!hasDataPermission('manage',param.privileges)" size="small" class="item-axis" :type="item.groupType === 'q'?'success':''">
      <span style="float: left">
        <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
        <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
        <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
        <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
        <svg-icon v-if="item.sort === 'asc'" icon-class="sort-asc" class-name="field-icon-sort" />
        <svg-icon v-if="item.sort === 'desc'" icon-class="sort-desc" class-name="field-icon-sort" />
      </span>
      <span class="item-span-style" :title="item.name">{{ item.name }}</span>
    </el-tag>
    <el-dropdown v-else trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis" :type="item.groupType === 'q'?'success':''">
          <span style="float: left">
            <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
            <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
            <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
            <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
            <svg-icon v-if="item.sort === 'asc'" icon-class="sort-asc" class-name="field-icon-sort" />
            <svg-icon v-if="item.sort === 'desc'" icon-class="sort-desc" class-name="field-icon-sort" />
          </span>
          <span class="item-span-style" :title="item.name">{{ item.name }}</span>
          <i class="el-icon-arrow-down el-icon--right" style="position: absolute;top: 6px;right: 10px;" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="sort">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-sort" />
                  <span>{{ $t('chart.sort') }}</span>
                  <span class="summary-span">({{ $t('chart.'+item.sort) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSort('none')">{{ $t('chart.none') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('asc')">{{ $t('chart.asc') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('desc')">{{ $t('chart.desc') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete" divided :command="beforeClickItem('remove')">
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
export default {
  name: 'ChartDragItem',
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
    }
  },
  data() {
    return {
    }
  },
  mounted() {
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
    sort(param) {
      // console.log(param)
      this.item.sort = param.type
      this.$emit('onItemChange', this.item)
    },
    beforeSort(type) {
      return {
        type: type
      }
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('onItemRemove', this.item)
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
    width: 100px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
</style>
