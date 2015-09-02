create database sloca;
use sloca;





create table location_lookup


(
 location_id char(10) not null,
 

semantic_place varchar(25),
CONSTRAINT pk_location_lookup PRIMARY KEY (location_id)

)
;






create table demographics


(
 mac_address char(40) not null,
 

name varchar(100),
 

password varchar(50),
 

email varchar(100),
 

gender char(1),
 

CONSTRAINT pk_demographics PRIMARY KEY (mac_address)
)
;






create table location


(
 time_stamp TIMESTAMP,
 

mac_address char(40),
 

location_id char(10),
 

CONSTRAINT pk_location PRIMARY KEY (mac_address, time_stamp),
 

CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES location_lookup(location_id)
)
;