package no.nav.helse.flex

import no.nav.helse.flex.melding.MeldingKafkaProducer
import no.nav.helse.flex.melding.domene.MeldingKafkaDto
import no.nav.helse.flex.melding.domene.OpprettMelding
import org.amshove.kluent.`should be null`
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class IntegrationTest : FellesTestOppsett() {

    @Autowired
    lateinit var meldingKafkaProducer: MeldingKafkaProducer

    val fnr = "12343787332"

    @Test
    @Order(1)
    fun `mottar melding`() {
        val kafkaMelding = MeldingKafkaDto(
            fnr = fnr,
            opprettMelding = OpprettMelding(
                tekst = "Sjekk denne meldinga",
                lenke = "http://www.nav.no",
                meldingType = "whatever",
                synligFremTil = Instant.now().plus(2, ChronoUnit.DAYS)
            ),
            lukkMelding = null
        )
        val uuid = UUID.randomUUID().toString()
        meldingKafkaProducer.produserMelding(uuid, kafkaMelding)

        await().atMost(5, TimeUnit.SECONDS).until {
            meldingRepository.findByFnrIn(listOf(fnr)).isNotEmpty()
        }

        val melding = meldingRepository.findByFnrIn(listOf(fnr)).first()
        melding.lukket.`should be null`()
    }
}
