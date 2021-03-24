<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickItem('rename')">
            <span>{{ $t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete" divided :command="beforeClickItem('remove')">
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown>

    <el-dialog :title="$t('chart.show_name_set')" :visible="renameItem" :show-close="false" width="30%">
      <el-form ref="itemForm" :model="itemForm" :rules="itemFormRules">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="itemForm.name" size="mini" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeRename()">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveRename(itemForm)">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
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
      renameItem: false,
      itemForm: {
        name: ''
      },
      itemFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' }
        ]
      }
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
      this.itemForm.name = this.item.name
      this.renameItem = true
    },
    closeRename() {
      this.renameItem = false
      this.resetRename()
    },
    saveRename(param) {
      this.item.name = param.name
      this.$emit('onDimensionItemChange', this.item)
      this.closeRename()
    },
    resetRename() {
      this.itemForm = {
        name: ''
      }
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
