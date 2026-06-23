<template>
	<el-dialog v-dialogDrag top="5vh" title="语音通话" :close-on-click-modal="false" :close-on-press-escape="false"
		:visible.sync="isShow" width="50%" :append-to-body="true" :modal-append-to-body="true">
		<div class='rtc-group-video'>
			<div style="padding-top:60px;font-weight: 600; text-align: center;font-size: 18px;">
				多人音视频通话
			</div>
			<div style="padding-top:20px; text-align: center;font-size: 14px;color: #999;">
				功能开发中
			</div>
		</div>
	</el-dialog>
</template>

<script>
export default {
	name: "rtcGroupVideo",
	data() {
		return {
			isShow: false
		}
	},
	methods: {
		open() {
			this.isShow = true;
		},
		onRTCMessage() {
			//this.isShow = true;
		}
	}
}
</script>

<style lang="scss" scoped>
.rtc-group-video {
	height: 300px;
	background-color: #E8F2FF;
}
</style>