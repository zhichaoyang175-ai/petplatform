import request from './request'

/** 分页搜索宠物 */
export function searchPets(params = {}) {
  return request.get('/pets', { params })
}

/** 获取宠物详情 */
export function getPetDetail(id, currentUserId) {
  const params = currentUserId ? { currentUserId } : {}
  return request.get(`/pets/${id}`, { params })
}

/** 发布宠物 */
export function createPet(data) {
  return request.post('/pets', data)
}

/** 更新宠物 */
export function updatePet(id, data) {
  return request.put(`/pets/${id}`, data)
}

/** 下架宠物 */
export function deletePet(id) {
  return request.delete(`/pets/${id}`)
}

/** 修改宠物状态 */
export function updatePetStatus(id, status) {
  return request.put(`/pets/${id}/status`, null, { params: { status } })
}

/** 上传宠物图片 */
export function uploadPetImages(id, files) {
  const formData = new FormData()
  for (const file of files) {
    formData.append('files', file)
  }
  return request.post(`/pets/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 删除宠物图片 */
export function deletePetImage(petId, imageId) {
  return request.delete(`/pets/${petId}/images/${imageId}`)
}

/** 我的宠物列表 */
export function getMyPets(params = {}) {
  return request.get('/pets/my', { params })
}

/** 平台统计数据 */
export function getStats() {
  return request.get('/pets/stats')
}
