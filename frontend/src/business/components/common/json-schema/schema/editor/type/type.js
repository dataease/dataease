import _object from './object'
import _string from './string'
import _array from './array'
import _boolean from './boolean'
import _integer from './integer'
import _number from './number'
const TYPE_NAME = ['string', 'number', 'integer','object', 'array',  'boolean']

const TYPE = {
    'object': _object,
    'string': _string,
    'array': _array,
    'boolean': _boolean,
    'integer': _integer,
    'number': _number
}
export {TYPE ,TYPE_NAME}
