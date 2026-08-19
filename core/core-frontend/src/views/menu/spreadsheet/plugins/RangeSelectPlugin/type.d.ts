export declare interface IRangeSelectResult {
    startRowNumber: number
    startColumnNumber: number
    endRowNumber: number
    endColumnNumber: number
    sheetId: string
    sheetName: string
    range: string
    fullRange: string
}

export declare interface IRangeSelectDialogParams {
    callback: (result: IRangeSelectResult) => void
    onClose?: () => void
}
