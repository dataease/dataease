<template>
	<view class="page de-main">
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
    <uni-popup ref="popup" position="top" @change="popupChange">
      <view class="action-sheet-container">
        <view class="action-sheet-content-container">
          <view
            class="action-sheet-item"
            v-for="(item, index) in pickerValueArray"
            :key="index"
            :class="{ 'action-sheet-item-selected': selectedIndex === index }"
            @click="onActionSheetItemClick(index)"
          >
            {{ item.label }}
          </view>
        </view>
        <view class="action-sheet-close-container">
          <view class="action-sheet-item" @click="closePopup">
            关闭
          </view>
        </view>
      </view>
    </uni-popup>
	</view>
</template>

<script>
import { requestDir } from '@/api/panel'
import uniPopup from '@/components/uni-popup/uni-popup.vue'
import { treeSort } from '@/common/utils'
export default {
  components: {
    uniPopup
  },
	data() {
		return {
			showSwiper: false,
			imgUrls: [
				'../../../static/panelimg/panel2.png',
				'../../../static/panelimg/panel1.png'
				],
			nodes: [],
			pickerValueArray: [{
					label: this.$t('dir.create-time-asc'),
					value: 'time_asc'
				},
				{
					label: this.$t('dir.create-time-desc'),
					value: 'time_desc'
				},
				{
					label: this.$t('dir.create-name-asc'),
					value: 'name_asc'
				},
				{
					label:  this.$t('dir.create-name-desc'),
					value: 'name_desc'
				}
			],
      selectedIndex: 1,
      originResourceTree: [],
      curSortType: 'time_desc'
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
              this.originResourceTree = res.data
              if (localStorage.getItem('TreeSort-panel')) {
                this.curSortType = localStorage.getItem('TreeSort-panel')
                this.sortTypeChange(this.curSortType)
              } else {
                this.nodes = res.data
              }
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
	    },
      onActionSheetItemClick(index) {
        this.selectedIndex = index;
        this.sortTypeChange(this.pickerValueArray[index].value)
        const that = this
        setTimeout(() => {
          that.closePopup()
        }, 500)
      },
      onNavigationBarButtonTap(e) {
        let that = this;
        if (e.index === 0) {
          this.$refs.popup.open('bottom')
          uni.hideTabBar()
        }
      },
      popupChange(e) {
        if (!e.show) {
          uni.showTabBar()
        }
      },
      closePopup() {
        this.$refs.popup.close()
      },
      sortTypeChange(sortType) {
        this.nodes = treeSort(this.originResourceTree, this.curSortType, sortType)
        this.curSortType = sortType
        localStorage.setItem('TreeSort-panel', this.curSortType)
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
.action-sheet-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
}
.action-sheet-content-container {
  background-color: #FFFFFF;
  bottom: 0;
  left: 0;
  right: 0;
}
.action-sheet-close-container {
  background-color: #FFFFFF;
  bottom: 0;
  left: 0;
  right: 0;
  margin-top: 1px;
}
 
.action-sheet-item {
  padding: 10px;
  text-align: center;
  font-size: 18px;
  color: #666666;
}
 
.action-sheet-item-selected {
  color: #007AFF;
  font-weight: bold;
}
</style>
