
CREATE TABLE IF NOT EXISTS "t_preference"(
	"name" VARCHAR NOT NULL,
	"value" VARCHAR NULL,
	CONSTRAINT "pk_preference" PRIMARY KEY("name")
);

CREATE TABLE IF NOT EXISTS "t_build"(
	"name" VARCHAR NOT NULL,
	"project" VARCHAR NOT NULL,
	"target" VARCHAR NOT NULL,
	"region" VARCHAR NOT NULL,
	"invoked" INTEGER NOT NULL,
	"succeed" INTEGER NOT NULL,
	"aborted" INTEGER NOT NULL,
	"completed" BOOLEAN NOT NULL,
	"user" VARCHAR NULL,
	"comment" VARCHAR NOT NULL,
	"timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "pk_build" PRIMARY KEY("name")
);

CREATE TABLE IF NOT EXISTS "t_build_server"(
	"build" VARCHAR NOT NULL,
	"name" VARCHAR NOT NULL,
	"status" INTEGER NOT NULL,
	"output" VARCHAR NULL,
	"timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "pk_build_server" PRIMARY KEY("build", "name"),
	CONSTRAINT "fk_build_server__build" FOREIGN KEY("build") REFERENCES "t_build"("name")
);
