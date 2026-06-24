<template>
	<el-container class="friend-page">
		<resizable-aside :default-width="260" :min-width="200" :max-width="500" storage-key="friend-aside-width">
			<div class="header">
				<el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="添加好友" @click="onShowAddFriend()"></el-button>
				<add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
			</div>
			<el-scrollbar class="friend-items">
				<div v-for="(friends, i) in friendValues" :key="i">
					<div class="letter">{{ friendKeys[i] }}</div>
					<div v-for="friend in friends" :key="friend.id">
						<friend-item :friend="friend" :active="friend.id === activeFriend.id"
							@chat="onSendMessage(friend)" @delete="onDelFriend(friend)"
							@click.native="onActiveItem(friend)">
						</friend-item>
					</div>
					<div v-if="i < friendValues.length - 1" class="divider"></div>
				</div>
			</el-scrollbar>
		</resizable-aside>
		<el-container class="container">
			<transition name="fd" mode="out-in">
				<div class="detail" v-if="userInfo.id" :key="detailKey">
					<div class="det-avatar">
						<head-image :size="100" :name="userInfo.nickName" :url="userInfo.headImage" radius="24px" @click.native="showFullImage()"></head-image>
					</div>
					<h2 class="det-name">{{ userInfo.nickName }}</h2>
					<p class="det-username">@{{ userInfo.userName }}</p>
					<p class="det-sig" v-if="userInfo.signature">"{{ userInfo.signature }}"</p>
					<div class="det-meta">
						<div class="meta-item">
							<i class="el-icon-male" v-if="userInfo.sex==0"></i>
							<i class="el-icon-female" v-else></i>
							<span>{{ userInfo.sex == 0 ? "男" : "女" }}</span>
						</div>
					</div>
					<div class="det-actions">
						<el-button v-show="isFriend" size="large" icon="el-icon-chat-dot-round" type="primary" round @click="onSendMessage(activeFriend)">发消息</el-button>
						<el-button v-show="!isFriend" size="large" icon="el-icon-plus" type="primary" round @click="onAddFriend(userInfo)">加为好友</el-button>
						<el-button v-show="isFriend" size="large" icon="el-icon-delete" type="danger" round plain @click="onDelFriend(userInfo)">删除好友</el-button>
					</div>
				</div>
				<div class="empty-detail" v-else key="e">
					<i class="iconfont icon-friend" style="font-size:48px;opacity:.12;margin-bottom:16px"></i>
					<p>选择一个好友查看详情</p>
				</div>
			</transition>
		</el-container>
	</el-container>
</template>

<script>
import FriendItem from "../components/friend/FriendItem.vue"
import AddFriend from "../components/friend/AddFriend.vue"
import HeadImage from "../components/common/HeadImage.vue"
import ResizableAside from "../components/common/ResizableAside.vue"
import { pinyin } from 'pinyin-pro'

