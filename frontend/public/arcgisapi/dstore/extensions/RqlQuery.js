define([
	'dojo/_base/declare',
	'rql/js-array',
	'../SimpleQuery'
], function (declare, arrayEngine, SimpleQuery) {
	return declare(SimpleQuery, {
		_createFilterQuerier: function (filter) {
			return filter.type === 'string'
				? arrayEngine.query(filter.args[0])
				: this.inherited(arguments);
		}
	});
});
