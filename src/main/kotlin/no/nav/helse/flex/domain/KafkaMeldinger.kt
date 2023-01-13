package no.nav.helse.flex.domain

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.flex.objectMapper
import java.time.LocalDateTime

data class MeldingMedEventName(
    @JsonProperty("@event_name")
    val eventName: String,
)

fun String.hentEventName(): String {
    return objectMapper.readValue(this, MeldingMedEventName::class.java).eventName
}

data class SøknadMedId(
    @JsonProperty("id")
    val sykepengesoknadUuid: String,
    @JsonProperty("@id")
    val sykepengesoknadAtId: String,
)

fun String.tilSøknadMedId(): SøknadMedId {
    return objectMapper.readValue(this, SøknadMedId::class.java)
}

data class VedtaksperiodeEndretEvent(
    val vedtaksperiodeId: String,
    val gjeldendeTilstand: String,
    val hendelser: List<String>? = null,
    @JsonProperty("@opprettet")
    val opprettet: LocalDateTime
)

fun String.tilVedtaksperiodeEndretEvent(): VedtaksperiodeEndretEvent {
    return objectMapper.readValue(this, VedtaksperiodeEndretEvent::class.java)
}

data class VedtaksperiodeForkastetEvent(
    val vedtaksperiodeId: String,
    val hendelser: List<String>? = null,
)

fun String.tilVedtaksperiodeForkastetEvent(): VedtaksperiodeForkastetEvent {
    return objectMapper.readValue(this, VedtaksperiodeForkastetEvent::class.java)
}

data class AktivitetsloggNyAktivitetEvent(
    val aktiviteter: ArrayList<Aktiviteter> = arrayListOf(),
    @JsonProperty("@opprettet")
    val opprettet: LocalDateTime
)

data class Aktiviteter(
    val nivå: String? = null,
    val melding: String? = null,
    val kontekster: List<Kontekster>? = null,
)

data class Kontekster(
    val kontekstmap: Map<String, String>? = null
)

fun String.tilAktivitetsloggNyAktivitetEvent(): AktivitetsloggNyAktivitetEvent {
    return objectMapper.readValue(this, AktivitetsloggNyAktivitetEvent::class.java)
}
