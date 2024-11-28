--user--------------
create table thesis.yh (id bigint,dlm varchar(20),lx int4,xm varchar(50),xy_id bigint,
					   zy_id bigint,sfjxfyz bool,xt bool,sfjyszr bool,sfdbzz bool);
insert into thesis.yh(id,code,lx,name,department_id,sfjxfyz,xt,sfjyszr,sfdbzz)
select xs.id,xs.xh,0,xm,zy.xy_id,false,false,false,false from bysj.xs xs;

insert into thesis.yh(id,code,lx,name,department_id,sfjxfyz,xt,sfjyszr,sfdbzz)
select id+1000000,jsbh,1,xm,xy_id,false,false,false,false from bysj.js;

insert into thesis.yh(id,code,lx,name,department_id,sfjxfyz,xt,sfjyszr,sfdbzz)
select id+2000000,dlm,2,xm,xy_id,false,false,false,false from bysj.FDY;

insert into thesis.yh(id,code,lx,name,department_id,sfjxfyz,xt,sfjyszr,sfdbzz)
select id+3000000,dlm,3,xm,xy_id,false,false,false,false from bysj.jxms;

insert into thesis.yh(id,code,lx,name,department_id,sfjxfyz,xt,sfjyszr,sfdbzz)
select id+4000000,dlm,4,xm,xy_id,false,false,false,false from bysj.DDY;

insert into thesis.yh(id,dlm,lx,xm,sfjxfyz,xt,sfjyszr,sfdbzz)
select id+5000000,dlm,5,xm,false,false,false,false from bysj.XXGLY;
update thesis.yh set sfjxfyz =false,sfdbzz =false,sfjyszr=false;
-------------------plan-------------
create table thesis.plan(id bigint,department_id int4,audit_status int4,audit_opinion varchar(300));
insert into thesis.plan(id,department_id,audit_status,audit_opinion) select id,xy_id,zt,scyj from bysj.bysjgzjh;
create table thesis.stagetime(id bigint,plan_id bigint,stage int4,beginon date,endon date);
insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,1,tmtjbegin,tmtjend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,2,tmscbegin,tmscend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,11,tmcxbegin,tmcxend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,12,tmbxbegin,tmbxend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,13,jsrwsBegin,jsrwsend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,14,ktbgBegin,ktbgend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,16,jszd1Begin,jszd1end from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,17,zqjcBegin,zqjcend from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,18,jszd2Begin,jszd2end from bysj.bysjgzjh;

insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,20,zqjcend+'1 day',lwtjEnd from bysj.bysjgzjh;
insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,21,lwtjEnd+'1 day',lwpyEnd from bysj.bysjgzjh;
insert into thesis.stagetime(id,plan_id,stage,beginon,endon)
select datetime_id(),id,22,lwpyEnd+'1 day',dbEnd from bysj.bysjgzjh;
----------------writer---------------------
alter table bysj.xs add advisor_id bigint;
alter table bysj.xs add subject_id bigint;
update bysj.xs set subject_id =(select zzkt_id from thesis.ktxz where ktxz.xs_id=xs.id);
update bysj.xs set advisor_id =(select js_id from thesis.kt where kt.id=xs.subject_id);
name varchar(255) not null, l

insert into thesis.writer(id,code,name,department_id,major_id,squad,advisor_id,subject_id,email,mobile)
select id,xh,xm,zy.xy_id,zy.id,bj,advisor_id,subject_id,yx,sj from xy_idbysj.xs;
------------------notice-------------------
alter table bysj.tz   rename collegeable to collegeable1;
alter table bysj.tz add collegeable bool;
update bysj.tz set collegeable= case when collegeable1>0 then true else false end;
alter table bysj.tz drop column collegeable1;

alter table bysj.tz   rename teacherable to teacherable1;
alter table bysj.tz add teacherable bool;
update bysj.tz set teacherable= case when teacherable1>0 then true else false end;
alter table bysj.tz drop column teacherable1;

alter table bysj.tz   rename studentable to studentable1;
alter table bysj.tz add studentable bool;
update bysj.tz set studentable= case when studentable1>0 then true else false end;
alter table bysj.tz drop column studentable1;
------------------------------------------------
alter table bysj.js add user_id bigint;
update bysj.js j set user_id=(select u.id from base.users u where u.code=j.jsbh);
update  bysj.js set sfjxfyz=0 where sfjxfyz is null



