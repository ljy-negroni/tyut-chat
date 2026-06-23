<template>
	<div class="friend-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="friend-avatar">
			<head-image :size="42" :name="friend.nickName" :url="friend.headImage" :online="friend.online">
			</head-image>
		</div>
		<div class="friend-info">
			<div class="friend-name">{{ friend.nickName }}</div>
			<div class="friend-online">
				<i class="el-icon-monitor online" v-show="friend.onlineWeb" title="电脑设备在线">
					<span class="online-icon"></span>
				</i>
			</div>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
		<slot></slot>
	</div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from "../common/RightMenu.vue";

export default {
	name: "frinedItem",
	components: {
		HeadImage,
		RightMenu
	},
	data() {
		return {
			menuItems: [{
				key: 'CHAT',
				name: '发送消息',
				icon: 'el-icon-chat-dot-round'
			}, {
				key: 'DELETE',
				name: '删除好友',
				icon: 'el-icon-delete'
			}]
		}
	},
	methods: {
		showRightMenu(e) {
			if (this.menu) {
				this.$refs.rightMenu.open(e, this.menuItems);
			}
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase());
		}
	},
	props: {
		active: {
			type: Boolean
		},
		friend: {
			type: Object
		},
		menu: {
			type: Boolean,
			default: true
		}
	}

}
</script>

<style scope lang="scss">
.friend-item {
	height: 50px;
	display: flex;
	position: relative;
	align-items: center;
	white-space: nowrap;
	border-radius: 10px;
	margin: 0 3px;
	padding: 5px 8px;
	cursor: pointer;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background-color: var(--im-background-active-dark);
	}

	.friend-avatar {
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.friend-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 10px;
		text-align: left;

		.friend-name {
			font-size: var(--im-font-size);
			white-space: nowrap;
			overflow: hidden;
		}

		.friend-online {
			.online {
				color: rgba(255,255,255,0.45);
				padding-right: 2px;
				font-size: 14px;
				position: relative;
			}

			.online-icon {
				position: absolute;
				right: 0;
				bottom: 0;
				width: 5px;
				height: 5px;
				background: #30d158;
				border-radius: 50%;
				
			}
		}
	}
}
</style>
