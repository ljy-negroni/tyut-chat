<template>
	<el-container class="group-page">
		<resizable-aside :default-width="260" :min-width="200" :max-width="500" storage-key="group-aside-width">
			<div class="aside-hd">
				<el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
					<i class="el-icon-search el-input__icon" slot="prefix"> </i>
				</el-input>
				<el-button plain class="add-btn" icon="el-icon-plus" title="创建群聊" @click="onCreateGroup()"></el-button>
			</div>
			<el-scrollbar class="group-items">
				<div v-for="(groups, i) in groupValues" :key="i">
					<div class="letter">{{ groupKeys[i] }}</div>
					<div v-for="group in groups" :key="group.id">
						<group-item :group="group" :active="group.id == activeGroup.id"
							@click.native="onActiveItem(group)">
						</group-item>
					</div>
					<div v-if="i < groupValues.length - 1" class="divider"></div>
				</div>
			</el-scrollbar>
		</resizable-aside>

		<el-container class="main">
			<div class="empty-hint" v-if="!activeGroup.id">
				<i class="iconfont icon-group" style="font-size:48px;opacity:.15;margin-bottom:14px"></i>
				<p>选择一个群聊查看详情</p>
			</div>

			<transition name="gp" mode="out-in">
				<div v-if="activeGroup.id" :key="activeGroup.id" class="detail">
					<el-scrollbar class="detail-body">
						<div class="profile">
							<file-upload v-show="isOwner" class="pf-avatar" :action="imageAction" :showLoading="true"
								:maxSize="maxSize" @success="onUploadSuccess"
								:fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp']">
								<img v-if="activeGroup.headImage" :src="activeGroup.headImage" class="pf-avatar-img">
								<div v-else class="pf-avatar-empty"><i class="el-icon-camera"></i></div>
							</file-upload>
							<head-image v-show="!isOwner" :size="72" :url="activeGroup.headImage"
								:name="activeGroup.showGroupName" radius="18px" @click.native="showFullImage()">
							</head-image>
							<div class="pf-name">{{ activeGroup.showGroupName }}</div>
							<div class="pf-desc">{{ showMembers.length }} 名成员</div>
							<div class="pf-btns">
								<el-button size="small" type="primary" round @click="onSendMessage(activeGroup)">发消息</el-button>
								<el-button size="small" round @click="onInviteMember()">邀请</el-button>
								<el-dropdown trigger="click" @command="onMoreCmd">
									<el-button size="small" icon="el-icon-more" round></el-button>
									<el-dropdown-menu slot="dropdown">
										<el-dropdown-item v-if="isOwner" command="remove">移除成员</el-dropdown-item>
										<el-dropdown-item v-if="isOwner" command="dissolve" class="danger-item">解散群聊</el-dropdown-item>
										<el-dropdown-item v-if="!isOwner" command="quit" class="danger-item">退出群聊</el-dropdown-item>
									</el-dropdown-menu>
								</el-dropdown>
							</div>
						</div>

						<div class="block">
							<div class="block-title">群聊设置</div>
							<el-form :model="activeGroup" :rules="rules" size="small" ref="groupForm" label-width="85px" class="setting-form">
								<el-form-item label="群聊名称" prop="name">
									<el-input v-model="activeGroup.name" :disabled="!isOwner" maxlength="20"></el-input>
								</el-form-item>
								<el-form-item label="群主">
									<el-input :value="ownerName" disabled></el-input>
								</el-form-item>
								<el-form-item label="备注">
									<el-input v-model="activeGroup.remarkGroupName" :placeholder="activeGroup.name" maxlength="20"></el-input>
								</el-form-item>
								<el-form-item label="我的群昵称">
									<el-input v-model="activeGroup.remarkNickName" maxlength="20" :placeholder="userStore.userInfo.nickName"></el-input>
								</el-form-item>
								<el-form-item label="群公告">
									<el-input v-model="activeGroup.notice" :disabled="!isOwner" type="textarea" :rows="2" maxlength="1024" placeholder="群主未设置"></el-input>
								</el-form-item>
								<el-form-item>
									<el-button type="primary" size="small" @click="onSaveGroup()">保存</el-button>
								</el-form-item>
							</el-form>
						</div>

						<div class="block">
							<div class="block-title">群成员 · {{ showMembers.length }}</div>
							<div class="member-grid">
								<group-member v-for="(member, idx) in showMembers" :key="member.id"
									v-show="idx < showMaxIdx" :member="member"></group-member>
							</div>
							<div class="load-more" v-if="showMembers.length > showMaxIdx" @click="showMaxIdx += 50">查看更多</div>
						</div>
					</el-scrollbar>
				</div>
			</transition>
		</el-container>

		<add-group-member ref="addGroupMember" :groupId="activeGroup.id" :members="groupMembers" @reload="reloadMembers"></add-group-member>
		<group-member-selector ref="removeSelector" title="选择成员进行移除" :group="activeGroup" @complete="onRemoveComplete"></group-member-selector>
	</el-container>
