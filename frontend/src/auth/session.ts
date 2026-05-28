export interface AuthUser {
  username: string
  mustChangePassword: boolean
}

const TOKEN_KEY = 'qrcode_admin_token'
const USER_KEY = 'qrcode_admin_user'

export function getAuthToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setAuthSession(token: string, username: string, mustChangePassword: boolean) {
  localStorage.setItem(TOKEN_KEY, token)
  setAuthUser(username, mustChangePassword)
}

export function setAuthUser(username: string, mustChangePassword: boolean) {
  const user: AuthUser = { username, mustChangePassword }
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getAuthUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    clearAuthSession()
    return null
  }
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
