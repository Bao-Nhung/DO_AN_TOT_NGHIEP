const defaultPaymentConfig = {
  vietqr: {
    bankId: 'VCB',
    accountNo: '9869167207',
    accountName: 'NGUYEN TIEN THANH',
  },
  momo: {
    account: '0869167207',
    holder: 'NGUYEN TIEN THANH',
  },
  zalopay: {
    account: '0869167207',
    holder: 'NGUYEN TIEN THANH',
  },
}

export const paymentConfig = {
  vietqr: {
    bankId: import.meta.env.VITE_VIETQR_BANK_ID || defaultPaymentConfig.vietqr.bankId,
    accountNo: import.meta.env.VITE_VIETQR_ACCOUNT_NO || defaultPaymentConfig.vietqr.accountNo,
    accountName: import.meta.env.VITE_VIETQR_ACCOUNT_NAME || defaultPaymentConfig.vietqr.accountName,
  },
  momo: {
    account: import.meta.env.VITE_MOMO_ACCOUNT || defaultPaymentConfig.momo.account,
    holder: import.meta.env.VITE_PAYMENT_ACCOUNT_NAME || defaultPaymentConfig.momo.holder,
  },
  zalopay: {
    account: import.meta.env.VITE_ZALOPAY_ACCOUNT || defaultPaymentConfig.zalopay.account,
    holder: import.meta.env.VITE_PAYMENT_ACCOUNT_NAME || defaultPaymentConfig.zalopay.holder,
  },
}

export function isVietQrConfigured() {
  return Boolean(paymentConfig.vietqr.bankId && paymentConfig.vietqr.accountNo && paymentConfig.vietqr.accountName)
}

export function createVietQrUrl(amount, content) {
  if (!isVietQrConfigured()) return ''
  const bankId = encodeURIComponent(paymentConfig.vietqr.bankId)
  const accountNo = encodeURIComponent(paymentConfig.vietqr.accountNo)
  const accountName = encodeURIComponent(paymentConfig.vietqr.accountName)
  const addInfo = encodeURIComponent(content || 'ZESTIA')
  return `https://img.vietqr.io/image/${bankId}-${accountNo}-compact2.png?amount=${Number(amount || 0)}&addInfo=${addInfo}&accountName=${accountName}`
}
