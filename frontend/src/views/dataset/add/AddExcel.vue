<template>
  <div v-loading="loading" class="dataset-excel">
    <p v-show="!showLeft" class="arrow-right" @click="showLeft = true">
      <i class="el-icon-d-arrow-right" />
    </p>
    <div v-show="showLeft" class="table-list">
      <p class="select-ds">
        <span
          >{{ $t('deDataset.select_data_table ') }}
          <el-tooltip class="item" effect="dark" placement="right">
            <div slot="content">
              {{ $t('dataset.excel_info_1') }}<br />
              {{ $t('dataset.excel_info_2') }}<br />
              {{ $t('dataset.excel_info_3') }}
            </div>
            <i class="el-icon-warning-outline" /> </el-tooltip
        ></span>
        <i class="el-icon-d-arrow-left" @click="showLeft = false" />
      </p>
      <el-upload
        :action="baseUrl + 'dataset/table/excel/upload'"
        :multiple="false"
        :show-file-list="false"
        :file-list="fileList"
        :data="param"
        accept=".xls,.xlsx,.csv"
        :before-upload="beforeUpload"
        :on-success="uploadSuccess"
        :on-error="uploadFail"
        name="file"
        :headers="headers"
      >
        <deBtn
          class="search"
          icon="el-icon-upload2"
          :loading="uploading"
          secondary
          :disabled="uploading"
          >{{ $t('deDataset.upload_data') }}
        </deBtn>
      </el-upload>
      <div class="table-checkbox-list">
        <el-tree
          ref="tree"
          class="de-tree"
          :data="excelData"
          :default-expanded-keys="defaultExpandedKeys"
          :default-checked-keys="defaultCheckedKeys"
          node-key="id"
          :props="props"
          show-checkbox
          highlight-current
          @node-click="handleNodeClick"
          @check-change="handleCheckChange"
        >
          <span
            :title="data.excelLable"
            slot-scope="{ data }"
            class="custom-tree-node"
          >
            {{ data.excelLable }}
            <span
              v-if="
                (data.nameExsit && !param.tableId) ||
                data.empty ||
                data.overLength
              "
              class="error-name-exsit"
            >
              <svg-icon icon-class="exclamationmark" class="ds-icon-scene" />
            </span>
          </span>
        </el-tree>
      </div>
    </div>
    <div class="table-detail">
      <el-empty
        v-if="!excelData.length"
        style="padding-top: 172px"
        :image-size="125"
        :image="errImg"
        :description="$t('deDataset.excel_data_first')"
      />
      <template v-else>
        <div class="dataset">
          <span class="name">{{ $t('dataset.name') }}</span>
          <el-input
            v-model="sheetObj.datasetName"
            :placeholder="$t('commons.name')"
            size="small"
            @change="changeDatasetName"
          />
          <div
            v-if="
              (sheetObj.nameExsit && !param.tableId) ||
              sheetObj.empty ||
              sheetObj.overLength
            "
            style="left: 107px; top: 52px"
            class="el-form-item__error"
          >
            {{
              $t(
                sheetObj.nameExsit
                  ? 'deDataset.already_exists'
                  : sheetObj.overLength
                  ? 'dataset.char_can_not_more_50'
                  : 'dataset.pls_input_name'
              )
            }}
          </div>
        </div>
        <div class="data">
          <div class="result-num">
            {{
              `${$t('dataset.preview_show')} 1000 ${$t('dataset.preview_item')}`
            }}
          </div>

          <ux-grid
            ref="plxTable"
            size="mini"
            style="width: 100%"
            :height="height"
            :checkbox-config="{ highlight: true }"
            :width-resize="true"
          >
            <ux-table-column
              v-for="field in sheetObj.fields"
              :key="field.fieldName + field.fieldType"
              min-width="200px"
              :field="field.fieldName"
              :title="field.remarks"
              :resizable="true"
            >
              <template slot="header">
                <el-dropdown
                  placement="bottom-start"
                  trigger="click"
                  @command="(type) => handleCommand(type, field)"
                >
                  <span class="type-switch">
                    <svg-icon
                      v-if="field.fieldType === 'TEXT'"
                      icon-class="field_text"
                      class="field-icon-text" />
                    <svg-icon
                      v-if="field.fieldType === 'DATETIME'"
                      icon-class="field_time"
                      class="field-icon-time" />
                    <svg-icon
                      v-if="
                        field.fieldType === 'LONG' ||
                        field.fieldType === 'DOUBLE'
                      "
                      icon-class="field_value"
                      class="field-icon-value" />
                    <i class="el-icon-arrow-down el-icon--right"
                  /></span>
                  <el-dropdown-menu
                    slot="dropdown"
                    style="width: 178px"
                    class="de-card-dropdown"
                  >
                    <el-dropdown-item
                      v-for="item in fieldOptions"
                      :key="item.value"
                      :command="item.value"
                      ><span>
                        <svg-icon
                          v-if="item.value === 'TEXT'"
                          icon-class="field_text"
                          class="field-icon-text"
                        />
                        <svg-icon
                          v-if="item.value === 'DATETIME'"
                          icon-class="field_time"
                          class="field-icon-time"
                        />
                        <svg-icon
                          v-if="
                            item.value === 'LONG' || item.value === 'DOUBLE'
                          "
                          icon-class="field_value"
                          class="field-icon-value"
                        />
                      </span>
                      <span
                        style="
                          color: #8492a6;
                          font-size: 14px;
                          margin-left: 10px;
                        "
                        >{{ item.label }}</span
                      ></el-dropdown-item
                    >
                  </el-dropdown-menu>
                </el-dropdown>
                <span style="font-size: 14px; margin-left: 10px">
                  {{ field.remarks }}
                </span>
              </template>
            </ux-table-column>
          </ux-grid>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import { getToken } from '@/utils/auth'
