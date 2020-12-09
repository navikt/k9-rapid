# k9-rapid alerts
Alerts for alle Omsorgspenger sine applikasjonar.

For meir info om korleis alarmane fungerer kan de sjå:
> https://doc.nais.io/observability/alerts

### Nyttige kommandoar
Sjå info om alerten i Kubernetes
> kubectl describe alerts k9-rapid-alerts -n omsorgspenger

Manuell deploy av alert (legg inn MILJO, t.d. dev-gcp)
> kubectl apply -f MILJO-alerts.yml

Manuell sletting av alert
> kubectl delete alert k9-rapid-alerts -n omsorgspenger

### Varsling til Slack
Varsla hamnar i fylgjande Slack-kanalar avhengig av miljø
- `#omsorgspenger-alerts`

### For NAV-ansatte
Vi er tilgjengelege på Slack-kanalen #omsorgspenger