</template>


<script>
import GroupItem from '../components/group/GroupItem';
import FileUpload from '../components/common/FileUpload';
import GroupMember from '../components/group/GroupMember.vue';
import AddGroupMember from '../components/group/AddGroupMember.vue';
import GroupMemberSelector from '../components/group/GroupMemberSelector.vue';
import HeadImage from '../components/common/HeadImage.vue';
import ResizableAside from "../components/common/ResizableAside.vue";
import { pinyin } from 'pinyin-pro';

export default {
	name: "group",
	components: { GroupItem, GroupMember, FileUpload, AddGroupMember, GroupMemberSelector, HeadImage, ResizableAside },
	data() { return { searchText: "", maxSize: 5 * 1024 * 1024, activeGroup: {}, showMaxIdx: 150,
		rules: { name: [{ required: true, message: '请输入群聊名称', trigger: 'blur' }] } } },
	methods: {
		onCreateGroup() {
			this.$prompt('请输入群聊名称', '创建群聊', { confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /\S/, inputErrorMessage: '请输入群聊名称' })
				.then(o => { this.$http({ url: `/group/create?groupName=${o.value}`, method: 'post', data: { name: o.value } })
					.then(group => { this.groupStore.addGroup(group); this.onActiveItem(group); this.$message.success('创建成功') }) })
		},
		onActiveItem(group) { this.showMaxIdx = 150; this.activeGroup = JSON.parse(JSON.stringify(group)); this.reloadMembers() },
		onInviteMember() { this.$refs.addGroupMember.open() },
		onMoreCmd(cmd) { if(cmd==="remove")this.onRemoveMember(); if(cmd==="dissolve")this.onDissolve(this.activeGroup); if(cmd==="quit")this.onQuit(this.activeGroup) },
		onRemoveMember() { this.$refs.removeSelector.open(50,[],[],[this.activeGroup.ownerId]) },
		onRemoveComplete(members) {
			this.$http({ url:"/group/members/remove", method:'delete', data:{ groupId:this.activeGroup.id, userIds: members.map(m=>m.userId) } })
				.then(()=>{ this.reloadMembers(); this.$message.success(`已移除 ${members.length} 位成员`) })
		},
		onUploadSuccess(data) { this.activeGroup.headImage = data.originUrl; this.activeGroup.headImageThumb = data.thumbUrl },
		onSaveGroup() { this.$refs.groupForm.validate(valid => { if(!valid)return; this.$http({ url:"/group/modify", method:"put", data:this.activeGroup }).then(g=>{ this.groupStore.updateGroup(g); this.$message.success("修改成功") }) }) },
		onDissolve(group) { this.$confirm(`确认要解散'${group.name}'吗?`, '确认解散?', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' }).then(()=>{ this.$http({ url:`/group/delete/${group.id}`, method:'delete' }).then(()=>{ this.$message.success(`群聊'${group.name}'已解散`); this.groupStore.removeGroup(group.id); this.reset() }) }) },
		onQuit(group) { this.$confirm(`确认退出'${group.showGroupName}',并清空聊天记录吗？`, '确认退出?', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' }).then(async()=>{ await this.$http({ url:`/group/quit/${group.id}`, method:'delete' }); this.groupStore.removeGroup(group.id); await this.$http({ url:`/message/group/deleteChat`, method:'delete', data:{ chatId:group.id } }); this.chatStore.remove(this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP,group.id)); this.$message.success(`已退出'${group.name}'`); this.reset() }) },
		async onSendMessage(group) { const ck=this.$db.buildConversationKey(this.$enums.CONVERSATION_TYPE.GROUP,group.id); await this.chatStore.openChat({key:ck,type:this.$enums.CONVERSATION_TYPE.GROUP,targetId:group.id,showName:group.showGroupName,headImage:group.headImageThumb,isDnd:group.isDnd}); await this.chatStore.moveTop(ck); this.chatStore.setActive(ck); this.$router.push("/home/chat") },
		showFullImage() { if(this.activeGroup.headImage) this.$eventBus.$emit("openFullImage",this.activeGroup.headImage) },
		reloadMembers() { this.groupStore.refreshMember(this.activeGroup.id) },
		reset() { this.activeGroup = {} },
		firstLetter(s){ return pinyin(s,{toneType:'none',type:'normal'})[0] },
		isEnglish(c){ return /^[A-Za-z]+$/.test(c) }
	},
	computed: {
		ownerName() { const m=this.groupMembers.find(m=>m.userId==this.activeGroup.ownerId); return m&&m.showNickName },
		isOwner() { return this.activeGroup.ownerId == this.userStore.userInfo.id },
		imageAction() { return '/image/upload?thumbSize=20' },
		groupMap() {
			let map=new Map(); this.groupStore.groups.forEach(g=>{ if(g.quit||(this.searchText&&!g.showGroupName.includes(this.searchText)))return; let l=this.firstLetter(g.showGroupName).toUpperCase(); if(!this.isEnglish(l))l="#"; if(map.has(l))map.get(l).push(g); else map.set(l,[g]) });
			let a=Array.from(map); a.sort((a,b)=>a[0]=='#'||b[0]=='#'?b[0].localeCompare(a[0]):a[0].localeCompare(b[0]));
			return new Map(a.map(i=>[i[0],i[1]]))
		},
		groupKeys(){return Array.from(this.groupMap.keys())},
		groupValues(){return Array.from(this.groupMap.values())},
		showMembers(){return this.groupMembers.filter(m=>!m.quit)},
		groupMembers(){const g=this.groupStore.findGroup(this.activeGroup.id);return g?g.members:[]}
	}
}
</script>

<style lang="scss" scoped>
.group-page {
	.aside-hd { height:50px; display:flex; align-items:center; padding:0 8px; .add-btn { padding:5px!important; margin:5px; font-size:16px; border-radius:50% } }
	.group-items { flex:1; .letter { text-align:left; font-size:12px; padding:8px 16px 4px; color:rgba(255,255,255,.28) } }
	.main { display:flex; flex-direction:column; background:#1c1c1e }
	.empty-hint { flex:1; display:flex; flex-direction:column; align-items:center; justify-content:center; color:var(--im-text-color-lighter); font-size:14px }
	.detail { flex:1; display:flex; flex-direction:column; overflow:hidden }
	.detail-body { flex:1 }

	.profile {
		padding:32px 32px 24px; background:#2c2c2e;
		display:flex; flex-direction:column; align-items:center;
		.pf-avatar { width:72px; height:72px; cursor:pointer; margin-bottom:14px;
			::v-deep .el-upload { width:72px; height:72px; border-radius:18px; border:2px dashed var(--im-color-primary-light-4); display:flex; align-items:center; justify-content:center; transition:all .2s; &:hover{ border-color:var(--im-color-primary); background:var(--im-color-primary-light-9) } }
			.pf-avatar-img { width:72px; height:72px; border-radius:18px; object-fit:cover; display:block }
			.pf-avatar-empty { color:var(--im-text-color-lighter); display:flex; flex-direction:column; align-items:center; font-size:12px; gap:4px; .el-icon-camera{ font-size:24px } }
		}
		.pf-name { font-size:18px; font-weight:600; color:var(--im-text-color); margin-bottom:4px }
		.pf-desc { font-size:13px; color:var(--im-text-color-lighter); margin-bottom:18px }
		.pf-btns { display:flex; gap:8px; justify-content:center }
	}

	.block {
		background:#2c2c2e; border: 1px solid rgba(255,255,255,.06);
		border-radius:14px; padding:20px 28px; margin:16px 20px; margin-bottom:0;
		box-shadow:none;
		.block-title { font-size:14px; font-weight:600; color:var(--im-text-color); padding-bottom:12px; margin-bottom:12px; border-bottom:1px solid rgba(255,255,255,.06) }
	}
	.block+.block { margin-top:14px; margin-bottom:22px }

	.setting-form { max-width:100%; ::v-deep .el-form-item{margin-bottom:12px} ::v-deep .el-form-item__label{font-weight:500;color:var(--im-text-color-regular);white-space:nowrap} ::v-deep .el-input__inner,::v-deep .el-textarea__inner{border-radius:8px} }

	.member-grid { display:flex; flex-wrap:wrap; gap:10px }
	.load-more { text-align:center; padding:14px; color:var(--im-color-primary); font-size:13px; cursor:pointer; &:hover{text-decoration:underline} }
	.danger-item { color:var(--im-color-danger)!important }
}

// 群切换过渡
.gp-enter-active { transition: all .3s cubic-bezier(.22,.61,.36,1) }
.gp-leave-active { transition: all .2s cubic-bezier(.55,0,1,1) }
.gp-enter { opacity:0; transform: translateX(16px) }
.gp-leave-to { opacity:0; transform: translateX(-8px) }
</style>
