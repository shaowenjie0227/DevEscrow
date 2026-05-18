function readRoles(userInfo) {
  return Array.isArray(userInfo?.roles) ? userInfo.roles : []
}

export function isDeveloperWorkspaceEnabled(userInfo) {
  const redirectPath = typeof userInfo?.redirectPath === 'string' ? userInfo.redirectPath : ''
  if (redirectPath.startsWith('/developer')) {
    return true
  }
  if (redirectPath.startsWith('/client')) {
    return false
  }

  if (readRoles(userInfo).includes('DEVELOPER')) {
    return true
  }

  return Number(userInfo?.developerStatus || 0) === 2
}

export function resolveWorkspaceBasePath(userInfo) {
  return isDeveloperWorkspaceEnabled(userInfo) ? '/developer' : '/client'
}

export function resolveWorkspaceSectionPath(userInfo, section = 'home') {
  return `${resolveWorkspaceBasePath(userInfo)}/${section}`
}

export function resolveWorkspaceHomePath(userInfo) {
  return resolveWorkspaceSectionPath(userInfo, 'home')
}

export function resolveWorkspaceInboxPath(userInfo) {
  return resolveWorkspaceSectionPath(userInfo, 'inbox')
}

export function resolveWorkspaceChatPath(userInfo) {
  return resolveWorkspaceSectionPath(userInfo, 'messages')
}
