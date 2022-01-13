<template>
	<view class="uni-card uni-border" :class="{ 'uni-card--full': isFull === true || isFull === 'true', 'uni-card--shadow': isShadow === true || isShadow === 'true'}">
		<!-- 基础 -->
		<view v-if="mode === 'basic' && title" @click.stop="onClick" class="uni-card__head-padding">
			<view class="uni-card__header uni-border-bottom">
				<slot name="header">
					<view v-if="thumbnail" class="uni-card__header-extra-img-view">
						<image :src="thumbnail" class="uni-card__header-extra-img" />
					</view>
					<text class="uni-card__header-title-text">{{ title }}</text>
					<text v-if="extra" class="uni-card__header-extra-text">{{ extra }}</text>
				</slot>
			</view>
		</view>
		<!-- 标题 -->
		<view v-if="mode === 'title'" @click.stop="onClick" class="uni-card__head-padding">
			<view class="uni-card__title uni-border-bottom">
				<slot name="header">
					<view class="uni-card__title-box">
						<view v-if="thumbnail" class="uni-card__title-header">
							<image class="uni-card__title-header-image" :src="thumbnail" mode="scaleToFill" />
						</view>
						<view class="uni-card__title-content">
							<text class="uni-card__title-content-title uni-ellipsis">{{ title }}</text>
							<text class="uni-card__title-content-extra uni-ellipsis">{{ subTitle }}</text>
						</view>
					</view>
					<view v-if="extra">
						<text class="uni-card__header-extra-text">{{ extra }}</text>
					</view>
				</slot>
			</view>
		</view>
		<!-- 图文 -->
		<view v-if="mode === 'style'" class="uni-card__thumbnailimage" @click.stop="onClick">
			<view class="uni-card__thumbnailimage-box">
				<image v-if="thumbnail" class="uni-card__thumbnailimage-image" :src="thumbnail" mode="aspectFill" />
				<uni-icons v-if="!thumbnail" type="image" size="30" color="#999" />
			</view>
			<view v-if="title" class="uni-card__thumbnailimage-title">
				<text class="uni-card__thumbnailimage-title-text">{{ title }}</text>
			</view>
		</view>
		<!-- 内容 -->
		<view class="uni-card__content uni-card__content--pd" @click.stop="onClick">
			<view v-if="mode === 'style' && extra" class=""><text class="uni-card__content-extra">{{ extra }}</text>
			</view>
			<slot />
		</view>
		<!-- 底部 -->
		<view v-if="note" class="uni-card__footer uni-border-top">
			<slot name="footer">
				<text class="uni-card__footer-text">{{ note }}</text>
			</slot>
		</view>
	</view>
</template>

