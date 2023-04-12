<script lang="ts" setup>
import { PropType, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
export interface Field {
  id: number
  deType: number
  name: string
}
type UnionType = 'left' | 'right' | 'inner'

const unionTypeFromParent = ref('left')
const { t } = useI18n()
const props = defineProps({
  unionType: {
    type: String as PropType<UnionType>,
    default: 'left'
  },
  parentFieldList: {
    type: Array as PropType<Field[]>,
    default: () => []
  },
  nodeFieldList: {
    type: Array as PropType<Field[]>,
    default: () => []
  },
  unionParam: {
    type: Object,
    default: () => ({})
  }
})
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const unionOptions = [
  { label: t('dataset.left_join'), value: 'left' },
  { label: t('dataset.right_join'), value: 'right' },
  { label: t('dataset.inner_join'), value: 'inner' }
]

const init = () => {
  unionTypeFromParent.value = props.unionType
  if (props.unionParam.node.unionToParent.unionFields.length < 1) {
    addUnion()
  }
}
const addUnion = () => {
  const item = {
    parentField: {},
    currentField: {}
  }
  emit('changeUnionFields', item)
}
const removeUnionItem = index => {
  emit('changeUnionFields', index)
}

init()

const emit = defineEmits(['changeUnionFields', 'changeUnionType'])
</script>

<template>
  <div class="union-container">
    <div class="union-header">
      {{ t('dataset.union_relation') }}
      <div class="union-header-operator">
        <el-select
          v-model="unionTypeFromParent"
          class="union-selector"
          @change="emit('changeUnionType')"
        >
          <template #prefix>
            <el-icon>
              <Icon :name="`${unionType || 'no'}-join`"></Icon>
            </el-icon>
          </template>
          <el-option
            v-for="item in unionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" class="union-add" @click="addUnion">
          <template #icon>
            <el-icon>
              <Icon name="icon_add_outlined"></Icon>
            </el-icon>
          </template>
          {{ t('dataset.add_union_field') }}
        </el-button>
      </div>
    </div>
    <div class="union-body">
      <div class="union-body-header">
        <span class="column" :title="unionParam.parent.currentDs.name">{{
          unionParam.parent.currentDs.name
        }}</span>
        <span class="column" :title="unionParam.node.currentDs.name">{{
          unionParam.node.currentDs.name
        }}</span>
        <span class="column-last">{{ t('dataset.operator') }}</span>
      </div>
      <div class="union-body-container">
        <div
          v-for="(field, index) in unionParam.node.unionToParent.unionFields"
          :key="index"
          class="union-body-item"
        >
          <!--左侧父级field-->
          <span class="column">
            <el-select
              v-model="field.parentField.id"
              :placeholder="t('dataset.pls_slc_union_field')"
              filterable
              clearable
              class="select-field"
            >
              <el-option
                v-for="item in parentFieldList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <el-icon>
                  <Icon :name="`field_${fieldType(item.deType)}`"></Icon>
                </el-icon>
                <span>
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>
          </span>
          <el-icon>
            <Icon name="join-join"></Icon>
          </el-icon>
          <!--右侧孩子field-->
          <span class="column">
            <el-select
              v-model="field.currentField.id"
              :placeholder="t('dataset.pls_slc_union_field')"
              filterable
              clearable
              class="select-field"
            >
              <el-option
                v-for="item in nodeFieldList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <el-icon>
                  <Icon :name="`field_${fieldType(item.deType)}`"></Icon>
                </el-icon>
                <span>
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>
          </span>

          <span class="column-last">
            <el-button
              :disabled="
                unionParam.node.unionToParent.unionFields &&
                unionParam.node.unionToParent.unionFields.length === 1
              "
              type="text"
              icon="el-icon-delete"
              @click="removeUnionItem(index)"
            />
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.union-container {
  height: 275px;
  font-family: PingFang SC;
}
.union-header {
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: space-between;
  margin-bottom: 8px;
  color: var(--deTextPrimary, #1f2329);
  font-size: 16px;
  font-weight: 500;
}
.union-header-operator {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  position: relative;

  .select-svg-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    z-index: 2;
    transform: translateY(-50%);
  }
}
.union-selector {
  width: 180px;
  ::v-deep.el-input__inner {
    padding-left: 32px;
  }
}
.union-add {
  margin-left: 12px;
}
.union-body {
  height: 240px;
  width: 100%;
}
.union-body-header {
  height: 46px;
  align-items: center;
  justify-content: space-between;
  display: flex;
  font-size: 14px;
  font-weight: 500;
  color: var(--deTextSecondary, #646a73);
}
.union-body-header .column {
  width: 336px;
  display: inline-block;
  margin-right: 9px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin-left: 10px;
}
.union-body-header .column-last {
  width: 40px;
  text-align: center;
}
.union-body-container {
  height: 180px;
  overflow-y: auto;
}
.select-field {
  width: 352px;
  display: inline-block;
}
.union-body-item {
  height: 32px;
  align-items: center;
  justify-content: space-between;
  display: flex;
  margin-bottom: 10px;
}
.union-body-item .column {
  width: 352px;
  display: inline-block;
  margin-right: 5px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.union-body-item .column-last {
  width: 40px;
  text-align: center;
  ::v-deep i {
    font-size: 16px;
    color: var(--deTextSecondary, #646a73);
  }
}
</style>
