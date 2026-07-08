import request from './request'

/** 分页公开列表（支持 province/city/keyword 过滤） */
export function listShelters(params = {}) {
  return request.get('/shelters', { params })
}

/** 获取救助站详情 */
export function getShelter(id) {
  return request.get(`/shelters/${id}`)
}

/** 获取救助站在养宠物列表 */
export function getShelterPets(id) {
  return request.get(`/shelters/${id}/pets`)
}

/** 创建救助站（需登录） */
export function createShelter(data) {
  return request.post('/shelters', data)
}

/** 更新救助站（需登录） */
export function updateShelter(id, data) {
  return request.put(`/shelters/${id}`, data)
}

/** 我的救助站（需登录） */
export function getMyShelters() {
  return request.get('/shelters/my')
}
