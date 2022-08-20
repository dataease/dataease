import amap from '@/components/amap-wx/lib/amap-wx.js';
// 地铁颜色图
const line = {
	'1号线': '#C43B33',
	'2号线': '#016299',
	'4号线/大兴线': '#008E9C',
	'5号线': '#A42380',
	'6号线': '#D09900',
	'7号线': '#F2C172',
	'8号线': '#009D6A',
	'9号线': '#8FC41E',
	'10号线': '#009DBE',
	'13号线': '#F9E701',
	'14号线东段': '#D4A7A2',
	'14号线西段': '#D4A7A2',
	'15号线': '#5D2D69',
	'八通线': '#C33A32',
	'昌平线': '#DE82B1',
	'亦庄线': '#E40177',
	'房山线': '#E66021',
	'机场线': '#A29BBC',
}

// 150500:地铁站 ,150700:公交站 , 190700:地名地址
const typecode = [{
	id: '150500',
	icon: 'icon-ditie'
}, {
	id: '150700',
	icon: 'icon-gongjiao'
}, {
	id: '190700',
	icon: 'icon-gonglu'
}];

const util = {
	key:'b526b09b86cd2996e7732be8ab8c4430',
	/**
	 * 初始化高德地图api
	 */
	mapInit() {
		return new amap.AMapWX({
			key: this.key
		});
	},
	// 服务状态吗
	typecode,
	/**
	 * 获取地图颜色
	 */
	lineColor(name) {
		if (line[name]) {
			return line[name];
		} else {
			return '#ccc';
		}
	},
	/**
	 * 关键字颜色变化
	 */
	serachNmme(val, name) {
		let namestr = new RegExp(val);
		let nameresult =
			`<div style="font-size: 14px;color: #333;line-height: 1.5;">
		    ${name.replace(namestr, "<span style='color:#66ccff;'>" + val + '</span>')}
		    </div>`
			.trim();

		return nameresult;
	},
	/**
	 *  地址转地铁线路
	 */
	addressToLine(address, type) {
		let addr = address.split(';');
		let dt = '';
		addr.forEach(elm => {
			let color = '#cccccc';
			if (type === typecode[0].id) {
				color = this.lineColor(elm)
			} else if (type === typecode[1].id) {
				color = '#4075cb'
			}
			let style = 'margin:5px 0;margin-right:5px;padding:0 5px;background:' + color +
				';font-size:12px;color:#fff;border-radius:3px;';
			dt += `<div style=\'${style}\'>${elm}</div>`;

		});
		return `<div style="display:flex;flex-wrap: wrap;">${dt}</div>`;
	},
	/**
	 * 数据处理
	 */
	dataHandle(item, val) {
		// 改变字体颜色
		if (val) {
			item.nameNodes = util.serachNmme(val, item.name);
		} else {
			item.nameNodes = `<div style="font-size: 14px;color: #333;line-height: 1.5;">${item.name}</div>`;

		}
		// 地址解析 地铁
		/* if (
			item.typecode === util.typecode[0].id ||
			item.typecode === util.typecode[1].id
		) {
			item.addressNodes = util.addressToLine(item.address, item.typecode);
			if (item.typecode === util.typecode[0].id) {
				item.icon = util.typecode[0].icon;
			} else if (item.typecode === util.typecode[1].id) {
				item.icon = util.typecode[1].icon;
			}
		} else {
			item.addressNodes = `<span>${item.district}${
				item.address.length > 0 ? '·' + item.address : ''
			}</span>`.trim();
			item.icon = 'icon-weizhi';
		}

		if (item.location && item.location.length === 0) {
			item.icon = 'icon-sousuo';
		} */
        item.icon = 'icon-weizhi'
		return item;
	},
	/**
	 * 存储历史数据
	 * val [string | object]需要存储的内容
	 */
	setHistory(val) {
		let searchHistory = uni.getStorageSync('search:history');
		if (!searchHistory) searchHistory = [];
		let serachData = {};
		if (typeof(val) === 'string') {
			serachData = {
				adcode: [],
				address: [],
				city: [],
				district: [],
				id: [],
				location: [],
				name: val,
				typecode: []
			};
		} else {
			serachData = val
		}

		// 判断数组是否存在，如果存在，那么将放到最前面
		for (var i = 0; i < searchHistory.length; i++) {
			if (searchHistory[i].name === serachData.name) {
				searchHistory.splice(i, 1);
				break;
			}
		}

		searchHistory.unshift(util.dataHandle(serachData));
		uni.setStorage({
			key: 'search:history',
			data: searchHistory,
			success: function() {
			}
		});
	},
	getHistory() {

	},
	removeHistory() {
		uni.removeStorage({
			key: 'search:history',
			success: function(res) {
			}
		});
		return []
	}

}

export default util;
