<template>
  <el-col class="tree-style">
    <el-col>

      <el-row style="margin-bottom: 10px">
        <el-input
          v-model="filterText"
          size="small"
          :placeholder="$t('commons.search')"
          prefix-icon="el-icon-search"
          clearable
          class="main-area-input"
        />

      </el-row>

      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="tree"
            class="filter-tree"
            :data="treeDatas"
            :props="defaultProps"
            :filter-node-method="filterNode"
            :expand-on-click-node="false"
            node-key="code"
            :accordion="true"
            highlight-current
            :default-expanded-keys="expandedKeys"
            @current-change="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node father">
              <span style="display: flex;flex: 1;width: 0;">

                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ node.data.name }}</span>
              </span>
              <span v-if="!isChina(data.code)" class="child">
                <span @click.stop>
                  <span class="el-dropdown-link">
                    <el-button
                      icon="el-icon-plus"
                      type="text"
                      size="small"
                      @click="addHandler(data, node)"
                    />
                  </span>
                </span>
                <span v-if="!isGlobal(data.code)" style="margin-left: 12px;" @click.stop>
                  <span class="el-dropdown-link">
                    <el-button
                      icon="el-icon-delete"
                      type="text"
                      size="small"
                      @click="removeHandler(data, node)"
                    />
                  </span>
                </span>
              </span>
            </span>
          </el-tree>
        </div>

      </el-col>

    </el-col>

  </el-col>
</template>

<script>
import { removeMap } from '@/api/map/map'
import msgCfm from '@/components/msgCfm'
export default {
  name: 'MapSettingLeft',
  props: {
    treeDatas: {
      type: Array,
      default: () => []
    }
  },
  mixins: [msgCfm],
  data() {
    return {
      filterText: '',
      defaultProps: {
        children: 'children',
        label: 'name',
        value: 'id'
      },
      data: [],
      expandedKeys: []
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val)
    }
  },
  created() {
  },
  methods: {

    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },

    nodeClick(data, node) {
      let parent = null
      if (node.parent.data instanceof Array) {
        parent = node.parent.data[0]
      } else {
        parent = node.parent.data
      }
      const nodeInfo = {
        code: data.code,
        name: data.name,
        pcode: data.pcode,
        pname: parent.name
      }
      this.$emit('show-node-info', this.setStatus(nodeInfo, 'read-only'))
    },
    addHandler(data, node) {
      let form = {
        pLevel: node.level,
        pCode: data.code
      }
      if (node.level > 4) {
        this.$error('不支持4级行政级别')
        form = {}
      }
      this.$emit('emit-add', this.setStatus(form, 'add'))
    },
    removeHandler(data, node) {
      let parent = null
      if (node.parent.data instanceof Array) {
        parent = node.parent.data[0]
      } else {
        parent = node.parent.data
      }
      const param = {
        code: data.code,
        pcode: parent.code,
        plevel: node.parent.level,
        name: data.name
      }
      const msg = this.$t('map_setting.cur_node') + '[' + data.name + ']' + this.$t('map_setting.delete_confirm')
      this.$confirm(msg, '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        removeMap(param).then(res => {
          this.$store.dispatch('map/setGeo', {
            key: param.code,
            value: null
          }).then(() => {
            this.$emit('refresh-tree')
            this.openMessageSuccess("commons.delete_success");
          })
        })
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'))
      })
    },
    setStatus(data, status) {
      const form = JSON.parse(JSON.stringify(data))
      return Object.assign(form, {
        status: status || 'read-only'
      })
    },
    showNewNode(code) {
      this.$refs.tree.setCurrentKey(code)
    },
    isChina(code) {
      return code && code.startsWith('156')
    },
    isGlobal(code) {
      return code && code.startsWith('000')
    }
  }
}
</script>

<style lang="scss" scoped>
  .el-divider--horizontal {
    margin: 12px 0
  }

  .search-input {
    padding: 12px 0;
  }

  .custom-tree-container{
    margin-top: 10px;
  }

  .tree-list ::v-deep .el-tree-node__expand-icon.is-leaf{
    display: none;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right:8px;
  }

  .custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }

  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 14px;
    flex-flow: row nowrap;
  }

  .form-item {
    margin-bottom: 0;
  }

  .title-css {
    height: 26px;
  }

  .title-text {
    line-height: 26px;
  }

  .scene-title{
    width: 100%;
    display: flex;
  }
  .scene-title-name{
    width: 100%;
    overflow: hidden;
    display: inline-block;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  .father .child {
    visibility: hidden;
  }
  .father:hover .child {
    visibility: visible;
  }

  .dialog-css ::v-deep .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .inner-dropdown-menu{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%
  }
  .tree-style {
    height: 100%;
    overflow-y: auto;
  }
</style>
