<template>
  <el-col>
    <el-row style="margin-top: 5px">
      <el-row>
        <el-input
          v-model="state.templateFilterText"
          :placeholder="t('visualization.filter_keywords')"
          size="mini"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="state.defaultExpandedKeys"
          :data="templateList"
          node-key="id"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-click="nodeClick"
        >
          <template #default="{ data }">
            <span class="custom-tree-node">
              <span style="display: flex; flex: 1 1 0%; width: 0px">
                <span v-if="data.nodeType === 'template'">
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span v-if="data.nodeType === 'folder'">
                  <i class="el-icon-folder" />
                </span>
                <span
                  :title="data.name"
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  >{{ data.name }}</span
                >
              </span>
            </span>
          </template>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script setup lang="ts">
import { findOne } from '@/api/template'
import { useI18n } from '@/hooks/web/useI18n'
import { reactive } from 'vue'
const { t } = useI18n()
const emits = defineEmits(['showCurrentTemplateInfo'])

const props = defineProps({
  templateList: {
    type: Array,
    default: function () {
      return []
    }
  }
})

const state = reactive({
  templateFilterText: '',
  defaultExpandedKeys: [],
  currentTemplateShowList: []
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.label.indexOf(value) !== -1
}

const nodeClick = (data, node) => {
  findOne(data.id).then(res => {
    emits('showCurrentTemplateInfo', res.data)
  })
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
