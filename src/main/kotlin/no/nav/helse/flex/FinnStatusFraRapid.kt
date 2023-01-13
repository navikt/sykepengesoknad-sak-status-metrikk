package no.nav.helse.flex

import no.nav.helse.flex.domain.hentEventName
import no.nav.helse.flex.domain.tilVedtaksperiodeForkastetEvent
import no.nav.helse.flex.domain.tilSøknadMedId
import no.nav.helse.flex.domain.tilVedtaksperiodeEndretEvent
import no.nav.helse.flex.repository.SykepengesoknadIdRepository
import no.nav.helse.flex.repository.SykepengesoknadVedtaksperiodeRepository
import no.nav.helse.flex.repository.VedtaksperiodeForkastetRepository
import no.nav.helse.flex.repository.VedtaksperiodeTilstandDbRecord
import no.nav.helse.flex.repository.VedtaksperiodeTilstandRepository
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class FinnStatusFraRapid(
    val sykepengesoknadIdRepository: SykepengesoknadIdRepository,
    val sykepengesoknadVedtaksperiodeRepository: SykepengesoknadVedtaksperiodeRepository,
    val vedtaksperiodeTilstandRepository: VedtaksperiodeTilstandRepository,
    val vedtaksperodeForkastetRepository: VedtaksperiodeForkastetRepository,
) {

    fun oppdater(value: String) {

        val eventName = value.hentEventName()

        when (eventName) {
            "sendt_søknad_nav", "sendt_søknad_arbeidsgiver" -> {
                håndterSendtSøknadEvents(value)
            }

            "vedtaksperiode_endret" -> {
                håndterVedtaksperiodeEndretEvents(value)
            }

            "vedtaksperiode_forkastet" -> {
                håndterVedtaksperiodeForkastetEvents(value)
            }
        }
    }

    private fun håndterVedtaksperiodeEndretEvents(value: String) {
        val vedtaksperiodeEndretEvent = value.tilVedtaksperiodeEndretEvent()

        vedtaksperiodeEndretEvent.hendelser.forEach {
            sykepengesoknadVedtaksperiodeRepository.insert(
                sykepengesoknadAtId = it,
                vedtaksperiodeId = vedtaksperiodeEndretEvent.vedtaksperiodeId
            )
        }

        vedtaksperiodeTilstandRepository.save(
            VedtaksperiodeTilstandDbRecord(
                vedtaksperiodeId = vedtaksperiodeEndretEvent.vedtaksperiodeId,
                tilstand = vedtaksperiodeEndretEvent.gjeldendeTilstand,
                tidspunkt = vedtaksperiodeEndretEvent.opprettet.toInstant(ZoneOffset.UTC)
            )
        )
    }

    private fun håndterSendtSøknadEvents(value: String) {
        val soknadIder = value.tilSøknadMedId()
        sykepengesoknadIdRepository.insert(
            sykepengesoknadUuid = soknadIder.sykepengesoknadUuid,
            sykepengesoknadAtId = soknadIder.sykepengesoknadAtId
        )
    }

    private fun håndterVedtaksperiodeForkastetEvents(value: String) {
        val vedtaksperiodeForkastetEvent = value.tilVedtaksperiodeForkastetEvent()


        vedtaksperiodeForkastetEvent.hendelser.forEach {
            sykepengesoknadVedtaksperiodeRepository.insert(
                sykepengesoknadAtId = it,
                vedtaksperiodeId = vedtaksperiodeForkastetEvent.vedtaksperiodeId
            )
        }

        vedtaksperodeForkastetRepository.insert(
            vedtaksperiodeId = vedtaksperiodeForkastetEvent.vedtaksperiodeId,
        )
    }
}
