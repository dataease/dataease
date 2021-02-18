<template>
  <div class="app-container">
    <h2>切换admin、editor、readonly用户看到不同的内容</h2>
    <br/>

    <div class="permission-block admin" v-permission="['admin']">
      需要admin角色才能看到, 指令设置：v-permission="['admin']"
    </div>

    <div class="permission-block editor" v-permission="['editor']">
      需要editor角色才能看到, 指令设置：v-permission="['editor']"
    </div>

    <div class="permission-block admin-editor" v-permission="['admin', 'editor']">
      需要admin或者editor角色才能看到, 指令设置：v-permission="['admin', 'editor']"
    </div>

    <div class="permission-block">
      任何人都能看到
    </div>
  </div>
</template>

<script>
export default {
  name: "PermissionDemo"
}
</script>

<style lang="scss" scoped>
.permission-block {
  width: 100%;
  height: 40px;
  line-height: 40px;
  font-size: 18px;

  &.admin {
    color: #2D61A2;
  }

  &.admin-editor {
    color: mix(#2D61A2, #FFBA00)
  }

  &.editor {
    color: #FFBA00;
  }
}
</style>
