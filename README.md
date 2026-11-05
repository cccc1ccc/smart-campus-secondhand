# Smart Campus Second-hand Trading Platform

Course: Software Engineering (MUST) &nbsp;|&nbsp; Homework ID: Task1 — Project Proposal
Group Number: 11 &nbsp;|&nbsp; Group Name: SeekDeep
Team Leader: Wu Yutong (1240016186)

A campus marketplace for Macau University of Science and Technology students to publish
second-hand items, search and filter listings, post want-to-buy requests, and receive
automatic buyer-seller matches.

> **Version 1.0 (first implementation) is in this repository.** The running code, the
> rule-based matching engine, the demo data and the test report are described in
> [Section 11](#11-version-10--implementation). The interface ships in **English and 中文**
> with a switch in the header. Quick start:
> `mvn spring-boot:run` → <http://localhost:8080> → log in with student number
> `1240016186` / `Password123`.

---

## 1. Team Profile

| Member | Student No. | Main Functional Ownership | Initial Strength Focus |
| --- | --- | --- | --- |
| Wu Yutong | 1240016186 | User account and product listing management | Programming and organization |
| Wang Yike | 1240012305 | Product search, filtering, sorting and favorites | Programming and system design |
| Chen Moxiong | 1240008865 | Want-to-buy requests and buyer-seller matching | Programming and problem solving |
| Gao Shengzhe | 1240008949 | Transaction status and notifications | Documentation, integration and presentation |

All members participate in requirements analysis, design, implementation, testing,
documentation and presentation. Work is divided by **functional features**, not by
technical layers.

---

## 2. Problem Diagnosis

University students often own used textbooks, electronic devices and household items that
are still usable but no longer needed, while other students want to buy them below the new
price. Today this information is scattered across chat groups, social-media posts and
personal contacts, which leads to:

- fragmented information,
- inefficient searching,
- repeated manual communication,
- difficulty connecting a suitable buyer with a suitable seller.

## 3. Proposed Treatment

A single campus-focused platform that provides ordinary marketplace functions **and**
automatic data processing: the system continuously compares active product listings with
want-to-buy requests and reports potential matches by category, keywords and price range.
The project uses straightforward **rule-based** processing rather than complex AI.

### 3.1 Typical Use Scenario

Student A lists a used monitor at **MOP 800** under Electronics. Student B has posted a
want-to-buy request for a monitor with a budget of **MOP 600-900**. The system compares the
two records, marks them as a potential match, and notifies Student B. If both agree to
trade, the item status moves from Available → Reserved → Sold.

---

## 4. Functional Features

| ID | Feature | Description |
| --- | --- | --- |
| **F1** | User Registration and Login | Students create an account, log in and maintain basic profile information. |
| **F2** | Product Listing Management | Sellers publish a product with title, category, description, condition and price, and can edit or mark it as sold. |
| **F3** | Product Search, Filter and Sort | Buyers search by keyword and filter or sort results by category, price and condition. |
| **F4** | Want-to-buy Requests | Buyers publish a request containing desired item, category, budget and basic requirements. |
| **F5** | Automatic Buyer-Seller Matching | The system compares active listings with want-to-buy requests and reports potential matches. |
| **F6** | Favorites | Users save products they may want to purchase later. |
| **F7** | Transaction Status Management | Users track item status: Available / Reserved / Sold. |
| **F8** | Notifications | Users receive in-system notifications when a potential match is found or a relevant item status changes. |

### 4.1 Matching Rule (F5)

A pair is reported as a match when:

1. the categories are the same,
2. the keyword similarity is above a set threshold,
3. the asking price falls inside the buyer's stated budget range.

Candidates are ranked by a weighted score:

```
score = 0.40 * category + 0.35 * price overlap + 0.25 * condition
```

Only pairs scoring above **0.70** are shown to the user.

---

## 5. Plan of Work

| Week | Main Work | Expected Result |
| --- | --- | --- |
| Week 1 | Confirm project scope, user requirements, use cases and GitHub repository. | Approved proposal and requirement list |
| Week 2 | Design the main system structure, database entities and required UML diagrams. | Initial software design |
| Weeks 3-4 | Implement user accounts, product listings, search/filter and want-to-buy requests. | Basic marketplace functions |
| Weeks 5-6 | Implement automatic matching, favorites, transaction status and notifications. | Complete core functions |
| Week 7 | Integrate all functions and perform functional testing and bug fixing. | Stable integrated system |
| Week 8 | Prepare final documentation, demonstration data and presentation. | Final report and project demonstration |

## 6. Product Ownership

| Member | Owned Functional Features | Main Responsibility |
| --- | --- | --- |
| Wu Yutong | F1, F2 | Complete the user account and product publishing/management workflow. |
| Wang Yike | F3, F6 | Complete product search, filtering, sorting and the favorites function. |
| Chen Moxiong | F4, F5 | Complete want-to-buy requests and the rule-based buyer-seller matching function. |
| Gao Shengzhe | F7, F8 | Complete transaction status and in-system notification functions. |

The four members work in **two pairs**:

- **Pair A — Wu Yutong & Wang Yike** owns F1, F2, F3 and F6.
  Quality commitment: search and filtering results returned within **2 seconds** on a
  seeded database of 5,000 listings; publishing a listing reduced to at most five form
  fields and three clicks.
- **Pair B — Chen Moxiong & Gao Shengzhe** owns F4, F5, F7 and F8.
  Quality commitment: **top-3 precision ≥ 70%** on a labelled test set; a notification
  delivered within **60 seconds** of a qualifying match.

---

## 7. Success Criteria

The project is considered successful when:

1. a new user can register, publish a listing and publish a want-to-buy request within **five minutes**;
2. search and filtering return results that satisfy every selected condition in all prepared test cases;
3. on a hand-labelled test set of 50 request-listing pairs, matching reaches **recall ≥ 85%** and **top-3 precision ≥ 70%**;
4. a qualifying match produces a notification to both users within **60 seconds**;
5. search and filtering respond within **2 seconds** on 5,000 seeded listings.

## 8. Scope

**Included:** publishing and discovering items, want-to-buy requests, automatic matching,
favorites, transaction status and in-system notifications.

**Out of scope for the initial version:** online payment, delivery logistics, external
identity verification, and complex recommendation algorithms.

## 9. Technology (planned)

Java 17 + Spring Boot, MySQL 8, Maven, JUnit 5.

---

## 10. Contributions to This Report

This report was produced jointly by the four members of Group 11. Each member contributed
approximately **25%** of the total work.

| Member | Student No. | Contribution to This Report | Share |
| --- | --- | --- | --- |
| Wu Yutong | 1240016186 | Led the team; organised meetings and the task breakdown; wrote Section 1 (Team Profile) and Section 3 (Plan of Work and Product Ownership); compiled and finalised the complete report. | 25% |
| Wang Yike | 1240012305 | Wrote Section 2.1 (Problem Diagnosis) and Section 2.2 (Proposed Treatment); defined the F1-F8 functional feature list and the typical use scenario in Section 2.3; sketched the initial system structure. | 25% |
| Chen Moxiong | 1240008865 | Designed the rule-based matching method and the weighted scoring formula for F5; wrote Section 4 (Success Criteria) and the measurable quality commitments of Pair A and Pair B. | 25% |
| Gao Shengzhe | 1240008949 | Applied the MUST report template and formatting; created the GitHub repository and the project README; wrote Section 5 (Expected Result) and the project scope; proofread the final document. | 25% |

---

## 11. Version 1.0 — Implementation

**Stack:** Java 17 (compiled with `--release 17`), Spring Boot 3.5, Spring MVC + Spring Data JPA,
BCrypt password hashing, Thymeleaf-free static frontend (plain HTML/CSS/JS single-page app).
**Database:** MySQL 8 in deployment; a file-based H2 database is the default profile so the
project runs on any machine without installing a database server.

### 11.1 Requirement coverage

Every label required by the URS is present in English (the default), and the same screen can be
switched to Chinese at runtime — see [11.10](#1110-interface-language-english--中文).

| Req. | Status | Where |
| --- | --- | --- |
| REQ-1.1 Registration | Done | `AuthController#register`, `AuthService`, `AuthControllerTests`-free form validation in `RegisterForm` |
| REQ-1.2 Login, profile, account deletion, lockout | Done | `AuthService` (5 failures → 10 min lock), `UserService#changePassword/deleteAccount`, 2 h session timeout |
| REQ-2.1 Publish listing | Done | `ListingService#create`, 50 active listings per user, immediate matching trigger |
| REQ-2.2 Manage / delete listing | Done | `ListingService#update/delete`, edits only while Available, cascading cleanup |
| REQ-3.1 Search, filter, sort | Done | `SearchService` (JPA Specification, 20 per page, newest/price asc/price desc) |
| REQ-4.1 Want-to-buy requests | Done | `BuyRequestService`, 20 active requests per user, edit and close |
| REQ-5.1 Automatic matching | Done | `MatchingService` + `TextSimilarity` |
| REQ-6.1 Favorites | Done | `FavoriteService`, 200 per user, price/status change notifications |
| REQ-7.1 Transaction status | Done | `ListingService#changeStatus` + `listing_status_history` timeline |
| REQ-8.1 Notifications | Done | `NotificationService`, bell counter, mark one / mark all, 90-day purge job |

### 11.2 How to run

**Fastest way (Windows):** double-click **`Open Campus Loop.bat`** (or `start.bat`). It starts
the packaged jar on port 8080 and opens the browser automatically; closing the console window
stops the server. The Chinese walkthrough is in `启动说明.txt`.

```bash
# 1. build and run with the embedded H2 database (default, no installation needed)
mvn spring-boot:run

# 2. open the application
#    http://localhost:8080

# 3. run the unit tests
mvn test

# 4. deploy against MySQL 8 (URS section 7.1)
mysql -u root -p -e "CREATE DATABASE secondhand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mvn spring-boot:run -Dspring-boot.run.profiles=mysql   # DB_USER / DB_PASSWORD env vars
```

On the first start the seeder creates **16 students**, **5,000 listings** and **8 active
want-to-buy requests**, and runs the matcher so the Matches page is populated immediately.
Set `--app.seed.enabled=false` to start with an empty database.

### 11.3 Demo accounts (all use password `Password123`)

| Student number | Name | Useful for |
| --- | --- | --- |
| `1240016186` | Wu Yutong | Seller: monitor, mouse, CLRS textbook, desk, charger, headphones |
| `1240008865` | Chen Moxiong | Buyer with an active `monitor 24 inch hdmi ips` request |
| `1240008949` | Gao Shengzhe | Buyer with `algorithm textbook clrs` and `calculator ti-84` requests |
| `1240012305` | Wang Yike | Buyer with an active `mouse wireless logitech` request |

### 11.4 Code layout

```
src/main/java/com/must/secondhand
├── common/      ApiResponse, PageResult, BizException, GlobalExceptionHandler, session helpers
├── config/      AuthInterceptor, WebConfig, ScheduledJobs, SeedDataRunner
├── controller/  Auth, User, Listing, Search, BuyRequest, Match, Favorite, Notification, Meta, My
├── dto/         request forms (validation) and read models returned to the browser
├── model/       entities (User, Listing, BuyRequest, Favorite, MatchRecord, Notification,
│                ListingStatusHistory) and enums
├── repository/  Spring Data JPA repositories
└── service/     Auth, User, Listing, Search, BuyRequest, Matching, Favorite, Notification,
                 TextSimilarity
src/main/resources/static   index.html + css/app.css + js/app.js (single-page frontend)
src/test/java               MatchingServiceTest, TextSimilarityTest
```

### 11.5 REST API (session cookie authentication)

| Method & path | Purpose |
| --- | --- |
| `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` | Account session |
| `PUT /api/me`, `PUT /api/me/password`, `DELETE /api/me` | Profile, password, account deletion |
| `GET /api/search?keyword=&category=&minPrice=&maxPrice=&condition=&sort=&page=` | Search, filter, sort |
| `GET /api/listings/{id}` | Listing detail with viewer flags and status history |
| `POST /api/listings`, `PUT /api/listings/{id}`, `DELETE /api/listings/{id}` | Publish / edit / delete |
| `PUT /api/listings/{id}/status` | Available → Reserved → Sold |
| `GET /api/my/listings`, `GET /api/my/requests` | Owner pages |
| `POST /api/requests`, `PUT /api/requests/{id}`, `POST /api/requests/{id}/close` | Want-to-buy requests |
| `GET /api/matches` | Open matches with sub-scores |
| `GET /api/favorites`, `POST /api/favorites/{id}`, `DELETE /api/favorites/{id}` | Favorites |
| `GET /api/notifications`, `GET /api/notifications/summary`, `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all` | Notifications |
| `GET /api/meta` | Fixed option lists + published matching weights |

All responses use the envelope `{ "success": boolean, "message": string, "data": ... }`;
errors carry an HTTP status and a user-readable English message.

### 11.6 Matching rule as implemented

A pair becomes a **candidate** when all four gates hold:

1. same category,
2. keyword similarity ≥ **0.30**,
3. asking price inside the buyer's budget interval,
4. listing condition at least as good as the buyer accepts.

Keyword similarity is computed on lower-cased, punctuation-free tokens (stop words removed):

```
matched      = Σ per buyer keyword (1.0 exact, 0.5 substring, else 0)
coverage     = matched / number of buyer keywords
precision    = matched / number of listing tokens
similarity   = 0.75 * coverage + 0.25 * precision      (capped at 1.0)
```

Candidates are ranked with the documented weighted score, and only pairs **above 0.70** are
stored and shown:

```
score = 0.40 * category + 0.35 * price overlap + 0.25 * condition
```

`condition` is graded (exact accepted condition 1.0, one grade better 0.75, two 0.5, three
0.25); `price overlap` is 1.0 because an out-of-budget price is already rejected by the gate;
`category` is 1.0 because only same-category pairs are candidates. The Matches page prints
every sub-score so the result stays explainable.

### 11.7 Non-functional requirements

| Req. | Implementation | Measured on this machine |
| --- | --- | --- |
| PERF-1.2 search P95 < 2 s on 5,000 listings | Indexed specification query + pagination | **~18 ms** per filtered search request (5 runs) |
| SEC-1.1 salted password hash | BCrypt via `spring-security-crypto` | password never returned by any endpoint |
| SEC-1.2 personal data privacy | read models expose only display name | student number / e-mail only on `/api/me` |
| SEC-1.3 own data only | ownership checks in services + `AuthInterceptor` | 403 on foreign listing edits |
| SEC-1.4 lockout after 5 failures | `AuthService#login` | 10-minute lock, message with remaining minutes |
| CAP-3.1 limits | 50 listings, 20 requests, 200 favorites, 90-day notifications | enforced with explicit messages |

### 11.8 Tests

`mvn test` runs 14 JUnit 5 tests: six on text similarity and eight on the matching rule
(category gate, budget boundaries, condition gate, weak keywords, reserved/sold listings and
condition grading). All pass.

### 11.9 Known limitations in version 1

- No online payment, delivery or identity verification (as scoped in Section 8).
- One photo per listing, stored as a URL; no file upload.
- Notifications are in-system only; e-mail is not enabled.
- Listing edit history is not kept (only status history).
- The scheduled matching retry only re-checks items created in the last 24 hours, which keeps
  the 5,000 seeded listings out of every retry loop.
- Notification **text** is generated by the server in English; when the interface is switched to
  Chinese, the surrounding UI, dates and all other messages are translated but the notification
  body keeps the language it was created in. (Bilingual notification bodies are on the v2 list.)

### 11.10 Interface language (English / 中文)

The header carries an **EN / 中文** switch. The choice is stored in `localStorage`, applies to
every page, survives reloads, and a URL is never language-specific — one deployment serves both.

| Piece | Implementation |
| --- | --- |
| Strings | `static/js/i18n.js`: two dictionaries (`en`, `zh`) with ~170 flat keys, `t(key, params)` |
| Static shell | `data-i18n` / `data-i18n-title` attributes in `index.html`, filled by `applyStaticTranslations()` |
| Dynamic pages | every view in `app.js` renders through `t()`, so switching re-renders the current page immediately |
| Enum labels | `cat.*`, `cond.*`, `state.*`, `reqState.*` — the browser translates the enum value, not the server label |
| Dates | `toLocaleString('en-GB')` vs `toLocaleString('zh-CN')` |
| Server errors | `serverMessage()` maps the backend's English messages to Chinese (exact map plus regex rules for dynamic texts such as the lockout timer), falling back to the original text |
| Default | first visit follows `navigator.language` (`zh*` → Chinese), then the stored preference wins |

Backend contract is unchanged: the API still returns English labels and English error messages,
so the REST interface stays language-neutral and the translation lives entirely in the client.


---

The full proposal document is available at
[`docs/Task1_Project_Proposal_Campus_Secondhand_Platform.docx`](docs/Task1_Project_Proposal_Campus_Secondhand_Platform.docx).

Decisions, changes and open items are tracked in
[`docs/PROJECT_LOG.md`](docs/PROJECT_LOG.md).
