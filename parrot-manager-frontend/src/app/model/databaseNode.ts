import { EncryptedValue } from "app/model/encryptedValue"

export class DatabaseNode
{
    id: string;
    parentId: string;
    name: string;
    lastModified: number;
    isRoot: boolean;
    value: EncryptedValue;

    constructor(nativeDatabaseNode)
    {
        this.id = nativeDatabaseNode.getId();

        var parentNativeNode = nativeDatabaseNode.getParent();
        if (parentNativeNode != null)
        {
            this.parentId = parentNativeNode.getId();
        }

        this.name = nativeDatabaseNode.getName();
        this.lastModified = nativeDatabaseNode.getLastModified();
        this.isRoot = nativeDatabaseNode.isRoot();

        var nativeValue = nativeDatabaseNode.getValue();
        if (nativeValue != null)
        {
            this.value = new EncryptedValue(nativeValue);
        }
    }

}
