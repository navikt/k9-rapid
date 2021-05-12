# Kafka

## oneshot.json (on-prem)
Blir brukt mot **deprecated** [Kafka on-prem](https://doc.nais.io/legacy/sunset/#kafka-onprem)
Må kjøres manuelt av en `MANAGER` for å effektuere endringer i filen.

Her skal det ikke legges til nye consumers/producers - kun fjernes. Nye skal kun legges i `kafkarator.yaml`.

### k9-vaktmester
[k9-vaktmester](https://github.com/navikt/k9-vaktmester) kjører to instanser. `k9-vaktmester` bruker topic på `Aiven`, mens `k9-vaktmesterassistent` bruker topic on-prem.

Derfor er fortsatt `srv-k9-vaktmester` både `PRODUCER` og `CONSUMER` i `oneshot.json` ettersom `k9-vaktmesterassistent` bruker dem.

Dette muliggjør en gradvis oppdatering av alle appene. Samt at producere vi ikke bestemmer oppdateringstakt på kan endres løpende.

Når alle apper som bruker on-prem har byttet til `Aiven` kan `k9-vaktmesterassistent` fjernes.

## kafkarator.yaml (Aiven)
Blir brukt mot [Kafka på Aiven](https://doc.nais.io/addons/kafka/)

Inneholder acl for `omsorgspenger.k9-rapid-v2` topicen. Rapids-apper må ha `access` satt til `readwrite`.

Ved oppdatering på `master` branch vil endringene deployes både til `dev` og `prod`.

Om man opprette en branch og endrer vil man kunne deploye denne manuelt, og vil da kun deployes til `dev`.