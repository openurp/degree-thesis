[@b.head/]
  <div class="container-fluid">
  [@b.toolbar title="上传论文归档材料"]
    bar.addBack();
  [/@]
  <style>
    fieldset.listset li > label.title{
      min-width: 10rem;
    }
  </style>
  [#if (archive.confirmed!false)]<div class="alert alert-warning">材料已经确认，无需上传</div>[/#if]
  [@b.form name="form" action="!upload" theme="list"]
    [@b.field label="上传提示"] 请核对上传资料的PDF中的题目、指导老师是否和实际相吻合。[/@]
    [@b.field label="学号姓名"] ${writer.std.code} ${writer.std.name}[/@]
    [@b.field label="专业、方向"] ${writer.std.major.name} ${(writer.std.state.direction.name)!}[/@]
    [@b.select label="撰写语种" name="paper.language.id" items=languages value=paper.language! empty="..." required="true"/]
    [@b.textfield label="论文研究方向" onchange="detectField(this)" name="paper.researchField" value=paper.researchField! required="true" comment="不能和专业同名"/]
    [@b.textfield label="论文关键词" name="paper.keywords" value=paper.keywords! comment="关键词之间采用全角分号隔开"! style="width:400px" required="true"/]
    [#assign thesisTypes={'毕业论文':'毕业论文','毕业设计':'毕业设计'} /]
    [@b.radios label="上传论文（设计）类型"  name="paper.thesisType" value=paper.thesisType! items=thesisTypes required="true"/]
    [#list docTypes?sort_by("idx") as t]
      [#assign result][#if docs.get(t)??]<span class="text-muted">已上传:${docs.get(t).updatedAt?string("yyyy-MM-dd HH:mm")}</span>[/#if] [#if t.remark??]${t.remark!}&nbsp;&nbsp;[/#if] [/#assign]
      [#assign docRequired][#if docs.get(t)??]false[#else]true[/#if][/#assign]
      [@b.file name=t.code extensions=t.extensions maxSize=t.maxSize+"MB" label=t.name comment=result required=docRequired/]
    [/#list]
    [@b.formfoot]
      [#if !(archive.confirmed!false)]
      [@b.submit value="所有材料都已核对，提交归档资料"/]
      [/#if]
    [/@]
  [/@]
</div>
<script>
  function detectField(field){
    var majorNames=["${writer.std.state.major.name}"];
    [#if writer.std.state.direction??]
    majorNames.push("${writer.std.state.direction.name}");
    [/#if]
    var v = field.value;
    if(v){
      if(majorNames.includes(v)){
        jQuery(field).parent().find(".error").remove();
        jQuery(field).after('<label for="' + field.id + '" class="error">研究方向不能和专业同名</label>')
      }else{
        jQuery(field).parent().find(".error").remove();
      }
    }
  }
</script>
[@b.foot/]
