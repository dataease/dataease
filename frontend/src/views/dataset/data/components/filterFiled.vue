<template>
  <div class="white-nowrap">
    <div
      class="filed"
      :style="computedWidth"
      @mouseover="showDel = true"
      @mouseleave="showDel = false"
    >
      <span class="filed-title">{{ $t("auth.filter_fields") }}</span>

      <el-dropdown
        trigger="click"
        :hide-on-click="false"
      >
        <el-input
          v-model="item.name"
          class="w100"
          :placeholder="$t('auth.select_filter_fields')"
          size="mini"
          @input="cancel"
        />
        <el-dropdown-menu
          slot="dropdown"
          class="de-el-dropdown-menu"
        >
          <el-input
            v-model="keywords"
            :placeholder="$t('auth.enter_keywords')"
            prefix-icon="el-icon-search"
            size="mini"
          />
          <ul class="dimension">
            <li
              v-for="ele in dimensions"
              :key="ele.id"
              :style="{
                backgroundColor: activeName === ele.name ? '#f0f7ff' : '',
              }"
              @click="selectItem(ele)"
            >
              <svg-icon
                :icon-class="`field_${fieldEnum[ele.deType]}`"
                :class="`field-icon-${fieldEnum[ele.deType]}`"
              />
              <span>{{ ele.name }}</span>
            </li>
          </ul>
        </el-dropdown-menu>
      </el-dropdown>
      <div
        v-if="item.fieldId"
        class="white-nowrap"
        style="position: relative"
      >
        <span class="filed-title">{{ $t("auth.screen_method") }}</span>
        <el-select
          v-model="item.filterType"
          size="mini"
          class="w100"
          :placeholder="$t('auth.select')"
          @change="filterTypeChange"
        >
          <el-option
            v-for="ele in filterList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          />
        </el-select>
        <span class="filed-title">{{ $t("auth.fixed_value") }}</span>
        <template v-if="item.filterType === 'logic'">
          <el-select
            v-model="item.term"
            class="w120"
            size="mini"
            :placeholder="$t('auth.default_method')"
            @change="onOptionsChange"
          >
            <el-option
              v-for="ele in operators"
              :key="ele.value"
              :label="$t(ele.label)"
              :value="ele.value"
            />
          </el-select>
          <template v-if="[2 ,3].includes(item.deType) && !['null', 'empty', 'not_null', 'not_empty'].includes(item.term)">
            <el-input-number
              v-model="item.value"
              class="w70 mar5"
              size="mini"
              controls-position="right"
            />
            <div class="de-bottom-line short" />
          </template>
          <template v-else-if="!['null', 'empty', 'not_null', 'not_empty'].includes(item.term)">
            <el-input
              v-model="item.value"
              class="w120 mar5"
              size="mini"
            />
            <div class="de-bottom-line" />
          </template>
        </template>

        <el-dropdown
          v-else
          ref="deElDropdownMenuFixed"
          trigger="click"
          :hide-on-click="false"
        >
          <el-input
            ref="enumInput"
            v-model="item.enumValue"
            size="mini"
            clearable
            @input="cancelfixValue"
            @clear="clearAll"
          />
          <el-dropdown-menu
            slot="dropdown"
            class="de-el-dropdown-menu-fixed"
          >
            <div class="de-panel clearfix">
              <div class="mod-left">
                <el-input
                  v-model="filterFiled"
                  :placeholder="$t('auth.enter_keywords')"
                />
                <ul
                  class="infinite-list autochecker-list"
                  style="overflow: auto;height: 231px"
                >
                  <li
                    v-for="i in checkListWithFilter"
                    :key="i"
                    class="infinite-list-item"
                    @click="checkItem(i)"
                  >
                    <i
                      class="el-icon-check"
                      :style="{ opacity: checklist.includes(i) ? 1 : 0 }"
                    />
                    <label>{{ i }}</label>
                    <span>+</span>
                  </li>
                </ul>
                <button
                  class="select-all"
                  @click="selectAll"
                >
                  {{ $t("auth.select_all") }}
                </button>
              </div>
              <div class="mod-left right-de">
                <div class="right-top clearfix">
                  {{ $t("auth.added") }}{{ checklist.length }}
                  <div class="right-btn">
                    <span @click="cancelKeyDow">
                      <i class="el-icon-edit" />
                      {{ $t("auth.manual_input") }}
                    </span>
                    <transition name="el-zoom-in-top">
                      <div
                        v-if="showTextArea"
                        class="de-el-dropdown-menu-manu"
                      >
                        <div class="text-area">
                          <textarea
                            v-model="textareaValue"
                            :placeholder="$t('auth.please_fill')"
                            class="input"
                          />
                          <div class="text-area-btn">
                            <button
                              type="button"
                              class="btn"
                              @click="showTextArea = false"
                            >
                              <span>{{ $t("auth.close") }}</span>
                            </button>
                            <button
                              type="button"
                              class="btn right-add"
                              @click="addFields"
                            >
                              <span>{{ $t("auth.add") }}</span>
                            </button>
                          </div>
                        </div>
                      </div>
                    </transition>
                  </div>
                </div>
                <ul
                  class="infinite-list autochecker-list"
                  style="overflow: auto"
                >
                  <li
                    v-for="(i, idx) in checklist"
                    :key="i"
                    class="infinite-list-item"
                  >
                    <el-tooltip
                      class="item"
                      effect="light"
                      :content="i"
                      placement="top"
                      :open-delay="1000"
                    >
                      <label>{{ i }}</label>
                    </el-tooltip>
                    <i
                      class="el-icon-delete"
                      style="opacity: 1"
                      @click="delChecks(idx)"
                    />
                  </li>
                </ul>
                <div class="right-menu-foot">
                  <div class="footer-left">
                    &nbsp;
                  </div>
                  <div
                    class="confirm-btn"
                    @click="confirm"
                  >
                    {{ $t("auth.sure") }}
                  </div>
                  <div class="footer-right">
                    <i
                      class="el-icon-delete"
                      @click="clearAll"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <i
        v-if="showDel"
        class="el-icon-delete font12"
        @click="$emit('del', index)"
      />
    </div>
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'

