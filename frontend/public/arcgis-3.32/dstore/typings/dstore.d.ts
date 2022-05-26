/// <reference path="../dojo/dojo.d.ts" />

declare module dstore {
	export interface FetchArray<T> extends Array<T> {
		totalLength: number;
	}

	export interface FetchPromise<T> extends IPromise<T[]> {
		forEach(callback: (value: T, index?: number, array?: T[]) => void, thisObject?: any): FetchPromise<T>;
		response?: FetchResponse<T>;
		totalLength: IPromise<number>;
	}

	export interface FetchResponse<T> {
		data: T;
		options: { [key: string]: any; };
		status?: number;
		text: string;
		url: string;
		getHeader(name: string): string;
	}

	export interface ChangeEvent<T> {
		id: any;
		index?: number;
		previousIndex?: number;
		target: T;
		totalLength: number;
		type: string;
	}

	export interface ICollection<T> {
		idProperty: string;
		Model: { new (...args: any[]): T; };
		tracking?: { remove(): void; };

		add(object: T, options?: {}): IPromise<T>;
		emit(eventName: string, event: ChangeEvent<T>): boolean;
		fetch(): dstore.FetchPromise<T>;
		fetchRange(kwArgs: { start?: number; end?: number; }): dstore.FetchPromise<T>;
		filter(query: string | {} | { (item: T, index: number): boolean; }): ICollection<T>;
		forEach(callback: (item: T, index: number) => void, thisObject?: any): IPromise<T[]>;
		get(id: any): IPromise<T>;
		getIdentity(object: T): any;
		on(eventName: string, listener: (event: ChangeEvent<T>) => void): IHandle;
		put(object: T, options?: {}): IPromise<T>;
		remove(id: any): IPromise<Object>;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): ICollection<T>;
		track?(): ICollection<T>;
	}

	export interface ISyncCollection<T> extends ICollection<T> {
		addSync(object: T, options?: {}): T;
		fetchSync(): dstore.FetchArray<T>;
		fetchRangeSync(kwArgs: { start?: number; end?: number; }): dstore.FetchArray<T>;
		filter(query: string | {} | { (item: T, index: number): boolean; }): ISyncCollection<T>;
		getSync(id: any): T;
		putSync(object: T, options?: {}): T;
		removeSync(id: any): boolean;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): ISyncCollection<T>;
		track?(): ISyncCollection<T>;
	}
}

declare module 'dstore/Csv' {
	interface IStringifyOptions {
		alwaysQuote: boolean;
		trailingNewline: boolean;
	}

	class Csv<T> {
		fieldNames: string[];
		delimiter: string;
		newline: string;
		trim: boolean;

		parse(value: string): string[];
		toCsv(options: IStringifyOptions): string;
		stringify(data: any, options: IStringifyOptions): string;
	}

	export = Csv;
}

declare module 'dstore/Cache' {
	import Store = require('dstore/Store');

	class Cache<T> extends Store<T> {
		cachingStore: dstore.ICollection<T>;
		evict(id: any): void;
		invalidate(): void;
		isAvailableInCache(): void;
	}

	export = Cache;
}

declare module 'dstore/legacy/StoreAdapter' {
	import Store = require('dstore/Store');

	class StoreAdapter<T> extends Store<T> {}

	export = StoreAdapter;
}

declare module 'dstore/legacy/DstoreAdapter' {
	class DstoreAdapter<T> {
		constructor(collection: dstore.ICollection<T>);
		get(id: any): any;
		put(object: T, options?: {}): any;
		remove(id: any): any;
		query(query: any, options?: {}): any;
	}

	export = DstoreAdapter;
}

declare module 'dstore/Memory' {
	import Store = require('dstore/Store');

	class Memory<T> extends Store<T> implements dstore.ISyncCollection<T> {
		data: T[];

		constructor(kwArgs?: Memory.KwArgs<T>);

		addSync(object: T, options?: {}): T;
		fetchSync(): dstore.FetchArray<T>;
		fetchRangeSync(kwArgs: { start?: number; end?: number; }): dstore.FetchArray<T>;
		filter(query: string | {} | { (item: T, index: number): boolean; }): Memory<T>;
		getSync(id: any): T;
		putSync(object: T, options?: {}): T;
		removeSync(id: any): boolean;
		setData(data: T[]): void;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): Memory<T>;
		track(): Memory<T>;
	}

	module Memory {
		export interface KwArgs<T> extends Store.KwArgs {
			data?: T[];
		}
	}

	export = Memory;
}

declare module 'dstore/charting/StoreSeries' {
	class StoreSeries<T> {
		constructor(collection: dstore.ICollection<T> | dstore.ISyncCollection<T>, value: any);
		destroy(): void;
		setSeriesObject(series: any): void;
		fetch(): void;
	}

	export = StoreSeries;
}

declare module 'dstore/Tree' {
	import Store = require('dstore/Store');

	class Tree<T> extends Store<T> {
		mayHaveChildren(parent: T): boolean;
		getRootCollection(): Tree<T>;
		getChildren(parent: T): Tree<T>;
	}

	export = Tree;
}

