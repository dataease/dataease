<template>
<view class="page dataease-main">
    <uni-list>
        <uni-list-item class="person-title" :title="$t('navigate.personInfo')" />
    </uni-list>
    <swiper  class="swiper-box" style="flex: 1;" :duration="300" >
        <swiper-item class="swiper-item" >
            <uni-list>
                <uni-list-item :title="$t('me.organization')" :rightText="me.deptName"/>
                <uni-list-item :title="$t('me.email')" :rightText="user.email" />
                <uni-list-item :title="$t('me.createTime')"  :rightText="me.timeStr" />
            </uni-list>
        </swiper-item>
    </swiper>
</view>
</template>
<script>
import {getUserInfo} from '@/common/utils'
import {requestMe} from '@/api/panel'
export default {
    data() {
        return {
            user: null,
            me: {}
        }
    },
    created() {
        this.user = getUserInfo()
        this.load()
    },
    onLoad(event) {
        
    },
    methods: {
        load() {
            requestMe().then(res => {
                if(res.success && res.data)
                    this.me = res.data
                if(this.me.createTime) {
                    this.me.timeStr = new Date(this.me.createTime).format('yyyy-MM-dd')
                }
            })
        }
    }
}
</script>
<style  scoped>

.dataease-main {
    
    position: fixed;
    left: var(--window-left);
    right: var(--window-right);
    padding: 5px;
    height: calc(100vh - 90px);
    
}
.swiper-box {
    flex: 1;
    margin-top: 5px;
    background-color: #ffffff;
    height: calc(100vh - 60px);
}

.swiper-item {
    flex: 1;
    flex-direction: row;
}
.person-title {
    font-weight: 700;
    font-size: 15px;
    text-align: center;
    overflow: hidden;
    white-space: nowrap;
    
    text-overflow: ellipsis;
    
}

</style>