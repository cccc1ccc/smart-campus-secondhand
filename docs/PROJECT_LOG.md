# Project Log

Smart Campus Second-hand Trading Platform — Group 11 (SeekDeep), MUST Software Engineering.
Purpose: record decisions, changes and open items so every task report can be written from
this file instead of from memory.

| Item | Value |
| --- | --- |
| Course / Homework | Software Engineering, Task1 — Project Proposal |
| Group | 11 — SeekDeep |
| Team leader | Wu Yutong (1240016186) |
| Repository (personal) | https://github.com/cccc1ccc/smart-campus-secondhand |
| Repository (team, D2) | https://github.com/MUST-Workspace/D2-team11-project |
| Proposal document | `docs/Task1_Project_Proposal_Campus_Secondhand_Platform.docx` |
| Planned tech stack | Java 17 + Spring Boot, MySQL 8, Maven, JUnit 5 |

---

## 1. Timeline

| Date | Entry |
| --- | --- |
| 2026-10-06 | Topic confirmed: campus second-hand trading platform. Initial draft written with F1-F8 functional list and functional-feature-based ownership. |
| 2026-10-07 | Proposal formatted with the MUST report template; cover fields filled (leader, group no., team name). |
| 2026-10-08 | Gap review against Task1 Part C. Added: GitHub URL, pair-based ownership (Pair A / Pair B), qualitative quality properties, quantified Success Criteria, F5 matching rule, Week 1-8 plan, member names and student numbers. |
| 2026-10-08 | GitHub repository created and pushed; proposal URL corrected to point at the real repository. |
| 2026-10-08 | Course rules recorded: every report must list per-member contributions; Task2 requirement elicitation has three methods. Added Section 6 "Contributions to This Report". |
| 2026-10-08 | Member tables normalised: Member column = name, second column = student number only. |
| 2026-10-18 | Task2 URS written on Desktop (`Task2_User_Requirements_Specifications_Campus_Secondhand_Platform.docx`): F1-F8 expanded into REQ-1.1..REQ-8.1, plus PERF/SEC/AVA/CAP/DOM non-functional + domain requirements, Section 10 elicitation (Group 10 client meeting, user interviews, surrogate users), Section 11 per-member contributions. PDF export still pending (A1 rule: use Word Save As PDF). |
| 2026-11-05 | **Version 1.0 implemented.** Spring Boot 3.5 + Spring Data JPA backend (10 REST controllers, 8 services), plain JavaScript single-page frontend, file-based H2 as default profile plus a MySQL profile, seeder (16 students / 5,000 listings / 8 requests), 14 unit tests, README Section 11 and `docs/RELEASE_v1.0.md`. Git tag `v1.0`. |
| 2026-11-05 | One-click launcher added: `start.bat` (packaged jar, explicit port, browser auto-open via `open-browser.bat`), `Open Campus Loop.bat`, Chinese `启动说明.txt`; release zip now bundles the jar so it runs with only a JDK. |
| 2026-11-05 | **Bilingual interface (EN / 中文)** added: `static/js/i18n.js` holds two dictionaries and `t()`, header switch persists in `localStorage`, all pages re-render on switch, dates follow the locale, and `serverMessage()` maps backend errors to Chinese. Documented in README 11.10, RELEASE 2.1 and `启动说明.txt`. |
| 2026-11-05 | v1.0 pushed to **both repositories** (tag `v1.0`): personal `cccc1ccc/smart-campus-secondhand` and team `MUST-Workspace/D2-team11-project`. The team repository had an unrelated history containing the same proposal/URS documents, so it was synchronised with a merge commit (`-X ours`) instead of a force push: no team commit was lost and both trees are now identical. |

## 2. Key Decisions

