<script lang="ts" setup>
import { ref } from 'vue'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const numberToken = ref('empty')
const options = [
  {
    value: 'number',
    label: t('api_pagination.number__size')
  },
  {
    value: 'token',
    label: t('api_pagination.cursor__size')
  },
  {
    value: 'empty',
    label: t('chart.line_symbol_none')
  }
]

const requestData = ref([
  {
    parameterName: t('api_pagination.page_number'),
    builtInParameterName: '${pageToken}',
    requestParameterName: '',
    parameterDefaultValue: ''
  },
  {
    parameterName: t('api_pagination.pagination_size'),
    builtInParameterName: '${pageSize}',
    requestParameterName: '',
    parameterDefaultValue: ''
  }
])

const defaultPathArr = [
  {
    value: 'number',
    label: t('api_pagination.total_number_de')
  },
  {
    value: 'page',
    label: t('api_pagination.number_of_pages')
  }
]

const resolutionPathOptions = ref(cloneDeep(defaultPathArr))

const responseData = ref([
  {
    parameterName: t('api_pagination.total_number'),
    resolutionPath: '',
    resolutionPathType: 'number'
  }
])

const handleNumberSizeChange = () => {
  resolutionPathOptions.value =
    numberToken.value === 'number' ? cloneDeep(defaultPathArr) : [cloneDeep(defaultPathArr)[0]]
  responseData.value[0].resolutionPathType = 'number'
  requestData.value[0].parameterName =
    numberToken.value === 'number' ? t('api_pagination.page_number') : t('api_pagination.cursor')
  responseData.value[0].parameterName =
    numberToken.value === 'number' ? t('api_pagination.total_number') : t('api_pagination.cursor')
}
</script>

<template>
  <div class="api-pagination">
    <span class="type">{{ t('api_pagination.pagination_method') }}</span>
    <el-select v-model="numberToken" @change="handleNumberSizeChange" style="width: 100%">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    <template v-if="numberToken !== 'empty'">
      <div class="table-title request">{{ t('datasource.request') }}</div>
      <el-table header-cell-class-name="header-cell" :data="requestData" style="width: 100%">
        <el-table-column prop="parameterName" :label="t('api_pagination.parameter_name')" />
        <el-table-column
          prop="builtInParameterName"
          :label="t('api_pagination.built_in_parameter_name')"
        />
        <el-table-column :label="t('api_pagination.request_parameter_name')" width="220">
          <template #default="scope">
            <el-input
              v-model="scope.row.requestParameterName"
              style="width: 100%"
              :placeholder="t('api_pagination.enter_parameter_name')"
            />
          </template>
        </el-table-column>
        <el-table-column :label="t('api_pagination.parameter_default_value')" width="220">
          <template #default="scope">
            <el-input
              v-model="scope.row.parameterDefaultValue"
              style="width: 100%"
              :placeholder="t('api_pagination.enter_default_value')"
            />
          </template>
        </el-table-column>
      </el-table>
      <div class="table-title response">{{ t('api_pagination.response') }}</div>
      <el-table header-cell-class-name="header-cell" :data="responseData" style="width: 100%">
        <el-table-column
          prop="parameterName"
          :label="t('api_pagination.parameter_name')"
          width="160"
        />
        <el-table-column prop="resolutionPath" :label="t('api_pagination.parsing_path')">
          <template #default="scope">
            <el-input
              v-model="scope.row.resolutionPath"
              style="width: 100%"
              :placeholder="t('api_pagination.please_enter_jsonpath')"
              ><template #prepend>
                <el-select
                  class="bg-white"
                  v-model="scope.row.resolutionPathType"
                  style="width: 89px"
                >
                  <el-option
                    v-for="item in resolutionPathOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select> </template
            ></el-input>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<style lang="less" scoped>
.api-pagination {
  margin-top: -8px;
  .type {
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    margin-bottom: 8px;
  }

  .table-title {
    width: 100%;
    height: 30px;
    padding: 4px 12px;

    &.request {
      background: #ebf1ff;
      margin-top: 16px;
      border-top: 1px solid #dddedf;
    }

    &.response {
      background: #e6f7f5;
    }
  }

  .bg-white {
    :deep(.ed-input__wrapper) {
      background: white;
    }
  }
}
</style>
