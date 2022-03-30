<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis" :type="tagType">
          <span style="float: left">
            <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
            <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
            <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
            <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
            <svg-icon v-if="item.sort === 'asc'" icon-class="sort-asc" class-name="field-icon-sort" />
            <svg-icon v-if="item.sort === 'desc'" icon-class="sort-desc" class-name="field-icon-sort" />
          </span>
          <span class="item-span-style" :title="item.name">{{ item.name }}</span>
          <field-error-tips v-if="tagType === 'danger'" />
          <span v-if="item.summary" class="summary-span">{{ $t('chart.'+item.summary) }}</span>
          <span v-if="item.deType === 1" class="summary-span">
            {{ $t('chart.' + item.dateStyle) }}
          </span>
          <i class="el-icon-arrow-down el-icon--right" style="position: absolute;top: 6px;right: 10px;" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-show="conf && conf.includes('summary')">
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="summary">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-notebook-2" />
                  <span>{{ $t('chart.summary') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.summary) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-if="item.id === 'count' || item.deType === 0 || item.deType === 1" :command="beforeSummary('count')">{{ $t('chart.count') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('sum')">{{ $t('chart.sum') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('avg')">{{ $t('chart.avg') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('max')">{{ $t('chart.max') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('min')">{{ $t('chart.min') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('stddev_pop')">{{ $t('chart.stddev_pop') }}</el-dropdown-item>
                <el-dropdown-item v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1" :command="beforeSummary('var_pop')">{{ $t('chart.var_pop') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>

          <el-dropdown-item v-show="item.deType === 1">
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="dateStyle">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-c-scale-to-original" />
                  <span>{{ $t('chart.dateStyle') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.dateStyle) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeDateStyle('y')">{{ $t('chart.y') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeDateStyle('y_M')">{{ $t('chart.y_M') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeDateStyle('y_M_d')">{{ $t('chart.y_M_d') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeDateStyle('H_m_s')">{{ $t('chart.H_m_s') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m')">{{ $t('chart.y_M_d_H_m') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m_s')">{{ $t('chart.y_M_d_H_m_s') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item v-show="item.deType === 1">
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="datePattern">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-timer" />
                  <span>{{ $t('chart.datePattern') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.datePattern) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeDatePattern('date_sub')">{{ $t('chart.date_sub') }}(1990-01-01)</el-dropdown-item>
                <el-dropdown-item :command="beforeDatePattern('date_split')">{{ $t('chart.date_split') }}(1990/01/01)</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>

          <el-dropdown-item v-show="conf && conf.includes('sort')" :divided="item.deType === 1">
            <el-dropdown placement="right-start" size="mini" style="width: 100%" @command="sort">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-sort" />
                  <span>{{ $t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.sort) }})</span>
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
import { getItemType } from '@/views/chart/components/drag-item/utils'
import FieldErrorTips from '@/views/chart/components/drag-item/components/FieldErrorTips'

export default {
  name: 'ChartDragItem',
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
    conf: {
      type: String,
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
    summary(param) {
      // console.log(param)
      this.item.summary = param.type
      this.$emit('onItemChange', this.item)
    },
    beforeSummary(type) {
      return {
        type: type
      }
    },
    removeItem() {
      this.item.index = this.index
      this.$emit('onItemRemove', this.item)
    },

    dateStyle(param) {
      // console.log(param)
      this.item.dateStyle = param.type
      this.$emit('onItemChange', this.item)
    },
    beforeDateStyle(type) {
      return {
        type: type
      }
    },
    datePattern(param) {
      // console.log(param)
      this.item.datePattern = param.type
      this.$emit('onItemChange', this.item)
    },
    beforeDatePattern(type) {
      return {
        type: type
      }
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

  .inner-dropdown-menu{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%
  }

  .item-span-style{
    display: inline-block;
    width: 70px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .summary-span-item{
    margin-left: 4px;
    color: #878d9f;
  }

  .summary-span{
    margin-left: 4px;
    color: #878d9f;
    position: absolute;
    right: 25px;
  }
</style>
