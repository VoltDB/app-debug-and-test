CREATE TABLE demo
(
  mynum       bigint        NOT NULL,
  othernum    bigint        NOT NULL,
  myjson      varchar(5000) NOT NULL
);
PARTITION TABLE demo ON COLUMN mynum;

-- stored procedures
CREATE PROCEDURE PARTITION ON TABLE demo COLUMN mynum FROM CLASS debugandtest.ProcA;
CREATE PROCEDURE PARTITION ON TABLE demo COLUMN mynum FROM CLASS debugandtest.ProcB;
CREATE PROCEDURE FROM CLASS debugandtest.Unpack;
CREATE PROCEDURE FROM CLASS debugandtest.BuggyProc;
