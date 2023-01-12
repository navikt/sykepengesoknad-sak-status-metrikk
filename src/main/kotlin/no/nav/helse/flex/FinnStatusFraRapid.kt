package no.nav.helse.flex

import no.nav.helse.flex.domain.hentEventName
import no.nav.helse.flex.domain.tilSøknadMedId
import no.nav.helse.flex.repository.SykepengesoknadIdRepository
import org.springframework.stereotype.Component

@Component
class FinnStatusFraRapid(
    val sykepengesoknadIdRepository: SykepengesoknadIdRepository,
) {

    fun oppdater(value: String) {

        val eventName = value.hentEventName()

        when (eventName) {
            "sendt_søknad_nav", "sendt_søknad_arbeidsgiver" -> {
                håndterSendtSøknadEvents(value)
            }
        }
    }

    fun håndterSendtSøknadEvents(value: String) {
        val soknadIder = value.tilSøknadMedId()
        sykepengesoknadIdRepository.insert(sykepengesoknadUuid = soknadIder.sykepengesoknadUuid, sykepengesoknadAtId = soknadIder.sykepengesoknadAtId)
    }
}
