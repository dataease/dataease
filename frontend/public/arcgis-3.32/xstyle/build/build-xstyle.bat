cd ..\util\buildscripts
cmd /C build.bat profile=../../xstyle/build/xstyle.profile.js
cd ..\..\xstyle
copy /Y ..\xstyle-release\xstyle\main.js ..\xstyle-gh-pages\xstyle.js
copy /Y ..\xstyle-release\xstyle\gh-pages.js ..\xstyle-gh-pages\gh-pages.js
# cat core/amdLoader.js ../put-selector/put.js core/observe.js core/Rule.js core/expression.js core/generate.js core/elemental.js core/base.js core/parser.js main.js > xstyle.js
# uglifyjs xstyle.js -o xstyle.min.js --source-map xstyle.min.js.map -p 2 -c -m