# Recap deploy — Giugno 2026

Riepilogo operativo delle modifiche di questo ciclo di lavoro e dei passi da eseguire
a livello di **database** e **applicativo** per metterle in esercizio.

## Indice delle modifiche

| # | Ambito | Modifica | Commit |
|---|--------|----------|--------|
| 1 | App | Fix 500 `Index 0 out of bounds for length 0` su delete/deleteAll PKMI (guard su Stats mancanti) | `89b5636` |
| 2 | App | Override categoria item ↔ GSG (rimappa la categoria in questa app rispetto a GSG) | `50c1a2d` |
| 3 | App | Hardening del lookup `KitchenMenuItem` nullo nell'ingestione ordini (no più NPE/500) | `50c1a2d` |
| 4 | App + **DB** | Resilienza Postgres LISTEN/NOTIFY (riconnessione + keepalive) e fix trigger `pg_notify` | `57389b5` |

> ⚠️ Solo la modifica **#4 ha un passo a livello di DB** (sul Postgres di Gestione Stand
> Gastronomico). Tutte le altre sono solo applicative. MongoDB non richiede alcun intervento
> (le collezioni vengono create automaticamente).

---

## 1. Passi a livello di DATABASE

### 1.1 Postgres GSG — aggiornare la function `menu_items_notify()` (modifica #4)

La function del trigger costruiva la NOTIFY per concatenazione di stringhe
(`EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt)`), che si rompe quando i campi testo
(`clientName`, `orderNotes`, `menuItemNotes`) contengono un **apostrofo** e fallisce sul limite
di **8000 byte** del payload → l'INSERT va in rollback e l'ordine si perde. Ora usa `pg_notify`.

**Cosa fare:** eseguire sul DB Postgres di GSG **solo** la porzione
`CREATE OR REPLACE FUNCTION public.menu_items_notify()` aggiornata, presente in:

```
src/main/resources/postgres/gsg_trigger_function.sql
```

La riga modificata (dentro la function):

```sql
-- prima (fragile)
EXECUTE FORMAT('NOTIFY plate_orders, ''%s''', v_txt);

-- dopo (robusto)
PERFORM pg_notify('plate_orders', v_txt);
```

Note:
- **NON** ricreare `CREATE TABLE orders_ack` né `CREATE TRIGGER righe_articoli_trigger`: restano
  invariati. `CREATE OR REPLACE FUNCTION` aggiorna la function senza toccare il trigger collegato.
- Nessun downtime richiesto: la sostituzione della function è atomica.
- Verifica rapida: inserire un ordine di test con un apostrofo nel nome cliente (es. `D'Angelo`)
  e controllare che arrivi la notifica e la riga in `orders_ack`.

### 1.2 MongoDB — nessuna azione

La collezione `category_mapping` (modifica #2) viene creata automaticamente da Spring Data al
primo inserimento. Nessuna migrazione manuale.

---

## 2. Passi a livello APPLICATIVO

### 2.1 Build & deploy

```bash
./mvnw clean package   # richiede accesso al repository Maven
# deploy dell'artefatto secondo la procedura standard dell'ambiente
```

> In ambienti dietro proxy aziendale assicurarsi che il repository Maven sia raggiungibile
> (in sviluppo locale `proxy.corvallis.it` deve essere accessibile per scaricare le dipendenze).

### 2.2 Configurazione (`application.yaml`) — modifica #4

Nuove/corrette proprietà già presenti nel file:

```yaml
spring:
  task:
    scheduling:
      enabled: true
      pool:
        size: 2            # health-check e batch job non si serializzano più
application:
  jobs:
    listener-health-check:
      fixedDelay: 30000    # intervallo (ms) di validazione/keepalive della connessione LISTEN
```

- `spring.task.scheduling` è stato **spostato al livello corretto** (prima era erroneamente sotto
  `spring.data`, quindi ignorato).
- `listener-health-check.fixedDelay` è facoltativo (default 30000 ms). La query di validazione fa
  anche da keepalive contro il reaping delle connessioni idle.

### 2.3 Post-deploy — configurare gli override di categoria (modifica #2)

Gli override rimappano la categoria di un item in questa applicazione rispetto a quella di GSG
(es. *Elton John* è *Panini* in GSG ma deve stare in *Piatti Unici*). Sono riferiti agli **id
esterni GSG** (stabili tra i re-import) e vengono applicati durante l'import `/gsg/init`.

1. Creare i mapping via API `POST /category-mapping`:

   Override per singolo item (`Articolo.id` GSG → `Tipologia.id` GSG di destinazione):
   ```json
   { "externalItemId": 123, "targetExternalCategoryId": 7, "description": "Elton John -> Piatti Unici" }
   ```

   Override per intera categoria GSG (`Tipologia.id` sorgente → destinazione):
   ```json
   { "sourceExternalCategoryId": 1, "targetExternalCategoryId": 7 }
   ```

   Regole: esattamente uno tra `externalItemId` e `sourceExternalCategoryId`; l'item-level ha
   precedenza sul category-level. Endpoint CRUD: `GET/POST/PUT/DELETE /category-mapping`.

2. **Rilanciare l'import** perché gli override abbiano effetto:
   ```
   POST /gsg/init
   ```
   ⚠️ `/gsg/init` ricarica categorie e menu da GSG e **cancella ordini, categorie e menu item**
   esistenti (e azzera gli slot dei piatti). Eseguirlo in finestra controllata. I mapping in
   `category_mapping` **sopravvivono** all'import e vengono riapplicati ogni volta.

### 2.4 Verifica post-deploy

- **LISTEN/NOTIFY (#4):** nei log all'avvio deve comparire
  `ServletContextListenerImpl - listening on channel plate_orders`. Simulare una caduta della
  connessione (es. riavvio del Postgres GSG): entro ~30s deve apparire il WARN di riconnessione
  seguito di nuovo dal log di `listening on channel`.
- **Override categoria (#2):** dopo `/gsg/init`, verificare che l'item rimappato risulti nella
  categoria di destinazione (`GET /category/{id}/items` o la vista per categoria nel FE).
- **Hardening lookup (#3):** un ordine GSG per un item non presente in Mongo non genera più 500;
  produce un WARN e l'ordine resta `ack=false` (verrà ritentato dal batch job dopo un re-import).
- **Stats fix (#1):** delete/deleteAll di PKMI non restituiscono più 500 anche per item con data
  priva di documento Stats.

---

## 3. Rollback

| Modifica | Rollback |
|----------|----------|
| #4 DB | Ripristinare la versione precedente della function con `CREATE OR REPLACE FUNCTION` (variante `EXECUTE FORMAT`). |
| #1–#4 App | Redeploy dell'artefatto del commit precedente. Le proprietà yaml nuove sono retrocompatibili (hanno default). |
| #2 dati | I documenti in `category_mapping` possono essere rimossi via `DELETE /category-mapping/{id}`; un successivo `/gsg/init` ripristina le categorie 1:1 da GSG. |
