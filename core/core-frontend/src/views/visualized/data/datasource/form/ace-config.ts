import ace from 'ace-builds'
import themeChromeUrl from 'ace-builds/src-noconflict/theme-chrome?url'
ace.config.setModuleUrl('ace/theme/chrome', themeChromeUrl)

import modeJsonUrl from 'ace-builds/src-noconflict/mode-json?url'
ace.config.setModuleUrl('ace/mode/json', modeJsonUrl)

import modeXmlUrl from 'ace-builds/src-noconflict/mode-xml?url'
ace.config.setModuleUrl('ace/mode/xml', modeXmlUrl)

import modeTextUrl from 'ace-builds/src-noconflict/mode-text?url'
ace.config.setModuleUrl('ace/mode/text', modeTextUrl)

import 'ace-builds/src-noconflict/ext-language_tools'
ace.require('ace/ext/language_tools')
