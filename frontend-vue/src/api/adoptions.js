import request from './request'

/** 领养记录详情 */
export function getAdoptionDetail(id) {
  return request.get(`/adoptions/${id}`)
}

/** 我的领养（领养人视角） */
export function getMyAdoptions(params = {}) {
  return request.get('/adoptions', { params })
}

/** 我送养出去的（送养人视角） */
export function getMySentPets(params = {}) {
  return request.get('/adoptions/my-pets', { params })
}
