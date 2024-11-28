[@b.head/]
[@b.toolbar title="签名"]
  bar.addBack();
[/@]
[@b.form action="!save" theme="list" name="signForm"]
  [@b.field label="签名文档"]
    1. 承诺书 2.任务书 3.反抄袭检测情况记录表
  [/@]
  [#if signature??]
    [@b.field label="已有签名"]
      <img style="width:300px;height:100px" src="${signature}"/>
    [/@]
  [/#if]
  [@b.esign id="signature" name="signature" label="签名" required="false" theme="sign"/]
  [@b.formfoot]
    <button id="process_btn" class="btn btn-outline-primary btn-sm" onclick="return processSign(this.form)">提交</button>
  [/@]
[/@]
<script>
  function processSign(){
    document.getElementById("process_btn").innerHTML="正在生成签名..."
    sign.generate().then(function(res){
      document.getElementById('signature').value = res;
      bg.form.submit(document.signForm);
      return false;
    }).catch(function(err){alert(err)});
    document.getElementById("process_btn").innerHTML="提交"
    return false;
  }
</script>
[@b.foot/]
