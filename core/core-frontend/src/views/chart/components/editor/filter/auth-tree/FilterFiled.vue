<script lang="ts" setup>
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, inject, computed, watch, onBeforeMount, toRefs, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { multFieldValuesForPermissions } from '@/api/dataset'
import {
  textOptions,
  dateOptions,
  valueOptions,
  textOptionsForSysParams,
  sysParamsIlns,
  fieldEnums
} from '@/views/visualized/data/dataset/options.js'
import { iconFieldMap } from '@/components/icon-group/field-list'
export interface Item {
  term: string
  fieldId: string
  filterType: string
  deType: number
  enumValue: string
  name: string
  value: number
  timeValue: string
}

type Props = {
  index: number
  item: Item
}

const props = withDefaults(defineProps<Props>(), {
  index: 0,
  item: () => ({
    term: '',
    fieldId: '',
    filterType: '',
    deType: 0,
    enumValue: '',
    name: '',
    value: null,
    timeValue: ''
  })
})

const { t } = useI18n()
const enumInput = ref()
const deElDropdownMenuFixed = ref()
const showDel = ref(false)
const keywords = ref('')
const activeName = ref('')
const filterFiled = ref('')
const enumList = ref([])
const showTextArea = ref()
const keydownCanceled = ref(false)
const checklist = ref([])
const filterList = ref([])
const textareaValue = ref('')

const { item } = toRefs(props)

const getAuthTargetType = inject('getAuthTargetType')
const filedList = inject('filedList')

const checkListWithFilter = computed(() => {
  if (!filterFiled.value) return enumList.value
  return enumList.value.filter(ele => ele.includes(filterFiled.value))
})
const checkResult = computed(() => {
  return checklist.value.join(',')
})

const sysParamsIln = computed(() => {
  if (['in', 'not in'].includes(item.value.term)) {
    return [
      {
        value: '${sysParams.roles}',
        label: t('auth.sysParams_type.role')
      }
    ]
  } else {
    return sysParamsIlns
  }
})
const operators = computed(() => {
  const { deType } = item.value
  if (authTargetType.value === 'sysParams') {
    return textOptionsForSysParams
  }
  if ([0, 5, 7].includes(deType)) {
    return textOptions
  } else if (deType === 1) {
    return dateOptions
  } else {
    return valueOptions
  }
})
const dimensions = computed(() => {
  if (!keywords.value) return computedFiledList.value
  return computedFiledList.value.filter(ele => ele.name.includes(keywords.value))
})
const computedFiledList = computed(() => {
  return Object.values(filedList.value || {})
})

watch(checkResult, () => {
  cancelfixValue()
})

const authTargetType = ref('')

watch(
  () => getAuthTargetType?.authTargetType,
  value => {
    if (authTargetType.value === value || !value) return
    authTargetType.value = value
  },
  {
    immediate: true
  }
)

onBeforeMount(() => {
  initNameEnumName()
  filterListInit(item.value.deType)
})

const confirm = () => {
  enumInput.value.$el.click()
}
const initNameEnumName = () => {
  const { name, enumValue, fieldId } = item.value
  const arr = enumValue.trim() ? enumValue.split(',') : []
  if (!name && fieldId) {
    checklist.value = arr
  }
  if (!name && !fieldId) return
  initEnumOptions()
  activeName.value = item.value.name
  checklist.value = arr
}
const cancelKeyDow = () => {
  if (!showTextArea.value && !keydownCanceled.value) {
    keydownCanceled.value = true
  }
  showTextArea.value = true
}
const filterTypeChange = () => {
  item.value.term = ''
  item.value.value = null
  initEnumOptions()
}

const filterTypeChangeTime = () => {
  item.value.term = ''
  item.value.timeValue = null
}
const initEnumOptions = () => {
  // 查找枚举值
  if (authTargetType.value === 'sysParams') {
    return
  }
  const { deType, filterType, fieldId } = item.value
  // 查找枚举值
  if (filterType === 'enum' && [0, 5, 7].includes(deType)) {
    multFieldValuesForPermissions({ fieldIds: [fieldId] }).then(res => {
      enumList.value = optionData(res.data)
    })
  }
}

