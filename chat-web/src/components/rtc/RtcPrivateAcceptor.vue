<template>
	<div class="rtc-private-acceptor">
		<head-image :id="friend.id" :name="friend.nickName" :url="friend.headImage" :size="100"
			:isShowUserInfo="false"></head-image>
		<div class="acceptor-text">
			{{ tip }}
		</div>
		<div class="btn-group">
			<div class="icon iconfont icon-phone-accept accept" @click="$emit('accept')" title="接受"></div>
			<div class="icon iconfont icon-phone-reject reject" @click="$emit('reject')" title="拒绝"></div>
		</div>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
	name: "rtcPrivateAcceptor",
	components: {
		HeadImage
	},
	data() {
		return {}
	},
	props: {
		mode: {
			type: String
		},
		friend: {
			type: Object
		}
	},
	computed: {
		tip() {
			let modeText = this.mode == "video" ? "视频" : "语音"
			return `${this.friend.nickName} 请求和您进行${modeText}通话...`
		}
	}
}
</script>

<style scoped lang="scss">
.rtc-private-acceptor {
  position: absolute; display: flex; flex-direction: column; align-items: center;
  right: 5px; bottom: 5px; width: 250px; height: 250px; padding: 20px;
  background-color: #2c2c2e; box-shadow: none; border-radius: 4px;

  .acceptor-text { padding: 10px; text-align: center; font-size: 16px; color: rgba(255,255,255,0.72); }

  .btn-group {
    display: flex; justify-content: space-around; margin-top: 20px; width: 100%;
    .icon { font-size: 60px; cursor: pointer; border-radius: 50%; }
    .accept { color: #30d158; }
    .reject { color: #ff453a; }
  }
}
</style>