import i18n from '@/lang'
import { $alert, $confirm } from '@/utils/message'
import store from '@/store'
import msgCfm from '@/components/msgCfm/index'
import cancelMix from './cancelMix'

const token = getToken()

export default {
  name: 'AddExcel',
  mixins: [msgCfm, cancelMix],
  props: {
    param: {
      type: Object,
      default: null
    },
    tableId: {
      type: String,
      default: ''
    },
    editType: {
      type: Number,
      default: 0
    },
    nameList: {
      type: Array,
      default: () => []
    },
    originName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      showLeft: true,
      errImg: require('@/assets/None_Select_ds.png'),
      sheetObj: { datasetName: ' ', fields: [] },
      sheets: [],
      data: [],
      mode: '1',
      height: 600,
      fileList: [],
      headers: {
        Authorization: token,
        'Accept-Language': i18n.locale.replace('_', '-')
      },
      baseUrl: process.env.VUE_APP_BASE_API,
      path: '',
      uploading: false,
      fieldOptions: [
        { label: this.$t('dataset.text'), value: 'TEXT' },
        { label: this.$t('dataset.time'), value: 'DATETIME' },
        { label: this.$t('dataset.value'), value: 'LONG' },
        {
          label:
            this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
          value: 'DOUBLE'
        }
      ],
      props: {
        label: 'excelLable',
        children: 'sheets'
      },
      count: 1,
      excelData: [],
      defaultExpandedKeys: [],
      defaultCheckedKeys: []
    }
  },
  computed: {
    nameListCopy() {
      return this.nameList.filter((ele) => ele !== this.originName)
    }
  },
  mounted() {
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()
  },
  created() {
    if (!this.param.tableId) {
      this.param.tableId = ''
    }
    if (!this.param.editType) {
      this.param.editType = 0
    }
  },
  methods: {
    handleCheckChange(data, checked, indeterminate) {
      if (checked) {
        this.defaultCheckedKeys.push(data.id)
        this.handleNodeClick(data)
      } else {
        var index = this.defaultCheckedKeys.findIndex((id) => {
          if (id == data.id) {
            return true
          }
        })
        this.defaultCheckedKeys.splice(index, 1)
      }
      this.validateName()
      const labelList = this.$refs.tree.getCheckedNodes().map((ele) => ele.id)
      const excelList = this.excelData.map((ele) => ele.id)
      this.$emit(
        'setTableNum',
        labelList.filter((ele) => !excelList.includes(ele)).length
      )
    },
    nameExsitValidator(ele, checkList) {
      this.$set(
        ele,
        'nameExsit',
        this.nameListCopy
          .concat(checkList)
          .filter((name) => name === ele.datasetName).length > 1
      )
    },
    validateName() {
      const checkList = this.$refs.tree
        .getCheckedNodes()
        .map((ele) => ele.datasetName)
      this.excelData
        .reduce((pre, next) => pre.concat(next.sheets), [])
        .forEach((ele, index) => {
          if (checkList.includes(ele.datasetName)) {
            this.nameExsitValidator(ele, checkList)
            this.nameLengthValidator(ele)
          } else {
            this.$set(ele, 'nameExsit', false)
            this.$set(ele, 'empty', false)
            this.$set(ele, 'overLength', false)
          }
        })
    },
    nameLengthValidator(ele) {
      this.$set(ele, 'empty', !ele.datasetName.length)
      this.$set(ele, 'overLength', ele.datasetName.length > 50)
    },
    handleNodeClick(data) {
      if (data.sheet) {
        this.sheetObj = data
        this.fields = data.fields
        this.jsonArray = data.jsonArray
        const datas = this.jsonArray
        this.$refs.plxTable.reloadData(datas)
      }
    },
    handleCommand(type, field) {
      field.fieldType = type
      this.changeDatasetName()
    },
    changeDatasetName() {
      for (var i = 0; i < this.excelData.length; i++) {
        if (this.excelData[i].excelId == this.sheetObj.sheetExcelId) {
          for (var j = 0; j < this.excelData[i].sheets.length; j++) {
            if (this.excelData[i].sheets[j].excelId == this.sheetObj.sheetId) {
              this.excelData[i].sheets[j] = this.sheetObj
            }
          }
        }
      }
      this.validateName()
    },
    calHeight() {
      const that = this
      setTimeout(function () {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 35 - 10 - 37 - 20 - 10
      }, 10)
    },
    beforeUpload(file) {
      this.uploading = true
    },
    uploadFail(response, file, fileList) {
      let myError = response.toString()
      myError = myError.replace('Error: ', '')

      if (myError.indexOf('AuthenticationException') >= 0) {
        const message = i18n.t('login.tokenError')
        $alert(
          message,
          () => {
            store.dispatch('user/logout').then(() => {
              location.reload()
            })
          },
          {
            confirmButtonText: i18n.t('login.re_login'),
            showClose: false
          }
        )
        return
      }

      const errorMessage =
        JSON.parse(myError).message + ', ' + this.$t('dataset.parse_error')

      this.path = ''
      this.fields = []
      this.sheets = []
      this.data = []
      const datas = this.data
      this.$refs.plxTable?.reloadData(datas)
      this.fileList = []
      this.uploading = false
      this.$message({
        type: 'error',
        message: errorMessage,
        showClose: true
      })
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.excelData.push(response.data)
      this.defaultExpandedKeys.push(response.data.id)
      this.defaultCheckedKeys.push(response.data.sheets[0].id)
      this.$nextTick(() => {
        this.$refs.tree.setCheckedKeys(this.defaultCheckedKeys)
      })
      this.fileList = fileList
    },

    save() {
      var validate = true
      var selectedSheet = []
      var sheetFileMd5 = []
      var effectExtField = false
      var changeFiled = false
      var selectNode = this.$refs.tree.getCheckedNodes()
      if (!this.param.tableId && selectNode.some((ele) => ele.nameExsit)) {
        this.openMessageSuccess('deDataset.cannot_be_duplicate', 'error')
        return
      }
      for (var i = 0; i < selectNode.length; i++) {
        if (selectNode[i].sheet) {
          if (!selectNode[i].datasetName || selectNode[i].datasetName === '') {
            validate = false
            this.openMessageSuccess('dataset.pls_input_name', 'error')
            return
          }
          if (selectNode[i].datasetName.length > 50 && !this.param.tableId) {
            validate = false
            this.openMessageSuccess('dataset.char_can_not_more_50', 'error')
            return
          }
          if (selectNode[i].effectExtField) {
            effectExtField = true
          }
          if (selectNode[i].changeFiled) {
            changeFiled = true
          }
          selectedSheet.push(selectNode[i])
          sheetFileMd5.push(selectNode[i].fieldsMd5)
        }
      }
      if (selectedSheet.length == 0) {
        this.openMessageSuccess('dataset.ple_select_excel', 'error')
        return
      }
      if (!validate) {
        return
      }

      let table = {}
      if (!this.param.tableId) {
        table = {
          id: this.param.tableId,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          sheets: selectedSheet,
          mode: parseInt(this.mode),
          editType: 0
        }
      } else {
        table = {
          id: this.param.tableId,
          name: this.param.table.name,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          sheets: selectedSheet,
          mode: parseInt(this.mode),
          editType: this.param.editType ? this.param.editType : 0
        }
      }

      if (
        this.param.editType === 0 &&
        this.param.tableId &&
        (effectExtField || changeFiled)
      ) {
        const options = {
          title: 'deDataset.replace_the_data',
          confirmButtonText: this.$t('commons.confirm'),
          content:
            '替换可能会影响自定义数据集、关联数据集、仪表板等，是否替换？',
          type: 'primary',
          cb: () => this.saveExcelData(sheetFileMd5, table)
        }
        this.handlerConfirm(options)
      } else {
        this.saveExcelData(sheetFileMd5, table)
      }
    },
    saveExcelData(sheetFileMd5, table) {
      if (
        new Set(sheetFileMd5).size !== sheetFileMd5.length &&
        !this.param.tableId
      ) {
        const options = {
          title: 'dataset.merge_title',
          content: 'dataset.task.excel_replace_msg',
          confirmButtonText: this.$t('dataset.merge'),
          cancelButtonText: this.$t('dataset.no_merge'),
          type: 'primary',
          cb: () => {
            table.mergeSheet = true
            this.loading = true
            post('/dataset/table/update', table)
              .then((response) => {
                this.openMessageSuccess('deDataset.set_saved_successfully')
                this.cancel(response.data)
              })
              .finally(() => {
                this.loading = false
              })
          },
          cancelCb: (action) => {
            if (action === 'close') {
              return
            }
            this.loading = true
            table.mergeSheet = false
            post('/dataset/table/update', table)
              .then((response) => {
                this.openMessageSuccess('deDataset.set_saved_successfully')
                this.cancel(response.data)
              })
              .finally(() => {
                this.loading = false
              })
          }
        }
        this.handlerConfirm(options)
      } else {
        this.loading = true
        post('/dataset/table/update', table)
          .then((response) => {
            this.openMessageSuccess('deDataset.set_saved_successfully')
            this.cancel(response.data)
          })
          .finally(() => {
            this.loading = false
          })
      }
    },
    dataReset() {
      this.searchTable = ''
      this.options = []
      this.dataSource = ''
      this.tables = []
      this.checkTableList = []
    }
  }
}
</script>

