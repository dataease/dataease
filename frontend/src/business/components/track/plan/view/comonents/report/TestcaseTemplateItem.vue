<template>
  <div class="testcase-template" @click="templateEdit">
    <div class="template-img">
      <i class="el-icon-error" @click.stop="templateDelete"/>
    </div>
    <span class="demonstration">{{ template.name }}</span>
  </div>
</template>

<script>
    export default {
      name: "TestcaseTemplateItem",
      props: {
        template: {
          type: Object,
          default() {
            return {}
          }
        }
      },
      methods: {
        templateEdit() {
          this.$emit('templateEdit', this.template.id);
        },
        templateDelete() {
          this.$alert(this.$t('load_test.delete_file_confirm') + this.template.name + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this.handleDelete();
              }
            }
          });
        },
        handleDelete() {
          this.$post('/case/report/template/delete/' + this.template.id, {}, () => {
            this.$success(this.$t('commons.delete_success'));
            this.$emit('refresh');
          });
        }
      }
    }
</script>

<style scoped>

  .testcase-template {
    display: inline-block;
    margin: 10px 30px;
    width: 150px;
  }

  .demonstration {
    display: block;
    text-align: center;
    margin: 10px auto;
    width: 150px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .template-img {
    height: 100px;
    width: 80px;
    margin: 0 auto;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 2px #fff;
    box-sizing: border-box;
    border-radius: 3px;
    background: url(../../../../../../../assets/template.png) no-repeat center;
  }

  .template-img:hover {
    border: solid 1px #4b8fdf;
    border-radius: 3px;
    color: deepskyblue;
    cursor: pointer;
  }

  .template-img > i{
    display:none;
    float: right;
    color: gray;
    margin: 2px;
  }

  .template-img > i:hover {
    color: red;
  }

  .template-img:hover > .el-icon-error {
    display: inline;
  }

</style>
