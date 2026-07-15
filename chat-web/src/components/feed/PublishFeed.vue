<template>
  <el-dialog
    title="发布动态"
    :visible.sync="dialogVisible"
    width="540px"
    :close-on-click-modal="false"
    @close="reset"
    custom-class="publish-dialog"
  >
    <div class="publish-body">
      <el-input
        type="textarea"
        v-model="content"
        :rows="4"
        placeholder="分享新鲜事..."
        maxlength="2000"
        show-word-limit
        class="content-input"
      />
      <div class="image-preview" v-if="imageList.length">
        <div v-for="(img, idx) in imageList" :key="idx" class="preview-item">
          <img :src="img.url" />
          <span class="remove-btn" @click="removeImage(idx)">
            <i class="el-icon-close"></i>
          </span>
          <div v-if="img.uploading" class="upload-mask">
            <i class="el-icon-loading"></i>
          </div>
        </div>
        <div class="preview-item add-btn" v-if="imageList.length < 9" @click="triggerUpload">
          <i class="el-icon-plus"></i>
        </div>
      </div>
      <div class="image-actions" v-else>
        <span class="add-image-btn" @click="triggerUpload">
          <i class="el-icon-picture-outline"></i> 添加图片
        </span>
      </div>
      <input ref="fileInput" type="file" accept="image/*" multiple style="display:none" @change="onFileChange" />
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="publishing" :disabled="!canPublish" @click="publish">发 布</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "publishFeed",
  data() {
    return {
      dialogVisible: false,
      content: "",
      imageList: [],
      publishing: false
    }
  },
  computed: {
    canPublish() {
      return this.content.trim() || this.imageList.some(img => !img.uploading);
    }
  },
  methods: {
    open() {
      this.dialogVisible = true;
    },
    reset() {
      this.content = "";
      this.imageList = [];
      this.publishing = false;
    },
    triggerUpload() {
      this.$refs.fileInput.click();
    },
    onFileChange(e) {
      const files = Array.from(e.target.files);
      files.forEach(file => this.uploadImage(file));
      e.target.value = "";
    },
    uploadImage(file) {
      const idx = this.imageList.length;
      this.imageList.push({ url: "", uploading: true });
      const formData = new FormData();
      formData.append("file", file);
      this.$http({
        url: "/image/upload?isPermanent=true",
        data: formData,
        method: "post",
        headers: { "Content-Type": "multipart/form-data" }
      }).then(data => {
        this.$set(this.imageList, idx, {
          url: data.originUrl,
          thumbUrl: data.thumbUrl,
          uploading: false
        });
      }).catch(() => {
        this.imageList.splice(idx, 1);
        this.$message.error("图片上传失败");
      });
    },
    removeImage(idx) {
      this.imageList.splice(idx, 1);
    },
    publish() {
      if (!this.canPublish) return;
      this.publishing = true;
      const imageUrls = this.imageList
        .filter(img => !img.uploading)
        .map(img => img.url);
      this.$http({
        url: "/feed/publish",
        method: "post",
        data: {
          content: this.content.trim(),
          images: imageUrls.length ? imageUrls : null
        }
      }).then(() => {
        this.$message.success("发布成功");
        this.dialogVisible = false;
        this.$emit("published");
      }).catch(() => {
        this.$message.error("发布失败");
      }).finally(() => {
        this.publishing = false;
      });
    }
  }
}
</script>

<style scoped>
.publish-body { padding: 0 4px; }
.content-input { margin-bottom: 16px; }
.image-actions { margin-bottom: 8px; }
.add-image-btn {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 13px; color: var(--im-color-primary);
  cursor: pointer; padding: 8px 0;
}
.image-preview {
  display: flex; flex-wrap: wrap; gap: 8px;
}
.preview-item {
  width: 80px; height: 80px; border-radius: 6px;
  position: relative; overflow: hidden;
  border: 1px solid rgba(255,255,255,0.08);
}
.preview-item img {
  width: 100%; height: 100%; object-fit: cover;
}
.remove-btn {
  position: absolute; top: 2px; right: 2px;
  width: 18px; height: 18px; border-radius: 50%;
  background: rgba(0,0,0,0.5); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; cursor: pointer;
}
.upload-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
}
.add-btn {
  display: flex; align-items: center; justify-content: center;
  border: 1px dashed rgba(255,255,255,0.15); cursor: pointer;
  font-size: 24px; color: rgba(255,255,255,0.3);
}
</style>
