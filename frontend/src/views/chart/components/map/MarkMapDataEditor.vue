<template>

  <div>
    <el-row
      class="padding-lr"
    >
      <span class="data-area-label">
        <span>{{ $t('chart.longitude') }}</span>
        <span> / </span>
        <span> {{ $t('chart.dimension') }}</span>
        <i
          class="el-icon-arrow-down el-icon-delete data-area-clear"
          @click="clearData('locationXaxis')"
        />
      </span>
      <draggable
        v-model="busiFieldMap.locationXaxis"
        group="drag"
        animation="300"
        :move="onMove"
        class="drag-block-style"
        @add="addLocationXaxis"
        @update="calcData(true)"
      >
        <transition-group class="draggable-group">
          <detail-item
            v-for="(item,index) in busiFieldMap.locationXaxis"
            :key="item.id"
            :param="param"
            :index="index"
            :item="item"
            :dimension-data="dimensionData"
            :quota-data="quotaData"
            @onDetailItemRemove="locationXItemRemove"
          />
        </transition-group>
      </draggable>
      <div
        v-if="!busiFieldMap.locationXaxis || busiFieldMap.locationXaxis.length === 0"
        class="drag-placeholder-style"
      >
        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
      </div>
    </el-row>
    <el-row

      class="padding-lr"
    >
      <span class="data-area-label">
        <span>{{ $t('chart.latitude') }} / {{ $t('chart.dimension') }}</span>
        <i
          class="el-icon-arrow-down el-icon-delete data-area-clear"
          @click="clearData('locationYaxis')"
        />
      </span>
      <draggable
        v-model="busiFieldMap.locationYaxis"
        group="drag"
        animation="300"
        :move="onMove"
        class="drag-block-style"
        @add="addLocationYaxis"
        @update="calcData(true)"
      >
        <transition-group class="draggable-group">
          <detail-item
            v-for="(item,index) in busiFieldMap.locationYaxis"
            :key="item.id"
            :param="param"
            :index="index"
            :item="item"
            :dimension-data="dimensionData"
            :quota-data="quotaData"
            @onDetailItemRemove="locationYItemRemove"
          />
        </transition-group>
      </draggable>
      <div
        v-if="!busiFieldMap.locationYaxis || busiFieldMap.locationYaxis.length === 0"
        class="drag-placeholder-style"
      >
        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
      </div>
    </el-row>

  </div>
</template>

