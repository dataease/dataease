<template>
  <el-dialog :visible.sync="dialogVisible" width="80%" :destroy-on-close="true" :before-close="close" :append-to-body="true">
    <div>
      <img :src="'/test/case/file/preview/' + file.id" :alt="$t('test_track.case.img_loading_fail')" style="width: 100%;height: 100%;"
           v-if="file.type === 'JPG' || file.type === 'JPEG' || file.type === 'PNG'">
      <div v-if="file.type === 'PDF'">
        <test-case-pdf :file-id="file.id"/>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import TestCasePdf from "@/business/components/track/case/components/TestCasePdf";

export default {
  name: "TestCaseFiles",
  components: {TestCasePdf},
  props: {},
  data() {
    return {
      file: {
        id: '',
        type: ''
      },
      dialogVisible: false,
    }
  },
  methods: {
    open(file) {
      this.file = file;
      this.dialogVisible = true;
    },
    close() {
      this.file = {
        id: '',
        type: ''
      };
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
