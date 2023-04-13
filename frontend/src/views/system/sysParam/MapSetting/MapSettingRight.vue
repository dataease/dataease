<template>
  <div>
    <!--基础配置表单-->
    <el-form
      v-if="!['read-only', 'empty'].includes(status)"
      ref="formInline"
      v-loading="loading"
      :model="formInline"
      :rules="rules"
      class="demo-form-inline"
      size="small"
    >
      <el-row>
        <el-row>
          <el-col>
            <el-form-item
              :label="$t('map_setting.parent_area')"
              prop="pCode"
            >
              <el-tree-select
                v-if="treeShow"
                ref="deSelectTree"
                v-model="formInline.pCode"
                popper-append-to-body
                popover-class="map-class-wrap"
                :data="treeData"
                :select-params="selectParams"
                :tree-params="treeParams"
                :filter-node-method="_filterFun"
                :tree-render-fun="_renderFun"
                @searchFun="_searchFun"
                @node-click="changeNode"
                @select-clear="selectClear"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item
              :label="$t('map_setting.area_code')"
              prop="code"
            >
              <el-input
                v-model="formInline.code"
                :placeholder="$t('map_setting.please_input')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col>
            <el-form-item
              :label="$t('map_setting.area_name')"
              prop="name"
            >
              <el-input
                v-model="formInline.name"
                maxlength="30"
                show-word-limit
                :placeholder="$t('map_setting.please_input')"
              />

            </el-form-item>
          </el-col>
        </el-row>

      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('map_setting.geo_json')">
            <el-upload
              style="float: right;margin-left: 10px"
              class="upload-demo"
              action=""
              accept=".json"
              :on-exceed="handleExceed"
              :before-upload="uploadValidate"
              :on-error="handleError"
              :show-file-list="false"
              :file-list="filesTmp"
              :http-request="uploadMapFile"
            >
              <el-button
                style="display: inline-block"
                size="mini"
                type="success"
                plain
              >
                {{ $t('commons.upload') }}
              </el-button>
            </el-upload>
            <el-button
              style="float:right;margin-top: 3px"
              size="mini"
              type="danger"
              plain
              @click="removeFile"
            >
              {{ $t('commons.clear') }}
            </el-button>
            <el-input
              v-model="formInline.fileName"
              disabled
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div>

        <el-button
          type="success"
          size="small"
          @click="save('formInline')"
        >
          {{ $t('commons.save') }}
        </el-button>
      </div>
    </el-form>

    <el-descriptions
      v-else-if="status === 'read-only'"
      title="区域信息"
      :column="1"
    >

      <el-descriptions-item :label="$t('map_setting.area_code')">{{ nodeInfo.code }}</el-descriptions-item>

      <el-descriptions-item :label="$t('map_setting.area_name')">{{ nodeInfo.name }}</el-descriptions-item>

      <el-descriptions-item :label="$t('map_setting.parent_name')">{{ nodeInfo.pname }}</el-descriptions-item>

      <el-descriptions-item :label="$t('map_setting.geo_json')">
        <json-view :data="json" />
      </el-descriptions-item>
    </el-descriptions>

    <el-empty
      v-else-if="status === 'empty'"
      :description="$t('sysParams.select_left')"
    />
  </div>
</template>

<script>
import jsonView from 'vue-json-views'
import { geoJson, saveMap } from '@/api/map/map'
import ElTreeSelect from '@/components/elTreeSelect'
import msgCfm from '@/components/msgCfm'

