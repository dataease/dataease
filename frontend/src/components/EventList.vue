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
                <el-tab-pane v-for="item in eventList" :key="item.key" :label="item.label" :name="item.key" style="padding: 0 20px">
                    <el-input v-if="item.key == 'redirect'" v-model="item.param" type="textarea" placeholder="请输入完整的 URL" />
                    <el-input v-if="item.key == 'alert'" v-model="item.param" type="textarea" placeholder="请输入要 alert 的内容" />
                    <el-button style="margin-top: 20px;" @click="addEvent(item.key, item.param)">确定</el-button>
                </el-tab-pane>
            </el-tabs>
        </Modal>
    </div>
</template>

<script>
import { mapState } from 'vuex'
import Modal from '@/components/Modal'
import { eventList } from '@/utils/events'

export default {
    components: { Modal },
    data() {
        return {
            isShowEvent: false,
            eventURL: '',
            eventActiveName: 'redirect',
            eventList,
        }
    },
    computed: mapState([
        'curComponent',
    ]),
    methods: {
        addEvent(event, param) {
            this.isShowEvent = false
            this.$store.commit('addEvent', { event, param })
        },

        removeEvent(event) {
            this.$store.commit('removeEvent', event)
        },
    },
}
</script>

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