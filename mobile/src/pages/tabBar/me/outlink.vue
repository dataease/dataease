<template>
    <view>
        <web-view 
        :src="urls[banner.linkIndex]" >                
        </web-view>
    </view>
</template>

<script>
export default {
    data() {
        return {
        urls: [
            'http://mivicelab.com',
            'http://mivicelab.com'
        ],
        banner: {}
        }
    },
    onLoad(event) {
        // TODO 后面把参数名替换成 payload
        const payload = event.detailDate || event.payload;
        // 目前在某些平台参数会被主动 decode，暂时这样处理。
        try {
            this.banner = JSON.parse(decodeURIComponent(payload));
        } catch (error) {
            this.banner = JSON.parse(payload);
        }
        
        
    },
}
</script>