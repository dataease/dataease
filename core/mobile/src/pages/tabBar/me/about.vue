<template>
<view class="page dataease-main">
    <uni-list>
        <uni-list-item class="person-title" :title="$t('navigate.about')" />
    </uni-list>
    <swiper  class="swiper-box" style="flex: 1;" :duration="300" >
        <swiper-item class="swiper-item" >
            <view class="uni-center" style=" font-size:0;padding: 40px 40px 20px;">
                <image class="image" mode="widthFix"  src="../../../static/DataEase-01.png" />
            </view>
            <view class="person-lic">
                  
                  <span v-if="license.edition === 'Enterprise'">{{$t('me.enterprise')}}</span>
                  <span v-else>{{$t('me.standard')}}</span>
            </view>
            <view class="person-lic">
                <span>{{build}}</span>
            </view>
            <uni-list>
                <uni-list-item clickable showArrow thumb-size="base" :title="$t('me.userManual')" @click="toUserManual" />
                            
                <uni-list-item clickable showArrow thumb-size="base" :title="$t('me.community')" @click="toGithub"/>
            </uni-list>
        </swiper-item>
    </swiper>
</view>
</template>

<script>
import{buildVersion, validate} from '@/api/auth'
export default {
    data() {
        return {
            build: null,
            license: {}
        }
    },
    onLoad(event) {
        this.initVersion()
    },
    methods: {
        toGithub() {
            
            window.location.href = 'https://github.com/dataease/dataease'
        },
        toUserManual() {
            const param = {linkIndex: 1}
            uni.navigateTo({
                url: './outlink?detailDate=' + encodeURIComponent(JSON.stringify(param))
            });
        },
        initVersion() {
            buildVersion().then(res => {
                this.build = res.data
            })
        },
        getLicenseInfo() {
    
            this.validateHandler({}, res => {
                this.license = this.getLicense(res.data)
            })
        },
        validateHandler(param, success) {
            validate(param).then(success)
        },
        getLicense(result) {
            return {
                status: result.status,
                corporation: result.license ? result.license.corporation : '',
                expired: result.license ? result.license.expired : '',
                count: result.license ? result.license.count : '',
                version: result.license ? result.license.version : '',
                edition: result.license ? result.license.edition : ''
            }
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
.person-lic {
    font-weight: 500;
    font-size: 15px;
    text-align: center;
    overflow: hidden;
    white-space: nowrap;
    height: 50px;
    text-overflow: ellipsis;
    display: flex;
    align-items: center;
    justify-content: center;
}
.image {
    width: 185px;
}
</style>