<template>
  <el-row v-loading="loading" style="height: 100%;overflow-y: hidden;width: 100%;border-left: 1px solid #E6E6E6">
    <el-tooltip :content="$t('chart.draw_back')">
      <el-button
        class="el-icon-d-arrow-right"
        style="position:absolute;left: 4px;top: 5px;z-index: 1000"
        size="mini"
        circle
        @click="closePanelEdit"
      />
    </el-tooltip>
    <!--    <i class="el-icon-d-arrow-right" style="position:absolute;left: 4px;top: 11px"></i>-->
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
        <!--        <el-button size="mini" @click="closePanelEdit">-->
        <!--          {{ $t('chart.draw_back') }}-->
        <!--        </el-button>-->
        <el-button type="warning" round size="mini" :disabled="!hasEdit" @click="reset">
          {{ $t('chart.recover') }}
        </el-button>
        <!--        <el-button size="mini" type="primary" @click="closeEdit">-->
        <!--          {{ $t('commons.save') }}-->
        <!--        </el-button>-->
      </span>
    </el-row>
    <el-row class="view-panel-row">
      <el-tabs :stretch="true" class="tab-header">
        <el-tab-pane :label="$t('chart.chart_data')" class="padding-tab" style="width: 300px">
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
            <el-col class="theme-border-class" :span="12" style="border-right: 1px solid #E6E6E6;">
              <div style="display: flex;align-items: center;justify-content: center;padding: 6px;">
                <el-input
                  v-model="searchField"
                  size="mini"
                  :placeholder="$t('chart.search')"
                  prefix-icon="el-icon-search"
                  clearable
                  class="main-area-input"
                />
                <el-button
                  :title="$t('dataset.edit_field')"
                  :disabled="!table || !hasDataPermission('manage',table.privileges)"
                  icon="el-icon-setting"
                  type="text"
                  size="mini"
                  style="float: right;width: 20px;margin-left: 4px;"
                  @click="editField"
                />
                <el-button
                  :title="$t('chart.change_ds')"
                  icon="el-icon-refresh"
                  type="text"
                  size="mini"
                  style="float: right;width: 20px;margin-left: 4px;"
                  @click="changeDs"
                />
              </div>
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
                    <span v-for="item in dimensionData" :key="item.id" class="item-dimension" :title="item.name">
                      <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                      <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                      <svg-icon
                        v-if="item.deType === 2 || item.deType === 3"
                        icon-class="field_value"
                        class="field-icon-value"
                      />
                      <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                      {{ item.name }}
                    </span>
                  </transition-group>
                </draggable>
              </div>
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
                      class="item-quota"
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
                      <span>{{ item.name }}</span>
                    </span>
                  </transition-group>
                </draggable>
              </div>
            </el-col>

            <el-col
              :span="12"
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
                                @change="changeChartType()"
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
                          <!--                          <el-row class="title-text" style="color: #909399;">-->
                          <!--                            <span>-->
                          <!--                              <span v-show="chart.type && (chart.type.includes('pie') || chart.type.includes('funnel') || chart.type.includes('text') || chart.type.includes('gauge') || chart.type.includes('treemap'))">-->
                          <!--                                Tips: {{ $t('chart.only_one_quota') }}-->
                          <!--                              </span>-->
                          <!--                              &lt;!&ndash;              <span v-show="chart.type && (chart.type.includes('text'))">&ndash;&gt;-->
                          <!--                              &lt;!&ndash;                Tips: {{ $t('chart.only_one_result') }}&ndash;&gt;-->
                          <!--                              &lt;!&ndash;              </span>&ndash;&gt;-->
                          <!--                              &lt;!&ndash;              <span v-show="chart.type && chart.type.includes('gauge')">&ndash;&gt;-->
                          <!--                              &lt;!&ndash;                Tips: {{ $t('chart.only_one_quota') }},{{ $t('chart.only_one_result') }}&ndash;&gt;-->
                          <!--                              &lt;!&ndash;              </span>&ndash;&gt;-->
                          <!--                            </span>-->
                          <!--                          </el-row>-->
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
                    <span style="width: 80px;text-align: right;">
                      {{ $t('chart.result_count') }}
                    </span>
                    <el-row>
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
                          v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall')"
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
                        /
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
                            @onDimensionItemChange="dimensionItemChange"
                            @onDimensionItemRemove="dimensionItemRemove"
                            @editItemFilter="showDimensionEditFilter"
                            @onNameEdit="showRename"
                          />
                        </transition-group>
                      </draggable>
                      <div v-if="!view.xaxis || view.xaxis.length === 0" class="drag-placeholder-style">
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
                          v-else-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'waterfall')"
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
                        /
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
                      <!--                    <el-button :disabled="!hasDataPermission('manage',param.privileges)" size="mini" class="filter-btn-class" @click="showResultFilter">-->
                      <!--                      {{ $t('chart.filter_condition') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
                      <!--                    </el-button>-->
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
                      v-if="view.type && !(view.type.includes('table') && view.render === 'echarts') && !view.type.includes('text') && !view.type.includes('gauge') && view.type !== 'liquid' && view.type !== 'word-cloud' && view.type !== 'table-pivot' && view.type !=='label'"
                      class="padding-lr"
                      style="margin-top: 6px;"
                    >
                      <span style="width: 80px;text-align: right;">
                        <span>{{ $t('chart.drill') }}</span>
                        /
                        <span>{{ $t('chart.dimension') }}</span>
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
        <el-tab-pane :label="$t('chart.chart_style')" class="padding-tab" style="width: 300px">
          <el-row class="view-panel">
            <plugin-com
              v-if="view.isPlugin"
              style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;"
              class="attr-style theme-border-class"
              :component-name="view.type + '-style'"
              :obj="{view, param, chart}"
            />
            <div
              v-else
              style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;padding-right: 6px"
              class="attr-style theme-border-class"
            >
              <el-row class="padding-lr">
                <span class="title-text">{{ $t('chart.style_priority') }}</span>
                <el-row>
                  <el-radio-group
                    v-model="view.stylePriority"
                    class="radio-span"
                    size="mini"
                    @change="calcStyle"
                  >
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
                  <el-collapse-item
                    v-show="view.render && view.render === 'echarts' && chart.type !== 'map' && chart.type !== 'waterfall' && chart.type !== 'word-cloud'"
                    name="size"
                    :title="$t('chart.size')"
                  >
                    <size-selector
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onSizeChange="onSizeChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.render && view.render === 'antv' && chart.type !== 'map' && chart.type !== 'waterfall' && chart.type !== 'word-cloud' && chart.type !== 'treemap' && chart.type !== 'funnel' && chart.type !== 'bar-stack'"
                    name="size"
                    :title="(chart.type && chart.type.includes('table')) ? $t('chart.table_config') : $t('chart.size')"
                  >
                    <size-selector-ant-v
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onSizeChange="onSizeChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="!view.type.includes('table') && !view.type.includes('text') && view.type !== 'word-cloud' && view.type !== 'label'"
                    name="label"
                    :title="$t('chart.label')"
                  >
                    <label-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onLabelChange="onLabelChange"
                    />
                    <label-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onLabelChange="onLabelChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="!view.type.includes('table') && !view.type.includes('text') && view.type !== 'liquid' && view.type !== 'gauge' && view.type !== 'label'"
                    name="tooltip"
                    :title="$t('chart.tooltip')"
                  >
                    <tooltip-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onTooltipChange="onTooltipChange"
                    />
                    <tooltip-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onTooltipChange="onTooltipChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.type === 'table-pivot'"
                    name="totalCfg"
                    :title="$t('chart.total_cfg')"
                  >
                    <total-cfg
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onTotalCfgChange="onTotalCfgChange"
                    />
                  </el-collapse-item>
                </el-collapse>
              </el-row>
              <el-row>
                <span class="padding-lr">{{ $t('chart.module_style') }}</span>
                <el-collapse v-model="styleActiveNames" class="style-collapse">
                  <el-collapse-item
                    v-show="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall')"
                    name="xAxis"
                    :title="$t('chart.xAxis')"
                  >
                    <x-axis-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeXAxisForm="onChangeXAxisForm"
                    />
                    <x-axis-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeXAxisForm="onChangeXAxisForm"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall')"
                    name="yAxis"
                    :title="view.type === 'chart-mix' ? $t('chart.yAxis_main') : $t('chart.yAxis')"
                  >
                    <y-axis-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeYAxisForm="onChangeYAxisForm"
                    />
                    <y-axis-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeYAxisForm="onChangeYAxisForm"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.type && view.type === 'chart-mix'"
                    name="yAxisExt"
                    :title="$t('chart.yAxis_ext')"
                  >
                    <y-axis-ext-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeYAxisForm="onChangeYAxisExtForm"
                    />
                    <y-axis-ext-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeYAxisForm="onChangeYAxisExtForm"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.type && view.type.includes('radar')"
                    name="split"
                    :title="$t('chart.split')"
                  >
                    <split-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeSplitForm="onChangeSplitForm"
                    />
                    <split-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeSplitForm="onChangeSplitForm"
                    />
                  </el-collapse-item>
                  <el-collapse-item v-show="view.type" name="title" :title="$t('chart.title')">
                    <title-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onTextChange="onTextChange"
                    />
                    <title-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onTextChange="onTextChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-show="view.type && view.type !== 'map' && !view.type.includes('table') && !view.type.includes('text') && view.type !== 'label' && (chart.type !== 'treemap' || chart.render === 'antv') && view.type !== 'liquid' && view.type !== 'waterfall' && chart.type !== 'gauge' && chart.type !== 'word-cloud'"
                    name="legend"
                    :title="$t('chart.legend')"
                  >
                    <legend-selector
                      v-if="view.render && view.render === 'echarts'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onLegendChange="onLegendChange"
                    />
                    <legend-selector-ant-v
                      v-else-if="view.render && view.render === 'antv'"
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onLegendChange="onLegendChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item v-if="chart.customStyle && view.customStyle.background" name="background" :title="$t('chart.background')">
                    <background-color-selector
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onChangeBackgroundForm="onChangeBackgroundForm"
                    />
                  </el-collapse-item>
                </el-collapse>
              </el-row>
            </div>
          </el-row>
        </el-tab-pane>
        <el-tab-pane :label="$t('chart.senior')" class="padding-tab" style="width: 300px;">
          <el-row class="view-panel">
            <div
              v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('mix') || view.type.includes('gauge'))"
              style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;"
              class="attr-style theme-border-class"
            >
              <el-row
                v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('mix'))"
              >
                <span class="padding-lr">{{ $t('chart.senior_cfg') }}</span>
                <el-collapse v-model="attrActiveNames" class="style-collapse">
                  <el-collapse-item name="function" :title="$t('chart.function_cfg')">
                    <function-cfg
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onFunctionCfgChange="onFunctionCfgChange"
                    />
                  </el-collapse-item>
                </el-collapse>
              </el-row>
              <el-row
                v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('mix') || view.type.includes('gauge'))"
              >
                <span class="padding-lr">{{ $t('chart.analyse_cfg') }}</span>
                <el-collapse v-model="styleActiveNames" class="style-collapse">
                  <el-collapse-item
                    v-if="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('mix'))"
                    name="analyse"
                    :title="$t('chart.assist_line')"
                  >
                    <assist-line
                      :param="param"
                      class="attr-selector"
                      :chart="chart"
                      @onAssistLineChange="onAssistLineChange"
                    />
                  </el-collapse-item>
                  <el-collapse-item
                    v-if="view.type && (view.type.includes('gauge'))"
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
              class="chart-class"
            />
            <chart-component
              v-else-if="httpRequest.status && chart.type && !chart.type.includes('table') && !chart.type.includes('text') && chart.type !== 'label' && renderComponent() === 'echarts'"
              ref="dynamicChart"
              :chart-id="chart.id"
              :chart="chart"
              class="chart-class"
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
      <table-selector @getTable="getTable" />
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
          {{ $t('dataset.field_manage') }}
          <span v-if="table">[{{ table.name }}]</span>
        </span>
        <el-button size="mini" style="float: right;" @click="closeEditDsField">{{ $t('chart.close') }}</el-button>
      </div>
    </el-dialog>

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
  </el-row>
