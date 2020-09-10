# Overføre omsorgsdager

Bibliotek for å melde om behov og få løsning på overføring av omsorgsdager.
Her eksempel med en helt plain Kafka producer/consumer - men det er ikke noe krav.
Så lenge key og value på entryen man legger på topicen er fra `keyValue` i en `Behovssekvens`.

## Melde behov om overføring av dager

- `id` må være en [ULID](https://github.com/ulid/spec) og ideelt sett genereres i klienten og er ny for hver overføring.

```kotlin
val rapidTopic = "k9-rapid-v1"
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

- Om løsningen `erGjennomført()` kan det ha konsekvenser for X antall personer.
Derfor inneholder løsningen et Map hvor nøkkelen er personens identitetsnummer, og inneholder hvilke overføringer den nå har `fått` og `gitt` (To separate lister)
Eventuelle tidligere overføringer man har lagret på de samme personene kan nå slettes. Er kun de i den siste løsningen som gjelder.

- Om løsningen `erAvslått()` har ingen overføringer blitt gjort, men inneholder overføringen(e) som ble forsøkt gjort. Disse kan ev. lagres om man ønsker å vise frem feilede overføringer også.

- Om løsningen `behandlesIkkeINyLøsning()` har ingen overføringer blitt gjort, og inneholder heller ingen overføringer som ble forsøkt gjort da dette er sendt til behandling utenom ny løsning.

```kotlin
val rapidTopic = "k9-rapid-v1"
val consumer = KafkaConsumer(consumerConfig, stringDeserializer, stringDeserializer).also {
    it.subsribe(rapidTopic)
}

consumer.poll(Duration.ofSeconds(1)).records(rapidTopic)
    .filter { it.value().somMelding().harLøsningPå(OverføreOmsorgsdagerLøsningResolver.Instance) }
    .forEach {
        val (id, løsning) = it.value().somMelding().løsningPå(OverføreOmsorgsdagerLøsningResolver.Instance)
        // id er den samme som fra behovet. Kan være lur å lagre unna sammen med overføringen for å ha en referanse på hvor de kommer fra.
        // Lagre unna overføringene som nå gjelder for personene i løsningen
        // Slette eller deaktivere ev. tidligere overføringer på samme personene.
    }
```