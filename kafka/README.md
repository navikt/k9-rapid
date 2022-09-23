# Kafka

## kafkarator.yaml (Aiven)
Blir brukt mot [Kafka på Aiven](https://doc.nais.io/addons/kafka/)

Inneholder acl for `omsorgspenger.k9-rapid-v2` topicen. Rapids-apper må ha `access` satt til `readwrite`.

Ved oppdatering på `master` branch vil endringene deployes både til `dev` og `prod`.

Om man opprette en branch og endrer vil man kunne deploye denne manuelt, og vil da kun deployes til `dev`.