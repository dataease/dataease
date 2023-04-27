<script lang="ts">
export default {
  name: 'logic-relation'
}
</script>
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { PropType, computed, ref } from 'vue'
import FilterFiled from './FilterFiled.vue'
import type { Item } from './FilterFiled.vue'
import { clone } from 'lodash'
export type Logic = 'or' | 'and'
export type Relation = {
  child?: Relation[]
  logic: Logic
  x: number
} & Item
const { t } = useI18n()

const props = defineProps({
  relationList: {
    type: Array as PropType<Relation[]>,
    default: () => []
  },
  x: {
    type: Number,
    default: 0
  },
  logic: {
    type: String as PropType<Logic>,
    default: 'or'
  }
})

const marginLeft = computed(() => {
  return {
    marginLeft: props.x ? '20px' : 0
  }
})

const emits = defineEmits([
  'addCondReal',
  'changeAndOrDfs',
  'update:logic',
  'changeRelationList',
  'removeRelationList',
  'del'
])

const authRelationList = ref<Relation[]>([])

const initList = () => {
  authRelationList.value = clone(props.relationList)
  console.log('props.relationList', props.relationList.length)
}
const handleCommand = type => {
  emits('update:logic', type)
  emits('changeAndOrDfs', type)
}
const removeRelationList = index => {
  emits('changeRelationList', index)
}
const addCondReal = type => {
  const obj =
    type === 'condition'
      ? {
          fieldId: '',
          value: '',
          enumValue: '',
          term: '',
          filterType: 'logic',
          name: '',
          deType: ''
        }
      : { child: [], logic: props.logic === 'or' ? 'and' : 'or' }

  authRelationList.value.push(obj as Relation)
  emits('changeRelationList', authRelationList.value.length - 1, obj)
}

const updateItem = (val: Relation, index: number) => {
  authRelationList.value[index] = val
  emits('changeRelationList', index, val)
}

const changeRelationList = (index, idx, obj) => {
  if (obj) {
    authRelationList.value[index].child.push(obj as Relation)
    return
  }

  if (!isNaN(idx)) {
    authRelationList.value[index].child.splice(idx, 1, obj as Relation)
  } else {
    authRelationList.value[index].child.splice(idx, 1)
    emits('changeRelationList', index, obj)
    return
  }

  emits('changeRelationList', index, obj)
}

const add = (type, child, logic) => {
  const obj =
    type === 'condition'
      ? {
          fieldId: '',
          value: '',
          enumValue: '',
          term: '',
          filterType: 'logic',
          name: '',
          deType: ''
        }
      : { child: [], logic }

  authRelationList.value.push(obj as Relation)
  emits('changeRelationList', authRelationList.value.length - 1, obj)
}
const del = (arr, index) => {
  authRelationList.value[index].child = arr
  emits('changeRelationList', authRelationList.value.length - 1, arr)
}

const delItem = index => {
  authRelationList.value.splice(index, 1)
  emits('del', index, authRelationList.value)
}

defineExpose({
  initList
})
</script>

<template>
  <div class="logic" :style="marginLeft">
    <div class="logic-left">
      <div class="operate-title">
        <span style="color: #bfbfbf" class="mrg-title" v-if="x">
          {{ logic === 'or' ? 'OR' : 'AND' }}
        </span>
        <el-dropdown @command="handleCommand" trigger="click" v-else>
          <span style="color: rgba(0 0 0 / 65%)" class="mrg-title">
            {{ logic === 'or' ? 'OR' : 'AND' }}
            <el-icon>
              <Icon name="icon_down_outlined"></Icon>
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="and">AND</el-dropdown-item>
              <el-dropdown-item command="or">OR</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <span class="operate-icon" v-if="x">
        <el-icon @click="emits('removeRelationList')">
          <Icon name="icon_delete-trash_outlined"></Icon>
        </el-icon>
      </span>
    </div>
    <div class="logic-right">
      <template :key="index" v-for="(item, index) in authRelationList">
        <logic-relation
          v-if="item.child"
          :x="item.x"
          @del="(_, arr) => del(arr, index)"
          @addCondReal="(type, logic) => add(type, item.child, logic)"
          :logic="item.logic"
          @changeRelationList="(idx, obj) => changeRelationList(index, idx, obj)"
          @removeRelationList="removeRelationList(index)"
          :relationList="item.child"
        >
        </logic-relation>
        <filter-filed
          v-else
          :item="item"
          @update:item="val => updateItem(val, index)"
          @del="delItem(index)"
          :index="index"
        ></filter-filed>
      </template>
      <div class="logic-right-add">
        <button @click="addCondReal('condition')" class="operand-btn">
          + {{ t('auth.add_condition') }}
        </button>
        <button v-if="x < 2" @click="addCondReal('relation')" class="operand-btn">
          + {{ t('auth.add_relationship') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.logic {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
  width: 100%;

  .logic-left {
    box-sizing: border-box;
    width: 48px;
    display: flex;
    position: relative;
    align-items: center;
    z-index: 10;

    .operate-title {
      font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
      word-wrap: break-word;
      box-sizing: border-box;
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      display: inline-block;
      white-space: nowrap;
      margin: 0;
      padding: 0;
      width: 65px;
      background-color: #f8f8fa;
      line-height: 28px;
      position: relative;
      z-index: 1;
      height: 28px;

      .mrg-title {
        text-align: left;
        box-sizing: border-box;
        position: relative;
        display: block;
        margin-left: 11px;
        margin-right: 11px;
        line-height: 28px;
        height: 28px;
      }
    }

    &:hover {
      .operate-icon {
        display: inline-block;
      }

      .operate-title {
        .mrg-title {
          margin: 0 5px;
        }
      }
    }

    .operate-icon {
      width: 40px;
      height: 28px;
      line-height: 28px;
      background-color: #f8f8fa;
      z-index: 1;
      display: none;

      i {
        font-size: 12px;
        font-style: normal;
        display: unset;
        padding: 5px 3px;
        cursor: pointer;
        position: relative;
        z-index: 10;
      }
    }
  }

  .logic-right-add {
    display: flex;
    height: 41.4px;
    align-items: center;
    padding-left: 26px;

    .operand-btn {
      box-sizing: border-box;
      font-weight: 400;
      text-align: center;
      box-shadow: 0 2px 0 rgba(0, 0, 0, 0.015);
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
      outline: 0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
      -webkit-appearance: button;
      cursor: pointer;
      height: 28px;
      padding: 0 10px;
      margin-right: 10px;
      font-size: 12px;
      color: #246dff;
      background: #fff;
      border: 1px solid #246dff;
      border-radius: 2px;
    }
  }
}
</style>
