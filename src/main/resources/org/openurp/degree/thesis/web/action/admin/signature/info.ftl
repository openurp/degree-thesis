[@b.toolbar title="签名信息"]
  bar.addBack();
[/@]

<table class="table">
  <tr>
    <th>学生/教师</th>
    <td>签名</td>
  </tr>
  <tr>
    <td>${signature.writer.std.code} ${signature.writer.std.name}</td>
    <td>
      [#if writer_signature??]
      <img src="${writer_signature}" height="80px"/>
      [#else]
      没有签名
      [/#if]
    </td>
  </tr>
  <tr>
    <td>${(signature.writer.advisor.code)!} ${(signature.writer.advisor.name)!}</td>
    <td>
      [#if advisor_signature??]
      <img src="${advisor_signature}" height="80px"/>
      [#else]
      没有签名
      [/#if]
    </td>
  </tr>
</table>
  [@b.form action="!uploadAdvisorSignature" theme="list"]
    [@b.field label="提示"]该签名会更新导师指导的所有本届学生的导师签名[/@]
    [@b.file label="指导老师签名图片" name="signature_file" extensions="png" required="true"/]
    [@b.formfoot]
      <input type="hidden" name="advisor.id" value="${signature.writer.advisor.id}"/>
      <input type="hidden" name="signature.writer.season.id" value="${signature.writer.season.id}"/>
      [@b.submit value="上传更新"/]
    [/@]
  [/@]
[@b.foot/]
