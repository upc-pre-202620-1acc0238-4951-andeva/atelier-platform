# Guía de pendientes: taller demo y cierre del trabajo

Estado al 23/10/2026. Nada de lo de abajo está commiteado todavía.

| # | Pendiente | Quién | Cuándo |
|---|-----------|-------|--------|
| A1 | Volver a correr el seed en local y confirmar las tildes | Yo o tú | Antes de publicar |
| A2 | Probar el inicio de sesión (dueño y mecánico) y las pantallas clave | Yo o tú | Antes de publicar |
| A3 | Correr la suite completa de tests | Yo o tú | Antes de publicar |
| A4 | Commit, release 1.2.0 y push | Yo (con tu OK) o tú | Después de A1–A3 |
| B1 | Configurar las variables `DEMO_*` en Render y desplegar | **Solo tú** | Después de A4 |
| B2 | Revisar la demo en producción | **Solo tú** | Después de B1 |
| B3 | Rotar las claves de Google Maps y Firebase | **Solo tú** | Cuanto antes |
| B4 | Actualizar la imagen del backlog de Jira (quitar US07) | **Solo tú** | Antes de entregar el informe |
| B5 | Commitear atelier-report y atelier-mobile | Tú (o yo con tu OK) | Cuando quieras |
| B6 | Decidir qué hacer con los trials que nunca vencen | Tú decides, yo lo implemento | Después de la clase |
| B7 | Limpiar los contenedores y bases locales | Yo o tú | Al final |

**Estado de la parte A (23/10):**
- A1 y A2 están hechos y verificados en local, en dos escenarios:
  - **taller nuevo:** las tildes ya se guardan bien;
  - **taller existente adoptado:** simula tu cuenta de prueba registrada sin planes.
- A3 y A4 los cierro yo. La release y el push esperan tu OK.

Abajo queda igual cómo hacer cada paso por tu cuenta.

---

## Parte A: antes de publicar

### A1. Volver a correr el seed en local

**Por qué:** la última corrida guardó "SuÃ¡rez" en vez de "Suárez". Ya lo corregí (los nombres con tilde se leen desde Java, no desde `application.properties`), pero no lo volví a probar.

Todo esto es contra la base local `sqldemo` del contenedor `atelier-postgres`, nunca contra Aiven.

1. Recrea la base de prueba desde `sqlfresh`, que ya tiene los planes:

   ```bash
   docker exec atelier-postgres psql -U postgres -c "drop database if exists sqldemo" -c "create database sqldemo template sqlfresh"
   ```

2. Empaqueta el backend:

   ```bash
   ./mvnw -B -q -DskipTests package
   ```

3. Arranca la app con el seed activado. Uso un script del scratchpad de Claude que carga `.env.prod` y las credenciales de prueba. Corre el jar dentro de un contenedor que comparte la red de `atelier-postgres`, porque Docker no está exponiendo el puerto 5432 al host:

   ```bash
   /c/Users/joelh/AppData/Local/Temp/claude/C--Users-joelh-Downloads-development-atelier-platform/cab7fdd0-0c3e-44d6-ab71-1bd58e636a69/scratchpad/run-demo-local.sh
   ```

   Las credenciales de prueba (correos `shouyestefanero+demoowner@gmail.com` y `+demomechanic`) están en `demo-local.env`, en esa misma carpeta.

4. Espera esta línea en el log, unos 50 segundos:

   ```
   DemoWorkshopSeeder : Demo workshop seeded: tenant …, owner …, mechanic …
   ```

5. Comprueba las tildes:

   ```bash
   docker exec atelier-postgres psql -U postgres -d sqldemo -Atc "select name, legal_name from tenants"
   ```

   Debe decir `Taller Suárez Automotriz|Suárez Servicios Automotrices S.A.C.`.

**Resultado de la corrida anterior** (como referencia de lo que debe salir): 240 lecturas de telemetría, 1 alerta predictiva, 2 fallas DTC, 5 lotes de inventario, suscripción **MAX activa** hasta 2027-10-23, y el dueño y el mecánico en estado `active`.

### A2. Probar el inicio de sesión y las pantallas

Con la app de A1 corriendo (puerto 8099 dentro del contenedor), usa un contenedor de `curl` en la misma red:

