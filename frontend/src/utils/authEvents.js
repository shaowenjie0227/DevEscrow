export const AUTH_EXPIRED_EVENT = 'app:auth-expired'
const AUTH_PROMPT_STORAGE_KEY = 'app:auth-prompt'

function resolveCurrentPath() {
  return `${window.location.pathname}${window.location.search}${window.location.hash}`
}

function resolveAuthPrompt(detail = {}) {
  return {
    redirectPath: resolveCurrentPath(),
    ...detail
  }
}

function persistAuthPrompt(detail) {
  try {
    window.sessionStorage.setItem(AUTH_PROMPT_STORAGE_KEY, JSON.stringify(detail))
  } catch (error) {
    // Ignore storage failures and still emit the runtime event.
  }
}

export function clearPendingAuthPrompt() {
  try {
    window.sessionStorage.removeItem(AUTH_PROMPT_STORAGE_KEY)
  } catch (error) {
    // Ignore storage failures when clearing stale prompts.
  }
}

export function consumePendingAuthPrompt() {
  try {
    const raw = window.sessionStorage.getItem(AUTH_PROMPT_STORAGE_KEY)
    if (!raw) {
      return null
    }

    window.sessionStorage.removeItem(AUTH_PROMPT_STORAGE_KEY)
    return JSON.parse(raw)
  } catch (error) {
    clearPendingAuthPrompt()
    return null
  }
}

export function emitAuthExpired(detail = {}) {
  const payload = resolveAuthPrompt(detail)
  persistAuthPrompt(payload)

  window.dispatchEvent(
    new CustomEvent(AUTH_EXPIRED_EVENT, {
      detail: payload
    })
  )
}
