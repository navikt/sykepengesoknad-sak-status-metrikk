package no.nav.helse.flex.repository

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface SykepengesoknadIdRepository : CrudRepository<SykepengesoknadIdDbRecord, String> {

    @Modifying
    @Query(
        """
        INSERT INTO sykepengesoknad_id(sykepengesoknad_uuid, sykepengesoknad_at_id)
        VALUES (:sykepengesoknadUuid, :sykepengesoknadAtId)
        ON CONFLICT DO NOTHING
        """
    )
    fun insert(sykepengesoknadUuid: String, sykepengesoknadAtId: String)
}

@Table("sykepengesoknad_id")
data class SykepengesoknadIdDbRecord(
    val sykepengesoknadUuid: String,
    val sykepengesoknadAtId: String,
)

@Repository
interface SykepengesoknadVedtaksperiodeRepository : CrudRepository<SykepengesoknadVedtaksperiodeDbRecord, String> {

    @Modifying
    @Query(
        """
        INSERT INTO sykepengesoknad_vedtaksperiode(sykepengesoknad_at_id, vedtaksperiode_id)
        VALUES (:sykepengesoknadAtId, :vedtaksperiodeId)
        ON CONFLICT DO NOTHING
        """
    )
    fun insert(sykepengesoknadAtId: String, vedtaksperiodeId: String)
}

@Table("sykepengesoknad_vedtaksperiode")
data class SykepengesoknadVedtaksperiodeDbRecord(
    val sykepengesoknadAtId: String,
    val vedtaksperiodeId: String,
)

@Repository
interface VedtaksperiodeTilstandRepository : CrudRepository<VedtaksperiodeTilstandDbRecord, String>

@Table("vedtaksperiode_tilstand")
data class VedtaksperiodeTilstandDbRecord(
    @Id
    val id: String? = null,
    val vedtaksperiodeId: String,
    val tilstand: String,
    val tidspunkt: Instant
)
