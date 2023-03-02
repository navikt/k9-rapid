# k9-rapid

---

Verktøy for bruk av [rapids-and-rivers](https://github.com/navikt/rapids-and-rivers) på i `k9`-sfæren.

### Rapids-and-rivers applikasjoner i k9-rapid

- **k9-vaktmester**
  - Håndtering av meldinger som ikke er løst. F.eks. republisering, legge til eller ignorere ulike behov.
  - Lagrer grunnlag for behandling på norsk jord

- **k9-personopplysninger**
  - Løser behov for identitetsnummer, gradering & familierelasjoner fra PDL

- **omsorgspenger-journalforing**
  - Håndtering av oppgaver & journalposter
  - Oppretter gosys-oppgaver, oppdaterer & ferdigstiller journalposter
  - Genererer & journalfør json-payloads i pdf-format
  - Kopierer ferdigstilte journalposter & knytter til riktig sak.

- **omsorgspenger-sak**
  - Oppretter saksnummer & lagrer i database
  - Rest-api som sjekker om saksbehandler har tilgang til saksnummer

- **omsorgspenger-rammemeldinger** [
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_omsorgspenger-rammemeldinger&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=navikt_omsorgspenger-rammemeldinger)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_omsorgspenger-rammemeldinger&metric=ncloc)](https://sonarcloud.io/summary/overall?id=navikt_omsorgspenger-rammemeldinger)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=navikt_omsorgspenger-rammemeldinger&metric=duplicated_lines_density)](https://sonarcloud.io/summary/overall?id=navikt_omsorgspenger-rammemeldinger)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_omsorgspenger-rammemeldinger&metric=coverage)](https://sonarcloud.io/summary/overall?id=navikt_omsorgspenger-rammemeldinger) ]
  - Håndterer behandling og lagring av rammemeldinger som er flyttet ut av Infotrygd, samt sammenstille nye rammemeldinger med det som allerede finnes i Infotrygd. 

- **omsorgsdager**
  - Håndtering av nye rammevedtak (kronisk syk, midlertidig alene, alene omsorg) som opprettes i k9-sak & brukes for beregning av kvote i k9-aarskvantum.

### Andre applikasjoner som kun sender meldinger in på rapiden
- **k9-punsjbolle**
- **k9-punsj**
- **k9-sak**
- **omsorgsdager-melding-prosessering**

### Applikasjoner som er del av k9-rapid men ikke bruker kafka
- **omsorgspenger-tilgangsstyring**
  - Brukes for o gjøre tilgangssjekk m.h.a AD-grupper & PDL-data (gradering)
- **omsorgspenger-proxy**
  - Proxy for kommunikation mellom fss<->gcp och utveksling av token.
- **omsorgspenger-infotrygd-rammevedtak**
  - Henter & tolker vedtak i fritekst-format fra infotrygd.

# Henvendelser

---

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

---

Interne henvendelser kan sendes via Slack i kanalen #sif_omsorgspenger.
