<template>
  <el-row v-loading="loading" style="height: 100%;overflow-y: hidden;width: 100%;border-left: 1px solid #E6E6E6">
    <el-tooltip :content="$t('chart.draw_back')">
      <el-button
        class="el-icon-d-arrow-right"
        style="position:absolute;left: 4px;top: 5px;z-index: 1"
        size="mini"
        circle
        @click="closePanelEdit"
      />
    </el-tooltip>
    <el-row style="height: 40px;" class="padding-lr">
      <el-popover
        placement="right-start"
        width="400"
        trigger="click"
        @show="showTab"
        @hide="hideTab"
      >
        <dataset-chart-detail type="chart" :data="view" :tab-status="tabStatus" />
        <i
          slot="reference"
          class="el-icon-warning icon-class"
          style="position:absolute; margin-left: 30px; top:14px;cursor: pointer;"
        />
      </el-popover>
      <span class="title-text view-title-name" style="line-height: 40px;">{{ view.name }}</span>
      <span style="float: right;line-height: 40px;">
        <el-button round size="mini" :disabled="!hasEdit" @click="reset">
          {{ $t('chart.recover') }}
        </el-button>
      </span>
    </el-row>
    <el-row class="view-panel-row">
      <el-tabs v-model="tabActive" :stretch="true" class="tab-header">
        <el-tab-pane name="data" :label="$t('chart.chart_data')" class="padding-tab" style="width: 350px">
          <div v-if="view.dataFrom==='template'" class="view-panel-Mask">
            <span style="opacity: 1;">
              <el-button
                style="opacity: 1!important;"
                type="warning"
                :title="$t('chart.change_ds')"
                size="mini"
                round
                @click="changeDs"
              >
                <span style="font-weight: bold">{{ $t('panel.template_view_tips') }}<i class="el-icon-refresh el-icon--right" /></span>
              </el-button>
            </span>
          </div>
          <el-row class="view-panel">
            <el-col class="theme-border-class" :span="11" style="border-right: 1px solid #E6E6E6;">
              <div style="display: flex;align-items: center;justify-content: center;padding: 6px;">
                <el-input
                  v-model="searchField"
                  size="mini"
                  :placeholder="$t('chart.search')"
                  prefix-icon="el-icon-search"
                  clearable
                  class="main-area-input"
                />
                <el-dropdown trigger="click" size="mini" @command="fieldEdit">
                  <span class="el-dropdown-link">
                    <el-button
                      :title="$t('dataset.field_manage')"
                      icon="el-icon-setting"
                      type="text"
                      size="mini"
                      style="float: right;width: 20px;margin-left: 4px;"
                    />
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item
                      :disabled="!table || !hasDataPermission('manage',table.privileges)"
                      :command="beforeFieldEdit('ds')"
                      icon="el-icon-s-grid"
                    >
                      {{ $t('chart.ds_field_edit') }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="beforeFieldEdit('chart')"
                      icon="el-icon-s-data"
                    >
                      {{ $t('chart.chart_field_edit') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
                <el-button
                  :title="$t('chart.change_ds')"
                  icon="el-icon-refresh"
                  type="text"
                  size="mini"
                  style="float: right;width: 20px;margin-left: 4px;"
                  @click="changeDs"
                />
              </div>

              <div v-if="fieldShow && tabActive === 'data'" class="field-split">
                <fu-split-pane top="50%" direction="vertical">
                  <template v-slot:top>
                    <div class="padding-lr field-height">
                      <span>{{ $t('chart.dimension') }}</span>
                      <draggable
                        v-if="table && hasDataPermission('use',table.privileges)"
                        v-model="dimensionData"
                        :options="{group:{name: 'drag',pull:'clone'},sort: true}"
                        animation="300"
                        :move="onMove"
                        class="drag-list"
                        @add="moveToDimension"
                      >
                        <transition-group>
                          <span v-for="item in dimensionData" :key="item.id" class="item-dimension father" :title="item.name">
                            <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                            <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                            <svg-icon
                              v-if="item.deType === 2 || item.deType === 3"
                              icon-class="field_value"
                              class="field-icon-value"
                            />
                            <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                            <span class="field-name">{{ item.name }}</span>
                            <el-dropdown v-show="false" placement="right-start" trigger="click" size="mini" class="field-setting child" @command="chartFieldEdit">
                              <span class="el-dropdown-link">
                                <i class="el-icon-s-tools" />
                              </span>
                              <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item :command="handleChartFieldEdit(item,'copy')" icon="el-icon-document-copy">{{ $t('commons.copy') }}</el-dropdown-item>
                                <span v-if="item.chartId">
                                  <el-dropdown-item :command="handleChartFieldEdit(item,'edit')" icon="el-icon-edit">{{ $t('commons.edit') }}</el-dropdown-item>
                                  <el-dropdown-item :command="handleChartFieldEdit(item,'delete')" icon="el-icon-delete">{{ $t('commons.delete') }}</el-dropdown-item>
                                </span>
                              </el-dropdown-menu>
                            </el-dropdown>
                          </span>
                        </transition-group>
                      </draggable>
                    </div>
                  </template>
                  <template v-slot:bottom>
                    <div class="padding-lr field-height">
                      <span>{{ $t('chart.quota') }}</span>
                      <draggable
                        v-if="table && hasDataPermission('use',table.privileges)"
                        v-model="quotaData"
                        :options="{group:{name: 'drag',pull:'clone'},sort: true}"
                        animation="300"
                        :move="onMove"
                        class="drag-list"
                        @add="moveToQuota"
                      >
                        <transition-group>
                          <span
                            v-for="item in quotaData"
                            v-show="chart.type && (chart.type !== 'table-info' || (chart.type === 'table-info' && item.id !=='count'))"
                            :key="item.id"
                            class="item-quota father"
                            :title="item.name"
                          >
                            <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                            <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                            <svg-icon
                              v-if="item.deType === 2 || item.deType === 3"
                              icon-class="field_value"
                              class="field-icon-value"
                            />
                            <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                            <span class="field-name">{{ item.name }}</span>
                            <el-dropdown v-show="false" v-if="item.id !== 'count'" placement="right-start" trigger="click" size="mini" class="field-setting child" @command="chartFieldEdit">
                              <span class="el-dropdown-link">
                                <i class="el-icon-s-tools" />
                              </span>
                              <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item :command="handleChartFieldEdit(item,'copy')" icon="el-icon-document-copy">{{ $t('commons.copy') }}</el-dropdown-item>
                                <span v-if="item.chartId">
                                  <el-dropdown-item :command="handleChartFieldEdit(item,'edit')" icon="el-icon-edit">{{ $t('commons.edit') }}</el-dropdown-item>
                                  <el-dropdown-item :command="handleChartFieldEdit(item,'delete')" icon="el-icon-delete">{{ $t('commons.delete') }}</el-dropdown-item>
                                </span>
                              </el-dropdown-menu>
                            </el-dropdown>
                          </span>
                        </transition-group>
                      </draggable>
                    </div>
                  </template>
                </fu-split-pane>
              </div>
            </el-col>

            <el-col
              :span="13"
              style="height: 100%;border-right: 1px solid #E6E6E6;"
              class="theme-border-class"
            >
              <div style="height: 60px;overflow:auto" class="padding-lr theme-border-class">
                <span class="theme-border-class">
                  <span>{{ $t('chart.chart_type') }}</span>
                  <el-row style="padding: 4px 0 4px 10px;">
                    <span>
                      <svg-icon :icon-class="view.isPlugin && view.type && view.type !== 'buddle-map' ? ('/api/pluginCommon/staticInfo/' + view.type + '/svg') : view.type" class="chart-icon" />
                    </span>
                    <span style="float: right;">
                      <el-popover
                        placement="bottom-end"
                        width="400"
                        trigger="click"
                        :append-to-body="true"
                      >
                        <div class="padding-lr">
                          <span>
                            <span class="theme-border-class">{{ $t('chart.chart_type') }}</span>
                            <span style="float: right;">
                              <el-select
                                v-model="view.render"
                                class="render-select"
                                style="width: 100px"
                                size="mini"
                                @change="changeChartRender()"
                              >
                                <el-option
                                  v-for="item in pluginRenderOptions"
                                  :key="item.value"
                                  :value="item.value"
                                  :label="item.name"
                                />
                              </el-select>
                            </span>
                          </span>
                          <el-row>
                            <div>
                              <el-radio-group
                                v-model="view.type"
                                style="width: 100%"
                                @change="changeChartType()"
                              >
                                <chart-type ref="cu-chart-type" :chart="view" style="height: 480px" />
                              </el-radio-group>
                            </div>
                          </el-row>
                        </div>
                        <el-button
                          slot="reference"
                          size="mini"
                          style="padding: 6px;"
                        >
                          {{ $t('chart.change_chart_type') }}
                          <i class="el-icon-caret-bottom" />
                        </el-button>
                      </el-popover>
                    </span>
                  </el-row>
                </span>
              </div>
              <div style="overflow:auto;border-top: 1px solid #e6e6e6" class="attr-style theme-border-class">
                <el-row style="height: 100%;">
                  <el-row class="padding-lr">
                    <span v-show="view.type==='richTextView'" style="color: #909399; font-size: 8px;width: 80px;text-align: right;">
                      Tips:{{ $t('chart.rich_text_view_result_tips') }}
                    </span>
                    <span v-show="view.type!=='richTextView'" style="width: 80px;text-align: right;">
                      {{ $t('chart.result_count') }}
                    </span>
                    <el-row v-show="view.type!=='richTextView'">
                      <el-radio-group
                        v-model="view.resultMode"
                        class="radio-span"
                        size="mini"
                        @change="calcData"
                      >
                        <el-radio label="all"><span>{{ $t('chart.result_mode_all') }}</span></el-radio>
                        <el-radio label="custom">
                          <el-input
                            v-model="view.resultCount"
                            class="result-count"
                            size="mini"
                            @change="calcData"
                          />
                        </el-radio>
                      </el-radio-group>
                    </el-row>
                  </el-row>

                  <plugin-com
                    v-if="view.isPlugin"
                    :component-name="view.type + '-data'"
                    :obj="{view, param, chart, dimensionData, quotaData}"
                  />
                  <div v-else>

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
                          :no-children-text="$t('commons.treeselect.no_children_text')"
                          :no-options-text="$t('commons.treeselect.no_options_text')"
                          :no-results-text="$t('commons.treeselect.no_results_text')"
                          @input="calcData"
                          @deselect="calcData"
                        />
                      </span>
                    </el-row>

                    <!--xAxisExt-->
                    <el-row
                      v-if="view.type === 'table-pivot'"
                      class="padding-lr"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.table_pivot_row') }}</span>
                        /
                        <span>{{ $t('chart.dimension') }}</span>
                      </span>
                      <draggable
                        v-model="view.xaxisExt"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addXaxisExt"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <dimension-ext-item
                            v-for="(item,index) in view.xaxisExt"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onDimensionItemChange="dimensionItemChange"
                            @onDimensionItemRemove="dimensionItemRemove"
                            @editItemFilter="showDimensionEditFilter"
                            @onNameEdit="showRename"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.xaxisExt || view.xaxisExt.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--xAxis-->
                    <el-row
                      v-if="view.type !=='text' && view.type !== 'gauge' && view.type !== 'liquid'"
                      class="padding-lr"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span v-if="view.type && view.type.includes('table')">{{
                          $t('chart.drag_block_table_data_column')
                        }}</span>
                        <span
                          v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall' || view.type === 'area')"
                        >{{ $t('chart.drag_block_type_axis') }}</span>
                        <span
                          v-else-if="view.type && view.type.includes('pie')"
                        >{{ $t('chart.drag_block_pie_label') }}</span>
                        <span v-else-if="view.type && view.type.includes('funnel')">{{
                          $t('chart.drag_block_funnel_split')
                        }}</span>
                        <span v-else-if="view.type && view.type.includes('radar')">{{
                          $t('chart.drag_block_radar_label')
                        }}</span>
                        <span v-else-if="view.type && view.type === 'map'">{{ $t('chart.area') }}</span>
                        <span v-else-if="view.type && view.type.includes('treemap')">{{
                          $t('chart.drag_block_treemap_label')
                        }}</span>
                        <span v-else-if="view.type && view.type === 'word-cloud'">{{
                          $t('chart.drag_block_word_cloud_label')
                        }}</span>
                        <span v-else-if="view.type && view.type === 'label'">{{ $t('chart.drag_block_label') }}</span>
                        <span v-show="view.type !== 'richTextView'"> / </span>
                        <span v-if="view.type && view.type !== 'table-info'">{{ $t('chart.dimension') }}</span>
                        <span
                          v-else-if="view.type && view.type === 'table-info'"
                        >{{ $t('chart.dimension_or_quota') }}</span>
                      </span>
                      <draggable
                        v-model="view.xaxis"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addXaxis"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <dimension-item
                            v-for="(item,index) in view.xaxis"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            :chart="chart"
                            @onDimensionItemChange="dimensionItemChange"
                            @onDimensionItemRemove="dimensionItemRemove"
                            @editItemFilter="showDimensionEditFilter"
                            @onNameEdit="showRename"
                            @valueFormatter="valueFormatter"
                            @onCustomSort="onCustomSort"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.xaxis || view.xaxis.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--group field,use xaxisExt-->
                    <el-row
                      v-if="view.type === 'bar-group'"
                      class="padding-lr"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span>
                          {{ $t('chart.chart_group') }}
                          <span style="color:#F54A45;">*</span>
                        </span>
                        /
                        <span>{{ $t('chart.dimension') }}</span>
                        <el-tooltip class="item" effect="dark" placement="bottom">
                          <div slot="content">
                            该字段为必填项，且不应使用类别轴中字段，若无需该字段，请选择基础柱状图展示；否则展示结果不理想
                          </div>
                          <i class="el-icon-info" style="cursor: pointer;color: #606266;" />
                        </el-tooltip>
                      </span>
                      <draggable
                        v-model="view.xaxisExt"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addXaxisExt"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <dimension-ext-item
                            v-for="(item,index) in view.xaxisExt"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onDimensionItemChange="dimensionItemChange"
                            @onDimensionItemRemove="dimensionItemRemove"
                            @editItemFilter="showDimensionEditFilter"
                            @onNameEdit="showRename"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.xaxisExt || view.xaxisExt.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--yaxis-->
                    <el-row
                      v-if="view.type !=='table-info' && view.type !=='label'"
                      class="padding-lr"
                      style="margin-top: 6px;"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span v-if="view.type && view.type.includes('table')">{{
                          $t('chart.drag_block_table_data_column')
                        }}</span>
                        <span
                          v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'waterfall' || view.type === 'area')"
                        >{{ $t('chart.drag_block_value_axis') }}</span>
                        <span
                          v-else-if="view.type && view.type.includes('pie')"
                        >{{ $t('chart.drag_block_pie_angel') }}</span>
                        <span v-else-if="view.type && view.type.includes('funnel')">{{
                          $t('chart.drag_block_funnel_width')
                        }}</span>
                        <span v-else-if="view.type && view.type.includes('radar')">{{
                          $t('chart.drag_block_radar_length')
                        }}</span>
                        <span v-else-if="view.type && view.type.includes('gauge')">{{
                          $t('chart.drag_block_gauge_angel')
                        }}</span>
                        <span
                          v-else-if="view.type && view.type.includes('text')"
                        >{{ $t('chart.drag_block_label_value') }}</span>
                        <span v-else-if="view.type && view.type === 'map'">{{ $t('chart.chart_data') }}</span>
                        <span v-else-if="view.type && view.type.includes('tree')">{{
                          $t('chart.drag_block_treemap_size')
                        }}</span>
                        <span v-else-if="view.type && view.type === 'chart-mix'">{{
                          $t('chart.drag_block_value_axis_main')
                        }}</span>
                        <span
                          v-else-if="view.type && view.type === 'liquid'"
                        >{{ $t('chart.drag_block_progress') }}</span>
                        <span v-else-if="view.type && view.type === 'word-cloud'">{{
                          $t('chart.drag_block_word_cloud_size')
                        }}</span>
                        <span v-show="view.type !== 'richTextView'"> / </span>
                        <span>{{ $t('chart.quota') }}</span>
                      </span>
                      <draggable
                        v-model="view.yaxis"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addYaxis"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <quota-item
                            v-for="(item,index) in view.yaxis"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :chart="chart"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onQuotaItemChange="quotaItemChange"
                            @onQuotaItemRemove="quotaItemRemove"
                            @editItemFilter="showQuotaEditFilter"
                            @onNameEdit="showRename"
                            @editItemCompare="showQuotaEditCompare"
                            @valueFormatter="valueFormatter"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.yaxis || view.yaxis.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--yAxisExt-->
                    <el-row v-if="view.type && view.type === 'chart-mix'" class="padding-lr" style="margin-top: 6px;">
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.drag_block_value_axis_ext') }}</span>
                        /
                        <span>{{ $t('chart.quota') }}</span>
                      </span>
                      <draggable
                        v-model="view.yaxisExt"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addYaxisExt"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <quota-ext-item
                            v-for="(item,index) in view.yaxisExt"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :chart="chart"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onQuotaItemChange="quotaItemChange"
                            @onQuotaItemRemove="quotaItemRemove"
                            @editItemFilter="showQuotaEditFilter"
                            @onNameEdit="showRename"
                            @editItemCompare="showQuotaEditCompare"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.yaxisExt || view.yaxisExt.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--extStack-->
                    <el-row v-if="view.type && view.type.includes('stack')" class="padding-lr" style="margin-top: 6px;">
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.stack_item') }}</span>
                        /
                        <span>{{ $t('chart.dimension') }}</span>
                      </span>
                      <draggable
                        v-model="view.extStack"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addStack"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <chart-drag-item
                            v-for="(item,index) in view.extStack"
                            :key="item.id"
                            :conf="'sort'"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onItemChange="stackItemChange"
                            @onItemRemove="stackItemRemove"
                            @onItemCustomSort="stackItemCustomSort"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.extStack || view.extStack.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <!--extBubble-->
                    <el-row
                      v-if="view.type && view.type.includes('scatter')"
                      class="padding-lr"
                      style="margin-top: 6px;"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.bubble_size') }}</span>
                        /
                        <span>{{ $t('chart.quota') }}</span>
                        <el-tooltip class="item" effect="dark" placement="bottom">
                          <div slot="content">
                            该指标生效时，样式大小中的气泡大小属性将失效
                          </div>
                          <i class="el-icon-info" style="cursor: pointer;color: #606266;" />
                        </el-tooltip>
                      </span>
                      <draggable
                        v-model="view.extBubble"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addBubble"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <chart-drag-item
                            v-for="(item,index) in view.extBubble"
                            :key="item.id"
                            :conf="'summary'"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onItemChange="bubbleItemChange"
                            @onItemRemove="bubbleItemRemove"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.extBubble || view.extBubble.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <el-row class="padding-lr" style="margin-top: 6px;">
                      <span>{{ $t('chart.result_filter') }}</span>
                      <draggable
                        v-model="view.customFilter"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="theme-item-class"
                        style="padding:2px 0 0 0;width:100%;min-height: 32px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;background-color: white;"
                        @add="addCustomFilter"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <filter-item
                            v-for="(item,index) in view.customFilter"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onFilterItemRemove="filterItemRemove"
                            @editItemFilter="showEditFilter"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.customFilter || view.customFilter.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                    <el-row
                      v-if="view.type && !(view.type.includes('table') && view.render === 'echarts') && !view.type.includes('text') && !view.type.includes('gauge') && view.type !== 'liquid' && view.type !== 'word-cloud' && view.type !== 'table-pivot' && view.type !=='label'&&view.type !=='richTextView'"
                      class="padding-lr"
                      style="margin-top: 6px;"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.drill') }}</span>
                        /
                        <span>{{ $t('chart.dimension') }}</span>
                        <el-tooltip class="item" effect="dark" placement="bottom">
                          <div slot="content">
                            钻取字段仅支持数据集中的字段
                          </div>
                          <i class="el-icon-info" style="cursor: pointer;color: #606266;" />
                        </el-tooltip>
                      </span>
                      <draggable
                        v-model="view.drillFields"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        @add="addDrill"
                        @update="calcData(true)"
                      >
                        <transition-group class="draggable-group">
                          <drill-item
                            v-for="(item,index) in view.drillFields"
                            :key="item.id"
                            :param="param"
                            :index="index"
                            :item="item"
                            :dimension-data="dimension"
                            :quota-data="quota"
                            @onDimensionItemChange="drillItemChange"
                            @onDimensionItemRemove="drillItemRemove"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.drillFields || view.drillFields.length === 0" class="drag-placeholder-style">
                        <span class="drag-placeholder-style-span">{{ $t('chart.placeholder_field') }}</span>
                      </div>
                    </el-row>
                  </div>
                </el-row>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane name="style" :label="$t('chart.chart_style')" class="padding-tab" style="width: 350px">
          <chart-style
            v-if="chartProperties || view.isPlugin"
            :param="param"
            :view="view"
            :chart="chart"
            :properties="chartProperties"
            :property-inner-all="chartPropertyInnerAll"
            :dimension-data="dimensionData"
            :quota-data="quotaData"
            @calcStyle="calcStyle"
            @onColorChange="onColorChange"
            @onSizeChange="onSizeChange"
            @onLabelChange="onLabelChange"
            @onTooltipChange="onTooltipChange"
            @onTotalCfgChange="onTotalCfgChange"
            @onChangeXAxisForm="onChangeXAxisForm"
            @onChangeYAxisForm="onChangeYAxisForm"
            @onChangeYAxisExtForm="onChangeYAxisExtForm"
            @onChangeSplitForm="onChangeSplitForm"
            @onTextChange="onTextChange"
            @onLegendChange="onLegendChange"
            @onMarginChange="onMarginChange"
            @onChangeBackgroundForm="onChangeBackgroundForm"
          />
        </el-tab-pane>
        <el-tab-pane name="senior" :label="$t('chart.senior')" class="padding-tab" style="width: 350px;">
          <el-row class="view-panel">
            <div
              v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('area') || view.type.includes('mix') || view.type.includes('gauge') || view.type === 'text' || view.type.includes('table') || view.type === 'map' || view.type === 'buddle-map')"
              style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;"
              class="attr-style theme-border-class"
            >
              <el-row
                v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('area') || view.type.includes('mix') || view.type === 'table-normal' || view.type === 'table-info')"
              >
                <span class="padding-lr">{{ $t('chart.senior_cfg') }}</span>
                <el-collapse v-model="attrActiveNames" class="style-collapse">
                  <el-collapse-item v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('area') || view.type.includes('mix'))" name="function" :title="$t('chart.function_cfg')">
                    <function-cfg
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onFunctionCfgChange="onFunctionCfgChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item v-if="view.type && (view.type === 'table-normal' || view.type === 'table-info')" name="scroll" :title="$t('chart.scroll_cfg')">
                    <scroll-cfg
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onScrollCfgChange="onScrollChange"
                    />
                  </el-collapse-item>
                </el-collapse>
              </el-row>
              <el-row
                v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('area') || view.type.includes('mix') || view.type.includes('gauge') || view.type === 'text' || (view.render === 'antv' && view.type.includes('table')))"
              >
                <span class="padding-lr">{{ $t('chart.analyse_cfg') }}</span>
                <el-collapse v-model="styleActiveNames" class="style-collapse">
                  <el-collapse-item
                    v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('area') || view.type.includes('mix'))"
                    name="analyse"
                    :title="$t('chart.assist_line')"
                  >
                    <assist-line
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      :quota-data="view.yaxis"
                      @onAssistLineChange="onAssistLineChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-if="view.type && (view.type.includes('gauge') || view.type === 'text' || (view.render === 'antv' && view.type.includes('table')))"
                    name="threshold"
                    :title="$t('chart.threshold')"
                  >
                    <threshold
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onThresholdChange="onThresholdChange"
                    />
                  </el-collapse-item>
                </el-collapse>
              </el-row>

              <el-row v-if="view.type && (view.type === 'map' || view.type === 'buddle-map')">

                <span v-if="false" class="padding-lr">{{ $t('chart.senior_cfg') }}</span>
                <el-collapse v-model="mapActiveNames" class="style-collapse">

                  <el-collapse-item title="地名映射" name="map-mapping">
                    <map-mapping
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      :dynamic-area-code="currentAreaCode"
                      @onMapMappingChange="onMapMappingChange"
                    />

                  </el-collapse-item>

                </el-collapse>
              </el-row>

            </div>
            <div v-else class="no-senior">
              {{ $t('chart.chart_no_senior') }}
            </div>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <el-col v-if="editFrom==='view'" style="height: 100%;min-width: 500px;border-top: 1px solid #E6E6E6;">
        <el-row style="width: 100%;height: 100%;" class="padding-lr">
          <div ref="imageWrapper" style="height: 100%">
            <plugin-com
              v-if="httpRequest.status && chart.type && view.isPlugin"
              ref="dynamicChart"
              :component-name="chart.type + '-view'"
              :obj="{chart}"
              :chart-id="chart.id"
              :chart="chart"
              :theme-style="curComponent.commonBackground"
              class="chart-class"
              @onChartClick="chartClick"
            />
            <chart-component
              v-else-if="httpRequest.status && chart.type && !chart.type.includes('table') && !chart.type.includes('text') && chart.type !== 'label' && renderComponent() === 'echarts'"
              ref="dynamicChart"
              :chart-id="chart.id"
              :chart="chart"
              class="chart-class"
              :theme-style="curComponent.commonBackground"
              @onChartClick="chartClick"
            />
            <chart-component-g2
              v-else-if="httpRequest.status && chart.type && !chart.type.includes('table') && !chart.type.includes('text') && chart.type !== 'label' && renderComponent() === 'antv'"
              ref="dynamicChart"
              :chart-id="chart.id"
              :chart="chart"
              class="chart-class"
              @onChartClick="chartClick"
            />
            <chart-component-s2
              v-else-if="httpRequest.status && chart.type && chart.type.includes('table') && !chart.type.includes('text') && chart.type !== 'label' && renderComponent() === 'antv'"
              ref="dynamicChart"
              :chart-id="chart.id"
              :chart="chart"
              class="chart-class"
              @onChartClick="chartClick"
            />
            <table-normal
              v-else-if="httpRequest.status && chart.type && chart.type.includes('table') && renderComponent() === 'echarts' && chart.type !== 'table-pivot'"
              :show-summary="chart.type === 'table-normal'"
              :chart="chart"
              class="table-class"
            />
            <label-normal
              v-else-if="httpRequest.status && chart.type && chart.type.includes('text')"
              :chart="chart"
              class="table-class"
            />
            <label-normal-text
              v-else-if="httpRequest.status && chart.type && chart.type === 'label'"
              :chart="chart"
              class="table-class"
            />
            <div v-if="!httpRequest.status" class="chart-error-class">
              <div
                style="font-size: 12px; color: #9ea6b2;height: 100%;display: flex;align-items: center;justify-content: center;"
              >
                {{ httpRequest.msg }},{{ $t('chart.chart_show_error') }}
                <br>
                {{ $t('chart.chart_error_tips') }}
              </div>
            </div>
          </div>
          <div style="position: absolute;left: 8px;bottom:8px;">
            <drill-path :drill-filters="drillFilters" @onDrillJump="drillJump" />
          </div>
        </el-row>
      </el-col>
    </el-row>

    <!--显示名修改-->
    <el-dialog v-dialogDrag :title="$t('chart.show_name_set')" :visible="renameItem" :show-close="false" width="30%">
      <el-form ref="itemForm" label-width="80px" :model="itemForm" :rules="itemFormRules">
        <el-form-item :label="$t('dataset.field_origin_name')" class="form-item">
          <span style="padding: 0 16px;">{{ itemForm.dsFieldName }}</span>
        </el-form-item>
        <el-form-item :label="$t('chart.show_name')" class="form-item">
          <el-input v-model="itemForm.name" style="width: 200px" size="mini" clearable />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeRename()">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveRename">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--指标过滤器-->
    <el-dialog
      v-if="quotaFilterEdit"
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
      v-if="dimensionFilterEdit"
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
      v-if="resultFilterEdit"
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
      v-if="selectTableFlag"
      v-dialogDrag
      :title="changeDsTitle"
      :visible="selectTableFlag"
      :show-close="false"
      width="70%"
      class="dialog-css"
    >
      <table-selector :checked-table="table" @getTable="getTable" />
      <p style="margin-top: 10px;color:#F56C6C;font-size: 12px;">{{ $t('chart.change_ds_tip') }}</p>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeChangeChart">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" :disabled="!changeTable || !changeTable.id" @click="changeChart">
          {{ $t('chart.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <!--编辑视图使用的数据集的字段-->
    <el-dialog
      v-if="editDsField"
      :visible="editDsField"
      :show-close="false"
      class="dialog-css"
      :fullscreen="true"
    >
      <field-edit :param="table" :table="table" />
      <div slot="title" class="dialog-footer title-text">
        <span style="font-size: 14px;">
          {{ $t('chart.ds_field_edit') }}
          <span v-if="table">[{{ table.name }}]</span>
        </span>
        <el-button size="mini" style="float: right;" @click="closeEditDsField">{{ $t('chart.close') }}</el-button>
      </div>
    </el-dialog>

    <!--编辑视图的字段-->
    <el-dialog
      v-if="showEditChartField"
      :visible="showEditChartField"
      :show-close="false"
      class="dialog-css"
      :fullscreen="true"
    >
      <chart-field-edit :param="chart" :table="table" :chart="chart" />
      <div slot="title" class="dialog-footer title-text">
        <span style="font-size: 14px;">
          {{ $t('chart.chart_field_edit') }}
          <span v-if="table">[{{ chart.title }}]</span>
        </span>
        <el-button size="mini" style="float: right;" @click="closeEditChartField">{{ $t('chart.close') }}</el-button>
      </div>
    </el-dialog>

    <!--同环比设置-->
    <el-dialog
      v-if="showEditQuotaCompare"
      v-dialogDrag
      :title="$t('chart.yoy_setting')"
      :visible="showEditQuotaCompare"
      :show-close="false"
      width="600px"
      class="dialog-css"
    >
      <compare-edit :compare-item="quotaItemCompare" :chart="chart" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeQuotaEditCompare">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveQuotaEditCompare">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--数值格式-->
    <el-dialog
      v-if="showValueFormatter"
      v-dialogDrag
      :title="$t('chart.value_formatter') + ' - ' + valueFormatterItem.name"
      :visible="showValueFormatter"
      :show-close="false"
      width="600px"
      class="dialog-css"
    >
      <value-formatter-edit :formatter-item="valueFormatterItem" :chart="chart" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeValueFormatter">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveValueFormatter">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--xAxis自定义排序-->
    <el-dialog
      v-if="showCustomSort"
      v-dialogDrag
      :title="$t('chart.custom_sort')"
      :visible="showCustomSort"
      :show-close="false"
      width="500px"
      class="dialog-css"
    >
      <custom-sort-edit :chart="chart" field-type="xAxis" :field="customSortField" @onSortChange="customSortChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeCustomSort">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveCustomSort">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--extStack自定义排序-->
    <el-dialog
      v-if="showStackCustomSort"
      v-dialogDrag
      :title="$t('chart.custom_sort')"
      :visible="showStackCustomSort"
      :show-close="false"
      width="500px"
      class="dialog-css"
    >
      <custom-sort-edit :chart="chart" field-type="extStack" :field="customSortField" @onSortChange="customSortChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeStackCustomSort">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveStackCustomSort">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--视图计算字段弹框-->
    <el-dialog
      v-if="editChartCalcField"
      v-dialogDrag
      :visible="editChartCalcField"
      :show-close="false"
      class="dialog-css"
      :title="$t('chart.copy_field')"
      append-to-body
    >
      <calc-chart-field-edit
        :param="chart"
        :field="currEditField"
        :mode="currEditField.extField === 1 ? 'copy' : 'normal'"
        @onEditClose="closeChartCalcField"
      />
    </el-dialog>
  </el-row>
</template>

<script>
import {
  ajaxGetDataOnly,
  post,
  getChartDetails,
  viewEditSave,
  resetViewCacheCallBack
} from '@/api/chart/chart'
import DimensionItem from '../components/drag-item/DimensionItem'
import QuotaItem from '../components/drag-item/QuotaItem'
import FilterItem from '../components/drag-item/FilterItem'
import ChartDragItem from '../components/drag-item/ChartDragItem'
import DrillItem from '../components/drag-item/DrillItem'
import ResultFilterEditor from '../components/filter/ResultFilterEditor'
import ChartComponent from '../components/ChartComponent'
import DrillPath from '@/views/chart/view/DrillPath'
import bus from '@/utils/bus'
import DatasetChartDetail from '../../dataset/common/DatasetChartDetail'
// shape attr,component style
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_FUNCTION_CFG,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_SIZE,
  DEFAULT_SPLIT,
  DEFAULT_THRESHOLD,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  DEFAULT_TOTAL,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '../chart/chart'
import QuotaFilterEditor from '../components/filter/QuotaFilterEditor'
import DimensionFilterEditor from '../components/filter/DimensionFilterEditor'
import TableNormal from '../components/table/TableNormal'
import LabelNormal from '../components/normal/LabelNormal'
// import html2canvas from 'html2canvasde'
import TableSelector from './TableSelector'
import FieldEdit from '../../dataset/data/FieldEdit'
import { areaMapping } from '@/api/map/map'
import QuotaExtItem from '@/views/chart/components/drag-item/QuotaExtItem'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import ChartType from '@/views/chart/view/ChartType'
import CompareEdit from '@/views/chart/components/compare/CompareEdit'
import { compareItem } from '@/views/chart/chart/compare'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import DimensionExtItem from '@/views/chart/components/drag-item/DimensionExtItem'
import PluginCom from '@/views/system/plugin/PluginCom'
import { mapState } from 'vuex'

import FunctionCfg from '@/views/chart/components/senior/FunctionCfg'
import MapMapping from '@/views/chart/components/senior/MapMapping'
import AssistLine from '@/views/chart/components/senior/AssistLine'
import Threshold from '@/views/chart/components/senior/Threshold'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import { pluginTypes } from '@/api/chart/chart'
import ValueFormatterEdit from '@/views/chart/components/value-formatter/ValueFormatterEdit'
import ChartStyle from '@/views/chart/view/ChartStyle'
import CustomSortEdit from '@/views/chart/components/compare/CustomSortEdit'
import ScrollCfg from '@/views/chart/components/senior/ScrollCfg'
import ChartFieldEdit from '@/views/chart/view/ChartFieldEdit'
import CalcChartFieldEdit from '@/views/chart/view/CalcChartFieldEdit'

export default {
  name: 'ChartEdit',
  components: {
    ScrollCfg,
    CalcChartFieldEdit,
    ChartFieldEdit,
    CustomSortEdit,
    ChartStyle,
    ValueFormatterEdit,
    LabelNormalText,
    Threshold,
    AssistLine,
    FunctionCfg,
    DimensionExtItem,
    ChartComponentS2,
    CompareEdit,
    ChartType,
    ChartComponentG2,
    QuotaExtItem,
    FilterItem,
    FieldEdit,
    TableSelector,
    ResultFilterEditor,
    LabelNormal,
    DimensionFilterEditor,
    TableNormal,
    DatasetChartDetail,
    QuotaFilterEditor,
    ChartComponent,
    QuotaItem,
    DimensionItem,
    ChartDragItem,
    DrillItem,
    DrillPath,
    PluginCom,
    MapMapping
  },
  props: {
    param: {
      type: Object,
      required: true
    },
    editFrom: {
      type: String,
      required: false,
      default: 'view'
    },
    editStatue: {
      type: Boolean,
      required: false,
      default: false
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
        xaxisExt: [],
        yaxis: [],
        yaxisExt: [],
        extStack: [],
        drillFields: [],
        viewFields: [],
        extBubble: [],
        show: true,
        type: 'bar',
        title: '',
        customAttr: {
          color: DEFAULT_COLOR_CASE,
          size: DEFAULT_SIZE,
          label: DEFAULT_LABEL,
          tooltip: DEFAULT_TOOLTIP,
          totalCfg: DEFAULT_TOTAL
        },
        customStyle: {
          text: DEFAULT_TITLE_STYLE,
          legend: DEFAULT_LEGEND_STYLE,
          xAxis: DEFAULT_XAXIS_STYLE,
          yAxis: DEFAULT_YAXIS_STYLE,
          yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
          split: DEFAULT_SPLIT
        },
        senior: {
          functionCfg: DEFAULT_FUNCTION_CFG,
          assistLine: [],
          threshold: DEFAULT_THRESHOLD
        },
        customFilter: [],
        render: 'antv',
        isPlugin: false
      },
      moveId: -1,
      chart: {
        id: 'echart',
        type: null
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
      mapActiveNames: ['map-mapping'],
      styleActiveNames: [],
      drillClickDimensionList: [],
      drillFilters: [],
      renderOptions: [
        { name: 'AntV', value: 'antv' },
        { name: 'ECharts', value: 'echarts' }
      ],
      drill: false,
      hasEdit: false,
      quotaItemCompare: {},
      showEditQuotaCompare: false,
      preChartId: '',
      pluginRenderOptions: [],
      showValueFormatter: false,
      valueFormatterItem: {},
      showCustomSort: false,
      customSortList: [],
      customSortField: {},
      showEditChartField: false,
      currEditField: {},
      editChartCalcField: false,
      fieldShow: false,
      tabActive: 'data',
      currentAreaCode: '',
      showStackCustomSort: false

    }
  },
  computed: {
    chartConfig() {
      const _this = this
      if (_this.chart && _this.chart.render) {
        const viewConfig = this.allViewRender.filter(item => item.render === _this.chart.render && item.value === _this.chart.type)
        if (viewConfig && viewConfig.length) {
          return viewConfig[0]
        } else {
          return null
        }
      } else {
        return null
      }
    },
    chartProperties() {
      return this.chartConfig ? this.chartConfig.properties : null
    },
    chartPropertyInnerAll() {
      return this.chartConfig ? this.chartConfig.propertyInner : null
    },
    chartType() {
      return this.chart ? this.chart.type : null
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'curComponent',
      'panelViewEditInfo',
      'allViewRender'

    ])
    /* pluginRenderOptions() {
      const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views')) || []
      const pluginOptions = plugins.filter(plugin => !this.renderOptions.some(option => option.value === plugin.render)).map(plugin => {
        return { name: plugin.render, value: plugin.render }
      })
      return [...this.renderOptions, ...pluginOptions]
    } */
  },
  watch: {
    'editStatue': function(val) {
      if (val && this.param.id !== this.preChartId) {
        this.preChartId = this.param.id
        this.chartInit()
      }
    },
    'param': function(val) {
      if (this.param.optType === 'new') {
        //
      } else if (this.param.id !== this.preChartId && this.editStatue) {
        this.preChartId = this.param.id
        this.chartInit()
      }
    },
    searchField(val) {
      this.fieldFilter(val)
    },
    'chartType': function(newVal, oldVal) {
      if ((newVal === 'map' || newVal === 'buddle-map') && newVal !== oldVal) {
        this.initAreas()
      }
      this.$emit('typeChange', newVal)
    },
    'view.type': function(newVal, oldVal) {
      this.view.isPlugin = this.$refs['cu-chart-type'] && this.$refs['cu-chart-type'].currentIsPlugin(newVal)
    }
  },
  created() {
    this.bindPluginEvent()
    this.initFromPanel()
    this.chartInit()
    const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views'))
    if (plugins) {
      this.loadPluginType()
    } else {
      pluginTypes().then(res => {
        const plugins = res.data
        localStorage.setItem('plugin-views', JSON.stringify(plugins))
        this.loadPluginType()
      }).catch(e => {
        localStorage.setItem('plugin-views', null)
        this.loadPluginType()
      })
    }
  },
  mounted() {
  },
  beforeDestroy() {
    bus.$off('show-dimension-edit-filter', this.showDimensionEditFilter)
    bus.$off('show-rename', this.showRename)
    bus.$off('show-quota-edit-filter', this.showQuotaEditFilter)
    bus.$off('show-quota-edit-compare', this.showQuotaEditCompare)
    bus.$off('show-edit-filter', this.showEditFilter)
    bus.$off('calc-data', this.calcData)
    bus.$off('plugins-calc-style', this.calcStyle)
    bus.$off('plugin-chart-click', this.chartClick)
    bus.$off('set-dynamic-area-code', this.setDynamicAreaCode)
  },
  activated() {
  },

  methods: {
    resetChartData(){
      this.getChart(this.param.id)
    },
    setDynamicAreaCode(code) {
      this.currentAreaCode = code
    },
    loadPluginType() {
      const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views')) || []
      const pluginOptions = plugins.filter(plugin => !this.renderOptions.some(option => option.value === plugin.render)).map(plugin => {
        return { name: plugin.render, value: plugin.render }
      })
      this.pluginRenderOptions = [...this.renderOptions, ...pluginOptions]
    },
    emptyTableData(id) {
      this.table = {}
      this.dimension = []
      this.quota = []
      this.dimensionData = []
      this.quotaData = []
    },
    initFromPanel() {
      this.hasEdit = (this.panelViewEditInfo[this.param.id] || false)
    },
    chartInit() {
      this.fieldShow = false
      this.resetDrill()
      this.initFromPanel()
      this.getChart(this.param.id)
      // if (this.componentViewsData[this.param.id]) {
      //   this.chart = this.componentViewsData[this.param.id]
      // }
    },
    bindPluginEvent() {
      bus.$on('show-dimension-edit-filter', this.showDimensionEditFilter)
      bus.$on('show-rename', this.showRename)
      bus.$on('show-quota-edit-filter', this.showQuotaEditFilter)
      bus.$on('show-quota-edit-compare', this.showQuotaEditCompare)
      bus.$on('show-edit-filter', this.showEditFilter)
      bus.$on('calc-data', this.calcData)
      bus.$on('plugins-calc-style', this.calcStyle)
      bus.$on('plugin-chart-click', this.chartClick)
      bus.$on('set-dynamic-area-code', this.setDynamicAreaCode)
    },
    initTableData(id, optType) {
      if (id != null) {
        post('/dataset/table/getWithPermission/' + id, null).then(response => {
          // If click too fast on the panel, the data here may be inconsistent, so make a verification
          if (this.view.tableId === id) {
            this.table = response.data
            this.initTableField(id, optType)
          }
        }).catch(err => {
          this.table = null
          this.resetDatasetField()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      }
    },
    initTableField(id, optType) {
      if (this.table) {
        post('/dataset/table/getFieldsFromDE', this.table).then(response => {
          // If click too fast on the panel, the data here may be inconsistent, so make a verification
          post('/chart/field/listByDQ/' + this.param.id + '/' + this.panelInfo.id, null).then(res => {
            if (this.view.tableId === id) {
              this.dimension = response.data.dimension.concat(res.data.dimensionList)
              this.quota = response.data.quota.concat(res.data.quotaList)
              this.dimensionData = JSON.parse(JSON.stringify(this.dimension))
              this.quotaData = JSON.parse(JSON.stringify(this.quota))
              this.fieldFilter(this.searchField)
              if (optType === 'change') {
                this.resetChangeTable()
                this.$nextTick(() => {
                  bus.$emit('reset-change-table', 'change')
                  this.calcData()
                })
              }
            }
            this.fieldShow = true
          }).catch(err => {
            console.error(err)
            this.resetView()
            this.httpRequest.status = err.response.data.success
            this.httpRequest.msg = err.response.data.message
            return true
          })
        }).catch(err => {
          console.error(err)

          this.resetView()
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      } else {
        this.resetDatasetField()
      }
    },
    resetChangeTable() {
      const compareData = {}
      this.dimensionData.forEach(deimension => {
        compareData[deimension.originName] = deimension
      })
      this.quotaData.forEach(quota => {
        compareData[quota.originName] = quota
      })
      const compareCols = ['xaxis', 'xaxisExt', 'yaxis', 'yaxisExt', 'customFilter', 'extStack', 'extBubble', 'drillFields']
      this.viewFieldChange(compareData, compareCols)
    },
    viewFieldChange(compareData, compareCols) {
      const _this = this
      compareCols.forEach(compareCol => {
        _this.view[compareCol].forEach(function(item, index) {
          if (compareData[item.originName]) {
            const itemTemp = {
              ...compareData[item.originName],
              name: item.name,
              deType: item.deType,
              type: item.type,
              groupType: item.groupType,
              sort: item.sort
            }
            _this.view[compareCol][index] = itemTemp
          }
        })
      })
    },
    buildParam(getData, trigger, needRefreshGroup = false, switchType = false, switchRender = false) {
      if (!this.view.resultCount ||
        this.view.resultCount === '' ||
        isNaN(Number(this.view.resultCount)) ||
        String(this.view.resultCount).includes('.') ||
        parseInt(this.view.resultCount) < 1) {
        this.view.resultCount = '1000'
      }
      if (switchType) {
        this.view.senior.threshold = {}
      }
      if (switchType && (this.view.type === 'table-info' || this.chart.type === 'table-info') && this.view.xaxis.length > 0) {
        this.$message({
          showClose: true,
          message: this.$t('chart.table_info_switch'),
          type: 'warning'
        })
        this.view.xaxis = []
      }
      const view = JSON.parse(JSON.stringify(this.view))
      view.id = this.view.id
      view.sceneId = this.view.sceneId
      view.name = this.view.title ? this.view.title : this.table.name
      if (view.title.length > 50) {
        this.$error(this.$t('chart.title_limit'))
        return
      }
      view.tableId = this.view.tableId
      if (view.type === 'map' && view.xaxis.length > 1) {
        view.xaxis = [view.xaxis[0]]
      }
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
      if (view.type === 'table-pivot' || view.type === 'bar-group') {
        view.xaxisExt.forEach(function(ele) {
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
      }
      if (view.type === 'map' && view.yaxis.length > 1) {
        view.yaxis = [view.yaxis[0]]
      }
      view.yaxis.forEach(function(ele) {
        if (!ele.chartType) {
          ele.chartType = 'bar'
        }
        if (ele.chartId) {
          ele.summary = ''
        } else {
          if (!ele.summary || ele.summary === '') {
            if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
              ele.summary = 'count'
            } else {
              ele.summary = 'sum'
            }
          }
        }

        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
        if (!ele.compareCalc) {
          ele.compareCalc = compareItem
        }
      })
      if (view.type === 'chart-mix') {
        view.yaxisExt.forEach(function(ele) {
          if (!ele.chartType) {
            ele.chartType = 'bar'
          }
          if (ele.chartId) {
            ele.summary = ''
          } else {
            if (!ele.summary || ele.summary === '') {
              if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
                ele.summary = 'count'
              } else {
                ele.summary = 'sum'
              }
            }
          }

          if (!ele.sort || ele.sort === '') {
            ele.sort = 'none'
          }
          if (!ele.filter) {
            ele.filter = []
          }
          if (!ele.compareCalc) {
            ele.compareCalc = compareItem
          }
        })
      }
      view.extStack.forEach(function(ele) {
        if (!ele.dateStyle || ele.dateStyle === '') {
          ele.dateStyle = 'y_M_d'
        }
        if (!ele.datePattern || ele.datePattern === '') {
          ele.datePattern = 'date_sub'
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
      })
      view.extBubble.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
            ele.summary = 'count'
          } else {
            ele.summary = 'sum'
          }
        }
      })
      if (view.type === 'label') {
        if (view.xaxis.length > 1) {
          view.xaxis.splice(1, view.xaxis.length)
        }
      }
      if (view.type.startsWith('pie') ||
        view.type.startsWith('funnel') ||
        view.type.startsWith('text') ||
        view.type.startsWith('gauge') ||
        view.type === 'treemap' ||
        view.type === 'liquid' ||
        view.type === 'word-cloud' ||
        view.type === 'waterfall' ||
        view.type.includes('group')) {
        if (view.yaxis.length > 1) {
          view.yaxis.splice(1, view.yaxis.length)
        }
      }
      if (view.type === 'line-stack' && trigger === 'chart') {
        view.customAttr.size.lineArea = true
      }
      if (view.type === 'line' && trigger === 'chart') {
        view.customAttr.size.lineArea = false
      }
      if (view.type === 'treemap' && trigger === 'chart') {
        view.customAttr.label.show = true
      }
      if (view.type === 'liquid' ||
        (view.type.includes('table') && view.render === 'echarts') ||
        view.type.includes('text') ||
        view.type.includes('gauge') ||
        view.type === 'table-pivot') {
        view.drillFields = []
      }
      view.customFilter.forEach(function(ele) {
        if (ele && !ele.filter) {
          ele.filter = []
        }
      })
      this.chart = JSON.parse(JSON.stringify(view))
      this.view = JSON.parse(JSON.stringify(view))
      // stringify json param
      view.xaxis = JSON.stringify(view.xaxis)
      view.viewFields = JSON.stringify(view.viewFields)
      view.xaxisExt = JSON.stringify(view.xaxisExt)
      view.yaxis = JSON.stringify(view.yaxis)
      view.yaxisExt = JSON.stringify(view.yaxisExt)
      view.customAttr = JSON.stringify(view.customAttr)
      view.customStyle = JSON.stringify(view.customStyle)
      view.customFilter = JSON.stringify(view.customFilter)
      view.extStack = JSON.stringify(view.extStack)
      view.drillFields = JSON.stringify(view.drillFields)
      view.extBubble = JSON.stringify(view.extBubble)
      view.senior = JSON.stringify(view.senior)
      delete view.data
      return view
    },
    calcData(getData, trigger, needRefreshGroup = false, switchType = false, switchRender = false) {
      this.changeEditStatus(true)
      const view = this.buildParam(true, 'chart', false, switchType, switchRender)
      if (!view) return
      viewEditSave(this.panelInfo.id, view).then(() => {
        // this.getData(this.param.id)
        bus.$emit('view-in-cache', { type: 'propChange', viewId: this.param.id })
      })
    },
    calcStyle(modifyName) {
      this.changeEditStatus(true)
      // 将视图传入echart...组件
      const view = JSON.parse(JSON.stringify(this.view))
      view.xaxis = JSON.stringify(this.view.xaxis)
      view.viewFields = JSON.stringify(this.view.viewFields)
      view.xaxisExt = JSON.stringify(this.view.xaxisExt)
      view.yaxis = JSON.stringify(this.view.yaxis)
      view.yaxisExt = JSON.stringify(this.view.yaxisExt)
      view.extStack = JSON.stringify(this.view.extStack)
      view.drillFields = JSON.stringify(this.view.drillFields)
      view.extBubble = JSON.stringify(this.view.extBubble)
      view.customAttr = JSON.stringify(this.view.customAttr)
      view.customStyle = JSON.stringify(this.view.customStyle)
      view.customFilter = JSON.stringify(this.view.customFilter)
      view.senior = JSON.stringify(this.view.senior)
      view.title = this.view.title
      view.stylePriority = this.view.stylePriority
      // view.data = this.data
      this.chart = view

      // 保存到缓存表
      const viewSave = this.buildParam(true, 'chart', false, false)
      if (!viewSave) return
      viewEditSave(this.panelInfo.id, viewSave)

      if (modifyName === 'color') {
        bus.$emit('view-in-cache', { type: 'styleChange', viewId: this.view.id, viewInfo: view, refreshProp: 'customAttr' })
      } else {
        bus.$emit('view-in-cache', { type: 'styleChange', viewId: this.view.id, viewInfo: view })
      }
    },

    closeEdit() {
      if (this.view.title && this.view.title.length > 50) {
        this.$warning(this.$t('chart.title_limit'))
        return
      }
      const view = this.buildParam(true, 'chart', false, false)
      if (!view) return
      post('/chart/view/save/' + this.panelInfo.id, view).then(response => {
        this.getChart(response.data.id)
        this.hasEdit = false
        this.refreshGroup(view)
        this.closeChangeChart()
        // 从仪表板入口关闭
        if (this.$route.path.indexOf('panel') > -1) {
          this.$store.commit('recordSnapshot')
          bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
        }
        this.$success(this.$t('commons.save_success'))
      })
    },
    closePanelEdit() {
      bus.$emit('change_panel_right_draw', false)
    },
    close() {
      this.closeChangeChart()
      // 从仪表板入口关闭
      if (this.$route.path.indexOf('panel') > -1) {
        this.$store.commit('recordSnapshot')
        bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
      }
    },
    getData(id) {
      // this.hasEdit = true
      if (id) {
        ajaxGetDataOnly(id, this.panelInfo.id, {
          filter: [],
          drill: this.drillClickDimensionList,
          queryFrom: 'panel'
        }).then(response => {
          this.initTableData(response.data.tableId)
          this.view = JSON.parse(JSON.stringify(response.data))
          this.view.viewFields = this.view.viewFields ? JSON.parse(this.view.viewFields) : []
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.xaxisExt = this.view.xaxisExt ? JSON.parse(this.view.xaxisExt) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
          this.view.yaxisExt = this.view.yaxisExt ? JSON.parse(this.view.yaxisExt) : []
          this.view.extStack = this.view.extStack ? JSON.parse(this.view.extStack) : []
          this.view.drillFields = this.view.drillFields ? JSON.parse(this.view.drillFields) : []
          this.view.extBubble = this.view.extBubble ? JSON.parse(this.view.extBubble) : []
          this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
          this.view.customStyle = this.view.customStyle ? JSON.parse(this.view.customStyle) : {}
          this.view.customFilter = this.view.customFilter ? JSON.parse(this.view.customFilter) : {}
          this.view.senior = this.view.senior ? JSON.parse(this.view.senior) : {}
          // 将视图传入echart组件
          this.chart = response.data
          this.data = response.data.data
          this.httpRequest.status = true
          if (this.chart.privileges) {
            this.param.privileges = this.chart.privileges
          }
          if (!response.data.drill) {
            this.drillClickDimensionList.splice(this.drillClickDimensionList.length - 1, 1)

            this.resetDrill()
          }
          this.drill = response.data.drill
          this.drillFilters = JSON.parse(JSON.stringify(response.data.drillFilters ? response.data.drillFilters : []))
        }).catch(err => {
          this.resetView()
          this.resetDrill()
          this.$nextTick(() => {
            this.getChart(id)
          })
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      } else {
        this.view = {}
      }
    },
    getChart(id, queryFrom = 'panel_edit') {
      if (id) {
        getChartDetails(id, this.panelInfo.id, { queryFrom: queryFrom }).then(response => {
          // If click too fast on the panel, the data here may be inconsistent, so make a verification
          if (response.data.id === this.param.id) {
            if (response.data.dataFrom === 'template') {
              this.emptyTableData(response.data.id)
            } else {
              this.initTableData(response.data.tableId)
            }
            this.view = JSON.parse(JSON.stringify(response.data))
            this.view.viewFields = this.view.viewFields ? JSON.parse(this.view.viewFields) : []
            this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
            this.view.xaxisExt = this.view.xaxisExt ? JSON.parse(this.view.xaxisExt) : []
            this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
            this.view.yaxisExt = this.view.yaxisExt ? JSON.parse(this.view.yaxisExt) : []
            this.view.extStack = this.view.extStack ? JSON.parse(this.view.extStack) : []
            this.view.drillFields = this.view.drillFields ? JSON.parse(this.view.drillFields) : []
            this.view.extBubble = this.view.extBubble ? JSON.parse(this.view.extBubble) : []
            this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
            this.view.customStyle = this.view.customStyle ? JSON.parse(this.view.customStyle) : {}
            this.view.customFilter = this.view.customFilter ? JSON.parse(this.view.customFilter) : {}
            this.view.senior = this.view.senior ? JSON.parse(this.view.senior) : {}

            // 将视图传入echart组件
            this.chart = response.data
            this.data = response.data.data
          }
        }).catch(err => {
          this.httpRequest.status = err.response.data.success
          this.httpRequest.msg = err.response.data.message
          return true
        })
      } else {
        this.view = {}
      }
    },

    // move回调方法
    onMove(e, originalEvent) {
      this.moveId = e.draggedContext.element.id
      return true
    },

    onCustomSort(item) {
      this.customSortField = this.view.xaxis[item.index]
      this.customSort()
    },

    dimensionItemChange(item) {
      this.calcData(true)
    },

    dimensionItemRemove(item) {
      if (item.removeType === 'dimension') {
        this.view.xaxis.splice(item.index, 1)
      } else if (item.removeType === 'dimensionExt') {
        this.view.xaxisExt.splice(item.index, 1)
      }
      this.calcData(true)
    },

    quotaItemChange(item) {
      this.calcData(true)
    },

    quotaItemRemove(item) {
      if (item.removeType === 'quota') {
        this.view.yaxis.splice(item.index, 1)
      } else if (item.removeType === 'quotaExt') {
        this.view.yaxisExt.splice(item.index, 1)
      }
      this.calcData(true)
    },

    onColorChange(val) {
      this.view.customAttr.color = val
      this.calcStyle('color')
    },

    onSizeChange(val) {
      this.view.customAttr.size = val
      this.calcData()
    },

    onTextChange(val) {
      this.view.customStyle.text = val
      this.view.title = val.title
      this.calcStyle()
    },

    onLegendChange(val) {
      this.view.customStyle.legend = val
      this.calcStyle()
    },
    onMarginChange(val) {
      this.view.customStyle.margin = val
      this.calcStyle()
    },

    onLabelChange(val) {
      this.view.customAttr.label = val
      this.calcStyle()
    },

    onTooltipChange(val) {
      this.view.customAttr.tooltip = val
      this.calcStyle()
    },

    onTotalCfgChange(val) {
      this.view.customAttr.totalCfg = val
      this.calcStyle()
    },

    onChangeXAxisForm(val) {
      this.view.customStyle.xAxis = val
      this.calcStyle()
    },

    onChangeYAxisForm(val) {
      this.view.customStyle.yAxis = val
      this.calcStyle()
    },

    onChangeYAxisExtForm(val) {
      this.view.customStyle.yAxisExt = val
      this.calcStyle()
    },

    onChangeBackgroundForm(val) {
      this.view.customStyle.background = val
      this.calcStyle()
    },

    onChangeSplitForm(val) {
      this.view.customStyle.split = val
      this.calcStyle()
    },

    onFunctionCfgChange(val) {
      this.view.senior.functionCfg = val
      this.calcStyle()
    },

    onAssistLineChange(val) {
      this.view.senior.assistLine = val
      this.calcData()
    },

    onThresholdChange(val) {
      this.view.senior.threshold = val
      this.calcStyle()
    },

    onScrollChange(val) {
      this.view.senior.scrollCfg = val
      this.calcStyle()
    },
    onMapMappingChange(val) {
      this.view.senior.mapMapping = val
      this.calcStyle()
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
        if (!f.term.includes('null') && !f.term.includes('empty') && (!f.value || f.value === '')) {
          this.$message({
            message: this.$t('chart.filter_value_can_null'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.view.xaxis[this.dimensionItem.index].filter = this.dimensionItem.filter
      this.calcData(true)
      this.closeDimensionFilter()
    },

    showQuotaEditFilter(item) {
      this.quotaItem = JSON.parse(JSON.stringify(item))
      if (!this.quotaItem.logic) {
        this.quotaItem.logic = 'and'
      }
      this.quotaFilterEdit = true
    },
    closeQuotaFilter() {
      this.quotaFilterEdit = false
    },
    saveQuotaFilter() {
      for (let i = 0; i < this.quotaItem.filter.length; i++) {
        const f = this.quotaItem.filter[i]
        if (!f.term.includes('null') && !f.term.includes('empty') && (!f.value || f.value === '')) {
          this.$message({
            message: this.$t('chart.filter_value_can_null'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (parseFloat(f.value).toString() === 'NaN') {
          this.$message({
            message: this.$t('chart.filter_value_can_not_str'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      if (this.quotaItem.filterType === 'quota') {
        this.view.yaxis[this.quotaItem.index].filter = this.quotaItem.filter
        this.view.yaxis[this.quotaItem.index].logic = this.quotaItem.logic
      } else if (this.quotaItem.filterType === 'quotaExt') {
        this.view.yaxisExt[this.quotaItem.index].filter = this.quotaItem.filter
        this.view.yaxisExt[this.quotaItem.index].logic = this.quotaItem.logic
      }
      this.calcData(true)
      this.closeQuotaFilter()
    },

    filterItemRemove(item) {
      this.view.customFilter.splice(item.index, 1)
      this.calcData(true)
    },
    showEditFilter(item) {
      this.filterItem = JSON.parse(JSON.stringify(item))
      this.chartForFilter = JSON.parse(JSON.stringify(this.view))
      if (!this.filterItem.logic) {
        this.filterItem.logic = 'and'
      }
      if (!this.filterItem.filterType) {
        this.filterItem.filterType = 'logic'
      }
      if (!this.filterItem.enumCheckField) {
        this.filterItem.enumCheckField = []
      }
      this.resultFilterEdit = true
    },
    closeResultFilter() {
      this.resultFilterEdit = false
    },
    saveResultFilter() {
      if (((this.filterItem.deType === 0 || this.filterItem.deType === 5) && this.filterItem.filterType !== 'enum') ||
        this.filterItem.deType === 1 ||
        this.filterItem.deType === 2 ||
        this.filterItem.deType === 3) {
        for (let i = 0; i < this.filterItem.filter.length; i++) {
          const f = this.filterItem.filter[i]
          if (!f.term.includes('null') && !f.term.includes('empty') && (!f.value || f.value === '')) {
            this.$message({
              message: this.$t('chart.filter_value_can_null'),
              type: 'error',
              showClose: true
            })
            return
          }
          if (this.filterItem.deType === 2 || this.filterItem.deType === 3) {
            if (parseFloat(f.value).toString() === 'NaN') {
              this.$message({
                message: this.$t('chart.filter_value_can_not_str'),
                type: 'error',
                showClose: true
              })
              return
            }
          }
        }
      }

      this.view.customFilter[this.filterItem.index].filter = this.filterItem.filter
      this.view.customFilter[this.filterItem.index].logic = this.filterItem.logic
      this.view.customFilter[this.filterItem.index].filterType = this.filterItem.filterType
      this.view.customFilter[this.filterItem.index].enumCheckField = this.filterItem.enumCheckField
      this.calcData(true)
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
          } else if (this.itemForm.renameType === 'quotaExt') {
            this.view.yaxisExt[this.itemForm.index].name = this.itemForm.name
          } else if (this.itemForm.renameType === 'dimensionExt') {
            this.view.xaxisExt[this.itemForm.index].name = this.itemForm.name
          }
          this.calcData(true)
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

    showQuotaEditCompare(item) {
      this.quotaItemCompare = JSON.parse(JSON.stringify(item))
      this.showEditQuotaCompare = true
    },
    closeQuotaEditCompare() {
      this.showEditQuotaCompare = false
    },
    saveQuotaEditCompare() {
      // 更新指标
      if (this.quotaItemCompare.calcType === 'quota') {
        this.view.yaxis[this.quotaItemCompare.index].compareCalc = this.quotaItemCompare.compareCalc
      } else if (this.quotaItemCompare.calcType === 'quotaExt') {
        this.view.yaxisExt[this.quotaItemCompare.index].compareCalc = this.quotaItemCompare.compareCalc
      }
      this.calcData(true)
      this.closeQuotaEditCompare()
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

    // 更换数据集
    changeChart() {
      this.view.dataFrom = 'dataset'
      const optType = this.view.tableId === this.changeTable.id ? 'same' : 'change'
      // this.save(true, 'chart', false)
      this.view.tableId = this.changeTable.id
      // 更换数据集后清空视图字段
      post('/chart/field/deleteByChartId/' + this.param.id + '/' + this.panelInfo.id, null).then(response => {
        // reset gauge
        this.view.customAttr.size.gaugeMinType = 'fix'
        this.view.customAttr.size.gaugeMaxType = 'fix'
        this.calcData(true, 'chart', false)
        this.initTableData(this.view.tableId, optType)
        this.closeChangeChart()
      })
    },

    fieldFilter(val) {
      if (val && val !== '') {
        this.dimensionData = JSON.parse(JSON.stringify(this.dimension.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
        this.quotaData = JSON.parse(JSON.stringify(this.quota.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
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
      this.initTableField(this.table.id)
      // 因动态计算较多，更新字段后重新计算视图数据
      this.calcData()
    },

    editChartField() {
      this.showEditChartField = true
    },

    closeEditChartField() {
      this.showEditChartField = false
      this.initTableField(this.table.id)
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
    dragMoveDuplicate(list, e, mode) {
      if (mode === 'ds') {
        list.splice(e.newDraggableIndex, 1)
      } else {
        const that = this
        const dup = list.filter(function(m) {
          return m.id === that.moveId
        })
        if (dup && dup.length > 1) {
          list.splice(e.newDraggableIndex, 1)
        }
      }
    },
    dragRemoveChartField(list, e) {
      const that = this
      const dup = list.filter(function(m) {
        return m.id === that.moveId
      })
      if (dup && dup.length > 0) {
        if (dup[0].chartId) {
          list.splice(e.newDraggableIndex, 1)
        }
      }
    },
    addXaxis(e) {
      if (this.view.type !== 'table-info') {
        this.dragCheckType(this.view.xaxis, 'd')
      }
      this.dragMoveDuplicate(this.view.xaxis, e)
      if ((this.view.type === 'map' || this.view.type === 'word-cloud' || this.view.type === 'label') && this.view.xaxis.length > 1) {
        this.view.xaxis = [this.view.xaxis[0]]
      }
      this.calcData(true)
    },
    addXaxisExt(e) {
      if (this.view.type !== 'table-info') {
        this.dragCheckType(this.view.xaxisExt, 'd')
      }
      this.dragMoveDuplicate(this.view.xaxisExt, e)
      if ((this.view.type === 'map' || this.view.type === 'word-cloud') && this.view.xaxisExt.length > 1) {
        this.view.xaxisExt = [this.view.xaxisExt[0]]
      }
      this.calcData(true)
    },
    addYaxis(e) {
      this.dragCheckType(this.view.yaxis, 'q')
      this.dragMoveDuplicate(this.view.yaxis, e)
      if ((this.view.type === 'map' || this.view.type === 'waterfall' || this.view.type === 'word-cloud' || this.view.type.includes('group')) && this.view.yaxis.length > 1) {
        this.view.yaxis = [this.view.yaxis[0]]
      }
      this.calcData(true)
    },
    addYaxisExt(e) {
      this.dragCheckType(this.view.yaxisExt, 'q')
      this.dragMoveDuplicate(this.view.yaxisExt, e)
      if (this.view.type === 'map' && this.view.yaxisExt.length > 1) {
        this.view.yaxisExt = [this.view.yaxisExt[0]]
      }
      this.calcData(true)
    },
    moveToDimension(e) {
      this.dragMoveDuplicate(this.dimensionData, e, 'ds')
      this.calcData(true)
    },
    moveToQuota(e) {
      this.dragMoveDuplicate(this.quotaData, e, 'ds')
      this.calcData(true)
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
      this.dragRemoveChartField(this.view.customFilter, e)
      this.calcData(true)
    },

    initAreas() {
      //   let mapping
      //   if ((mapping = localStorage.getItem('areaMapping')) !== null) {
      //     this.places = JSON.parse(mapping)
      //     return
      //   }
      Object.keys(this.places).length === 0 && areaMapping().then(res => {
        this.places = res.data
        // localStorage.setItem('areaMapping', JSON.stringify(res.data))
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
      this.dragCheckType(this.view.extStack, 'd')
      if (this.view.extStack && this.view.extStack.length > 1) {
        this.view.extStack = [this.view.extStack[0]]
      }
      this.calcData(true)
    },
    stackItemChange(item) {
      this.calcData(true)
    },
    stackItemRemove(item) {
      this.view.extStack.splice(item.index, 1)
      this.calcData(true)
    },
    stackItemCustomSort(item) {
      this.customSortField = this.view.extStack[item.index]
      this.stackCustomSort()
    },

    drillItemChange(item) {
      this.calcData(true)
    },
    drillItemRemove(item) {
      this.view.drillFields.splice(item.index, 1)
      this.calcData(true)
    },
    addDrill(e) {
      this.dragCheckType(this.view.drillFields, 'd')
      this.dragMoveDuplicate(this.view.drillFields, e)
      this.dragRemoveChartField(this.view.drillFields, e)
      this.calcData(true)
    },

    addBubble(e) {
      this.dragCheckType(this.view.extBubble, 'q')
      if (this.view.extBubble && this.view.extBubble.length > 1) {
        this.view.extBubble = [this.view.extBubble[0]]
      }
      this.calcData(true)
    },
    bubbleItemChange(item) {
      this.calcData(true)
    },
    bubbleItemRemove(item) {
      this.view.extBubble.splice(item.index, 1)
      this.calcData(true)
    },

    chartClick(param) {
      if (this.drillClickDimensionList.length < this.view.drillFields.length - 1) {
        // const isSwitch = (this.chart.type === 'map' && this.sendToChildren(param))
        if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
          if (this.sendToChildren(param)) {
            this.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
            // this.getData(this.param.id)
            this.calcData(true, 'chart', false, false)
          }
        } else {
          this.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
          // this.getData(this.param.id)
          this.calcData(true, 'chart', false, false)
        }
      } else if (this.view.drillFields.length > 0) {
        this.$message({
          type: 'error',
          message: this.$t('chart.last_layer'),
          showClose: true
        })
      }
    },

    resetDrill() {
      const length = this.drillClickDimensionList.length
      this.drillClickDimensionList = []
      if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
        this.backToParent(0, length)
        this.currentAcreaNode = null
        const current = this.$refs.dynamicChart
        this.setDetailMapCode(null)
        if (this.view.isPlugin) {
          current && current.callPluginInner && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: null })
        } else {
          current && current.registerDynamicMap && current.registerDynamicMap(null)
        }
        // this.$refs.dynamicChart && this.$refs.dynamicChart.registerDynamicMap && this.$refs.dynamicChart.registerDynamicMap(null)
      }
    },
    drillJump(index) {
      const length = this.drillClickDimensionList.length
      this.drillClickDimensionList = this.drillClickDimensionList.slice(0, index)
      if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
        this.backToParent(index, length)
      }

      // this.getData(this.param.id)
      this.calcData(true, 'chart', false, false)
    },
    // 回到父级地图
    backToParent(index, length) {
      if (length <= 0) return
      const times = length - 1 - index

      let temp = times
      let tempNode = this.currentAcreaNode
      while (temp >= 0) {
        tempNode = this.findEntityByCode(tempNode.pcode, this.places)
        temp--
      }

      this.currentAcreaNode = tempNode
      // this.$refs.dynamicChart && this.$refs.dynamicChart.registerDynamicMap && this.$refs.dynamicChart.registerDynamicMap(this.currentAcreaNode.code)
      const current = this.$refs.dynamicChart
      if (this.view.isPlugin) {
        current && current.callPluginInner && this.setDetailMapCode(this.currentAcreaNode.code) && current.callPluginInner({
          methodName: 'registerDynamicMap',
          methodParam: this.currentAcreaNode.code
        })
      } else {
        current && current.registerDynamicMap && this.setDetailMapCode(this.currentAcreaNode.code) && current.registerDynamicMap(this.currentAcreaNode.code)
      }
    },

    setDetailMapCode(code) {
      this.currentAreaCode = code
      this.curComponent.DetailAreaCode = code
      return true
    },

    // 切换下一级地图
    sendToChildren(param) {
      const length = param.data.dimensionList.length
      const name = param.data.dimensionList[length - 1].value
      let aCode = null
      if (this.currentAcreaNode) {
        aCode = this.currentAcreaNode.code
      }
      //   const aCode = this.currentAcreaNode ? this.currentAcreaNode.code : null
      const currentNode = this.findEntityByCode(aCode || this.view.customAttr.areaCode, this.places)
      if (currentNode && currentNode.children && currentNode.children.length > 0) {
        const nextNode = currentNode.children.find(item => item.name === name)
        if (!nextNode || !nextNode.code) return null
        // this.view.customAttr.areaCode = nextNode.code
        this.currentAcreaNode = nextNode
        // this.$refs.dynamicChart && this.$refs.dynamicChart.registerDynamicMap && this.$refs.dynamicChart.registerDynamicMap(nextNode.code)
        const current = this.$refs.dynamicChart
        if (this.view.isPlugin) {
          nextNode && current && current.callPluginInner && this.setDetailMapCode(nextNode.code) && current.callPluginInner({
            methodName: 'registerDynamicMap',
            methodParam: nextNode.code
          })
        } else {
          nextNode && current && current.registerDynamicMap && this.setDetailMapCode(nextNode.code) && current.registerDynamicMap(nextNode.code)
        }
        return nextNode
      }
    },

    findEntityByCode(code, array) {
      if (array === null || array.length === 0) array = this.places
      for (let index = 0; index < array.length; index++) {
        const node = array[index]
        if (node.code === code) return node
        if (node.children && node.children.length > 0) {
          const temp = this.findEntityByCode(code, node.children)
          if (temp) return temp
        }
      }
    },

    renderComponent() {
      return this.chart.render
    },

    reset() {
      const _this = this
      this.$confirm(this.$t('chart.view_reset_tips'), this.$t('chart.view_reset'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        resetViewCacheCallBack(_this.param.id, _this.panelInfo.id, function(rsp) {
          _this.changeEditStatus(false)
          _this.getChart(_this.param.id, 'panel')
          bus.$emit('view-in-cache', { type: 'propChange', viewId: _this.param.id })
        })
      }).catch(() => {
        // Do Nothing
      })
    },
    changeEditStatus(status) {
      this.hasEdit = status
      this.$store.commit('recordViewEdit', { viewId: this.param.id, hasEdit: status })
    },
    changeChartRender() {
      this.setChartDefaultOptions()
      this.calcData(true, 'chart', true, false, true)
    },
    changeChartType() {
      this.setChartDefaultOptions()
      this.calcData(true, 'chart', true, true)
    },

    setChartDefaultOptions() {
      const type = this.view.type
      if (type.includes('pie')) {
        if (this.view.render === 'echarts') {
          this.view.customAttr.label.position = 'inside'
        } else {
          this.view.customAttr.label.position = 'inner'
        }
      } else if (type.includes('line')) {
        this.view.customAttr.label.position = 'top'
      } else if (type.includes('treemap')) {
        if (this.view.render === 'echarts') {
          this.view.customAttr.label.position = 'inside'
        } else {
          this.view.customAttr.label.position = 'middle'
        }
      } else {
        if (this.view.render === 'echarts') {
          this.view.customAttr.label.position = 'inside'
        } else {
          this.view.customAttr.label.position = 'middle'
        }
      }
      // reset custom colors
      this.view.customAttr.color.seriesColors = []
    },

    valueFormatter(item) {
      this.valueFormatterItem = JSON.parse(JSON.stringify(item))
      this.showValueFormatter = true
    },
    closeValueFormatter() {
      this.showValueFormatter = false
    },
    saveValueFormatter() {
      const ele = this.valueFormatterItem.formatterCfg.decimalCount
      if (ele === undefined || ele.toString().indexOf('.') > -1 || parseInt(ele).toString() === 'NaN' || parseInt(ele) < 0 || parseInt(ele) > 10) {
        this.$message({
          message: this.$t('chart.formatter_decimal_count_error'),
          type: 'error',
          showClose: true
        })
        return
      }
      // 更新指标
      if (this.valueFormatterItem.formatterType === 'quota') {
        this.view.yaxis[this.valueFormatterItem.index].formatterCfg = this.valueFormatterItem.formatterCfg
      } else if (this.valueFormatterItem.formatterType === 'quotaExt') {
        this.view.yaxisExt[this.valueFormatterItem.index].formatterCfg = this.valueFormatterItem.formatterCfg
      } else if (this.valueFormatterItem.formatterType === 'dimension') {
        this.view.xaxis[this.valueFormatterItem.index].formatterCfg = this.valueFormatterItem.formatterCfg
      }
      this.calcData(true)
      this.closeValueFormatter()
    },

    customSort() {
      this.showCustomSort = true
    },
    customSortChange(val) {
      this.customSortList = val
    },
    closeCustomSort() {
      this.showCustomSort = false
      this.customSortField = {}
      this.customSortList = []
    },
    saveCustomSort() {
      this.view.xaxis.forEach(ele => {
        if (ele.id === this.customSortField.id) {
          ele.sort = 'custom_sort'
          ele.customSort = this.customSortList
        }
      })
      this.closeCustomSort()
      this.calcData(true)
    },

    stackCustomSort() {
      this.showStackCustomSort = true
    },
    closeStackCustomSort() {
      this.showStackCustomSort = false
    },
    saveStackCustomSort() {
      this.view.extStack.forEach(ele => {
        if (ele.id === this.customSortField.id) {
          ele.sort = 'custom_sort'
          ele.customSort = this.customSortList
        }
      })
      this.closeStackCustomSort()
      this.calcData(true)
    },

    fieldEdit(param) {
      switch (param.type) {
        case 'ds':
          this.editField()
          break
        case 'chart':
          this.editChartField()
          break
        default:
          break
      }
    },
    beforeFieldEdit(type) {
      return {
        type: type
      }
    },

    chartFieldEdit(param) {
      this.currEditField = JSON.parse(JSON.stringify(param.item))
      switch (param.type) {
        case 'copy':
          this.currEditField.id = null
          this.currEditField.extField = 1
          this.showChartCalcField()
          break
        case 'edit':
          this.showChartCalcField()
          break
        case 'delete':
          this.deleteChartCalcField(param.item)
          break
      }
    },
    handleChartFieldEdit(item, type) {
      return {
        type: type,
        item: item
      }
    },

    showChartCalcField() {
      this.editChartCalcField = true
    },
    closeChartCalcField() {
      this.editChartCalcField = false
      this.currEditField = {}
      this.initTableField(this.table.id)
      // 因动态计算较多，更新字段后重新计算视图数据
      this.calcData()
    },
    deleteChartCalcField(item) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('chart.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        post('/chart/field/delete/' + item.id + '/' + this.panelInfo.id, null).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('chart.delete_success'),
            showClose: true
          })
          this.initTableField(this.table.id)
        })
      }).catch(() => {
      })
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
  background-color: rgba(92,94,97, 0.7);
  position:absolute;
  top:0px;
  left: 0px;
  width: 350px;
  z-index: 2;
  cursor:not-allowed;
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
  width: 20px;
  height: 20px;
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

::v-deep .el-slider__runway.show-input{
  width: 80px!important;
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

.form-item-slider::v-deep.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item::v-deep.el-form-item__label{
  font-size: 12px;
}

.field-name{
  display: inline-block;
  width: 90px;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  position: absolute;
  top: 2px;
}

.field-setting{
  position: absolute;
  right: 8px;
}

.father .child {
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}

.field-split{
  height: calc(100% - 40px);
}

.field-split ::v-deep .fu-split-pane__left{
  padding-right: 0!important;
}

.field-split ::v-deep .fu-split-pane__right{
  padding-left: 0!important;
}

.view-panel-row ::v-deep .el-collapse-item__header{
  height: 34px !important;
  line-height: 34px !important;
  padding: 0 0 0 6px !important;
  font-size: 12px !important;
  font-weight: 400 !important;
}

</style>
