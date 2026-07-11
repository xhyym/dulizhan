/**
 * 后台图片上传用途，与后端预签名接口的 purpose 参数保持一致。
 */
export type AdminImageUploadPurpose =
  | 'category_image'
  | 'product_gallery'
  | 'product_poster'
  | 'product_detail'
  | 'site_logo'
  | 'site_favicon'
  | 'site_page_banner'
  | 'home_banner'
  | 'about_story'
  | 'about_craft'

export interface ImageUploadRule {
  purpose: AdminImageUploadPurpose
  fieldLabel: string
  minWidth: number
  minHeight: number
  ratioLabel: string
  minRatio: number
  maxRatio: number
  maxSizeInBytes: number
  allowedTypes?: string[]
}

interface ImageSize {
  width: number
  height: number
}

export const DEFAULT_ALLOWED_IMAGE_TYPES = [
  'image/jpeg',
  'image/jpg',
  'image/png',
  'image/gif',
  'image/webp'
]

/**
 * 读取图片真实尺寸，统一用于上传前做分辨率和比例校验。
 */
function readImageSize(file: File): Promise<ImageSize> {
  return new Promise((resolve, reject) => {
    const imageUrl = URL.createObjectURL(file)
    const image = new Image()

    image.onload = () => {
      const imageSize = { width: image.width, height: image.height }
      URL.revokeObjectURL(imageUrl)
      resolve(imageSize)
    }

    image.onerror = () => {
      URL.revokeObjectURL(imageUrl)
      reject(new Error('图片解析失败，请更换文件后重试'))
    }

    image.src = imageUrl
  })
}

function formatFileSize(bytes: number): string {
  if (bytes >= 1024 * 1024) {
    return `${(bytes / 1024 / 1024).toFixed(0)}MB`
  }

  if (bytes >= 1024) {
    return `${(bytes / 1024).toFixed(0)}KB`
  }

  return `${bytes}B`
}

/**
 * 统一校验后台图片上传规则，避免各页面重复维护同类逻辑。
 */
export async function validateImageUpload(file: File, rule: ImageUploadRule) {
  const allowedTypes = rule.allowedTypes?.length ? rule.allowedTypes : DEFAULT_ALLOWED_IMAGE_TYPES
  const normalizedFileType = file.type.toLowerCase()

  if (!allowedTypes.includes(normalizedFileType)) {
    throw new Error(`${rule.fieldLabel}仅支持 JPG、PNG、GIF、WEBP 格式`)
  }

  if (file.size > rule.maxSizeInBytes) {
    throw new Error(`${rule.fieldLabel}大小不能超过 ${formatFileSize(rule.maxSizeInBytes)}`)
  }

  const { width, height } = await readImageSize(file)
  if (width < rule.minWidth || height < rule.minHeight) {
    throw new Error(`${rule.fieldLabel}分辨率不能低于 ${rule.minWidth}×${rule.minHeight}`)
  }

  const ratio = width / height
  if (ratio < rule.minRatio || ratio > rule.maxRatio) {
    throw new Error(`${rule.fieldLabel}比例不符合要求，请上传接近 ${rule.ratioLabel} 的图片`)
  }
}
