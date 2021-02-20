<template>
  <div style="margin-left: 120px">
    <el-button type="primary" size="mini" :disabled="!show" @click="testConnection">
      {{ $t('ldap.test_connect') }}
    </el-button>
    <el-button v-if="showEdit" size="mini" @click="edit">
      {{ $t('commons.edit') }}
    </el-button>
    <el-button type="primary" v-if="showSave" size="mini" @click="save">
      {{ $t('commons.save') }}
    </el-button>
    <el-button v-if="showCancel" size="mini" @click="cancelEdit">
      {{ $t('organization.integration.cancel_edit') }}
    </el-button>
    <el-button type="info" size="mini" :disabled="!show" @click="cancelIntegration">
      {{ $t('organization.integration.cancel_integration') }}
    </el-button>
  </div>
</template>

<script>

export default {
  name: "BugManageBtn",
  data() {
    return {
      showEdit: true,
      showSave: false,
      showCancel: false,
    }
  },
  props: {
    show: {
      type: Boolean,
      default: true
    },
    form: Object,
  },
  methods: {
    testConnection() {
      this.$emit("testConnection");
    },
    edit() {
      this.$emit("update:show", false);
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
    },
    cancelEdit() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.$emit("update:show", true);
      this.init();
    },
    init() {
      this.$emit("init");
    },
    save() {
      this.$emit("save");
    },
    cancelIntegration() {
      this.$emit("cancelIntegration");
    }
  }
}
</script>

<style scoped>

</style>
