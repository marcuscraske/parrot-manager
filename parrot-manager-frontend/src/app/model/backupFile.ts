export class BackupFile
{
    name: string;
    path: string;
    lastModified: number;

    constructor(name, path, lastModified)
    {
        this.name = name;
        this.path = path;
        this.lastModified = lastModified;
    }

}