import request from '@/utils/http'

/** 获取 R2 预签名上传 URL */
export function fetchGetPresignedUrl(fileName: string, contentType: string) {
  return request.get<Api.Upload.PresignedUrlVo>({
    url: '/api/admin/upload/presigned',
    params: { fileName, contentType }
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

/** 一键上传：获取预签名URL + 直传R2，返回文件访问地址 */
export async function uploadImage(file: File): Promise<string> {
  // 1. 获取预签名 URL
  const { data } = await fetchGetPresignedUrl(file.name, file.type)
  // 2. 直传到 R2
  const response = await uploadToR2(data.uploadUrl, file)
  if (!response.ok) {
    throw new Error('上传失败: ' + response.status)
  }
  // 3. 返回文件访问 URL
  return data.fileUrl
}