declare module 'dstore/SimpleQuery' {
	import Store = require('dstore/Store');

	class SimpleQuery<T> extends Store<T> {}

	export = SimpleQuery;
}

declare module 'dstore/extensions/RqlQuery' {
	import SimpleQuery = require('dstore/SimpleQuery');

	class RqlQuery<T> extends SimpleQuery<T> {}

	export = RqlQuery;
}

declare module 'dstore/LocalDB' {
	import Store = require('dstore/Store');

	class LocalDB<T> extends Store<T> {
		constructor(kwArgs: LocalDB.KwArgs<T>);
		transaction(): LocalDB.ITransaction;
	}

	module LocalDB {
		export interface IConfigurationProperty {
			autoIncrement?: boolean;
			indexed?: boolean;
			multiEntry?: boolean;
			preference: number;
		}

		export interface IDBConfig {
			stores: { [key: string]: IDBStoreConfig };
			version: number;
		}

		export interface IDBStoreConfig {
			[key: string]: number | IConfigurationProperty;
		}

		export interface ITransaction {
			abort(): void;
			commit(): void;
		}

		export interface KwArgs<T> extends Store.KwArgs {
			dbConfig: IDBConfig;
			storeName: string;
		}
	}

	export = LocalDB;
}

declare module 'dstore/db/IndexedDB' {
	import LocalDB = require('dstore/LocalDB');

	class IndexedDB<T> extends LocalDB<T> {}

	export = IndexedDB;
}

declare module 'dstore/db/SQL' {
	import LocalDB = require('dstore/LocalDB');

	class SQL<T> extends LocalDB<T> {}

	export = SQL;
}

declare module 'dstore/db/LocalStorage' {
	import LocalDB = require('dstore/LocalDB');

	class LocalStorageDB<T> extends LocalDB<T> {}

	export = LocalStorageDB;
}

declare module 'dstore/Trackable' {
	class Trackable<T> {
		currentRange: any[];
		track(): dstore.ICollection<T>;
	}

	export = Trackable;
}

declare module 'dstore/Request' {
	import Store = require('dstore/Store');

	class Request<T> extends Store<T> {
		headers: {};
		parse: (serializedObject: string) => {};
		target: string;
		ascendingPrefix: string;
		descendingPrefix: string;
		accepts: string;

		constructor(kwArgs?: Request.KwArgs);

		filter(query: string | {} | { (item: T, index: number): boolean; }): Request<T>;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): Request<T>;
		track(): Request<T>;
	}

	module Request {
		export interface KwArgs extends Store.KwArgs {
			headers?: typeof Request.prototype.headers;
			parse?: typeof Request.prototype.parse;
			target?: typeof Request.prototype.target;
			ascendingPrefix?: typeof Request.prototype.ascendingPrefix;
			descendingPrefix?: typeof Request.prototype.descendingPrefix;
			accepts?: typeof Request.prototype.accepts;
		}
	}

	export = Request;
}

declare module 'dstore/RequestMemory' {
	import Request = require('dstore/Request');
	import Cache = require('dstore/Cache');

	class RequestMemory<T> extends Request<T> implements Cache<T> {
		cachingStore: dstore.ICollection<T>;
		evict(id: any): void;

		filter(query: string | {} | { (item: T, index: number): boolean; }): RequestMemory<T>;
		invalidate(): void;
		isAvailableInCache(): void;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): RequestMemory<T>;
		track(): RequestMemory<T>;
	}

	export = RequestMemory;
}

declare module 'dstore/Rest' {
	import Request = require('dstore/Request');

	class Rest<T> extends Request<T> {
		filter(query: string | {} | { (item: T, index: number): boolean; }): Rest<T>;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): Rest<T>;
		track(): Rest<T>;
	}

	export = Rest;
}

declare module 'dstore/Store' {
	import decl = require('dojo/_base/declare');
	class Store<T> extends decl.Base implements dstore.ICollection<T> {
		idProperty: string;
		Model: { new (...args: any[]): T; };
		total: IPromise<number>;

		constructor(kwArgs?: Store.KwArgs);

		add(object: T, options?: {}): IPromise<T>;
		emit(eventName: string, event: dstore.ChangeEvent<T>): boolean;
		fetch(): dstore.FetchPromise<T>;
		fetchRange(kwArgs: { start?: number; end?: number; }): dstore.FetchPromise<T>;
		filter(query: string | {} | { (item: T, index: number): boolean; }): Store<T>;
		forEach(callback: (item: T, index: number) => void, thisObject?: any): IPromise<T[]>;
		get(id: any): IPromise<T>;
		getIdentity(object: T): any;
		on(eventName: string, listener: (event: dstore.ChangeEvent<T>) => void): IHandle;
		put(object: T, options?: {}): IPromise<T>;
		remove(id: any): IPromise<{}>;
		sort(property: string | { (a: T, b: T): number; }, descending?: boolean): Store<T>;
	}

	module Store {
		export interface KwArgs {
			idProperty?: typeof Store.prototype.idProperty;
			Model?: typeof Store.prototype.Model;
		}
	}

	export = Store;
}
