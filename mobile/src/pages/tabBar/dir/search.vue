<template>
	<view class="wrapper">
		<view v-if="isHistory" class="history-box">
			<view v-if="historyList.length > 0">
				<view class="history-title">
					<text>{{$t('dir.searchHistory')}}</text>
					<text class="uni-icon uni-icon-trash" @click="clearSearch"></text>
				</view>
				<view class="history-content">
					<view class="history-item" v-for="(item, index) in historyList" clickable @click="clickHandler(item)" :key="index">
						{{ item.name }}
					</view>
				</view>
			</view>
			<view v-else class="no-data">{{$t('dir.noHistory')}}</view>
		</view>
		<view v-else class="history-box">
			<view v-if="historyList.length > 0" class="history-list-box">
				<view
					class="history-list-item"
					v-for="(item, index) in historyList"
					:key="index"
					@click="listTap(item)"
				>
					<rich-text :nodes="item.nameNodes"></rich-text>
				</view>
			</view>
			<view v-else class="no-data">没有搜索到相关内容</view>
		</view>
	</view>
</template>

<script>
import util from '@/components/amap-wx/js/util.js';
import {requestDir} from '@/api/panel'
export default {
	data() {
		return {
			historyList: [],
			isHistory: true,
			list: [],
			flng: true,
			timer: null
		};
	},
	onLoad() {		
		// 本示例用的是高德 sdk ，请根据具体需求换成自己的服务器接口。
		this.amapPlugin = util.mapInit();
		this.historyList = uni.getStorageSync('search:history');		
	},
	created() {
		this.historyList = uni.getStorageSync('search:history');
	},
	methods: {
		/**
		 * 列表点击
		 */
		listTap(item) {
			item = JSON.parse(JSON.stringify(item));
			// 如果当前是历史搜索页面 ，那么点击不储存,直接 跳转
			if (this.history) {
				return;
			} else {
				this.isHistory = true;

				// 去做一些相关搜索功能 ，这里直接返回到上一个页面
				// 点击列表存储搜索数据
				util.setHistory(item);
				// uni.navigateBack();
                this.clickHandler(item)
			}
		},
		/**
		 * 清理历史搜索数据
		 */
		clearSearch() {
			uni.showModal({
				title: this.$t('commons.tips'),
				content: this.$t('dir.clearConfirm'),
				success: res => {
					if (res.confirm) {
						this.historyList = util.removeHistory();
					}
				}
			});
		},
		/**
		 * 关键字搜索
		 * 调用高德地图关键字搜索api
		 */
		getInputtips(val) {
            const param = {name: val}
            requestDir(param).then(res => {
                let dataObj = res.data
                dataObj && dataObj.map(item => {
                    item.name = item.text
                    return util.dataHandle(item, val);
                });
                this.historyList = dataObj;
            })
			
		},
        clickHandler(node) {
            const param = {
                id: node.id,
                title: node.text,
                index: 4,
                userId: node.userId
            }
			this.isHistory= false
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
        
	},
	/**
	 * 当 searchInput 输入时触发
	 */
	onNavigationBarSearchInputChanged(e) {
		let text = e.text;
		if (!text) {
			this.isHistory = true;
			this.historyList = [];
			this.historyList = uni.getStorageSync('search:history');

			return;
		} else {
			this.isHistory = false;
			this.getInputtips(text);
		}
	},
	/**
	 * 点击软键盘搜索按键触发
	 */
	onNavigationBarSearchInputConfirmed(e) {
		/* let text = e.text;
		if (!text) {
			this.isHistory = true;
			this.historyList = [];
			this.historyList = uni.getStorageSync('search:history');
			uni.showModal({
				title: this.$t('commons.tips'),
				content: this.$t('searchPlaceholder'),
				success: res => {
					if (res.confirm) {
					}
				}
			});
			return;
		} else {
			uni.showModal({
				title: this.$t('commons.tips'),
                content: this.$t('dir.contentPrefix') + text +  this.$t('dir.contentSuffix'),
				success: res => {
					if (res.confirm) {
						util.setHistory(text);
						uni.navigateBack();
					}
				}
			});
		} */
	},
	/**
	 *  点击导航栏 buttons 时触发
	 */
	onNavigationBarButtonTap() {
		uni.showModal({
			title: this.$t('commons.tips'),
			content: '点击确定，修改输入框的内容为abc',
			success: res => {
				if (res.confirm) {
					const currentWebview = this.$mp.page.$getAppWebview();
					currentWebview.setTitleNViewSearchInputText("abc");
				}
			}
		});
	}
};
</script>

<style>
.history-title {
	display: flex;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	padding-bottom: 0;
	font-size: 34rpx;
	color: #333;
}
.history-title .uni-icon {
	font-size: 40rpx;
}
.history-content {
	display: flex;
	flex-wrap: wrap;
	padding: 15rpx;
}
.history-item {
	padding: 4rpx 35rpx;
	border: 1px #f1f1f1 solid;
	background: #fff;
	border-radius: 50rpx;
	margin: 12rpx 10rpx;
	color: #999;
}
.history-list-box {
	/* margin: 10rpx 0; */
}
.history-list-item {
	padding: 30rpx 0;
	margin-left: 30rpx;
	border-bottom: 1px #EEEEEE solid;
	font-size: 28rpx;
}

.no-data {
	text-align: center;
	color: #999;
	margin: 100rpx;
}
</style>
