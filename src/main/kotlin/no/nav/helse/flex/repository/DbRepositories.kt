package no.nav.helse.flex.repository

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

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
