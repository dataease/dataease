<template>
	<view class="page dataease-main">
        <view>
            <uni-list-item class="person-title" :title="banner.title" />
        </view>
        <!-- <uni-list>
            <uni-list-item class="person-title" :title="banner.title" />
        </uni-list> -->
        <swiper  class="swiper-box" style="flex: 1;" :duration="300" >
            <swiper-item class="swiper-item" >
            <uni-list >
                    
                <uni-list-item v-for="(node, index) in nodes" :key="index" 
                :title="node.text"  
                :showArrow="node.type === 'folder'"
                :thumb="node.type === 'folder' ? '../../../static/folder.png' : '../../../static/yibiaobans.png'" 
                thumb-size="base" 
                clickable
                @click="clickHandler(node)"
                rightText="" />
                
                
            </uni-list>
            </swiper-item>
        </swiper>
	</view>
</template>

<script>
import {requestDir} from '@/api/panel'
export default {
	data() {
		return {
			showSwiper: false,
			imgUrls: [
				'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/b4b60b10-5168-11eb-bd01-97bc1429a9ff.jpg',
				'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/b1dcfa70-5168-11eb-bd01-97bc1429a9ff.jpg'
			],

            nodes: [],
            banner: {}
		};
	},
    onLoad(event) {
        
        const payload = event.detailDate || event.payload;
			// 目前在某些平台参数会被主动 decode，暂时这样处理。
        try {
            this.banner = JSON.parse(decodeURIComponent(payload));
        } catch (error) {
            this.banner = JSON.parse(payload);
        }
        uni.setNavigationBarTitle({
            title: this.banner.title
        });
        setTimeout(()=>{
            
            this.loadData(this.banner.id);
        },350)
    },
	

    methods: {
        loadData(pid) {
            pid = pid || 'panel_list'
            const param = {pid: pid}
            
            requestDir(param).then(res => {
                this.nodes = res.data
            }).catch(e => {

            })
        },
        clickHandler(node) {
            const param = {
                    id: node.id,
                    title: node.text,
                    index: 4,
                    userId: node.userId
                }
            if(node.type === 'panel') {
                
                uni.navigateTo({
                    url: '../home/detail?detailDate=' + encodeURIComponent(JSON.stringify(param))
                });
                return
            }
            uni.navigateTo({
                url: './folder?detailDate=' + encodeURIComponent(JSON.stringify(param))
            });
        }
    }
};
</script>

<style>
image,
swiper,
.img-view {
	width: 750rpx;
	width: 100%;
	height: 500rpx;
}
.page-section-title {
	margin-top: 50rpx;
}

.dataease-main {
    
    position: fixed;
    left: var(--window-left);
    right: var(--window-right);
    padding: 5px;
    height: calc(100vh - 10px);
    
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
.uni-list{
    overflow-y: scroll;
    height: 100%;
}

</style>
