import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'
import { TERMINAL_TYPE } from "../api/enums.js"
import { getDB } from '../db/index.js';
import { isToday } from '../api/date.js';

export default defineStore('friendStore', {
	state: () => {
		return {
			friends: [], // 数组用于展示，防止更新时不刷新
			friendMap: new Map() // map用于查询，提高查询效率
		}
	},
	actions: {
		init(friends) {
			this.friends = friends;
			this.friendMap.clear();
			friends.forEach(f => this.friendMap.set(f.id, f))
		},
		append(friends) {
			friends.forEach(f => {
				if (this.friendMap.has(f.id)) {
					const friend = this.friendMap.get(f.id);
					Object.assign(friend, f);
				} else {
					this.friends.push(f);
					this.friendMap.set(f.id, f);
				}
			})
		},
		resetOnline(friends) {
			friends.forEach(f => {
				f.online = false;
				f.onlineWeb = false;
				f.onlineApp = false;
			})
		},
		async updateFriend(friend) {
			const f = this.findFriend(friend.id);
			friend.online = f.online;
			friend.onlineWeb = f.onlineWeb;
			friend.onlineApp = f.onlineApp;
			Object.assign(f, friend);
			await getDB().saveFriend(friend);
		},
		async removeFriend(id) {
			const friend = this.findFriend(id);
			friend.deleted = true;
			await getDB().saveFriend(friend);
		},
		async addFriend(friend) {
			if (this.friendMap.has(friend.id)) {
				this.updateFriend(friend)
			} else {
				this.friends.unshift(friend);
				this.friendMap.set(friend.id, friend);
				await getDB().saveFriend(friend);
			}
		},
		updateOnlineStatus(onlineData) {
			const friend = this.findFriend(onlineData.userId);
			if (onlineData.terminal == TERMINAL_TYPE.WEB) {
				friend.onlineWeb = onlineData.online;
			} else if (onlineData.terminal == TERMINAL_TYPE.APP) {
				friend.onlineApp = onlineData.online;
			}
			friend.online = friend.onlineWeb || friend.onlineApp;
		},
		async setDnd(id, isDnd) {
			const friend = this.findFriend(id);
			friend.isDnd = isDnd;
			await getDB().saveFriend(friend);
		},
		clear() {
			this.friends = [];
			this.friendMap.clear();
		},
		async pullFriends() {
			const version = Math.max(...this.friends.map(f => f.version || 0));
			const friends = await http({ url: '/friend/list?version=' + version });
			this.resetOnline(friends);
			this.append(friends);
			await getDB().saveFriends(friends);
		},
		async refreshOnline() {
			const onlines = await http({ url: '/friend/online' });
			this.resetOnline(this.friends);
			onlines.forEach(online => this.updateOnlineStatus(online));
		},
		async loadFriend() {
			const lastSyncTime = await getDB().findLastSyncFriendsTime();
			if (!lastSyncTime || !isToday(new Date(lastSyncTime))) {
				// 每日首次登录进行全量同步
				const friends = await http({ url: '/friend/list' });
				this.resetOnline(friends);
				this.init(friends);
				await getDB().syncAllFriends(friends);
				console.log("全量同步好友信息")
			} else {
				// 从本地数据库加载
				const friends = await getDB().findAllFriends();
				this.resetOnline(friends);
				this.init(friends);
				// 从服务器增量拉取
				await this.pullFriends();
				console.log("增量同步好友信息")
			}
			this.refreshOnline();
		}
	},
	getters: {
		isFriend: (state) => (userId) => {
			let f = state.findFriend(userId)
			return f && !f.deleted
		},
		findFriend: (state) => (userId) => {
			return state.friendMap.get(userId);
		}
	}
});