<script>
import DetailItem from '@/views/chart/components/dragItem/DetailItem'
export default {
  name: 'MarkMapDataEditor',
  components: { DetailItem },
  props: {
    view: {
      type: Object,
      require: true
    },
    param: {
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
    }
  },
  data() {
    return {
      busiFieldMap: {
        locationXaxis: [],
        locationYaxis: [],
        daxis: []
      }
    }
  },
  created() {
    this.releaseViewFields()
  },
  methods: {
    fillViewFields() {
      const result = []
      for (const key in this.busiFieldMap) {
        if (Object.hasOwnProperty.call(this.busiFieldMap, key)) {
          const element = JSON.parse(JSON.stringify(this.busiFieldMap[key]))
          element.forEach(item => {
            item.busiType = key
            result.push(item)
          })
        }
      }
      this.view.viewFields = result
    },
    releaseViewFields() {
      this.view.viewFields.forEach(item => {
        const busiType = item.busiType
        if (this.busiFieldMap?.[busiType]) {
          this.busiFieldMap[busiType].push(item)
        }
      })
    },
    clearData(type) {
      this.busiFieldMap[type] = []
      this.calcData(true)
    },
    onMove(e, originalEvent) {
      this.moveId = e.draggedContext.element.id
      return true
    },
    dragCheckType(list, type) {
      if (list && list.length > 0) {
        for (let i = 0; i < list.length; i++) {
          if (list[i].groupType !== type) {
            list.splice(i, 1)
          }
        }
      }
    },
    dragMoveDuplicate(list, e) {
      const that = this
      const dup = list.filter(function(m) {
        return m.id === that.moveId
      })
      if (dup && dup.length > 1) {
        list.splice(e.newDraggableIndex, 1)
      }
    },

    addDaxis(e) {
      this.dragCheckType(this.busiFieldMap.daxis, 'd')
      this.dragMoveDuplicate(this.busiFieldMap.daxis, e)
      this.calcData(true)
    },
    addLocationXaxis(e) {
      this.dragCheckType(this.busiFieldMap.locationXaxis, 'd')
      this.dragMoveDuplicate(this.busiFieldMap.locationXaxis, e)
      if (this.busiFieldMap.locationXaxis?.length) {
        this.busiFieldMap.locationXaxis = [this.busiFieldMap.locationXaxis[0]]
      }

      this.calcData(true)
    },
    addLocationYaxis(e) {
      this.dragCheckType(this.busiFieldMap.locationYaxis, 'd')
      this.dragMoveDuplicate(this.busiFieldMap.locationYaxis, e)
      if (this.busiFieldMap.locationYaxis?.length) {
        this.busiFieldMap.locationYaxis = [this.busiFieldMap.locationYaxis[0]]
      }
      this.calcData(true)
    },
    locationXItemRemove(item) {
      this.busiFieldMap.locationXaxis.splice(item.index, 1)
      this.calcData(true)
    },
    locationYItemRemove(item) {
      this.busiFieldMap.locationYaxis.splice(item.index, 1)
      this.calcData(true)
    },
    detailItemRemove(item) {
      this.busiFieldMap.daxis.splice(item.index, 1)
      this.calcData(true)
    },
    calcData(cache) {
      this.fillViewFields()
      this.$emit('calc-data', cache)
    }
  }
}
</script>

<style lang="scss" scoped>
.padding-lr {
  padding: 0 6px;
}

.itxst {
  margin: 10px;
  text-align: left;
}

.col {
  width: 40%;
  flex: 1;
  padding: 10px;
  border: solid 1px #eee;
  border-radius: 5px;
  float: left;
}

.col + .col {
  margin-left: 10px;
}

.view-panel-row {
  display: flex;
  background-color: #f7f8fa;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 96px);
}