import {
  textOptions,
  dateOptions,
  valueOptions,
  fieldEnum
} from '../options'
export default {
  inject: ['filedList'],
  props: {
    index: {
      type: Number,
      default: 0
    },
    item: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      showDel: false,
      keywords: '',
      activeName: '',
      fieldEnum,
      filterFiled: '',
      enumList: [],
      showTextArea: false,
      keydownCanceled: false,
      checklist: [],
      filterList: [],
      textareaValue: ''
    }
  },
  computed: {
    checkListWithFilter() {
      if (!this.filterFiled) return this.enumList
      return this.enumList.filter((ele) => ele.includes(this.filterFiled))
    },
    checkResult() {
      return this.checklist.join(',')
    },
    computedWidth() {
      const { term, fieldId, filterType } = this.item
      const isNull = ['null', 'empty', 'not_null', 'not_empty'].includes(term) && filterType === 'logic'
      return {
        width: !fieldId ? '240px' : isNull ? '540px' : '670px'
      }
    },
    operators() {
      const { deType } = this.item
      if ([0, 5].includes(deType)) {
        return textOptions
      } else if (deType === 1) {
        return dateOptions
      } else {
        return valueOptions
      }
    },
    dimensions() {
      if (!this.keywords) return this.computedFiledList
      return this.computedFiledList.filter((ele) =>
        ele.name.includes(this.keywords)
      )
    },
    computedFiledList() {
      return this.filedList()
    }
  },
  watch: {
    checkResult() {
      this.cancelfixValue()
    }
  },
  created() {
    this.initNameEnumName()
    this.filterListInit(this.item.deType)
  },
  methods: {
    confirm() {
      this.$refs.enumInput.$el.click()
    },
    initNameEnumName() {
      const { name, enumValue, fieldId } = this.item
      if (!name && fieldId) {
        this.checklist = [...new Set(enumValue.split(',') || [])]
      }
      if (!name && !fieldId) return
      this.initEnumOptions()
      this.activeName = this.item.name
      this.checklist = [...new Set(enumValue.split(',') || [])]
    },
    cancelKeyDow() {
      if (!this.showTextArea && !this.keydownCanceled) {
        this.keydownCanceled = true
        const { dropdownElm, handleItemKeyDown } =
          this.$refs.deElDropdownMenuFixed
        dropdownElm.removeEventListener('keydown', handleItemKeyDown, true)
      }
      this.showTextArea = true
    },
    filterTypeChange() {
      this.item.term = ''
      this.item.value = ''
      this.initEnumOptions()
    },
    initEnumOptions() {
      const { deType, filterType, fieldId } = this.item
      // 查找枚举值
      if (filterType === 'enum' && [0, 5].includes(deType)) {
        post('dataset/field/multFieldValuesForPermissions', { fieldIds: [fieldId] }).then(res => {
          this.enumList = this.optionData(res.data)
        })
      }
    },
    onOptionsChange(term) {
      this.item.value = ''
    },
    optionData(data) {
      if (!data) return null
      return data.filter((item) => !!item)
    },
    cancel() {
      this.item.name = this.activeName || ''
    },
    cancelfixValue() {
      this.item.enumValue = this.checkResult || ''
    },
    delChecks(idx) {
      this.checklist.splice(idx, 1)
    },
    selectItem({ name, id, deType }) {
      this.activeName = name
      Object.assign(this.item, {
        fieldId: id,
        name,
        deType,
        filterType: 'logic',
        enumValue: '',
        value: '',
        term: ''
      })
      this.filterListInit(deType)
      this.checklist = []
    },
    filterListInit(deType) {
      this.filterList = [
        {
          value: 'logic',
          label: this.$t('deDataset.logic_filter')
        },
        {
          value: 'enum',
          label: this.$t('deDataset.enum_filter')
        }
      ]
      if ([1, 2, 3].includes(deType)) {
        this.filterList = [this.filterList[0]]
      }
    },
    clearAll() {
      this.checklist = []
    },
    selectAll() {
      this.checkListWithFilter.forEach((ele) => {
        if (!this.checklist.includes(ele)) {
          this.checklist.push(ele)
        }
      })
    },
    addFields() {
      let list = this.textareaValue.split('\n').reduce((pre, next) => {
        const str = next.trim()
        if (!str) return pre
        pre.push(str)
        return pre
      }, [])
      if (list.length) {
        if (list.length > 500) {
          list = list.slice(0, 500)
        }
        this.checklist = [...new Set([...this.checklist, ...list])]
      }
      this.showTextArea = false
    },
    checkItem(i) {
      const index = this.checklist.findIndex((ele) => ele === i)
      if (index === -1) {
        this.checklist.push(i)
      } else {
        this.delChecks(index)
      }
      this.checklist = [...new Set(this.checklist)]
    },
    handleClickOutside() {
      this.showTextArea = false
    }
  }
}
</script>

