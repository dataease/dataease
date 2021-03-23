<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<span v-if="item.summary" class="summary-span">{{ $t('chart.'+item.summary) }}</span><i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="summary">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-notebook-2" />
                  <span>{{ $t('chart.summary') }}</span>
                  <span class="summary-span">({{ $t('chart.'+item.summary) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSummary('sum')">{{ $t('chart.sum') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('count')">{{ $t('chart.count') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('avg')">{{ $t('chart.avg') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('max')">{{ $t('chart.max') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('min')">{{ $t('chart.min') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('std')">{{ $t('chart.std') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('var_samp')">{{ $t('chart.var_samp') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item>
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="quickCalc">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-s-grid" />
                  <span>{{ $t('chart.quick_calc') }}</span>
                  <span class="summary-span">(无)</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeQuickCalc('none')">无</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item divided>
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
          <el-dropdown-item icon="el-icon-files">
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
  name: 'QuotaItem',
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

    summary(param) {
      // console.log(param)
      this.item.summary = param.type
      this.$emit('onQuotaItemChange', this.item)
    },
    beforeSummary(type) {
      return {
        type: type
      }
    },

    quickCalc(param) {

    },
    beforeQuickCalc(type) {
      return {
        type: type
      }
    },

    sort(param) {
      // console.log(param)
      this.item.sort = param.type
      this.$emit('onQuotaItemChange', this.item)
    },
    beforeSort(type) {
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
      this.$emit('onQuotaItemChange', this.item)
      this.closeRename()
    },
    resetRename() {
      this.itemForm = {
        name: ''
      }
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('onQuotaItemRemove', this.item)
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
</style>