const optionData = data => {
  if (!data) return null
  return data.filter(item => !!item)
}
const cancel = () => {
  item.value.name = activeName.value || ''
}
const cancelfixValue = () => {
  item.value.enumValue = checkResult.value || ''
}
const delChecks = idx => {
  checklist.value.splice(idx, 1)
}
const selectItem = ({ name, id, deType }) => {
  activeName.value = name
  Object.assign(item.value, {
    fieldId: id,
    name,
    deType,
    filterType: 'logic',
    enumValue: '',
    value: '',
    term: '',
    timeValue: ''
  })
  filterListInit(deType)
  checklist.value = []
}

const filterListInit = deType => {
  filterList.value = [
    {
      value: 'logic',
      label: t('deDataset.logic_filter')
    },
    {
      value: 'enum',
      label: t('deDataset.enum_filter')
    }
  ]
  if (authTargetType.value === 'sysParams' || [1, 2, 3].includes(deType)) {
    filterList.value = [filterList.value[0]]
  }
}

const clearAll = () => {
  checklist.value = []
}
const selectAll = () => {
  checkListWithFilter.value.forEach(ele => {
    if (!checklist.value.includes(ele)) {
      checklist.value.push(ele)
    }
  })
}
const addFields = () => {
  const list = textareaValue.value.split('\n').reduce((pre, next) => {
    const str = next.trim()
    if (!str) return pre
    pre.add(str)
    return pre
  }, new Set([]))
  if (list.size) {
    checklist.value = [...new Set([...checklist.value, ...Array.from(list)])]
  }
  showTextArea.value = false
}
const checkItem = i => {
  const index = checklist.value.findIndex(ele => ele === i)
  if (index === -1) {
    checklist.value.push(i)
  } else {
    delChecks(index)
  }
}

const emits = defineEmits(['update:item', 'del'])
</script>

