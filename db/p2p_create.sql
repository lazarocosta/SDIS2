DROP SCHEMA IF EXISTS p2p CASCADE; 
CREATE SCHEMA p2p; 
SET SCHEMA 'p2p';


CREATE TABLE users (
	user_id		serial PRIMARY KEY,
	email		text NOT NULL UNIQUE,
	password	text NOT NULL,
	last_time	timestamp DEFAULT current_timestamp,
	last_ip		inet ,
	last_port	integer
);

CREATE TABLE files (
	file_id		serial PRIMARY KEY,
	user_id		integer REFERENCES users(user_id),
	name		text NOT NULL,
	size		integer NOT NULL,
	added_time	timestamp DEFAULT current_timestamp,
	public		boolean	DEFAULT false
);

CREATE TABLE deleted_files (
	file_id		integer PRIMARY KEY
);