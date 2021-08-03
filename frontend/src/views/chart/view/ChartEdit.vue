<template>
  <el-row v-loading="loading" style="height: 100%;overflow-y: hidden;width: 100%;">
    <!--    <span v-show="false">{{ vId }}</span>-->
    <el-row style="height: 40px;background-color: white" class="padding-lr">
      <el-popover
        placement="right-start"
        width="400"
        trigger="click"
        @show="showTab"
        @hide="hideTab"
      >
        <dataset-chart-detail type="chart" :data="view" :tab-status="tabStatus" />
        <span slot="reference" style="line-height: 40px;cursor: pointer;">{{ view.name }}</span>
      </el-popover>
      <span style="float: right;line-height: 40px;">
        <!--        <el-button v-if="hasDataPermission('manage',param.privileges)" size="mini" @click="changeDs">-->
        <!--          {{ $t('chart.change_ds') }}-->
        <!--        </el-button>-->
        <el-button size="mini" @click="closeEdit">
          {{ $t('commons.save') }}
        </el-button>

        <!--        <el-button type="primary" size="mini" @click="save">-->
        <!--          {{ $t('chart.confirm') }}-->
        <!--        </el-button>-->
      </span>
    </el-row>
    <el-row class="view-panel">
      <el-tabs type="card" :stretch="true" class="tab-header">
        <el-tab-pane :label="$t('chart.chart_data')" class="padding-tab" style="width: 360px">
          <el-row class="view-panel">
            <el-col
              style="width: 180px;border-right: 1px solid #E6E6E6;"
            >
              <div style="display: flex;align-items: center;justify-content: center;padding: 6px;">
                <el-input
                  v-model="searchField"
                  size="mini"
                  :placeholder="$t('chart.search')"
                  prefix-icon="el-icon-search"
                  clearable
                />
                <el-button :title="$t('dataset.edit_field')" :disabled="!table || !hasDataPermission('manage',table.privileges)" icon="el-icon-setting" type="text" size="mini" style="float: right;width: 20px;margin-left: 4px;" @click="editField" />
                <el-button :title="$t('chart.change_ds')" :disabled="!hasDataPermission('manage',param.privileges)" icon="el-icon-refresh" type="text" size="mini" style="float: right;width: 20px;margin-left: 4px;" @click="changeDs" />
              </div>
              <div class="padding-lr field-height">
                <span>{{ $t('chart.dimension') }}</span>
                <draggable
                  v-model="dimensionData"
                  :options="{group:{name: 'drag',pull:'clone'},sort: true}"
                  animation="300"
                  :move="onMove"
                  class="drag-list"
                  :disabled="!hasDataPermission('manage',param.privileges)"
                  @add="moveToDimension"
                >
                  <transition-group>
                    <span v-for="item in dimensionData" :key="item.id" class="item-dimension" :title="item.name">
                      <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                      <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                      <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                      <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                      {{ item.name }}
                    </span>
                  </transition-group>
                </draggable>
              </div>
              <div class="padding-lr field-height">
                <span>{{ $t('chart.quota') }}</span>
                <draggable
                  v-model="quotaData"
                  :options="{group:{name: 'drag',pull:'clone'},sort: true}"
                  animation="300"
                  :move="onMove"
                  class="drag-list"
                  :disabled="!hasDataPermission('manage',param.privileges)"
                  @add="moveToQuota"
                >
                  <transition-group>
                    <span v-for="item in quotaData" :key="item.id" class="item-quota" :title="item.name">
                      <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                      <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                      <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                      <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                      <span>{{ item.name }}</span>
                    </span>
                  </transition-group>
                </draggable>
              </div>
            </el-col>

            <el-col
              style="height: 100%;width: 180px;border-right: 1px solid #E6E6E6;"
            >
              <!--        <div style="border-bottom: 1px solid #E6E6E6;overflow-y:hidden;height: 62px;" class="padding-lr">-->
              <!--          <el-row>-->
              <!--            <span>{{ $t('chart.title') }}</span>-->
              <!--            <el-button style="float: right;padding: 0;margin: 8px 0 0 0;font-size: 12px;" type="text" @click="save">{{ $t('chart.confirm') }}</el-button>-->
              <!--          </el-row>-->
              <!--          <el-form>-->
              <!--            <el-form-item class="form-item">-->
              <!--              <el-input-->
              <!--                v-model="view.title"-->
              <!--                size="mini"-->
              <!--                :placeholder="$t('chart.title')"-->
              <!--                prefix-icon="el-icon-search"-->
              <!--                clearable-->
              <!--              />-->
              <!--            </el-form-item>-->
              <!--          </el-form>-->
              <!--        </div>-->
              <div style="height: 25vh;overflow:auto" class="padding-lr">
                <span>{{ $t('chart.chart_type') }}</span>
                <el-row>
                  <div class="chart-type">
                    <!--这里要替换好看点的图标，UI标签可以重新定义-->
                    <el-radio-group
                      v-model="view.type"
                      style="width: 100%"
                      :disabled="!hasDataPermission('manage',param.privileges)"
                      @change="save(true,'chart',true)"
                    >
                      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
                        <el-radio value="table-normal" label="table-normal">
                          <span :title="$t('chart.chart_table_normal')">
                            <svg-icon icon-class="table-normal" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="text" label="text">
                          <span :title="$t('chart.chart_card')">
                            <svg-icon icon-class="text" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="bar" label="bar">
                          <span :title="$t('chart.chart_bar')">
                            <svg-icon icon-class="bar" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="bar-stack" label="bar-stack">
                          <span :title="$t('chart.chart_bar_stack')">
                            <svg-icon icon-class="bar-stack" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="bar-horizontal" label="bar-horizontal">
                          <span :title="$t('chart.chart_bar_horizontal')">
                            <svg-icon icon-class="bar-horizontal" class="chart-icon" />
                          </span>
                        </el-radio>
                      </div>
                      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
                        <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal">
                          <span :title="$t('chart.chart_bar_stack_horizontal')">
                            <svg-icon icon-class="bar-stack-horizontal" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="line" label="line">
                          <span :title="$t('chart.chart_line')">
                            <svg-icon icon-class="line" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="line-stack" label="line-stack">
                          <span :title="$t('chart.chart_line_stack')">
                            <svg-icon icon-class="line-stack" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="pie" label="pie">
                          <span :title="$t('chart.chart_pie')">
                            <svg-icon icon-class="pie" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="pie-rose" label="pie-rose">
                          <span :title="$t('chart.chart_pie_rose')">
                            <svg-icon icon-class="pie-rose" class="chart-icon" />
                          </span>
                        </el-radio>
                      </div>
                      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
                        <el-radio value="funnel" label="funnel">
                          <span :title="$t('chart.chart_funnel')">
                            <svg-icon icon-class="funnel" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="radar" label="radar">
                          <span :title="$t('chart.chart_radar')">
                            <svg-icon icon-class="radar" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="gauge" label="gauge">
                          <span :title="$t('chart.chart_gauge')">
                            <svg-icon icon-class="gauge" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="map" label="map">
                          <span :title="$t('chart.chart_map')">
                            <svg-icon icon-class="map" class="chart-icon" />
                          </span>
                        </el-radio>
                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                      </div>
                    </el-radio-group>
                  </div>
                </el-row>
                <el-row style="color: #909399;">
                  <span>
                    <span v-show="chart.type && (chart.type.includes('pie') || chart.type.includes('funnel') || chart.type.includes('text') || chart.type.includes('gauge'))">
                      Tips: {{ $t('chart.only_one_quota') }}
                    </span>
                  <!--              <span v-show="chart.type && (chart.type.includes('text'))">-->
                  <!--                Tips: {{ $t('chart.only_one_result') }}-->
                  <!--              </span>-->
                  <!--              <span v-show="chart.type && chart.type.includes('gauge')">-->
                  <!--                Tips: {{ $t('chart.only_one_quota') }},{{ $t('chart.only_one_result') }}-->
                  <!--              </span>-->
                  </span>
                </el-row>
              </div>
              <div style="overflow:auto;border-top: 1px solid #e6e6e6" class="attr-style">
                <el-row style="height: 100%;">
                  <el-row v-if="view.type ==='map'" class="padding-lr">
                    <span style="width: 80px;text-align: right;">
                      <span>{{ $t('chart.map_range') }}</span>
                    </span>
                    <span class="tree-select-span">
                      <treeselect
                        ref="mapSelector"
                        v-model="view.customAttr.areaCode"
                        :options="places"
                        :placeholder="$t('chart.select_map_range')"
                        :normalizer="normalizer"
                        @input="save"
                        @deselect="save"
                      />
                    </span>
                  </el-row>
                  <el-row v-if="view.type !=='text' && view.type !== 'gauge'" class="padding-lr">
                    <span style="width: 80px;text-align: right;">
                      <span v-if="view.type && view.type.includes('table')">{{ $t('chart.drag_block_table_data_column') }}</span>
                      <span v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line'))">{{ $t('chart.drag_block_type_axis') }}</span>
                      <span v-else-if="view.type && view.type.includes('pie')">{{ $t('chart.drag_block_pie_label') }}</span>
                      <span v-else-if="view.type && view.type.includes('funnel')">{{ $t('chart.drag_block_funnel_split') }}</span>
                      <span v-else-if="view.type && view.type.includes('radar')">{{ $t('chart.drag_block_radar_label') }}</span>
                      <span v-else-if="view.type && view.type.includes('map')">{{ $t('chart.area') }}</span>
                      /
                      <span>{{ $t('chart.dimension') }}</span>
                    </span>
                    <draggable
                      v-model="view.xaxis"
                      :disabled="!hasDataPermission('manage',param.privileges)"
                      group="drag"
                      animation="300"
                      :move="onMove"
                      class="drag-block-style"
                      @add="addXaxis"
                    >
                      <transition-group class="draggable-group">
                        <dimension-item v-for="(item,index) in view.xaxis" :key="item.id" :param="param" :index="index" :item="item" @onDimensionItemChange="dimensionItemChange" @onDimensionItemRemove="dimensionItemRemove" @editItemFilter="showDimensionEditFilter" @onNameEdit="showRename" />
                      </transition-group>
                    </draggable>
                    <div v-if="!view.xaxis || view.xaxis.length === 0" class="drag-placeholder-style">
                      <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                    </div>
                  </el-row>
                  <el-row class="padding-lr" style="margin-top: 6px;">
                    <span style="width: 80px;text-align: right;">
                      <span v-if="view.type && view.type.includes('table')">{{ $t('chart.drag_block_table_data_column') }}</span>
                      <span v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line'))">{{ $t('chart.drag_block_value_axis') }}</span>
                      <span v-else-if="view.type && view.type.includes('pie')">{{ $t('chart.drag_block_pie_angel') }}</span>
                      <span v-else-if="view.type && view.type.includes('funnel')">{{ $t('chart.drag_block_funnel_width') }}</span>
                      <span v-else-if="view.type && view.type.includes('radar')">{{ $t('chart.drag_block_radar_length') }}</span>
                      <span v-else-if="view.type && view.type.includes('gauge')">{{ $t('chart.drag_block_gauge_angel') }}</span>
                      <span v-else-if="view.type && view.type.includes('text')">{{ $t('chart.drag_block_label_value') }}</span>
                      <span v-else-if="view.type && view.type.includes('map')">{{ $t('chart.chart_data') }}</span>
                      /
                      <span>{{ $t('chart.quota') }}</span>
                    </span>
                    <draggable
                      v-model="view.yaxis"
                      :disabled="!hasDataPermission('manage',param.privileges)"
                      group="drag"
                      animation="300"
                      :move="onMove"
                      class="drag-block-style"
                      @add="addYaxis"
                    >
                      <transition-group class="draggable-group">
                        <quota-item v-for="(item,index) in view.yaxis" :key="item.id" :param="param" :index="index" :item="item" @onQuotaItemChange="quotaItemChange" @onQuotaItemRemove="quotaItemRemove" @editItemFilter="showQuotaEditFilter" @onNameEdit="showRename" />
                      </transition-group>
                    </draggable>
                    <div v-if="!view.yaxis || view.yaxis.length === 0" class="drag-placeholder-style">
                      <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                    </div>
                  </el-row>
                  <el-row v-if="view.type && view.type.includes('stack')" class="padding-lr" style="margin-top: 6px;">
                    <span style="width: 80px;text-align: right;">
                      <span>{{ $t('chart.stack_item') }}</span>
                      /
                      <span>{{ $t('chart.dimension') }}</span>
                    </span>
                    <draggable
                      v-model="view.extStack"
                      :disabled="!hasDataPermission('manage',param.privileges)"
                      group="drag"
                      animation="300"
                      :move="onMove"
                      class="drag-block-style"
                      @add="addStack"
                    >
                      <transition-group class="draggable-group">
                        <chart-drag-item v-for="(item,index) in view.extStack" :key="item.id" :param="param" :index="index" :item="item" @onItemChange="stackItemChange" @onItemRemove="stackItemRemove" />
                      </transition-group>
                    </draggable>
                    <div v-if="!view.extStack || view.extStack.length === 0" class="drag-placeholder-style">
                      <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                    </div>
                  </el-row>
                  <el-row class="padding-lr" style="margin-top: 6px;">
                    <span>{{ $t('chart.result_filter') }}</span>
                    <!--                    <el-button :disabled="!hasDataPermission('manage',param.privileges)" size="mini" class="filter-btn-class" @click="showResultFilter">-->
                    <!--                      {{ $t('chart.filter_condition') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
                    <!--                    </el-button>-->
                    <draggable
                      v-model="view.customFilter"
                      :disabled="!hasDataPermission('manage',param.privileges)"
                      group="drag"
                      animation="300"
                      :move="onMove"
                      style="padding:2px 0 0 0;width:100%;min-height: 32px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;background-color: white;"
                      @add="addCustomFilter"
                    >
                      <transition-group class="draggable-group">
                        <filter-item v-for="(item,index) in view.customFilter" :key="item.id" :param="param" :index="index" :item="item" @onFilterItemRemove="filterItemRemove" @editItemFilter="showEditFilter" />
                      </transition-group>
                    </draggable>
                    <div v-if="!view.customFilter || view.customFilter.length === 0" class="drag-placeholder-style">
                      <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                    </div>
                  </el-row>
                </el-row>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane :label="$t('chart.chart_style')" class="padding-tab" style="width: 360px;">
          <el-row class="view-panel">
            <div style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;" class="attr-style">
              <el-row class="padding-lr">
                <span>{{ $t('chart.style_priority') }}</span>
                <el-row>
                  <el-radio-group v-model="view.stylePriority" :disabled="!hasDataPermission('manage',param.privileges)" size="mini" @change="save">
                    <el-radio label="view"><span>{{ $t('chart.chart') }}</span></el-radio>
                    <el-radio label="panel"><span>{{ $t('chart.dashboard') }}</span></el-radio>
                  </el-radio-group>
                </el-row>
              </el-row>
              <el-row>
                <span class="padding-lr">{{ $t('chart.shape_attr') }}</span>
                <el-collapse v-model="attrActiveNames" class="style-collapse">
                  <el-collapse-item name="color" :title="$t('chart.color')">
                    <color-selector :param="param" class="attr-selector" :chart="chart" @onColorChange="onColorChange" />
                  </el-collapse-item>
                  <el-collapse-item v-show="chart.type !== 'map'" name="size" :title="$t('chart.size')">
                    <size-selector :param="param" class="attr-selector" :chart="chart" @onSizeChange="onSizeChange" />
                  </el-collapse-item>
                  <el-collapse-item v-show="!view.type.includes('table') && !view.type.includes('text')" name="label" :title="$t('chart.label')">
                    <label-selector :param="param" class="attr-selector" :chart="chart" @onLabelChange="onLabelChange" />
                  </el-collapse-item>
                  <el-collapse-item v-show="!view.type.includes('table') && !view.type.includes('text')" name="tooltip" :title="$t('chart.tooltip')">
                    <tooltip-selector :param="param" class="attr-selector" :chart="chart" @onTooltipChange="onTooltipChange" />
                  </el-collapse-item>
                </el-collapse>
                <!--              <el-row>-->
                <!--                <color-selector :param="param" class="attr-selector" :chart="chart" @onColorChange="onColorChange" />-->
                <!--                <size-selector v-show="chart.type !== 'map'" :param="param" class="attr-selector" :chart="chart" @onSizeChange="onSizeChange" />-->
                <!--                <label-selector v-show="!view.type.includes('table') && !view.type.includes('text')" :param="param" class="attr-selector" :chart="chart" @onLabelChange="onLabelChange" />-->
                <!--                <tooltip-selector v-show="!view.type.includes('table') && !view.type.includes('text')" :param="param" class="attr-selector" :chart="chart" @onTooltipChange="onTooltipChange" />-->
                <!--              </el-row>-->
              </el-row>
              <el-row>
                <span class="padding-lr">{{ $t('chart.module_style') }}</span>
                <el-collapse v-model="styleActiveNames" class="style-collapse">
                  <el-collapse-item v-show="view.type && (view.type.includes('bar') || view.type.includes('line'))" name="xAxis" :title="$t('chart.xAxis')">
                    <x-axis-selector :param="param" class="attr-selector" :chart="chart" @onChangeXAxisForm="onChangeXAxisForm" />
                  </el-collapse-item>
                  <el-collapse-item v-show="view.type && (view.type.includes('bar') || view.type.includes('line'))" name="yAxis" :title="$t('chart.yAxis')">
                    <y-axis-selector :param="param" class="attr-selector" :chart="chart" @onChangeYAxisForm="onChangeYAxisForm" />
                  </el-collapse-item>
                  <el-collapse-item v-show="view.type && view.type.includes('radar')" name="split" :title="$t('chart.split')">
                    <split-selector :param="param" class="attr-selector" :chart="chart" @onChangeSplitForm="onChangeSplitForm" />
                  </el-collapse-item>
                  <el-collapse-item name="title" :title="$t('chart.title')">
                    <title-selector :param="param" class="attr-selector" :chart="chart" @onTextChange="onTextChange" />
                  </el-collapse-item>
                  <el-collapse-item v-show="view.type && !view.type.includes('map') && !view.type.includes('table') && !view.type.includes('text')" name="legend" :title="$t('chart.legend')">
                    <legend-selector :param="param" class="attr-selector" :chart="chart" @onLegendChange="onLegendChange" />
                  </el-collapse-item>
                  <el-collapse-item name="background" :title="$t('chart.background')">
                    <background-color-selector :param="param" class="attr-selector" :chart="chart" @onChangeBackgroundForm="onChangeBackgroundForm" />
                  </el-collapse-item>
                </el-collapse>
                <!--              <el-row>-->
                <!--                <x-axis-selector v-show="view.type && (view.type.includes('bar') || view.type.includes('line'))" :param="param" class="attr-selector" :chart="chart" @onChangeXAxisForm="onChangeXAxisForm" />-->
                <!--                <y-axis-selector v-show="view.type && (view.type.includes('bar') || view.type.includes('line'))" :param="param" class="attr-selector" :chart="chart" @onChangeYAxisForm="onChangeYAxisForm" />-->
                <!--                <split-selector v-show="view.type && view.type.includes('radar')" :param="param" class="attr-selector" :chart="chart" @onChangeSplitForm="onChangeSplitForm" />-->
                <!--                <title-selector :param="param" class="attr-selector" :chart="chart" @onTextChange="onTextChange" />-->
                <!--                <legend-selector v-show="view.type && !view.type.includes('map') && !view.type.includes('table') && !view.type.includes('text')" :param="param" class="attr-selector" :chart="chart" @onLegendChange="onLegendChange" />-->
                <!--                <background-color-selector :param="param" class="attr-selector" :chart="chart" @onChangeBackgroundForm="onChangeBackgroundForm" />-->
                <!--              </el-row>-->
              </el-row>
            </div>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <el-col style="height: 100%;min-width: 500px;border-top: 1px solid #E6E6E6;">
        <el-row style="width: 100%;height: 100%;" class="padding-lr">
          <div ref="imageWrapper" style="height: 100%">
            <chart-component v-if="httpRequest.status && chart.type && !chart.type.includes('table') && !chart.type.includes('text')" :chart-id="chart.id" :chart="chart" class="chart-class" />
            <table-normal v-if="httpRequest.status && chart.type && chart.type.includes('table')" :chart="chart" class="table-class" />
            <label-normal v-if="httpRequest.status && chart.type && chart.type.includes('text')" :chart="chart" class="table-class" />
            <div v-if="!httpRequest.status" class="chart-error-class">
              <div style="font-size: 12px; color: #9ea6b2;height: 100%;display: flex;align-items: center;justify-content: center;">
                {{ httpRequest.msg }},{{ $t('chart.chart_show_error') }}
                <br>
                {{ $t('chart.chart_error_tips') }}
              </div>
            </div>
          </div>
        </el-row>
      </el-col>
    </el-row>

    <!--显示名修改-->
    <el-dialog v-dialogDrag :title="$t('chart.show_name_set')" :visible="renameItem" :show-close="false" width="30%">
      <el-form ref="itemForm" :model="itemForm" :rules="itemFormRules">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="itemForm.name" size="mini" clearable />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeRename()">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveRename">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--指标过滤器-->
    <el-dialog
      v-dialogDrag
      :title="$t('chart.add_filter')"
      :visible="quotaFilterEdit"
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <quota-filter-editor :item="quotaItem" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeQuotaFilter">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveQuotaFilter">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
    <el-dialog
      v-dialogDrag
      :title="$t('chart.add_filter')"
      :visible="dimensionFilterEdit"
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <dimension-filter-editor :item="dimensionItem" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeDimensionFilter">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveDimensionFilter">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
    <el-dialog
      v-dialogDrag
      :title="$t('chart.add_filter')"
      :visible="resultFilterEdit"
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <result-filter-editor :chart="chartForFilter" :item="filterItem" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeResultFilter">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveResultFilter">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--视图更换数据集-->
    <el-dialog
      v-dialogDrag
      :title="changeDsTitle"
      :visible="selectTableFlag"
      :show-close="false"
      width="70%"
      class="dialog-css"
      :destroy-on-close="true"
    >
      <table-selector @getTable="getTable" />
      <p style="margin-top: 10px;color:#F56C6C;font-size: 12px;">{{ $t('chart.change_ds_tip') }}</p>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeChangeChart">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" :disabled="!table || !table.id || !changeTable || !changeTable.id" @click="changeChart">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--编辑视图使用的数据集的字段-->
    <el-dialog
      :visible="editDsField"
      :show-close="false"
      class="dialog-css"
      :destroy-on-close="true"
      :fullscreen="true"
    >
      <field-edit :param="table" />
      <div slot="title" class="dialog-footer">
        <span style="font-size: 14px;">
          {{ $t('dataset.field_manage') }}
          <span v-if="table">[{{ table.name }}]</span>
        </span>
        <el-button size="mini" style="float: right;" @click="closeEditDsField">{{ $t('chart.close') }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import { ajaxGetData, post } from '@/api/chart/chart'
import draggable from 'vuedraggable'
import DimensionItem from '../components/drag-item/DimensionItem'
import QuotaItem from '../components/drag-item/QuotaItem'
import FilterItem from '../components/drag-item/FilterItem'
import ChartDragItem from '../components/drag-item/ChartDragItem'
import ResultFilterEditor from '../components/filter/ResultFilterEditor'
import ChartComponent from '../components/ChartComponent'
import bus from '@/utils/bus'
import DatasetChartDetail from '../../dataset/common/DatasetChartDetail'
// shape attr,component style
import {
  DEFAULT_BACKGROUND_COLOR,
  DEFAULT_COLOR_CASE,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_SPLIT
} from '../chart/chart'
import ColorSelector from '../components/shape-attr/ColorSelector'
import SizeSelector from '../components/shape-attr/SizeSelector'
import LabelSelector from '../components/shape-attr/LabelSelector'
import TitleSelector from '../components/component-style/TitleSelector'
import LegendSelector from '../components/component-style/LegendSelector'
import TooltipSelector from '../components/shape-attr/TooltipSelector'
import XAxisSelector from '../components/component-style/XAxisSelector'
import YAxisSelector from '../components/component-style/YAxisSelector'
import BackgroundColorSelector from '../components/component-style/BackgroundColorSelector'
import SplitSelector from '../components/component-style/SplitSelector'
import QuotaFilterEditor from '../components/filter/QuotaFilterEditor'
import DimensionFilterEditor from '../components/filter/DimensionFilterEditor'
import TableNormal from '../components/table/TableNormal'
import LabelNormal from '../components/normal/LabelNormal'
// import html2canvas from 'html2canvasde'
import TableSelector from './TableSelector'
import FieldEdit from '../../dataset/data/FieldEdit'
import { areaMapping } from '@/api/map/map'
export default {
  name: 'ChartEdit',
  components: {
    FilterItem,
    FieldEdit,
    SplitSelector,
    TableSelector,
    ResultFilterEditor,
    LabelNormal,
    DimensionFilterEditor,
    TableNormal,
    DatasetChartDetail,
    QuotaFilterEditor,
    BackgroundColorSelector,
    XAxisSelector,
    YAxisSelector,
    TooltipSelector,
    LabelSelector,
    LegendSelector,
    TitleSelector,
    SizeSelector,
    ColorSelector,
    ChartComponent,
    QuotaItem,
    DimensionItem,
    draggable,
    ChartDragItem
  },
  props: {
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      table: {},
      dimension: [],
      quota: [],
      dimensionData: [],
      quotaData: [],
      view: {
        xaxis: [],
        yaxis: [],
        extStack: [],
        show: true,
        type: 'bar',
        title: '',
        customAttr: {
          color: DEFAULT_COLOR_CASE,
          size: DEFAULT_SIZE,
          label: DEFAULT_LABEL,
          tooltip: DEFAULT_TOOLTIP
        },
        customStyle: {
          text: DEFAULT_TITLE_STYLE,
          legend: DEFAULT_LEGEND_STYLE,
          xAxis: DEFAULT_XAXIS_STYLE,
          yAxis: DEFAULT_YAXIS_STYLE,
          background: DEFAULT_BACKGROUND_COLOR,
          split: DEFAULT_SPLIT
        },
        customFilter: []
      },
      moveId: -1,
      chart: {
        id: 'echart'
      },
      dimensionFilterEdit: false,
      dimensionItem: {},
      quotaFilterEdit: false,
      quotaItem: {},
      resultFilterEdit: false,
      chartForFilter: {},
      renameItem: false,
      itemForm: {
        name: ''
      },
      itemFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' },
          { max: 50, message: this.$t('commons.char_can_not_more_50'), trigger: 'change' }
        ]
      },
      tabStatus: false,
      data: {},
      httpRequest: {
        status: true,
        msg: ''
      },
      selectTableFlag: false,
      changeTable: {},
      searchField: '',
      editDsField: false,
      changeDsTitle: '',
      filterItem: {},
      places: [],
      attrActiveNames: [],
      styleActiveNames: []
    }
  },
  computed: {
    // vId() {
    //   // console.log(this.$store.state.chart.viewId);
    //   this.getData(this.$store.state.chart.viewId)
    //   return this.$store.state.chart.viewId
    // }
  },
  watch: {
    'param': function() {
      if (this.param.optType === 'new') {
        //
      } else {
        this.getData(this.param.id)
      }
    },
    searchField(val) {
      this.fieldFilter(val)
    }
  },
  created() {
    // this.get(this.$store.state.chart.viewId);
    this.initAreas()
  },
  mounted() {
    // this.get(this.$store.state.chart.viewId);
    this.getData(this.param.id)
    // this.myEcharts();
  },
  activated() {
  },
  methods: {
    initTableData(id) {
      if (id != null) {
        post('/dataset/table/getWithPermission/' + id, null).then(response => {
          this.table = response.data
          this.initTableField(id)
        }).catch(err => {
          this.resetView()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      }
    },
    initTableField(id) {
      if (this.table) {
        post('/dataset/table/getFieldsFromDE', this.table).then(response => {
          this.dimension = response.data.dimension
          this.quota = response.data.quota
          this.dimensionData = JSON.parse(JSON.stringify(this.dimension))
          this.quotaData = JSON.parse(JSON.stringify(this.quota))
          this.fieldFilter(this.searchField)
        }).catch(err => {
          this.resetView()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      } else {
        this.resetDatasetField()
      }
    },
    save(getData, trigger, needRefreshGroup = false) {
      const view = JSON.parse(JSON.stringify(this.view))
      view.id = this.view.id
      view.sceneId = this.view.sceneId
      view.name = this.view.title ? this.view.title : this.table.name
      if (view.title.length > 50) {
        this.$error(this.$t('chart.title_limit'))
        return
      }
      view.tableId = this.view.tableId
      view.xaxis.forEach(function(ele) {
        // if (!ele.summary || ele.summary === '') {
        //   ele.summary = 'sum'
        // }
        if (!ele.dateStyle || ele.dateStyle === '') {
          ele.dateStyle = 'y_M_d'
        }
        if (!ele.datePattern || ele.datePattern === '') {
          ele.datePattern = 'date_sub'
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
      })
      view.yaxis.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
            ele.summary = 'count'
          } else {
            ele.summary = 'sum'
          }
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
      })
      view.extStack.forEach(function(ele) {
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
      })
      if (view.type.startsWith('pie') || view.type.startsWith('funnel') || view.type.startsWith('text') || view.type.startsWith('gauge')) {
        if (view.yaxis.length > 1) {
          view.yaxis.splice(1, view.yaxis.length)
        }
      }
      if (view.type === 'line' && trigger === 'chart') {
        view.customAttr.size.lineArea = false
      }
      if (view.type === 'line-stack' && trigger === 'chart') {
        view.customAttr.size.lineArea = true
      }
      view.customFilter.forEach(function(ele) {
        if (ele && !ele.filter) {
          ele.filter = []
        }
      })
      view.xaxis = JSON.stringify(view.xaxis)
      view.yaxis = JSON.stringify(view.yaxis)
      view.customAttr = JSON.stringify(view.customAttr)
      view.customStyle = JSON.stringify(view.customStyle)
      view.customFilter = JSON.stringify(view.customFilter)
      view.extStack = JSON.stringify(view.extStack)
      post('/chart/view/save', view).then(response => {
        // this.get(response.data.id);
        // this.getData(response.data.id)

        if (getData) {
          this.getData(response.data.id)
        } else {
          this.getChart(response.data.id)
        }

        // this.$store.dispatch('chart/setChartSceneData', null)
        // this.$store.dispatch('chart/setChartSceneData', response.data)
        if (needRefreshGroup) {
          this.refreshGroup(view)
        }
        this.closeChangeChart()
      })
    },

    // saveSnapshot() {
    //   if (this.view.title && this.view.title.length > 50) {
    //     this.$warning(this.$t('chart.title_limit'))
    //     return
    //   }
    //   if (this.loading) {
    //     return
    //   }
    //   this.loading = true
    //   html2canvas(this.$refs.imageWrapper).then(canvas => {
    //     const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.1是图片质量
    //     if (snapshot !== '') {
    //       const view = JSON.parse(JSON.stringify(this.view))
    //       view.id = this.view.id
    //       view.sceneId = this.view.sceneId
    //       view.name = this.view.name ? this.view.name : this.table.name
    //       view.tableId = this.view.tableId
    //       view.xaxis.forEach(function(ele) {
    //         // if (!ele.summary || ele.summary === '') {
    //         //   ele.summary = 'sum'
    //         // }
    //         if (!ele.sort || ele.sort === '') {
    //           ele.sort = 'none'
    //         }
    //         if (!ele.filter) {
    //           ele.filter = []
    //         }
    //       })
    //       view.yaxis.forEach(function(ele) {
    //         if (!ele.summary || ele.summary === '') {
    //           if (ele.id === 'count') {
    //             ele.summary = 'count'
    //           } else {
    //             ele.summary = 'sum'
    //           }
    //         }
    //         if (!ele.sort || ele.sort === '') {
    //           ele.sort = 'none'
    //         }
    //         if (!ele.filter) {
    //           ele.filter = []
    //         }
    //       })
    //       if (view.type.startsWith('pie') || view.type.startsWith('funnel') || view.type.startsWith('gauge')) {
    //         if (view.yaxis.length > 1) {
    //           view.yaxis.splice(1, view.yaxis.length)
    //         }
    //       }
    //       view.xaxis = JSON.stringify(view.xaxis)
    //       view.yaxis = JSON.stringify(view.yaxis)
    //       view.customAttr = JSON.stringify(view.customAttr)
    //       view.customStyle = JSON.stringify(view.customStyle)
    //       view.customFilter = JSON.stringify(view.customFilter)
    //       view.snapshot = snapshot
    //       post('/chart/view/save', view).then(response => {
    //         this.loading = false
    //         this.$success(this.$t('commons.save_success'))
    //       })
    //     }
    //   })
    // },
    closeEdit() {
      if (this.view.title && this.view.title.length > 50) {
        this.$warning(this.$t('chart.title_limit'))
        return
      }
      // 从仪表板入口关闭
      bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
      // this.$emit('switchComponent', { name: '' })
      this.$success(this.$t('commons.save_success'))
    },
    getData(id) {
      if (id) {
        ajaxGetData(id, {
          filter: []
        }).then(response => {
          this.initTableData(response.data.tableId)
          this.view = JSON.parse(JSON.stringify(response.data))
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
          this.view.extStack = this.view.extStack ? JSON.parse(this.view.extStack) : []
          this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
          this.view.customStyle = this.view.customStyle ? JSON.parse(this.view.customStyle) : {}
          this.view.customFilter = this.view.customFilter ? JSON.parse(this.view.customFilter) : {}
          // 将视图传入echart组件
          this.chart = response.data
          this.data = response.data.data
          // console.log(JSON.stringify(this.chart))
          this.httpRequest.status = true
          if (this.chart.privileges) {
            this.param.privileges = this.chart.privileges
          }
        }).catch(err => {
          this.resetView()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          this.$nextTick(() => {
            this.getChart(id)
          })
          return true
        })
      } else {
        this.view = {}
      }
    },
    getChart(id) {
      if (id) {
        post('/chart/view/get/' + id, {}).then(response => {
          this.initTableData(response.data.tableId)
          this.view = JSON.parse(JSON.stringify(response.data))
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
          this.view.extStack = this.view.extStack ? JSON.parse(this.view.extStack) : []
          this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
          this.view.customStyle = this.view.customStyle ? JSON.parse(this.view.customStyle) : {}
          this.view.customFilter = this.view.customFilter ? JSON.parse(this.view.customFilter) : {}

          response.data.data = this.data
          this.chart = response.data

          // this.httpRequest.status = true
        }).catch(err => {
          // this.resetView()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      } else {
        this.view = {}
      }
    },

    // 左边往右边拖动时的事件
    // start1(e) {
    //   // console.log(e)
    //   e.clone.className = 'item'
    //   e.item.className = 'item'
    // },
    // end1(e) {
    //   // console.log(e)
    //   e.clone.className = 'item'
    //   e.item.className = 'item'
    //   this.refuseMove(e)
    //   this.removeCheckedKey(e)
    //   this.save(true)
    // },
    // 右边往左边拖动时的事件
    // start2(e) {
    // console.log(e)
    // },
    // end2(e) {
    //   // console.log(e)
    //   this.removeDuplicateKey(e)
    //   this.save(true)
    // },
    // removeCheckedKey(e) {
    //   const that = this
    //   const xItems = this.view.xaxis.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   const yItems = this.view.yaxis.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   if (xItems && xItems.length > 1) {
    //     this.view.xaxis.splice(e.newDraggableIndex, 1)
    //   }
    //   if (yItems && yItems.length > 1) {
    //     this.view.yaxis.splice(e.newDraggableIndex, 1)
    //   }
    // },
    // refuseMove(e) {
    //   const that = this
    //   const xItems = this.dimension.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   const yItems = this.quota.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   if (xItems && xItems.length > 1) {
    //     this.dimension.splice(e.newDraggableIndex, 1)
    //   }
    //   if (yItems && yItems.length > 1) {
    //     this.quota.splice(e.newDraggableIndex, 1)
    //   }
    // },
    // removeDuplicateKey(e) {
    //   const that = this
    //   const xItems = this.dimension.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   const yItems = this.quota.filter(function(m) {
    //     return m.id === that.moveId
    //   })
    //   if (xItems && xItems.length > 1) {
    //     this.dimension.splice(e.newDraggableIndex, 1)
    //   }
    //   if (yItems && yItems.length > 1) {
    //     this.quota.splice(e.newDraggableIndex, 1)
    //   }
    // },
    // move回调方法
    onMove(e, originalEvent) {
      // console.log(e)
      this.moveId = e.draggedContext.element.id
      return true
    },

    dimensionItemChange(item) {
      this.save(true)
    },

    dimensionItemRemove(item) {
      this.view.xaxis.splice(item.index, 1)
      this.save(true)
    },

    quotaItemChange(item) {
      // 更新item
      // this.view.yaxis.forEach(function(ele) {
      //   if (ele.id === item.id) {
      //     ele.summary = item.summary
      //   }
      // })
      this.save(true)
    },

    quotaItemRemove(item) {
      this.view.yaxis.splice(item.index, 1)
      this.save(true)
    },

    onColorChange(val) {
      this.view.customAttr.color = val
      this.save()
    },

    onSizeChange(val) {
      this.view.customAttr.size = val
      this.save()
    },

    onTextChange(val) {
      this.view.customStyle.text = val
      this.view.title = val.title
      this.save(false, '', true)
    },

    onLegendChange(val) {
      this.view.customStyle.legend = val
      this.save()
    },

    onLabelChange(val) {
      this.view.customAttr.label = val
      this.save()
    },

    onTooltipChange(val) {
      this.view.customAttr.tooltip = val
      this.save()
    },

    onChangeXAxisForm(val) {
      this.view.customStyle.xAxis = val
      this.save()
    },

    onChangeYAxisForm(val) {
      this.view.customStyle.yAxis = val
      this.save()
    },

    onChangeBackgroundForm(val) {
      this.view.customStyle.background = val
      this.save()
    },

    onChangeSplitForm(val) {
      this.view.customStyle.split = val
      this.save()
    },

    showDimensionEditFilter(item) {
      this.dimensionItem = JSON.parse(JSON.stringify(item))
      this.dimensionFilterEdit = true
    },
    closeDimensionFilter() {
      this.dimensionFilterEdit = false
    },
    saveDimensionFilter() {
      for (let i = 0; i < this.dimensionItem.filter.length; i++) {
        const f = this.dimensionItem.filter[i]
        if (!f.term.includes('null') && (!f.value || f.value === '')) {
          this.$message({
            message: this.$t('chart.filter_value_can_null'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.view.xaxis[this.dimensionItem.index].filter = this.dimensionItem.filter
      this.save(true)
      this.closeDimensionFilter()
    },

    showQuotaEditFilter(item) {
      this.quotaItem = JSON.parse(JSON.stringify(item))
      this.quotaFilterEdit = true
    },
    closeQuotaFilter() {
      this.quotaFilterEdit = false
    },
    saveQuotaFilter() {
      for (let i = 0; i < this.quotaItem.filter.length; i++) {
        const f = this.quotaItem.filter[i]
        if (!f.term.includes('null') && (!f.value || f.value === '')) {
          this.$message({
            message: this.$t('chart.filter_value_can_null'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.view.yaxis[this.quotaItem.index].filter = this.quotaItem.filter
      this.save(true)
      this.closeQuotaFilter()
    },

    filterItemRemove(item) {
      this.view.customFilter.splice(item.index, 1)
      this.save(true)
    },
    showEditFilter(item) {
      this.filterItem = JSON.parse(JSON.stringify(item))
      this.chartForFilter = JSON.parse(JSON.stringify(this.view))
      this.resultFilterEdit = true
    },
    closeResultFilter() {
      this.resultFilterEdit = false
    },
    saveResultFilter() {
      for (let i = 0; i < this.filterItem.filter.length; i++) {
        const f = this.filterItem.filter[i]
        if (!f.term.includes('null') && (!f.value || f.value === '')) {
          this.$message({
            message: this.$t('chart.filter_value_can_null'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.view.customFilter[this.filterItem.index].filter = this.filterItem.filter
      this.save(true)
      this.closeResultFilter()
    },

    showRename(val) {
      this.itemForm = JSON.parse(JSON.stringify(val))
      this.renameItem = true
    },
    saveRename() {
      this.$refs['itemForm'].validate((valid) => {
        if (valid) {
          if (this.itemForm.renameType === 'quota') {
            this.view.yaxis[this.itemForm.index].name = this.itemForm.name
          } else if (this.itemForm.renameType === 'dimension') {
            this.view.xaxis[this.itemForm.index].name = this.itemForm.name
          }
          this.save(true)
          this.closeRename()
        } else {
          return false
        }
      })
    },
    closeRename() {
      this.renameItem = false
      this.resetRename()
    },
    resetRename() {
      // this.itemForm = {}
    },

    showTab() {
      this.tabStatus = true
    },
    hideTab() {
      this.tabStatus = false
    },
    resetDatasetField() {
      this.dimension = []
      this.dimensionData = []
      this.quota = []
      this.quotaData = []
    },
    resetView() {
      this.resetDatasetField()
      this.view = {
        xAxis: [],
        yAxis: [],
        type: ''
      }
    },

    refreshGroup(view) {
      this.$emit('saveSuccess', view)
    },

    getTable(table) {
      this.changeTable = JSON.parse(JSON.stringify(table))
    },

    changeDs() {
      const dialogTitle = (this.table && this.table.name) ? ('[' + this.table.name + ']') : ''
      this.changeDsTitle = this.$t('chart.change_ds') + dialogTitle
      this.selectTableFlag = true
    },

    closeChangeChart() {
      this.selectTableFlag = false
    },

    changeChart() {
      if (this.view.tableId !== this.changeTable.id) {
        this.view.tableId = this.changeTable.id
        this.view.xaxis = []
        this.view.yaxis = []
        this.view.customFilter = []
      }
      this.save(true, 'chart', false)
    },

    fieldFilter(val) {
      if (val && val !== '') {
        this.dimensionData = JSON.parse(JSON.stringify(this.dimension.filter(ele => { return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) })))
        this.quotaData = JSON.parse(JSON.stringify(this.quota.filter(ele => { return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) })))
      } else {
        this.dimensionData = JSON.parse(JSON.stringify(this.dimension))
        this.quotaData = JSON.parse(JSON.stringify(this.quota))
      }
    },

    editField() {
      this.editDsField = true
    },

    closeEditDsField() {
      this.editDsField = false
      this.initTableField()
    },

    // drag
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
    addXaxis(e) {
      this.dragCheckType(this.view.xaxis, 'd')
      this.dragMoveDuplicate(this.view.xaxis, e)
      this.save(true)
    },
    addYaxis(e) {
      this.dragCheckType(this.view.yaxis, 'q')
      this.dragMoveDuplicate(this.view.yaxis, e)
      this.save(true)
    },
    moveToDimension(e) {
      this.dragCheckType(this.dimensionData, 'd')
      this.dragMoveDuplicate(this.dimensionData, e)
      this.save(true)
    },
    moveToQuota(e) {
      this.dragCheckType(this.quotaData, 'q')
      this.dragMoveDuplicate(this.quotaData, e)
      this.save(true)
    },
    addCustomFilter(e) {
      // 记录数等自动生成字段不做为过滤条件
      if (this.view.customFilter && this.view.customFilter.length > 0) {
        for (let i = 0; i < this.view.customFilter.length; i++) {
          if (this.view.customFilter[i].id === 'count') {
            this.view.customFilter.splice(i, 1)
          }
        }
      }
      this.dragMoveDuplicate(this.view.customFilter, e)
      this.save(true)
    },

    initAreas() {
      let mapping
      if ((mapping = localStorage.getItem('areaMapping')) !== null) {
        this.places = JSON.parse(mapping)
        return
      }
      Object.keys(this.places).length === 0 && areaMapping().then(res => {
        this.places = res.data
        localStorage.setItem('areaMapping', JSON.stringify(res.data))
      })
    },

    normalizer(node) {
      const resultNode = {
        id: node.code,
        label: node.name
      }
      if (node.children && node.children.length > 0) {
        resultNode.children = node.children
      }

      if (resultNode.children && (!node.children || node.children.length === 0)) {
        delete resultNode.children
      }
      return resultNode
    },
    addStack(e) {
      this.dragCheckType(this.dimensionData, 'd')
      if (this.view.extStack && this.view.extStack.length > 1) {
        this.view.extStack = [this.view.extStack[0]]
      }
      this.save(true)
    },
    stackItemChange(item) {
      this.save(true)
    },
    stackItemRemove(item) {
      this.view.extStack.splice(item.index, 1)
      this.save(true)
    }
  }
}
</script>

<style lang='scss' scoped>
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

  .view-panel {
    display: flex;
    height: calc(100% - 40px);
    background-color: #f7f8fa;
  }

  .drag-list {
    height: calc(100% - 26px);
    overflow:auto;
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

  .item-quota {
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

  .el-form-item {
    margin-bottom: 0;
  }

  span {
    font-size: 12px;
  }

  .tab-header>>>.el-tabs__item{
    font-size: 12px;
    background-color: #E8EAED;
  }

  .tab-header>>>.is-active{
    background-color: #f7f8fa;
    border-bottom-color: #f7f8fa!important;
  }
  .tab-header>>>.el-tabs__nav-scroll{
    padding-left: 0!important;
  }
  .tab-header>>>.el-tabs__header{
    margin: 0!important;
  }
  .tab-header>>>.el-tabs__content{
    height: 100%;
  }

  .draggable-group {
    display: block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .chart-type{
    padding: 4px;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .chart-icon{
    width: 20px;
    height: 20px;
  }

  .chart-type>>>.el-radio__input{
    display: none;
  }

  .el-radio{
    margin:5px;
  }

  .el-radio>>>.el-radio__label{
    padding-left: 0;
  }

  .attr-style{
    height: calc(100vh - 56px - 25vh - 40px - 40px);
  }

  .attr-selector{
    width:100%;
    height: 100%;
    margin:6px 0;
    padding:0 4px;
    display: flex;
    align-items: center;
    background-color: white
  }

  .style-collapse>>>.el-collapse-item__content {
    padding-bottom: 0!important;
  }
  .style-collapse>>>.el-collapse-item__header {
    height: 34px;
    line-height: 34px;
    padding: 0 0 0 6px;
    font-size: 12px;
    font-weight: 400;
  }

  .disabled-none-cursor{
    cursor: not-allowed;
    pointer-events:none;
  }

  .chart-class{
    height: 100%;
    padding: 10px;
  }
  .table-class{
    height: calc(100% - 20px);
    margin: 10px;
  }

  .dialog-css>>>.el-dialog__title {
    font-size: 14px;
  }
  .dialog-css >>> .el-dialog__header {
    padding: 20px 20px 0;
  }
  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .filter-btn-class{
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .chart-error-class{
    text-align: center;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #ece7e7;
  }
  .field-height{
    height: calc(50% - 20px);
    border-top: 1px solid #E6E6E6;
  }
  .padding-tab{
    padding: 0;
    height: 100%;
  }
  .tree-select-span {
      >>>div.vue-treeselect__control {
          height: 32px !important;
          font-weight: normal !important;
      }
  }
  .drag-block-style{
    padding:2px 0 0 0;
    width:100%;
    min-height: 32px;
    border-radius: 4px;
    border: 1px solid #DCDFE6;
    overflow-x: hidden;
    display: flex;
    align-items: center;
    background-color: white;
  }
  .drag-placeholder-style{
    position: absolute;
    top: calc(50% - 2px);
    left: 0;
    width: 100%;
    color: #CCCCCC;
  }
  .drag-placeholder-style-span{
    padding-left: 16px;
  }
</style>
