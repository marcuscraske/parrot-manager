import { SyncProfile } from 'app/model/syncProfile'

export class SshSyncProfile extends SyncProfile
{
    id: string;
    name: string;
    host: string;
    port: number;
    user: string;
    remotePath: string;
    userPass: string;
    privateKeyPath: string;
    privateKeyPass: string;
    proxyHost: string;
    proxyPort: string;
    proxyType: string;
    promptUserPass: boolean;
    promptKeyPass: boolean;
    strictHostChecking: boolean;
}
