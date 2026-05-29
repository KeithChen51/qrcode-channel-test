export interface AuthUser {
  username: string
  mustChangePassword: boolean
}

const LEGACY_TOKEN_KEY = 'qrcode_admin_token'
const USER_KEY = 'qrcode_admin_user'

export function setAuthSession(_token: string, username: string, mustChangePassword: boolean) {
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
  localStorage.removeItem(LEGACY_TOKEN_KEY)
  sessionStorage.removeItem(LEGACY_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function hasAuthSession(): boolean {
  return Boolean(getAuthUser())
}