| ID | Decision | Reason |
| --- | --- | --- |
| D1 | Work is split by **functional features**, not by technical layers. | Task1 Part C gives credit for feature ownership; layer-based split (one person does "the database") is explicitly discouraged. |
| D2 | Four members form **two pairs**: Pair A = Wu Yutong & Wang Yike (F1, F2, F3, F6); Pair B = Chen Moxiong & Gao Shengzhe (F4, F5, F7, F8). | Part C asks four members to be organised into two pairs, each declaring its functional and quality contribution. |
| D3 | The core value is **automatic buyer-seller matching (F5)**, not CRUD. | Task1 requires real automatic data processing beyond listing data in a table. |
| D4 | Matching uses a **transparent weighted rule**, not AI: `score = 0.40*category + 0.35*price overlap + 0.25*condition`, shown only above 0.70. | Keeps the project feasible for a third-year team and makes the result explainable and testable. |
| D5 | Out of scope for v1: online payment, delivery logistics, external identity verification, recommendation algorithms. | Keeps Week 1-8 achievable and shows boundary awareness. |
| D6 | Examples use **MOP** prices and MUST campus scenarios. | Local context reads as real; generic examples do not. |
| D7 | Language and tooling: English report body, GitHub as the project website. | Part C explicitly allows free hosting such as GitHub as the project URL. |
| D8 | Implementation stack: **Spring Boot 3.5 + Spring Data JPA + BCrypt**, frontend in plain HTML/CSS/JS served from `static/`. | Matches the planned Java stack in D7, needs no Node/npm build, and keeps every page a thin client of the REST API, which is easy to demo and to grade. |
| D9 | Default runtime database is a **file-based H2** database with a `mysql` profile for MySQL 8. | The URS asks for MySQL 8, but the demo machine has no MySQL server; H2 keeps `mvn spring-boot:run` a one-command start while the MySQL profile still satisfies the written requirement. |
| D10 | Keyword similarity is **coverage of the buyer's keywords (0.75) plus listing-text precision (0.25)** rather than a symmetric text distance. | The URS only says "keyword similarity above a threshold"; coverage matches the question the buyer asks ("does this listing contain what I want") and stops long descriptions from diluting a good match. |
| D11 | Matching **re-runs on every create/edit/close**, with a scheduled retry limited to items created in the last 24 hours. | Satisfies REQ-5.1 without re-scoring 5,000 seeded listings every ten minutes. |
| D12 | Seeded bulk listings are assigned to the eight "other student" accounts and backdated. | Keeps demo accounts under the 50-active-listing limit and out of the scheduled retry window. |
| D13 | The interface is **bilingual (English default + 中文)** and the translation lives entirely in the browser (`i18n.js`); the API keeps returning English labels and messages. | The URS asks for English labels, while the demo audience is Chinese-speaking. Client-side translation keeps the REST contract unchanged and lets the switch work without a reload or a second deployment. |

## 3. Member Ownership (fixed)

| Member | Student No. | Features | Strength focus |
| --- | --- | --- | --- |
| Wu Yutong | 1240016186 | F1, F2 | Programming and organization |
| Wang Yike | 1240012305 | F3, F6 | Programming and system design |
| Chen Moxiong | 1240008865 | F4, F5 | Programming and problem solving |
| Gao Shengzhe | 1240008949 | F7, F8 | Documentation, integration and presentation |

Pair A = Wu Yutong & Wang Yike; Pair B = Chen Moxiong & Gao Shengzhe.

## 4. Course Rules To Keep In Mind

1. **Every report must list the contributions of each member to that specific report.** Copy the
   template in Section 7 and fill it once per report.
2. **Task2 requirement elicitation** — three methods:
   - Method 1 (**mandatory**): paired groups act as each other's client. Group 11's client is
     **Group 10**; Group 11 is the client of **Group 12**.
   - Method 2 (**strongly recommended**): interview real potential users, i.e. MUST students who
     actually buy or sell second-hand items.
   - Method 3 (optional): team members act as surrogate users.

## 5. Open Items / Backlog

