<template>
	<div class="chat-message-item" :class="active ? 'active' : ''">
		<div class="message-tip" v-if="message.type == $enums.MESSAGE_TYPE.TIP_TEXT">
			{{ message.content }}
		</div>
		<div class="message-tip" v-else-if="message.type == $enums.MESSAGE_TYPE.TIP_TIME">
			{{ $date.toTimeText(message.sendTime) }}
		</div>
		<div class="message-normal" v-else-if="isNormal" :class="{ 'message-mine': mine }">
			<div class="head-image">
				<head-image :name="showName" :size="38" :url="headImage" :id="message.sendId"></head-image>
			</div>
			<div class="content">
				<div v-show="mode == 1 && message.groupId && !message.selfSend" class="message-top">
					<span>{{ showName }}</span>
				</div>
				<div v-show="mode == 2" class="message-top">
					<span>{{ showName }}</span>
					<span>{{ $date.toTimeText(message.sendTime) }}</span>
				</div>
				<div class="message-bottom" @contextmenu.prevent="showRightMenu($event)">
					<div ref="chatMsgBox" class="message-content-wrapper">
						<span class="message-text" v-if="isTextMessage" v-html="htmlText"></span>
						<div class="message-image" v-else-if="message.type == $enums.MESSAGE_TYPE.IMAGE"
							@click="showFullImageBox()">
							<img :style="imageStyle" :src="contentData.thumbUrl" loading="lazy" />
						</div>
						<div class="message-file" v-else-if="message.type == $enums.MESSAGE_TYPE.FILE">
							<div class="chat-file-box" v-loading="sending">
								<div class="chat-file-info">
									<el-link class="chat-file-name" :underline="true" target="_blank" type="primary"
										:href="contentData.url" :download="contentData.name">{{ contentData.name
										}}</el-link>
									<div class="chat-file-size">{{ fileSize }}</div>
								</div>
								<div class="chat-file-icon">
									<span type="primary" class="el-icon-document"></span>
								</div>
							</div>
						</div>
						<div class="message-voice" v-else-if="message.type == $enums.MESSAGE_TYPE.AUDIO"
							@click="onPlayVoice()">
							<audio controls :src="JSON.parse(message.content).url"></audio>
						</div>
						<div title="发送中" v-if="sending" class="sending" v-loading="'true'"></div>
						<div title="发送失败" v-else-if="sendFail" @click="onSendFail" class="send-fail el-icon-warning">
						</div>
					</div>
					<div class="chat-action message-text" v-if="isAction">
						<span v-if="message.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE" title="重新呼叫"
							@click="$emit('call')" class="iconfont icon-chat-voice"></span>
						<span v-if="message.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO" title="重新呼叫"
							@click="$emit('call')" class="iconfont icon-chat-video"></span>
						<span>{{ message.content }}</span>
					</div>
					<div class="message-status" v-if="!isAction && message.selfSend && !isGroupMessage">
						<span class="chat-readed" v-if="isReaded">已读</span>
						<span class="chat-unread" v-else>未读</span>
					</div>
					<div class="chat-receipt" v-show="message.receipt && message.selfSend" @click="onShowReadedBox">
						<span v-if="message.receiptOk" class="icon iconfont icon-ok" title="全体已读"></span>
						<span v-else>{{ message.readedCount }}人已读</span>
					</div>
				</div>
			</div>
		</div>
		<right-menu ref="rightMenu" @select="onSelectMenu"></right-menu>
		<chat-group-readed ref="chatGroupReadedBox" :message="message" :group="group"></chat-group-readed>
	</div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
