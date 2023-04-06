<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="sizeFormBar"
        :model="sizeForm"
        label-width="80px"
        size="mini"
      >
        <!--bar-begin-->
        <el-form-item
          v-show="showProperty('barDefault')"
          :label="$t('chart.adapt')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.barDefault"
            @change="changeBarSizeCase('barDefault')"
          >{{ $t('chart.adapt') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('barGap')"
          :label="$t('chart.bar_gap')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.barGap"
            :disabled="sizeForm.barDefault"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="5"
            :step="0.1"
            @change="changeBarSizeCase('barGap')"
          />
        </el-form-item>
        <!--bar-end-->
        <!--line-begin-->
        <el-form-item
          v-show="showProperty('lineWidth')"
          :label="$t('chart.line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineWidth"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="10"
            @change="changeBarSizeCase('lineWidth')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('lineSymbol')"
          :label="$t('chart.line_symbol')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.lineSymbol"
            :placeholder="$t('chart.line_symbol')"
            @change="changeBarSizeCase('lineSymbol')"
          >
            <el-option
              v-for="item in lineSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('lineSymbolSize')"
          :label="$t('chart.line_symbol_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineSymbolSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="20"
            @change="changeBarSizeCase('lineSymbolSize')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('lineSmooth')"
          :label="$t('chart.line_smooth')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.lineSmooth"
            @change="changeBarSizeCase('lineSmooth')"
          >{{ $t('chart.line_smooth') }}
          </el-checkbox>
        </el-form-item>
        <!--line-end-->

        <!--pie-begin-->
        <el-form-item
          v-show="showProperty('pieInnerRadius')"
          :label="$t('chart.pie_inner_radius_percent')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.pieInnerRadius"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('pieInnerRadius')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('pieOuterRadius')"
          :label="$t('chart.pie_outer_radius_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.pieOuterRadius"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('pieOuterRadius')"
          />
        </el-form-item>
        <!--pie-end-->
        <!--radar-begin-->
        <el-form-item
          v-show="showProperty('radarShape')"
          :label="$t('chart.shape')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.radarShape"
            size="mini"
            @change="changeBarSizeCase('radarShape')"
          >
            <el-radio-button label="polygon">{{ $t('chart.polygon') }}</el-radio-button>
            <el-radio-button label="circle">{{ $t('chart.circle') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-show="showProperty('radarSize')"
          :label="$t('chart.radar_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.radarSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('radarSize')"
          />
        </el-form-item>
        <!--radar-end-->
        <!--table-begin-->

        <el-form-item
          v-show="showProperty('tablePageMode')"
          label-width="100px"
          :label="$t('chart.table_page_mode')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tablePageMode"
            :placeholder="$t('chart.table_page_mode')"
            @change="changeBarSizeCase('tablePageMode')"
          >
            <el-option
              :label="$t('chart.page_mode_page')"
              value="page"
            />
            <el-option
              :label="$t('chart.page_mode_pull')"
              value="pull"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tablePageSize')&&sizeForm.tablePageMode === 'page'"
          label-width="100px"
          :label="$t('chart.table_page_size')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tablePageSize"
            :placeholder="$t('chart.table_page_size')"
            @change="changeBarSizeCase('tablePageSize')"
          >
            <el-option
              v-for="item in pageSizeOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableTitleFontSize')"
          label-width="100px"
          :label="$t('chart.table_title_fontsize')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tableTitleFontSize"
            :placeholder="$t('chart.table_title_fontsize')"
            @change="changeBarSizeCase('tableTitleFontSize')"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableItemFontSize')"
          label-width="100px"
          :label="$t('chart.table_item_fontsize')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tableItemFontSize"
            :placeholder="$t('chart.table_item_fontsize')"
            @change="changeBarSizeCase('tableItemFontSize')"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableHeaderAlign')"
          label-width="100px"
          :label="$t('chart.table_header_align')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tableHeaderAlign"
            :placeholder="$t('chart.table_header_align')"
            @change="changeBarSizeCase('tableHeaderAlign')"
          >
            <el-option
              v-for="option in alignOptions"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableItemAlign')"
          label-width="100px"
          :label="$t('chart.table_item_align')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.tableItemAlign"
            :placeholder="$t('chart.table_item_align')"
            @change="changeBarSizeCase('tableItemAlign')"
          >
            <el-option
              v-for="option in alignOptions"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableTitleHeight')"
          label-width="100px"
          :label="$t('chart.table_title_height')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableTitleHeight"
            :min="20"
            :max="100"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="changeBarSizeCase('tableTitleHeight')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableItemHeight')"
          label-width="100px"
          :label="$t('chart.table_item_height')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableItemHeight"
            :min="20"
            :max="100"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="changeBarSizeCase('tableItemHeight')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableColumnMode')"
          label-width="100px"
          :label="$t('chart.table_column_width_config')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.tableColumnMode"
            @change="changeBarSizeCase('tableColumnMode')"
          >
            <el-radio label="adapt"><span>{{ $t('chart.table_column_adapt') }}</span></el-radio>
            <el-radio label="custom">
              <span>{{ $t('chart.table_column_custom') }}</span>
            </el-radio>
          </el-radio-group>
          <el-tooltip
            class="item"
            effect="dark"
            placement="bottom"
          >
            <div
              slot="content"
              v-html="$t('chart.table_column_width_tip')"
            />
            <i
              class="el-icon-info"
              style="cursor: pointer;color: #606266;margin-left: 4px;"
            />
          </el-tooltip>
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableColumnMode') && sizeForm.tableColumnMode === 'custom'"
          label=""
          label-width="100px"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableColumnWidth"
            :min="10"
            :max="500"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="changeBarSizeCase('tableColumnWidth')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('showIndex')"
          label-width="100px"
          :label="$t('chart.table_show_index')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.showIndex"
            input-size="mini"
            @change="changeBarSizeCase('showIndex')"
          >
            <el-radio :label="true">{{ $t('panel.yes') }}</el-radio>
            <el-radio :label="false">{{ $t('panel.no') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-show="showProperty('indexLabel') && sizeForm.showIndex"
          label-width="100px"
          :label="$t('chart.table_index_desc')"
          class="form-item"
        >
          <el-input
            v-model="sizeForm.indexLabel"
            type="text"
            @blur="changeBarSizeCase('indexLabel')"
          />
        </el-form-item>

        <!--chart-mix-start-->
        <span v-show="showProperty('mix')">
          <el-divider
            content-position="center"
            class="divider-style"
          >{{ $t('chart.chart_bar') }}</el-divider>
          <el-form-item
            :label="$t('chart.adapt')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.barDefault"
              @change="changeBarSizeCase('barDefault')"
            >{{ $t('chart.adapt') }}</el-checkbox>
          </el-form-item>
          <el-form-item
            :label="$t('chart.bar_gap')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.barGap"
              :disabled="sizeForm.barDefault"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="5"
              :step="0.1"
              @change="changeBarSizeCase('barGap')"
            />
          </el-form-item>
          <el-divider
            content-position="center"
            class="divider-style"
          >{{ $t('chart.chart_line') }}</el-divider>
          <el-form-item
            :label="$t('chart.line_width')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.lineWidth"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="10"
              @change="changeBarSizeCase('lineWidth')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.line_symbol')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.lineSymbol"
              :placeholder="$t('chart.line_symbol')"
              @change="changeBarSizeCase('lineSymbol')"
            >
              <el-option
                v-for="item in lineSymbolOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('chart.line_symbol_size')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.lineSymbolSize"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="20"
              @change="changeBarSizeCase('lineSymbolSize')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.line_smooth')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.lineSmooth"
              @change="changeBarSizeCase('lineSmooth')"
            >{{ $t('chart.line_smooth') }}
            </el-checkbox>
          </el-form-item>
          <el-divider
            content-position="center"
            class="divider-style"
          >{{ $t('chart.chart_scatter') }}</el-divider>
          <el-form-item
            :label="$t('chart.bubble_symbol')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.scatterSymbol"
              :placeholder="$t('chart.line_symbol')"
              @change="changeBarSizeCase('scatterSymbol')"
            >
              <el-option
                v-for="item in lineSymbolOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('chart.bubble_size')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.scatterSymbolSize"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="1"
              :max="40"
              @change="changeBarSizeCase('scatterSymbolSize')"
            />
          </el-form-item>
        </span>
        <!--chart-mix-end-->
      </el-form>
      <!--table-end-->

      <!--gauge-begin-->
      <el-form
        ref="sizeFormGauge"
        :model="sizeForm"
        label-width="100px"
        size="mini"
      >
        <el-form-item
          v-show="showProperty('gaugeMin')"
          :label="$t('chart.min')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.gaugeMinType"
            size="mini"
            @change="changeQuotaField('min')"
          >
            <el-radio-button label="fix">{{ $t('chart.fix') }}</el-radio-button>
            <el-radio-button label="dynamic">{{ $t('chart.dynamic') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeMin') && sizeForm.gaugeMinType === 'fix'"
          class="form-item form-item-slider"
        >
          <el-input-number
            v-model="sizeForm.gaugeMin"
            size="mini"
            @change="changeBarSizeCase('gaugeMin')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeMin') && sizeForm.gaugeMinType === 'dynamic'"
          class="form-item form-flex"
        >
          <el-select
            v-model="sizeForm.gaugeMinField.id"
            :placeholder="$t('chart.field')"
            @change="changeQuotaField('min',true)"
          >
            <el-option
              v-for="item in quotaData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
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
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>
            </el-option>
          </el-select>
          <el-select
            v-model="sizeForm.gaugeMinField.summary"
            :placeholder="$t('chart.summary')"
            @change="changeQuotaField('min')"
          >
            <el-option
              v-if="validMinField"
              key="sum"
              value="sum"
              :label="$t('chart.sum')"
            />
            <el-option
              v-if="validMinField"
              key="avg"
              value="avg"
              :label="$t('chart.avg')"
            />
            <el-option
              v-if="validMinField"
              key="max"
              value="max"
              :label="$t('chart.max')"
            />
            <el-option
              v-if="validMinField"
              key="min"
              value="min"
              :label="$t('chart.min')"
            />
            <el-option
              v-if="validMinField"
              key="stddev_pop"
              value="stddev_pop"
              :label="$t('chart.stddev_pop')"
            />
            <el-option
              v-if="validMinField"
              key="var_pop"
              value="var_pop"
              :label="$t('chart.var_pop')"
            />
            <el-option
              key="count"
              value="count"
              :label="$t('chart.count')"
            />
            <el-option
              v-if="minField.id !== 'count'"
              key="count_distinct"
              value="count_distinct"
              :label="$t('chart.count_distinct')"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-show="showProperty('gaugeMax')"
          :label="$t('chart.max')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.gaugeMaxType"
            size="mini"
            @change="changeQuotaField('max')"
          >
            <el-radio-button label="fix">{{ $t('chart.fix') }}</el-radio-button>
            <el-radio-button label="dynamic">{{ $t('chart.dynamic') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeMax') && sizeForm.gaugeMaxType === 'fix'"
          class="form-item form-item-slider"
        >
          <el-input-number
            v-model="sizeForm.gaugeMax"
            size="mini"
            @change="changeBarSizeCase('gaugeMax')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeMax') && sizeForm.gaugeMaxType === 'dynamic'"
          class="form-item form-flex"
        >
          <el-select
            v-model="sizeForm.gaugeMaxField.id"
            :placeholder="$t('chart.field')"
            @change="changeQuotaField('max',true)"
          >
            <el-option
              v-for="item in quotaData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
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
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>
            </el-option>
          </el-select>
          <el-select
            v-model="sizeForm.gaugeMaxField.summary"
            :placeholder="$t('chart.summary')"
            @change="changeQuotaField('max')"
          >
            <el-option
              v-if="validMaxField"
              key="sum"
              value="sum"
              :label="$t('chart.sum')"
            />
            <el-option
              v-if="validMaxField"
              key="avg"
              value="avg"
              :label="$t('chart.avg')"
            />
            <el-option
              v-if="validMaxField"
              key="max"
              value="max"
              :label="$t('chart.max')"
            />
            <el-option
              v-if="validMaxField"
              key="min"
              value="min"
              :label="$t('chart.min')"
            />
            <el-option
              v-if="validMaxField"
              key="stddev_pop"
              value="stddev_pop"
              :label="$t('chart.stddev_pop')"
            />
            <el-option
              v-if="validMaxField"
              key="var_pop"
              value="var_pop"
              :label="$t('chart.var_pop')"
            />
            <el-option
              key="count"
              value="count"
              :label="$t('chart.count')"
            />
            <el-option
              v-if="maxField.id !== 'count'"
              key="count_distinct"
              value="count_distinct"
              :label="$t('chart.count_distinct')"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-show="showProperty('gaugeStartAngle')"
          :label="$t('chart.start_angle')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.gaugeStartAngle"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="-360"
            :max="360"
            @change="changeBarSizeCase('gaugeStartAngle')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('gaugeEndAngle')"
          :label="$t('chart.end_angle')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.gaugeEndAngle"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="-360"
            :max="360"
            @change="changeBarSizeCase('gaugeEndAngle')"
          />
        </el-form-item>
        <!--        <el-form-item v-show="showProperty('gaugeTickCount')" :label="$t('chart.tick_count')" class="form-item form-item-slider">-->
        <!--          <el-input-number v-model="sizeForm.gaugeTickCount" :min="1" :step="1" :precision="0" size="mini" @change="changeBarSizeCase('gaugeTickCount')" />-->
        <!--        </el-form-item>-->
      </el-form>
      <!--gauge-end-->

      <el-form
        ref="sizeFormPie"
        :model="sizeForm"
        label-width="100px"
        size="mini"
      >
        <!--text&label-start-->
        <el-form-item
          v-show="showProperty('quotaFontSize')"
          :label="$t('chart.quota_font_size')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaFontSize"
            :placeholder="$t('chart.quota_font_size')"
            @change="changeBarSizeCase('quotaFontSize')"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('quotaFontFamily')"
          :label="$t('chart.quota_font_family')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaFontFamily"
            :placeholder="$t('chart.quota_font_family')"
            @change="changeBarSizeCase('quotaFontFamily')"
          >
            <el-option
              v-for="option in fontFamily"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('quotaFontStyle')"
          :label="$t('chart.quota_text_style')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaFontIsItalic"
            @change="changeBarSizeCase('quotaFontIsItalic')"
          >{{ $t('chart.italic') }}</el-checkbox>
          <el-checkbox
            v-model="sizeForm.quotaFontIsBolder"
            @change="changeBarSizeCase('quotaFontIsBolder')"
          >{{ $t('chart.bolder') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('quotaLetterSpace')"
          :label="$t('chart.quota_letter_space')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaLetterSpace"
            :placeholder="$t('chart.quota_letter_space')"
            @change="changeBarSizeCase('quotaLetterSpace')"
          >
            <el-option
              v-for="option in fontLetterSpace"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('quotaFontShadow')"
          :label="$t('chart.font_shadow')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaFontShadow"
            @change="changeBarSizeCase('quotaFontShadow')"
          >{{ $t('chart.font_shadow') }}</el-checkbox>
        </el-form-item>
        <el-divider v-if="showProperty('dimensionShow')" />
        <el-form-item
          v-show="showProperty('dimensionShow')"
          :label="$t('chart.dimension_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.dimensionShow"
            @change="changeBarSizeCase('dimensionShow')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="sizeForm.dimensionShow">
          <el-form-item
            v-show="showProperty('dimensionFontSize')"
            :label="$t('chart.dimension_font_size')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.dimensionFontSize"
              :placeholder="$t('chart.dimension_font_size')"
              @change="changeBarSizeCase('dimensionFontSize')"
            >
              <el-option
                v-for="option in fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('dimensionFontFamily')"
            :label="$t('chart.dimension_font_family')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.dimensionFontFamily"
              :placeholder="$t('chart.dimension_font_family')"
              @change="changeBarSizeCase('dimensionFontFamily')"
            >
              <el-option
                v-for="option in fontFamily"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('dimensionFontStyle')"
            :label="$t('chart.dimension_text_style')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.dimensionFontIsItalic"
              @change="changeBarSizeCase('dimensionFontIsItalic')"
            >{{ $t('chart.italic') }}</el-checkbox>
            <el-checkbox
              v-model="sizeForm.dimensionFontIsBolder"
              @change="changeBarSizeCase('dimensionFontIsBolder')"
            >{{ $t('chart.bolder') }}</el-checkbox>
          </el-form-item>
          <el-form-item
            v-show="showProperty('dimensionLetterSpace')"
            :label="$t('chart.dimension_letter_space')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.dimensionLetterSpace"
              :placeholder="$t('chart.dimension_letter_space')"
              @change="changeBarSizeCase('dimensionLetterSpace')"
            >
              <el-option
                v-for="option in fontLetterSpace"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('dimensionFontShadow')"
            :label="$t('chart.font_shadow')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.dimensionFontShadow"
              @change="changeBarSizeCase('dimensionFontShadow')"
            >{{ $t('chart.font_shadow') }}</el-checkbox>
          </el-form-item>
          <el-divider v-if="showProperty('spaceSplit')" />
          <el-form-item
            v-show="showProperty('spaceSplit')"
            :label="$t('chart.space_split')"
            class="form-item"
          >
            <el-input-number
              v-model="sizeForm.spaceSplit"
              :min="0"
              size="mini"
              @change="changeBarSizeCase('spaceSplit')"
            />
          </el-form-item>
          <el-form-item
            v-show="showProperty('hPosition')"
            :label="$t('chart.h_position')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.hPosition"
              :placeholder="$t('chart.h_position')"
              @change="changeBarSizeCase('hPosition')"
            >
              <el-option
                value="start"
                :label="$t('chart.p_left')"
              >{{ $t('chart.p_left') }}</el-option>
              <el-option
                value="center"
                :label="$t('chart.p_center')"
              >{{ $t('chart.p_center') }}</el-option>
              <el-option
                value="end"
                :label="$t('chart.p_right')"
              >{{ $t('chart.p_right') }}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="showProperty('vPosition')"
            :label="$t('chart.v_position')"
            class="form-item"
          >
            <el-select
              v-model="sizeForm.vPosition"
              :placeholder="$t('chart.v_position')"
              @change="changeBarSizeCase('vPosition')"
            >
              <el-option
                value="start"
                :label="$t('chart.p_top')"
              >{{ $t('chart.p_top') }}</el-option>
              <el-option
                value="center"
                :label="$t('chart.p_center')"
              >{{ $t('chart.p_center') }}</el-option>
              <el-option
                value="end"
                :label="$t('chart.p_bottom')"
              >{{ $t('chart.p_bottom') }}</el-option>
            </el-select>
          </el-form-item>
        </div>
        <!--text&label-end-->
        <!--scatter-begin-->
        <el-form-item
          v-show="showProperty('scatterSymbol')"
          :label="$t('chart.bubble_symbol')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.scatterSymbol"
            :placeholder="$t('chart.line_symbol')"
            @change="changeBarSizeCase('scatterSymbol')"
          >
            <el-option
              v-for="item in lineSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('scatterSymbolSize')"
          :label="$t('chart.bubble_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.scatterSymbolSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="1"
            :max="40"
            @change="changeBarSizeCase('scatterSymbolSize')"
          />
        </el-form-item>
        <!--scatter-end-->
        <!--liquid-begin-->
        <el-form-item
          v-show="showProperty('liquidShape')"
          :label="$t('chart.liquid_shape')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.liquidShape"
            :placeholder="$t('chart.liquid_shape')"
            @change="changeBarSizeCase('liquidShape')"
          >
            <el-option
              v-for="item in liquidShapeOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('liquidMax')"
          :label="$t('chart.liquid_max')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.liquidMaxType"
            size="mini"
            @change="changeQuotaField('max')"
          >
            <el-radio-button label="fix">{{ $t('chart.fix') }}</el-radio-button>
            <el-radio-button label="dynamic">{{ $t('chart.dynamic') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="showProperty('liquidMax') && sizeForm.liquidMaxType === 'fix'"
          class="form-item form-item-slider"
        >
          <el-input-number
            v-model="sizeForm.liquidMax"
            :min="1"
            size="mini"
            @change="changeBarSizeCase('liquidMax')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('liquidMax') && sizeForm.liquidMaxType === 'dynamic'"
          class="form-item form-flex"
        >
          <el-select
            v-model="sizeForm.liquidMaxField.id"
            :placeholder="$t('chart.field')"
            @change="changeQuotaField('max',true)"
          >
            <el-option
              v-for="item in quotaData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
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
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>
            </el-option>
          </el-select>
          <el-select
            v-model="sizeForm.liquidMaxField.summary"
            :placeholder="$t('chart.summary')"
            @change="changeQuotaField('max')"
          >
            <el-option
              v-if="validLiquidMaxField"
              key="sum"
              value="sum"
              :label="$t('chart.sum')"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="avg"
              value="avg"
              :label="$t('chart.avg')"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="max"
              value="max"
              :label="$t('chart.max')"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="min"
              value="min"
              :label="$t('chart.min')"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="stddev_pop"
              value="stddev_pop"
              :label="$t('chart.stddev_pop')"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="var_pop"
              value="var_pop"
              :label="$t('chart.var_pop')"
            />
            <el-option
              key="count"
              value="count"
              :label="$t('chart.count')"
            />
            <el-option
              v-if="liquidMaxField.id !== 'count'"
              key="count_distinct"
              value="count_distinct"
              :label="$t('chart.count_distinct')"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-show="showProperty('liquidSize')"
          :label="$t('chart.radar_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="1"
            :max="100"
            @change="changeBarSizeCase('liquidSize')"
          />
        </el-form-item>
        <!--liquid-end-->
        <el-form-item
          v-if="showProperty('symbolOpacity')"
          :label="$t('chart.not_alpha')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.symbolOpacity"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="10"
            @change="changeBarSizeCase('symbolOpacity')"
          />
        </el-form-item>

        <el-form-item
          v-if="showProperty('symbolStrokeWidth') && sizeForm.scatterSymbol && sizeForm.scatterSymbol !== 'marker'"
          :label="$t('plugin_style.border')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.symbolStrokeWidth"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="5"
            @change="changeBarSizeCase('symbolStrokeWidth')"
          />
        </el-form-item>
      </el-form>
      <!--flow-map-start-->
      <el-form
        ref="flowMapForm"
        :model="sizeForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          v-show="showProperty('mapPitch')"
          :label="$t('chart.map_pitch')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.mapPitch"
            :min="0"
            :max="90"
            @change="changeBarSizeCase('mapPitch')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('mapLineType')"
          :label="$t('chart.map_line_type')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.mapLineType"
            @change="changeBarSizeCase('mapLineType')"
          >
            <el-option
              v-for="item in lineTypeOptions"
              :key="item.name"
              :label="item.name"
              :value="item.value"
              :disabled="checkMapLineType(item)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('mapLineWidth')"
          :label="$t('chart.map_line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.mapLineWidth"
            :min="1"
            :max="10"
            @change="changeBarSizeCase('mapLineWidth')"
          />
        </el-form-item>
        <el-form-item
          v-show="false"
          :label="$t('chart.map_line_animate')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.mapLineAnimate"
            :disabled="checkMapLineAnimate"
            @change="changeBarSizeCase('mapLineAnimate')"
          />
        </el-form-item>
        <div v-if="sizeForm.mapLineAnimate">
          <el-form-item
            v-show="showProperty('mapLineAnimateDuration')"
            :label="$t('chart.map_line_animate_duration')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateDuration"
              :min="0"
              :max="20"
              @change="changeBarSizeCase('mapLineAnimateDuration')"
            />
          </el-form-item>
          <el-form-item
            v-show="false"
            :label="$t('chart.map_line_animate_interval')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateInterval"
              :min="0"
              :max="1"
              :step="0.1"
              @change="changeBarSizeCase('mapLineAnimateInterval')"
            />
          </el-form-item>
          <el-form-item
            v-show="false"
            :label="$t('chart.map_line_animate_trail_length')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateTrailLength"
              :min="0"
              :max="1"
              :step="0.1"
              @change="changeBarSizeCase('mapLineAnimateTrailLength')"
            />
          </el-form-item>
        </div>
      </el-form>
      <!--flow-map-end-->
    </el-col>
  </div>
</template>

<script>
import { CHART_FONT_FAMILY, CHART_FONT_LETTER_SPACE, DEFAULT_SIZE } from '../../chart/chart'
import { equalsAny } from '@/utils/StringUtils'

export default {
  name: 'SizeSelectorAntV',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    },
    quotaFields: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      lineSymbolOptions: [
        // { name: this.$t('chart.line_symbol_none'), value: 'none' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'square' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'diamond' }
      ],
      liquidShapeOptions: [
        { name: this.$t('chart.liquid_shape_circle'), value: 'circle' },
        { name: this.$t('chart.liquid_shape_diamond'), value: 'diamond' },
        { name: this.$t('chart.liquid_shape_triangle'), value: 'triangle' },
        { name: this.$t('chart.liquid_shape_pin'), value: 'pin' },
        { name: this.$t('chart.liquid_shape_rect'), value: 'rect' }
      ],
      pageSizeOptions: [
        { name: '10' + this.$t('chart.table_page_size_unit'), value: '10' },
        { name: '20' + this.$t('chart.table_page_size_unit'), value: '20' },
        { name: '50' + this.$t('chart.table_page_size_unit'), value: '50' },
        { name: '100' + this.$t('chart.table_page_size_unit'), value: '100' }
      ],
      fontSize: [],
      alignOptions: [
        { name: this.$t('chart.table_align_left'), value: 'left' },
        { name: this.$t('chart.table_align_center'), value: 'center' },
        { name: this.$t('chart.table_align_right'), value: 'right' }
      ],
      fontFamily: CHART_FONT_FAMILY,
      fontLetterSpace: CHART_FONT_LETTER_SPACE,
      minField: {},
      maxField: {},
      liquidMaxField: {},
      quotaData: [],
      lineTypeOptions: [
        { name: this.$t('chart.map_line_type_line'), value: 'line' },
        { name: this.$t('chart.map_line_type_arc'), value: 'arc' },
        { name: this.$t('chart.map_line_type_arc_3d'), value: 'arc3d' }
      ]
    }
  },
  computed: {
    validLiquidMaxField() {
      return this.isValidField(this.liquidMaxField)
    },
    validMinField() {
      return this.isValidField(this.minField)
    },
    validMaxField() {
      return this.isValidField(this.maxField)
    },
    checkMapLineAnimate() {
      const chart = this.chart
      if (chart.type === 'flow-map') {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        return customAttr.color.mapLineGradient && equalsAny(this.sizeForm.mapLineType, 'line', 'arc')
      }
      return false
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initField()
        this.initData()
      }
    },
    'quotaFields': function() {
      this.initField()
    }
  },
  mounted() {
    this.initField()
    this.init()
    this.initData()
  },
  methods: {
    initField() {
      this.quotaData = this.quotaFields.filter(ele => !ele.chartId && ele.id !== 'count')
      if (this.sizeForm.gaugeMinField.id) {
        this.minField = this.getQuotaField(this.sizeForm.gaugeMinField.id)
      }
      if (this.sizeForm.gaugeMaxField.id) {
        this.maxField = this.getQuotaField(this.sizeForm.gaugeMaxField.id)
      }
      if (this.sizeForm.liquidMaxField.id) {
        this.liquidMaxField = this.getQuotaField(this.sizeForm.liquidMaxField.id)
      }
    },
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.size) {
          this.sizeForm = customAttr.size
          this.sizeForm.treemapWidth = this.sizeForm.treemapWidth ? this.sizeForm.treemapWidth : 80
          this.sizeForm.treemapHeight = this.sizeForm.treemapHeight ? this.sizeForm.treemapHeight : 80
          this.sizeForm.radarSize = this.sizeForm.radarSize ? this.sizeForm.radarSize : 80

          this.sizeForm.liquidShape = this.sizeForm.liquidShape ? this.sizeForm.liquidShape : DEFAULT_SIZE.liquidShape
          this.sizeForm.liquidMax = this.sizeForm.liquidMax ? this.sizeForm.liquidMax : DEFAULT_SIZE.liquidMax
          this.sizeForm.liquidSize = this.sizeForm.liquidSize ? this.sizeForm.liquidSize : DEFAULT_SIZE.liquidSize
          this.sizeForm.liquidOutlineBorder = this.sizeForm.liquidOutlineBorder ? this.sizeForm.liquidOutlineBorder : DEFAULT_SIZE.liquidOutlineBorder
          this.sizeForm.liquidOutlineDistance = (this.sizeForm.liquidOutlineDistance || this.sizeForm.liquidOutlineDistance === 0) ? this.sizeForm.liquidOutlineDistance : DEFAULT_SIZE.liquidOutlineDistance
          this.sizeForm.liquidWaveLength = this.sizeForm.liquidWaveLength ? this.sizeForm.liquidWaveLength : DEFAULT_SIZE.liquidWaveLength
          this.sizeForm.liquidWaveCount = this.sizeForm.liquidWaveCount ? this.sizeForm.liquidWaveCount : DEFAULT_SIZE.liquidWaveCount
          this.sizeForm.liquidMaxType = this.sizeForm.liquidMaxType ? this.sizeForm.liquidMaxType : DEFAULT_SIZE.liquidMaxType
          if (!this.sizeForm.liquidMaxField) {
            this.sizeForm.liquidMaxField = DEFAULT_SIZE.liquidMaxField
            this.sizeForm.liquidMaxField.id = this.quotaData[0]?.id
            this.sizeForm.liquidMaxField.summary = 'count'
          }

          this.sizeForm.tablePageMode = this.sizeForm.tablePageMode ? this.sizeForm.tablePageMode : DEFAULT_SIZE.tablePageMode
          this.sizeForm.tablePageSize = this.sizeForm.tablePageSize ? this.sizeForm.tablePageSize : DEFAULT_SIZE.tablePageSize

          this.sizeForm.tableColumnMode = this.sizeForm.tableColumnMode ? this.sizeForm.tableColumnMode : DEFAULT_SIZE.tableColumnMode
          this.sizeForm.tableColumnWidth = this.sizeForm.tableColumnWidth ? this.sizeForm.tableColumnWidth : DEFAULT_SIZE.tableColumnWidth

          this.sizeForm.tableHeaderAlign = this.sizeForm.tableHeaderAlign ? this.sizeForm.tableHeaderAlign : DEFAULT_SIZE.tableHeaderAlign
          this.sizeForm.tableItemAlign = this.sizeForm.tableItemAlign ? this.sizeForm.tableItemAlign : DEFAULT_SIZE.tableItemAlign

          this.sizeForm.showIndex = this.sizeForm.showIndex ? this.sizeForm.showIndex : DEFAULT_SIZE.showIndex
          if (this.sizeForm.indexLabel === null || this.sizeForm.indexLabel === undefined) {
            this.sizeForm.indexLabel = DEFAULT_SIZE.indexLabel
          }

          this.sizeForm.gaugeTickCount = this.sizeForm.gaugeTickCount ? this.sizeForm.gaugeTickCount : DEFAULT_SIZE.gaugeTickCount

          this.sizeForm.gaugeMinType = this.sizeForm.gaugeMinType ? this.sizeForm.gaugeMinType : DEFAULT_SIZE.gaugeMinType
          if (!this.sizeForm.gaugeMinField) {
            this.sizeForm.gaugeMinField = DEFAULT_SIZE.gaugeMinField
            this.sizeForm.gaugeMinField.id = this.quotaData[0]?.id
            this.sizeForm.gaugeMinField.summary = 'count'
          }
          this.sizeForm.gaugeMaxType = this.sizeForm.gaugeMaxType ? this.sizeForm.gaugeMaxType : DEFAULT_SIZE.gaugeMaxType
          if (!this.sizeForm.gaugeMaxField) {
            this.sizeForm.gaugeMaxField = DEFAULT_SIZE.gaugeMaxField
            this.sizeForm.gaugeMaxField.id = this.quotaData[0]?.id
            this.sizeForm.gaugeMaxField.summary = 'count'
          }

          this.sizeForm.quotaFontFamily = this.sizeForm.quotaFontFamily ? this.sizeForm.quotaFontFamily : DEFAULT_SIZE.quotaFontFamily
          this.sizeForm.quotaFontIsBolder = this.sizeForm.quotaFontIsBolder ? this.sizeForm.quotaFontIsBolder : DEFAULT_SIZE.quotaFontIsBolder
          this.sizeForm.quotaFontIsItalic = this.sizeForm.quotaFontIsItalic ? this.sizeForm.quotaFontIsItalic : DEFAULT_SIZE.quotaFontIsItalic
          this.sizeForm.quotaLetterSpace = this.sizeForm.quotaLetterSpace ? this.sizeForm.quotaLetterSpace : DEFAULT_SIZE.quotaLetterSpace
          this.sizeForm.quotaFontShadow = this.sizeForm.quotaFontShadow ? this.sizeForm.quotaFontShadow : DEFAULT_SIZE.quotaFontShadow
          this.sizeForm.dimensionFontFamily = this.sizeForm.dimensionFontFamily ? this.sizeForm.dimensionFontFamily : DEFAULT_SIZE.dimensionFontFamily
          this.sizeForm.dimensionFontIsBolder = this.sizeForm.dimensionFontIsBolder ? this.sizeForm.dimensionFontIsBolder : DEFAULT_SIZE.dimensionFontIsBolder
          this.sizeForm.dimensionFontIsItalic = this.sizeForm.dimensionFontIsItalic ? this.sizeForm.dimensionFontIsItalic : DEFAULT_SIZE.dimensionFontIsItalic
          this.sizeForm.dimensionLetterSpace = this.sizeForm.dimensionLetterSpace ? this.sizeForm.dimensionLetterSpace : DEFAULT_SIZE.dimensionLetterSpace
          this.sizeForm.dimensionFontShadow = this.sizeForm.dimensionFontShadow ? this.sizeForm.dimensionFontShadow : DEFAULT_SIZE.dimensionFontShadow

          this.sizeForm.hPosition = this.sizeForm.hPosition ? this.sizeForm.hPosition : DEFAULT_SIZE.hPosition
          this.sizeForm.vPosition = this.sizeForm.vPosition ? this.sizeForm.vPosition : DEFAULT_SIZE.vPosition

          this.sizeForm.mapPitch = this.sizeForm.mapPitch ? this.sizeForm.mapPitch : DEFAULT_SIZE.mapPitch
          this.sizeForm.mapLineType = this.sizeForm.mapLineType ? this.sizeForm.mapLineType : DEFAULT_SIZE.mapLineType
          this.sizeForm.mapLineWidth = this.sizeForm.mapLineWidth ? this.sizeForm.mapLineWidth : DEFAULT_SIZE.mapLineWidth
          this.sizeForm.mapLineAnimate = this.sizeForm.mapLineAnimate !== undefined ? this.sizeForm.mapLineAnimate : DEFAULT_SIZE.mapLineAnimate
          this.sizeForm.mapLineAnimateDuration = this.sizeForm.mapLineAnimateDuration !== undefined ? this.sizeForm.mapLineAnimateDuration : DEFAULT_SIZE.mapLineAnimateDuration
          this.sizeForm.mapLineAnimateInterval = this.sizeForm.mapLineAnimateInterval !== undefined ? this.sizeForm.mapLineAnimateInterval : DEFAULT_SIZE.mapLineAnimateInterval
          this.sizeForm.mapLineAnimateTrailLength = this.sizeForm.mapLineAnimateTrailLength !== undefined ? this.sizeForm.mapLineAnimateTrailLength : DEFAULT_SIZE.mapLineAnimateTrailLength
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeBarSizeCase(modifyName) {
      this.sizeForm['modifyName'] = modifyName
      if (this.sizeForm.gaugeMax <= this.sizeForm.gaugeMin) {
        this.$message.error(this.$t('chart.max_more_than_mix'))
        return
      }
      this.$emit('onSizeChange', this.sizeForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    },

    changeQuotaField(type, resetSummary) {
      if (type === 'min') {
        if (this.sizeForm.gaugeMinType === 'dynamic') {
          if (!this.sizeForm.gaugeMinField.id) {
            this.sizeForm.gaugeMinField.id = this.quotaData[0]?.id
          }
          if (!this.sizeForm.gaugeMinField.summary) {
            this.sizeForm.gaugeMinField.summary = 'count'
          }
          if (resetSummary) {
            this.sizeForm.gaugeMinField.summary = 'count'
          }
          if (this.sizeForm.gaugeMinField.id && this.sizeForm.gaugeMinField.summary) {
            this.minField = this.getQuotaField(this.sizeForm.gaugeMinField.id)
            this.changeBarSizeCase('gaugeMinField')
          }
        } else {
          if (this.sizeForm.gaugeMaxType === 'dynamic') {
            if (this.sizeForm.gaugeMaxField.id && this.sizeForm.gaugeMaxField.summary) {
              this.changeBarSizeCase('gaugeMinField')
            }
          } else {
            this.changeBarSizeCase('gaugeMinField')
          }
        }
      } else if (type === 'max') {
        if (this.chart.type === 'liquid') {
          if (!this.sizeForm.liquidMaxField.id) {
            this.sizeForm.liquidMaxField.id = this.quotaData[0]?.id
          }
          if (!this.sizeForm.liquidMaxField.summary) {
            this.sizeForm.liquidMaxField.summary = 'count'
          }
          if (resetSummary) {
            this.sizeForm.liquidMaxField.summary = 'count'
          }
          if (this.sizeForm.liquidMaxField.id && this.sizeForm.liquidMaxField.summary) {
            this.maxField = this.getQuotaField(this.sizeForm.liquidMaxField.id)
            this.changeBarSizeCase('liquidMaxField')
          }
        } else {
          if (this.sizeForm.gaugeMaxType === 'dynamic') {
            if (!this.sizeForm.gaugeMaxField.id) {
              this.sizeForm.gaugeMaxField.id = this.quotaData[0]?.id
            }
            if (!this.sizeForm.gaugeMaxField.summary) {
              this.sizeForm.gaugeMaxField.summary = 'count'
            }
            if (resetSummary) {
              this.sizeForm.gaugeMaxField.summary = 'count'
            }
            if (this.sizeForm.gaugeMaxField.id && this.sizeForm.gaugeMaxField.summary) {
              this.maxField = this.getQuotaField(this.sizeForm.gaugeMaxField.id)
              this.changeBarSizeCase('gaugeMaxField')
            }
          } else {
            if (this.sizeForm.gaugeMinType === 'dynamic') {
              if (this.sizeForm.gaugeMinField.id && this.sizeForm.gaugeMinField.summary) {
                this.changeBarSizeCase('gaugeMaxField')
              }
            } else {
              this.changeBarSizeCase('gaugeMaxField')
            }
          }
        }
      }
    },
    getQuotaField(id) {
      if (!id) {
        return {}
      }
      const fields = this.quotaData.filter(ele => {
        return ele.id === id
      })
      if (fields.length === 0) {
        return {}
      } else {
        return fields[0]
      }
    },
    isValidField(field) {
      return field.id !== 'count' &&
        field.deType !== 0 &&
        field.deType !== 1 &&
        field.deType !== 5
    },
    checkMapLineType(item) {
      const chart = this.chart
      if (chart.type === 'flow-map') {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.color.mapLineGradient && customAttr.size.mapLineAnimate) {
          return equalsAny(item.value, 'line', 'arc')
        }
      }
      return false
    }
  }
}
</script>

<style scoped>
  .shape-item {
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .form-item-slider ::v-deep .el-form-item__label {
    font-size: 12px;
    line-height: 38px;
  }

  .form-item ::v-deep .el-form-item__label {
    font-size: 12px;
  }

  .el-select-dropdown__item {
    padding: 0 20px;
  }

  span {
    font-size: 12px
  }

  .el-form-item {
    margin-bottom: 6px;
  }

  .el-divider--horizontal {
    margin: 10px 0
  }

  .divider-style ::v-deep .el-divider__text {
    color: #606266;
    font-size: 12px;
    font-weight: 400;
    padding: 0 10px;
  }
  .form-flex >>> .el-form-item__content {
    display: flex;
  }
</style>
