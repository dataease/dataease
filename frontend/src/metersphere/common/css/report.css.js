const ReportStyle = `.body{
  font-size: 14px;
  color: #7B0274;
}
.container {
  height: 100vh;
  background: #F5F5F5;
}
.preview {
  position: relative;
}
/* <-- 表格拖拽表头调整宽度，在 t-bable 上添加 border 属性，并添加 adjust-table 类名*/
.adjust-table td {
  border-right-color: white;
}

.adjust-table th {
  border-right-color: white;
}

.adjust-table {
  border-color: white;
}

.adjust-table:after {
  background-color: white;
}

.adjust-table th:hover:after {
  content: '';
  position: absolute;
  top: 25%;
  right: 0;
  height: 50%;
  width: 3px;
  background-color: #EBEEF5;
}
span {
    margin-right: 5px;
    display: inline-block;
  }

  .el-col span:first-child {
    font-weight: bold;
    width: 100px;
  }

  .el-row {
    height: 60px;
  }

  .select-time span {
    display: inline-block;
  }

  .el-date-editor {
    width: 150px;
  }
`

export default ReportStyle;
