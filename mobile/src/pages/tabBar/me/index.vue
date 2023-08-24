<template>
    <view class="page dataease-main">
        
        <view>
            <uni-card  >
                <view class="uni-flex uni-row">
                    <view class="text uni-flex" style="width: 200rpx;height: 220rpx;-webkit-justify-content: center;justify-content: center;-webkit-align-items: center;align-items: center;">
                        <image src="../../../static/user.png" style="width: 150rpx;height: 150rpx;"></image>
                    </view>
                    <view class="uni-flex uni-column" style="-webkit-flex: 1;flex: 1;">
                        <view class="text" style="height: 90rpx;text-align: left;padding-left: 20rpx;padding-top: 40rpx; front-size: 25rpx;">
                            {{user.nickName}}
                        </view>
                        <view class="uni-flex uni-row" style="padding-left: 20rpx;">
                            <view class="text" style="-webkit-flex: 1;flex: 1;">{{roleNames}}</view>
                            <view class="more-info" style="-webkit-flex: 1;flex: 1;color: #409eff;" @click="showMore">{{$t('me.moreInfo')}}</view>
                            
                        </view>
                    </view>
                </view>
            </uni-card>
        </view>

        <view>
            <uni-list>
                
                <uni-list-item :title="$t('navigate.language')" clickable  showArrow thumb="../../../static/language.png" thumb-size="base" @click="toLanguage" :rightText="languageName" />
                <uni-list-item :title="$t('navigate.about')" clickable  showArrow thumb="../../../static/about.png" thumb-size="base" @click="toAbout" :rightText="$t('me.systemInfo')" />
            </uni-list>
        </view>

        <view class="uni-padding-wrap uni-common-mt">
            <button type="warn" @click="logout">{{$t('me.logout')}}</button>
        </view>
    </view>
</template>
<script>
import { setToken, getLanguage } from '@/common/utils'
import { getUserInfo, setUserInfo } from '@/common/utils'
import { logout } from '@/api/auth'
export default {
    data() {
        return {
            user: null,
            roleNames: ''
        }
    },
    created() {
        this.user = getUserInfo()
        this.roleNames = this.user.roles.map(role => role.name).toString()
    },
    onLoad(event) {
        
    },
    computed: {
        languageName() {
            const key = getLanguage();
            if(key === 'sys') {
                return this.$t('commons.sysLanguage')
            }
            if(key === 'en') {
                return this.$t('commons.en')
            }
            if(key === 'zh-Hans') {
                return this.$t('commons.zh')
            }
            if(key === 'zh-Hant') {
                return this.$t('commons.tw')
            }
        }
    },
    methods: {
        showMore() {
            uni.navigateTo({
                url: './person'
            });
        },
        toLanguage() {
            uni.navigateTo({
                url: './language'
            });
        },
        toAbout() {
            uni.navigateTo({
                url: './about'
            });
        },
        logout() {
            const callBack = () => {
                setToken(null)
                setUserInfo(null)
                uni.reLaunch({
                    url: '/'
                });
            }
            logout().then(res => {callBack()}).catch(e => {callBack()})
            
        }
    }
}
</script>

<style scoped>
.dataease-main {
    
    position: fixed;
    left: var(--window-left);
    right: var(--window-right);
    padding: 5px;
    height: calc(100vh - 90px);
    
}
.uni-card {
    margin: 0px;
}
.uni-padding-wrap {
    padding: 0px;
}
</style>