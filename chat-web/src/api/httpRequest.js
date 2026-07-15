import axios from 'axios'

import { Message } from 'element-ui'
import { getRefreshToken, saveTokens } from './auth.js'

const http = axios.create({
	baseURL: process.env.VUE_APP_BASE_API,
	timeout: 1000 * 30,
	withCredentials: true
})

/** token 刷新状态 */
let isRefreshing = false
/** 等待刷新完成的请求队列 */
let refreshQueue = []

/** 执行队列中的请求 */
function resolveQueue(newToken) {
	refreshQueue.forEach(({ resolve }) => resolve(newToken))
	refreshQueue = []
}

/** 拒绝队列中的请求 */
function rejectQueue(err) {
	refreshQueue.forEach(({ reject }) => reject(err))
	refreshQueue = []
}

/**
 * 请求拦截
 */
http.interceptors.request.use(config => {
	// refreshToken 请求不附加 accessToken，避免循环
	if (config.url === '/refreshToken') {
		return config
	}
	let accessToken = sessionStorage.getItem("accessToken");
	if (accessToken) {
		config.headers.accessToken = encodeURIComponent(accessToken);
	}
	return config
}, error => {
	return Promise.reject(error)
})

/**
 * 响应拦截
 */
http.interceptors.response.use(async response => {
	if (response.data.code == 200) {
		return response.data.data;
	} else if (response.data.code == 400) {
		location.href = "/";
	} else if (response.data.code == 401) {
		// refreshToken 请求本身 401 → 直接跳登录，不再尝试刷新
		if (response.config.url === '/refreshToken') {
			location.href = "/";
			return Promise.reject(response.data);
		}
		console.log("token失效，尝试重新获取")
		let refreshToken = getRefreshToken();
		if (!refreshToken) {
			location.href = "/";
			return Promise.reject(response.data);
		}

		// 如果正在刷新，把当前请求加入队列等待
		if (isRefreshing) {
			return new Promise((resolve, reject) => {
				refreshQueue.push({ resolve, reject })
			}).then(newToken => {
				response.config.headers.accessToken = encodeURIComponent(newToken);
				return http(response.config)
			})
		}

		isRefreshing = true
		try {
			const data = await axios({
				method: 'put',
				url: process.env.VUE_APP_BASE_API + '/refreshToken',
				headers: { refreshToken: refreshToken }
			}).then(res => {
				if (res.data.code == 200) {
					return res.data.data
				}
				throw new Error('refresh failed')
			})
			saveTokens(data);
			isRefreshing = false
			resolveQueue(data.accessToken)
			response.config.headers.accessToken = encodeURIComponent(data.accessToken);
			return http(response.config)
		} catch (e) {
			isRefreshing = false
			rejectQueue(e)
			location.href = "/";
			return Promise.reject(response.data);
		}
	} else {
		Message({
			message: response.data.message,
			type: 'error',
			duration: 1500,
			customClass: 'element-error-message-zindex'
		})
		return Promise.reject(response.data)
	}
}, error => {
	switch (error.response.status) {
		case 400:
			Message({
				message: error.response.data,
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 401:
			location.href = "/";
			break
		case 405:
			Message({
				message: 'http请求方式有误',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 404:
		case 500:
			Message({
				message: '服务器出了点小差，请稍后再试',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
		case 501:
			Message({
				message: '服务器不支持当前请求所需要的某个功能',
				type: 'error',
				duration: 1500,
				customClass: 'element-error-message-zindex'
			})
			break
	}

	return Promise.reject(error)
})


export default http