<style scoped>
.el-header {
  background-color: var(--ContentBG, rgb(241, 243, 248));
  color: var(--TextActive, #333);
  line-height: 30px;
}

.limit-length-data {
  font-size: 12px;
  color: var(--TableColor, #3d4d66);
}
</style>
<style scoped lang="scss">
.dataset-excel {
  display: flex;
  height: 100%;
  position: relative;
  width: 100%;

  .arrow-right {
    position: absolute;
    z-index: 2;
    top: 15px;
    cursor: pointer;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    height: 24px;
    width: 20px;
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-top-right-radius: 13px;
    border-bottom-right-radius: 13px;
  }
  .table-list {
    p {
      margin: 0;
    }
    height: 100%;
    width: 240px;
    padding: 16px 12px;
    font-family: PingFang SC;
    border-right: 1px solid rgba(31, 35, 41, 0.15);

    .select-ds {
      font-size: 14px;
      font-weight: 500;
      display: flex;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);
      i {
        font-size: 14px;
        color: var(--deTextPlaceholder, #8f959e);
      }
    }

    .search {
      margin: 12px 0;
    }

    .table-checkbox-list {
      height: calc(100% - 100px);
      overflow-y: auto;
      .custom-tree-node {
        position: relative;
        width: 80%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
      .error-name-exsit {
        position: absolute;
        top: 0;
        right: 0;
      }
      .item {
        height: 40px;
        width: 215px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        padding: 12px;

        &:hover {
          background: rgba(31, 35, 41, 0.1);
        }

        &.active {
          background-color: var(--deWhiteHover, #3370ff);
          color: var(--primary, #3370ff);
        }

        .el-checkbox__label {
          overflow: hidden;
        }
      }
    }
  }

  .table-detail {
    font-family: PingFang SC;
    flex: 1;

    .dataset {
      padding: 21px 24px;
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      position: relative;
      .name {
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
      }

      .el-input {
        width: 420px;
        margin-left: 12px;
      }
    }

    .data {
      padding: 16px 24px;
      box-sizing: border-box;
      height: calc(100% - 80px);
      overflow-y: auto;

      .result-num {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-bottom: 16px;
      }

      .type-switch {
        padding: 2px 1.5px;
        display: inline-block;
        cursor: pointer;
        i {
          margin-left: 4px;
          font-size: 12px;
        }
        &:hover {
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
        }
      }
    }
  }
}
</style>
