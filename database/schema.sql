create database if not exists swe395carmanagementsystem;
use swe395carmanagementsystem;

create table if not exists cars (
    id int auto_increment primary key,
    brand varchar(50) not null,
    model varchar(50) not null,
    year int not null,
    price double not null
