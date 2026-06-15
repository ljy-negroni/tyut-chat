const path = require('path')
const fs = require('fs')

module.exports = {
	devServer: {
		proxy: {
			'/api': {
				target: 'http://127.0.0.1:8088',
				changeOrigin: true,
				ws: false,
				pathRewrite: {
					'^/api': ''
				}
			}
		}
	}

}