```bash
docker run --rm --network container:atelier-postgres curlimages/curl -s -X POST http://localhost:8099/api/v1/auth/sign-in -H "Content-Type: application/json" -d '{"email":"shouyestefanero+demoowner@gmail.com","password":"<DEMO_ADMIN_PASSWORD de demo-local.env>"}'
```

La respuesta trae `token` y `plan` con `"tier": "MAX"` y `"status": "ACTIVE"`. Con ese token (`-H "Authorization: Bearer <token>"`) revisa:

| Endpoint | Qué debe mostrar |
|----------|------------------|
| `GET /api/v1/customers` | Carlos Ramírez Torres y Transportes Andinos S.A.C. |
| `GET /api/v1/inventory/items` | 5 repuestos; refrigerante y batería cerca del mínimo |
| `GET /api/v1/work-orders` | La orden #1 de la Hyundai H-1 (placa D4K719) |
| `GET /api/v1/iot/alerts/tenant` | La alerta predictiva de la H-1 |
| `GET /api/v1/iot/telemetry/vehicle/{vehicleId}/history` | 120 lecturas por vehículo (10 días) |
| `GET /api/v1/billing/subscriptions/me` | Plan Max activo |

Luego inicia sesión como mecánico (`+demomechanic`) y prueba `GET /api/v1/tasks/my`: deben salir las 2 tareas de la orden #1.

**Alternativa más simple:** si reinicias Docker Desktop, el puerto 5432 vuelve a exponerse. Así puedes correr el jar directo en tu máquina y probar todo desde Postman en `localhost`.

### A3. Suite completa de tests

Los tests usan H2 en memoria, así que no necesitan Docker ni tocan ninguna base real:

```bash
./mvnw -B test
```

Debe terminar en `BUILD SUCCESS`. Si algo falla, pásame la salida.

### A4. Commit, release 1.2.0 y push

El trabajo está sin commitear sobre `develop`. Con git flow:

1. Mueve los cambios a una rama feature (se llevan los archivos modificados):

   ```bash
   git flow feature start demo-workshop-seed
   ```

2. Agrega solo los archivos del trabajo. Revisa con `git status` que no entren `.env.prod`, `.env.dev` ni `firebase-service-account.json`:

   ```bash
   git add src/main/java/com/andeva/atelier/platform/billing src/main/java/com/andeva/atelier/platform/iam src/main/java/com/andeva/atelier/platform/demo src/main/resources/application.properties src/test/java/com/andeva/atelier/platform/billing/application/TrialSubscriptionReconcilerTest.java
   ```

3. Haz el commit:

   ```bash
   git commit -m "feat(demo): seed a complete demo workshop and reconcile missing trials at start-up"
   ```

4. Cierra la feature y saca la release:

   ```bash
   git flow feature finish demo-workshop-seed
   ```

   ```bash
   git flow release start 1.2.0
   ```

   ```bash
   git flow release finish -m "1.2.0" 1.2.0
   ```

5. Sube todo:

   ```bash
   git push origin main develop --tags
   ```

También puedes usar `/release` (tu skill), que hace los pasos 4 y 5.

**Qué va en 1.2.0:**
- `TrialSubscriptionReconciler`: al arrancar, da el trial de 14 días a todo taller que no tenga suscripción.
- `handleGrantComplimentaryPlan`: plan de cortesía activo sin cobro.
- `DemoWorkshopSeeder`: el taller demo. Está desactivado por defecto.

---

## Parte B: lo que solo puedes hacer tú

### B1. Variables en Render y despliegue

**Antes de empezar, revisa que:**
- 1.2.0 esté en `main` (A4).
- La base de producción tenga el plan **Max activo**. Tú ya corriste los scripts de planes; si faltara, el log dirá `No active plan of tier MAX in the catalog`.
- Uses tus **dos cuentas Google nuevas**: una para el dueño y otra para el mecánico. Con correos que no existen en producción, el seed crea el taller desde cero.
  - Si el correo del dueño ya hubiera registrado un taller (como tu prueba inicial), el seed no falla: **adopta** ese taller, le pone el plan Max y carga la demo ahí, sin tocar su contraseña.
  - El correo del mecánico sí debe ser nuevo.

**Pasos:**

