export interface AuthenticationResponse {
    accessToken: string;
    refreshToken: string;
}
export interface UserState {
    id: string;
    name: string;
    email: string;
    phone: string;
    enable?: boolean;
    lastOrganizationId?: string;
    lastProjectId?: string;
    avatar?: string;
    userGroup?: string[]
}