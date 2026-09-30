import os
from playwright.sync_api import sync_playwright
with sync_playwright() as p:
 b=p.chromium.launch(headless=True)
 page=b.new_page(viewport={'width':1000,'height':800})
 page.on('pageerror',lambda e: print('ERROR',str(e)))
 state={'maxUploadMb':20,'maxStorageMb':512,'usedBytes':40*1024*1024,'maxUploadAllowedMb':499}
 def handle(route):
  if route.request.method=='POST': state.update(route.request.post_data_json)
  route.fulfill(json={'code':0,'data':state})
 page.route('**/typeface/settings',handle)
 page.goto(os.getenv('FONT_SETTINGS_TEST_URL', 'http://localhost:7070/tests/font-settings/index.html'))
 page.get_by_text('打开字体设置',exact=True).click(timeout=30000)
 page.get_by_role('dialog').wait_for()
 print(page.get_by_role('dialog').inner_text())
 page.get_by_role('spinbutton').first.wait_for()
 page.wait_for_timeout(400)
 inputs=page.get_by_role('spinbutton')
 assert inputs.nth(0).input_value()=='20'
 inputs.nth(0).fill('30'); inputs.nth(1).fill('25'); inputs.nth(1).blur()
 assert page.get_by_role('button',name='保存',exact=True).is_disabled()
 inputs.nth(1).fill('50'); inputs.nth(1).blur()
 page.get_by_role('button',name='保存',exact=True).click()
 page.get_by_role('dialog').wait_for(state='hidden')
 assert state['maxUploadMb']==30 and state['maxStorageMb']==50
 page.get_by_text('打开字体设置',exact=True).click()
 inputs.nth(0).wait_for()
 assert inputs.nth(0).input_value()=='30'
 inputs.nth(0).fill('10'); inputs.nth(1).fill('20'); inputs.nth(1).blur()
 print(page.get_by_role('dialog').inner_text())
 assert page.locator('.ed-alert').is_visible()
 page.get_by_role('button',name='取消',exact=True).click()
 assert state['maxUploadMb']==30
 print('PASS UI: render, bounds, save, reopen, quota warning, cancel')
 b.close()
