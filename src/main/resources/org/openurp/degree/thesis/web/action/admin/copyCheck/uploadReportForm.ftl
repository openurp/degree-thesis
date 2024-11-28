[@b.head "论文检测报告上传"/]
  <div class="container" style="width:600px">
    <div>
      <h1>论文检测报告 <small>上传</small></h1>
    </div>
[@b.form action="!uploadReport"  enctype="multipart/form-data" class="form-inline" role="form"]
    <label for="zipfile" class="control-label">选择zip文件：</label>
    <div class="form-group">
      <input type="file" name="zipfile"  id="zipfile" class="form-control">
    </div>
    <div class="form-group">
        [@b.submit class="btn btn-primary" value="上传"/]
    </div>
[/@]
</div>
[@b.foot/]
