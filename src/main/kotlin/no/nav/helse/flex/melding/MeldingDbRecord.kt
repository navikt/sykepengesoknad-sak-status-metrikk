package no.nav.helse.flex.melding

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("melding")
data class MeldingDbRecord(
    @Id
    val id: String? = null,
    val meldingUuid: String,
    val fnr: String,
    val tekst: String,
    val lenke: String,
    val meldingType: String,
    val synligFremTil: Instant,
    val opprettet: Instant,
    val lukket: Instant?
)
