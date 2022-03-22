<template>
	<view class="page de-main">
		<!-- <swiper indicator-dots="true">
			<swiper-item v-for="(img, key) in imgUrls" :key="key"><image :src="img" /></swiper-item>
		</swiper> -->
		<view class=" ">
			<view class="uni-title">
				
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

			</view>
		</view>
		<!-- <view style="height: 1000rpx;"></view> -->
	</view>
</template>

<script>
import {requestDir} from '@/api/panel'
export default {
	data() {
		return {
			showSwiper: false,
			imgUrls: [
				'../../../static/panelimg/panel2.png',
				'../../../static/panelimg/panel1.png'
				],
			nodes: []
		};
	},
	onLoad() {
	    setTimeout(()=>{
	        this.loadData('panel_list');
	    },350)
	},
	/**
	 * 当 searchInput 配置 disabled 为 true 时触发
	 */
	onNavigationBarSearchInputClicked(e) {
		uni.navigateTo({
			url: './search'
		});
	},
	onPullDownRefresh() {
	    this.loadData('panel_list');
	},
	
	methods: {
	    loadData(pid) {
	        pid = pid || 'panel_list'
	        const param = {pid: pid}
	        
	        requestDir(param).then(res => {
	            this.nodes = res.data
	            uni.stopPullDownRefresh();
	        }).catch(e => {
	            uni.stopPullDownRefresh();
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
.de-main {
	padding-top: 60rpx;
}
</style>