import RightMenu from '../common/RightMenu.vue';
import ChatGroupReaded from './ChatGroupReaded.vue';
export default {
	name: "messageItem",
	components: {
		HeadImage,
		RightMenu,
		ChatGroupReaded
	},
	props: {
		active: {
			type: Boolean,
			default: false
		},
		mode: {
			type: Number,
			default: 1
		},
		mine: {
			type: Boolean,
			required: true
		},
		headImage: {
			type: String,
			required: true
		},
		showName: {
			type: String,
			required: true
		},
		conversation: {
			type: Object,
			required: true
		},
		group: {
			type: Object,
		},
		message: {
			type: Object,
			required: true
		},
		menu: {
			type: Boolean,
			default: true
		}
	},
	data() {
		return {
			audioPlayState: 'STOP'
		}
	},
	methods: {
		onSendFail() {
			this.$emit("resend", this.message);
		},
		showFullImageBox() {
			let imageUrl = JSON.parse(this.message.content).originUrl;
			if (imageUrl) {
				this.$eventBus.$emit("openFullImage", imageUrl);
			}
		},
		onPlayVoice() {
			if (!this.audio) {
				this.audio = new Audio();
			}
			this.audio.src = JSON.parse(this.message.content).url;
			this.audio.play();
			this.onPlayVoice = 'RUNNING';
		},
		showRightMenu(e) {
			this.$refs.rightMenu.open(e, this.menuItems);
		},
		onSelectMenu(item) {
			this.$emit(item.key.toLowerCase(), this.message);
		},
		onShowReadedBox() {
			let rect = this.$refs.chatMsgBox.getBoundingClientRect();
			this.$refs.chatGroupReadedBox.open(rect);
		}
	},
	computed: {
		sending() {
			return this.message.status == this.$enums.MESSAGE_STATUS.SENDING;
		},
		sendFail() {
			return this.message.status == this.$enums.MESSAGE_STATUS.FAILED;
		},
		contentData() {
			return JSON.parse(this.message.content)
		},
		fileSize() {
			let size = this.contentData.size;
			if (size > 1024 * 1024) {
				return Math.round(size / 1024 / 1024) + "M";
			}
			if (size > 1024) {
				return Math.round(size / 1024) + "KB";
			}
			return size + "B";
		},
		menuItems() {
			let items = [];
			items.push({
				key: 'DELETE',
				name: '删除',
				danger: true
			});
			if (this.message.selfSend && this.message.id > 0) {
				items.push({
					key: 'RECALL',
					name: '撤回'
				});
			}
			return items;
		},
		isTextMessage() {
			return this.message.type == this.$enums.MESSAGE_TYPE.TEXT
		},
		isAction() {
			return this.$msgType.isAction(this.message.type);
		},
		isNormal() {
			const type = this.message.type;
			return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
		},
		isReaded() {
			return this.message.status == this.$enums.MESSAGE_STATUS.READED || this.conversation.maxReadedId >= this.message.id
		},
		htmlText() {
			let color = this.message.selfSend ? 'white' : '';
			let text = this.$str.html2Escape(this.message.content)
			text = this.$url.replaceURLWithHTMLLinks(text, color)
			return this.$emo.transform(text, 'emoji-normal')
		},
		isGroupMessage() {
			return !!this.message.groupId;
		},
		imageStyle() {
			// 计算图片的显示宽高，要求：任意边不能高于360px,不能低于60px,不能拉伸图片比例
			let maxSize = this.configStore.fullScreen ? 360 : 240;
			let minSize = 60;
			let width = this.contentData.width;
			let height = this.contentData.height;
			if (width && height) {
				let ratio = Math.min(width, height) / Math.max(width, height);
				let w = Math.max(Math.min(width > height ? maxSize : ratio * maxSize, width), minSize);
				let h = Math.max(Math.min(width > height ? ratio * maxSize : maxSize, height), minSize);
				return `width: ${w}px;height:${h}px;object-fit: cover;`
			} else {
				// 兼容历史版本，历史数据没有记录宽高
				return `max-width: ${maxSize}px;min-width:60px;max-height: ${maxSize}px;min-height:60px;`
			}
		}
	}
}
</script>

