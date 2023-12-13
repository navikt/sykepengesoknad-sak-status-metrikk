package no.nav.helse.flex.kafka

import no.nav.helse.flex.FinnStatusFraRapid
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val RAPID_TOPIC = "tbd.rapid.v1"

@Component
class RapidListener(
    val finnStatusFraRapid: FinnStatusFraRapid,
) {
    @KafkaListener(
        topics = [RAPID_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        concurrency = "4",
    )
    fun listen(
        cr: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment,
    ) {
        finnStatusFraRapid.oppdater(cr.value())
        acknowledgment.acknowledge()
    }
}
