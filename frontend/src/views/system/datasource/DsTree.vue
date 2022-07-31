<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <el-col>
      <el-row class="title-css" v-show="showView === 'Datasource'">
        <span class="title-text">
          {{ $t('commons.datasource') }}
        </span>
      </el-row>
      <el-row v-show="showView === 'Datasource'">
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: left;" @click="addFolder"> {{ $t('datasource.create') }}</el-button>
        <el-button icon="el-icon-setting" type="text" size="mini" style="float: right;" @click="driverMgm" v-show="user.isAdmin"> {{ $t('driver.mgm') }}</el-button>
      </el-row>

      <el-row class="title-css" v-show="showView === 'Driver'">
        <template>
          <el-icon name="back" class="back-button"  size="mini"  @click.native="dsMgm" />
          {{$t('driver.exit_mgm')}}
        </template>
      </el-row>
      <el-row v-show="showView === 'Driver'">
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: left;" @click="addFolder"> {{ $t('driver.add') }}</el-button>
      </el-row>

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
                <span
                  v-if=" data.status === 'Error'"
                  style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                >
                  <el-tooltip effect="dark" :content="$t('datasource.in_valid')" placement="right">
                    <span>
                      {{ data.name }}
                    </span>
                  </el-tooltip>
                </span>
                <span
                  v-if=" data.status === 'Warning'"
                  style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                >
                  <el-tooltip effect="dark" :content="$t('datasource.warning')" placement="right">
                    <span>
                      {{ data.name }}
                    </span>
                  </el-tooltip>
                </span>
                <span
                  v-if="data.status !== 'Error' && data.status !== 'Warning'"
                  style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                >
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

      <el-dialog v-dialogDrag :title="dialogTitle" :visible="editDriver" :show-close="false" width="50%" append-to-body>
        <el-form ref="driverForm" :model="driverForm" label-position="right" label-width="100px" :rules="rule">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="driverForm.name"/>
          </el-form-item>
          <el-form-item :label="$t('commons.description')">
            <el-input v-model="driverForm.desc"/>
          </el-form-item>
          <el-form-item :label="$t('datasource.type')" prop="type">
            <el-select
              v-model="driverForm.type"
              :placeholder="$t('datasource.please_choose_type')"
              class="select-width"
              :disabled="disabledModifyType"
              filterable
            >
              <el-option
                v-for="item in dsTypesForDriver"
                :key="item.type"
                :label="item.name"
                :value="item.type"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="close()">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveDriver(driverForm)">{{ $t('commons.save') }}
          </el-button>
        </div>
      </el-dialog>

    </el-col>
  </el-col>
</template>
<script>

