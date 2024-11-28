<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <title></title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta http-equiv="pragma" content="no-cache"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <meta http-equiv="expires" content="0"/>
    <meta http-equiv="content-style-type" content="text/css"/>
    <meta http-equiv="content-script-type" content="text/javascript"/>
    <link rel="stylesheet" href="${b.static_url('pdfjs','web/viewer.css')}">
    <link rel="resource" type="application/l10n" href="${b.static_url('pdfjs','web/locale/locale.properties')}">
  </head>
<body>
  <script src="${b.static_url('pdfjs','build/pdf.js')}"></script>
  <div class="wrapper" id="pdf-container" style="text-align:center">
  </div>

<script>
  PDFJS.workerSrc="${b.static_url('pdfjs','build/pdf.worker.js')}"
  PDFJS.disableStream = true;
  PDFJS.disableAutoFetch = true;
  var url = "${b.url("!report")}";

  window.onload = function () {
    function createPdfContainer(id,className) {
      var pdfContainer = document.getElementById('pdf-container');
      var canvasNew =document.createElement('canvas');
      canvasNew.id = id;
      canvasNew.className = className;
      pdfContainer.appendChild(canvasNew);
    };

    //渲染pdf
    //建议给定pdf宽度
    function renderPDF(pdf,i,id) {
      pdf.getPage(i).then(function(page) {
        var scale = 1.5;
        var viewport = page.getViewport(scale);
        var canvas = document.getElementById(id);
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        var renderContext = {
          canvasContext: context,
          viewport: viewport
        };
        page.render(renderContext);
      });
    };
    //创建和pdf页数等同的canvas数
    function createSeriesCanvas(num,template) {
      var id = '';
      for(var j = 1; j <= num; j++){
        id = template + j;
        createPdfContainer(id,'pdfClass');
      }
    }
    //读取pdf文件，并加载到页面中
    function loadPDF(fileURL) {
      PDFJS.getDocument(fileURL).then(function(pdf) {
        //用 promise 获取页面
        var id = '';
        var idTemplate = 'cw-pdf-';
        var pageNum = pdf.numPages;
        //根据页码创建画布
        createSeriesCanvas(pageNum,idTemplate);
        //将pdf渲染到画布上去
        for (var i = 1; i <= pageNum; i++) {
          id = idTemplate + i;
          renderPDF(pdf,i,id);
        }

      });
    }
    loadPDF(url);
  };
</script>
[@b.foot/]
