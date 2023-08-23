export default class BuddleMap {
    
    constructor(param, view) {
        this.param = param
        this.view = view
        this.widgets = [
            {
                labels: ['地区', '维度'],        
                value: this.view.xaxis,
                dragMove: this.onMove,
                dragAdd: this.addXaxis,
                dragUpdate: this.calcData,
                solt: 'dimension-item'
            },{
                labels: ['数据', '指标'],        
                value: this.view.yaxis,
                dragMove: this.onMove,
                dragAdd: this.addYaxis,
                dragUpdate: this.calcData,
                solt: 'quota-item'
            }    
        ]
    }

    
    calcData(cache) {

    }

    onMove(e) {

    }

    addXaxis(e) {

    }

    addYaxis(e) {

    }

    




}