<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <el-row>
      <el-col>

        <el-form
          ref="DsForm"
          :model="form"
          :rules="rule"
          size="small"
          :disabled="disabled"
          label-width="180px"
          label-position="right"
        >
          <el-form-item :label="$t('end_point')" prop="configuration.end_point">
            <el-input v-model="form.configuration.end_point" autocomplete="off"/>
          </el-form-item>


          <el-form-item :label="$t('projectName')" prop="configuration.projectName">
            <el-input v-model="form.configuration.projectName" autocomplete="off"/>
          </el-form-item>


          <el-form-item :label="$t('access_id')" prop="configuration.access_id">
            <el-input v-model="form.configuration.access_id" autocomplete="off"/>
          </el-form-item>

          <el-form-item :label="$t('access_key')" prop="configuration.access_key">
            <el-input v-model="form.configuration.access_key" autocomplete="off" show-password/>
          </el-form-item>

          <el-form-item :label="$t('datasource.extra_params')">
            <el-input v-model="form.configuration.extraParams" autocomplete="off"/>
          </el-form-item>

          <el-form-item  :label="$t('query_timeout')">
            <el-input
                v-model="form.configuration.queryTimeout"
                autocomplete="off"
                type="number"
                :min="0"
              >
                <template slot="append">{{ $t("second") }}</template>
              </el-input>
          </el-form-item>

        </el-form>

      </el-col>
    </el-row>
  </div>
</template>

<script>


import messages from '@/de-base/lang/messages'

export default {
  name: "maxcompute",
  components: {},
  props: {
    method: String,
    request: {},
    response: {},
    editApiItem: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showScript: {
      type: Boolean,
      default: true,
    },
    obj: {
      type: Object,
      default() {
        return {
          configuration: {
            initialPoolSize: 5,
            extraParams: '',
            minPoolSize: 5,
            maxPoolSize: 50,
            maxIdleTime: 30,
            acquireIncrement: 5,
            idleConnectionTestPeriod: 5,
            connectTimeout: 5,
            queryTimeout: 30
          },
          apiConfiguration: []
        }
      }
    },
  },
  data() {
    return {
      rule: {
        'configuration.end_point': [{required: true, message: this.$t('commons.required'), trigger: 'blur'}],
        'configuration.projectName': [{required: true, message: this.$t('commons.required'), trigger: 'blur'}],
        'configuration.access_id': [{required: true, message: this.$t('commons.required'), trigger: 'blur'}],
        'configuration.access_key': [{required: true, message: this.$t('commons.required'), trigger: 'blur'}],

      },
      canEdit: false,
      originConfiguration: {},
      height: 500,
      disabledNext: false
    }
  },
  computed: {
    form() {
      return this.obj.form
    },
    disabled() {
      return this.obj.disabled
    }
  },
  created() {
    this.$emit('on-add-languages', messages)
  },
  watch: {},
  methods: {
    validate() {
      let status = null;
      this.$refs["DsForm"].validate((val) => {
        if (val) {
          status = true
        } else {
          status = false
        }
      })
      return status
    }
  }
}
</script>

<style scoped>
.ms-query {
  background: #409EFF;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.ms-header {
  background: #409EFF;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.request-tabs {
  margin: 20px;
  min-height: 200px;
}

.ms-el-link {
  float: right;
  margin-right: 45px;
}
</style>
