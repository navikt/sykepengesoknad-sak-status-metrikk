package no.nav.helse.flex.melding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.kafka.dittSykefravaerMeldingTopic
import no.nav.helse.flex.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("avslatt")
class MeldingListener(
    val oppdaterMeldingerFraKafka: OppdaterMeldingerFraKafka
) {

    @KafkaListener(
        topics = [dittSykefravaerMeldingTopic],
        properties = ["auto.offset.reset = earliest"], // TODO Fjern meg når vi er live
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        oppdaterMeldingerFraKafka.oppdater(cr.key(), objectMapper.readValue(cr.value()))
        acknowledgment.acknowledge()
    }
}