</template>

<script>
import {
  ajaxGetDataOnly,
  post,
  getChartDetails,
  save2Cache,
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
  DEFAULT_BACKGROUND_COLOR,
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
import QuotaExtItem from '@/views/chart/components/drag-item/QuotaExtItem'
import YAxisExtSelector from '@/views/chart/components/component-style/YAxisExtSelector'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import ChartType from '@/views/chart/view/ChartType'
import TitleSelectorAntV from '@/views/chart/components/component-style/TitleSelectorAntV'
import LabelSelectorAntV from '@/views/chart/components/shape-attr/LabelSelectorAntV'
import TooltipSelectorAntV from '@/views/chart/components/shape-attr/TooltipSelectorAntV'
import LegendSelectorAntV from '@/views/chart/components/component-style/LegendSelectorAntV'
import XAxisSelectorAntV from '@/views/chart/components/component-style/XAxisSelectorAntV'
import YAxisSelectorAntV from '@/views/chart/components/component-style/YAxisSelectorAntV'
import YAxisExtSelectorAntV from '@/views/chart/components/component-style/YAxisExtSelectorAntV'
import SizeSelectorAntV from '@/views/chart/components/shape-attr/SizeSelectorAntV'
import SplitSelectorAntV from '@/views/chart/components/component-style/SplitSelectorAntV'
import CompareEdit from '@/views/chart/components/compare/CompareEdit'
import { compareItem } from '@/views/chart/chart/compare'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import DimensionExtItem from '@/views/chart/components/drag-item/DimensionExtItem'
import PluginCom from '@/views/system/plugin/PluginCom'
import { mapState } from 'vuex'

import FunctionCfg from '@/views/chart/components/senior/FunctionCfg'
import AssistLine from '@/views/chart/components/senior/AssistLine'
import Threshold from '@/views/chart/components/senior/Threshold'
import TotalCfg from '@/views/chart/components/shape-attr/TotalCfg'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import { pluginTypes } from '@/api/chart/chart'
export default {
  name: 'ChartEdit',
  components: {
    LabelNormalText,
    TotalCfg,
    Threshold,
    AssistLine,
    FunctionCfg,
    DimensionExtItem,
    ChartComponentS2,
    CompareEdit,
    SplitSelectorAntV,
    SizeSelectorAntV,
    YAxisExtSelectorAntV,
    YAxisSelectorAntV,
    XAxisSelectorAntV,
    LegendSelectorAntV,
    TooltipSelectorAntV,
    LabelSelectorAntV,
    TitleSelectorAntV,
    ChartType,
    ChartComponentG2,
    YAxisExtSelector,
    QuotaExtItem,
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
    ChartDragItem,
    DrillItem,
    DrillPath,
    PluginCom
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
          background: DEFAULT_BACKGROUND_COLOR,
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
      pluginRenderOptions: []

    }
  },
  computed: {
    chartType() {
      return this.chart.type
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'panelViewEditInfo'
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
    'param': function(val) {
      if (this.param.optType === 'new') {
        //
      } else if (this.param.id !== this.preChartId) {
        this.preChartId = this.param.id
        this.chartInit()
        // console.log('fromwatch:' + JSON.stringify(val))
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
    this.bindPluginEvent()
    this.initFromPanel()
    this.chartInit()
    // console.log('mounted')
  },
  activated() {
  },

  methods: {
    loadPluginType() {
      const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views')) || []
      const pluginOptions = plugins.filter(plugin => !this.renderOptions.some(option => option.value === plugin.render)).map(plugin => {
        return { name: plugin.render, value: plugin.render }
      })
      this.pluginRenderOptions = [...this.renderOptions, ...pluginOptions]
    },
    emptyTableData() {
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
      this.resetDrill()
      this.initFromPanel()
      this.getChart(this.param.id)
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
    },
    initTableData(id) {
      if (id != null) {
        post('/dataset/table/getWithPermission/' + id, null).then(response => {
          this.table = response.data
          this.initTableField(id)
        }).catch(err => {
          this.table = null
          this.resetDatasetField()
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
    buildParam(getData, trigger, needRefreshGroup = false, switchType = false) {
      if (!this.view.resultCount ||
        this.view.resultCount === '' ||
        isNaN(Number(this.view.resultCount)) ||
        String(this.view.resultCount).includes('.') ||
        parseInt(this.view.resultCount) < 1) {
        this.view.resultCount = '1000'
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
      if (view.type === 'table-pivot') {
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
        if (!ele.compareCalc) {
          ele.compareCalc = compareItem
        }
      })
      if (view.type === 'chart-mix') {
        view.yaxisExt.forEach(function(ele) {
          if (!ele.chartType) {
            ele.chartType = 'bar'
          }
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
        view.type === 'waterfall') {
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
    // calcData(getData, trigger, needRefreshGroup = false, switchType = false) {
    // this.hasEdit = true
    // const view = this.buildParam(getData, trigger, needRefreshGroup, switchType)
    // if (!view) return
    // post('/chart/view/calcData/' + this.panelInfo.id, {
    //   view: view,
    //   requestList: {
    //     filter: [],
    //     drill: this.drillClickDimensionList
    //   }
    // }).then(response => {
    //   const view = JSON.parse(JSON.stringify(response.data))
    //   this.view.xaxis = view.xaxis ? JSON.parse(view.xaxis) : []
    //   this.view.xaxisExt = view.xaxisExt ? JSON.parse(view.xaxisExt) : []
    //   this.view.yaxis = view.yaxis ? JSON.parse(view.yaxis) : []
    //   this.view.yaxisExt = view.yaxisExt ? JSON.parse(view.yaxisExt) : []
    //   this.view.extStack = view.extStack ? JSON.parse(view.extStack) : []
    //   this.view.drillFields = view.drillFields ? JSON.parse(view.drillFields) : []
    //   this.view.extBubble = view.extBubble ? JSON.parse(view.extBubble) : []
    //   this.view.customAttr = view.customAttr ? JSON.parse(view.customAttr) : {}
    //   this.view.customStyle = view.customStyle ? JSON.parse(view.customStyle) : {}
    //   this.view.customFilter = view.customFilter ? JSON.parse(view.customFilter) : {}
    // this.view.senior = view.senior ? JSON.parse(view.senior) : {}
    // 将视图传入echart组件
    //   this.chart = response.data
    //   this.data = response.data.data
    //   // console.log(JSON.stringify(this.chart))
    //   this.httpRequest.status = true
    //   if (this.chart.privileges) {
    //     this.param.privileges = this.chart.privileges
    //   }
    //   if (!response.data.drill) {
    //     this.drillClickDimensionList.splice(this.drillClickDimensionList.length - 1, 1)
    //
    //     this.resetDrill()
    //   }
    //   this.drill = response.data.drill
    //   this.drillFilters = JSON.parse(JSON.stringify(response.data.drillFilters ? response.data.drillFilters : []))
    //
    //   this.closeChangeChart()
    // })
    // },
    calcData(getData, trigger, needRefreshGroup = false, switchType = false) {
      this.changeEditStatus(true)
      const view = this.buildParam(true, 'chart', false, switchType)
      if (!view) return
      save2Cache(this.panelInfo.id, view).then(() => {
        bus.$emit('view-in-cache', { type: 'propChange', viewId: this.param.id })
      })
    },
    calcStyle() {
      this.changeEditStatus(true)
      // 将视图传入echart...组件
      const view = JSON.parse(JSON.stringify(this.view))
      view.xaxis = JSON.stringify(this.view.xaxis)
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
      save2Cache(this.panelInfo.id, viewSave)

      bus.$emit('view-in-cache', { type: 'styleChange', viewId: this.param.id, viewInfo: view })
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
      this.hasEdit = false
      if (id) {
        ajaxGetDataOnly(id, this.panelInfo.id, {
          filter: [],
          drill: this.drillClickDimensionList,
          queryFrom: 'panelEdit'
        }).then(response => {
          this.initTableData(response.data.tableId)
          this.view = JSON.parse(JSON.stringify(response.data))
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
          // console.log(JSON.stringify(this.chart))
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
          if (response.data.dataFrom === 'template') {
            this.emptyTableData()
          } else {
            this.initTableData(response.data.tableId)
          }
          this.view = JSON.parse(JSON.stringify(response.data))
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
      // console.log(e)
      this.moveId = e.draggedContext.element.id
      return true
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
      this.calcStyle()
    },

    onSizeChange(val) {
      this.view.customAttr.size = val
      this.calcStyle()
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
      this.calcStyle()
    },

    onThresholdChange(val) {
      this.view.senior.threshold = val
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
      if (this.view.tableId !== this.changeTable.id) {
        this.view.tableId = this.changeTable.id
        this.view.xaxis = []
        this.view.xaxisExt = []
        this.view.yaxis = []
        this.view.yaxisExt = []
        this.view.customFilter = []
        this.view.extStack = []
        this.view.extBubble = []
        this.view.drillFields = []
      }
      // this.save(true, 'chart', false)
      this.calcData(true, 'chart', false)
      this.initTableData(this.view.tableId)
      this.closeChangeChart()
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
        this.dragCheckType(this.view.xaxis, 'd')
      }
      this.dragMoveDuplicate(this.view.xaxis, e)
      if ((this.view.type === 'map' || this.view.type === 'word-cloud') && this.view.xaxis.length > 1) {
        this.view.xaxis = [this.view.xaxis[0]]
      }
      this.calcData(true)
    },
    addYaxis(e) {
      this.dragCheckType(this.view.yaxis, 'q')
      this.dragMoveDuplicate(this.view.yaxis, e)
      if ((this.view.type === 'map' || this.view.type === 'waterfall' || this.view.type === 'word-cloud') && this.view.yaxis.length > 1) {
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
        current && current.callPluginInner && current.callPluginInner({
          methodName: 'registerDynamicMap',
          methodParam: this.currentAcreaNode.code
        })
      } else {
        current && current.registerDynamicMap && current.registerDynamicMap(this.currentAcreaNode.code)
      }
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
          nextNode && current && current.callPluginInner && current.callPluginInner({
            methodName: 'registerDynamicMap',
            methodParam: nextNode.code
          })
        } else {
          nextNode && current && current.registerDynamicMap && current.registerDynamicMap(nextNode.code)
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
      this.changeEditStatus(false)
      this.getChart(this.param.id, 'panel')
      const _this = this
      resetViewCacheCallBack(this.param.id, this.panelInfo.id, function(rsp) {
        bus.$emit('view-in-cache', { type: 'propChange', viewId: _this.param.id })
      })
    },
    changeEditStatus(status) {
      this.hasEdit = status
      this.$store.commit('recordViewEdit', { viewId: this.param.id, hasEdit: status })
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
  height: calc(100vh - 75px);
}

.view-panel-Mask {
  display: flex;
  height: calc(100vh - 60px);
  background-color: #5c5e61;
  opacity: 0.7;
  position:absolute;
  top:0px;
  left: 0px;
  width: 300px;
  z-index: 2;
  cursor:not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-panel {
  display: flex;
  height: calc(100% - 80px);
  background-color: #f7f8fa;
}

.blackTheme .view-panel {
  background-color: var(--MainBG);
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
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

.tab-header > > > .el-tabs__header {
  border-top: solid 1px #eee;
  border-right: solid 1px #eee;
}

.tab-header > > > .el-tabs__item {
  font-size: 12px;
  padding: 0 20px !important;
}

.blackTheme .tab-header > > > .el-tabs__item {
  background-color: var(--MainBG);
}

.tab-header > > > .el-tabs__nav-scroll {
  padding-left: 0 !important;
}

.tab-header > > > .el-tabs__header {
  margin: 0 !important;
}

.tab-header > > > .el-tabs__content {
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

.el-radio > > > .el-radio__label {
  padding-left: 0;
}

.attr-style {
  height: calc(100vh - 56px - 60px - 40px - 40px);
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

.dialog-css > > > .el-dialog__title {
  font-size: 14px;
}

.dialog-css > > > .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css > > > .el-dialog__body {
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
  height: calc(50% - 20px);
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
  > > > div.vue-treeselect__control {
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

.result-count > > > input {
  padding: 0 4px;
}

.radio-span > > > .el-radio__label {
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
  width: 128px !important;
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

</style>
