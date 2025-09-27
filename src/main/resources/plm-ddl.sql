create table llm_word(
	n int primary key auto_increment,
	language varchar(10) default 'korean',
	word varchar(20) not null,
	type varchar(10) not null,
	updated_date datetime default now()
);
alter table llm_word
add column memo varchar(30)
;
create table llm_word_compound(
	n int primary key auto_increment,
	word int not null unique,
	leftword int not null,
	rightword int not null,
	updated_date datetime default now(),
	foreign key (word) references llm_word(n),
	foreign key (leftword) references llm_word(n),
	foreign key (rightword) references llm_word(n)
);
create table plm_learn(
	n int primary key auto_increment,
	word int not null unique,
	src varchar(50) not null,
	updated_date datetime default now(),
	foreign key (word) references llm_word(n)
);
alter table plm_learn
add column rightword varchar(20)
;
alter table plm_learn
add column leftword varchar(20) not null default ''
;
update plm_learn l set leftword = (select w.word from llm_word w where w.n = l.word)
;
alter table plm_learn
alter column leftword drop default
;
alter table plm_learn change leftword value varchar(20)
;
create table plm_src_box(
	n int primary key auto_increment,
	src varchar(400) not null unique, -- plm_learn 도 디비버로 길이 늘려준다
	updated_date datetime default now()
);
create table plm_context(
	n int primary key auto_increment,
	leftword int not null,
	rightword int not null,
	cnt int default 0,
	updated_date datetime default now(),
	unique (leftword, rightword),
	foreign key (leftword) references llm_word(n),
	foreign key (rightword) references llm_word(n)
);
ALTER table plm_context add column space int default 0;
create table plm_understand_box(
	n int primary key auto_increment,
	src varchar(400) not null unique,
	updated_date datetime default now()
);
create table plm_understand_box_word(
    n int primary key auto_increment,
	understand int not null,
	i int not null,
	word int not null,
	updated_date datetime default now(),
	unique(understand, i),
	foreign key (understand) references plm_understand_box(n)
);
create table plm_ultron_sentence(
	n int primary key auto_increment,
	target int,
	opener int not null,
	updated_date datetime default now(),
	foreign key (target) references plm_ultron_sentence(n),
	foreign key (opener) references llm_word(n)
);
create table plm_ultron_context(
	n int primary key auto_increment,
	sentence int not null,
	context int not null,
	i int not null,
	updated_date datetime default now(),
	unique(sentence, i),
	foreign key (sentence) references plm_ultron_sentence(n),
	foreign key (context) references plm_context(n)
);