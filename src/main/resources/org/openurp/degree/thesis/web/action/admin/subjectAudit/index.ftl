[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  [@b.nav class="nav-tabs nav-tabs-compact"]
    [@b.navitem href="!search?status="+Submited.id target="subjectList"]待审查[/@]
    [@b.navitem href="!search?status="+Rejected.id target="subjectList"]审查未通过[/@]
    [@b.navitem href="!search?status="+Passed.id target="subjectList"]审查通过[/@]
  [/@]
  [@b.div href="!search?status="+Submited.id id="subjectList"/]
[@b.foot/]