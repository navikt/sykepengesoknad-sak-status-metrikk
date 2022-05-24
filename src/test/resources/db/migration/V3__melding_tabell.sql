CREATE TABLE melding
(
    id           VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    melding_uuid VARCHAR                  NOT NULL UNIQUE,
    fnr          VARCHAR                  NOT NULL,
    tekst        VARCHAR                  NOT NULL,
    url          VARCHAR                  NOT NULL,
    opprettet    TIMESTAMP WITH TIME ZONE NOT NULL,
    lukket       TIMESTAMP WITH TIME ZONE
);

CREATE INDEX melding_fnr_index ON melding (fnr);
