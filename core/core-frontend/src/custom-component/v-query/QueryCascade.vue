<script lang="ts" setup>
import icon_info_colorful from '@/assets/svg/icon_info_colorful.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import joinJoin from '@/assets/svg/join-join.svg'
import { ref, shallowRef } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { listByDsIds } from '@/api/dataset'
import { cloneDeep } from 'lodash-es'
interface Cascade {
  datasetId: string
  name: string
  queryId: string
  deType: string
  fieldId: string
}

type cascadeMap = Record<string, Cascade>
const { t } = useI18n()
let deTypeMap = shallowRef({})
const emits = defineEmits(['saveCascade'])

const dialogVisible = ref(false)
const handleBeforeClose = () => {
  dialogVisible.value = false
}
const cascadeList = ref([])
const optionsMap = shallowRef({})
const datasetMap = shallowRef([])
const cancelClick = () => {
  handleBeforeClose()
}

const confirmClick = () => {
  const { isError, arr } = setCascadeArrBack()
  if (isError) {
    ElMessage.error(t('v_query.cannot_be_empty'))
    return
  }
  emits('saveCascade', arr)
  handleBeforeClose()
}

const setCascadeArrBack = () => {
  let isError = false
  const arr = cloneDeep(cascadeList.value)
  arr.forEach(ele => {
    ele.forEach(item => {
      if (!item.placeholder && !item.fieldId) {
        isError = true
      }
      if (!item.datasetId) {
        isError = true
      }
      item.selectValue = []
    })
  })

  return {
    arr,
    isError
  }
}

const init = (cascadeMap: cascadeMap, arr) => {
  cascadeList.value = cloneDeep(arr)
  datasetMap.value = Object.values(cascadeMap).map(ele => ({
    label: ele.name,
    deType: ele.deType,
    value: `${ele.datasetId}--${ele.queryId}--${ele.fieldId}`
  }))
  let obj = {}
  Object.values(cascadeMap).forEach(ele => {
    obj[`${ele.datasetId}--${ele.queryId}--${ele.fieldId}`] = ele.deType
  })
  deTypeMap.value = obj
  listByDsIds(datasetMap.value.map(ele => ele.value.split('--')[0]))
    .then(res => {
      optionsMap.value = res
    })
    .finally(() => {
      dialogVisible.value = true
    })
}

const disabledDatasetId = shallowRef([])

const visibleChange = (val, index, idx) => {
  let topId = ''
  let topIdArr = []
  let bottomId = ''
  let bottomIdArr = []
  for (let i in cascadeList.value[index]) {
    if (i > idx) {
      if (cascadeList.value[index][i].datasetId && !bottomId) {
        bottomId = cascadeList.value[index][i].datasetId
      }
      continue
    }
    if (cascadeList.value[index][i].datasetId) {
      topId = cascadeList.value[index][i].datasetId
    }
    if (i === idx) {
      topId = (cascadeList.value[index][idx - 1] || {}).datasetId
    }
  }

  cascadeList.value.forEach(ele => {
    let tentativeTopArr = []
    let tentativeBottomArr = []
    for (let i in ele) {
      if (topIdArr[topIdArr.length - 1] === tentativeTopArr || bottomId === ele[i].datasetId) {
        if (bottomId === ele[i].datasetId) {
          bottomIdArr.push(tentativeBottomArr)
        }

        if (bottomIdArr[bottomIdArr.length - 1] === tentativeBottomArr) {
          tentativeBottomArr.push(ele[i].datasetId)
        }
        continue
      }
      if (ele[i].datasetId) {
        tentativeTopArr.push(ele[i].datasetId)
      }
      if (topId === ele[i].datasetId) {
        topIdArr.push(tentativeTopArr)
      }
    }
  })

  if (val) {
    disabledDatasetId.value = [...new Set([...topIdArr.flat(), ...bottomIdArr.flat()])].filter(
      ele => !!ele
    )
  }
}

const addCascadeItem = item => {
  item.push({
    datasetId: '',
    fieldId: '',
    placeholder: item.length ? '' : t('v_query.the_first_level'),
    id: guid()
  })
}

const setPlaceholder = () => {
  cascadeList.value.forEach(ele => {
    ele.forEach((item, idx) => {
      if (idx) {
        item.placeholder = ''
      }
      if (
        item &&
        ele[idx - 1] &&
        item.datasetId &&
        item.datasetId.split('--')[0] === ele[idx - 1].datasetId.split('--')[0]
      ) {
        item.placeholder = t('v_query.configure_cascaded_fields')
        item.fieldId = ''
      }
    })
  })
}

const deleteCascade = (idx, item) => {
  item.splice(idx, 1)
  item[0].fieldId = ''
  item[0].placeholder = t('v_query.the_first_level')
  setPlaceholder()
}

const deleteCascadeBlock = idx => {
  cascadeList.value.splice(idx, 1)
}

