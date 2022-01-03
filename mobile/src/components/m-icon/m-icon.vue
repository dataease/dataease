<template>
	<view class="m-icon" :class="['m-icon-'+type]" @click="onClick()"></view>
</template>

<script>
	export default {
		props: {
			/**
			 * 图标类型
			 */
			type: String
		},
		methods: {
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>

<style>
	@import "./m-icon.css";
</style>
