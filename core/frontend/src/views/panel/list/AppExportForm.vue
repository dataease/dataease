<template>
  <el-drawer
    v-closePress
    :title="$t('app_template.app_export')"
    :visible.sync="applyDownloadDrawer"
    custom-class="de-user-drawer"
    size="600px"
    direction="rtl"
  >
    <div class="app-export">
      <el-form
        ref="applyDownloadForm"
        :model="form"
        :rules="rule"
        size="small"
        class="de-form-item"
        label-width="180px"
        label-position="right"
      >
        <el-form-item
          :label="$t('app_template.app_name')"
          prop="appName"
        >
          <el-input
            v-model="form.appName"
            autocomplete="off"
            :placeholder="$t('commons.input_name')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('app_template.app_version')"
          prop="version"
        >
          <el-input
            v-model="form.version"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="$t('app_template.program_version')"
          prop="required"
        >
          <el-input
            v-model="form.required"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="$t('app_template.creator')"
          prop="creator"
        >
          <el-input
            v-model="form.creator"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="$t('commons.description')"
          prop="description"
        >
          <deTextarea
            v-model="form.description"
            class="w100-textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <div
      class="app-export-bottom"
    >
      <div
        class="apply"
        style="width: 100%"
      >
        <deBtn
          secondary
          @click="close"
        >{{ $t('commons.cancel') }}
        </deBtn>

        <deBtn
          type="primary"
          @click="downloadApp"
        >{{ $t('app_template.export') }}
        </deBtn>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import i18n from '@/lang/index'
import deTextarea from '@/components/deCustomCm/DeTextarea.vue'
import msgCfm from '@/components/msgCfm'

export default {
  name: 'AppExportForm',
  components: {
    deTextarea
  },
  mixins: [msgCfm],
  props: {},
  data() {
    return {
      applyDownloadDrawer: false,
      form: {
        appName: null,
        icon: null,
        version: null,
        creator: null,
        required: '1.16.0',
        description: null
      },
      rule: {
        appName: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        creator: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        required: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        version: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  created() {
  },
  methods: {
    init(params) {
      this.applyDownloadDrawer = true
      this.form = params
    },
    close() {
      this.$emit('closeDraw')
      this.applyDownloadDrawer = false
    },
    downloadApp() {
      this.$refs.applyDownloadForm.validate(valid => {
        if (valid) {
          this.$emit('downLoadApp', this.form)
          this.applyDownloadDrawer = false
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>

.app-export{
  width: 100%;
  height: calc(100% - 56px);
}

.app-export-bottom{
  width: 100%;
  height: 56px;
  text-align: right;
}

::v-deep .el-drawer__body{
  padding-bottom: 0px!important;
}

</style>
