[@b.head/]
    <div class="esign-box">
      <canvas id="sign_area" class="esignature" style="width:800px;height:300px;" ></canvas>
    </div>
    <div>
      <button onclick="sign.reset()" class="btn btn-sm btn-outline-primary">清空</button>
      <button onclick="handleGenerate()" >生成图片</button>
    </div>
    <div class="esigh-result">
      <img id="resultImg" alt=""/>
    </div>
<style>

.esignature {
  margin: 10px 0;
  border: 2px solid #ccc;
}
</style>

<script>
  function Esign(canvas){
    this.canvas=canvas;
    this.width = 800;
    this.height = 300;
    this.lineWidth = 4;
    this.lineColor = '#000000';
    this.bgColor = '';
    this.isCrop = false; //是否裁剪
    this.isClearBgColor = true;
    this.format = 'image/png';
    this.quality = 1;
    //data
    this.hasDrew = false;
    this.resultImg = '';
    this.points = [];
    this.canvasTxt = null;
    this.startX = 0;
    this.startY = 0;
    this.isDrawing = false;
    this.sratio = 1;

    this.ratio = function() {
      return this.height / this.width
    }
    this.myBg= function () {
      return this.bgColor ? this.bgColor : 'rgba(255, 255, 255, 0)'
    }
    this.stageInfo = function() {
      return this.canvas.getBoundingClientRect()
    }
    this.resizeHandler = function(){
      this.canvas.style.width = this.width + "px"
      var realw = parseFloat(window.getComputedStyle(canvas).width);
      this.canvas.style.height = this.ratio() * realw + "px";
      this.canvasTxt = this.canvas.getContext('2d');
      this.canvasTxt.scale(1 * this.sratio, 1 * this.sratio);
      this.sratio = realw / this.width;
      this.canvasTxt.scale(1 / this.sratio, 1 / this.sratio);
    }
    //mounting
    this.mounted = function(){
      this.canvas.height = this.height;
      this.canvas.width = this.width;
      this.canvas.style.background = this.myBg();
      var t = this;
      document.addEventListener("mouseup", function(){
        t.isDrawing = false;
      });
      //for pc
      this.canvas.addEventListener("mousedown",function(e){
        e.preventDefault();
        t.isDrawing = true
        t.hasDrew = true
        t.drawStart({x: e.offsetX,y: e.offsetY })
      });
      this.canvas.addEventListener("mousemove",function (e) {
        e.preventDefault();
        if (t.isDrawing) {
          t.drawMove({x: e.offsetX,y: e.offsetY })
        }
      });
      this.canvas.addEventListener("mouseup",function (e) {
        e.preventDefault();
        t.drawEnd({x: e.offsetX,y: e.offsetY })
        t.isDrawing = false;
      });
      //for mobile
      this.canvas.addEventListener("touchstart",function(e) {
        e.preventDefault();
        t.hasDrew = true;
        if (e.touches.length === 1) {
          var loc = {
            x: e.targetTouches[0].clientX - this.canvas.getBoundingClientRect().left,
            y: e.targetTouches[0].clientY - this.canvas.getBoundingClientRect().top
          }
          t.drawStart(loc);
        }
      });
      this.canvas.addEventListener("touchmove",function (e) {
        e.preventDefault();
        if (e.touches.length === 1) {
          var loc = {
            x: e.targetTouches[0].clientX - this.canvas.getBoundingClientRect().left,
            y: e.targetTouches[0].clientY - this.canvas.getBoundingClientRect().top
          }
          t.drawMove(loc);
        }
      });
      this.canvas.addEventListener("touchend", function (e) {
        e.preventDefault();
        if (e.touches.length === 1) {
          var loc = {
            x: e.targetTouches[0].clientX - this.canvas.getBoundingClientRect().left,
            y: e.targetTouches[0].clientY - this.canvas.getBoundingClientRect().top
          }
          t.drawEnd(loc);
        }
      });
      this.resizeHandler();
    }
    // 绘制
    this.drawStart = function(loc) {
      this.startX = loc.x;
      this.startY = loc.y;
      this.canvasTxt.beginPath();
      this.canvasTxt.moveTo(this.startX, this.startY);
      this.canvasTxt.lineTo(loc.x, loc.y);
      this.canvasTxt.lineCap = 'round';
      this.canvasTxt.lineJoin = 'round';
      this.canvasTxt.lineWidth = this.lineWidth * this.sratio;
      this.canvasTxt.stroke();
      this.canvasTxt.closePath();
      this.points.push(loc);
    }

    this.drawMove = function (loc) {
      this.canvasTxt.beginPath();
      this.canvasTxt.moveTo(this.startX, this.startY);
      this.canvasTxt.lineTo(loc.x, loc.y);
      this.canvasTxt.strokeStyle = this.lineColor;
      this.canvasTxt.lineWidth = this.lineWidth * this.sratio
      this.canvasTxt.lineCap = 'round';
      this.canvasTxt.lineJoin = 'round';
      this.canvasTxt.stroke();
      this.canvasTxt.closePath();
      this.startY = loc.y;
      this.startX = loc.x;
      this.points.push(loc);
    }
    this.drawEnd = function (loc) {
      this.canvasTxt.beginPath();
      this.canvasTxt.moveTo(this.startX, this.startY);
      this.canvasTxt.lineTo(loc.x, loc.y);
      this.canvasTxt.lineCap = 'round';
      this.canvasTxt.lineJoin = 'round';
      this.canvasTxt.stroke();
      this.canvasTxt.closePath();
      this.points.push(loc);
      this.points.push({x: -1, y: -1})
    }
    this.reset = function () {
      this.canvasTxt.clearRect(0, 0, this.canvas.width, this.canvas.height)
      if (this.isClearBgColor) {
        this.canvas.style.background = 'rgba(255, 255, 255, 0)'
      }
      this.points = [];
      this.hasDrew = false;
      this.resultImg = '';
    }
    function getCropArea(esign,imgData) {
      var topX = esign.canvas.width; var btmX = 0; var topY = esign.canvas.height; var btnY = 0;
      for (var i = 0; i < esign.canvas.canvas.width; i++) {
        for (var j = 0; j < esign.canvas.canvas.height; j++) {
          var pos = (i + esign.canvas.canvas.width * j) * 4
          if (imgData[pos] > 0 || imgData[pos + 1] > 0 || imgData[pos + 2] || imgData[pos + 3] > 0) {
            btnY = Math.max(j, btnY);
            btmX = Math.max(i, btmX);
            topY = Math.min(j, topY);
            topX = Math.min(i, topX);
          }
        }
      }
      topX++;
      btmX++;
      topY++;
      btnY++;
      return [topX, topY, btmX, btnY];
    }
    this.generate = function(options) {
      var imgFormat = options && options.format ? options.format: this.format;
      var imgQuality = options && options.quality ? options.quality: this.quality;
      var esign = this;
      return new Promise(function(resolve, reject) {
        if (!esign.hasDrew) {
          reject("Warning: Not Signned!");
          return;
        }
        var resImgData = esign.canvasTxt.getImageData(0, 0, esign.canvas.width, esign.canvas.height);
        esign.canvasTxt.globalCompositeOperation = "destination-over";
        esign.canvasTxt.fillStyle = esign.myBg();
        esign.canvasTxt.fillRect(0,0,esign.canvas.width ,esign.canvas.height);
        esign.resultImg = esign.canvas.toDataURL(imgFormat, imgQuality);
        var resultImg = esign.resultImg;
        esign.canvasTxt.clearRect(0, 0, esign.canvas.width ,esign.canvas.height);
        esign.canvasTxt.putImageData(resImgData, 0, 0);
        esign.canvasTxt.globalCompositeOperation = "source-over";
        if (esign.isCrop) {
          var crop_area = getCropArea(esign,resImgData.data);
          var crop_canvas = document.createElement('canvas');
          var crop_ctx = crop_canvas.getContext('2d');
          crop_canvas.width = crop_area[2] - crop_area[0];
          crop_canvas.height = crop_area[3] - crop_area[1];
          var crop_imgData = esign.canvasTxt.getImageData(...crop_area);
          crop_ctx.globalCompositeOperation = "destination-over"
          crop_ctx.putImageData(crop_imgData, 0, 0);
          crop_ctx.fillStyle = esign.myBg();
          crop_ctx.fillRect(0, 0, crop_canvas.width , crop_canvas.height);
          resultImg = crop_canvas.toDataURL(imgFormat, imgQuality);
          crop_canvas = null;
        }
        resolve(resultImg);
      });
    }
    this.mounted();

  }
  var sign = new Esign(document.getElementById("sign_area"));
  function handleGenerate(){
    sign.generate().then(function(res){ document.getElementById('resultImg').src = res}).catch(function(err){alert(err)});
  }
</script>
[@b.foot/]
