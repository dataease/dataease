<template>
  <div :style="{ height: maxHeight, overflow: 'auto' }">
    <el-row style="margin: 6px 0 16px 0">
      <el-col :span="12">
        <deBtn
          v-if="hasDataPermission('manage', param.privileges)"
          secondary
          icon="el-icon-plus"
          @click="addCalcField"
        >{{ $t('dataset.add_calc_field') }}
        </deBtn>
        <deBtn
          v-if="
            hasDataPermission('manage', param.privileges) &&
              table.type !== 'excel' &&
              table.type !== 'custom' &&
              table.type !== 'union'
          "
          secondary
          :loading="isSyncField"
          icon="el-icon-refresh-left"
          @click="syncField"
        >{{ $t('dataset.sync_field') }}
        </deBtn>
        &nbsp;
      </el-col>
      <el-col
        style="text-align: right"
        :span="12"
      >
        <el-input
          v-model="searchField"
          size="small"
          style="width: 240px"
          :placeholder="$t('deDataset.search_fields')"
          prefix-icon="el-icon-search"
          clearable
          class="main-area-input"
        />
      </el-col>
    </el-row>

    <el-collapse
      v-model="fieldActiveNames"
      class="style-collapse"
    >
      <el-collapse-item
        class="dimension"
        name="d"
        :title="`${$t('chart.dimension')} (${
          tableFields.dimensionListData.length
        })`"
      >
        <el-table
          :data="tableFields.dimensionListData"
          size="mini"
        >
          <el-table-column
            property="checked"
            :label="$t('dataset.field_check')"
            width="60"
          >
            <template
              #header
            >
              <el-checkbox
                v-model="dimensionChecked"
                :indeterminate="dimensionIndeterminate"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @change="saveDimension"
              />
            </template>
            <template slot-scope="scope">
              <el-checkbox
                v-model="scope.row.checked"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @change="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column
            property="name"
            :label="$t('dataset.field_name')"
            width="180"
          >
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.name"
                size="mini"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @blur="saveEdit(scope.row,false)"
                @keyup.enter.native="saveEdit(scope.row,false)"
              />
            </template>
          </el-table-column>
          <el-table-column
            v-if="
              !(
                (table.mode === 0 && table.type === 'custom') ||
                table.type === 'union'
              )
            "
            property="originName"
            :label="$t('dataset.field_origin_name')"
            width="100"
          >
            <template slot-scope="scope">
              <span
                v-if="scope.row.extField === 0"
                :title="scope.row.originName"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px">{{ scope.row.originName }}</span>
              </span>
              <span
                v-else-if="scope.row.extField === 2"
                :title="$t('dataset.calc_field')"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px; color: #c0c0c0">{{
                  $t('dataset.calc_field')
                }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column
            property="deTypeCascader"
            :label="$t('dataset.field_type')"
            min-width="200"
          >
            <template slot-scope="scope">
              <el-cascader
                v-model="scope.row.deTypeCascader"
                size="small"
                popper-class="select-date-resolution-format"
                :disabled="!hasDataPermission('manage', param.privileges)"
                class="select-type"
                :options="getFields(scope.row)"
                @change="saveEdit(scope.row)"
              >
                <template slot-scope="{ node, data }">
                  <span
                    v-if="node.level === 2 && data.label === 'yyyy-MM-dd'"
                    class="format-title"
                    :style="popPosition"
                  >{{ $t('chart.date_format') }}</span>
                  <span>
                    <svg-icon
                      v-if="data.value === 0"
                      icon-class="field_text"
                      class="field-icon-text field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 1"
                      icon-class="field_time"
                      class="field-icon-time field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 2 || data.value === 3"
                      icon-class="field_value"
                      class="field-icon-value field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 5"
                      icon-class="field_location"
                      class="field-icon-location field-icon-dimension"
                    />
                  </span>
                  <span style="color: #8492a6; font-size: 12px">{{
                    data.label
                  }}</span>
                </template>
              </el-cascader>
              <span class="select-svg-icon">
                <span v-if="scope.row.deType === 0 || scope.row.deType === 6">
                  <svg-icon
                    v-if="scope.row.deType === 0 || scope.row.deType === 6"
                    icon-class="field_text"
                    class="field-icon-text field-icon-dimension"
                  />
                </span>
                <span v-if="scope.row.deType === 1">
                  <svg-icon
                    v-if="scope.row.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time field-icon-dimension"
                  />
                </span>
                <span v-if="scope.row.deType === 2 || scope.row.deType === 3">
                  <svg-icon
                    v-if="scope.row.deType === 2 || scope.row.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value field-icon-dimension"
                  />
                </span>
                <span v-if="scope.row.deType === 5">
                  <svg-icon
                    v-if="scope.row.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location field-icon-dimension"
                  />
                </span>
              </span>
              <el-input
                v-if="scope.row.deType === 1 && scope.row.deExtractType === 0"
                v-model="scope.row.dateFormat"
                :placeholder="$t('dataset.date_format')"
                size="small"
                class="input-type"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @blur="saveEdit(scope.row)"
                @keyup.enter.native="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column
              property="key"
              :label="$t('dataset.change_to_key')"
              v-if="table.mode === 1 && (table.type === 'db' || table.mode === 'sql')"
          >
            <template slot-scope="scope">
              <el-select v-model="scope.row.key" @change="saveKey(scope.row)" >
                <el-option
                    v-for="item in getKeyFields(scope.row)"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column
            property="deExtractType"
            :label="$t('dataset.origin_field_type')"
            width="100"
          >
            <template slot-scope="scope">
              <span v-if="scope.row.extField === 0">
                <span
                  v-if="
                    scope.row.deExtractType === 0 ||
                      scope.row.deExtractType === 6
                  "
                >
                  <svg-icon
                    v-if="
                      scope.row.deExtractType === 0 ||
                        scope.row.deExtractType === 6
                    "
                    icon-class="field_text"
                    class="field-icon-text field-icon-dimension"
                  />
                  <span class="field-class">{{ $t('dataset.text') }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 1">
                  <svg-icon
                    v-if="scope.row.deExtractType === 1"
                    icon-class="field_time"
                    class="field-icon-time field-icon-dimension"
                  />
                  <span class="field-class">{{ $t('dataset.time') }}</span>
                </span>
                <span
                  v-if="
                    scope.row.deExtractType === 2 ||
                      scope.row.deExtractType === 3 ||
                      scope.row.deExtractType === 4
                  "
                >
                  <svg-icon
                    v-if="
                      scope.row.deExtractType === 2 ||
                        scope.row.deExtractType === 3 ||
                        scope.row.deExtractType === 4
                    "
                    icon-class="field_value"
                    class="field-icon-value field-icon-dimension"
                  />
                  <span
                    v-if="
                      scope.row.deExtractType === 2 ||
                        scope.row.deExtractType === 4
                    "
                    class="field-class"
                  >{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deExtractType === 3"
                    class="field-class"
                  >{{
                    $t('dataset.value') + '(' + $t('dataset.float') + ')'
                  }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 5">
                  <svg-icon
                    v-if="scope.row.deExtractType === 5"
                    icon-class="field_location"
                    class="field-icon-location field-icon-dimension"
                  />
                  <span class="field-class">{{ $t('dataset.location') }}</span>
                </span>
              </span>
              <span
                v-else-if="scope.row.extField === 2"
                :title="$t('dataset.calc_field')"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px; color: #c0c0c0">{{
                  $t('dataset.calc_field')
                }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column
            min-width="182"
            :label="$t('dataset.operator')"
          >
            <template slot-scope="scope">
              <el-button
                class="de-text-btn"
                type="text"
                style="margin-left: -4px"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @click="dqTrans(scope.row, 'd')"
              >{{ $t('deDataset.convert_to_indicator') }}
              </el-button>
              <template v-if="scope.row.extField !== 0">
                <el-button
                  :disabled="!hasDataPermission('manage', param.privileges)"
                  type="text"
                  class="de-text-btn"
                  style="margin-left: 8px"
                  @click="editField(scope.row)"
                >{{ $t('dataset.edit') }}
                </el-button>
                <el-dropdown
                  size="medium"
                  trigger="click"
                  @command="(type) => handleCommand(type, scope.row)"
                >
                  <el-button
                    style="margin-left: 8px"
                    class="el-icon-more de-text-btn"
                    type="text"
                  />
                  <el-dropdown-menu
                    slot="dropdown"
                    class="de-card-dropdown"
                  >
                    <slot>
                      <el-dropdown-item
                        :disabled="
                          !hasDataPermission('manage', param.privileges)
                        "
                        command="copy"
                      >
                        <i class="el-icon-document-copy" />
                        {{ $t('dataset.copy') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        :disabled="
                          !hasDataPermission('manage', param.privileges)
                        "
                        command="delete"
                      >
                        <i class="el-icon-delete" />
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </slot>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
              <el-button
                v-else
                :disabled="!hasDataPermission('manage', param.privileges)"
                type="text"
                style="margin-left: 8px"
                class="de-text-btn"
                @click="copyField(scope.row)"
              >{{ $t('dataset.copy') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item
        class="quota"
        name="q"
        :title="`${$t('chart.quota')} (${tableFields.quotaListData.length})`"
      >
        <el-table
          :data="tableFields.quotaListData"
          size="mini"
        >
          <el-table-column
            property="checked"
            :label="$t('dataset.field_check')"
            width="60"
          >
            <template
              #header
            >
              <el-checkbox
                v-model="quotaChecked"
                :indeterminate="quotaIndeterminate"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @change="saveQuota"
              />
            </template>
            <template slot-scope="scope">
              <el-checkbox
                v-model="scope.row.checked"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @change="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column
            property="name"
            :label="$t('dataset.field_name')"
            width="180"
          >
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.name"
                size="mini"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @blur="saveEdit(scope.row,false)"
                @keyup.enter.native="saveEdit(scope.row,false)"
              />
            </template>
          </el-table-column>
          <el-table-column
            v-if="
              !(
                (table.mode === 0 && table.type === 'custom') ||
                table.type === 'union'
              )
            "
            property="originName"
            :label="$t('dataset.field_origin_name')"
            width="100"
          >
            <template slot-scope="scope">
              <span
                v-if="scope.row.extField === 0"
                :title="scope.row.originName"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px">{{ scope.row.originName }}</span>
              </span>
              <span
                v-else-if="scope.row.extField === 2"
                :title="$t('dataset.calc_field')"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px; color: #c0c0c0">{{
                  $t('dataset.calc_field')
                }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column
            property="deTypeCascader"
            :label="$t('dataset.field_type')"
            min-width="200"
          >
            <template slot-scope="scope">
              <el-cascader
                v-model="scope.row.deTypeCascader"
                size="small"
                popper-class="select-date-resolution-format"
                :disabled="!hasDataPermission('manage', param.privileges)"
                class="select-type"
                :options="getFields(scope.row)"
                @change="saveEdit(scope.row)"
              >
                <template slot-scope="{ node, data }">
                  <span
                    v-if="node.level === 2 && data.label === 'yyyy-MM-dd'"
                    class="format-title"
                    :style="popPosition"
                  >{{ $t('chart.date_format') }}</span>
                  <span>
                    <svg-icon
                      v-if="data.value === 0"
                      icon-class="field_text"
                      class="field-icon-text field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 1"
                      icon-class="field_time"
                      class="field-icon-time field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 2 || data.value === 3"
                      icon-class="field_value"
                      class="field-icon-value field-icon-dimension"
                    />
                    <svg-icon
                      v-if="data.value === 5"
                      icon-class="field_location"
                      class="field-icon-location field-icon-dimension"
                    />
                  </span>
                  <span style="color: #8492a6; font-size: 12px">{{
                    data.label
                  }}</span>
                </template>
              </el-cascader>
              <span class="select-svg-icon">
                <span v-if="scope.row.deType === 0">
                  <svg-icon
                    v-if="scope.row.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text field-icon-quota"
                  />
                </span>
                <span v-if="scope.row.deType === 1">
                  <svg-icon
                    v-if="scope.row.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time field-icon-quota"
                  />
                </span>
                <span v-if="scope.row.deType === 2 || scope.row.deType === 3">
                  <svg-icon
                    v-if="scope.row.deType === 2 || scope.row.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value field-icon-quota"
                  />
                </span>
                <span v-if="scope.row.deType === 5">
                  <svg-icon
                    v-if="scope.row.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location field-icon-quota"
                  />
                </span>
              </span>
              <el-input
                v-if="scope.row.deType === 1 && scope.row.deExtractType === 0"
                v-model="scope.row.dateFormat"
                :placeholder="$t('dataset.date_format')"
                size="small"
                class="input-type"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @blur="saveEdit(scope.row)"
                @keyup.enter.native="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column
              property="key"
              :label="$t('dataset.change_to_key')"
              v-if="table.mode === 1 && (table.type === 'db' || table.mode === 'sql')"
          >
            <template slot-scope="scope">
              <el-select v-model="scope.row.key" @change="saveKey(scope.row)" >
                <el-option
                    v-for="item in getKeyFields(scope.row)"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column
            property="deExtractType"
            :label="$t('dataset.origin_field_type')"
            width="100"
          >
            <template slot-scope="scope">
              <span v-if="scope.row.extField === 0">
                <span v-if="scope.row.deExtractType === 0">
                  <svg-icon
                    v-if="scope.row.deExtractType === 0"
                    icon-class="field_text"
                    class="field-icon-text field-icon-quota"
                  />
                  <span class="field-class">{{ $t('dataset.text') }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 1">
                  <svg-icon
                    v-if="scope.row.deExtractType === 1"
                    icon-class="field_time"
                    class="field-icon-time field-icon-quota"
                  />
                  <span class="field-class">{{ $t('dataset.time') }}</span>
                </span>
                <span
                  v-if="
                    scope.row.deExtractType === 2 ||
                      scope.row.deExtractType === 3 ||
                      scope.row.deExtractType === 4
                  "
                >
                  <svg-icon
                    v-if="
                      scope.row.deExtractType === 2 ||
                        scope.row.deExtractType === 3 ||
                        scope.row.deExtractType === 4
                    "
                    icon-class="field_value"
                    class="field-icon-value field-icon-quota"
                  />
                  <span
                    v-if="
                      scope.row.deExtractType === 2 ||
                        scope.row.deExtractType === 4
                    "
                    class="field-class"
                  >{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deExtractType === 3"
                    class="field-class"
                  >{{
                    $t('dataset.value') + '(' + $t('dataset.float') + ')'
                  }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 5">
                  <svg-icon
                    v-if="scope.row.deExtractType === 5"
                    icon-class="field_location"
                    class="field-icon-location field-icon-quota"
                  />
                  <span class="field-class">{{ $t('dataset.location') }}</span>
                </span>
              </span>
              <span
                v-else-if="scope.row.extField === 2"
                :title="$t('dataset.calc_field')"
                class="field-class"
                style="
                  width: 100%;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
              >
                <span style="font-size: 12px; color: #c0c0c0">{{
                  $t('dataset.calc_field')
                }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column
            min-width="182"
            property=""
            :label="$t('dataset.operator')"
          >
            <template slot-scope="scope">
              <el-button
                class="de-text-btn"
                type="text"
                style="margin-left: -4px"
                :disabled="!hasDataPermission('manage', param.privileges)"
                @click="dqTrans(scope.row, 'q')"
              >{{ $t('deDataset.convert_to_dimension') }}
              </el-button>
              <template v-if="scope.row.extField !== 0">
                <el-button
                  :disabled="!hasDataPermission('manage', param.privileges)"
                  type="text"
                  class="de-text-btn"
                  style="margin-left: 8px"
                  @click="editField(scope.row)"
                >{{ $t('dataset.edit') }}
                </el-button>
                <el-dropdown
                  size="medium"
                  trigger="click"
                  @command="(type) => handleCommand(type, scope.row)"
                >
                  <el-button
                    style="margin-left: 8px"
                    class="el-icon-more de-text-btn"
                    type="text"
                  />
                  <el-dropdown-menu
                    slot="dropdown"
                    class="de-card-dropdown"
                  >
                    <slot>
                      <el-dropdown-item
                        :disabled="
                          !hasDataPermission('manage', param.privileges)
                        "
                        command="copy"
                      >
                        <i class="el-icon-document-copy" />
                        {{ $t('dataset.copy') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        :disabled="
                          !hasDataPermission('manage', param.privileges)
                        "
                        command="delete"
                      >
                        <i class="el-icon-delete" />
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </slot>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
              <el-button
                v-else
                :disabled="!hasDataPermission('manage', param.privileges)"
                type="text"
                style="margin-left: 8px"
                class="de-text-btn"
                @click="copyField(scope.row)"
              >{{ $t('dataset.copy') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog
      :visible.sync="editCalcField"
      class="de-dialog-form de-center-dialog"
      width="980px"
      :before-close="closeCalcField"
      :title="
        currEditField.id
          ? $t('dataset.edit_calc_field')
          : $t('dataset.add_calc_field')
      "
      append-to-body
    >
      <calc-field-edit
        ref="calcFieldEdit"
        :param="param"
        :table-fields="tableFields"
        :field="currEditField"
        @onEditClose="closeCalcField"
      />
    </el-dialog>
  </div>
</template>

<script>
import { batchEdit, dateformats, fieldListDQ, post } from '@/api/dataset/dataset'
import CalcFieldEdit from './CalcFieldEdit'
import { getFieldName } from '@/views/dataset/data/utils'
import msgCfm from '@/components/msgCfm/index'
import {engineMode} from "@/api/system/engine";

export default {
  name: 'FieldEdit',
  components: { CalcFieldEdit },
  mixins: [msgCfm],
  props: {
    param: {
      type: Object,
      required: true
    },
    table: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      maxHeight: 'auto',
      tableFields: {
        dimensionList: [],
        quotaList: [],
        dimensionListData: [],
        quotaListData: []
      },
      popPosition: {},
      dateformats: [],
      fieldActiveNames: ['d', 'q'],
      searchField: '',
      editCalcField: false,
      currEditField: {},
      isSyncField: false,
      dimensionChecked: false,
      dimensionIndeterminate: false,
      quotaChecked: false,
      quotaIndeterminate: false,
      engineMode: 'local',
    }
  },
  watch: {
    param: function() {
      this.initField()
    },
    searchField(val) {
      this.filterField(val)
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calcHeight)
  },
  created() {
    engineMode().then((res) => {
      this.engineMode = res.data
    })
  },
  mounted() {
    window.addEventListener('resize', this.calcHeight)
    this.calcHeight()
    this.initField()
  },
  methods: {
    calcHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.maxHeight = currentHeight - 56 - 30 - 35 - 26 - 10 - 10 + 'px'
      }, 10)
    },
    initField() {
      fieldListDQ(this.param.id).then((response) => {
        this.tableFields = response.data
        this.tableFields.dimensionListData = JSON.parse(
          JSON.stringify(this.tableFields.dimensionList)
        )
        this.tableFields.quotaListData = JSON.parse(
          JSON.stringify(this.tableFields.quotaList)
        )
        this.filterField(this.searchField)
        this.dimensionChange()
        this.quotaChange()
      })
      dateformats(this.param.id).then((response) => {
        const children = (response?.data || []).map(ele => ({ label: ele.dateformat + (ele.desc !== null ? ('(' + ele.desc) + ')' : ''), value: ele.dateformat }))
        children.push({ label: this.$t('commons.custom'), value: 'custom' })
        this.dateformats = children
      })
    },
    getKeyFields(item) {
      return [
        { label: this.$t('commons.yes'), value: true },
        { label: this.$t('commons.no'), value: false }
      ]
    },
    getFields(item) {
      if (item.deExtractType === 0) {
        const children = this.dateformats
        return [
          { label: this.$t('dataset.text'), value: 0 },
          { label: this.$t('dataset.time'), value: 1, children },
          { label: this.$t('dataset.value'), value: 2 },
          {
            label:
              this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
            value: 3
          },
          { label: this.$t('dataset.location'), value: 5 }
        ]
      } else {
        return [
          { label: this.$t('dataset.text'), value: 0 },
          { label: this.$t('dataset.time'), value: 1 },
          { label: this.$t('dataset.value'), value: 2 },
          { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
          { label: this.$t('dataset.location'), value: 5 }
        ]
      }
    },

    saveKey(item ) {
      post('/dataset/field/saveKey', item)
          .then((response) => {
            this.initField()
            localStorage.setItem('reloadDsData', 'true')
          })
          .catch((res) => {
            this.initField()
            localStorage.setItem('reloadDsData', 'true')
          })
    },

    saveEdit(item, checkExp = true) {
      if (item.name && item.name.length > 50) {
        this.$message.error(this.$t('dataset.field_name_less_50'))
        return
      }
      item.deType = item.deTypeCascader[0]
      if (item.deTypeCascader.length === 2) { // 时间
        item.dateFormatType = item.deTypeCascader[1]
        if (item.dateFormatType !== 'custom') {
          item.dateFormat = item.dateFormatType
        }
      } else {
        item.dateFormatType = ''
        item.dateFormat = ''
      }
      if (item.dateFormatType === 'custom' && !item.dateFormat) {
        return
      }
      let url
      if (checkExp) {
        url = '/dataset/field/save'
      } else {
        url = '/dataset/field/saveNotCheck'
      }
      post(url, item)
        .then((response) => {
          this.initField()
          localStorage.setItem('reloadDsData', 'true')
        })
        .catch((res) => {
          this.initField()
          localStorage.setItem('reloadDsData', 'true')
        })
    },

    dqTrans(item, val) {
      if (val === 'd') {
        item.groupType = 'q'
      } else if (val === 'q') {
        item.groupType = 'd'
      }
      this.saveEdit(item, false)
    },

    addCalcField() {
      this.currEditField = {}
      this.editCalcField = true
    },

    closeCalcField() {
      this.editCalcField = false
      this.$refs.calcFieldEdit.resetField()
      this.initField()
    },

    filterField(val) {
      if (val && val !== '') {
        this.tableFields.dimensionListData = JSON.parse(
          JSON.stringify(
            this.tableFields.dimensionList.filter((ele) => {
              return ele.name
                .toLocaleLowerCase()
                .includes(val.toLocaleLowerCase())
            })
          )
        )
        this.tableFields.quotaListData = JSON.parse(
          JSON.stringify(
            this.tableFields.quotaList.filter((ele) => {
              return ele.name
                .toLocaleLowerCase()
                .includes(val.toLocaleLowerCase())
            })
          )
        )
      } else {
        this.tableFields.dimensionListData = JSON.parse(
          JSON.stringify(this.tableFields.dimensionList)
        )
        this.tableFields.quotaListData = JSON.parse(
          JSON.stringify(this.tableFields.quotaList)
        )
      }
    },

    editField(item) {
      this.currEditField = item
      this.editCalcField = true
    },
    handleCommand(type, row) {
      switch (type) {
        case 'copy':
          this.copyField(row)
          break
        case 'delete':
          this.deleteField(row)
          break
        default:
          break
      }
    },
    deleteField(item) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('chart.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      })
        .then(() => {
          post(
            '/dataset/field/delete/' + item.id + '/' + item.tableId,
            null
          ).then((response) => {
            this.$message({
              type: 'success',
              message: this.$t('chart.delete_success'),
              showClose: true
            })
            this.initField()
            localStorage.setItem('reloadDsData', 'true')
          })
        })
        .catch(() => {
        })
    },

    syncField() {
      const options = {
        title: 'commons.prompt',
        content: this.$t('deDataset.sure_to_synchronize'),
        type: 'primary',
        confirmButtonText: this.$t('commons.confirm'),
        cb: () => {
          this.isSyncField = true
          post('/dataset/table/syncField/' + this.param.id, null).then(
            (response) => {
              localStorage.setItem('reloadDsData', 'true')
              setTimeout(() => {
                this.isSyncField = false
                this.initField()
                // tips
                let msg = ''
                let type = ''
                if (response.data.mode === 0) {
                  msg = this.$t('dataset.sync_success')
                  type = 'success'
                } else {
                  msg = this.$t('dataset.sync_success_1')
                  type = 'warning'
                }
                this.$message({
                  type: type,
                  message: msg,
                  showClose: true
                })
              }, 500)
            }
          )
        }
      }
      this.handlerConfirm(options)
    },

    saveDimension() {
      const list = JSON.parse(
        JSON.stringify(this.tableFields.dimensionListData)
      )
      if (this.dimensionChecked) {
        list.forEach((ele) => {
          ele.checked = true
        })
      } else {
        list.forEach((ele) => {
          ele.checked = false
        })
      }
      batchEdit(list).then((response) => {
        this.initField()
        localStorage.setItem('reloadDsData', 'true')
      })
    },
    saveQuota() {
      const list = JSON.parse(JSON.stringify(this.tableFields.quotaListData))
      if (this.quotaChecked) {
        list.forEach((ele) => {
          ele.checked = true
        })
      } else {
        list.forEach((ele) => {
          ele.checked = false
        })
      }
      batchEdit(list).then((response) => {
        this.initField()
        localStorage.setItem('reloadDsData', 'true')
      })
    },

    dimensionChange() {
      let checkedCount = 0
      this.tableFields.dimensionListData.forEach((ele) => {
        if (ele.checked) {
          checkedCount++
        }
      })
      this.dimensionChecked =
        checkedCount === this.tableFields.dimensionListData.length
      this.dimensionIndeterminate =
        checkedCount > 0 &&
        checkedCount < this.tableFields.dimensionListData.length
    },
    quotaChange() {
      let checkedCount = 0
      this.tableFields.quotaListData.forEach((ele) => {
        if (ele.checked) {
          checkedCount++
        }
      })
      this.quotaChecked = checkedCount === this.tableFields.quotaListData.length
      this.quotaIndeterminate =
        checkedCount > 0 && checkedCount < this.tableFields.quotaListData.length
    },

    copyField(item) {
      const param = { ...item }
      param.id = null
      param.extField = 2
      param.originName =
        item.extField === 2 ? item.originName : '[' + item.id + ']'
      param.name = getFieldName(
        this.tableFields.dimensionListData.concat(
          this.tableFields.quotaListData
        ),
        item.name
      )
      param.dataeaseName = null
      param.lastSyncTime = null
      param.columnIndex =
        this.tableFields.dimensionListData.length +
        this.tableFields.quotaListData.length

      post('/dataset/field/save', param)
        .then((response) => {
          this.initField()
          localStorage.setItem('reloadDsData', 'true')
        })
        .catch((res) => {
          this.initField()
          localStorage.setItem('reloadDsData', 'true')
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

span {
  font-size: 14px;
}

.field-class {
  font-size: 12px !important;
}

.el-select ::v-deep input {
  padding-right: 10px;
}

.el-select ::v-deep .el-input__suffix {
  right: 0;
}

.el-radio {
  margin-right: 10px !important;
}

.style-collapse {
  border: none;

  .select-type {
    width: 180px;

    ::v-deep.el-input__inner {
      padding-left: 32px;
    }
  }

  .input-type {
    width: 180px;
  }

  .select-svg-icon {
    position: absolute;
    left: 24px;
    top: 10px;
  }

  ::v-deep.el-collapse-item__header {
    height: 30px;
    line-height: 30px;
    padding: 0 0 0 30px;
    position: relative;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }

  ::v-deep.el-checkbox {
    margin-left: 8px;
  }

  .dimension {
    ::v-deep.el-collapse-item__header {
      background-color: rgba(51, 112, 255, 0.1);
    }
  }

  .quota {
    ::v-deep.el-collapse-item__header {
      background-color: rgba(4, 180, 156, 0.1);
    }
  }

  ::v-deep.el-collapse-item__arrow {
    position: absolute;
    top: 9px;
    left: 13px;
    font-size: 12px;
  }

  ::v-deep.el-collapse-item__arrow::before {
    content: '\E791';
  }

  ::v-deep.el-collapse-item__arrow.is-active {
    transform: rotate(90deg);
  }
}

.style-collapse ::v-deep .el-collapse-item__wrap {
  border-bottom: 0 solid #e6ebf5 !important;
}
</style>

<style lang="scss">
.select-date-resolution-format {
  .format-title {
    position: fixed;
    display: inline-block;
    height: 30px;
    transform: translate(-30px, -37px);
    background: #dfe6ec;
    width: 180px;
    padding-left: 30px;
  }
}
</style>