export default {
  name: 'MapSettingRight',
  components: { jsonView, ElTreeSelect },
  mixins: [msgCfm],
  props: {
    status: {
      type: String,
      default: 'empty'
    },
    treeData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      formInline: { pCode: '', fileName: '' },
      loading: false,
      rules: {
        pCode: [
          {
            required: true,
            message: this.$t('map_setting.parent_name') + this.$t('commons.cannot_be_null'),
            trigger: 'change'
          }
        ],
        code: [
          {
            required: true,
            message: this.$t('map_setting.area_code') + this.$t('commons.cannot_be_null'),
            trigger: ['change', 'blur']
          },
          { pattern: /^\d{9}$/, message: this.$t('map_setting.area_code_tip'), trigger: ['change', 'blur'] }
        ],
        name: [
          {
            required: true,
            message: this.$t('map_setting.area_name') + this.$t('commons.cannot_be_null'),
            trigger: ['change', 'blur']
          }
        ],
        fileName: [
          {
            required: true,
            message: this.$t('map_setting.geo_json') + this.$t('commons.cannot_be_null'),
            trigger: 'change'
          }
        ]
      },
      levelOptions: [
        { value: 1, label: '国家' },
        { value: 2, label: '一级行政区划(省)' },
        { value: 3, label: '二级行政区划(市)' },
        { value: 4, label: '三级行政区划(区县)' },
        { value: 5, label: '四级行政区划(乡镇)' }
      ],
      suffixes: new Set(['json']),
      errList: [],
      filesTmp: [],

      nodeInfo: {
        code: -1,
        name: '',
        level: -1,
        pcode: -1,
        pname: ''
      },
      noGsoJson: { success: false, message: 'no json file' },
      json: {},
      selectParams: {
        clearable: true,
        placeholder: this.$t('commons.please_select') + this.$t('map_setting.parent_name')
      },
      treeParams: {
        showParent: true,
        clickParent: true,
        filterable: true,
        // 只想要子节点，不需要父节点
        leafOnly: false,
        includeHalfChecked: false,
        'check-strictly': false,
        'default-expand-all': false,
        'expand-on-click-node': false,
        'render-content': this._renderFun,
        data: [],
        props: {
          children: 'children',
          label: 'name',
          rootId: '000000000',
          disabled: 'disabled',
          parentId: 'pcode',
          value: 'code'
        }
      },
      treeShow: true
    }
  },
  watch: {
    treeData: function(val) {
      this.treeParams.data = val
    }
  },
  methods: {
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'))
    },
    handleError() {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'))
    },
    uploadValidate(file) {
      const suffix = file.name.substring(file.name.lastIndexOf('.') + 1)
      if (!this.suffixes.has(suffix)) {
        this.$warning(this.$t('test_track.case.import.upload_limit_format'))
        return false
      }

      if (file.size / 1024 / 1024 > 30) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'))
        return false
      }
      this.errList = []
      return true
    },
    uploadMapFile(file) {
      this.$set(this.formInline, 'fileName', file.file.name)
      this.formInline.file = file.file
    },
    removeFile() {
      this.formInline.fileName = null
      this.formInline.file = null
    },
    buildFormData(file, files, param) {
      const formData = new FormData()
      if (file) {
        formData.append('file', file)
      }
      if (files) {
        files.forEach(f => {
          formData.append('files', f)
        })
      }
      formData.append('request', new Blob([JSON.stringify(param)], { type: 'application/json' }))
      return formData
    },
    save(formInline) {
      const param = {
        code: this.formInline.code,
        name: this.formInline.name,
        pcode: this.formInline.pCode,
        plevel: this.formInline.pLevel
      }

      this.$refs[formInline].validate(valid => {
        if (valid) {
          this.saveHandler(param)
        }
      })
    },
    saveHandler(param) {
      const formData = this.buildFormData(this.formInline.file, null, param)
      saveMap(formData).then(response => {
        const flag = response.success
        if (flag) {
          this.$emit('refresh-tree', param)
          this.openMessageSuccess('commons.save_success')
        } else {
          this.$message.error(this.$t('commons.save_failed'))
        }
      })
    },

    emitAdd(form) {
      this.formInline = JSON.parse(JSON.stringify(form))
      this.treeShow = false
      this.$nextTick(() => {
        this.treeShow = true
      })
    },

    loadForm(form) {
      if (form?.code) {
        this.nodeInfo = JSON.parse(JSON.stringify(form))
        this.setGeoJson()
      }
    },
    setGeoJson() {
      if (!this.nodeInfo?.code) {
        this.json = JSON.parse(JSON.stringify(this.noGsoJson))
        return
      }
      const cCode = this.nodeInfo.code
      if (this.$store.getters.geoMap[cCode]) {
        const json = this.$store.getters.geoMap[cCode]
        this.json = JSON.parse(JSON.stringify(json))
        return
      }

      geoJson(cCode).then(res => {
        this.$store.dispatch('map/setGeo', {
          key: cCode,
          value: res
        }).then(() => {
          this.json = JSON.parse(JSON.stringify(res))
        })
      })
    },
    _filterFun(value, data, node) {
      if (!value) return true
      return data.id.toString().indexOf(value.toString()) !== -1
    },
    // 树过滤
    _searchFun(value) {
      this.$refs.deSelectTree.filterFun(value)
    },
    // 自定义render
    _renderFun(h, { node, data, store }) {
      const { props, clickParent } = this.treeParams
      return (
        <span
          class={['custom-tree-node', !clickParent && data[props.children] && data[props.children].length ? 'disabled' : null]}
        >
          <span>{node.label}</span>
        </span>
      )
    },
    changeNode(data, node) {
      if (node.level > 4) {
        this.$error('不支持4级行政级别')
        this.formInline.pLevel = null
        this.formInline.pCode = null
        return
      }
      this.formInline.pLevel = node.level
    }
  }
}
</script>

<style lang="scss" scoped>
.map-class-wrap {
  top: 65px !important;
  left: 0px !important;
}
</style>