<style lang="scss" scoped>
.white-nowrap {
  white-space: nowrap;
}
.filed {
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

  ::v-deep.el-input--mini {
    height: 26px;
  }

  >>>.el-input--mini {
    height: 26px;
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

  .el-input {
    width: 170px;
  }

  .w100 {
    width: 100px;
  }

  .w120 {
    width: 120px;
  }

  .w70 {
    width: 70px;
  }

  .mar5 {
    margin-left: -5px;
  }
  ::v-deep.el-input-number__decrease,
  ::v-deep.el-input-number__increase {
    width: 20px;
    height: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: height 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    display: none;
  }

  ::v-deep.el-input-number__decrease:hover {
    height: 16px;
    & + .el-input-number__increase {
      height: 8px;
    }
  }
  ::v-deep.el-input-number__increase:hover {
    height: 16px;
    & + .el-input-number__decrease {
      height: 8px;
    }
  }

  .de-bottom-line {
    font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
    font-variant: tabular-nums;
    font-feature-settings: "tnum";
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
    bottom: 9px;
    width: 100px;
    z-index: 10;
  }

  .short {
    width: 83px;
    right: 22px;
  }

  ::v-deep.el-input-number.is-controls-right .el-input__inner {
    padding-right: 20px;
  }

  .el-input-number {
    line-height: 26px;
    height: 27px;
  }

  ::v-deep.el-select {
    >>> .el-input__suffix-inner {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .el-input__icon {
        height: auto;
      }
    }
  }

  ::v-deep.el-input__inner {
    background-color: #f8f8fa;
    border: none;
    border-radius: 0;
    height: 26px;

    font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
    word-wrap: break-word;
    text-align: left;
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    font-variant: tabular-nums;
    font-feature-settings: "tnum";
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

  i {
    margin-left: 5px;
    color: #7e7e7e;
  }
}
.filed:hover {
  background-color: #e9eaef;
  ::v-deep.el-input-number__decrease,
  ::v-deep.el-input-number__increase {
    display: flex;
  }
}
</style>

<style lang="scss">
.de-el-dropdown-menu {
  .dimension {
    max-height: 200px;
    overflow-y: auto;
    padding: 0;
    li {
      font-family: Alibaba-PuHuiTi-Regular, Helvetica Neue, Helvetica, Arial,
        PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
      font-variant: tabular-nums;
      font-feature-settings: "tnum";
      list-style: none;
      box-sizing: border-box;
      display: block;
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
    }
    li:hover {
      color: #2e74ff;
      background-color: #f0f7ff;
    }
  }
  .el-input__inner {
    font-family: inherit;
    overflow: visible;
    box-sizing: border-box;
    margin: 0;
    font-variant: tabular-nums;
    list-style: none;
    font-feature-settings: "tnum";
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
    -webkit-appearance: none;
    touch-action: manipulation;
    text-overflow: ellipsis;
    position: relative;
    text-align: inherit;
    min-height: 100%;
    border: 0;
    border-bottom: 1px solid #e5e5e5;
    border-radius: 0;
    padding-left: 26px;

    &:focus {
      box-shadow: 0 0 0 2px rgb(46 116 255 / 20%);
      border-right-width: 1px !important;
      outline: 0;
      border-color: none;
    }
  }

  .el-input {
    font-family: Alibaba-PuHuiTi-Regular, Helvetica Neue, Helvetica, Arial,
      PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
    box-sizing: border-box;
    margin: 0;
    color: rgba(0, 0, 0, 0.65);
    font-size: 12px;
    font-variant: tabular-nums;
    line-height: 1.5;
    list-style: none;
    font-feature-settings: "tnum";
    position: relative;
    display: inline-block;
    width: 100%;
    text-align: start;
    padding: 0 6px;
  }
}

.de-el-dropdown-menu-fixed {
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
      font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
      color: rgba(0, 0, 0, 0.65);
      font-size: 12px;
      vertical-align: top;
      padding: 5px;
      width: 50%;
      height: 300px;
      float: left;
      box-sizing: border-box;
      .el-input__inner {
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
      font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif;
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
          font-family: PingFang SC, Hiragino Sans GB, Microsoft YaHei,
            sans-serif;
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

    .select-all {
      box-sizing: border-box;
      margin: 0;
      overflow: visible;
      position: relative;
      font-weight: 400;
      white-space: nowrap;
      border: 1px solid transparent;
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
      user-select: none;
      touch-action: manipulation;
      padding: 0 15px;
      font-size: 12px;
      outline: 0;
      color: #fff;
      border-color: #2e74ff;
      text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);
      box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
      align-items: center;
      justify-content: center;
      line-height: 1;
      -webkit-appearance: button;
      cursor: pointer;
      border-radius: 0;
      background: #2153d4;
      padding-left: 5px;
      text-align: center;
      display: inline-block;
      width: 100%;
      height: 25px;

      &:hover {
        border: 1px solid transparent;
        background: #4794ff;
        color: #fff;
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
      // overflow: hidden;
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
        // overflow: hidden;
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
      margin-top: 5px;
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

      .confirm-btn {
        box-sizing: border-box;
        position: relative;
        font-weight: 400;
        white-space: nowrap;
        text-align: center;
        background-image: none;
        border: 1px solid transparent;
        transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
        user-select: none;
        touch-action: manipulation;
        height: 28px;
        padding: 0 15px;
        font-size: 12px;
        border-radius: 2px;
        outline: 0;
        color: #fff;
        background-color: #2e74ff;
        border-color: #2e74ff;
        text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);
        box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
        display: inline-flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
        -webkit-appearance: button;
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
  content: "020";
  display: block;
  height: 0;
  clear: both;
  visibility: hidden;
}

.clearfix {
  zoom: 1;
}
</style>
