<template>
  <span>
    <el-dropdown trigger="click" size="small">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item icon="el-icon-edit-outline">
            item1
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete">
            item2
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown></span>
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
    }
  },
  data() {
    return {}
  },
  mounted() {
  },
  methods: {}
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
