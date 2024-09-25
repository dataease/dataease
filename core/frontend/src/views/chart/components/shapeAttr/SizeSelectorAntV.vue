<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="sizeFormBar"
        :model="sizeForm"
        label-width="84px"
        size="mini"
      >
        <!--bar-begin-->
        <el-form-item
          v-if="showProperty('barDefault')"
          :label="$t('chart.adapt')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.barDefault"
            @change="changeBarSizeCase('barDefault')"
          >{{ $t('chart.adapt') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          v-if="showProperty('barGap')"
          :label="$t('chart.bar_gap')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.barGap"
            :disabled="sizeForm.barDefault"
            :max="5"
            :min="0"
            :show-input-controls="false"
            :step="0.1"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('barGap')"
          />
        </el-form-item>
        <!--bar-end-->
        <!--line-begin-->
        <el-form-item
          v-if="showProperty('lineWidth')"
          :label="$t('chart.line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineWidth"
            :max="10"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('lineWidth')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('lineSymbol')"
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
          v-if="showProperty('lineSymbolSize')"
          :label="$t('chart.line_symbol_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineSymbolSize"
            :max="20"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('lineSymbolSize')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('lineSmooth')"
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
          v-if="showProperty('pieInnerRadius')"
          :label="$t('chart.pie_inner_radius_percent')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.pieInnerRadius"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('pieInnerRadius')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('pieOuterRadius')"
          :label="$t('chart.pie_outer_radius_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.pieOuterRadius"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('pieOuterRadius')"
          />
        </el-form-item>
        <!--pie-end-->
        <!--radar-begin-->
        <el-form-item
          v-if="showProperty('radarShape')"
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
          v-if="showProperty('radarSize')"
          :label="$t('chart.radar_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.radarSize"
            :max="100"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('radarSize')"
          />
        </el-form-item>
        <!--radar-end-->
        <!--table-begin-->
        <el-form-item
          v-if="showProperty('tableLayoutMode')"
          :label="$t('chart.table_layout_mode')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.tableLayoutMode"
            class="column-radio"
            @change="changeBarSizeCase('tableLayoutMode')"
          >
            <el-radio label="grid"><span>{{ $t('chart.table_layout_grid') }}</span></el-radio>
            <el-radio label="tree">
              <span>{{ $t('chart.table_layout_tree') }}</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="showProperty('tableItemFontSize')"
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
          v-if="showProperty('tableItemAlign')"
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
          v-if="showProperty('tableItemHeight')"
          :label="$t('chart.table_item_height')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableItemHeight"
            :max="100"
            :min="20"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('tableItemHeight')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('tablePageMode')"
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
          v-if="showProperty('tablePageSize')&&sizeForm.tablePageMode === 'page'"
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
          v-if="showProperty('tableColumnMode')"
          :label="$t('chart.table_column_width_config')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.tableColumnMode"
            class="column-radio"
            @change="changeBarSizeCase('tableColumnMode')"
          >
            <el-radio label="adapt"><span>{{ $t('chart.table_column_adapt') }}</span></el-radio>
            <!--此处为了兼容原有的配置，原先的自定义效果实际为固定列宽，后续添加的按列配置才是实际的自定义列宽。-->
            <el-radio label="custom">
              <span>{{ $t('chart.table_column_fixed') }}</span>
            </el-radio>
            <el-radio
              v-if="equalsAny(chart.type, 'table-info', 'table-normal')"
              label="field"
            >
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
          v-if="showProperty('tableColumnMode') && sizeForm.tableColumnMode === 'custom'"
          class="form-item form-item-slider"
          label=""
        >
          <el-slider
            v-model="sizeForm.tableColumnWidth"
            :max="500"
            :min="10"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('tableColumnWidth')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('tableColumnMode') && sizeForm.tableColumnMode === 'field'"
          class="form-item"
          label=""
        >
          <el-row>
            <el-col :span="10">
              <el-select
                v-model="fieldColumnWidth.fieldId"
                input-size="mini"
                @change="changeFieldColumn()"
              >
                <el-option
                  v-for="item in sizeForm.tableFieldWidth"
                  :key="item.fieldId"
                  :label="item.name"
                  :value="item.fieldId"
                />
              </el-select>
            </el-col>
            <el-col
              :offset="2"
              :span="12"
            >
              <el-input
                v-model.number="fieldColumnWidth.width"
                :max="100"
                :min="0"
                type="number"
                @change="changeFieldColumnWidth()"
              >
                <template #append>%</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item
          v-if="showProperty('tableFreeze')"
          :label="$t('chart.table_freeze')"
          class="form-item"
        >
          <span>{{ $t('dynamic_time.before') }} </span>
          <el-input-number
            v-model="sizeForm.tableColumnFreezeHead"
            :max="100"
            :min="0"
            :step-strictly="true"
            @change="changeBarSizeCase('tableColumnFreezeHead')"
          />
          <span> {{ $t('chart.column') }}</span>
          <div style="margin: 5px 0" />
          <span>{{ $t('dynamic_time.before') }} </span>
          <el-input-number
            v-model="sizeForm.tableRowFreezeHead"
            :max="1000"
            :min="0"
            :step-strictly="true"
            @change="changeBarSizeCase('tableRowFreezeHead')"
          />
          <span> {{ $t('deDataset.row') }}</span>
        </el-form-item>
        <el-form-item
          v-if="showProperty('showIndex')"
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
          v-if="showProperty('tableCellTooltip')"
          :label="$t('chart.table_cell_tooltip')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.tableCellTooltip.show"
            @change="changeBarSizeCase('tableCellTooltip')"
          />
        </el-form-item>

        <el-form-item
          v-if="showProperty('indexLabel') && sizeForm.showIndex"
          :label="$t('chart.table_index_desc')"
          class="form-item"
        >
          <el-input
            v-model="sizeForm.indexLabel"
            type="text"
            @blur="changeBarSizeCase('indexLabel')"
          />
        </el-form-item>
        <el-divider v-if="equalsAny(chart.type, 'table-info', 'table-normal')" />
        <el-form-item
          v-if="showProperty('showTableHeader')"
          :label="$t('chart.table_show_table_header')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.showTableHeader"
            @change="changeBarSizeCase('showTableHeader')"
          >
            <el-radio :label="true">{{ $t('commons.yes') }}</el-radio>
            <el-radio :label="false">{{ $t('commons.no') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="(showProperty('showTableHeader') && sizeForm.showTableHeader) || chart.type === 'table-pivot'">
          <el-form-item
            v-if="showProperty('tableHeaderSort')"
            :label="$t('chart.table_header_sort')"
            class="form-item"
          >
            <el-radio-group
              v-model="sizeForm.tableHeaderSort"
              @change="changeBarSizeCase('tableHeaderSort')"
            >
              <el-radio :label="true">{{ $t('commons.yes') }}</el-radio>
              <el-radio :label="false">{{ $t('commons.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="showProperty('tableTitleFontSize')"
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
            v-if="showProperty('tableHeaderAlign')"
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
            v-if="showProperty('tableTitleHeight')"
            :label="$t('chart.table_title_height')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.tableTitleHeight"
              :max="100"
              :min="20"
              :show-input-controls="false"
              input-size="mini"
              show-input
              @change="changeBarSizeCase('tableTitleHeight')"
            />
          </el-form-item>
          <el-form-item
            v-if="showProperty('tableRowTooltip')"
            :label="$t('chart.table_row_tooltip')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.tableRowTooltip.show"
              @change="changeBarSizeCase('tableRowTooltip')"
            />
          </el-form-item>
          <el-form-item
            v-if="showProperty('tableColTooltip')"
            :label="$t('chart.table_col_tooltip')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.tableColTooltip.show"
              @change="changeBarSizeCase('tableColTooltip')"
            />
          </el-form-item>
          <el-form-item
            v-if="showProperty('showSummary')"
            :label="$t('chart.show_summary')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.showSummary"
              @change="changeBarSizeCase('showSummary')"
            />
          </el-form-item>
          <el-form-item
            v-if="showProperty('summaryLabel') && sizeForm.showSummary"
            :label="$t('chart.summary_label')"
            class="form-item"
          >
            <el-input
              v-model="sizeForm.summaryLabel"
              type="text"
              :max-length="10"
              @blur="changeBarSizeCase('summaryLabel')"
            />
          </el-form-item>
        </div>
        <!--table-end-->
        <!--chart-mix-start-->
        <span v-if="showProperty('mix')">
          <el-divider
            class="divider-style"
            content-position="center"
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
              :max="5"
              :min="0"
              :show-input-controls="false"
              :step="0.1"
              input-size="mini"
              show-input
              @change="changeBarSizeCase('barGap')"
            />
          </el-form-item>
          <el-divider
            class="divider-style"
            content-position="center"
          >{{ $t('chart.chart_line') }}</el-divider>
          <el-form-item
            :label="$t('chart.line_width')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.lineWidth"
              :max="10"
              :min="0"
              :show-input-controls="false"
              input-size="mini"
              show-input
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
              :max="20"
              :min="0"
              :show-input-controls="false"
              input-size="mini"
              show-input
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
            class="divider-style"
            content-position="center"
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
              :max="40"
              :min="1"
              :show-input-controls="false"
              input-size="mini"
              show-input
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
        label-width="80px"
        size="mini"
      >
        <div v-if="!batchOptStatus">
          <el-form-item
            v-if="showProperty('gaugeMin')"
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
                    class="field-icon-text"
                    icon-class="field_text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    class="field-icon-time"
                    icon-class="field_time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    class="field-icon-value"
                    icon-class="field_value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    class="field-icon-location"
                    icon-class="field_location"
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
                :label="$t('chart.sum')"
                value="sum"
              />
              <el-option
                v-if="validMinField"
                key="avg"
                :label="$t('chart.avg')"
                value="avg"
              />
              <el-option
                v-if="validMinField"
                key="max"
                :label="$t('chart.max')"
                value="max"
              />
              <el-option
                v-if="validMinField"
                key="min"
                :label="$t('chart.min')"
                value="min"
              />
              <el-option
                v-if="validMinField"
                key="stddev_pop"
                :label="$t('chart.stddev_pop')"
                value="stddev_pop"
              />
              <el-option
                v-if="validMinField"
                key="var_pop"
                :label="$t('chart.var_pop')"
                value="var_pop"
              />
              <el-option
                key="count"
                :label="$t('chart.count')"
                value="count"
              />
              <el-option
                v-if="minField.id !== 'count'"
                key="count_distinct"
                :label="$t('chart.count_distinct')"
                value="count_distinct"
              />
            </el-select>
          </el-form-item>

          <el-form-item
            v-if="showProperty('gaugeMax')"
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
                    class="field-icon-text"
                    icon-class="field_text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    class="field-icon-time"
                    icon-class="field_time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    class="field-icon-value"
                    icon-class="field_value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    class="field-icon-location"
                    icon-class="field_location"
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
                :label="$t('chart.sum')"
                value="sum"
              />
              <el-option
                v-if="validMaxField"
                key="avg"
                :label="$t('chart.avg')"
                value="avg"
              />
              <el-option
                v-if="validMaxField"
                key="max"
                :label="$t('chart.max')"
                value="max"
              />
              <el-option
                v-if="validMaxField"
                key="min"
                :label="$t('chart.min')"
                value="min"
              />
              <el-option
                v-if="validMaxField"
                key="stddev_pop"
                :label="$t('chart.stddev_pop')"
                value="stddev_pop"
              />
              <el-option
                v-if="validMaxField"
                key="var_pop"
                :label="$t('chart.var_pop')"
                value="var_pop"
              />
              <el-option
                key="count"
                :label="$t('chart.count')"
                value="count"
              />
              <el-option
                v-if="maxField.id !== 'count'"
                key="count_distinct"
                :label="$t('chart.count_distinct')"
                value="count_distinct"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item
          v-if="showProperty('gaugeStartAngle')"
          :label="$t('chart.start_angle')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.gaugeStartAngle"
            :max="360"
            :min="-360"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('gaugeStartAngle')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeEndAngle')"
          :label="$t('chart.end_angle')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.gaugeEndAngle"
            :max="360"
            :min="-360"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('gaugeEndAngle')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugeAxisLine')"
          :label="$t('chart.gauge_axis_label')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.gaugeAxisLine"
            @change="changeBarSizeCase('gaugeAxisLine')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('gaugePercentLabel') && sizeForm.gaugeAxisLine"
          :label="$t('chart.gauge_percentage_tick')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.gaugePercentLabel"
            @change="changeBarSizeCase('gaugePercentLabel')"
          />
        </el-form-item>
        <!--        <el-form-item v-if="showProperty('gaugeTickCount')" :label="$t('chart.tick_count')" class="form-item form-item-slider">-->
        <!--          <el-input-number v-model="sizeForm.gaugeTickCount" :min="1" :step="1" :precision="0" size="mini" @change="changeBarSizeCase('gaugeTickCount')" />-->
        <!--        </el-form-item>-->
      </el-form>
      <!--gauge-end-->

      <el-form
        ref="sizeFormPie"
        :model="sizeForm"
        label-width="80px"
        size="mini"
      >
        <!--text&label-start-->
        <el-form-item
          v-if="showProperty('quotaFontSize')"
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
          v-if="showProperty('quotaFontFamily')"
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
          v-if="showProperty('quotaFontStyle')"
          :label="$t('chart.quota_text_style')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaFontIsItalic"
            @change="changeBarSizeCase('quotaFontIsItalic')"
          >{{ $t('chart.italic') }}
          </el-checkbox>
          <el-checkbox
            v-model="sizeForm.quotaFontIsBolder"
            @change="changeBarSizeCase('quotaFontIsBolder')"
          >{{ $t('chart.bolder') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          v-if="showProperty('quotaLetterSpace')"
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
          v-if="showProperty('quotaFontShadow')"
          :label="$t('chart.font_shadow')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaFontShadow"
            @change="changeBarSizeCase('quotaFontShadow')"
          >{{ $t('chart.font_shadow') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          v-if="showProperty('hPosition')"
          :label="$t('chart.h_position')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.hPosition"
            :placeholder="$t('chart.h_position')"
            @change="changeBarSizeCase('hPosition')"
          >
            <el-option
              :label="$t('chart.p_left')"
              value="start"
            >{{ $t('chart.p_left') }}
            </el-option>
            <el-option
              :label="$t('chart.p_center')"
              value="center"
            >{{ $t('chart.p_center') }}
            </el-option>
            <el-option
              :label="$t('chart.p_right')"
              value="end"
            >{{ $t('chart.p_right') }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="showProperty('vPosition')"
          :label="$t('chart.v_position')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.vPosition"
            :placeholder="$t('chart.v_position')"
            @change="changeBarSizeCase('vPosition')"
          >
            <el-option
              :label="$t('chart.p_top')"
              value="start"
            >{{ $t('chart.p_top') }}
            </el-option>
            <el-option
              :label="$t('chart.p_center')"
              value="center"
            >{{ $t('chart.p_center') }}
            </el-option>
            <el-option
              :label="$t('chart.p_bottom')"
              value="end"
            >{{ $t('chart.p_bottom') }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-divider v-if="showProperty('quotaSuffix')" />
        <el-form-item
          v-if="showProperty('quotaSuffix')"
          :label="$t('chart.quota_suffix')"
          class="form-item"
        >
          <el-input
            v-model="sizeForm.quotaSuffix"
            @blur="changeBarSizeCase('quotaSuffix')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('quotaSuffixFontSize')"
          :label="$t('chart.quota_suffix_font_size')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaSuffixFontSize"
            :placeholder="$t('chart.quota_suffix_font_size')"
            @change="changeBarSizeCase('quotaSuffixFontSize')"
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
          v-if="showProperty('quotaSuffixFontFamily')"
          :label="$t('chart.quota_suffix_font_family')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaSuffixFontFamily"
            :placeholder="$t('chart.quota_suffix_font_family')"
            @change="changeBarSizeCase('quotaSuffixFontFamily')"
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
          v-if="showProperty('quotaSuffixFontStyle')"
          :label="$t('chart.quota_suffix_text_style')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaSuffixFontIsItalic"
            @change="changeBarSizeCase('quotaSuffixFontIsItalic')"
          >{{ $t('chart.italic') }}
          </el-checkbox>
          <el-checkbox
            v-model="sizeForm.quotaSuffixFontIsBolder"
            @change="changeBarSizeCase('quotaSuffixFontIsBolder')"
          >{{ $t('chart.bolder') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          v-if="showProperty('quotaSuffixLetterSpace')"
          :label="$t('chart.quota_suffix_letter_space')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.quotaSuffixLetterSpace"
            :placeholder="$t('chart.quota_suffix_letter_space')"
            @change="changeBarSizeCase('quotaSuffixLetterSpace')"
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
          v-if="showProperty('quotaSuffixFontShadow')"
          :label="$t('chart.font_shadow')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.quotaSuffixFontShadow"
            @change="changeBarSizeCase('quotaSuffixFontShadow')"
          >{{ $t('chart.font_shadow') }}
          </el-checkbox>
        </el-form-item>
        <el-divider v-if="showProperty('dimensionShow')" />
        <el-form-item
          v-if="showProperty('dimensionShow')"
          :label="$t('chart.dimension_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.dimensionShow"
            @change="changeBarSizeCase('dimensionShow')"
          >{{ $t('chart.show') }}
          </el-checkbox>
        </el-form-item>
        <div v-if="sizeForm.dimensionShow">
          <el-form-item
            v-if="showProperty('dimensionFontSize')"
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
            v-if="showProperty('dimensionFontFamily')"
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
            v-if="showProperty('dimensionFontStyle')"
            :label="$t('chart.dimension_text_style')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.dimensionFontIsItalic"
              @change="changeBarSizeCase('dimensionFontIsItalic')"
            >{{ $t('chart.italic') }}
            </el-checkbox>
            <el-checkbox
              v-model="sizeForm.dimensionFontIsBolder"
              @change="changeBarSizeCase('dimensionFontIsBolder')"
            >{{ $t('chart.bolder') }}
            </el-checkbox>
          </el-form-item>
          <el-form-item
            v-if="showProperty('dimensionLetterSpace')"
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
            v-if="showProperty('dimensionFontShadow')"
            :label="$t('chart.font_shadow')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.dimensionFontShadow"
              @change="changeBarSizeCase('dimensionFontShadow')"
            >{{ $t('chart.font_shadow') }}
            </el-checkbox>
          </el-form-item>
          <el-form-item
            v-if="showProperty('spaceSplit')"
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
        </div>
        <!--text&label-end-->
        <!--scatter-begin-->
        <el-form-item
          v-if="showProperty('scatterSymbol')"
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
          v-if="showProperty('scatterSymbolSize')"
          :label="$t('chart.bubble_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.scatterSymbolSize"
            :max="40"
            :min="1"
            :show-input-controls="false"
            input-size="mini"
            show-input
            @change="changeBarSizeCase('scatterSymbolSize')"
          />
        </el-form-item>
        <!--scatter-end-->
        <!--liquid-begin-->
        <el-form-item
          v-if="showProperty('liquidShape')"
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
          v-if="showProperty('liquidMax')"
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
                  class="field-icon-text"
                  icon-class="field_text"
                />
                <svg-icon
                  v-if="item.deType === 1"
                  class="field-icon-time"
                  icon-class="field_time"
                />
                <svg-icon
                  v-if="item.deType === 2 || item.deType === 3"
                  class="field-icon-value"
                  icon-class="field_value"
                />
                <svg-icon
                  v-if="item.deType === 5"
                  class="field-icon-location"
                  icon-class="field_location"
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
              :label="$t('chart.sum')"
              value="sum"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="avg"
              :label="$t('chart.avg')"
              value="avg"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="max"
              :label="$t('chart.max')"
              value="max"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="min"
              :label="$t('chart.min')"
              value="min"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="stddev_pop"
              :label="$t('chart.stddev_pop')"
              value="stddev_pop"
            />
            <el-option
              v-if="validLiquidMaxField"
              key="var_pop"
              :label="$t('chart.var_pop')"
              value="var_pop"
            />
            <el-option
              key="count"
              :label="$t('chart.count')"
              value="count"
            />
            <el-option
              v-if="liquidMaxField.id !== 'count'"
              key="count_distinct"
              :label="$t('chart.count_distinct')"
              value="count_distinct"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="showProperty('liquidSize')"
          :label="$t('chart.radar_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidSize"
            :max="100"
            :min="1"
            :show-input-controls="false"
            input-size="mini"
            show-input
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
            :max="10"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
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
            :max="5"
            :min="0"
            :show-input-controls="false"
            input-size="mini"
            show-input
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
          v-if="showProperty('mapPitch')"
          :label="$t('chart.map_pitch')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.mapPitch"
            :max="90"
            :min="0"
            @change="changeBarSizeCase('mapPitch')"
          />
        </el-form-item>
        <el-form-item
          v-if="showProperty('mapLineType')"
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
              :disabled="checkMapLineType(item)"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="showProperty('mapLineWidth')"
          :label="$t('chart.map_line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.mapLineWidth"
            :max="10"
            :min="1"
            @change="changeBarSizeCase('mapLineWidth')"
          />
        </el-form-item>
        <el-form-item
          v-if="false"
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
            v-if="showProperty('mapLineAnimateDuration')"
            :label="$t('chart.map_line_animate_duration')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateDuration"
              :max="20"
              :min="0"
              @change="changeBarSizeCase('mapLineAnimateDuration')"
            />
          </el-form-item>
          <el-form-item
            v-if="false"
            :label="$t('chart.map_line_animate_interval')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateInterval"
              :max="1"
              :min="0"
              :step="0.1"
              @change="changeBarSizeCase('mapLineAnimateInterval')"
            />
          </el-form-item>
          <el-form-item
            v-if="false"
            :label="$t('chart.map_line_animate_trail_length')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.mapLineAnimateTrailLength"
              :max="1"
              :min="0"
              :step="0.1"
              @change="changeBarSizeCase('mapLineAnimateTrailLength')"
            />
          </el-form-item>
          <!-- word-cloud start -->
          <el-form-item
            v-if="showProperty('wordSizeRange') "
            :label="$t('chart.word_size_range')"
            class="form-item form-item-slider form-item-range-slider"
          >
            <el-slider
              v-model="sizeForm.wordSizeRange"
              :max="100"
              :min="1"
              range
              @change="changeBarSizeCase('wordSizeRange')"
            />
          </el-form-item>
          <el-form-item
            v-if="showProperty('wordSpacing') "
            :label="$t('chart.word_spacing')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.wordSpacing"
              :max="20"
              :min="0"
              :show-input-controls="false"
              input-size="mini"
              show-input
              @change="changeBarSizeCase('wordSpacing')"
            />
          </el-form-item>
          <!-- word-cloud end -->
        </div>
      </el-form>
      <!--flow-map-end-->
    </el-col>
  </div>
</template>

<script>
import { CHART_FONT_FAMILY, CHART_FONT_LETTER_SPACE, DEFAULT_SIZE } from '../../chart/chart'
import { equalsAny, includesAny } from '@/utils/StringUtils'
import { mapState } from 'vuex'
import { SERIES_NUMBER_FIELD } from '@antv/s2'
import _ from 'lodash'

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
      ],
      fieldColumnWidth: {
        fieldId: '',
        width: 10
      }
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
    },
    ...mapState(['batchOptStatus'])
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
    equalsAny,
    includesAny,
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
          this.sizeForm.tableRowTooltip = this.sizeForm.tableRowTooltip ?? DEFAULT_SIZE.tableRowTooltip
          this.sizeForm.tableColTooltip = this.sizeForm.tableColTooltip ?? DEFAULT_SIZE.tableColTooltip
          this.sizeForm.tableCellTooltip = this.sizeForm.tableCellTooltip ?? DEFAULT_SIZE.tableCellTooltip
          this.sizeForm.tableColumnFreezeHead = this.sizeForm.tableColumnFreezeHead ?? DEFAULT_SIZE.tableColumnFreezeHead
          this.sizeForm.tableColumnFreezeTail = this.sizeForm.tableColumnFreezeTail ?? DEFAULT_SIZE.tableColumnFreezeTail
          this.sizeForm.tableRowFreezeHead = this.sizeForm.tableRowFreezeHead ?? DEFAULT_SIZE.tableRowFreezeHead
          this.sizeForm.summaryLabel = this.sizeForm.summaryLabel ?? DEFAULT_SIZE.summaryLabel
          this.sizeForm.tableLayoutMode = this.sizeForm.tableLayoutMode ?? DEFAULT_SIZE.tableLayoutMode

          this.sizeForm.showIndex = this.sizeForm.showIndex ? this.sizeForm.showIndex : DEFAULT_SIZE.showIndex
          this.sizeForm.showTableHeader = this.sizeForm.showTableHeader !== false
          this.sizeForm.tableHeaderSort = this.sizeForm.tableHeaderSort !== false
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

          this.sizeForm.quotaSuffixFontSize = this.sizeForm.quotaSuffixFontSize ?? DEFAULT_SIZE.quotaSuffixFontSize
          this.sizeForm.quotaSuffixFontFamily = this.sizeForm.quotaSuffixFontFamily ? this.sizeForm.quotaSuffixFontFamily : DEFAULT_SIZE.quotaSuffixFontFamily
          this.sizeForm.quotaSuffixFontIsBolder = this.sizeForm.quotaSuffixFontIsBolder ? this.sizeForm.quotaSuffixFontIsBolder : DEFAULT_SIZE.quotaSuffixFontIsBolder
          this.sizeForm.quotaSuffixFontIsItalic = this.sizeForm.quotaSuffixFontIsItalic ? this.sizeForm.quotaSuffixFontIsItalic : DEFAULT_SIZE.quotaSuffixFontIsItalic
          this.sizeForm.quotaSuffixLetterSpace = this.sizeForm.quotaSuffixLetterSpace ? this.sizeForm.quotaSuffixLetterSpace : DEFAULT_SIZE.quotaSuffixLetterSpace
          this.sizeForm.quotaSuffixFontShadow = this.sizeForm.quotaSuffixFontShadow ? this.sizeForm.quotaSuffixFontShadow : DEFAULT_SIZE.quotaSuffixFontShadow

          this.sizeForm.hPosition = this.sizeForm.hPosition ? this.sizeForm.hPosition : DEFAULT_SIZE.hPosition
          this.sizeForm.vPosition = this.sizeForm.vPosition ? this.sizeForm.vPosition : DEFAULT_SIZE.vPosition

          this.sizeForm.mapPitch = this.sizeForm.mapPitch ? this.sizeForm.mapPitch : DEFAULT_SIZE.mapPitch
          this.sizeForm.mapLineType = this.sizeForm.mapLineType ? this.sizeForm.mapLineType : DEFAULT_SIZE.mapLineType
          this.sizeForm.mapLineWidth = this.sizeForm.mapLineWidth ? this.sizeForm.mapLineWidth : DEFAULT_SIZE.mapLineWidth
          this.sizeForm.mapLineAnimate = this.sizeForm.mapLineAnimate !== undefined ? this.sizeForm.mapLineAnimate : DEFAULT_SIZE.mapLineAnimate
          this.sizeForm.mapLineAnimateDuration = this.sizeForm.mapLineAnimateDuration !== undefined ? this.sizeForm.mapLineAnimateDuration : DEFAULT_SIZE.mapLineAnimateDuration
          this.sizeForm.mapLineAnimateInterval = this.sizeForm.mapLineAnimateInterval !== undefined ? this.sizeForm.mapLineAnimateInterval : DEFAULT_SIZE.mapLineAnimateInterval
          this.sizeForm.mapLineAnimateTrailLength = this.sizeForm.mapLineAnimateTrailLength !== undefined ? this.sizeForm.mapLineAnimateTrailLength : DEFAULT_SIZE.mapLineAnimateTrailLength
          if (this.sizeForm.gaugeAxisLine === null || this.sizeForm.gaugeAxisLine === undefined) {
            this.sizeForm.gaugeAxisLine = DEFAULT_SIZE.gaugeAxisLine
          }
          this.sizeForm.wordSizeRange = this.sizeForm.wordSizeRange ?? DEFAULT_SIZE.wordSizeRange
          this.sizeForm.wordSpacing = this.sizeForm.wordSpacing ?? DEFAULT_SIZE.wordSpacing
          this.sizeForm.gaugePercentLabel = this.sizeForm.gaugePercentLabel === false ? false : DEFAULT_SIZE.gaugePercentLabel
          if (this.chart.type !== 'table-pivot') {
            let { xaxis, yaxis } = this.chart
            if (!(xaxis instanceof Object) && xaxis) {
              xaxis = JSON.parse(xaxis)
            }
            if (!(yaxis instanceof Object) && yaxis) {
              yaxis = JSON.parse(yaxis)
            }
            let allAxis = xaxis
            if (this.chart.type === 'table-normal') {
              allAxis = allAxis.concat(yaxis)
            }
            if (allAxis?.length && this.sizeForm.showIndex) {
              allAxis.unshift({
                dataeaseName: SERIES_NUMBER_FIELD,
                name: this.sizeForm.indexLabel
              })
            }
            if (!allAxis?.length) {
              this.sizeForm.tableFieldWidth?.splice(0)
              this.fieldColumnWidth.fieldId = ''
              this.fieldColumnWidth.width = ''
            } else {
              if (!this.sizeForm.tableFieldWidth?.length) {
                this.sizeForm.tableFieldWidth = []
                const defaultWidth = parseFloat((100 / allAxis.length).toFixed(2))
                allAxis.forEach(item => {
                  this.sizeForm.tableFieldWidth.push({
                    fieldId: item.dataeaseName,
                    name: item.name,
                    width: defaultWidth
                  })
                })
              } else {
                const fieldMap = this.sizeForm.tableFieldWidth.reduce((p, n) => {
                  p[n.fieldId] = n
                  return p
                }, {})
                this.sizeForm.tableFieldWidth.splice(0)
                allAxis.forEach(item => {
                  let width = 10
                  if (fieldMap[item.dataeaseName]) {
                    width = fieldMap[item.dataeaseName].width
                  }
                  this.sizeForm.tableFieldWidth.push({
                    fieldId: item.dataeaseName,
                    name: item.name,
                    width
                  })
                })
              }
              let selectedField = this.sizeForm.tableFieldWidth[0]
              const curFieldIndex = this.sizeForm.tableFieldWidth.findIndex(i => i.fieldId === this.fieldColumnWidth.fieldId)
              if (curFieldIndex !== -1) {
                selectedField = this.sizeForm.tableFieldWidth[curFieldIndex]
              }
              this.fieldColumnWidth.fieldId = selectedField.fieldId
              this.fieldColumnWidth.width = selectedField.width
            }
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 6; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeBarSizeCase(modifyName) {
      if (!this.doChange) {
        this.doChange = _.debounce(() => {
          this.debounceChange(modifyName)
          this.doChange = undefined
        }, 200)
      }
      this.doChange()
    },
    debounceChange(modifyName) {
      this.sizeForm['modifyName'] = modifyName
      if (this.sizeForm.gaugeMax <= this.sizeForm.gaugeMin) {
        this.$message.error(this.$t('chart.max_more_than_mix'))
        return
      }
      const reg = /^\d+$/m
      if (!reg.test(this.sizeForm.tableRowFreezeHead) || !reg.test(this.sizeForm.tableColumnFreezeHead)) {
        this.$message.error(this.$t('chart.table_freeze') + this.$t('chart.needs_to_be_integer'))
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
    },
    changeFieldColumn() {
      const fieldWidth = this.sizeForm.tableFieldWidth?.find(i => i.fieldId === this.fieldColumnWidth.fieldId)
      if (fieldWidth) {
        this.fieldColumnWidth.width = fieldWidth.width
      }
    },
    changeFieldColumnWidth() {
      const fieldWidth = this.sizeForm.tableFieldWidth?.find(i => i.fieldId === this.fieldColumnWidth.fieldId)
      if (fieldWidth) {
        fieldWidth.width = this.fieldColumnWidth.width
        this.changeBarSizeCase('tableFieldWidth')
      }
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

.form-item-range-slider ::v-deep .el-form-item__content {
  padding-right: 6px
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.form-item ::v-deep .el-checkbox__label {
  font-size: 12px;
}

.form-item ::v-deep .el-radio__label {
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

::v-deep input::-webkit-outer-spin-button,
::v-deep input::-webkit-inner-spin-button {
  -webkit-appearance: none !important;
}

::v-deep input[type="number"] {
  -moz-appearance: textfield !important;
}

.column-radio {
  label {
    margin-right: 10px;
  }
}
</style>
