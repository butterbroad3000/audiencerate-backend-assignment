# AudienceRate Backend — Architecture & Decisions

## Оглавление
1. [Обзор проекта](#обзор-проекта)
2. [Стек и почему](#стек-и-почему)
3. [Слои приложения](#слои-приложения)
4. [DI: Guice + Jersey](#di-guice--jersey)
5. [Три пула HikariCP](#три-пула-hikaricp)
6. [API-дизайн](#api-дизайн)
7. [Безопасность данных](#безопасность-данных)
8. [Транзакции](#транзакции)
9. [Кросс-контекстная композиция](#кросс-контекстная-композиция)
10. [Обработка ошибок](#обработка-ошибок)
11. [OpenAPI](#openapi)
12. [Graceful shutdown](#graceful-shutdown)
13. [Тесты](#тесты)
14. [Известные ограничения](#известные-ограничения)

---

## Обзор проекта

AudienceRate Backend — REST API для DMP-платформы (Data Management Platform). Работает с тремя независимыми PostgreSQL-базами (`profiles`, `segments`, `activations`), каждая из которых представляет свой bounded context. Приложение агрегирует данные из всех трёх баз в единое API.

**Ключевая особенность:** три базы требуют три отдельных пула соединений HikariCP. Межбазовые JOIN'ы в SQL невозможны — вся кросс-контекстная композиция выполняется в Java-коде.

---

## Стек и почему

| Компонент | Выбор | Причина |
|---|---|---|
| **Язык** | Java 21 | LTS-релиз, records, virtual threads (опционально) |
| **HTTP-сервер** | Jetty 12 (embedded) | Лёгкий, не требует установки, запускается из `main()` |
| **REST-фреймворк** | Jersey 3.x (JAX-RS) | Стандарт Jakarta REST, аннотации `@Path/@GET/@POST` |
| **DI** | Google Guice 7 | Лёгкий, без магии classpath-scanning, явная конфигурация модулей |
| **Пул соединений** | HikariCP 6 | Самый быстрый JDBC-пул, минимум конфигурации |
| **JSON** | Jackson 2.18 | Стандарт, поддержка records, интеграция с Jersey |
| **OpenAPI** | Swagger 2.2 | Генерация спеки из аннотаций, `/openapi.json` |
| **Логирование** | SLF4J + Logback | Стандарт, настройка через XML |
| **Сборка** | Maven + shade-plugin | Fat JAR: `java -jar target/app.jar` |
| **Тесты** | JUnit 5 + Mockito + Instancio | Параметризованные тесты, моки, генерация тестовых объектов |

**Что не используется и почему:**
- **Spring Boot** — задание требует ручной сборки стека, показать понимание DI, Jetty, Jersey
- **ORM (Hibernate/JPA)** — задание требует plain JDBC, показать безопасную работу с SQL
- **Lombok** — Java 21 records покрывают 90% случаев, остальное — ручные геттеры/сеттеры

---

## Слои приложения

```
HTTP-запрос
  ↓
Resource (JAX-RS)   ← аннотации @Path, @GET, @POST; только маршрутизация
  ↓
Service              ← бизнес-логика, валидация, кросс-контекст
  ↓
DAO                  ← SQL-запросы через PreparedStatement
  ↓
HikariCP (3 пула)    ← по одному на каждую базу
  ↓
PostgreSQL           ← profiles / segments / activations
```

**Почему три слоя, а не два:**
- Resource — тонкая обёртка, не содержит логики. Только принимает запрос, вызывает сервис, возвращает Response.
- Service — вся логика здесь. Можно тестировать без HTTP.
- DAO — только SQL. Можно заменить на JDBI/MyBatis не трогая сервисы.

---

## DI: Guice + Jersey

**Проблема:** Jersey по умолчанию использует свой DI-контейнер HK2. Guice с ним несовместим.

**Решение:** создаём все ресурсы через Guice Injector и регистрируем их в Jersey вручную:

```java
Injector injector = Guice.createInjector(new AudienceRateModule());
ResourceConfig rc = new ResourceConfig();
rc.register(injector.getInstance(HealthResource.class));  // Guice → Jersey
```

**Структура модулей:**

```
AudienceRateModule          ← верхний уровень
  ├── DatabaseModule        ← @Provides для 3 HikariDataSource
  ├── DaoModule             ← байндит DAO → правильный пул
  └── ServiceModule         ← байндит сервисы → DAO
```

**Почему три модуля, а не один:**
- Каждый модуль — отдельный слой, зависимости явные
- В тестах можно подменить DaoModule на моки
- Порядок загрузки естественный: пулы → DAO → сервисы

---

## Три пула HikariCP

**Почему три пула:** PostgreSQL-соединение жёстко привязано к одной базе. Нельзя сделать `USE profiles; USE segments;` в одном соединении. Нужны три независимых пула.

**Как роутится:** кастомные `@BindingAnnotation`:

```java
@ProfilesDb   DataSource → jdbc:.../profiles
@SegmentsDb   DataSource → jdbc:.../segments
@ActivationsDb DataSource → jdbc:.../activations
```

Каждый DAO в конструкторе получает DataSource с нужной аннотацией:

```java
public SegmentDao(@SegmentsDb DataSource ds) { ... }     // → segments
public DataSourceDao(@ProfilesDb DataSource ds) { ... }   // → profiles
public ActivationDao(@ActivationsDb DataSource ds) { ... } // → activations
```

**Конфигурация пулов (из переменных окружения):**

| Параметр | Значение | Зачем |
|---|---|---|
| `poolMaxSize` | 5 | Достаточно для учебного проекта |
| `poolMinIdle` | 1 | Держим одно готовое соединение |
| `connectionTimeoutMs` | 5000 | Быстрый фейл если база недоступна |
| `idleTimeout` | 10 мин | Освобождаем неиспользуемые |
| `maxLifetime` | 30 мин | Принудительная ротация |
| `connectionTestQuery` | `SELECT 1` | Проверка при выдаче из пула |

---

## API-дизайн

**Конвенции ответов:**

- Список: `{ "data": [...], "pagination": { "page", "pageSize", "total", "totalPages" } }`
- Один объект: `{ "data": {...} }`
- Ошибка: `{ "error": { "code": 400, "message": "...", "details": { "name": "too short" } } }`

**Почему именно так:**
- Единый конверт — клиенту не нужно гадать, где данные а где ошибка
- `details` — машиночитаемая карта ошибок по полям (фронтенд может подсветить конкретное поле)

**Pagination — offset-based (почему не keyset):**
- Для 36 сегментов offset-пагинация работает мгновенно
- Keyset/cursor сложнее в SQL и не даёт выигрыша на таких объёмах
- При росте до миллионов записей — да, keyset обязателен

**Pagination — серверная (SQL):**
- `LIMIT ? OFFSET ?` с предварительным `COUNT(*)` — один запрос для данных, один для общего количества
- Сортировка через whitelist: только `name`, `audienceSize`, `updatedAt`, `matchRate` (с префиксом `-` для DESC)
- Фильтрация и поиск: `WHERE ... ILIKE ...` с динамической сборкой SQL (безопасно через `PreparedStatement`)

**Почему в API используется `camelCase`, а в БД `snake_case`:**
- JSON-стандарт де-факто — camelCase (JavaScript, фронтенды)
- PostgreSQL — snake_case (SQL-стандарт)
- Jackson и так мапит getSegmentId() → segmentId, ручные аннотации не нужны

---

## Безопасность данных

**SQL Injection:**
- Все запросы через `PreparedStatement` с `ps.setString()/setLong()/setObject()`
- Ни одной строковой конкатенации пользовательского ввода в SQL
- Динамическая сборка `WHERE` использует параметры `?`, а не подстановку строк
- Сортировка проверяется через whitelist `ALLOWED_SORTS`

**Connection leaks:**
- Все соединения открываются через `try-with-resources`
- Нет ручных `conn.close()` — автоматически при выходе из блока
- Для транзакций: соединение открывается в сервисе и передаётся в DAO, закрывается сервисом

**Secrets:**
- Все креденшелы из переменных окружения (`AppConfig.fromEnv()`)
- `.env.example` содержит тестовые креденшелы
- `.env` в `.gitignore` — реальные пароли не коммитятся

---

## Транзакции

Транзакции управляются внутри DAO через `executeInTransaction()` — общий хелпер, который открывает соединение, ставит `autoCommit=false`, выполняет лямбду, коммитит или откатывает:

```java
// SegmentDao — и create, и update используют один и тот же механизм
public Segment create(...) {
    return executeInTransaction(conn -> {
        Segment segment = insert(conn, ...);
        insertTags(conn, ...);
        insertDataSources(conn, ...);
        return findById(id).orElse(segment);
    });
}
```

**Почему единый подход, а не два разных:**
- `create()` и `update()` раньше управляли транзакциями по-разному (сервис vs DAO)
- Теперь оба используют `executeInTransaction()` — один источник истины
- Для одного запроса (activation create) транзакция управляется на уровне сервиса

**Где используются транзакции:**
- `POST /api/segments` — segment + tags + data_sources (внутри `SegmentDao.create()`)
- `PATCH /api/segments/{id}` — update + delete/insert tags + delete/insert data_sources (внутри `SegmentDao.update()`)
- `POST /api/activations` — activation insert (управляется в `ActivationService.create()`)

---

## Кросс-контекстная композиция

**Проблема:** сегмент лежит в `segments` базе, активация — в `activations` базе. SQL JOIN невозможен.

**Решение:** композиция в Java-коде:

```
1. SegmentDao.findById(id)           → segments pool  (проверить что сегмент есть)
2. ActivationDao.list(segmentId)     → activations pool (получить активации)
3. DestinationDao.findAll()          → activations pool (получить все destinations)
4. Обогащение в памяти              → activation.setDestination(dest)
```

**`GET /api/segments/{id}/activations`:**
- Сначала `segments` база: проверить что сегмент существует (404 если нет)
- Потом `activations` база: найти все активации для этого segmentId
- Обогатить каждую активацию полным объектом Destination

**`POST /api/activations` (кросс-контекст):**
- `segments` база: проверить что segmentId существует
- `activations` база: проверить что destinationId существует
- `activations` база: создать активацию

**Почему destinations грузятся все (`findAll()`) а не по ID:**
- Destinations всего 7 записей — полная загрузка дешевле чем 7 отдельных запросов
- При росте до тысяч — сделать `findByIds(Set<String> ids)`

---

## Обработка ошибок

**Три типа исключений:**

| Исключение | HTTP-код | Когда |
|---|---|---|
| `ValidationException` | 400 | name < 3 символов, status не из списка, segmentId не найден |
| `NotFoundException` | 404 | сегмент/активация не найдена |
| `Exception` (catch-all) | 500 | всё остальное (SQLException и т.д.) |

**Почему разделены ValidationException и NotFoundException:**
- 400 = «ты прислал не то» (можно исправить запрос)
- 404 = «этого нет» (другой ID или не создано)
- Фронтенд по коду понимает что делать: показать ошибку валидации или редирект на 404

**Особый случай — Jersey 404:**
Jersey выбрасывает `jakarta.ws.rs.NotFoundException` когда URL не соответствует ни одному ресурсу. Без отдельного маппера он попадал бы в GenericExceptionMapper и логировался как ERROR. Добавлен `JerseyNotFoundExceptionMapper` — возвращает 404 молча, без стектрейса.

**Формат ошибки:**
```json
{
  "error": {
    "code": 400,
    "message": "Validation failed",
    "details": {
      "name": "Name must be at least 3 characters",
      "status": "Status must be one of: active, draft, archived"
    }
  }
}
```

---

## OpenAPI

**Как работает:**
- Ресурсы аннотированы `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`
- Swagger JAX-RS сканирует аннотации при первом запросе к `/openapi.json`
- Результат кешируется (double-checked locking) — последующие запросы мгновенные

**Почему не Swagger UI:**
Swagger UI требует либо CDN (интернет), либо бандл в classpath. Задание просит только `/openapi.json` — его достаточно для кодогенерации клиентов.

---

## Graceful shutdown

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    server.stop();                          // перестать принимать запросы
    ((HikariDataSource) profilesDs).close();  // закрыть все соединения
    ((HikariDataSource) segmentsDs).close();
    ((HikariDataSource) activationsDs).close();
}));
```

**Почему важно:** без shutdown hook соединения с базой останутся висеть пока ОС не прибьёт процесс по таймауту.

---

## Тесты

**43 теста, 4 тестовых класса:**

| Класс | Что тестирует | Моки |
|---|---|---|
| `SegmentValidatorTest` | Валидация name (null, короткий, длинный), status | Без моков (pure logic) |
| `SegmentServiceTest` | CRUD, pagination, trend, транзакции, rollback | Mockito (DAO + DataSource) |
| `ActivationServiceTest` | Кросс-контекст: create с segment/destination валидацией, list, rollback | Mockito (3 DAO + DataSource) |
| `HealthServiceTest` | Все пулы up, один down, все down | Mockito (3 DataSource) |

**Инструменты:**
- **JUnit 5** — `@Test`, `@ParameterizedTest`, `@CsvSource`
- **Mockito** — `@Mock`, `when/thenReturn`, `verify`, `doThrow`
- **Instancio** — генерация тестовых объектов (`Instancio.of(Segment.class).create()`)

**Что не тестируется и почему:**
- DAO — требуют реальной базы (нужен Testcontainers — можно добавить следующим шагом)
- Resources — тонкие обёртки без логики; тестировать нечего
- Интеграционные тесты — требуют поднятого Docker Compose

---

## Известные ограничения

1. **ID-генерация** — `SELECT id FROM segments ORDER BY id DESC LIMIT 1` + инкремент. При конкурентных запросах возможна гонка. Правильно: PostgreSQL `SEQUENCE` или UUID.

2. **Destinations кешируются per-request** — `findAll()` вызывается при каждом запросе. Для 7 записей ок, для тысяч — добавить кеш.

4. **Offset-пагинация** — при большом offset'е (`page=10000`) запрос сканирует все предыдущие страницы. Keyset/cursor-пагинация решила бы это.

5. **Нет observability** — нет request logging, метрик пулов, структурированного логирования. Добавить: Jersey `ContainerRequestFilter` + Logback JSON encoder + HikariCP metrics.

6. **Нет resilience** — если одна база недоступна, `/api/overview` возвращает 500. Правильно: деградация с частичными данными и `warnings` в ответе.

7. **Нет миграций** — схема БД управляется SQL-скриптами в `databases/init/`. Flyway/Liquibase дали бы версионирование и откат.
