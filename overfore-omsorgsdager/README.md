# Overføre omsorgsdager

Bibliotek for å melde om behov og få løsning på overføring av omsorgsdager.
Her eksempel med en helt plain Kafka producer/consumer - men det er ikke noe krav.
Så lenge key og value på entryen man legger på topicen er fra `keyValue` i en `Behovssekvens`.

## Melde behov om overføring av dager

- `id` må være en [ULID](https://github.com/ulid/spec) og ideelt sett genereres i klienten og er ny for hver overføring.

```kotlin
val rapidTopic = "k9-rapid-v2"
val producer = KafkaProducer(producerConfig, stringSerializer, stringSerializer)

val (id, overføring) = Behovssekvens(
        id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
        correlationId = UUID.randomUUID().toString(),
        behov = arrayOf(OverføreOmsorgsdagerBehov(
                fra = OverføreOmsorgsdagerBehov.OverførerFra(
                        identitetsnummer = "11111111111",
                        borINorge = true,
                        jobberINorge = true
                ),
                til = OverføreOmsorgsdagerBehov.OverførerTil(
                        identitetsnummer = "11111111112",
                        relasjon = OverføreOmsorgsdagerBehov.Relasjon.NåværendeSamboer,
                        harBoddSammenMinstEttÅr = false
                ),
                omsorgsdagerTattUtIÅr = 10,
                omsorgsdagerÅOverføre = 5,
                barn = listOf(OverføreOmsorgsdagerBehov.Barn(
                        identitetsnummer = "11111111113",
                        fødselsdato = LocalDate.now(),
                        aleneOmOmsorgen = true,
                        utvidetRett = false
                )),
                kilde = OverføreOmsorgsdagerBehov.Kilde.Brev,
                journalpostIder = listOf()
        ))
).keyValue

producer.send(ProducerRecord(rapidTopic, id, overføring))
```

## Hente løsninger på overføring av dager

- Om løsningen `erGjennomført()` kan det ha konsekvenser for `X` antall personer.
Derfor inneholder løsningen et map hvor nøkkelen er personens identitetsnummer, og inneholder hvilke overføringer den nå har `fått` og `gitt` (to separate lister).
Eventuelle tidligere overføringer man har lagret på de samme personene kan nå slettes/markeres som ikke lenger aktive. 
Det er kun de i den siste løsningen som gjelder.

- For å være sikker på at det er den siste løsningen kan man sjekke mot referansen på ev. nåværende lagrede overføringer. Bl.a. for å kunne håndetre uventet Kafka-oppførsel.
```kotlin
val gammelId = hentFraDb(identitetsnummer)
if (id.erNyereEnn(gammelId)) {
    lagreNyeOverføringerIDb(identitetsnummer, id, overføringer)
}
```
- Om løsningen `erAvslått()` har ingen overføringer blitt gjort, men inneholder overføringen(e) som ble forsøkt gjort. Disse kan ev. lagres om man ønsker å vise frem feilede overføringer også.

- Om løsningen `ikkeBehandlesAvNyttSystem()` har ingen overføringer blitt gjort, og inneholder heller ingen overføringer som ble forsøkt gjort da dette er sendt til behandling utenom nytt system.

```kotlin
val rapidTopic = "k9-rapid-v2"
val consumer = KafkaConsumer(consumerConfig, stringDeserializer, stringDeserializer).also {
    it.subsribe(rapidTopic)
}

consumer.poll(Duration.ofSeconds(1)).records(rapidTopic)
    .filter { it.value().somMelding().harLøsningPå(OverføreOmsorgsdagerLøsningResolver.Instance) }
    .forEach {
        val (id, løsning) = it.value().somMelding().løsningPå(OverføreOmsorgsdagerLøsningResolver.Instance)
        // id er den samme som fra behovet. Må lagres sammen med overføringene som en referanse.
    }
```