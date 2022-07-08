<template>
  <span>
    <el-dropdown trigger="click" size="mini" @command="clickItem">
      <span class="el-dropdown-link">
        <el-tag size="small" class="item-axis">
          {{ item.name }}<i class="el-icon-arrow-down el-icon--right" />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-if="isSortWidget" :disabled="disabledSort" :command="beforeClickItem('none')">
            <span
              class="de-sort-menu"
              :class="!disabledSort && (!sortNode || sortNode.sort === 'none') ? 'de-active-li': ''"
            >{{
              $t('chart.none')
            }}</span>
          </el-dropdown-item>
          <el-dropdown-item v-if="isSortWidget" :disabled="disabledSort" :command="beforeClickItem('asc')">
            <span
              v-popover:popoverasc
              class="el-dropdown-link inner-dropdown-menu de-sort-menu"
              :class="!disabledSort && sortNode.sort === 'asc' ? 'de-active-li': ''"
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
                  v-for="(node, i) in allFields"
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
          <el-dropdown-item v-if="isSortWidget" :disabled="disabledSort" :command="beforeClickItem('desc')">
            <span
              v-popover:popoverdesc
              class="el-dropdown-link inner-dropdown-menu de-sort-menu"
              :class="!disabledSort && sortNode.sort === 'desc' ? 'de-active-li': ''"
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
                  v-for="(node, i) in allFields"
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
          <el-dropdown-item :divided="isSortWidget" icon="el-icon-delete" :command="beforeClickItem('remove')">
            <span class="de-delete-field">{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
          <slot />
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
export default {
  name: 'DragItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    allFields: {
      type: Array,
      default: () => []
    },
    sort: {
      type: Object,
      default: () => null
    },
    isSortWidget: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      radio: 0,
      ascFieldsShow: false,
      descFieldsShow: false,
      defaultSortProp: {

        sort: 'none'
      },
      sortNode: null
    }
  },
  computed: {
    disabledSort() {
      return this.index > 0
    }
  },
  watch: {
    index(val, old) {
      if (val !== old) {
        this.sortChange('none')
      }
    }
  },
  mounted() {
  },
  created() {
    if (!this.sortNode) {
      this.sortNode = this.sort && this.sort.id ? JSON.parse(JSON.stringify(this.sort)) : JSON.parse(JSON.stringify(this.defaultSortProp))
    }
  },
  methods: {
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
        case 'remove':
          this.removeItem()
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

    removeItem() {
      this.item.index = this.index
      this.$emit('closeItem', this.item)
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
    sortChange(type) {
      this.sortNode.sort = type
      if (type === 'none') {
        this.sortNode = { sort: 'none' }
      }
      this.$emit('sort-change', this.sortNode)
    }

  }
}
</script>

<style scoped lang="scss">
.item-axis {
  padding: 1px 6px;
  margin: 0 3px 2px 3px;
  text-align: left;
  height: 24px;
  line-height: 22px;
  display: inline-block;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
}

.item-axis:hover {
  background-color: #fdfdfd;
  cursor: pointer;
}

span {
  font-size: 12px;
}

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
</style>
