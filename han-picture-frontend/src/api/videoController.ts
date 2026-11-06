// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** deleteVideo POST /api/video/delete */
export async function deleteVideoUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/video/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** downloadVideo GET /api/video/download */
export async function downloadVideoUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.downloadVideoUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/video/download', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** editVideo POST /api/video/edit */
export async function editVideoUsingPost(
  body: API.VideoEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/video/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getVideoVOById GET /api/video/get/vo */
export async function getVideoVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getVideoVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseVideoVO_>('/api/video/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listVideoVOByPage POST /api/video/list/page/vo */
export async function listVideoVoByPageUsingPost(
  body: API.VideoQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageVideoVO_>('/api/video/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadVideo POST /api/video/upload */
export async function uploadVideoUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadVideoUsingPOSTParams,
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseVideoVO_>('/api/video/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}
