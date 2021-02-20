<template>
    <span>
       <el-upload
         action="#"
         class="api-body-upload"
         list-type="picture-card"
         :http-request="upload"
         :beforeUpload="uploadValidate"
         :file-list="parameter.files"
         ref="upload">

         <div class="upload-default">
           <i class="el-icon-plus"/>
         </div>
           <div class="upload-item" slot="file" slot-scope="{file}" >
             <span>{{file.file ? file.file.name : file.name}}</span>
             <span class="el-upload-list__item-actions">
              <!--<span v-if="!disabled" class="el-upload-list__item-delete" @click="handleDownload(file)">-->
              <!--<i class="el-icon-download"/>-->
              <!--</span>-->
              <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
                <i class="el-icon-delete"/>
              </span>
             </span>
        </div>

      </el-upload>
    </span>
</template>

<script>
    export default {
      name: "MsApiBodyFileUpload",
      data() {
        return {
          disabled: false,
        };
      },
      props: {
        parameter: Object,
        default() {
          return {}
        }
      },
      methods: {
        handleRemove(file) {
          this.$refs.upload.handleRemove(file);
          for (let i = 0; i < this.parameter.files.length;  i++) {
            let fileName = file.file ? file.file.name : file.name;
            let paramFileName = this.parameter.files[i].file ?
              this.parameter.files[i].file.name : this.parameter.files[i].name;
            if (fileName === paramFileName) {
              this.parameter.files.splice(i, 1);
              this.$refs.upload.handleRemove(file);
              break;
            }
          }
        },
        upload(file) {
          this.parameter.files.push(file);
        },
        uploadValidate(file) {
          if (file.size / 1024 / 1024 > 500) {
            this.$warning(this.$t('api_test.request.body_upload_limit_size'));
            return false;
          }
          return true;
        },
      },
      created() {
        if (!this.parameter.files) {
          this.parameter.files = [];
        }
      }
    }
</script>

<style scoped>

  .el-upload {
    background-color: black;
  }

  .api-body-upload >>> .el-upload {
    height: 32px;
    width: 32px;
  }

  .upload-default {
    min-height: 32px;
    width: 32px;
    line-height: 32px;
  }

  .el-icon-plus {
    font-size: 16px;
  }

  .api-body-upload >>> .el-upload-list__item {
    height: 32px;
    width: auto;
    padding: 6px;
    margin-bottom: 0px;
  }

  .api-body-upload >>> .el-upload-list--picture-card {
  }

  .api-body-upload {
    min-height: 32px;
    border: 1px solid #EBEEF5;
    padding: 2px;
    border-radius: 4px;
  }

  .upload-item {
  }

</style>
