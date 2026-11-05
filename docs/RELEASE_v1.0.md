# Release v1.0 — First Implementation

| Item | Value |
| --- | --- |
| Project | Smart Campus Second-hand Trading Platform |
| Course | Software Engineering (MUST), Group 11 — SeekDeep |
| Version | **1.0.0 — first implementation** |
| Date | 2026-11-05 |
| Baseline | Task1 proposal + Task2 URS (REQ-1.1 … REQ-8.1) |
| Repository | https://github.com/cccc1ccc/smart-campus-secondhand (tag `v1.0`) |

## 1. What this version delivers

The complete F1–F8 feature set from the requirements specification, running as one Spring Boot
application with a browser frontend:

| Feature | Behaviour in v1.0 |
| --- | --- |
| F1 Accounts | Register (5 fields, inline validation), log in with e-mail or student number, salted BCrypt hashes, lockout after 5 failures for 10 minutes, 2-hour session |
| F2 Listings | Publish with 5 fields, edit while Available, delete with cascading cleanup, 50 active listings per user |
| F3 Search | Keyword over title and description, category, price range, multi-condition, sort by newest / price, 20 per page |
| F4 Want-to-buy | Publish budget range + acceptable condition, edit, close, 20 active requests per user |
| F5 Matching | Automatic on every create / edit / close; category, keyword, budget and condition gates; weighted score; Matches page with sub-scores |
| F6 Favorites | Save / remove, 200 per user, notification when a saved item changes price or status |
| F7 Status | Available → Reserved → Sold (Reserved can return to Available), timestamped history, matched buyers notified |
| F8 Notifications | Bell with unread counter, list with read / unread, mark all, 90-day retention job |

## 2. How to run

```bash
mvn spring-boot:run                 # H2 file database, http://localhost:8080
mvn test                            # 14 unit tests
mvn spring-boot:run -Dspring-boot.run.profiles=mysql    # MySQL 8 deployment
```

Demo accounts (password `Password123`): `1240016186` (seller), `1240008865` (monitor buyer),
`1240008949` (textbook + calculator buyer), `1240012305` (mouse buyer).

First start seeds 16 students, 5,000 listings and 8 active requests, then runs the matcher, so
the Matches page is populated without any manual setup.

## 3. Verification performed for this release

| Check | Result |
| --- | --- |
| `mvn test` | 14 / 14 tests pass (matching rule + text similarity) |
| Registration, duplicate student number, weak password | Rejected with specific English messages |
| Publishing a matching listing | Match created immediately, both users notified |
| Budget / condition / category gates | Rejected pairs are not stored |
| Available → Reserved → Sold | Applied, history recorded, open matches closed, buyers and favoriters notified |
| Sold → Available | Refused ("not allowed in version 1") |
| Editing another student's listing | HTTP 403 |
| Unauthenticated publish | HTTP 401 |
| Price change on a favorited listing | Favoriter notified with old and new price |
| Search on 5,000 listings (filtered, 5 runs) | 17–20 ms per request, far below the 2 s P95 target |
| Sold listing in search results | Never returned |
| UI pages (browse, detail, matches, requests, login) | Rendered and checked in a headless browser |

## 4. Deviations and decisions recorded for the report

1. **H2 as the default database.** The URS names MySQL 8; the code and profile for MySQL 8 are
   provided and documented, but the default profile uses a file-based H2 database so the demo
   runs without a database server (decision D9 in `PROJECT_LOG.md`).
2. **Keyword similarity definition.** The URS only requires "above a set threshold", so v1.0
   defines it as keyword coverage (0.75) plus listing precision (0.25) with a 0.30 threshold.
3. **Score sub-terms.** Category and price contribute their full weight for any candidate,
   because the gates already reject mismatching categories and out-of-budget prices; condition
   is graded, so the visible score ranges roughly 0.81–1.00. This is stated on the Matches page
   and in the README instead of hiding it.

## 5. Next version candidates

- Photo upload (multipart) instead of a photo URL field.
- Measurement evidence for Success Criteria 1–5 on a hand-labelled set of 50 pairs.
- E-mail notification through the campus SMTP service.
- Pagination on the Matches and Notifications pages when the data set grows.
- UML diagrams (use case, class, sequence) generated from the implemented code.
