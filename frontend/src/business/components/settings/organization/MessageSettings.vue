<template>
  <div>
    <el-card>
      <el-tabs class="system-setting" v-model="activeName">
        <el-tab-pane :label="$t('organization.message_settings')" name="message">
          <message-notification/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import MessageNotification from "./TaskNotification"

export default {
  name: "MessageSettings",
  components: {
    MessageNotification
  },
  data(){
    return{
      activeName: 'message'
    }
  }
}
</script>

<style scoped>

</style>
