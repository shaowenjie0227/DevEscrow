export const AI_ASSISTANT_QUERY_KEY = 'assistant'
export const AI_ASSISTANT_QUERY_VALUE = 'ai'

const AI_ASSISTANT_PREFILL_KEY = 'ai-demand-assistant:prefill'
const AI_ASSISTANT_PENDING_PROMPT_KEY = 'ai-demand-assistant:pending-prompt'

function safeSessionStorage() {
  if (typeof window === 'undefined') {
    return null
  }

  try {
    return window.sessionStorage
  } catch (error) {
    return null
  }
}

function writeSessionValue(key, value) {
  const storage = safeSessionStorage()
  if (!storage) return

  try {
    storage.setItem(key, JSON.stringify(value))
  } catch (error) {
    // Ignore storage write failures and keep runtime flow working.
  }
}

function readSessionValue(key, { consume = false } = {}) {
  const storage = safeSessionStorage()
  if (!storage) return null

  try {
    const raw = storage.getItem(key)
    if (!raw) {
      return null
    }

    if (consume) {
      storage.removeItem(key)
    }

    return JSON.parse(raw)
  } catch (error) {
    if (consume) {
      clearSessionValue(key)
    }
    return null
  }
}

function clearSessionValue(key) {
  const storage = safeSessionStorage()
  if (!storage) return

  try {
    storage.removeItem(key)
  } catch (error) {
    // Ignore storage clear failures.
  }
}

export function isAiAssistantQueryActive(query = {}) {
  return query?.[AI_ASSISTANT_QUERY_KEY] === AI_ASSISTANT_QUERY_VALUE
}

export function createAiAssistantOpenQuery(query = {}) {
  return {
    ...query,
    [AI_ASSISTANT_QUERY_KEY]: AI_ASSISTANT_QUERY_VALUE
  }
}

export function createAiAssistantClosedQuery(query = {}) {
  const nextQuery = { ...query }
  delete nextQuery[AI_ASSISTANT_QUERY_KEY]
  return nextQuery
}

export function saveAiDemandAssistantPrefill(payload) {
  writeSessionValue(AI_ASSISTANT_PREFILL_KEY, payload)
}

export function consumeAiDemandAssistantPrefill() {
  return readSessionValue(AI_ASSISTANT_PREFILL_KEY, { consume: true })
}

export function saveAiDemandAssistantPendingPrompt(payload) {
  writeSessionValue(AI_ASSISTANT_PENDING_PROMPT_KEY, payload)
}

export function consumeAiDemandAssistantPendingPrompt() {
  return readSessionValue(AI_ASSISTANT_PENDING_PROMPT_KEY, { consume: true })
}

export function clearAiDemandAssistantPendingPrompt() {
  clearSessionValue(AI_ASSISTANT_PENDING_PROMPT_KEY)
}
