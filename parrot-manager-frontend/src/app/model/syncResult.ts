import { Log } from 'app/model/log'

export class SyncResult
{
    profileId: string;
    hostName: string;
    log: Log;
    success: boolean;
}
