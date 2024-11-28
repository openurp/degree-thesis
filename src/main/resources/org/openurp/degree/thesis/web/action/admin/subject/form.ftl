[@b.toolbar title="修改/新建教师题目"]
  bar.addBack();
[/@]
    [@b.form name="form" action=b.rest.save(subject) theme="list"]
      [#if subject.advisor?? && subject.advisor.id?? && subject.advisor.name??]
        [@b.field label="指导教师"]${subject.advisor.name}<input name="subject.advisor.id" type="hidden" value="${subject.advisor.id}"/>[/@]
      [#else]
        [@b.select label="指导教师" name="subject.advisor.id" items=advisors value=subject.advisor! required="true"/]
      [/#if]
      [@b.textfield label="题目名称" name="subject.name" value=subject.name! style="width:400px" maxlength="150" comment="150字以内"  required="true"/]
      [@b.select label="面向院系" name="subject.depart.id" items=departs value=subject.depart! required="true"/]
      [@b.select label="面向专业" name="majorId" items=majors value=(subject.majors?first)!  required="true" multiple="true"/]
      [@b.textfield label="研究方向" name="subject.researchField" value=subject.researchField! style="width:400px" maxlength="150" comment="150字以内"  required="true"/]

      [@b.textarea label="主要内容" name="subject.contents" value=subject.contents  rows="5" cols="80"
                  maxlength="500" comment="(500字以内),包括课题的任务与主要内容、综合能力训练、工作量、题目难度和广度等"/]
      [@b.textarea label="现有条件" name="subject.conditions" value=subject.conditions  rows="5" cols="80"
                  maxlength="500" comment="(500字以内),包括资料的准备.经费落实.实验条件.场地条件等方面都进行阐述。"/]
      [@b.textarea label="主要内容" name="subject.requirements" value=subject.requirements  rows="5" cols="80"
                  maxlength="500" comment="(150字以内)"/]
      [@b.formfoot]
        [@b.reset/]
        [@b.submit value="提交"/]
      [/@]
    [/@]
