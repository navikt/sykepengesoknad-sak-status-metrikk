package no.nav.helse.flex.kafka

import no.nav.helse.flex.FinnStatusFraRapid
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class RapidListener(
    val finnStatusFraRapid: FinnStatusFraRapid,
) {
    @KafkaListener(
        topics = [rapidTopic],
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        finnStatusFraRapid.oppdater(cr.value())
        acknowledgment.acknowledge()
    }
}

const val rapidTopic = "todo-riktig-topic"
