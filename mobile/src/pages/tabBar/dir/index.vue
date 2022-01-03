<template>
	<view class="page dataease-main">

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
	/**
	 *  点击导航栏 buttons 时触发
	 */
	/* onNavigationBarButtonTap() {
		uni.showModal({
			title: '提示',
			content: '用户点击了功能按钮，这里仅做展示。',
			success: res => {
				if (res.confirm) {
					console.log('用户点击了确定');
				}
			}
		});
	} */

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
                    index: 4
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
    top: 40px;
    padding: 5px;
    height: calc(100vh - 90px);
    
}
.swiper-box {
    flex: 1;
    background-color: #ffffff;
    height: calc(100vh - 100px);
}

.swiper-item {
    flex: 1;
    flex-direction: row;
}

.uni-list {
    overflow-y: scroll;
    height: calc(100vh - 100px);
}

</style>
