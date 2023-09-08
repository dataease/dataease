<template>
<view class="page dataease-main">
    <uni-list>
        <uni-list-item class="person-title" :title="$t('navigate.language')" />
    </uni-list>
    <swiper  class="swiper-box" style="flex: 1;" :duration="300" >
        <swiper-item class="swiper-item" >
            <radio-group @change="radioChange">
                <label class="uni-list-cell uni-list-cell-pd" v-for="(item,index) in items" :key="index">
                    <view>{{item.text}}</view>
                    <view>
                        <radio :value="item.value" :checked="item.value === language" />
                    </view>
                </label>
            </radio-group>
        </swiper-item>
    </swiper>
</view>
</template>

<script>
import {setLanguage, getLanguage} from '@/common/utils'
export default {
    data() {
        return {
            
            language: 'sys'
        }
    },
    computed: {
        items() {
            return [
                {value: 'sys', text: this.$t('commons.sysLanguage')},                
                {value: 'zh-Hans', text: this.$t('commons.zh')},
                {value: 'zh-Hant', text: this.$t('commons.tw')},
                {value: 'en', text: this.$t('commons.en')}
            ]
        }
    },
    onLoad(e) {
        this.language = getLanguage()
        
    },
    methods: {
        radioChange(node) {
            this.language = node.detail.value
            if(node.detail.value === 'sys') {
                uni.getSystemInfo({
                    success: res => {
                        var local = res.language === 'zh-CN' ? 'zh-Hans' : res.language === 'zh-TW' ? 'zh-Hant' : 'en'
                        uni.setLocale(local)   
                        this.$i18n.locale = local
                    }
                })
                      
                
            }else {
                uni.setLocale(node.detail.value)   
                this.$i18n.locale = node.detail.value
                
            }
                         
            setLanguage(node.detail.value)

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