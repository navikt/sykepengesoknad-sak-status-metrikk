# ditt-sykefravaer-backend
Frontend for backend for ditt sykefravær. 
Tar imot hendelser på kafka og leverer en liste med meldinger som vises i frontend. 
Disse meldingene kan lukkes og har en lenke videre et annet sted.

# Komme i gang

Bygges med gradle. Standard spring boot oppsett.

# Hvordan produsere meldinger
Meldinger produseres til `flex.ditt-sykefravaer-melding` . 
Key er en uuid som settes av produsenten. Denne kan brukes til å senere fjerne meldingen.
For format på melding se `MeldingKafkaDto` i kildekoden i dette repoet.
Man må enten sende en opprettMelding eller en lukkMelding. 

Feltet `meldingType` settes til en beskrivende enum for meldingen, f.eks. `MANGLENDE_INNTEKTSMELDING`. 
Feltet kan ikke være personidentifiserbart siden dette brukes i amplitude sporingen.  

Meldinger som er lukket eller har synligFremTil i fortiden vil ikke bli vist på ditt sykefravær.

Når en melding lukkes i ditt sykefravær frontend publiseres dette som et event på `flex.ditt-sykefravaer-melding`.
Produsenten av meldingen kan da agere på denne. F.eks. til å fjerne en tilsvarende brukernotifikasjon.
Tilsvarende bruker denne appen det eventet til å oppdatere lukket tidspunktet i databasen.

## Hvordan teste å produsere meldinger
Meldinger kan produseres fra https://flex-testdata-generator.dev.nav.no/ditt-sykefravaer-melding i dev. Naisdevice påkrevd.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles til flex@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #flex.
