[@b.head/]
[@b.form action="!save" theme="list" name="signForm"]
  [@b.field label="签名文档"]
    1. 开题报告 2. 指导教师评分表
  [/@]
  [@b.field label="指导学生"]
    [#list writers as writer]${writer.name}[#sep]&nbsp;[/#list]
  [/@]
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
