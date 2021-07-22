create table outbox_msg (id int8 not null, finish_time timestamp, headers hstore, payload text, properties hstore,
                         timestamp timestamp not null default (now() at time zone 'utc'), primary key (id));

create table task (id bigserial not null, acceptable_delay int8, created_at timestamp not null default (now() at time zone 'utc'),
                   headers hstore, origin_id varchar(255) not null, payload text, period varchar(255) not null,
                   properties hstore, scheduled_at timestamp not null, primary key (id));

create sequence outbox_message_seq start 1 increment 10;
