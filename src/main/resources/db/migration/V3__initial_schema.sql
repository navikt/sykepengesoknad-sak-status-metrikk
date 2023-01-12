CREATE TABLE sykepengesoknad_id
(
    sykepengesoknad_uuid  VARCHAR NOT NULL,
    sykepengesoknad_at_id VARCHAR NOT NULL UNIQUE,
    PRIMARY KEY (sykepengesoknad_uuid, sykepengesoknad_at_id)
);

CREATE TABLE sykepengesoknad_vedtaksperiode
(
    sykepengesoknad_at_id VARCHAR NOT NULL PRIMARY KEY,
    vedtaksperiode_id     VARCHAR NOT NULL
);


CREATE TABLE vedtaksperiode_tilstand
(
    id                VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    vedtaksperiode_id VARCHAR                  NOT NULL,
    tilstand          VARCHAR                  NOT NULL,
    tidspunkt         TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE vedtaksperiode_forkastet
(
    vedtaksperiode_id VARCHAR NOT NULL PRIMARY KEY
);

CREATE TABLE vedtaksperiode_funksjonell_feil
(
    id                VARCHAR DEFAULT uuid_generate_v4() PRIMARY KEY,
    vedtaksperiode_id VARCHAR                  NOT NULL,
    melding           VARCHAR                  NOT NULL,
    tidspunkt         TIMESTAMP WITH TIME ZONE NOT NULL
);
