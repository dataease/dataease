/**
 * Applies all properties from a source object to a target object. Traverses inner objects.
 * Undefined properties are not written from the source object. 
 * Undefined properties in the target object are overwritten.
 * @param targetObject Target object (object to write to).
 * @param sourceObject Source object (object to write from).
 * @param overwrite If false, doesn't override existing non-object properties. Default - false.
 * @returns targetObject
 */
export function populateObject(targetObject: object, sourceObject: object, overwrite?: boolean): object;

/**
 * Copies own properties of the source object to the target object. A shallow copying is applied.
 * Own functions and undefined values are not copied.
 * @param source A source object.
 * @param target A target object. If missing, a new target object is created.
 * @param conversionCallback Optional conversion callback applied to Object-type properties.
 * If it is missing, these properties are assigned as is.
 * If object-type property should be ignored, the callback should return an undefined value.
 * @returns Returns the target object.
 */
export function copyOwnJsonProperties(
  source: object,
  target?: object | undefined,
  conversionCallback?: (property: string, value: AnyJson) => AnyJson
): object;

/**
 * Removes undefined properties in the given object.
 * @param obj 
 * @returns Returns the passed object.
 */
export function removeUndefinedProperties(obj: object): object;

/**
 * Accesses a value in an object by path.
 *  Path can be specified as "a.b.c", if not specified the passed object is returned.
 * @param object 
 * @param path
 */
export function getValue(object: object, path?: string): any; // any because of ArrayUtil.ts 

/**
 * // Rounds a number to the specified places after decimal
 * @param value 
 * @param places 
 */
export function roundNumber(value: number, places?: number): number;

/**
 * Retruns the number of places the value can be round with without losing the precision.
 * @param value 
 */
export function getBestPrecision(value: number): number;

// Get the number of places after the decimal point for the given numeric value.
//  value: Number
//      A numeric value.
//  ignoreInsignificantDigits: Boolean
//      If true, the method tries to recognize insignificant digits in the fractional part and reduce the number of places.
// Returns the number of places recognized or -1 if the input value is not a number.
/**
 *  Get the number of places after the decimal point for the given numeric value.
 * @param value A numeric value.
 * @param ignoreInsignificantDigits If true, the method tries to recognize insignificant digits in the fractional part and reduce the number of places.
 * @returns Returns the number of places recognized or -1 if the input value is not a number.
 */
export function getPlaces(value: number, ignoreInsignificantDigits?: boolean): number;

/**
 * @param places The number of places after the fractional separator.
 * @param locale The required locale. If missing, the current locale is applied.
 * @param preserveTrailingZeroes Preserve trailing zeroes after the decimal point or not. 
 * Trailing zeroes are remove by default.
 * @param noSeparator Don't use thousand separator. Thousand separator is added by default.
 */
export type FormatParams = {
  places: number,
  locale?: string,
  preserveTrailingZeroes?: boolean,
  noSeparator?: boolean
}

/**
 * Formats a numeric value in the current locale removing trailing zeroes after the fractional separator.
 * @param value A numeric value to format.
 * @param params It is either the number of places after the fractional separator or formatParams object
 * @returns Formatted value
 */
export function formatNumber(value: number, params: number | FormatParams | object): string;

/**
 * Parses a string as a number and rounds it to the given number of places.
 * First it parses a value in the current locale. If parsing fails, then it
 * tries to parse a value in the universal locale. If both attempts fail,
 * it returns NaN. If places parameter is specified, the resulting number
 * is additionally rounded to the given number of places.
 * @param value Value to parse.
 * @param places The number of decimal palces to round value.
 * @param emptyStringValue Value returned in the case of empty string.
 */
export function parseNumber(value: any, places?: number, emptyStringValue?: boolean): number;

/**
 * Compares two numbers with the specified precision.
 * @param n1 
 * @param n2 
 * @param decimals 
 */
export function compareEqual(n1: any, n2: any, decimals?: number): boolean;