1. En Render, entra al servicio del backend → **Environment** → **Add Environment Variable**.
2. Agrega estas variables:

   | Variable | Obligatoria | Ejemplo / valor por defecto |
   |----------|-------------|-----------------------------|
   | `DEMO_SEED_ENABLED` | Sí | `true` |
   | `DEMO_ADMIN_EMAIL` | Sí | la cuenta Google nueva del dueño |
   | `DEMO_ADMIN_PASSWORD` | Sí, para un taller nuevo | 8 a 64 caracteres; no reutilices una contraseña real. Se ignora si se adopta un taller existente |
   | `DEMO_MECHANIC_EMAIL` | Sí | la cuenta Google nueva del mecánico |
   | `DEMO_MECHANIC_PASSWORD` | Sí | 8 a 64 caracteres |
   | `DEMO_ADMIN_FIRST_NAME` / `DEMO_ADMIN_LAST_NAME` | No | `Pedro` / `Suárez` |
   | `DEMO_MECHANIC_FIRST_NAME` / `DEMO_MECHANIC_LAST_NAME` | No | `Andrés` / `Vílchez` |
   | `DEMO_WORKSHOP_NAME` | No | `Taller Suárez Automotriz` |
   | `DEMO_WORKSHOP_LEGAL_NAME` | No | `Suárez Servicios Automotrices S.A.C.` |
   | `DEMO_WORKSHOP_RUC` | No | `20612345679`; si cambias el RUC, debe pasar el dígito verificador de SUNAT |
   | `DEMO_BRANCH_LAT` / `DEMO_BRANCH_LNG` | No | `-12.0464` / `-77.0428` (centro de Lima). **Pon las coordenadas de tu universidad**: el marcado de asistencia del mecánico exige estar dentro del radio |
   | `DEMO_BRANCH_RADIUS` | No | `300` (metros) |

3. Guarda con **Save, rebuild, and deploy**. Si el auto-deploy desde `main` está activo, el push de A4 ya disparó un despliegue; guardar las variables dispara otro.
4. En **Logs**, busca:
   - `Seeding the demo workshop '…'`, que indica que empezó;
   - `Demo workshop seeded: …`, que indica que terminó bien;
   - `Demo workshop could not be seeded: <motivo>`, que indica un error. El backend sigue funcionando; copia el motivo y pásamelo;
   - `Demo seed enabled but … is missing`, que indica que falta una variable obligatoria;
   - `Seeding the demo into the existing workshop …`, que indica que el correo del dueño ya tenía un taller y se está usando ese.

**Lo que conviene saber:**
- **Llegan 2 correos reales.** El código de verificación al dueño y la invitación al mecánico. Ignóralos: el seed ya verifica al dueño y acepta la invitación con la contraseña de la variable.
- **Correos de clientes y proveedor.** Se crean como alias del correo del dueño (`dueño+carlos@…`, `+transportes`, `+proveedor`). Con Gmail esos alias llegan a tu bandeja; con otro proveedor puede que no.
- **Inicio de sesión con Google.** Además de la contraseña, el dueño y el mecánico pueden entrar con "Iniciar con Google": el backend busca la cuenta por correo, así que no se crea una cuenta duplicada.
- **Puedes dejar `DEMO_SEED_ENABLED=true`.** En cada reinicio ve que los vehículos demo ya existen y no hace nada. Ponerlo en `false` después también está bien.
- **El trial de los talleres sin suscripción.** En el mismo arranque, `TrialSubscriptionReconciler` le da el trial de 14 días a cualquier taller que se registró antes de que existieran los planes (como tu cuenta de prueba). Lo verás en el log como `Started the free trial of N workshop(s)`.
- **No hay "reset".** Si quieres rehacer la demo desde cero, hay que borrar ese taller de la base. Pídemelo y te preparo el script.
- **Datos con fecha.** La telemetría cubre los 10 días anteriores al despliegue, y el historial muestra por defecto los últimos 7 días. Si la clase es más de una semana después del despliegue, el gráfico saldrá vacío salvo que elijas un rango anterior. **Despliega la demo pocos días antes de la clase.**

### B2. Revisar la demo en producción

Entra como **dueño** (web) y revisa:

- [ ] El plan dice Max.
- [ ] En **Clientes** salen la persona natural y la empresa, cada una con su vehículo.
- [ ] En **Inventario** hay 5 repuestos con stock y un proveedor.
- [ ] En **Servicios** hay 3 servicios y 2 elevadores.
- [ ] La **orden #1** (Hyundai H-1) tiene 2 tareas asignadas al mecánico.
- [ ] La **telemetría** del Toyota Yaris se ve sana.
- [ ] La **telemetría** de la H-1 muestra sobrecalentamiento y batería baja, con las fallas P0217 y P0562 y una alerta predictiva.

