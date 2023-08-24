<template>
  <div>
    <el-dropdown
      trigger="click"
      size="mini"
      @command="clickItem"
    >

      <span class="el-dropdown-link filter-sort-span">
        {{ $t('chart.sort') }}
        <i class="el-icon-sort i-filter i-filter-active" />
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item
          :command="beforeClickItem('none')"
        >
          <span
            class="de-sort-menu"
            :class="(!sortNode || sortNode.sort === 'none') ? 'de-active-li': ''"
          >{{
            $t('chart.none')
          }}</span>
        </el-dropdown-item>
        <el-dropdown-item

          :command="beforeClickItem('asc')"
        >
          <span
            v-popover:popoverasc
            class="el-dropdown-link inner-dropdown-menu de-sort-menu"
            :class="sortNode.sort === 'asc' ? 'de-active-li': ''"
          >
            <span>
              <span>{{ $t('chart.asc') }}</span>
            </span>
            <i class="el-icon-arrow-right el-icon--right" />
          </span>
          <el-popover
            ref="popoverasc"
            v-model="ascFieldsShow"
            placement="right-start"
            width="120"
            :close-delay="500"
            trigger="hover"
          >
            <ul class="de-ul">
              <li
                v-for="(node, i) in tableFields"
                :key="node.id"
                :index="i"
                class="de-sort-field-span"
                :class="sortNode.sort === 'asc' && sortNode.id === node.id ? 'de-active-li': ''"
                @click="saveAscField(node)"
              >
                <span>{{ node.name }}</span>
              </li>
            </ul>
          </el-popover>
        </el-dropdown-item>
        <el-dropdown-item

          :command="beforeClickItem('desc')"
        >
          <span
            v-popover:popoverdesc
            class="el-dropdown-link inner-dropdown-menu de-sort-menu"
            :class="sortNode.sort === 'desc' ? 'de-active-li': ''"
          >
            <span>
              <span>{{ $t('chart.desc') }}</span>
            </span>
            <i class="el-icon-arrow-right el-icon--right" />
          </span>
          <el-popover
            ref="popoverdesc"
            v-model="descFieldsShow"
            placement="right-start"
            width="120"
            :close-delay="500"
            trigger="hover"
          >
            <ul class="de-ul">
              <li
                v-for="(node, i) in tableFields"
                :key="node.id"
                :index="i"
                class="de-sort-field-span"
                :class="sortNode.sort === 'desc' && sortNode.id === node.id ? 'de-active-li': ''"
                @click="saveDescField(node)"
              >
                <span>{{ node.name }}</span>
              </li>
            </ul>
          </el-popover>
        </el-dropdown-item>

        <el-dropdown-item
          v-if="isChinesSortWidget"
          :command="beforeClickItem('chinese')"
        >
          <span
            v-popover:popoverchinese
            class="el-dropdown-link inner-dropdown-menu de-sort-menu"
            :class="sortNode.sort === 'chinese' ? 'de-active-li': ''"
          >
            <span>
              <span>{{ $t('chart.chinese') }}</span>
            </span>
            <i class="el-icon-arrow-right el-icon--right" />
          </span>
          <el-popover
            ref="popoverchinese"
            v-model="chineseFieldsShow"
            placement="right-start"
            width="120"
            :close-delay="500"
            trigger="hover"
          >
            <ul class="de-ul">
              <li
                v-for="(node, i) in chineseFields"
                :key="node.id"
                :index="i"
                class="de-sort-field-span"
                :class="sortNode.sort === 'chinese' && sortNode.id === node.id ? 'de-active-li': ''"
                @click="saveChineseField(node)"
              >
                <span>{{ node.name }}</span>
              </li>
            </ul>
          </el-popover>
        </el-dropdown-item>

        <el-dropdown-item
          v-if="isCustomSortWidget"
          :command="beforeClickItem('custom')"
        >
          <span
            class="de-sort-menu"
            :class="(sortNode && sortNode.sort === 'custom') ? 'de-active-li': ''"
          >{{ $t('chart.custom_sort') }}...</span>
        </el-dropdown-item>

      </el-dropdown-menu>

    </el-dropdown>
    <el-dialog
      v-if="isCustomSortWidget && showCustomSort"
      append-to-body
      :title="$t('chart.custom_sort')"
      :visible="showCustomSort"
      :show-close="false"
      width="300px"
      class="dialog-css"
    >
      <div style="width: 100%;overflow-y: auto;overflow-x: hidden;word-break: break-all;position: relative;">
        <filter-custom-sort
          :field-id="fieldIds"
          :custom-sort-list="customSortList"
          @on-filter-sort-change="customSortChange"
        />

      </div>

      <div
        slot="footer"
        class="dialog-footer filter-custom-sort-footer"
      >
        <el-button
          size="mini"
          @click="cancelCustomSort"
        >{{ $t('chart.cancel') }}</el-button>
        <el-button
          type="primary"
          size="mini"
          @click="saveCustomSort"
        >{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { fieldListWithPermission } from '@/api/dataset/dataset'
import FilterCustomSort from './FilterCustomSort'
import { isSameArr } from '@/utils'
export default {
  name: 'FilterSort',
  components: { FilterCustomSort },
  props: {
    widget: {
      type: Object,
      default: null
    },

    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      ascFieldsShow: false,
      descFieldsShow: false,
      chineseFieldsShow: false,
      defaultSortProp: {
        sort: 'none'
      },
      tableFields: [],
      sortNode: null,
      showCustomSort: false,
      customSortList: [],
      chineseFields: [
        { id: 'chineseAsc', name: this.$t('chart.asc') },
        { id: 'chineseDesc', name: this.$t('chart.desc') }
      ]
    }
  },
  computed: {
    fieldIds() {
      return this.element.options.attrs.fieldId
    },
    isSortWidget() {
      return this.widget && this.widget.isSortWidget && this.widget.isSortWidget()
    },
    isCustomSortWidget() {
      return this.widget && this.widget.isCustomSortWidget && this.widget.isCustomSortWidget()
    },

    firstTableId() {
      if (!this.isSortWidget) return null
      if (this.element.options.attrs.dragItems && this.element.options.attrs.dragItems.length) {
        return this.element.options.attrs.dragItems[0].tableId
      }
      return null
    },
    isChinesSortWidget() {
      return this.widget?.isChinesSortWidget && this.widget.isChinesSortWidget()
    }
  },
  watch: {
    firstTableId(val, old) {
      if (val !== old) {
        if (this.isSortWidget && (this.sortNode?.sort === 'asc' || this.sortNode?.sort === 'desc')) {
          this.sortNode = { sort: 'none' }
          this.$emit('sort-change', this.sortNode)
        }
        this.loadFields()
      }
    },
    fieldIds(val, old) {
      if (val !== old) {
        if (this.isCustomSortWidget && this.sortNode.sort === 'custom') {
          const valArr = val?.length ? val.split(',') : null
          const oldArr = old?.length ? old.split(',') : null
          if (!isSameArr(valArr, oldArr)) {
            this.sortNode = { sort: 'none' }
            this.$emit('sort-change', this.sortNode)
          }
        }
      }
    }

  },
  mounted() {
  },
  created() {
    if (this.isSortWidget && this.element.options.attrs.dragItems && this.element.options.attrs.dragItems.length) {
      if (this.element.options.attrs.sort) {
        this.sortNode = JSON.parse(JSON.stringify(this.element.options.attrs.sort))
        if (this.isCustomSortWidget && this.sortNode.sort === 'custom' && this.sortNode.list) {
          this.customSortList = JSON.parse(JSON.stringify(this.sortNode.list))
        }
      }

      this.loadFields()
    }
    if (!this.sortNode) {
      this.sortNode = JSON.parse(JSON.stringify(this.defaultSortProp))
    }
  },
  methods: {
    customSortChange(list) {
      this.customSortList = list
    },
    cancelCustomSort() {
      this.customSortList = this.sortNode.list?.length ? JSON.parse(JSON.stringify(this.sortNode.list)) : []
      this.showCustomSort = false
    },
    saveCustomSort() {
      this.sortNode = {
        sort: 'custom',
        list: this.customSortList
      }
      this.$emit('sort-change', this.sortNode)
      this.showCustomSort = false
    },
    loadFields() {
      if (this.firstTableId) {
        fieldListWithPermission(this.firstTableId).then(res => {
          this.tableFields = JSON.parse(JSON.stringify(res.data))
        })
      } else {
        this.tableFields = []
      }
    },

    clickItem(param) {
      if (!param) {
        return
      }
      switch (param.type) {
        case 'none':
          this.sortChange('none')
          break
        case 'asc':
          this.sortChange('asc')
          break
        case 'desc':
          this.sortChange('desc')
          break
        case 'custom':
          this.sortChange('custom')
          break
        case 'chinese':
          this.sortChange('chinese')
          break
        default:
          break
      }
    },
    beforeClickItem(type) {
      return {
        type: type
      }
    },

    saveAscField({ id, name }) {
      this.ascFieldsShow = false
      const sort = 'asc'
      this.sortNode = { id, name, sort }
      this.$emit('sort-change', this.sortNode)
    },

    saveDescField({ id, name }) {
      this.descFieldsShow = false
      const sort = 'desc'
      this.sortNode = { id, name, sort }
      this.$emit('sort-change', this.sortNode)
    },
    saveChineseField({ id, name }) {
      this.chineseFieldsShow = false
      const sort = 'chinese'
      this.sortNode = { id, name, sort }
      this.$emit('sort-change', this.sortNode)
    },
    sortChange(type) {
      if (type === 'custom') {
        this.showCustomSort = true
        return
      }
      this.sortNode.sort = type
      if (type === 'none') {
        this.sortNode = { sort: 'none' }
      }
      if (type === 'chinese') {
        this.sortNode = { sort: 'chinese', id: 'chineseAsc' }
      }

      this.$emit('sort-change', this.sortNode)
    }

  }
}
</script>

<style lang="scss" scoped>
.de-ul li {
  margin: 5px 2px;
  cursor: pointer;

  &:hover {
    color: #409EFF;
    border-color: rgb(198, 226, 255);
    background-color: rgb(236, 245, 255);
  }

  &:before {
    content: "";
    width: 6px;
    height: 6px;
    display: inline-block;
    border-radius: 50%;
    vertical-align: middle;
    margin-right: 5px;
  }
}

.de-active-li {
  &:before {
    background: #409EFF;
  }
}

.de-sort-menu::before {
  content: "";
  width: 6px;
  height: 6px;
  display: inline-block;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 5px;
}

.de-delete-field {
  margin-left: 4px;
}

.de-sort-field-span {
  display: inline-flexbox;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

::v-deep .filter-custom-sort-footer {
  padding-top: 5px !important;
  border-top: solid 1px #eee;
  text-align: end;
}

.filter-sort-span {
  color: #303133;
  font-weight: 500;
  cursor: pointer;
  margin-left: 10px;
  i {
    margin-left: 1px;
  }
}
.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 5px;
}
</style>
