<template>
  <div>
    <ms-tag v-for="(role, index) in roles"
            :key="index"
            :effect="effect"
            :type="type"
            :content="$t('role.' + role.id)"/>
  </div>
</template>

<script>
  import MsTag from "./MsTag";

  export default {
    name: "MsRolesTag",
    components: {MsTag},
    props: {
      type: {
        type: String,
        default: 'primary',
      },
      roles: {
        type: Array
      },
      effect: {
        type: String,
        default: 'dark',
      }
    }
  }
</script>

<style scoped>

</style>
