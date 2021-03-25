<template>
  <div>
    <el-form ref="createOrganization" inline :model="form" size="small" label-width="80px">

      <el-form-item label="链接分享">
        <el-switch
          v-model="valid"
          style="width: 370px;"
          :active-value="true"
          :inactive-value="false"
        />
      </el-form-item>
      <el-form-item label=" ">
        <el-link class="de-link" style="width: 370px;" disabled>开启链接后，任何人可通过此链接访问仪表板。</el-link>
      </el-form-item>
      <el-form-item v-if="valid" label="链接">
        <el-input
          v-model.number="form.uri"
          disabled
          style="width: 370px;"
        />
      </el-form-item>

      <el-form-item v-if="valid" label=" ">
        <el-checkbox v-model="form.enablePwd">密码保护</el-checkbox>

        <span v-if="form.enablePwd" class="de-span">{{ form.pwd }}</span>
        <span v-if="form.enablePwd" class="de-span"><el-link :underline="false" type="primary">重置</el-link></span>
      </el-form-item>

      <div v-if="valid" class="auth-root-class">
        <span slot="footer">

          <el-button v-if="!form.enablePwd" v-clipboard:copy="form.uri" v-clipboard:success="onCopy" v-clipboard:error="onError" type="primary">复制链接及</el-button>
          <el-button v-if="form.enablePwd" v-clipboard:copy="form.uri + ' 密码: '+ form.pwd" v-clipboard:success="onCopy" v-clipboard:error="onError" type="primary">复制链接及密码</el-button>

        </span>
      </div>

    </el-form>
  </div>
</template>
<script>
export default {

  name: 'LinkGenerate',
  components: {

  },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      valid: false,
      form: {},
      defaultForm: { enablePwd: false, pwd: '000000', uri: 'http://baidu.com' }
    }
  },
  created() {
    this.form = this.defaultForm
  },
  methods: {
    onCopy(e) {
    //   alert('You just copied: ' + e.text)
    },
    onError(e) {
    //   alert('Failed to copy texts')
    }
  }
}
</script>

<style lang="scss" scoped>
    .de-link{
        justify-content: left !important;
    }
    .de-span {
        margin: 0 15px;
    }
    .auth-root-class {
        margin: 15px 0px 5px;
        text-align: right;
    }
</style>
