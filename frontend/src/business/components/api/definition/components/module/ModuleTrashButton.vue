<template>
  <div @click="exe" class="recycle" :class="{'is-active': condition.trashEnable}">
    <i class="el-icon-delete">  回收站</i>
  </div>
</template>

<script>
    export default {
      name: "ModuleTrashButton",
      props: {
        condition: {
          type: Object,
          default() {
            return {}
          }
        },
        exe: {
          type: Function
        },
      },
      methods: {
        // enableTrash() {
        //
        // }
      }
    }
</script>

<style scoped>

  .recycle {
    padding-left: 25px;
    margin-top: 15px;
    height: 26px;
    line-height: 26px;
    margin-bottom: -10px;
  }

  .recycle:hover {
    color: #6d317c;
    cursor: pointer;
  }

  .is-active {
    background-color: #f3f6f9;
  }

</style>
