<script lang="ts" setup>
import { ref, reactive } from 'vue'
import UnionFieldList from './UnionFieldList.vue'
import UnionItemEdit from './UnionItemEdit.vue'
const changeParentFields = val => {
  console.log(val)
}
const changeNodeFields = val => {
  console.log(val)
}
const parentField = ref([
  {
    id: '8a49718f-6140-4bea-8368-3a949a646b93',
    tableId: '2a482d0d-39c7-486b-9adb-4b923f184743',
    originName: 'id',
    name: 'id',
    dataeaseName: 'C_b80bb7740288fda1f201890375a60c8f',
    groupType: 'q',
    type: 'INT',
    size: 10,
    deType: 2,
    deTypeFormat: null,
    deExtractType: 2,
    extField: 0,
    checked: true,
    columnIndex: 0,
    lastSyncTime: 1669780525013,
    accuracy: 0,
    dateFormat: null,
    dateFormatType: null
  },
  {
    id: '1671021f-5cf4-4208-8073-6bd63da4dafc',
    tableId: '2a482d0d-39c7-486b-9adb-4b923f184743',
    originName: 'name',
    name: 'name',
    dataeaseName: 'C_b068931cc450442b63f5b3d276ea4297',
    groupType: 'd',
    type: 'VARCHAR',
    size: 255,
    deType: 0,
    deTypeFormat: null,
    deExtractType: 0,
    extField: 0,
    checked: true,
    columnIndex: 1,
    lastSyncTime: 1669780525013,
    accuracy: 0,
    dateFormat: null,
    dateFormatType: null
  }
])
const nodeField = ref([
  {
    id: '8f4e0c7f-56df-4da8-be72-2e53e290a789',
    tableId: 'd61725c8-f536-4745-9582-3cd71b47220e',
    originName: 'id',
    name: 'id',
    dataeaseName: 'C_b80bb7740288fda1f201890375a60c8f',
    groupType: 'q',
    type: 'INT',
    size: 10,
    deType: 2,
    deTypeFormat: null,
    deExtractType: 2,
    extField: 0,
    checked: true,
    columnIndex: 0,
    lastSyncTime: 1669780525159,
    accuracy: 0,
    dateFormat: null,
    dateFormatType: null
  },
  {
    id: '98ac3f1e-c57d-4ce5-ac88-945df0e32971',
    tableId: 'd61725c8-f536-4745-9582-3cd71b47220e',
    originName: 'name2',
    name: 'name2',
    dataeaseName: 'C_5a7fcd4f1c785c8ef4931a5a9c698ac0',
    groupType: 'd',
    type: 'VARCHAR',
    size: 255,
    deType: 0,
    deTypeFormat: null,
    deExtractType: 0,
    extField: 0,
    checked: true,
    columnIndex: 1,
    lastSyncTime: 1669780525159,
    accuracy: 0,
    dateFormat: null,
    dateFormatType: null
  }
])
const unionParam = reactive({
  type: 'add',
  nodeIndex: 0,
  node: {
    currentDs: {
      id: 'd61725c8-f536-4745-9582-3cd71b47220e',
      pid: '2bf4ceaa-bc5a-4a2e-81c9-25cff27d2d5c',
      nodeType: 'leaf',
      modelType: 'dataset',
      modelInnerType: 'db',
      authType: 'source',
      createBy: 'admin',
      level: 0,
      mode: 1,
      dataSourceId: '3419337a-e87a-4b35-bcc4-a9e17222fc4a',
      name: 'wjh7_d_test2',
      label: 'wjh7_d_test2',
      privileges: 'grant,manage,use',
      children: null,
      allLeafs: 0,
      innerId: null,
      isPlugin: false
    },
    currentDsField: [],
    childrenDs: [],
    unionToParent: { unionType: 'left', unionFields: [{ parentField: {}, currentField: {} }] },
    allChildCount: 0
  },
  parent: {
    currentDs: {
      id: '2a482d0d-39c7-486b-9adb-4b923f184743',
      pid: '2bf4ceaa-bc5a-4a2e-81c9-25cff27d2d5c',
      nodeType: 'leaf',
      modelType: 'dataset',
      modelInnerType: 'db',
      authType: 'source',
      createBy: 'admin',
      level: 0,
      mode: 1,
      dataSourceId: '3419337a-e87a-4b35-bcc4-a9e17222fc4a',
      name: 'wjh7_d_test1',
      label: 'wjh7_d_test1',
      privileges: 'grant,manage,use',
      children: null,
      allLeafs: 0,
      innerId: null,
      isPlugin: false
    },
    currentDsField: [],
    childrenDs: [
      {
        currentDs: {
          id: 'd61725c8-f536-4745-9582-3cd71b47220e',
          pid: '2bf4ceaa-bc5a-4a2e-81c9-25cff27d2d5c',
          nodeType: 'leaf',
          modelType: 'dataset',
          modelInnerType: 'db',
          authType: 'source',
          createBy: 'admin',
          level: 0,
          mode: 1,
          dataSourceId: '3419337a-e87a-4b35-bcc4-a9e17222fc4a',
          name: 'wjh7_d_test2',
          label: 'wjh7_d_test2',
          privileges: 'grant,manage,use',
          children: null,
          allLeafs: 0,
          innerId: null,
          isPlugin: false
        },
        currentDsField: [],
        childrenDs: [],
        unionToParent: { unionType: 'left', unionFields: [{ parentField: {}, currentField: {} }] },
        allChildCount: 0
      }
    ],
    unionToParent: { unionType: 'left', unionFields: [] },
    allChildCount: 1
  }
})
</script>

<template>
  <div v-if="unionParam.type" style="height: calc(100% - 55px); overflow-y: auto">
    <div class="field-style">
      <div class="fields">
        <p :title="unionParam.parent.currentDs.name">
          {{ unionParam.parent.currentDs.name }}
        </p>
        <union-field-list
          :field-list="parentField"
          :node="unionParam.parent"
          @checkedFields="changeParentFields"
        />
      </div>
      <div class="fields">
        <p :title="unionParam.parent.currentDs.name">
          {{ unionParam.node.currentDs.name }}
        </p>
        <union-field-list
          :field-list="nodeField"
          :node="unionParam.node"
          @checkedFields="changeNodeFields"
        />
      </div>
    </div>
    <union-item-edit
      :parent-field-list="parentField"
      :node-field-list="nodeField"
      :union-param="unionParam"
    />
  </div>
</template>

<style lang="less" scoped>
.field-style {
  height: 430px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  margin-bottom: 36px;
}
.fields {
  box-sizing: border-box;
  padding: 16px;
  width: 50%;

  & > p {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    margin: 0;
    margin-bottom: 16px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    width: 100%;
    color: var(--deTextPrimary, #1f2329);
  }
  &:nth-child(1) {
    border-right: 1px solid var(--deCardStrokeColor, #dee0e3);
  }
}
</style>