| # | Item | Owner | Status |
| --- | --- | --- | --- |
| A1 | Export the proposal to PDF with Word (LibreOffice is not installed on this machine) | Wu Yutong | pending |
| A2 | Check that the contribution percentages in the report match reality before each submission | whole team | pending |
| A3 | Task2: contact Group 10, arrange the elicitation meeting | Wu Yutong | not started |
| A4 | Task2: interview 3-5 real MUST buyers/sellers, keep notes | Wang Yike | not started |
| A5 | Week 2: UML use-case / class / sequence diagrams | Wang Yike | not started |
| A6 | Week 2: freeze interfaces between Pair A and Pair B (search, priceStats, findMatches, notify) | Wu Yutong | not started |
| A7 | Seed data: 5,000 listings for the P95 < 2s search benchmark | Chen Moxiong | **done in v1.0** (seeded automatically on first start, search measured at ~18 ms) |
| A8 | v1.0: measurement log for Success Criteria 1-5 (five-minute onboarding, recall/precision on 50 labelled pairs) | whole team | pending |
| A9 | v1.0: photo upload instead of a photo URL | not assigned | backlog |
| A10 | v1.0: e-mail notification via the host SMTP service (optional in the URS) | not assigned | backlog |
| A11 | v1.0: bilingual notification bodies (store a message key + parameters instead of a fixed English sentence) | not assigned | backlog |

## 6. Definition Of Done (Proposal Stage)

- [x] Part C items present: title + group no., GitHub URL, team profile, description + feature list, plan + product ownership
- [x] Two-pair ownership written with qualitative properties
- [x] Quantified Success Criteria
- [x] Member names and student numbers filled in every member table
- [x] Per-report contributions section added
- [x] Repository public and reachable

## 7. Contribution Template (copy once per report)

| Member | Student No. | Contribution to this report | Share |
| --- | --- | --- | --- |
| Wu Yutong | 1240016186 | | % |
| Wang Yike | 1240012305 | | % |
| Chen Moxiong | 1240008865 | | % |
| Gao Shengzhe | 1240008949 | | % |

## 8. Environment Notes

- No LibreOffice installed: convert `.docx` to PDF with **Word -> Save As -> PDF**.
- GitHub CLI (portable): `C:\Users\ROG\.workbuddy\binaries\gh\bin\gh.exe`, already authenticated
  as `cccc1ccc`, and registered as the git credential helper (`gh auth setup-git`).
- Local clone: `C:\Users\ROG\Desktop\smart-campus-secondhand`, branch `main`.
- If `git push` fails with `CONNECT tunnel failed 502`, the sandbox proxy is at fault; run
  `env -u http_proxy -u https_proxy -u HTTP_PROXY -u HTTPS_PROXY git push`.
- When the proposal document is open in the editor, edit it through the editor interface;
  its table tools use **1-based** row and column indices (row 1 is the header), and changes
  must be saved before other tools can read them.
- Build tool: Maven 3.9.9 at `D:\Dev\tools\maven\apache-maven-3.9.9\bin\mvn`; JDK 25 on
  `PATH` (the project compiles with `--release 17`).
- **The shell exports `SERVER__PORT`, which overrides `server.port`.** Start the app with
  `env -u SERVER__PORT -u SERVER_PORT mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"`,
  otherwise Spring Boot binds to that port and fails with "port already in use".
- Runtime data lives in `./data/` (H2 files, git-ignored). Delete the folder to force a fresh
  seed on the next start.
- Frontend files are served with caching disabled (`spring.web.resources.cache.period=0`), so
  edits to `app.js` / `app.css` show up on reload without rebuilding.
- **Stop the running application before `mvn package`.** On Windows the running jar holds a file
  lock, the repackage step fails with `PluginExecutionException`, and `target/*.jar` is left as a
  thin ~150 KB jar instead of the ~56 MB fat jar.
- Interface strings live only in `src/main/resources/static/js/i18n.js`. Adding a string means:
  add the key to both the `en` and `zh` dictionaries, then use `t('key')` in `app.js`; static
  header/footer text uses `data-i18n="key"` in `index.html`.
