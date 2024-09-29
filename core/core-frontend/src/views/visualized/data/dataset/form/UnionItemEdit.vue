<script lang="ts" setup>
import noJoin from '@/assets/svg/no-join.svg'
import icon_fullAssociation from '@/assets/svg/icon_full-association.svg'
import icon_intersect from '@/assets/svg/icon_intersect.svg'
import icon_leftAssociation from '@/assets/svg/icon_left-association.svg'
import icon_rightAssociation from '@/assets/svg/icon_right-association.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import joinJoin from '@/assets/svg/join-join.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { PropType, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { Field } from '@/api/chart'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'
const unionTypeFromParent = ref('left')
const { t } = useI18n()
const iconName = {
  left: icon_leftAssociation,
  right: icon_rightAssociation,
  inner: icon_intersect,
  full: icon_fullAssociation
}
const props = defineProps({
  tableName: {
    type: String,
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
  node: {
    type: Object,
    default: () => ({})
  }
})

const unionOptions = [
  { label: t('dataset.left_join'), value: 'left' },
  { label: t('dataset.right_join'), value: 'right' },
  { label: t('dataset.inner_join'), value: 'inner' },
  { label: t('dataset.full_join'), value: 'full' }
]

const init = () => {
  unionTypeFromParent.value = props.node.unionType
  if (props.node.unionFields.length < 1) {
    addUnion()
  }
}
const emit = defineEmits(['changeUnionFields', 'changeUnionType'])
const addUnion = () => {
  emit('changeUnionFields')
}
const removeUnionItem = index => {
  emit('changeUnionFields', index)
}

init()
</script>

<template>
  <div class="union-container">
    <div class="union-header">
      {{ t('dataset.union_relation') }}
      <div class="union-header-operator">
        <el-select
          v-model="unionTypeFromParent"
          class="union-selector"
          @change="emit('changeUnionType', unionTypeFromParent)"
        >
          <template #prefix>
            <el-icon>
              <Icon
                ><component
                  class="svg-icon"
                  :is="iconName[unionTypeFromParent] || noJoin"
                ></component
              ></Icon>
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
              <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
            </el-icon>
          </template>
          {{ t('dataset.add_union_field') }}
        </el-button>
      </div>
    </div>
    <div class="union-body">
      <div class="union-body-header">
        <span class="column" :title="tableName">{{ tableName }}</span>
        <span class="column" :title="node.tableName">{{ node.tableName }}</span>
      </div>
      <div class="union-body-container">
        <div v-for="(field, index) in node.unionFields" :key="index" class="union-body-item">
          <!--左侧父级field-->
          <span class="column">
            <el-select
              v-model="field.parentField"
              :placeholder="t('dataset.pls_slc_union_field')"
              filterable
              value-key="originName"
              clearable
              class="select-field"
            >
              <el-option
                v-for="item in parentFieldList"
                :key="item.originName"
                :label="item.name"
                :value="item"
              >
                <el-icon>
                  <Icon
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldType[item.deType]}`"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                <span>
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>
          </span>
          <el-icon>
            <Icon name="join-join"><joinJoin class="svg-icon" /></Icon>
          </el-icon>
          <!--右侧孩子field-->
          <span class="column">
            <el-select
              v-model="field.currentField"
              :placeholder="t('dataset.pls_slc_union_field')"
              filterable
              clearable
              value-key="originName"
              class="select-field"
            >
              <el-option
                v-for="item in nodeFieldList"
                :key="item.originName"
                :label="item.name"
                :value="item"
              >
                <el-icon>
                  <Icon
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldType[item.deType]}`"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                <span>
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>
          </span>

          <span class="column-last">
            <el-button
              v-if="node.unionFields && node.unionFields.length > 1"
              text
              @click="removeUnionItem(index)"
            >
              <template #icon>
                <Icon name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon"
                /></Icon>
              </template>
            </el-button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.union-container {
  height: 275px;
  font-family: var(--de-custom_font, 'PingFang');
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
  :deep(.ed-select__prefix--light) {
    border-right: none;
    font-size: 22px;
    padding: 0;
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
  height: 22px;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-family: var(--de-custom_font, 'PingFang');
  font-style: normal;
  font-weight: 400;
  margin: 20px 0 8px 0;
  color: var(--deTextSecondary, #1f2329);
}
.union-body-header .column {
  width: 364px;
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  &:nth-child(2) {
    margin-left: 37px;
  }
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
  width: 364px;
  display: inline-block;
}
.union-body-item {
  height: 32px;
  align-items: center;
  display: flex;
  margin-bottom: 8px;
  font-size: 18px;
  & > .ed-icon {
    margin: 0 9px;
  }
}
.union-body-item .column {
  width: 364px;
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.union-body-item .column-last {
  margin-left: auto;

  :deep(.ed-icon) {
    font-size: 16px;
    color: var(--deTextSecondary, #646a73);
  }
}
</style>