Entra como **mecánico** (app móvil) y revisa:

- [ ] Ves tus 2 tareas.
- [ ] Puedes marcar asistencia estando dentro del radio de la sede.

Consejo para la clase: muestra primero el Yaris sano y luego la H-1. El contraste de las curvas de temperatura es lo más vistoso.

### B3. Rotar las claves de Google Maps y Firebase

Esas claves estuvieron expuestas, así que hay que reemplazarlas.

**Google Maps**
1. Google Cloud Console → **APIs y servicios** → **Credenciales**.
2. Crea una clave nueva y restríngela:
   - Android: por nombre de paquete y huella SHA-1;
   - web: por dominio;
   - en ambos casos, solo a las APIs de Maps que usas.
3. Reemplaza la clave vieja donde se use (Render, `.env.prod`, la app) y luego **elimina la vieja**.

**Firebase (cuenta de servicio)**
1. Consola de Firebase → **Configuración del proyecto** → **Cuentas de servicio** → **Generar nueva clave privada**.
2. Reemplaza el *Secret File* `firebase-service-account.json` en Render y tu copia local. Sigue en `.gitignore`: nunca lo commitees.
3. Google Cloud Console → **IAM** → **Cuentas de servicio** → la cuenta de Firebase → **Claves** → **elimina la clave vieja**.
4. Redespliega y comprueba que lleguen las notificaciones push.

### B4. Imagen del backlog de Jira

El texto del informe ya no tiene US07, pero `report/assets/product-backlog/product-backlog-atelier.png` (en atelier-report) todavía la muestra.

1. En Jira, elimina o archiva US07.
2. Vuelve a capturar el backlog con el mismo encuadre.
3. Reemplaza el PNG con el **mismo nombre**, así no cambia ninguna referencia del informe.

### B5. Commitear atelier-report y atelier-mobile

**atelier-report** (rama `develop`): un archivo modificado, `24-requirements-specification.md`. Contiene US07 eliminada, el backlog renumerado (1–74), los puntos del Sprint 2 en 55 y los totales en 122 / 202.

```bash
git -C ../atelier-report add report/chapters/20-requirements-development-and-software-solution-design/24-requirements-specification.md
```

```bash
git -C ../atelier-report commit -m "docs(requirements): drop US07 and renumber the product backlog"
```

Conviene meter el PNG de B4 en el mismo commit o en uno seguido.

**atelier-mobile:** la rama `main` todavía **no tiene ningún commit ni remoto**. Todo es nuevo: `README.md`, `AGENTS.md`, `CLAUDE.md`, `.gitignore` y `docs/`.

1. Haz el primer commit:

   ```bash
   git -C ../atelier-mobile add .
   ```

   ```bash
   git -C ../atelier-mobile commit -m "docs: requirements, API reference and Android architecture for the staff app"
   ```

2. Crea el repo en la organización de GitHub (`upc-pre-202620-1acc0238-4951-andeva`), añade el remoto y haz push. Si usas git flow, ejecuta `git flow init` para crear `develop`.

### B6. Trials que nunca vencen (decisión tuya)

**Problema:** `TenantSubscription.isAccessGranted()` devuelve `true` para `TRIALING` y `ACTIVE` sin mirar la fecha de fin, y no existe ningún proceso que expire suscripciones.

**Consecuencia:**
- El trial de 14 días es eterno en la práctica.
- El Max de cortesía de la demo (365 días) tampoco vence. Para la clase eso no molesta.

**Opciones:**
1. **Dejarlo así** hasta después de la clase. Es lo más seguro ahora.
2. **Corregirlo** (te lo implemento con tests):
   - que `isAccessGranted()` compare con el fin del periodo;
   - un job diario que pase los trials vencidos a `EXPIRED` y limpie la caché de planes.

   Con esto, la demo sigue funcionando un año.

### B7. Limpieza local

Cuando termines con las pruebas locales:

```bash
docker rm -f atelier-demo atelier-test
```

```bash
docker exec atelier-postgres psql -U postgres -c "drop database if exists sqldemo"
```

Los archivos `run-demo-local.sh` y `demo-local.env` viven en el scratchpad temporal de Claude y no están en ningún repo.