<script>
	/**
	 * Card 卡片
	 * @description 卡片视图组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=22
	 * @property {String} title 标题文字
	 * @property {String} subTitle 副标题（仅仅mode=title下生效）
	 * @property {String} extra 标题额外信息
	 * @property {String} note 底部信息
	 * @property {String} thumbnail 标题左侧缩略图
	 * @property {String} mode = [basic|style|title] 卡片模式
	 * 	@value basic 基础卡片
	 * 	@value style 图文卡片
	 * 	@value title 标题卡片
	 * @property {Boolean} isFull = [true | false] 卡片内容是否通栏，为 true 时将去除padding值
	 * @property {Boolean} isShadow = [true | false] 卡片内容是否开启阴影
	 * @event {Function} click 点击 Card 触发事件
	 * @example <uni-card title="标题文字" thumbnail="xxx.jpg" extra="额外信息" note="Tips">内容主体，可自定义内容及样式</uni-card>
	 */
	export default {
		name: 'UniCard',
		emits: ['click'],
		props: {
			title: {
				type: String,
				default: ''
			},
			subTitle: {
				type: String,
				default: ''
			},
			extra: {
				type: String,
				default: ''
			},
			note: {
				type: String,
				default: ''
			},
			thumbnail: {
				type: String,
				default: ''
			},
			mode: {
				type: String,
				default: 'basic'
			},
			isFull: {
				// 内容区域是否通栏
				type: Boolean,
				default: false
			},
			isShadow: {
				// 是否开启阴影
				type: [Boolean, String],
				default: false
			}
		},
		methods: {
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style scoped>
	.uni-card {
		/* #ifndef APP-NVUE */
		display: flex;
		flex: 1;
		box-shadow: 0 0 0 rgba(0, 0, 0, 0);
		/* #endif */
		margin: 12px 15px;
		background-color: #ffffff;
		position: relative;
		flex-direction: column;
		border-radius: 5px;
		overflow: hidden;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-border {
		position: relative;
		/* #ifdef APP-NVUE */
		border-color: #e5e5e5;
		border-style: solid;
		border-width: 0.5px;
		/* #endif */
		z-index: 1;
	}

	/* #ifndef APP-NVUE */
	.uni-border:after {
		content: "";
		position: absolute;
		bottom: 0;
		left: 0;
		top: 0;
		right: 0;
		border: 1px solid #e5e5e5;
		border-radius: 10px;
		box-sizing: border-box;
		width: 200%;
		height: 200%;
		transform: scale(0.5);
		transform-origin: left top;
		z-index: -1;
	}

	/* #endif */
	.uni-border-bottom {
		position: relative;
		/* #ifdef APP-NVUE */
		border-bottom-color: #e5e5e5;
		border-bottom-style: solid;
		border-bottom-width: 0.5px;
		/* #endif */
		z-index: 1;
	}

	/* #ifndef APP-NVUE */
	.uni-border-bottom:after {
		content: "";
		position: absolute;
		bottom: 0;
		left: 0;
		top: 0;
		right: 0;
		border-bottom: 1px solid #e5e5e5;
		box-sizing: border-box;
		width: 200%;
		height: 200%;
		transform: scale(0.5);
		transform-origin: left top;
		z-index: -1;
	}

	/* #endif */
	.uni-border-top {
		position: relative;
		/* #ifdef APP-NVUE */
		border-top-color: #e5e5e5;
		border-top-style: solid;
		border-top-width: 0.5px;
		/* #endif */
		z-index: 1;
	}

	/* #ifndef APP-NVUE */
	.uni-border-top:after {
		content: "";
		position: absolute;
		bottom: 0;
		left: 0;
		top: 0;
		right: 0;
		border-top: 1px solid #e5e5e5;
		box-sizing: border-box;
		width: 200%;
		height: 200%;
		transform: scale(0.5);
		transform-origin: left top;
		z-index: -1;
	}

	/* #endif */
	.uni-card__thumbnailimage {
		position: relative;
		/* #ifndef APP-NVUE */
		/* #endif */
		flex-direction: column;
		justify-content: center;
		height: 150px;
		background-color: #F1F1F1;
		overflow: hidden;
	}

	.uni-card__thumbnailimage-box {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex: 1;
		height: 150px;
		flex-direction: row;
		justify-content: center;
		align-items: center;
		overflow: hidden;
	}

	.uni-card__thumbnailimage-image {
		flex: 1;
	}

	.uni-card__thumbnailimage-title {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		flex-direction: row;
		padding: 8px 12px;
		background-color: rgba(0, 0, 0, 0.4);
	}

	.uni-card__thumbnailimage-title-text {
		flex: 1;
		font-size: 14px;
		color: #fff;
	}

	.uni-card__title {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		padding: 10px;
	}

	.uni-card__title-box {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex: 1;
		flex-direction: row;
		align-items: center;
		overflow: hidden;
	}

	.uni-card__title-header {
		width: 40px;
		height: 40px;
		overflow: hidden;
		border-radius: 5px;
		padding-right: 10px;
	}

	.uni-card__title-header-image {
		width: 40px;
		height: 40px;
	}

	.uni-card__title-content {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		justify-content: center;
		flex: 1;
		height: 40px;
		overflow: hidden;
	}

	.uni-card__title-content-title {
		font-size: 14px;
		line-height: 22px;
	}

	.uni-card__title-content-extra {
		font-size: 12px;
		line-height: 27px;
		color: #999;
	}

	.uni-card__header {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		position: relative;
		flex-direction: row;
		padding: 12px;
		align-items: center;
	}

	.uni-card__header-title {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		margin-right: 8px;
		justify-content: flex-start;
		align-items: center;
	}

	.uni-card__header-title-text {
		font-size: 16px;
		flex: 1;
		color: #333;
	}

	.uni-card__header-extra-img {
		height: 20px;
		width: 20px;
		margin-right: 8px;
	}

	.uni-card__header-extra-text {
		flex: 1;
		margin-left: 8px;
		font-size: 12px;
		text-align: right;
		color: #999;
	}

	.uni-card__content {
		color: #333;
	}

	.uni-card__content--pd {
		padding: 12px;
	}

	.uni-card__content-extra {
		font-size: 14px;
		padding-bottom: 10px;
		color: #999;
	}

	.uni-card__footer {
		justify-content: space-between;
		padding: 12px;
	}

	.uni-card__footer-text {
		color: #999;
		font-size: 12px;
	}

	.uni-card--shadow {
		position: relative;
		/* #ifndef APP-NVUE */
		box-shadow: 0px 0px 5px 1px rgba(0, 0, 0, 0.1);
		/* #endif */
	}

	.uni-card--full {
		margin: 0;
		border-radius: 0;
	}

	/* #ifndef APP-NVUE */
	.uni-card--full:after {
		border-radius: 0;
	}

	/* #endif */
	.uni-ellipsis {
		/* #ifndef APP-NVUE */
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
		/* #endif */
		/* #ifdef APP-NVUE */
		lines: 1;
		/* #endif */
	}
</style>