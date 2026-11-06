// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** createTask POST /api/video/task/create */
export async function createTaskUsingPost(
  body: API.VideoTaskCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseVideoTaskVO_>('/api/video/task/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getTask GET /api/video/task/get */
export async function getTaskUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTaskUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseVideoTaskVO_>('/api/video/task/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
