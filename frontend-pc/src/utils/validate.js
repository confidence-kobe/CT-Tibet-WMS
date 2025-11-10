/**
 * 表单验证工具函数
 * 提供常用的表单字段验证规则
 */

/**
 * 验证手机号码（中国大陆）
 * @param {string} phone - 手机号码
 * @returns {boolean} 验证通过返回true
 */
export function isValidPhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证邮箱地址
 * @param {string} email - 邮箱地址
 * @returns {boolean} 验证通过返回true
 */
export function isValidEmail(email) {
  const reg = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/
  return reg.test(email)
}

/**
 * 验证身份证号码（18位）
 * @param {string} idCard - 身份证号码
 * @returns {boolean} 验证通过返回true
 */
export function isValidIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return reg.test(idCard)
}

/**
 * 验证用户名（4-20位字母、数字、下划线）
 * @param {string} username - 用户名
 * @returns {boolean} 验证通过返回true
 */
export function isValidUsername(username) {
  const reg = /^[a-zA-Z0-9_]{4,20}$/
  return reg.test(username)
}

/**
 * 验证密码强度（至少8位，包含大小写字母和数字）
 * @param {string} password - 密码
 * @returns {boolean} 验证通过返回true
 */
export function isValidPassword(password) {
  const reg = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/
  return reg.test(password)
}

/**
 * 验证中文姓名（2-20位中文字符）
 * @param {string} name - 姓名
 * @returns {boolean} 验证通过返回true
 */
export function isValidChineseName(name) {
  const reg = /^[\u4e00-\u9fa5]{2,20}$/
  return reg.test(name)
}

/**
 * 验证URL地址
 * @param {string} url - URL地址
 * @returns {boolean} 验证通过返回true
 */
export function isValidURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * 验证IP地址
 * @param {string} ip - IP地址
 * @returns {boolean} 验证通过返回true
 */
export function isValidIP(ip) {
  const reg = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  return reg.test(ip)
}

/**
 * 验证固定电话（区号-号码）
 * @param {string} tel - 固定电话
 * @returns {boolean} 验证通过返回true
 */
export function isValidTel(tel) {
  const reg = /^0\d{2,3}-?\d{7,8}$/
  return reg.test(tel)
}

/**
 * 验证邮政编码
 * @param {string} postcode - 邮政编码
 * @returns {boolean} 验证通过返回true
 */
export function isValidPostcode(postcode) {
  const reg = /^[1-9]\d{5}$/
  return reg.test(postcode)
}

/**
 * 验证是否为正整数
 * @param {string|number} num - 数字
 * @returns {boolean} 验证通过返回true
 */
export function isPositiveInteger(num) {
  const reg = /^[1-9]\d*$/
  return reg.test(num)
}

/**
 * 验证是否为数字（包含小数）
 * @param {string|number} num - 数字
 * @returns {boolean} 验证通过返回true
 */
export function isNumber(num) {
  const reg = /^-?\d+\.?\d*$/
  return reg.test(num)
}

/**
 * Element Plus表单验证规则 - 手机号
 */
export const phoneRule = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!isValidPhone(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

/**
 * Element Plus表单验证规则 - 邮箱
 */
export const emailRule = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!isValidEmail(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

/**
 * Element Plus表单验证规则 - 用户名
 */
export const usernameRule = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!isValidUsername(value)) {
    callback(new Error('用户名为4-20位字母、数字或下划线'))
  } else {
    callback()
  }
}

/**
 * Element Plus表单验证规则 - 密码
 */
export const passwordRule = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 8) {
    callback(new Error('密码至少8位'))
  } else {
    callback()
  }
}

/**
 * Element Plus表单验证规则 - 强密码
 */
export const strongPasswordRule = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!isValidPassword(value)) {
    callback(new Error('密码至少8位，需包含大小写字母和数字'))
  } else {
    callback()
  }
}