import {mapGetters} from 'vuex'
import i18n from '@/lang'
import {
  listDatasource,
  listDatasourceByType,
  delDs,
  listDatasourceType,
  listDrivers,
  addDriver,
  delDriver,
  listDriverByType
} from '@/api/system/datasource'
import {ApplicationContext} from '@/utils/ApplicationContext'

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
      dsTypes: [],
      dsTypesForDriver: [],
      showSearchInput: false,
      key: '',
      showView: 'Datasource',
      dialogTitle: '',
      editDriver: false,
      driverForm: {
        name: '',
        desc: '',
        type: ''
      },
      disabledModifyType: false,
      rule: {
        name: [{required: true, message: i18n.t('datasource.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur'}],
        desc: [{required: true, message: i18n.t('datasource.input_name'), trigger: 'blur'},
          {min: 2, max: 200, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur'}],
        type: [{required: true, message: i18n.t('datasource.please_choose_type'), trigger: 'blur'}]
      }
    }
  },
  watch: {
    key(val) {
      this.$refs.myDsTree.filter(val)
    }
  },
  computed: {
    ...mapGetters([
      'user'
    ])
  },
  created() {
    this.queryTreeDatas()
    this.datasourceTypes()
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
      if (this.showView === 'Datasource') {
        listDatasource().then(res => {
          this.tData = this.buildTree(res.data)
        })
      }
      if (this.showView === 'Driver') {
        listDrivers().then(res => {
          this.tData = this.buildTree(res.data)
        })
      }
    },
    datasourceTypes() {
      listDatasourceType().then(res => {
        this.dsTypes = res.data
        this.dsTypes.forEach(item => {
          if (item.isJdbc) {
            this.dsTypesForDriver.push(item)
          }
        })
      })
    },
    refreshType(datasource) {
      const method = this.showView === 'Datasource' ? listDatasourceByType : listDriverByType
      let typeData = []
      method(datasource.type).then(res => {
        typeData = this.buildTree(res.data)
        if (typeData.length === 0) {
          const index = this.tData.findIndex(item => {
            if (item.id === datasource.type) {
              return true
            }
          })
          this.tData.splice(index, 1)
        } else {
          let find = false
          for (let index = 0; index < this.tData.length; index++) {
            if (typeData[0].id === this.tData[index].id) {
              this.tData[index].children = typeData[0].children
              for (let i = 0; i < this.tData[index].children.length; i++) {
                if(this.tData[index].children[i].id === datasource.id){
                  this.showInfo({data: this.tData[index].children[i]})
                }
              }
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
            name: element.typeDesc,
            type: 'folder',
            children: types[element.type]
          })
        }
        types[element.type].push(element)
        // newArr.children.push({ id: element.id, label: element.name })
      }
      return newArr
    },

    addFolder() {
      if (this.showView === 'Driver') {
        this.dialogTitle = this.$t('driver.driver')
        this.editDriver = true
        // this.switchMain('DriverForm', {}, this.tData, this.dsTypes)
      } else {
        this.switchMain('DsForm', {}, this.tData, this.dsTypes)
      }
    },

    driverMgm() {
      this.$emit('switch-main', {})
      this.showView = 'Driver'
      this.expandedArray = []
      this.tData = []
      this.queryTreeDatas()
    },
    dsMgm() {
      this.$emit('switch-main', {})
      this.showView = 'Datasource'
      this.expandedArray = []
      this.tData = []
      this.queryTreeDatas()
    },
    addFolderWithType(data) {
      if (this.showView === 'Driver') {
        this.driverForm.type = data.id
        this.dialogTitle = this.$t('driver.driver')
        // this.editDriver = false
        this.editDriver = true
        // this.switchMain(switchMain'DriverForm', {}, this.tData, this.dsTypes)
      } else {
        this.switchMain('DsForm', {type: data.id}, this.tData, this.dsTypes)
      }
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
      this.switchMain('DsForm', row, this.tData, this.dsTypes)
    },
    showInfo(row) {
      const param = {...row.data, ...{showModel: 'show'}}
      if (this.showView === 'Datasource') {
        this.switchMain('DsForm', param, this.tData, this.dsTypes)
      } else {
        this.switchMain('DriverForm', param, this.tData, this.dsTypes)
      }
    },
    _handleDelete(datasource) {
      this.$confirm(this.$t('datasource.delete_warning'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        let method = delDriver
        let parma = {type: datasource.type, id: datasource.id}
        if (this.showView === 'Datasource') {
          method = delDs
          parma = datasource.id
        }
        method(parma).then(res => {
          if (res.success) {
            this.$success(this.$t('commons.delete_success'))
            this.switchMain('DataHome', {}, this.tData, this.dsTypes)
            this.refreshType(datasource)
          } else {
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
    switchMain(component, componentParam, tData, dsTypes) {
      this.$emit('switch-main', {
        component,
        componentParam,
        tData,
        dsTypes
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
    },
    close() {
      this.$refs['driverForm'].resetFields()
      this.editDriver = false
      this.driverForm = {
        name: '',
        desc: '',
        type: ''
      }
    },
    saveDriver(driverForm) {
      this.$refs['driverForm'].validate((valid) => {
        if (valid) {
          addDriver(driverForm).then(res => {
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.refreshType(driverForm)
            this.close()
          })
        } else {
          return false
        }
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
