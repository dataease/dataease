# Installing via npm

Installing dgrid via npm requires some additional setup due to dstore being registered as `dojo-dstore`
and npm's lack of support for aliasing packages.  dgrid does not explicitly list `peerDependencies`
due to this complication, to avoid installing `dojo-dstore` under npm 2 even if `dstore` is already present.

Note that while dgrid is retrievable via npm, its modules are still written in the AMD format and some involve the use
of AMD plugins (i.e. it cannot be used with browserify).

## Instructions

First, install Dojo, dgrid, and dstore:

`npm install dojo dgrid dojo-dstore`

This will create `dojo`, `dgrid`, and `dojo-dstore` directories under `node_modules`.  However, applications
typically access dstore via the `dstore` package, not `dojo-dstore`.  This can be accommodated in one of two ways:

* Rename or symlink `dojo-dstore` to `dstore`
* Configure the AMD loader to look for `dstore` under `dojo-dstore`

Example AMD `packages` configuration for the second option:

```js
{
    async: true,
    packages: [
        { name: 'dstore', location: '../dojo-dstore' }
    ]
}
```

Or, if you are using an explicit `baseUrl` and specifying all packages' locations:

```js
{
    async: true,
    baseUrl: '.',
    packages: [
        { name: 'dojo', location: 'dojo' },
        { name: 'dgrid', location: 'dgrid' },
        { name: 'dstore', location: 'dojo-dstore' }
    ]
}
```
