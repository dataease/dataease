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
          v-show="showProperty('barWidth')"
          :label="$t('chart.bar_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.barWidth"
            :disabled="sizeForm.barDefault"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="1"
            :max="80"
            @change="changeBarSizeCase('barWidth')"
          />
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
          v-show="showProperty('lineType')"
          :label="$t('chart.line_type')"
          class="form-item"
        >
          <el-radio-group
            v-model="sizeForm.lineType"
            @change="changeBarSizeCase('lineType')"
          >
            <el-radio-button label="solid">{{ $t('chart.line_type_solid') }}</el-radio-button>
            <el-radio-button label="dashed">{{ $t('chart.line_type_dashed') }}</el-radio-button>
          </el-radio-group>
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
          >{{ $t('chart.line_smooth') }}</el-checkbox>
        </el-form-item>

        <!--line-end-->
        <!--pie-begin-->
        <el-form-item
          v-show="showProperty('pieInnerRadius')"
          :label="$t('chart.pie_inner_radius')"
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
          :label="$t('chart.pie_outer_radius')"
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

        <span v-show="showProperty('pieRoseType') || showProperty('pieRoseRadius') ">
          <el-form-item
            v-show="showProperty('pieRoseType')"
            :label="$t('chart.rose_type')"
            class="form-item"
          >
            <el-radio-group
              v-model="sizeForm.pieRoseType"
              size="mini"
              @change="changeBarSizeCase('pieRoseType')"
            >
              <el-radio-button label="radius">{{ $t('chart.radius_mode') }}</el-radio-button>
              <el-radio-button label="area">{{ $t('chart.area_mode') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="showProperty('pieRoseRadius')"
            :label="$t('chart.rose_radius')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.pieRoseRadius"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="100"
              @change="changeBarSizeCase('pieRoseRadius')"
            />
          </el-form-item>
        </span>
        <!--pie-end-->
        <!--funnel-begin-->

        <el-form-item
          v-show="showProperty('funnelWidth')"
          :label="$t('chart.funnel_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.funnelWidth"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('funnelWidth')"
          />
        </el-form-item>

        <!--funnel-end-->
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
          v-show="showProperty('tablePageSize') && sizeForm.tablePageMode === 'page'"
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
          v-show="showProperty('tableAutoBreakLine')"
          label-width="100px"
          :label="$t('chart.table_auto_break_line')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.tableAutoBreakLine"
            @change="changeBarSizeCase('tableAutoBreakLine')"
          >{{ $t('chart.open') }}</el-checkbox>
          <el-tooltip
            class="item"
            effect="dark"
            placement="bottom"
          >
            <div slot="content">
              {{ $t('chart.table_break_line_tip') }}
            </div>
            <i
              class="el-icon-info"
              style="cursor: pointer;color: gray;font-size: 12px;"
            />
          </el-tooltip>
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
          v-show="showProperty('tableTitleHeight')"
          label-width="100px"
          :label="$t('chart.table_title_height')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableTitleHeight"
            :min="36"
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
            :disabled="sizeForm.tableAutoBreakLine"
            :min="36"
            :max="100"
            show-input
            :show-input-controls="false"
            input-size="mini"
            @change="changeBarSizeCase('tableItemHeight')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('tableColumnWidth')"
          label-width="100px"
          :label="$t('chart.table_column_width_config')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.tableColumnWidth"
            :min="10"
            :max="1000"
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

        <!--table-end-->
        <!--gauge-begin-->
        <el-form-item
          v-show="showProperty('gaugeMin')"
          :label="$t('chart.min')"
          class="form-item form-item-slider"
        >
          <el-input-number
            v-model="sizeForm.gaugeMin"
            size="mini"
            @change="changeBarSizeCase('gaugeMin')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('gaugeMax')"
          :label="$t('chart.max')"
          class="form-item form-item-slider"
        >
          <el-input-number
            v-model="sizeForm.gaugeMax"
            size="mini"
            @change="changeBarSizeCase('gaugeMax')"
          />
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
        <!--gauge-end-->
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
        <!--treemap-begin-->
        <el-form-item
          v-show="showProperty('treemapWidth')"
          :label="$t('chart.width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.treemapWidth"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('treemapWidth')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('treemapHeight')"
          :label="$t('chart.height')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.treemapHeight"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="100"
            @change="changeBarSizeCase('treemapHeight')"
          />
        </el-form-item>
        <!--treemap-end-->
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
            :label="$t('chart.bar_width')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="sizeForm.barWidth"
              :disabled="sizeForm.barDefault"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="1"
              :max="80"
              @change="changeBarSizeCase('barWidth')"
            />
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
            :label="$t('chart.line_type')"
            class="form-item"
          >
            <el-radio-group
              v-model="sizeForm.lineType"
              @change="changeBarSizeCase('lineType')"
            >
              <el-radio-button label="solid">{{ $t('chart.line_type_solid') }}</el-radio-button>
              <el-radio-button label="dashed">{{ $t('chart.line_type_dashed') }}</el-radio-button>
            </el-radio-group>
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
              @change="changeBarSizeCase"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.line_smooth')"
            class="form-item"
          >
            <el-checkbox
              v-model="sizeForm.lineSmooth"
              @change="changeBarSizeCase('lineSmooth')"
            >{{ $t('chart.line_smooth') }}</el-checkbox>
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
        <el-form-item
          v-show="showProperty('liquidOutlineBorder')"
          :label="$t('chart.liquid_outline_border')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidOutlineBorder"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="1"
            :max="20"
            @change="changeBarSizeCase('liquidOutlineBorder')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('liquidOutlineDistance')"
          :label="$t('chart.liquid_outline_distance')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidOutlineDistance"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="20"
            @change="changeBarSizeCase('liquidOutlineDistance')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('liquidWaveLength')"
          :label="$t('chart.liquid_wave_length')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidWaveLength"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="10"
            :max="500"
            @change="changeBarSizeCase('liquidWaveLength')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('liquidWaveCount')"
          :label="$t('chart.liquid_wave_count')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.liquidWaveCount"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="2"
            :max="10"
            @change="changeBarSizeCase('liquidWaveCount')"
          />
        </el-form-item>

        <!--liquid-end-->
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { CHART_FONT_FAMILY, CHART_FONT_LETTER_SPACE, DEFAULT_SIZE } from '../../chart/chart'
export default {
  name: 'SizeSelector',
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
    }
  },
  data() {
    return {
      sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      lineSymbolOptions: [
        { name: this.$t('chart.line_symbol_none'), value: 'none' },
        { name: this.$t('chart.line_symbol_emptyCircle'), value: 'emptyCircle' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'rect' },
        { name: this.$t('chart.line_symbol_roundRect'), value: 'roundRect' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'diamond' },
        { name: this.$t('chart.line_symbol_pin'), value: 'pin' },
        { name: this.$t('chart.line_symbol_arrow'), value: 'arrow' }
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
      fontFamily: CHART_FONT_FAMILY,
      fontLetterSpace: CHART_FONT_LETTER_SPACE
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initData()
  },
  methods: {
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

          this.sizeForm.tablePageMode = this.sizeForm.tablePageMode ? this.sizeForm.tablePageMode : DEFAULT_SIZE.tablePageMode
          this.sizeForm.tablePageSize = this.sizeForm.tablePageSize ? this.sizeForm.tablePageSize : DEFAULT_SIZE.tablePageSize

          this.sizeForm.showIndex = this.sizeForm.showIndex ? this.sizeForm.showIndex : DEFAULT_SIZE.showIndex
          if (this.sizeForm.indexLabel === null || this.sizeForm.indexLabel === undefined) {
            this.sizeForm.indexLabel = DEFAULT_SIZE.indexLabel
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

          this.sizeForm.tableAutoBreakLine = this.sizeForm.tableAutoBreakLine ? this.sizeForm.tableAutoBreakLine : DEFAULT_SIZE.tableAutoBreakLine
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
    }
  }
}
</script>

<style scoped>
.shape-item{
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{font-size: 12px}

.el-form-item{
  margin-bottom: 6px;
}
.el-divider--horizontal {
  margin: 10px 0
}
.divider-style ::v-deep .el-divider__text{
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
</style>
