<template>
  <el-table-v2 v-if="show" :columns="columns" :data="data" :width="700" :height="400" fixed />
</template>

<script lang="ts" setup>
import { ref, shallowRef, nextTick, reactive, onMounted } from 'vue'
import { getPreviewData } from '@/api/dataset'
interface Field {
  fieldShortName: string
  name: string
}
let columns = []
let data = []
const show = ref(false)
const arr = [
  {
    currentDs: {
      sqlVariableDetails: null,
      tableName: 'dataset_table',
      type: 'db',
      datasourceId: '3',
      id: '1681891955428',
      info: '{"table":"dataset_table","sql":""}'
    },
    currentDsFields: [
      {
        originName: 'id',
        name: 'id',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'name',
        name: 'name',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'scene_id',
        name: 'scene_id',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'data_source_id',
        name: 'data_source_id',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'type',
        name: 'type',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'mode',
        name: 'mode',
        groupType: 'q',
        type: 'INTEGER',
        deType: 2,
        deExtractType: 2,
        checked: true
      },
      {
        originName: 'info',
        name: 'info',
        groupType: 'd',
        type: 'ANY',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'create_by',
        name: 'create_by',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'create_time',
        name: 'create_time',
        groupType: 'q',
        type: 'BIGINT',
        deType: 2,
        deExtractType: 2,
        checked: true
      },
      {
        originName: 'qrtz_instance',
        name: 'qrtz_instance',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'sync_status',
        name: 'sync_status',
        groupType: 'd',
        type: 'VARCHAR',
        deType: 0,
        deExtractType: 0,
        checked: true
      },
      {
        originName: 'last_update_time',
        name: 'last_update_time',
        groupType: 'q',
        type: 'BIGINT',
        deType: 2,
        deExtractType: 2,
        checked: true
      },
      {
        originName: 'sql_variable_details',
        name: 'sql_variable_details',
        groupType: 'd',
        type: 'ANY',
        deType: 0,
        deExtractType: 0,
        checked: true
      }
    ],
    childrenDs: [
      {
        currentDs: {
          sqlVariableDetails: null,
          tableName: 'dataset_table_field',
          type: 'db',
          datasourceId: '3',
          id: '1681891961971',
          info: '{"table":"dataset_table_field","sql":""}'
        },
        currentDsFields: [
          {
            originName: 'id',
            name: 'id',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'table_id',
            name: 'table_id',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'origin_name',
            name: 'origin_name',
            groupType: 'd',
            type: 'ANY',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'name',
            name: 'name',
            groupType: 'd',
            type: 'ANY',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'dataease_name',
            name: 'dataease_name',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'group_type',
            name: 'group_type',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'type',
            name: 'type',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'size',
            name: 'size',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'de_type',
            name: 'de_type',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'de_type_format',
            name: 'de_type_format',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'de_extract_type',
            name: 'de_extract_type',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'ext_field',
            name: 'ext_field',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'checked',
            name: 'checked',
            groupType: 'd',
            type: 'BOOLEAN',
            deType: 0,
            deExtractType: 0,
            checked: true
          },
          {
            originName: 'column_index',
            name: 'column_index',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'last_sync_time',
            name: 'last_sync_time',
            groupType: 'q',
            type: 'BIGINT',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'accuracy',
            name: 'accuracy',
            groupType: 'q',
            type: 'INTEGER',
            deType: 2,
            deExtractType: 2,
            checked: true
          },
          {
            originName: 'date_format',
            name: 'date_format',
            groupType: 'd',
            type: 'VARCHAR',
            deType: 0,
            deExtractType: 0,
            checked: true
          }
        ],
        childrenDs: [],
        unionToParent: {
          unionType: 'left',
          unionFields: [
            {
              parentField: {
                originName: 'id',
                name: 'id',
                groupType: 'd',
                type: 'VARCHAR',
                deType: 0,
                deExtractType: 0,
                checked: true
              },
              currentField: {
                originName: 'table_id',
                name: 'table_id',
                groupType: 'd',
                type: 'VARCHAR',
                deType: 0,
                deExtractType: 0,
                checked: true
              }
            }
          ]
        }
      }
    ],
    unionToParent: {
      unionType: 'left',
      unionFields: []
    }
  }
]
getPreviewData({ union: arr }).then(res => {
  columns = (res.data.fields as unknown as Field[]).map(ele => {
    return {
      key: ele.fieldShortName,
      dataKey: ele.fieldShortName,
      title: ele.name,
      width: 150
    }
  })

  data = (res.data.data as unknown as []) || []
  show.value = true
})
</script>
