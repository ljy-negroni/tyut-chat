<template>
	<div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="chat-left">
			<head-image :url="conversation.headImage" :name="conversation.showName" :size="42"
				:id="isPrivate ? conversation.targetId : 0" :isShowUserInfo="false" :online="online"></head-image>
			<div v-show="!conversation.isDnd && conversation.unreadCount > 0" class="unread-text">{{
				conversation.unreadCount }}</div>
		</div>
		<div class="chat-right">
			<div class="chat-name">
				<div class="chat-name-text">
					<div>{{ conversation.showName }}</div>
					<el-tag v-if="isGroup" type="primary" size="mini">群</el-tag>
					<el-tag v-if="conversation.isTop" type="warning" size="mini">置顶</el-tag>
				</div>
				<div class="chat-time-text">{{ showTime }}</div>
			</div>
			<div class="chat-content">
				<div class="chat-at-text">{{ atText }}</div>
				<div class="chat-send-name" v-show="isShowSendName">{{ conversation.sendNickName + ':&nbsp;' }}</div>
				<div class="chat-content-text"
					v-html="$emo.transform($str.html2Escape(conversation.lastContent), 'emoji-small')"></div>
				<div class="icon iconfont icon-dnd" v-if="conversation.isDnd"></div>
			</div>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
	</div>

</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from '../common/RightMenu.vue';

export default {
	name: "chatItem",
	components: {
		HeadImage,
		RightMenu
	},
	data() {
		return {
		}
	},
	props: {
		conversation: {
			type: Object
		},
		active: {
			type: Boolean
		}
	},
	methods: {
		showRightMenu(e) {
			this.$refs.rightMenu.open(e, this.menuItems);
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.conversation);
		}
	},
	computed: {
		isShowSendName() {
			return !!this.conversation.sendNickName;
		},
		showTime() {
			return this.$date.toTimeText(this.conversation.lastSendTime, true)
		},
		atText() {
			if (this.conversation.atMe) {
				return "[有人@我]"
			} else if (this.conversation.atAll) {
				return "[@全体成员]"
			}
			return "";
		},
		menuItems() {
			let items = [];
			items.push({
				key: 'TOP',
				name: this.conversation.isTop ? '取消置顶' : '置顶'
			});
			if (this.conversation.isDnd) {
				items.push({
					key: 'DND',
					name: '新消息提醒'
				})
			} else {
				items.push({
					key: 'DND',
					name: '消息免打扰'
				})
			}
			items.push({
				key: 'DELETE',
				name: '删除聊天',
				danger: true
			})
			return items;
		},
		online() {
			if (this.isPrivate) {
				let friend = this.friendStore.findFriend(this.conversation.targetId);
				return friend && friend.online;
			}
			return false;
		},
		isPrivate() {
			return this.$enums.CONVERSATION_TYPE.PRIVATE == this.conversation.type
		},
		isGroup() {
			return this.$enums.CONVERSATION_TYPE.GROUP == this.conversation.type
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-item {
	height: 58px;
	display: flex;
	position: relative;
	margin: 2px 6px;
	padding: 5px 10px;
	align-items: center;
	background-color: transparent;
	white-space: nowrap;
	cursor: pointer;
	border-radius: 10px;
	transition: background-color 0.2s ease;

	&:hover {
		background-color: var(--im-background-active);
	}

	&.active {
		background: #2c2c2e;

		.chat-name-text {
			color: var(--im-color-primary);
			font-weight: 600;
		}
	}

	.chat-left {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: center;
		flex-shrink: 0;

		.unread-text {
			position: absolute;
			background-color: #FF3B30;
			right: -6px;
			top: -6px;
			color: #FFFFFF;
			border-radius: 10px;
			padding: 1px 5px;
			font-size: 10px;
			font-weight: 700;
			text-align: center;
			white-space: nowrap;
			border: 2px solid #1c1c1e;
			min-width: 18px;
			height: 18px;
			display: flex;
			align-items: center;
			justify-content: center;
			box-shadow: none;
			z-index: 2;
		}
	}

	.chat-right {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding-left: 12px;
		text-align: left;
		overflow: hidden;
		min-width: 0;

		.chat-name {
			display: flex;
			line-height: 24px;
			height: 24px;

			.chat-name-text {
				flex: 1;
				display: flex;
				align-items: center;
				gap: 4px;
				font-size: var(--im-font-size);
				font-weight: 500;
				white-space: nowrap;
				overflow: hidden;
				color: var(--im-text-color);
			}

			.chat-time-text {
				font-size: 11px;
				text-align: right;
				color: var(--im-text-color-lighter);
				white-space: nowrap;
				overflow: hidden;
				padding-left: 8px;
				flex-shrink: 0;
			}
		}

		.chat-content {
			display: flex;
			line-height: 22px;
			height: 22px;

			.chat-at-text {
				color: #FF3B30;
				font-size: 11px;
				flex-shrink: 0;
			}

			.chat-send-name {
				font-size: var(--im-font-size-small);
				color: var(--im-text-color-lighter);
				flex-shrink: 0;
			}

			.chat-content-text {
				flex: 1;
				white-space: nowrap;
				overflow: hidden;
				text-overflow: ellipsis;
				font-size: 12px;
				color: var(--im-text-color-lighter);
				min-width: 0;
			}

			.icon {
				color: var(--im-text-color-lighter);
				font-size: 14px;
				flex-shrink: 0;
				margin-left: 4px;
			}
		}
	}
}
</style>
