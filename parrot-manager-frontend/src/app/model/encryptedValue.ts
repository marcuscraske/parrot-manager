export class EncryptedValue
{
    id: string;
    lastModified: number;

    constructor(nativeEncryptedValue)
    {
        this.id = nativeEncryptedValue.getId();
        this.lastModified = nativeEncryptedValue.getLastModified();
    }
}
