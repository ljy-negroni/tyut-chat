<template>
	<div v-if="show" class="right-menu-mask" @click.stop="close()" @contextmenu.prevent="close()">
		<div class="right-menu" :style="{ 'left': pos.x + 'px', 'top': pos.y + 'px' }">
			<div class="menu-container">
				<div v-for="(item) in items" :key="item.key" class="menu-item" :class="{ 'danger': item.danger }"
					@click.stop="onSelectMenu(item)">
					<i v-if="item.icon" :class="item.icon" class="menu-icon"></i>
					<span class="menu-text">{{ item.name }}</span>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	name: "rightMenu",
	data() {
		return {
			show: false,
			pos: {
				x: 0,
				y: 0,
			},
			items: []
		}
	},
	methods: {
		open(pos, items) {
			this.pos.x = pos.x;
			this.pos.y = pos.y;
			this.items = items;
			this.show = true;
			this.rejustPos();
		},
		close() {
			this.show = false;
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.close();
		},
		rejustPos() {
			let menuH = this.items.length * 40 + 16; // 增加内边距
			let menuW = 140; // 增加宽度
			if (this.pos.y > window.innerHeight - menuH) {
				this.pos.y = window.innerHeight - menuH;
			}
			if (this.pos.x > window.innerWidth - menuW) {
				this.pos.x = window.innerWidth - menuW;
			}
		}
	}
}
</script>

<style lang="scss">
.right-menu-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	width: 100%;
	height: 100%;
	z-index: 9999;
}

.right-menu {
	position: fixed;
	border-radius: 12px;
	overflow: hidden;
	box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20px);
	border: 1px solid rgba(255, 255, 255, 0.2);
	z-index: 10000;

	.menu-container {
		padding: 8px 0;
		min-width: 120px;

		.menu-item {
			display: flex;
			align-items: center;
			padding: 8px 16px;
			cursor: pointer;
			transition: all 0.2s ease;
			position: relative;
			font-size: 14px;
			color: var(--im-text-color);

			&:hover {
				background: var(--im-background-active);
				color: var(--im-color-primary);
			}

			&.danger {
				color: var(--im-color-danger);

				&:hover {
					background: rgba(245, 108, 108, 0.1);
					color: var(--im-color-danger);
				}

				&:active {
					background: rgba(245, 108, 108, 0.2);
					transform: scale(0.98);
				}
			}

			.menu-icon {
				font-size: 16px;
				margin-right: 8px;
				width: 16px;
				text-align: center;
			}

			.menu-text {
				flex: 1;
				font-weight: 500;
			}
		}
	}
}
</style>
