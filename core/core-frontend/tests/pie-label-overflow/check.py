import os
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch()
    page = browser.new_page(viewport={"width": 1200, "height": 900})
    errors = []
    page.on('pageerror', lambda error: errors.append(str(error)))
    page.goto(os.environ.get('PIE_TEST_URL', 'http://127.0.0.1:7087/'))
    page.wait_for_function('window.charts?.length === 2')
    result = page.evaluate('''async () => {
      let cases = 0, originalOverflow = 0, wrapped = 0;
      for (const count of [3, 40, 80]) for (const width of [280, 390, 760])
      for (const fontSize of [12, 32]) for (const fullDisplay of [false, true])
      for (const rose of [false, true]) {
        await renderCase({count, width, fontSize, fullDisplay, rose});
        const all = charts[1].getContext().canvas.document.documentElement.querySelectorAll('.label');
        const labels = all.filter(l => l.style.visibility !== 'hidden');
        if (fullDisplay && labels.length !== count) throw Error('Missing labels');
        const boxes = labels.map(l => {
          const text = l.querySelector('text');
          if (!String(l.style.text).trim()) throw Error('Empty label');
          if (String(l.style.text).includes('\\n')) wrapped++;
          if (!l.querySelector('path')) throw Error('Missing connector');
          const b = text.getBounds();
          if (b.min[0] < -1 || b.max[0] > width+1 || b.min[1] < -1 || b.max[1] > 261)
            throw Error(JSON.stringify({count,width,fontSize,fullDisplay,rose,b}));
          return b;
        });
        if (!fullDisplay) boxes.forEach((a,i) => boxes.slice(i+1).forEach(b => {
          if (a.min[0]<b.max[0] && a.max[0]>b.min[0] && a.min[1]<b.max[1] && a.max[1]>b.min[1])
            throw Error('Overlapping normal labels');
        }));
        originalOverflow += charts[0].getContext().canvas.document.documentElement.querySelectorAll('.label')
          .filter(l=>{const b=l.querySelector('text').getBounds();return b.min[0]<0||b.max[0]>width}).length;
        cases++;
      }
      return {cases, originalOverflow, wrapped};
    }''')
    assert result['originalOverflow'] > 0, result
    assert result['wrapped'] > 0, result
    assert not errors, errors
    page.evaluate('renderCase({count:40, fullDisplay:true})')
    page.screenshot(path='/tmp/dataease-v3-pie-label-overflow.png')
    print('PASS', result)
    browser.close()
