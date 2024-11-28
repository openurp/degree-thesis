[@b.head/]
[@b.toolbar title="签名信息"]
  bar.addBack();
[/@]
[@b.messages slash="3"/]
<div class="container">
  [#if signature??]
    <img style="width:300px;height:100px" src="${signature}"/>
    [@b.a href="sign!editNew" class="btn btn-sm btn-outline-primary"]更新签名[/@]
    [@b.a href="sign!remove" class="btn btn-sm btn-outline-danger"]删除签名[/@]
  [#else]
    [@b.a href="sign!editNew" class="btn btn-sm btn-outline-primary"]开始签名[/@]
  [/#if]

  <div id="qrcode" style="width:200px"></div>
  <div id="tips" style="display:none">手机签名，打开微信扫一扫。</div>
  <script>
   var onmobile=("ontouchstart" in window);
   if(!onmobile){

     function generateQr(){
       new QRCode(document.getElementById("qrcode"), "${EmsBase}${b.url('!editNew')}");
     };
     var qrcode_url='${b.static_url("qrcodejs","qrcode.js")}'
     bg.require(qrcode_url,generateQr)
     document.getElementById("tips").style.display=""
   }
  </script>
</div>

[@b.foot/]
