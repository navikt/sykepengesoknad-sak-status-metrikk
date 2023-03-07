DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'sykepengesoknad-sak-status-metrikk')
        THEN
            ALTER USER "sykepengesoknad-sak-status-metrikk" WITH REPLICATION;
        END IF;
    END
$$;
DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'bigquery-datastream')
        THEN
            ALTER USER "bigquery-datastream" WITH REPLICATION;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "bigquery-datastream";
            GRANT USAGE ON SCHEMA public TO "bigquery-datastream";
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO "bigquery-datastream";
        END IF;
    END
$$;