<template>
  <span>
    <el-dropdown trigger="click" size="small">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<span class="summary-span">{{ $t('chart.'+item.summary) }}</span><i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item icon="el-icon-notebook-2">
            <el-dropdown placement="right-start" size="small" @command="summary">
              <span class="el-dropdown-link">
                {{ $t('chart.summary') }}<span class="summary-span">({{ $t('chart.'+item.summary) }})</span><i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSummary('sum')">{{ $t('chart.sum') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('avg')">{{ $t('chart.avg') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('max')">{{ $t('chart.max') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('min')">{{ $t('chart.min') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('std')">{{ $t('chart.std') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('var_samp')">{{ $t('chart.var_samp') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete">
            item4
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
export default {
  name: 'QuotaItem',
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
  methods: {
    summary(param) {
      // console.log(param)
      this.item.summary = param.type
      this.$emit('onQuotaSummaryChange', this.item)
    },
    beforeSummary(type) {
      return {
        type: type
      }
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
</style>
