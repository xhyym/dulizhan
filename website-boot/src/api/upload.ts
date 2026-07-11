import request from '@/utils/http'
import type { AdminImageUploadPurpose } from '@/utils/ui/image-upload'

/** 获取 R2 预签名上传 URL */
export function fetchGetPresignedUrl(
  fileName: string,
  contentType: string,
  fileSize?: number,
  purpose?: AdminImageUploadPurpose
) {
  return request.get<Api.Upload.PresignedUrlVo>({
    url: '/api/admin/upload/presigned',
    params: { fileName, contentType, fileSize, purpose }
  })
}

/** 直传文件到 R2 (不经过后端代理) */
export async function uploadToR2(uploadUrl: string, file: File): Promise<Response> {
  return fetch(uploadUrl, {
    method: 'PUT',
    body: file,
    headers: {
      'Content-Type': file.type
    }
  })
}

/** 上传图片到 R2 */
export async function uploadImage(file: File, purpose?: AdminImageUploadPurpose): Promise<string> {
  const presignedUrl = await fetchGetPresignedUrl(file.name, file.type, file.size, purpose)

  if (!presignedUrl?.uploadUrl) {
    throw new Error('获取预签名 URL 失败')
  }

  const response = await uploadToR2(presignedUrl.uploadUrl, file)
  if (!response.ok) {
    throw new Error(`R2 上传失败: ${response.status}`)
  }

  return presignedUrl.fileUrl
}

/** 删除 R2 图片 */
export function deleteImage(fileUrl: string) {
  return request.del({
    url: '/api/admin/upload',
    params: { fileUrl }
  })
}