<template>
  <div class="white-nowrap">
    <div class="filed" @mouseover="showDel = true" @mouseleave="showDel = false">
      <span class="filed-title">{{ t('auth.filter_fields') }}</span>

      <el-dropdown trigger="click" :hide-on-click="false">
        <el-input
          :placeholder="t('auth.select_filter_fields')"
          v-model="item.name"
          size="small"
          @input="cancel"
        >
        </el-input>
        <template #dropdown>
          <el-dropdown-menu class="de-el-dropdown-menu">
            <el-input
              @keydown.stop
              :placeholder="t('auth.enter_keywords')"
              size="small"
              v-model="keywords"
            >
              <template #prefix>
                <el-icon>
                  <Icon name="icon_search-outline_outlined"
                    ><icon_searchOutline_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </template>
            </el-input>
            <ul class="dimension">
              <li
                @click="selectItem(ele)"
                :style="{
                  backgroundColor: activeName === ele.name ? '#f0f7ff' : ''
                }"
                :key="ele.id"
                v-for="ele in dimensions"
              >
                <el-icon>
                  <Icon :className="`field-icon-${fieldEnums[ele.deType]}`"
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldEnums[ele.deType]}`"
                      :is="iconFieldMap[fieldEnums[ele.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                <span>{{ ele.name }}</span>
              </li>
            </ul>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div class="white-nowrap flex-align-center" style="position: relative" v-if="item.fieldId">
        <span class="filed-title">{{ t('auth.screen_method') }}</span>
        <el-select
          size="small"
          @change="filterTypeChange"
          v-model="item.filterType"
          :placeholder="t('auth.select')"
        >
          <el-option
            v-for="ele in filterList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          >
          </el-option>
        </el-select>
        <span class="filed-title">{{ t('auth.fixed_value') }}</span>
        <template v-if="item.filterType === 'logic'">
          <el-select
            class="w100"
            size="small"
            v-model="item.term"
            :placeholder="t('auth.default_method')"
          >
            <el-option
              v-for="ele in operators"
              :key="ele.value"
              :label="t(ele.label)"
              :value="ele.value"
            >
            </el-option>
          </el-select>

          <template v-if="authTargetType === 'sysParams'">
            <el-select class="w70 mar5" size="small" v-model="item.value">
              <el-option
                v-for="itx in sysParamsIln"
                :key="itx.value"
                :label="t(itx.label)"
                :value="itx.value"
              >
              </el-option>
            </el-select>
          </template>
          <template
            v-else-if="
              [2, 3].includes(item.deType) &&
              !['null', 'empty', 'not_null', 'not_empty'].includes(item.term)
            "
          >
            <el-input-number
              class="w70 mar5"
              size="small"
              effect="plain"
              v-model="item.value"
              controls-position="right"
            ></el-input-number>
            <div class="bottom-line"></div>
          </template>
          <template v-else-if="!['null', 'empty', 'not_null', 'not_empty'].includes(item.term)">
            <el-tooltip
              class="item"
              v-if="item.deType === 1"
              effect="light"
              :content="item.timeValue"
              placement="top"
              ><el-input class="w70 mar5" size="small" v-model="item.timeValue"
            /></el-tooltip>
            <el-input v-else class="w70 mar5" size="small" v-model="item.value" />
            <div class="bottom-line"></div>
          </template>
        </template>

        <el-popover
          popper-class="de-el-dropdown-menu-fixed"
          v-else
          width="360px"
          trigger="click"
          ref="deElDropdownMenuFixed"
          :hide-on-click="false"
        >
          <template #reference>
            <el-input
              v-model="item.enumValue"
              ref="enumInput"
              size="small"
              @input="cancelfixValue"
              clearable
              @clear="clearAll"
            >
            </el-input>
          </template>
          <div class="de-panel clearfix">
            <div class="mod-left">
              <el-input :placeholder="t('auth.enter_keywords')" v-model="filterFiled"> </el-input>
              <ul class="infinite-list autochecker-list" style="height: 231px; overflow: auto">
                <li
                  :key="i"
                  v-for="i in checkListWithFilter"
                  class="infinite-list-item"
                  @click="checkItem(i)"
                >
                  <i class="el-icon-check" :style="{ opacity: checklist.includes(i) ? 1 : 0 }"></i>
                  <label>{{ i }}</label>
                  <span>+</span>
                </li>
              </ul>
              <el-button style="width: 100%" type="primary" @click="selectAll">
                {{ t('auth.select_all') }}
              </el-button>
            </div>
            <div class="mod-left right-de">
              <div class="right-top clearfix">
                {{ t('auth.added') }}{{ checklist.length }}
                <div class="right-btn">
                  <span @click="cancelKeyDow">
                    <i class="el-icon-edit"></i>
                    {{ t('auth.manual_input') }}
                  </span>
                  <transition name="el-zoom-in-top">
                    <div v-if="showTextArea" class="de-el-dropdown-menu-manu">
                      <div class="text-area">
                        <textarea
                          :placeholder="t('auth.please_fill')"
                          class="input"
                          v-model="textareaValue"
                        ></textarea>
                        <div class="text-area-btn">
                          <button type="button" @click="showTextArea = false" class="btn">
                            <span>{{ t('auth.close') }}</span>
                          </button>
                          <button type="button" @click="addFields" class="btn right-add">
                            <span>{{ t('auth.add') }}</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </transition>
                </div>
              </div>
              <ul class="infinite-list autochecker-list" style="overflow: auto">
                <li :key="i" v-for="(i, idx) in checklist" class="infinite-list-item">
                  <el-tooltip
                    class="item"
                    effect="light"
                    :content="i"
                    placement="top"
                    :open-delay="1000"
                  >
                    <label>{{ i }}</label>
                  </el-tooltip>
                  <el-icon @click="delChecks(idx)" style="color: #646a73">
                    <Icon><icon_deleteTrash_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </li>
              </ul>
              <div class="right-menu-foot">
                <div class="footer-left">&nbsp;</div>
                <el-button type="primary" @click="confirm">
                  {{ t('auth.sure') }}
                </el-button>
                <div class="footer-right">
                  <el-icon style="color: #646a73" @click="clearAll">
                    <Icon><icon_deleteTrash_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </div>
              </div>
            </div>
          </div>
        </el-popover>
      </div>
      <el-icon v-if="showDel" class="font12" @click="emits('del')">
        <Icon name="icon_delete-trash_outlined"
          ><icon_deleteTrash_outlined class="svg-icon"
        /></Icon>
      </el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.white-nowrap {
  white-space: nowrap;
}
.filed {
  width: fit-content;
  height: 41.4px;
  padding: 1px 3px 1px 10px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  margin-left: 20px;
  min-width: 200px;
  justify-content: left;
  position: relative;
  white-space: nowrap;
  :deep(.ed-input-number.is-controls-right .ed-input__wrapper) {
    padding-left: 1px !important;
    padding-right: 1px !important;
  }

  .filed-title {
    word-wrap: break-word;
    line-height: 28px;
    color: #7e7e7e;
    font-size: 12px;
    white-space: nowrap;
    box-sizing: border-box;
    margin-right: 5px;
    display: inline-block;
    min-width: 50px;
    text-align: right;
  }

  .font12 {
    font-size: 12px;
    margin: 0 10px;
    cursor: pointer;
  }

  .ed-input {
    width: 170px;
  }

  .w100.ed-select {
    width: 100px;
  }

  .w70 {
    width: 70px;
  }

  .mar5 {
    margin-left: -5px;
  }

  :deep(.ed-input-number__decrease:not(.is-disabled)),
  :deep(.ed-input-number__increase:not(.is-disabled)) {
    &:hover {
      z-index: 10;
      &::after {
        display: block;
      }
      & ~ .ed-input:not(.is-disabled) .ed-input__wrapper {
        box-shadow: 0 0 0 0 #000 inset !important;
      }
    }
  }
  :deep(.ed-input-number__decrease),
  :deep(.ed-input-number__increase) {
    width: 20px;
    height: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: height 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    display: none;
  }

  :deep(.ed-input-number__decrease:hover) {
    height: 16px;
    & + .ed-input-number__increase {
      height: 8px;
    }
  }
  :deep(.ed-input-number__increase:hover) {
    height: 16px;
    & + .ed-input-number__decrease {
      height: 8px;
    }
  }

  .bottom-line {
    font-family: var(--de-custom_font, 'PingFang');
    font-variant: tabular-nums;
    font-feature-settings: 'tnum';
    word-wrap: break-word;
    text-align: left;
    line-height: 28px;
    color: #7e7e7e;
    font-size: 12px;
    white-space: pre;
    box-sizing: border-box;
    height: 1px;
    background-color: #000;
    opacity: 0.3;
    position: absolute;
    right: 5px;
    bottom: 5px;
    width: 70px;
    z-index: 10;
  }

  :deep(.ed-input-number.is-controls-right .ed-input__inner) {
    padding-right: 20px;
  }

  .ed-input-number {
    line-height: 26px;
    height: 26px;
  }

  :deep(.ed-select) {
    :deep(.ed-input__suffix-inner) {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .ed-input__icon {
        height: auto;
      }
    }
  }

  :deep(.ed-input__wrapper) {
    background-color: #f8f8fa;
    border: none;
    border-radius: 0;
    box-shadow: none;
    height: 26px;
    font-family: var(--de-custom_font, 'PingFang');
    word-wrap: break-word;
    text-align: left;
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    list-style: none;
    user-select: none;
    cursor: pointer;
    line-height: 26px;
    box-sizing: border-box;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    opacity: 1;
  }
  :deep(.ed-select .ed-input.is-focus .ed-input__wrapper),
  :deep(.ed-select:hover:not(.ed-select--disabled) .ed-input__wrapper),
  :deep(.ed-select .ed-input__wrapper.is-focus) {
    box-shadow: none !important;
  }

  i {
    margin-left: 5px;
    color: #7e7e7e;
  }
}
.filed:hover {
  background-color: #e9eaef;
  :deep(.ed-input-number__decrease),
  :deep(.ed-input-number__increase) {
    display: flex;
  }
}
</style>

<style lang="less">
.de-el-dropdown-menu {
  .dimension {
    max-height: 200px;
    padding: 0;
    overflow-y: auto;
    li {
      list-style: none;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      white-space: nowrap;
      cursor: pointer;
      transition: color 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        border-color 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        background 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        padding 0.15s cubic-bezier(0.645, 0.045, 0.355, 1);
      position: relative;
      overflow: hidden;
      font-size: 12px;
      text-overflow: ellipsis;
      padding: 0 16px 0 28px;
      line-height: 32px;
      height: 32px;
      margin: 0;
      padding-left: 16px;
      color: rgba(0, 0, 0, 0.65);
      .ed-icon {
        margin-right: 5px;
      }
    }
    li:hover {
      color: #2e74ff;
      background-color: #f0f7ff;
    }
  }

  .ed-input {
    font-family: inherit;
    overflow: visible;
    box-sizing: border-box;
    margin: 0;
    font-variant: tabular-nums;
    list-style: none;
    font-feature-settings: 'tnum';
    display: inline-block;
    width: 100%;
    height: 28px;
    padding: 4px 7px;
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    line-height: 28px;
    background-color: #fff;
    background-image: none;
    transition: all 0.3s;
    touch-action: manipulation;
    text-overflow: ellipsis;
    position: relative;
    text-align: inherit;
    min-height: 100%;
    border: 0;
    border-radius: 0;
    padding-left: 26px;

    .ed-input__wrapper {
      box-shadow: none;
      border-bottom: 1px solid #e5e5e5;
      &:focus {
        box-shadow: 0 0 0 2px rgb(46 116 255 / 20%);
        border-right-width: 1px !important;
        outline: 0;
        border-color: none;
      }
    }
  }

  .ed-input {
    font-family: var(--de-custom_font, 'PingFang');
    box-sizing: border-box;
    margin: 0;
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    font-variant: tabular-nums;
    line-height: 1.5;
    list-style: none;
    font-feature-settings: 'tnum';
    position: relative;
    display: inline-block;
    width: 100%;
    text-align: start;
    padding: 0 6px;
  }
}

.de-el-dropdown-menu-fixed {
  padding: 0 !important;
  border: none !important;
  .de-panel {
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    box-sizing: border-box;
    position: relative;
    padding: 0;
    width: 360px;
    max-width: 600px;
    min-height: 30px;
    background-color: #fff;
    box-shadow: none;
    border: 1px solid rgba(0, 0, 0, 0.05);
    .mod-left {
      font-family: var(--de-custom_font, 'PingFang');
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      vertical-align: top;
      padding: 5px;
      width: 50%;
      height: 300px;
      float: left;
      box-sizing: border-box;
      .ed-input__wrapper {
        font-family: inherit;
        overflow: visible;
        box-sizing: border-box;
        margin: 0;
        position: relative;
        display: inline-block;
        color: rgba(0, 0, 0, 0.65);
        font-size: 12px;
        line-height: 28px;
        background-color: #fff;
        background-image: none;
        border-radius: 2px;
        transition: all 0.3s;
        -webkit-appearance: none;
        touch-action: manipulation;
        text-overflow: ellipsis;
        width: 100%;
        border-bottom: 1px solid hsla(0, 0%, 59%, 0.1);
        border: 1px solid hsla(0, 0%, 59%, 0.1);
        height: 30px;
        padding: 0 8px;
        outline: 0;

        &:focus {
          box-shadow: 0 0 0 2px rgb(46 116 255 / 20%);
          border-right-width: 1px !important;
          outline: 0;
          border-color: none;
        }
      }
    }

    .right-de {
      border-left: 1px solid hsla(0, 0%, 59%, 0.1);
    }
    .autochecker-list {
      font-family: var(--de-custom_font, 'PingFang');
      color: rgba(0, 0, 0, 0.65);
      box-sizing: border-box;
      width: 100%;
      overflow: hidden;
      overflow-y: auto;
      height: 221px;
      position: relative;
      padding: 0;

      li {
        direction: ltr;
        padding: 0 5px;
        text-overflow: ellipsis;
        overflow: hidden;
        color: #333;
        white-space: nowrap;
        list-style: none;
        line-height: 28px;
        height: 28px;
        width: 100%;
        position: relative;
        box-sizing: border-box;

        &:hover {
          background-color: #f8f8fa;
          color: #2153d4;
          opacity: 1;
          span {
            display: block;
          }
        }

        i {
          color: #333;
          font-size: 12px;
          cursor: pointer;
          vertical-align: top;
          line-height: 28px;
          height: 28px;
          display: inline-block;
          opacity: 0;
        }

        label {
          font-family: var(--de-custom_font, 'PingFang');
          font-size: 12px;
          direction: ltr;
          color: #333;
          box-sizing: border-box;
          touch-action: manipulation;
          width: 87%;
          height: 28px;
          line-height: 14px;
          padding: 8px 20px;
          cursor: pointer;
          display: inline-block;
          position: relative;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
        }

        span {
          display: none;
          position: absolute;
          width: 14px;
          height: 14px;
          line-height: 11px;
          top: 6px;
          right: 5px;
          font-size: 15px;
          cursor: pointer;
          background: #2153d4;
          color: #fff;
          text-align: center;
          border-radius: 999px;
        }
      }
    }

    .right-top {
      color: rgba(0, 0, 0, 0.65);
      text-align: left;
      box-sizing: border-box;
      zoom: 1;
      border-bottom: 1px solid #f8f8fa;
      height: 30px;
      width: 100%;
      font-size: 12px;
      line-height: 35px;
      text-overflow: ellipsis;
      white-space: nowrap;

      .right-btn {
        color: rgba(0, 0, 0, 0.65);
        font-size: 12px;
        box-sizing: border-box;
        position: relative;
        z-index: 10;
        float: right;
        cursor: pointer;
        line-height: 25px;
        padding: 0 5px;
        text-overflow: ellipsis;
        white-space: nowrap;
        background: rgba(70, 140, 255, 0.1);
        width: 75px;
      }
    }

    .right-menu-foot {
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      box-sizing: border-box;
      height: 30px;
      text-align: right;
      line-height: 30px;
      margin-top: 9px;
      border-top: 1px solid hsla(0, 0%, 59%, 0.1);

      .footer-left {
        box-sizing: border-box;
        float: left;
      }

      .footer-right {
        float: right;
        padding-left: 10px;
        cursor: pointer;
      }
    }
  }
}

.de-el-dropdown-menu-manu {
  padding: 0;
  margin: 5px 0;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 12px;
  position: absolute;
  top: 20px;
  width: 150px;
  box-sizing: border-box;
  right: 0;
  .text-area {
    box-sizing: border-box;
    height: 220px;
    position: relative;
    .input {
      touch-action: manipulation;
      overflow: auto;
      resize: vertical;
      box-sizing: border-box;
      position: relative;
      display: inline-block;
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      background-color: #fff;
      background-image: none;
      max-width: 100%;
      min-height: 28px;
      line-height: 1.5;
      vertical-align: bottom;
      transition: all 0.3s, height 0s;
      text-overflow: ellipsis;
      outline: 0;
      padding: 5px 5px 28px;
      width: 100%;
      height: 220px;
      border-radius: 0;
      border: none;
      box-shadow: 0 2px 6px 0 rgba(130, 150, 183, 0.72);
    }

    .text-area-btn {
      position: absolute;
      width: 100%;
      text-align: center;
      background: #fff;
      bottom: -1px;
      padding-bottom: 4px;

      .btn {
        font-weight: 400;
        text-align: center;
        box-shadow: 0 2px 0 rgba(0, 0, 0, 0.015);
        transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
        user-select: none;
        touch-action: manipulation;
        height: 28px;
        padding: 0 15px;
        font-size: 12px;
        color: rgba(0, 0, 0, 0.65);
        background-color: #fff;
        outline: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
        -webkit-appearance: button;
        cursor: pointer;
        border-radius: 0;
        width: 70px;
        border: 1px solid rgba(33, 83, 212, 0.9);
      }

      .right-add {
        background: #2153d4;
        color: #fff;
      }
    }
  }
}

.clearfix:after {
  content: '020';
  display: block;
  height: 0;
  clear: both;
  visibility: hidden;
}

.clearfix {
  /* 触发 hasLayout */
  zoom: 1;
}
</style>