alter table thesis.kt add depart_id bigint;
update thesis.kt set depart_id=(select js.xy_id from bysj.js js where js.id=kt.js_id);



update bysj.js j set user_id=(select u.id from base.users u where u.code=j.jsbh) where user_id is null;
update thesis.kt set depart_id=(select js.xy_id from bysj.js js where js.id=kt.js_id) where depart_id is null;
------------------------
create table thesis.kt_zy (subject_id bigint,major_id bigint, constraint pk_kt_zy primary key(kt_id,zy_id));

insert into thesis.kt_zy(subject_id,major_id)
select kt.id, cast(regexp_split_to_table(mxzy, ',') as integer) from thesis.kt;
alter table thesis.kt alter column mxzy drop not null;
-------------update xs depart-----------
alter table bysj.xs add xy_id bigint;
update bysj.xs set xy_id=(select m.xy_id from thesis.zy m where m.id=xs.zy_id);
-------------deadline ---------------------
create table thesis.deadline(id bigint,writer_id bigint,submitat timestamp,updatedat timestamp,endat timestamp,stage int4,delaycount int4);
insert into thesis.deadline(id,writer_id,updatedat,endat,stage,delaycount)
select datetime_id(),kz.xs_id,now(),rwssj+ interval '3' day,13,rwsyqcs from thesis.gnkz kz left join thesis.yqjl jl on kz.xs_id=jl.xs_id;

update thesis.deadline set submitat=(select rws.qrsj from thesis.rws rws where rws.xs_id=writer_id) where stage=13;

insert into thesis.deadline(id,writer_id,updatedat,endat,stage,delaycount)
select datetime_id(),kz.xs_id,now(),ktbgsj+ interval '3' day,14,ktbgyqcs from thesis.gnkz kz left join thesis.yqjl jl on kz.xs_id=jl.xs_id;

insert into thesis.deadline(id,writer_id,updatedat,endat,stage,delaycount)
select datetime_id(),kz.xs_id,now(),zqjcsj+ interval '3' day,17,zqjcyqcs from thesis.gnkz kz left join thesis.yqjl jl on kz.xs_id=jl.xs_id;

insert into thesis.deadline(id,writer_id,updatedat,endat,stage,delaycount)
select datetime_id(),kz.xs_id,now(),lwsj+ interval '3' day,20,lwyqcs from thesis.gnkz kz left join thesis.yqjl jl on kz.xs_id=jl.xs_id;
-----------rws---------------------
alter table thesis.rws   rename qr to qr1;
alter table thesis.rws add qr bool;
update thesis.rws set qr= case when qr1>0 then true else false end;
alter table thesis.rws drop column qr1;
--------------zdjl--------------
alter table bysj.zdjl add stage int4;
alter table bysj.zdjl add idx smallint;
update bysj.zdjl set idx=1 ,stage=case when flag =1 then 16 else 18 end;
update bysj.zdjl a set idx=2 where exists(select * from bysj.zdjl b where a.xs_id=b.xs_id and a.flag=b.flag and a.id >b.id);
alter table bysj.zdjl alter column flag drop not null;
---------------midcheck----------------
insert into thesis.midterm_check_items(id,code,name)values(1,'1','任务书执行情况');
insert into thesis.midterm_check_items(id,code,name)values(2,'2','开题报告');
insert into thesis.midterm_check_items(id,code,name)values(3,'3','时间进程合理性');
insert into thesis.midterm_check_items(id,code,name)values(4,'4','指导教师指导情况');
insert into thesis.midterm_check_items(id,code,name)values(5,'5','学生学习态度');
insert into thesis.midterm_check_items(id,code,name)values(6,'6','学生接受论文(设计)指导情况');
insert into thesis.midterm_check_items(id,code,name)values(7,'7','资料收集');

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),1,id,rws,rwsbz from bysj.zqjc where rws is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),2,id,ktbg,ktbgbz from bysj.zqjc where ktbg is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),3,id,sjjc,sjjcbz from bysj.zqjc where sjjc is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),4,id,jszd,jszdbz from bysj.zqjc where jszd is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),5,id,xxtd,xxtdbz from bysj.zqjc where xxtd is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),6,id,jslwzd,jslwzdbz from bysj.zqjc where jslwzd is not null;

insert into thesis.midterm_check_details(id,item_id,check_id,audit_status,audit_opinion)
select datetime_id(),7,id,zlsj,zlsjbz from bysj.zqjc where zlsj is not null;
----------------------------

