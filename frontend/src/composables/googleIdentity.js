let initializedClientId = null
let activeCredentialHandler = null

export function renderGoogleButton(element, clientId, credentialHandler) {
  if (!element || !clientId || !window.google?.accounts?.id) return false
  activeCredentialHandler = credentialHandler
  if (initializedClientId !== clientId) {
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: response => activeCredentialHandler?.(response)
    })
    initializedClientId = clientId
  }
  element.replaceChildren()
  window.google.accounts.id.renderButton(element, {
    type: 'standard',
    theme: 'outline',
    size: 'large',
    text: 'continue_with',
    shape: 'rectangular',
    width: 400,
    locale: 'vi'
  })
  return true
}

export function detachGoogleCredentialHandler(credentialHandler) {
  if (activeCredentialHandler === credentialHandler) activeCredentialHandler = null
}
