import { MergeLog } from 'app/model/mergeLog'

export class SyncResult
{
    hostName: string;
    mergeLog: MergeLog;
    success: boolean;
    error: string;
}