const addCascadeBlock = () => {
  const arr = []
  addCascadeItem(arr)
  cascadeList.value.push(arr)
}

const indexNumCascade = [
  t('visualization.number1'),
  t('visualization.number2'),
  t('visualization.number3'),
  t('visualization.number4'),
  t('visualization.number5')
]

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    class="query-condition-cascade"
    v-model="dialogVisible"
    width="900px"
    @click.stop
    :before-close="handleBeforeClose"
    @mousedown.stop
    @mousedup.stop
  >
    <template #title>
      <div class="title">
        {{ t('v_query.condition_cascade_configuration')
        }}<span style="margin-left: 8px" class="tip">{{ t('v_query.not_reverse_cascade') }}</span>
      </div>
    </template>
    <div class="content">
      <el-icon style="font-size: 16px">
        <Icon name="icon_info_colorful"><icon_info_colorful class="svg-icon" /></Icon>
      </el-icon>
      {{ t('v_query.must_be_met') }}<br />
      {{ t('v_query.select_data_set') }}<br />
    </div>
    <el-button text @click="addCascadeBlock">
      <template #icon>
        <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
      </template>
      {{ t('v_query.add_cascade_configuration') }}
    </el-button>
    <div class="cascade-content" v-for="(item, index) in cascadeList" :key="index">
      <div style="display: flex; align-items: center; justify-content: space-between">
        <el-button :disabled="item.length === 2" text @click="addCascadeItem(item)">
          <template #icon>
            <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
          </template>
          {{ t('v_query.add_cascade_condition') }}
        </el-button>
        <el-button @click="deleteCascadeBlock(index)" class="cascade-delete-block" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"
              ><icon_deleteTrash_outlined class="svg-icon"
            /></Icon>
          </template>
        </el-button>
      </div>
      <div class="cascade-item">
        <div class="label">{{ t('v_query.query_condition_level') }}</div>
        <div class="item-name">{{ t('v_query.select_query_condition') }}</div>
        <div class="cascade-icon"></div>
        <div class="item-field">{{ t('v_query.select_cascaded_field') }}</div>
      </div>
      <div class="cascade-item" v-for="(ele, idx) in item" :key="ele.id">
        <div class="label">{{ t('v_query.level_1', { msg: indexNumCascade[idx] }) }}</div>
        <div class="item-name">
          <el-select
            @visible-change="val => visibleChange(val, index, idx)"
            v-model="ele.datasetId"
            @change="setPlaceholder"
            style="width: 300px"
          >
            <el-option
              v-for="itx in datasetMap"
              :key="itx.value"
              :label="itx.label"
              :value="itx.value"
              :disabled="
                (disabledDatasetId.includes(itx.value) &&
                  item.map(ele => ele.datasetId).includes(itx.value)) ||
                (!!ele.datasetId && deTypeMap[ele.datasetId] !== itx.deType)
              "
            />
          </el-select>
        </div>
        <div class="cascade-icon">
          <el-icon>
            <Icon name="join-join"><joinJoin class="svg-icon" /></Icon>
          </el-icon>
        </div>
        <div class="item-field">
          <el-select
            :placeholder="ele.placeholder"
            :disabled="!!ele.placeholder"
            v-model="ele.fieldId"
            style="width: 300px"
          >
            <el-option
              v-for="item in optionsMap[ele.datasetId.split('--')[0]]"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        <el-button v-show="idx !== 0" @click="deleteCascade(idx, item)" class="cascade-delete" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"
              ><icon_deleteTrash_outlined class="svg-icon"
            /></Icon>
          </template>
        </el-button>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelClick">{{ t('chart.cancel') }} </el-button>
        <el-button @click="confirmClick" type="primary">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.query-condition-cascade {
  .title {
    .tip {
      font-size: 12px;
      color: #646a73;
    }
  }

  .content {
    height: 62px;
    width: 852px;
    border-radius: 4px;
    background: #e1eaff;
    position: relative;
    padding: 9px 0 9px 40px;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;

    .ed-icon {
      position: absolute;
      top: 10.6px;
      left: 16px;
      font-size: 14px;
      color: var(--ed-color-primary, #3370ff);
    }

    margin-bottom: 16px;
  }

  .cascade-content {
    box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    border: 1px solid #e4e7ed;
    padding: 24px;
    padding-top: 8px;
    margin-top: 8px;

    .cascade-item {
      display: flex;
      align-items: center;
      width: 100%;
      height: 40px;
      .label {
        width: 100px;
      }

      .item-name {
        width: 300px;
      }

      .cascade-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        font-size: 20px;
      }

      .item-field {
        width: 300px;
      }
    }

    .cascade-delete-block,
    .cascade-delete {
      width: 40px;
      font-size: 20px;
      color: #646a73;
      margin-left: 20px;
    }
  }
}
</style>
