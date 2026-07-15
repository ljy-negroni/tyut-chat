/**
 * 图片前端压缩工具
 * 将图片压缩到指定尺寸和质量的 JPEG
 *
 * @param {File} file - 原始图片文件
 * @param {Object} options
 * @param {number} options.maxWidth - 最大宽度，默认 1920
 * @param {number} options.maxHeight - 最大高度，默认 1920
 * @param {number} options.quality - JPEG 质量 0-1，默认 0.8
 * @returns {Promise<File>} 压缩后的 File 对象
 */
export function compressImage(file, options = {}) {
  const { maxWidth = 1920, maxHeight = 1920, quality = 0.8 } = options;

  return new Promise((resolve, reject) => {
    // GIF/PNG 小图不压缩
    if (file.type === 'image/gif') {
      return resolve(file);
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        let { width, height } = img;

        // 无需压缩
        if (width <= maxWidth && height <= maxHeight && file.size < 500 * 1024) {
          return resolve(file);
        }

        // 等比缩放
        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width);
          width = maxWidth;
        }
        if (height > maxHeight) {
          width = Math.round((width * maxHeight) / height);
          height = maxHeight;
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        canvas.toBlob(
          (blob) => {
            if (!blob) return resolve(file);
            const compressed = new File([blob], file.name, {
              type: 'image/jpeg',
              lastModified: Date.now()
            });
            resolve(compressed);
          },
          'image/jpeg',
          quality
        );
      };
      img.onerror = () => resolve(file);
      img.src = e.target.result;
    };
    reader.onerror = () => resolve(file);
    reader.readAsDataURL(file);
  });
}

/**
 * 批量压缩
 */
export async function compressImages(files, options) {
  const results = await Promise.all(
    Array.from(files).map(f => compressImage(f, options))
  );
  return results;
}
