DROP SCHEMA IF EXISTS p2p CASCADE; 
CREATE SCHEMA p2p; 
SET SCHEMA 'p2p';


CREATE TABLE users (
	user_id		serial PRIMARY KEY,
	email		text NOT NULL UNIQUE,
	password	text NOT NULL,
	last_time	timestamp DEFAULT current_date,
	last_ip		inet NOT NULL,
	last_port	integer NOT NULL
);

CREATE TABLE files (
	file_id		serial PRIMARY KEY,
	user_id		integer REFERENCES users(user_id),
	name		text NOT NULL,
	added_time	timestamp DEFAULT current_date,
	public		boolean	DEFAULT false
);

CREATE TABLE peers (
	file_id		integer PRIMARY KEY REFERENCES files(file_id),
	user_id		integer REFERENCES users(user_id)
);