.view-panel-Mask {
  display: flex;
  height: calc(100vh - 80px);
  background-color: rgba(92, 94, 97, 0.7);
  position: absolute;
  top: 0px;
  left: 0px;
  width: 350px;
  z-index: 2;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-panel {
  display: flex;
  height: 100%;
  background-color: #f7f8fa;
}

.blackTheme .view-panel {
  background-color: var(--MainBG);
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
  padding: 2px 0;
}

.item-dimension {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  position: relative;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
  cursor: pointer;
}

.item-quota {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  position: relative;
}

.blackTheme .item-quota {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #b2d3a3;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
  background: var(--ContentBG);
}

.el-form-item {
  margin-bottom: 0;
}

span {
  font-size: 12px;
}

.tab-header ::v-deep .el-tabs__header {
  border-top: solid 1px #eee;
  border-right: solid 1px #eee;
}

.tab-header ::v-deep .el-tabs__item {
  font-size: 12px;
  padding: 0 20px !important;
}

.blackTheme .tab-header ::v-deep .el-tabs__item {
  background-color: var(--MainBG);
}

.tab-header ::v-deep .el-tabs__nav-scroll {
  padding-left: 0 !important;
}

.tab-header ::v-deep .el-tabs__header {
  margin: 0 !important;
}

.tab-header ::v-deep .el-tabs__content {
  height: calc(100% - 40px);
}

.draggable-group {
  display: block;
  width: 100%;
  height: calc(100% - 6px);
}

.chart-icon {
  width: 20px !important;
  height: 20px !important;
}

.el-radio {
  margin: 5px;
}

.el-radio ::v-deep .el-radio__label {
  padding-left: 0;
}

.attr-style {
  height: calc(100vh - 76px - 60px - 40px - 40px);
}

.blackTheme .attr-style {
  color: var(--TextPrimary);
}

.attr-selector {
  width: 100%;
  height: 100%;
  margin: 6px 0;
  padding: 0 4px;
  display: flex;
  align-items: center;
  background-color: white
}

.blackTheme .attr-selector {

  background-color: var(--MainBG)
}

.disabled-none-cursor {
  cursor: not-allowed;
  pointer-events: none;
}

.chart-class {
  height: 100%;
  padding: 10px;
}

.table-class {
  height: calc(100% - 20px);
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.filter-btn-class {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-error-class {
  text-align: center;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ece7e7;
}

.blackTheme .chart-error-class {

  background-color: var(--MainBG)
}

.field-height {
  height: 100%;
  border-top: 1px solid #E6E6E6;
}

.blackTheme .field-height {

  border-top: 1px solid;
  border-color: var(--TableBorderColor) !important;
}

.padding-tab {
  padding: 0;
  height: 100%;
}

.tree-select-span {
  ::v-deep div.vue-treeselect__control {
    height: 32px !important;
    font-weight: normal !important;
  }
}

.drag-block-style {
  padding: 2px 0 0 0;
  width: 100%;
  min-height: 32px;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  overflow-x: hidden;
  display: flex;
  align-items: center;
  background-color: white;
}

.blackTheme .drag-block-style {
  border: 1px solid;
  border-color: var(--TableBorderColor);
  background-color: var(--ContentBG);
}

.drag-placeholder-style {
  position: absolute;
  top: calc(50% - 2px);
  left: 0;
  width: 100%;
  color: #CCCCCC;
}

.blackTheme .drag-placeholder-style {
  color: var(--TextPrimary);
}

.drag-placeholder-style-span {
  padding-left: 16px;
}

.blackTheme .theme-border-class {
  color: var(--TextPrimary) !important;
  background-color: var(--ContentBG);
}

.blackTheme .padding-lr {
  border-color: var(--TableBorderColor) !important;
}

.blackTheme .theme-item-class {
  background-color: var(--MainBG) !important;
  border-color: var(--TableBorderColor) !important;
}

.icon-class {
  color: #6c6c6c;
}

.blackTheme .icon-class {
  color: #cccccc;
}

.result-count {
  width: 50px;
}

.result-count ::v-deep input {
  padding: 0 4px;
}

.radio-span ::v-deep .el-radio__label {
  margin-left: 4px;
}

.view-title-name {
  display: -moz-inline-box;
  display: inline-block;
  width: 130px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin-left: 45px;
}

::v-deep .item-axis {
  width: 168px !important;
}

::v-deep .el-slider__input {
  width: 80px !important;
}

::v-deep .el-input-number--mini {
  width: 100px !important;
}

::v-deep .el-slider__runway.show-input {
  width: 80px !important;
}

.no-senior {
  width: 100%;
  text-align: center;
  font-size: 12px;
  padding-top: 40px;
  overflow: auto;
  border-right: 1px solid #e6e6e6;
  height: 100%;
}

.form-item-slider::v-deep.el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item::v-deep.el-form-item__label {
  font-size: 12px;
}

.field-name {
  display: inline-block;
  width: 90px;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  position: absolute;
  top: 2px;
}

.field-setting {
  position: absolute;
  right: 8px;
}

.father .child {
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}

.field-split {
  height: calc(100% - 40px);
}

.field-split ::v-deep .fu-split-pane__left {
  padding-right: 0 !important;
}

.field-split ::v-deep .fu-split-pane__right {
  padding-left: 0 !important;
}

.view-panel-row ::v-deep .el-collapse-item__header {
  height: 34px !important;
  line-height: 34px !important;
  padding: 0 0 0 6px !important;
  font-size: 12px !important;
  font-weight: 400 !important;
}

.data-area-label {
  text-align: left;
  position: relative;
  width: 100%;
  display: inline-block;
}

.data-area-clear {
  position: absolute;
  top: 4px;
  right: 6px;
  color: rgb(135, 141, 159);
  cursor: pointer;
  z-index: 1;
}
</style>