<style lang="scss">
.chat-message-item {
	padding: 2px 14px;
	border-radius: 8px;
	transition: background 0.3s ease;

	&.active {
		background: rgba(255,255,255,.04);
	}

	.message-tip {
		line-height: 44px;
		font-size: var(--im-font-size-small);
		color: var(--im-text-color-lighter);
		user-select: none;
	}

	.message-normal {
		position: relative;
		font-size: 0;
		padding-left: 48px;
		min-height: 50px;
		margin-top: 8px;

		.head-image {
			position: absolute;
			width: 38px;
			height: 38px;
			top: 2px;
			left: 0;
		}

		.content {
			text-align: left;

			.message-top {
				display: flex;
				flex-wrap: nowrap;
				color: var(--im-text-color-light);
				font-size: var(--im-font-size);
				line-height: 20px;
				margin-bottom: 3px;

				span {
					margin-right: 12px;

					&:first-child {
						font-weight: 500;
						color: var(--im-text-color);
					}
				}
			}

			.message-bottom {
				display: inline-block;
				padding-right: 280px;
				padding-left: 4px;

				.message-content-wrapper {
					position: relative;
					display: flex;
					align-items: flex-end;

					.sending {
						width: 22px;
						height: 22px;
						margin-right: 4px;

						.circular {
							width: 22px;
							height: 22px;
						}
					}

					.send-fail {
						color: #FF3B30;
						font-size: 22px;
						cursor: pointer;
						margin: 0 4px;
					}
				}

				.message-text {
					flex: 1;
					display: inline-block;
					position: relative;
					line-height: 24px;
					padding: 8px 14px;
					background-color: #2c2c2e;
					border-radius: 4px 14px 14px 14px;
					font-size: var(--im-font-size);
					text-align: left;
					white-space: pre-wrap;
					word-break: break-word;
					color: var(--im-text-color);
					box-shadow: 0 1px 2px rgba(0,0,0,0.04);
				}

				.message-image {
					border-radius: 12px;
					border: 1px solid rgba(255, 255, 255, 0.08);
					overflow: hidden;
					cursor: pointer;
					background: var(--im-background);
					box-shadow: 0 1px 4px rgba(0,0,0,0.06);
				}

				.message-file {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;
					cursor: pointer;
					margin-bottom: 2px;

					.chat-file-box {
						display: flex;
						flex-wrap: nowrap;
						align-items: center;
						min-height: 56px;
						box-shadow: 0 1px 4px rgba(0,0,0,0.06);
						border-radius: 10px;
						padding: 10px 14px;
						background: #FFFFFF;
						border: 1px solid var(--im-color-primary-light-6);

						.chat-file-info {
							flex: 1;
							height: 100%;
							text-align: left;
							font-size: 14px;
							margin-right: 10px;

							.chat-file-name {
								display: inline-block;
								min-width: 140px;
								max-width: 200px;
								font-size: 13px;
								margin-bottom: 4px;
								white-space: pre-wrap;
								word-break: break-all;
								font-weight: 500;
							}

							.chat-file-size {
								font-size: 11px;
								color: var(--im-text-color-lighter);
							}
						}

						.chat-file-icon {
							font-size: 40px;
							color: #FF3B30;
						}
					}
				}

				.message-voice {
					cursor: pointer;

					audio {
						height: 42px;
						padding: 4px 0;
					}
				}

				.chat-action {
					display: flex;
					align-items: center;
					color: var(--im-text-color);

					.iconfont {
						cursor: pointer;
						font-size: 20px;
						padding-right: 6px;
						color: var(--im-color-primary-light-2);

						&:hover {
							color: var(--im-color-primary);
						}
					}
				}

				.message-status {
					margin-top: 2px;
					display: block;

					.chat-readed {
						font-size: 11px;
						color: var(--im-text-color-lighter);
					}

					.chat-unread {
						font-size: 11px;
						color: #FF9500;
					}
				}

				.chat-receipt {
					font-size: 11px;
					cursor: pointer;
					color: var(--im-text-color-lighter);

					.icon-ok {
						font-size: 18px;
						color: var(--im-color-success);
					}
				}

				.chat-at-user {
					padding: 2px 5px;
					border-radius: 3px;
					cursor: pointer;
				}
			}
		}

		&.message-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 48px;

			.head-image {
				left: auto;
				right: 0;
			}

			.content {
				text-align: right;

				.message-top {
					flex-direction: row-reverse;

					span {
						margin-left: 12px;
						margin-right: 0;
					}
				}

				.message-bottom {
					padding-left: 160px;
					padding-right: 4px;

					.message-content-wrapper {
						flex-direction: row-reverse;
					}

					.message-text {
						background: #0a84ff;
						color: #FFFFFF;
						border-radius: 14px 4px 14px 14px;
						box-shadow: none;
					}

					.chat-action {
						flex-direction: row-reverse;

						.iconfont {
							color: var(--im-color-primary-light-3);
							transform: rotateY(180deg);

							&:hover {
								color: #FFFFFF;
							}
						}
					}
				}
			}
		}
	}
}
</style>