<template>
	<div class="head-image" @click="showUserInfo($event)" :style="{ cursor: isShowUserInfo ? 'pointer' : null }">
		<img class="avatar-image" v-show="url" :src="url" :style="avatarImageStyle" loading="lazy" />
		<div class="avatar-text" v-show="!url" :style="avatarTextStyle">
			{{ avaterText }}</div>
		<div v-show="online" class="online" title="用户当前在线"></div>
		<slot></slot>
	</div>
</template>

<script>
export default {
	name: "headImage",
	data() {
		return {
			// macOS Vibrancy 暗调色板：白字深底，每人一个微妙色调
			colors: ["#3a3a3c", "#4a4a58", "#3d3848", "#3c4450",
				"#484842", "#463a3e", "#3e4248", "#45404c"]
		}
	},
	props: {
		id: {
			type: Number
		},
		size: {
			type: Number,
			default: 42
		},
		width: {
			type: Number
		},
		height: {
			type: Number
		},
		radius: {
			type: String,
			default: "50%"
		},
		url: {
			type: String
		},
		name: {
			type: String,
			default: null
		},
		online: {
			type: Boolean,
			default: false
		},
		isShowUserInfo: {
			type: Boolean,
			default: true
		}
	},
	methods: {
		showUserInfo(e) {
			if (!this.isShowUserInfo) return;
			if (this.id && this.id > 0) {
				this.$http({
					url: `/user/find/${this.id}`,
					method: 'get'
				}).then((user) => {
					let pos = {
						x: e.x + 30,
						y: e.y
					}
					this.$eventBus.$emit("openUserInfo", user, pos);
				})
			}
		},
		isChinese(charCode) {
			return charCode >= 0x4e00 && charCode <= 0x9fa5;
		}
	},
	computed: {
		avatarImageStyle() {
			let w = this.width ? this.width : this.size;
			let h = this.height ? this.height : this.size;
			return `width:${w}px; height:${h}px;
					border-radius: ${this.radius}; border: 1px solid rgba(0,0,0,0.06);`
		},
		avatarTextStyle() {
			let w = this.width ? this.width : this.size;
			let h = this.height ? this.height : this.size;
			return `width: ${w}px;height:${h}px;
				background: ${this.textColor}; color: rgba(255,255,255,0.88);
				font-size:${w * 0.42}px; font-weight: 600;
				border-radius: ${this.radius}; border: 1px solid rgba(0,0,0,0.06);`
		},
		avaterText() {
			if (!this.name) return '';
			if (this.isChinese(this.name.charCodeAt(0))) {
				return this.name.charAt(0)
			} else {
				return this.name.charAt(0).toUpperCase() + this.name.charAt(1)
			}
		},
		textColor() {
			if (!this.name) return 'fff';
			let hash = 0;
			for (var i = 0; i < this.name.length; i++) {
				hash += this.name.charCodeAt(i);
			}
			return this.colors[hash % this.colors.length];
		}
	}
}
</script>

<style scoped lang="scss">
.head-image {
	position: relative;

	.avatar-image {
		position: relative;
		overflow: hidden;
		display: block;
	}

	.avatar-text {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.online {
		position: absolute;
		right: -5px;
		bottom: 0;
		width: 12px;
		height: 12px;
		background: #30d158;
		border-radius: 50%;
		
	}
}
</style>
