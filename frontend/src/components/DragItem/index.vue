<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item icon="el-icon-delete" :command="beforeClickItem('remove')">
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
          <slot />
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
export default {
  name: 'DragItem',
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
        default:
          break
      }
    },
    beforeClickItem(type) {
      return {
        type: type
      }
    },
    showRename() {
      this.item.index = this.index
      this.item.renameType = 'dimension'
      this.$emit('onNameEdit', this.item)
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('closeItem', this.item)
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
