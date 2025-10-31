export const AuthScopeEnum = {
    SYSTEM: 'SYSTEM',
    ORGANIZATION: 'ORGANIZATION',
    PROJECT: 'PROJECT',
} as const
export type AuthScopeEnumType = typeof AuthScopeEnum[keyof typeof AuthScopeEnum];