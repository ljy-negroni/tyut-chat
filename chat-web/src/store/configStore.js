import { defineStore } from 'pinia';
import http from '../api/httpRequest.js'

export default defineStore('configStore', {
	state: () => {
		return {
			fullScreen: true, // 当前是否全屏
			webrtc: {}
		}
	},
	actions: {
		setConfig(config) {
			this.webrtc = config.webrtc;
		},
		setFullScreen(fullScreen) {
			this.fullScreen = fullScreen;
		},
		async loadConfig() {
			const config = await http({
				url: '/system/config',
				method: 'get'
			});
			console.log("系统配置", config)
			this.setConfig(config);
		}
	}
});