<template>
  <el-dialog
    :title="$t('commons.about_us')"
    :visible.sync="dialogVisible" class="about-us">

    <el-row>
      <el-col>
        <el-link :underline="false" :href="websiteUrl" target="_blank">
          <img class="logo" src="../../../../assets/favicon-彩色.png"/>
        </el-link>
        <el-link class="url" :href="websiteUrl" target="_blank">
          <span>{{websiteUrl}}</span>
        </el-link>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <div class="github">
          <el-link :underline="false" :href="githubUrl" target="_blank">
            <font-awesome-icon class="github-icon" :icon="['fab', 'github-square']"/>
          </el-link>
        </div>
        <el-link class="url" :href="githubUrl" target="_blank">
          <span>{{githubUrl}}</span>
        </el-link>
      </el-col>
    </el-row>

    <el-row>
      <el-col class="version">
        <span>版本:</span> &nbsp;
        <span>{{version}}</span>
      </el-col>
    </el-row>

  </el-dialog>

</template>

<script>
  export default {
    name: "AboutUs",
    data() {
      return {
        dialogVisible: false,
        githubUrl: 'https://github.com/metersphere/metersphere',
        websiteUrl: 'https://metersphere.io',
        version: '1.0.1'
      }
    },
    created() {
      this.getVersion();
    },
    methods: {
      open() {
        this.dialogVisible = true;
      },
      getVersion() {
        this.$get('/system/version', response => {
          this.version = response.data;
        });
      }
    }
  }
</script>

<style scoped>

  .logo {
    height: 30px;
    line-height: 30px;
    vertical-align: middle
  }

  .version {
    height: 30px;
    line-height: 30px;
    margin-left: 5px;
  }

  .github-icon {
    font-size: 20px;
    margin-left: 5px;
  }

  .github {
    height: 30px;
    width: 30px;
    line-height: 30px;
    display: inline-block;
  }


  .el-row {
    margin-bottom: 3%;
  }

  .url {
    margin-left: 5px;
  }

  .about-us >>> .el-dialog {
    width: 500px;
  }

</style>
