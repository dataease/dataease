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
            <svg-icon
              v-if="item.sort === 'custom_sort'"
              icon-class="custom_sort"
              class-name="field-icon-sort"
            />
          </span>
          <span
            class="item-span-style"
            :title="item.name"
          >{{ item.name }}</span>
          <field-error-tips v-if="tagType === 'danger'"/>
          <span
            v-if="false && item.deType === 1"
            class="summary-span"
          >
            {{ $t('chart.' + item.dateStyle) }}
          </span>
          <i
            class="el-icon-arrow-down el-icon--right"
            style="position: absolute;top: 6px;right: 10px;"
          />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>
            <el-dropdown
              placement="right-start"
              size="mini"
              style="width: 100%"
              @command="sort"
              v-if="dimensionName==='source'"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-sort"/>
                  <span>{{ $t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ item.sort ? $t('chart.' + item.sort) : $t('chart.none') }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right"/>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSort('none')">{{ $t('chart.none') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('asc')">{{ $t('chart.asc') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('desc')">{{ $t('chart.desc') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item
            icon="el-icon-edit-outline"
            :command="beforeClickItem('rename')"
          >
            <span>{{ $t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item
            icon="el-icon-delete"
            divided
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
import {getItemType, getOriginFieldName} from './utils'
import FieldErrorTips from './FieldErrorTips'
import {formatterItem} from '@/utils/map'

export default {
  name: 'DimensionItem',
  components: {FieldErrorTips},
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
    chart: {
      type: Object,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    },
    dimensionName: {
      type: String,
      required: true
    },
    bus: {
      type: Object,
      required: false,
      default: null
    },
  },
  data() {
    return {
      tagType: 'success',
      formatterItem: formatterItem,
      showDateExt: false
    }
  },
  watch: {
    dimensionData: function () {
      this.getItemTagType()
    },
    item: function () {
      this.getItemTagType()
    },
    chart: function () {
      this.getDateExtStatus()
    }
  },
  mounted() {
    this.bus.$on('reset-change-table', this.getItemTagType)
    this.init()
    this.getDateExtStatus()
  },
  beforeDestroy() {
    this.bus.$off('reset-change-table', this.getItemTagType)
  },
  methods: {
    init() {
      if (!this.item.formatterCfg) {
        this.item.formatterCfg = JSON.parse(JSON.stringify(this.formatterItem))
      }
    },
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
        case 'formatter':
          this.valueFormatter()
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
      if (param.type === 'custom_sort') {
        const item = {
          index: this.index,
          sort: param.type
        }
        this.$emit('onCustomSort', item)
      } else {
        this.item.index = this.index
        this.item.sort = param.type
        this.item.customSort = []
        this.$emit('onDimensionItemChange', this.item)
      }
    },
    beforeSort(type) {
      return {
        type: type
      }
    },
    dateStyle(param) {
      this.item.dateStyle = param.type
      this.$emit('onDimensionItemChange', this.item)
    },
    beforeDateStyle(type) {
      return {
        type: type
      }
    },
    datePattern(param) {
      this.item.datePattern = param.type
      this.$emit('onDimensionItemChange', this.item)
    },
    beforeDatePattern(type) {
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
      this.item.dsFieldName = getOriginFieldName(this.dimensionData, this.quotaData, this.item)
      console.log(this.item)
      this.$emit('onNameEdit', this.item)
    },
    removeItem() {
      this.item.index = this.index
      this.item.removeType = this.dimensionName
      this.$emit('onDimensionItemRemove', this.item)
    },
    getItemTagType() {
      this.tagType = getItemType(this.dimensionData, this.quotaData, this.item)
    },

    valueFormatter() {
      this.item.index = this.index
      this.item.formatterType = 'dimension'
      this.$emit('valueFormatter', this.item)
    },

    getDateExtStatus() {
      if (this.chart) {
        this.showDateExt = this.chart.datasourceType === 'mysql' ||
          this.chart.datasourceType === 'ds_doris' ||
          this.chart.datasourceType === 'StarRocks' ||
          this.chart.datasetMode === 1
      } else {
        this.showDateExt = false
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

.summary-span {
  margin-left: 4px;
  color: #878d9f;
  position: absolute;
  right: 25px;
}

.inner-dropdown-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%
}

.item-span-style {
  display: inline-block;
  width: 100px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.summary-span-item {
  margin-left: 4px;
  color: #878d9f;
}
</style>
