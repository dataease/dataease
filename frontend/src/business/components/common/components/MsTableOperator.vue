<template>
  <span>
    <slot name="front"></slot>
    <ms-table-operator-button :isTesterPermission="isTesterPermission" :tip="tip1" icon="el-icon-edit" @exec="editClick" @click.stop="editClickStop"/>
    <slot name="middle"></slot>
    <ms-table-operator-button :isTesterPermission="isTesterPermission" :tip="tip2" icon="el-icon-delete" type="danger" @exec="deleteClick" @click.stop="deleteClickStop"/>
    <slot name="behind"></slot>
  </span>

</template>

<script>
    import MsTableOperatorButton from "./MsTableOperatorButton";
    export default {
      name: "MsTableOperator",
      components: {MsTableOperatorButton},
      props: {
        tip1: {
          type: String,
          default() {
            return this.$t('commons.edit');
          }
        },
        tip2: {
          type: String,
          default() {
            return this.$t('commons.delete');
          }
        },
        isTesterPermission: {
          type: Boolean,
          default: false
        }
      },
      methods: {
        editClick() {
          this.$emit('editClick');
        },
        editClickStop() {
          this.$emit('editClickStop');
        },
        deleteClick() {
          this.$emit('deleteClick');
        },
        deleteClickStop() {
          this.$emit('deleteClickStop');
        }
      }
    }
</script>

<style scoped>

</style>
