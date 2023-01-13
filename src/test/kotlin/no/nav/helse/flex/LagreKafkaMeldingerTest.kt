package no.nav.helse.flex

import no.nav.helse.flex.kafka.rapidTopic
import no.nav.helse.flex.repository.SykepengesoknadIdRepository
import no.nav.helse.flex.repository.SykepengesoknadVedtaksperiodeRepository
import no.nav.helse.flex.repository.VedtaksperiodeForkastetRepository
import no.nav.helse.flex.repository.VedtaksperiodeTilstandRepository
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.shouldNotBeNull
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class LagreKafkaMeldingerTest : FellesTestOppsett() {

    @Autowired
    lateinit var sykepengesoknadIdRepository: SykepengesoknadIdRepository

    @Autowired
    lateinit var sykepengesoknadVedtaksperiodeRepository: SykepengesoknadVedtaksperiodeRepository

    @Autowired
    lateinit var vedtaksperiodeTilstandRepository: VedtaksperiodeTilstandRepository

    @Autowired
    lateinit var vedtaksperiodeForkastetRepository: VedtaksperiodeForkastetRepository

    @Test
    @Order(1)
    fun `Tar imot søknad ider`() {
        sykepengesoknadIdRepository.count() `should be equal to` 0
        kafkaProducer.send(ProducerRecord(rapidTopic, UUID.randomUUID().toString(), soknad))

        await().atMost(10, TimeUnit.SECONDS).until {
            sykepengesoknadIdRepository.count() == 1L
        }

        sykepengesoknadIdRepository.count() `should be equal to` 1
        val sykepengesoknadIdDbRecord = sykepengesoknadIdRepository.findAll().toList().first()
        sykepengesoknadIdDbRecord.sykepengesoknadAtId `should be equal to` "109d1fd9-50cf-4e21-b5e3-dbf10665a849"
        sykepengesoknadIdDbRecord.sykepengesoknadUuid `should be equal to` "7adb45bf-6de2-3c0a-bafe-1f822ddf21a4"

        sykepengesoknadIdRepository.insert(
            sykepengesoknadUuid = sykepengesoknadIdDbRecord.sykepengesoknadUuid,
            sykepengesoknadAtId = sykepengesoknadIdDbRecord.sykepengesoknadAtId
        )

        sykepengesoknadIdRepository.count() `should be equal to` 1
    }

    @Test
    @Order(2)
    fun `Tar imot vedtaksperiodeEndretEvent og lagrer i to tabeller`() {
        sykepengesoknadVedtaksperiodeRepository.count() `should be equal to` 0
        vedtaksperiodeTilstandRepository.count() `should be equal to` 0

        kafkaProducer.send(ProducerRecord(rapidTopic, UUID.randomUUID().toString(), vedtaktaksperiodeEndret))

        await().atMost(10, TimeUnit.SECONDS).until {
            sykepengesoknadVedtaksperiodeRepository.count() == 1L
        }

        await().atMost(10, TimeUnit.SECONDS).until {
            vedtaksperiodeTilstandRepository.count() == 1L
        }

        val sykepengesoknadVedtaksperiodeDbRecord = sykepengesoknadVedtaksperiodeRepository.findAll().toList().first()
        sykepengesoknadVedtaksperiodeDbRecord.vedtaksperiodeId `should be equal to` "9017447e-8480-46e8-b394-ba58874e35aa"
        sykepengesoknadVedtaksperiodeDbRecord.sykepengesoknadAtId `should be equal to` "109d1fd9-50cf-4e21-b5e3-dbf10665a849"

        val vedtaksperiodeTilstandDbRecord = vedtaksperiodeTilstandRepository.findAll().toList().first()
        vedtaksperiodeTilstandDbRecord.id.shouldNotBeNull()
        vedtaksperiodeTilstandDbRecord.vedtaksperiodeId `should be equal to` "9017447e-8480-46e8-b394-ba58874e35aa"
        vedtaksperiodeTilstandDbRecord.tilstand `should be equal to` "AVVENTER_INNTEKTSMELDING_ELLER_HISTORIKK"
        vedtaksperiodeTilstandDbRecord.tidspunkt.shouldNotBeNull()

        sykepengesoknadVedtaksperiodeRepository.count() `should be equal to` 1
        vedtaksperiodeTilstandRepository.count() `should be equal to` 1
    }

    @Test
    @Order(3)
    fun `Tar imot vedtaksperiodeForkastetEvent og lagrer i to tabeller`() {
        sykepengesoknadIdRepository.count() `should be equal to` 1
        vedtaksperiodeForkastetRepository.count() `should be equal to` 0

        kafkaProducer.send(ProducerRecord(rapidTopic, UUID.randomUUID().toString(), vedtaksperiodeForkastet))

        await().atMost(10, TimeUnit.SECONDS).until {
            sykepengesoknadVedtaksperiodeRepository.count() == 2L
        }

        await().atMost(10, TimeUnit.SECONDS).until {
            vedtaksperiodeForkastetRepository.count() == 1L
        }

        val sykepengesoknadVedtaksperiodeDbRecords = sykepengesoknadVedtaksperiodeRepository.findAll().toList()
        sykepengesoknadVedtaksperiodeDbRecords.size `should be equal to` 2

        sykepengesoknadVedtaksperiodeDbRecords.map { it.sykepengesoknadAtId } `should contain` "110d1fd9-50cf-4e21-b5e3-dbf10665a777"
        sykepengesoknadVedtaksperiodeDbRecords.map { it.vedtaksperiodeId } `should contain` "c8c2246a-0667-495b-8fb9-543e900bfd55"

        vedtaksperiodeForkastetRepository.findAll().first().vedtaksperiodeId `should be equal to` "c8c2246a-0667-495b-8fb9-543e900bfd55"
    }

    val soknad =
        """{"id":"7adb45bf-6de2-3c0a-bafe-1f822ddf21a4","@id":"109d1fd9-50cf-4e21-b5e3-dbf10665a849","fnr":"26897298928","fom":"2022-08-17","tom":"2022-08-23","type":"ARBEIDSTAKERE","fravar":[],"status":"SENDT","dodsdato":null,"mottaker":"ARBEIDSGIVER_OG_NAV","sendtNav":"2023-01-11T11:17:20.414860382","sporsmal":[{"id":"96212f3a-d74a-3330-aa7a-8434ec40b499","max":null,"min":null,"tag":"ANSVARSERKLARING","svar":[{"verdi":"CHECKED"}],"svartype":"CHECKBOX_PANEL","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Jeg vet at jeg kan miste retten til sykepenger hvis opplysningene jeg gir ikke er riktige eller fullstendige. Jeg vet også at NAV kan holde igjen eller kreve tilbake penger, og at å gi feil opplysninger kan være straffbart.","kriterieForVisningAvUndersporsmal":null},{"id":"de2aff1d-da6d-34dd-bffd-3e32dbea01ae","max":null,"min":null,"tag":"TILBAKE_I_ARBEID","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"90a2e743-5342-3261-8239-a5dccb860e74","max":"2022-08-23","min":"2022-08-17","tag":"TILBAKE_NAR","svar":[],"svartype":"DATO","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Når begynte du å jobbe igjen?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Var du tilbake i fullt arbeid hos HÅRREISENDE FRISØR i løpet av perioden 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"ca5e083c-0474-3b6c-82fe-5d5f72e8f709","max":null,"min":null,"tag":"FERIE_V2","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"f205175f-422c-366d-a8f9-3dad9da430ce","max":"2022-08-23","min":"2022-08-17","tag":"FERIE_NAR_V2","svar":[],"svartype":"PERIODER","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Når tok du ut feriedager?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Tok du ut feriedager i tidsrommet 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"8e70e98b-ea72-3c6d-a387-444b9468c31b","max":null,"min":null,"tag":"PERMISJON_V2","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"0ecf87b2-2bb1-3f89-b9fb-6c8395fd73e7","max":"2022-08-23","min":"2022-08-17","tag":"PERMISJON_NAR_V2","svar":[],"svartype":"PERIODER","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Når tok du permisjon?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Tok du permisjon mens du var sykmeldt 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"4df973a5-bbde-379f-b668-fb2f92871f4a","max":null,"min":null,"tag":"UTLAND_V2","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"0e36f681-97e6-37f6-b2fd-e8d2a50c5856","max":"2022-08-23","min":"2022-08-17","tag":"UTLAND_NAR_V2","svar":[],"svartype":"PERIODER","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Når var du utenfor EØS?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Var du på reise utenfor EØS mens du var sykmeldt 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"b94dae5e-1596-38df-9e60-1264128ec67d","max":null,"min":null,"tag":"ARBEID_UNDERVEIS_100_PROSENT_0","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"7cd2d485-0a13-3a18-ab8e-e4c9418a65d7","max":null,"min":null,"tag":"HVOR_MYE_HAR_DU_JOBBET_0","svar":[],"svartype":"RADIO_GRUPPE_TIMER_PROSENT","undertekst":null,"undersporsmal":[{"id":"e30fb7b7-3dc1-3cd6-b559-308e18fbd21e","max":null,"min":null,"tag":"HVOR_MYE_PROSENT_0","svar":[],"svartype":"RADIO","undertekst":null,"undersporsmal":[{"id":"16fcd110-6435-396a-8728-6d26f2db7ebf","max":"99","min":"1","tag":"HVOR_MYE_PROSENT_VERDI_0","svar":[],"svartype":"PROSENT","undertekst":"Oppgi i prosent. Eksempel: 40","undersporsmal":[],"sporsmalstekst":"Oppgi hvor mange prosent av din normale arbeidstid du jobbet hos HÅRREISENDE FRISØR i perioden 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Prosent","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"f5bed011-eb04-3301-aa96-471d72d44687","max":null,"min":null,"tag":"HVOR_MYE_TIMER_0","svar":[],"svartype":"RADIO","undertekst":null,"undersporsmal":[{"id":"c2d04428-2c04-36c4-86d6-2dafabfca3a8","max":"150","min":"1","tag":"HVOR_MYE_TIMER_VERDI_0","svar":[],"svartype":"TIMER","undertekst":"Oppgi i timer. Eksempel: 12","undersporsmal":[],"sporsmalstekst":"Oppgi totalt antall timer du jobbet i perioden 17. - 23. august 2022 hos HÅRREISENDE FRISØR","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Timer","kriterieForVisningAvUndersporsmal":"CHECKED"}],"sporsmalstekst":"Oppgi arbeidsmengde i timer eller prosent:","kriterieForVisningAvUndersporsmal":null},{"id":"f9c63ee7-47c4-36ff-9641-1b6d99bcfa8a","max":null,"min":null,"tag":"JOBBER_DU_NORMAL_ARBEIDSUKE_0","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"81debd13-3f40-3ab0-9a58-92e690499e7e","max":"150","min":"1","tag":"HVOR_MANGE_TIMER_PER_UKE_0","svar":[],"svartype":"TIMER","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Oppgi timer per uke","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Jobber du vanligvis 37,5 timer i uka hos HÅRREISENDE FRISØR?","kriterieForVisningAvUndersporsmal":"NEI"}],"sporsmalstekst":"I perioden 17. - 23. august 2022 var du 100 % sykmeldt fra HÅRREISENDE FRISØR. Jobbet du noe hos HÅRREISENDE FRISØR i denne perioden?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"4fb6afed-675b-377f-8fe9-f14ed9fbc998","max":null,"min":null,"tag":"ANDRE_INNTEKTSKILDER","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"2755c52d-4f00-3e22-976b-24f5be19c59e","max":null,"min":null,"tag":"HVILKE_ANDRE_INNTEKTSKILDER","svar":[],"svartype":"CHECKBOX_GRUPPE","undertekst":null,"undersporsmal":[{"id":"77b266f1-5d99-3a93-bfe2-3fb8ac8c1942","max":null,"min":null,"tag":"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[{"id":"3c1e0420-6226-3afe-bb79-a5803dcb6614","max":null,"min":null,"tag":"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD_ER_DU_SYKMELDT","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er du sykmeldt fra dette?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"andre arbeidsforhold","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"ac9e1ac5-a10d-3195-9eab-233548981c26","max":null,"min":null,"tag":"INNTEKTSKILDE_SELVSTENDIG","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[{"id":"b795bafe-ef95-3a36-b2c2-02619ec33be6","max":null,"min":null,"tag":"INNTEKTSKILDE_SELVSTENDIG_ER_DU_SYKMELDT","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er du sykmeldt fra dette?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"selvstendig næringsdrivende","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"f4c5d362-a0cd-3638-af4c-399fe5bd8b88","max":null,"min":null,"tag":"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[{"id":"27592bfb-3cb3-3142-a7aa-bf7ee549dabf","max":null,"min":null,"tag":"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA_ER_DU_SYKMELDT","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er du sykmeldt fra dette?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"dagmamma","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"26da99d9-6cf9-36b8-a225-68bbbaa46e70","max":null,"min":null,"tag":"INNTEKTSKILDE_JORDBRUKER","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[{"id":"7f86ffe6-77ce-37cb-b074-b34a32c70a7b","max":null,"min":null,"tag":"INNTEKTSKILDE_JORDBRUKER_ER_DU_SYKMELDT","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er du sykmeldt fra dette?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"jordbruk / fiske / reindrift","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"4a7a6506-51c8-3678-a7cb-14e86f85b09f","max":null,"min":null,"tag":"INNTEKTSKILDE_FRILANSER","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[{"id":"864cfac9-f50b-3fe9-87cc-756cdd236c8e","max":null,"min":null,"tag":"INNTEKTSKILDE_FRILANSER_ER_DU_SYKMELDT","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er du sykmeldt fra dette?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"frilanser","kriterieForVisningAvUndersporsmal":"CHECKED"},{"id":"56560045-c3fe-36c2-8049-a6f81c6839ad","max":null,"min":null,"tag":"INNTEKTSKILDE_ANNET","svar":[],"svartype":"CHECKBOX","undertekst":null,"undersporsmal":[],"sporsmalstekst":"annet","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Hvilke andre inntektskilder har du?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Har du andre inntektskilder enn HÅRREISENDE FRISØR?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"50f85cc7-23d1-399a-a550-336fb09027ba","max":null,"min":null,"tag":"UTDANNING","svar":[{"verdi":"NEI"}],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[{"id":"4b81b957-5b5d-308d-98ca-3ac7d3b4aa1d","max":null,"min":null,"tag":"FULLTIDSSTUDIUM","svar":[],"svartype":"JA_NEI","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Er utdanningen et fulltidsstudium?","kriterieForVisningAvUndersporsmal":null},{"id":"cc6c32e4-df96-3df5-af79-33b7d1365a50","max":"2022-08-23","min":null,"tag":"UTDANNING_START","svar":[],"svartype":"DATO","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Når startet du på utdanningen?","kriterieForVisningAvUndersporsmal":null}],"sporsmalstekst":"Har du vært under utdanning i løpet av perioden 17. - 23. august 2022?","kriterieForVisningAvUndersporsmal":"JA"},{"id":"a2cbc9a2-cc81-395d-bbff-40fb37e89c1d","max":null,"min":null,"tag":"VAER_KLAR_OVER_AT","svar":[],"svartype":"IKKE_RELEVANT","undertekst":"<ul><li>Du kan bare få sykepenger hvis det er din egen sykdom eller skade som hindrer deg i å jobbe. Sosiale eller økonomiske problemer gir ikke rett til sykepenger.</li><li>Du kan miste retten til sykepenger hvis du nekter å opplyse om din egen arbeidsevne, eller hvis du ikke tar imot behandling eller tilrettelegging.</li><li>Retten til sykepenger gjelder bare inntekt du har mottatt som lønn og betalt skatt av på sykmeldingstidspunktet.</li><li>NAV kan innhente opplysninger som er nødvendige for å behandle søknaden.</li><li>Du må melde fra til NAV hvis du satt i varetekt, sonet straff eller var under forvaring i sykmeldingsperioden.</li><li>Fristen for å søke sykepenger er som hovedregel 3 måneder</li></ul><p>Du kan lese mer om rettigheter og plikter på <a href=\"https://www.nav.no/sykepenger\" target=\"_blank\">nav.no/sykepenger</a>.</p>","undersporsmal":[],"sporsmalstekst":"Viktig å være klar over:","kriterieForVisningAvUndersporsmal":null},{"id":"8f4cc62f-7942-3928-a7bd-7cb7fa58a826","max":null,"min":null,"tag":"BEKREFT_OPPLYSNINGER","svar":[{"verdi":"CHECKED"}],"svartype":"CHECKBOX_PANEL","undertekst":null,"undersporsmal":[],"sporsmalstekst":"Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte.","kriterieForVisningAvUndersporsmal":null}],"merknader":null,"opprettet":"2022-09-29T15:30:24.857654","@opprettet":"2023-01-11T11:17:20.414860382","korrigerer":null,"@event_name":"sendt_søknad_nav","korrigertAv":null,"arbeidsgiver":{"navn":"HÅRREISENDE FRISØR","orgnummer":"839942907"},"avsendertype":"BRUKER","ettersending":false,"sendTilGosys":null,"sykmeldingId":"005bdcba-cdd1-4153-ad01-f740d7e28315","egenmeldinger":[],"permitteringer":[],"soknadsperioder":[{"fom":"2022-08-17","tom":"2022-08-23","grad":100,"avtaltTimer":null,"faktiskGrad":null,"faktiskTimer":null,"sykmeldingsgrad":100,"sykmeldingstype":"AKTIVITET_IKKE_MULIG"}],"arbeidssituasjon":"ARBEIDSTAKER","behandlingsdager":null,"opprinneligSendt":null,"arbeidGjenopptatt":null,"papirsykmeldinger":[],"sendtArbeidsgiver":"2023-01-11T11:17:20.414860382","startSyketilfelle":"2022-08-01","sykmeldingSkrevet":"2022-08-17T14:00:00","system_read_count":2,"arbeidUtenforNorge":null,"andreInntektskilder":[],"egenmeldtSykmelding":false,"soktUtenlandsopphold":null,"utenlandskSykmelding":false,"fravarForSykmeldingen":[],"merknaderFraSykmelding":null,"harRedusertVenteperiode":null,"arbeidsgiverForskutterer":null,"system_participating_services":[{"id":"4f2d97f0-5fe8-403d-a158-8da7259d5cf3","time":"2023-01-11T11:17:20.453706177","image":"ghcr.io/navikt/helse-spedisjon/spedisjon:13e3400","service":"spedisjon","instance":"spedisjon-5fd49c9888-xqg4b"},{"id":"f6b3764c-c176-4894-a7e2-606239939420","time":"2023-01-11T11:17:20.543015513","service":"spedisjon","instance":"spedisjon-5fd49c9888-xqg4b","image":"ghcr.io/navikt/helse-spedisjon/spedisjon:13e3400"},{"id":"109d1fd9-50cf-4e21-b5e3-dbf10665a849","time":"2023-01-11T11:17:20.550231728","service":"helse-spleis","instance":"helse-spleis-85df9584b6-5qbjm","image":"ghcr.io/navikt/helse-spleis/spleis:cc3747c"}],"fødselsdato":"1972-09-26","aktorId":"2002045142223","historiskeFolkeregisteridenter":[],"@forårsaket_av":{"id":"e35d0ef1-8774-4e94-9354-9563ffd00c6e","opprettet":"2023-01-11T11:17:20.534431583","event_name":"behov","behov":["HentPersoninfoV3"]}}"""

    val vedtaktaksperiodeEndret =
        """{"@event_name":"vedtaksperiode_endret","organisasjonsnummer":"839942907","vedtaksperiodeId":"9017447e-8480-46e8-b394-ba58874e35aa","gjeldendeTilstand":"AVVENTER_INNTEKTSMELDING_ELLER_HISTORIKK","forrigeTilstand":"START","hendelser":["109d1fd9-50cf-4e21-b5e3-dbf10665a849"],"makstid":"2023-07-10T11:17:20.588873284","fom":"2022-08-17","tom":"2022-08-23","@id":"b7fd0a82-eb47-4040-a4c1-3674dad07bfd","@opprettet":"2023-01-11T11:17:20.589589738","system_read_count":0,"system_participating_services":[{"id":"b7fd0a82-eb47-4040-a4c1-3674dad07bfd","time":"2023-01-11T11:17:20.589589738","service":"helse-spleis","instance":"helse-spleis-85df9584b6-5qbjm","image":"ghcr.io/navikt/helse-spleis/spleis:cc3747c"}],"aktørId":"2002045142223","fødselsnummer":"26897298928"}"""

    val vedtaksperiodeForkastet =
        """{"id":"350223d4-2115-4648-b8fb-6bae681209b8","@event_name":"vedtaksperiode_forkastet","tidsstempel":"2023-01-11T10:30:48.872371833","fodselsnummer":"66847100185","vedtaksperiodeId":"c8c2246a-0667-495b-8fb9-543e900bfd55","organisasjonsnummer":"907670201","hendelser":["110d1fd9-50cf-4e21-b5e3-dbf10665a777"]}"""
}
