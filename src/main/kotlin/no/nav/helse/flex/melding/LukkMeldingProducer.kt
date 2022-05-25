package no.nav.helse.flex.melding

import no.nav.helse.flex.kafka.dittSykefravaerMeldingTopic
import no.nav.helse.flex.melding.domene.LukkMelding
import no.nav.helse.flex.melding.domene.MeldingDbRecord
import no.nav.helse.flex.melding.domene.MeldingKafkaDto
import no.nav.helse.flex.serialisertTilString
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LukkMeldingProducer(
    private val producer: KafkaProducer<String, String>
) {

    fun produserMelding(meldingDbRecord: MeldingDbRecord): RecordMetadata {

        return producer.send(
            ProducerRecord(
                dittSykefravaerMeldingTopic, meldingDbRecord.meldingUuid,
                MeldingKafkaDto(
                    fnr = meldingDbRecord.fnr,
                    opprettMelding = null,
                    lukkMelding = LukkMelding(timestamp = Instant.now())
                ).serialisertTilString()
            )
        ).get()
    }
}
