<script setup lang="ts">
import Modal from '@/components/data-visualization/Modal'
import { eventList } from '@/utils/events'
import { ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { eventStoreWithOut } from '@/store/modules/data-visualization/event'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const eventStore = eventStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

const isShowEvent = ref(false)
const eventURL = ref('')
const eventActiveName = ref('redirect')

const addEvent = (event, param) => {
  isShowEvent.value = false
  eventStore.addEvent({ event, param })
}

const removeEvent = event => {
  eventStore.removeEvent(event)
}
</script>

<template>
  <div class="event-list">
    <div class="div-events">
      <el-button @click="isShowEvent = true">添加事件</el-button>
      <div>
        <el-tag
          v-for="event in Object.keys(curComponent.events)"
          :key="event"
          closable
          @close="removeEvent(event)"
        >
          {{ event }}
        </el-tag>
      </div>
    </div>

    <!-- 选择事件 -->
    <Modal v-model="isShowEvent">
      <el-tabs v-model="eventActiveName">
        <el-tab-pane
          v-for="item in eventList"
          :key="item.key"
          :label="item.label"
          :name="item.key"
          style="padding: 0 20px"
        >
          <el-input
            v-if="item.key == 'redirect'"
            v-model="item.param"
            type="textarea"
            placeholder="请输入完整的 URL"
          />
          <el-input
            v-if="item.key == 'alert'"
            v-model="item.param"
            type="textarea"
            placeholder="请输入要 alert 的内容"
          />
          <el-button style="margin-top: 20px" @click="addEvent(item.key, item.param)"
            >确定</el-button
          >
        </el-tab-pane>
      </el-tabs>
    </Modal>
  </div>
</template>

<style lang="scss" scoped>
.event-list {
  .div-events {
    text-align: center;
    padding: 0 20px;

    .el-button {
      display: inline-block;
      margin-bottom: 10px;
    }

    .el-tag {
      display: block;
      width: 50%;
      margin: auto;
      margin-bottom: 10px;
    }
  }
}
</style>
