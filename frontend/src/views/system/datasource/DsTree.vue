<template>
  <el-col style="padding: 0 5px 0 5px;">
    <el-col>
      <el-row>
        <span class="header-title">
          {{ $t('commons.datasource') }}
          <el-button style="float: right;padding-right: 7px;margin-top: -8px" icon="el-icon-plus" type="text" @click="addFolder" />
        </span>
      </el-row>
      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="panel_list_tree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node-list father">
              <span style="display: flex; flex: 1 1 0%; width: 0px;">
                <span v-if="data.type !== 'folder'">
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span v-if="data.type === 'folder'">
                  <i class="el-icon-folder" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
              </span>
              <span class="child">
                <span v-if="data.type ==='folder'" @click.stop>
                  <span class="el-dropdown-link">
                    <el-button
                      icon="el-icon-plus"
                      type="text"
                      size="small"
                      @click="addFolderWithType(data)"
                    />
                  </span>

                </span>
                <span v-if="data.type !=='folder'" style="margin-left: 12px;" @click.stop>
                  <el-dropdown trigger="click" size="small" @command="clickFileMore">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-more"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit" :command="beforeClickFile('edit',data,node)">
                        {{ $t('panel.edit') }}
                      </el-dropdown-item>

                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickFile('delete',data,node)">
                        {{ $t('panel.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
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
import { listDatasource, delDs } from '@/api/system/datasource'
export default {
  name: 'DsTree',
  data() {
    return {
      expandedArray: [],
      tData: []
    }
  },
  mounted() {
    this.queryTreeDatas()
  },
  methods: {
    queryTreeDatas() {
      listDatasource().then(res => {
        this.tData = this.buildTree(res.data)
      })
    },
    buildTree(array) {
      const types = {}
      const newArr = []
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (!(element.type in types)) {
          types[element.type] = []
          // newArr.push(...element, ...{ children: types[element.type] })
          newArr.push({ id: element.type, name: element.type, type: 'folder', children: types[element.type] })
        }
        types[element.type].push(element)
        // newArr.children.push({ id: element.id, label: element.name })
      }
      return newArr
    },

    addFolder() {
      this.switchMain('DsForm')
    },
    addFolderWithType(data) {
      this.switchMain('DsForm', { type: data.id })
    },
    nodeClick(node, data) {
      if (node.type === 'folder') return
      this.showInfo(data)
    },

    clickFileMore(param) {
      const { optType, data } = param
      switch (optType) {
        case 'edit':
          this.edit(data)
          break
        case 'delete':
          this._handleDelete(data)
          break
        default:
          break
      }
    },
    beforeClickFile(optType, data, node) {
      return { optType, data, node }
    },
    edit(row) {
      this.switchMain('DsForm', row)
    },
    showInfo(row) {
      const param = { ...row.data, ...{ showModel: 'show' }}
      this.switchMain('DsForm', param)
    },
    _handleDelete(datasource) {
      this.$confirm(this.$t('datasource.delete_warning'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        delDs(datasource.id).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.search()
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    switchMain(component, componentParam) {
      this.$emit('switch-main', {
        component,
        componentParam
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.header-title {
    font-size: 14px;
    flex: 1;
    color: #606266;
    font-weight: bold;
    display: block;
    height: 100%;
    /*line-height: 36px;*/
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

  .dialog-css>>>.el-dialog__body {
    padding: 15px 20px;
  }
  .dialog-css >>>.el-dialog__body {
    padding: 10px 20px 20px;
  }

  .father .child {
    display: none;
  }
  .father:hover .child {
    display: inline;
  }
</style>