export default {
	name: "friend",
	components: { FriendItem, AddFriend, HeadImage, ResizableAside },
	data() {
		return {
			searchText: "",
			showAddFriend: false,
			detailKey: 0,
			userInfo: {},
			activeFriend: {}
		}
	},
	methods: {
		onShowAddFriend() { this.showAddFriend = true },
		onCloseAddFriend() { this.showAddFriend = false },
		onActiveItem(friend) {
			this.detailKey++
			this.activeFriend = friend
			this.userInfo = {}
			this.loadUserInfo(friend.id)
		},
		onDelFriend(friend) {
			this.$confirm(`确认删除'${friend.nickName}',并清空聊天记录吗?`, '确认解除?', {
				confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
			}).then(async () => {
				await this.$http({ url: `/friend/delete/${friend.id}`, method: 'delete' })
				this.friendStore.removeFriend(friend.id)
				const data = { chatId: friend.id }
				await this.$http({ url: `/message/private/deleteChat`, method: 'delete', data })
				const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friend.id)
				await this.chatStore.remove(convKey)
				this.$message.success("删除好友成功")
			})
		},
		onAddFriend(user) {
			this.$http({ url: "/friend/add", method: "post", params: { friendId: user.id } }).then(() => {
				this.$message.success("添加成功，对方已成为您的好友")
				this.friendStore.addFriend({ id: user.id, nickName: user.nickName, headImage: user.headImageThumb, online: user.online, deleted: false, version: 0 })
			})
		},
		async onSendMessage(friend) {
			const convKey = this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.PRIVATE, friend.id)
			const chatInfo = { key: convKey, type: this.$enums.CONVERSATION_TYPE.PRIVATE, targetId: friend.id, showName: friend.nickName, headImage: friend.headImage, isDnd: friend.isDnd }
			await this.chatStore.openChat(chatInfo)
			await this.chatStore.moveTop(convKey)
			this.chatStore.setActive(convKey)
			this.$router.push("/home/chat")
		},
		showFullImage() { if (this.userInfo.headImage) this.$eventBus.$emit("openFullImage", this.userInfo.headImage) },
		updateFriendInfo() {
			if (this.isFriend) {
				const friend = JSON.parse(JSON.stringify(this.activeFriend))
				friend.headImage = this.userInfo.headImageThumb
				friend.nickName = this.userInfo.nickName
				this.chatStore.updateFromFriend(friend)
				this.friendStore.updateFriend(friend)
			}
		},
		loadUserInfo(id) {
			this.$http({ url: `/user/find/${id}`, method: 'GET' }).then(userInfo => {
				this.userInfo = userInfo
				this.updateFriendInfo()
			})
		},
		firstLetter(strText) {
			const opt = { toneType: 'none', type: 'normal' }
			return pinyin(strText, opt)[0]
		},
		isEnglish(character) { return /^[A-Za-z]+$/.test(character) }
	},
	computed: {
		isFriend() { return this.friendStore.isFriend(this.userInfo.id) },
		friendMap() {
			let map = new Map()
			this.friendStore.friends.forEach(f => {
				if (f.deleted || (this.searchText && !f.nickName.includes(this.searchText))) return
				let letter = this.firstLetter(f.nickName).toUpperCase()
				if (!this.isEnglish(letter)) letter = "#"
				if (f.online) letter = '在线'
				if (map.has(letter)) map.get(letter).push(f)
				else map.set(letter, [f])
			})
			let arrayObj = Array.from(map)
			arrayObj.sort((a, b) => a[0] == '#' || b[0] == '#' ? b[0].localeCompare(a[0]) : a[0].localeCompare(b[0]))
			return new Map(arrayObj.map(i => [i[0], i[1]]))
		},
		friendKeys() { return Array.from(this.friendMap.keys()) },
		friendValues() { return Array.from(this.friendMap.values()) }
	}
}
</script>

<style scoped lang="scss">
.friend-page { background: transparent;
  .header { height: 50px; display: flex; align-items: center; padding: 0 10px;
    .add-btn { padding: 5px !important; margin: 5px; font-size: 16px; border-radius: 50%;
      background: transparent; border-color: rgba(255,255,255,0.12); color: rgba(255,255,255,0.5);
      &:hover { border-color: rgba(255,255,255,0.25); color: rgba(255,255,255,0.7); }
    }
  }
  ::v-deep .header .el-input__inner {
    background: rgba(255,255,255,0.06);
    border: 1px solid rgba(255,255,255,0.08);
    border-radius: 8px;
    color: rgba(255,255,255,0.8);
    &::placeholder { color: rgba(255,255,255,0.25); }
    &:focus { border-color: rgba(255,255,255,0.18); }
  }
  .friend-items { flex: 1;
    .letter { text-align: left; font-size: 12px; padding: 6px 15px 4px; color: rgba(255,255,255,0.25); }
  }
}

.container { display: flex; flex-direction: column; align-items: center; justify-content: center;
  background:#2c2c2e;  -webkit-backdrop-filter: blur(16px) }

.empty-detail { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: var(--im-text-color-lighter); font-size: 14px }

.detail { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 32px }
.fd-enter-active { transition: opacity .25s ease, transform .28s cubic-bezier(.22,.61,.36,1) }
.fd-leave-active { transition: opacity .15s ease, transform .18s cubic-bezier(.55,0,1,1) }
.fd-enter { opacity: 0; transform: translateY(12px) }
.fd-leave-to { opacity: 0; transform: translateY(-4px) }

.det-avatar { margin-bottom: 20px; filter: none; }
.det-name { margin: 0; font-size: 26px; font-weight: 700; letter-spacing: 1px; color: var(--im-text-color) }
.det-username { margin: 6px 0 10px; font-size: 14px; color: var(--im-text-color-lighter) }
.det-sig { margin: 0 0 14px; font-size: 14px; color: var(--im-text-color-light); font-style: italic; max-width: 320px; text-align: center }
.det-meta { margin-bottom: 24px; display: flex; gap: 16px;
  .meta-item { display: flex; align-items: center; gap: 4px; font-size: 14px; color: var(--im-text-color-light);
    .el-icon-male { color: #4A90D9; font-size: 18px }
    .el-icon-female { color: #E85D75; font-size: 18px }
  }
}
.det-actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center;
  .el-button { transition: background-color 0.2s ease; }
}
</style>
