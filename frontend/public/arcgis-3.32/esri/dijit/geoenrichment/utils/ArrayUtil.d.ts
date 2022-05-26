type IdentityObject = string | { [s: string]: any };

declare class ArrayUtil {

    static composeIdentityFunction(
        identity: string | ((obj: IdentityObject) => string),
        allowUndefinedIds?: boolean
    ): (obj: IdentityObject) => string;

    /**
     * Removes objects that have the same identity. Objects with undefined identity are also removed.
     *  @param array
     *      Input array of objects.
     *  @param identity
     *      Specifies how to identify objects of the input array
     *          String value specifies the property name;
     *          Function value specifies a function composing an identity for an object
     *          Missing value means the string object itself to be used as identity.
     * @returns a new array.
     */
    static removeDuplicates(
        array: IdentityObject[],
        identity: string | ((obj: IdentityObject) => string)
    ): any[];

    static splitArrayToBunches(array: any[], batchSize?: number): any[];
}

export = ArrayUtil;