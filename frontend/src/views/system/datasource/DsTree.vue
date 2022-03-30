<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <el-col>
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('commons.datasource') }}
        </span>
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: right;"
                   @click="addFolder"/>

      </el-row>
      <el-divider/>
      <el-row>
        <el-form>
          <el-form-item class="form-item">
            <el-input
              v-model="key"
              size="mini"
              :placeholder="$t('chart.search')"
              prefix-icon="el-icon-search"
              clearable
              class="main-area-input"
            />
          </el-form-item>
        </el-form>
      </el-row>

      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="myDsTree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node-list father">
              <span style="display: flex;flex: 1;width: 0;">
                <span v-if="data.type !== 'folder' && data.status !== 'Error' && data.status !== 'Warning'">
                  <svg-icon icon-class="datasource" class="ds-icon-scene"/>
                </span>
                <span v-if="data.status === 'Error'">
                  <svg-icon icon-class="exclamationmark" class="ds-icon-scene"/>
                </span>
                <span v-if="data.status === 'Warning'">
                  <svg-icon icon-class="exclamationmark2" class="ds-icon-scene"/>
                </span>
                <span v-if="data.type === 'folder'">
                  <i class="el-icon-folder"/>
                </span>
                <span v-if=" data.status === 'Error'"
                      style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                  <el-tooltip effect="dark" :content="$t('datasource.in_valid')" placement="right">
                    <span>
                      {{ data.name }}
                    </span>
                  </el-tooltip>
                </span>
                <span v-if=" data.status === 'Warning'"
                      style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                  <el-tooltip effect="dark" :content="$t('datasource.warning')" placement="right">
                    <span>
                      {{ data.name }}
                    </span>
                  </el-tooltip>
                </span>
                <span v-if="data.status !== 'Error' && data.status !== 'Warning'"
                      style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                  {{ data.name }}
                </span>

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
                  <span v-if="hasDataPermission('manage',data.privileges)" class="el-dropdown-link">
                    <el-button
                      icon="el-icon-delete"
                      type="text"
                      size="small"
                      @click="_handleDelete(data)"
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
import {listDatasource, listDatasourceByType, delDs} from '@/api/system/datasource'

export default {
  name: 'DsTree',
  props: {
    datasource: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      expandedArray: [],
      tData: [],
      showSearchInput: false,
      key: ''
    }
  },
  watch: {
    key(val) {
      this.$refs.myDsTree.filter(val)
    }
  },
  mounted() {
    this.queryTreeDatas()
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    showSearchWidget() {
      this.showSearchInput = true
    },
    closeSearchWidget() {
      this.key = ''
      this.showSearchInput = false
    },
    queryTreeDatas() {
      listDatasource().then(res => {
        this.tData = this.buildTree(res.data)
      })
    },
    refreshType(datasource) {
      let typeData = []
      listDatasourceByType(datasource.type).then(res => {
        typeData = this.buildTree(res.data)
        if (typeData.length === 0) {
          let index = this.tData.findIndex(item => {
            if (item.id === datasource.type) {
              return true;
            }
          })
          this.tData.splice(index, 1)
        } else {
          let find = false;
          for (let index = 0; index < this.tData.length; index++) {
            if (typeData[0].id === this.tData[index].id) {
              this.tData[index].children = typeData[0].children
              find = true
            }
          }
          if (!find) {
            this.tData.push(typeData[0])
          }
        }
      })
    },
    buildTree(array) {
      const types = {}
      const newArr = []
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (this.msgNodeId) {
          if (element.id === this.msgNodeId) {
            element.msgNode = true
          }
        }
        if (!(element.type in types)) {
          types[element.type] = []
          // newArr.push(...element, ...{ children: types[element.type] })
          newArr.push({
            id: element.type,
            name: this.transTypeToName(element.type),
            type: 'folder',
            children: types[element.type]
          })
        }
        types[element.type].push(element)
        // newArr.children.push({ id: element.id, label: element.name })
      }
      return newArr
    },

    transTypeToName(type) {
      if (type === 'mysql') {
        return 'MySQL'
      } else if (type === 'sqlServer') {
        return 'SQL Server'
      } else if (type === 'oracle') {
        return 'Oracle'
      } else if (type === 'pg') {
        return 'PostgreSQL'
      } else if (type === 'es') {
        return 'Elasticsearch'
      } else if (type === 'ck') {
        return 'ClickHouse'
      } else if (type === 'mariadb') {
        return 'MariaDB'
      } else if (type === 'ds_doris') {
        return 'Doris'
      } else if (type === 'mongo') {
        return 'MongoDB'
      } else if (type === 'redshift') {
        return 'AWS Redshift'
      } else if (type === 'hive') {
        return 'Apache Hive'
      } else if (type === 'db2') {
        return 'Db2'
      } else if (type === 'api') {
        return 'API'
      } else if (type === 'impala') {
        return 'Apache Impala'
      }if (type === 'TiDB') {
        return 'TiDB'
      }if (type === 'StarRocks') {
        return 'StarRocks'
      }
    },

    addFolder() {
      this.switchMain('DsForm', {}, this.tData)
    },
    addFolderWithType(data) {
      this.switchMain('DsForm', {type: data.id}, this.tData)
    },
    nodeClick(node, data) {
      if (node.type === 'folder') return
      this.showInfo(data)
    },

    clickFileMore(param) {
      const {optType, data} = param
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
      return {optType, data, node}
    },
    edit(row) {
      this.switchMain('DsForm', row, this.tData)
    },
    showInfo(row) {
      const param = {...row.data, ...{showModel: 'show'}}
      this.switchMain('DsForm', param, this.tData)
    },
    _handleDelete(datasource) {
      this.$confirm(this.$t('datasource.delete_warning'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        delDs(datasource.id).then(res => {
          if(res.success){
            this.$success(this.$t('commons.delete_success'))
            this.switchMain('DataHome', {}, this.tData)
            this.refreshType(datasource)
          }else {
            this.$message({
              type: 'error',
              message: res.message
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    switchMain(component, componentParam, tData) {
      this.$emit('switch-main', {
        component,
        componentParam,
        tData
      })
    },
    markInvalid(msgParam) {
      const param = JSON.parse(msgParam)
      const msgNodeId = param.id
      this.msgNodeId = msgNodeId
      this.$nextTick(() => {
        this.tData.forEach(folder => {
          const nodes = folder.children
          nodes.forEach(node => {
            if (node.id === msgNodeId) {
              node.msgNode = true
            }
          })
        })
      })
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

.custom-tree-container {
  margin-top: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}

.tree-list > > > .el-tree-node__expand-icon.is-leaf {
  display: none;
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

.dialog-css > > > .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css > > > .el-dialog__body {
  padding: 10px 20px 20px;
}

.form-item > > > .el-form-item__label {
  font-size: 12px;
}

.scene-title {
  width: 100%;
  display: flex;
}

.scene-title-name {
  width: 100%;
  overflow: hidden;
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}

.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}

.msg-node-class {
  color: red;

  > > > i {
    color: red;
  }
}
</style>
