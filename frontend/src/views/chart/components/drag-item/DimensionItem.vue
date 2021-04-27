<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<i class="el-icon-arrow-down el-icon--right" />
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
          <el-dropdown-item icon="el-icon-files" :command="beforeClickItem('filter')">
            <span>{{ $t('chart.filter') }}...</span>
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-edit-outline" divided :command="beforeClickItem('rename')">
            <span>{{ $t('chart.show_name_set') }}</span>
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
  name: 'DimensionItem',
  props: {
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
        case 'rename':
          this.showRename()
          break
        case 'remove':
          this.removeItem()
          break
        case 'filter':
          this.editFilter()
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
      this.$emit('onDimensionItemChange', this.item)
    },
    beforeSort(type) {
      return {
        type: type
      }
    },
    editFilter() {
      this.item.index = this.index
      this.$emit('editItemFilter', this.item)
    },
    showRename() {
      this.item.index = this.index
      this.item.renameType = 'dimension'
      this.$emit('onNameEdit', this.item)
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('onDimensionItemRemove', this.item)
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
    display: inline-block;
    border-radius: 4px;
    box-sizing: border-box;
    white-space: nowrap;
  }

  .item-axis:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  span {
    font-size: 12px;
  }
</style>
