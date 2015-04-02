
CREATE TABLE IF NOT EXISTS "t_preference"(
	"key" VARCHAR NOT NULL,
	"value" VARCHAR NULL,
	CONSTRAINT "pk_preference" PRIMARY KEY("key")
);

CREATE TABLE IF NOT EXISTS "t_build"(
	"build" VARCHAR NOT NULL,
	"project" VARCHAR NOT NULL,
	"target" VARCHAR NOT NULL,
	"region" VARCHAR NOT NULL,
	"invoked" INTEGER NOT NULL,
	"succeed" INTEGER NOT NULL,
	"aborted" INTEGER NOT NULL,
	"user" VARCHAR NULL,
	"timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "pk_build" PRIMARY KEY("build")
);

CREATE TABLE IF NOT EXISTS "t_build_server"(
	"build" VARCHAR NOT NULL,
	"server" VARCHAR NOT NULL,
	"status" INTEGER NOT NULL,
	"output" VARCHAR NULL,
	"timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "pk_build_server" PRIMARY KEY("build", "server"),
	CONSTRAINT "fk_build_server__build" FOREIGN KEY("build") REFERENCES "t_build"("build")
);
