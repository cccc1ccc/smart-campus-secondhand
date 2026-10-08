# Project Log

Smart Campus Second-hand Trading Platform — Group 11 (SeekDeep), MUST Software Engineering.
Purpose: record decisions, changes and open items so every task report can be written from
this file instead of from memory.

| Item | Value |
| --- | --- |
| Course / Homework | Software Engineering, Task1 — Project Proposal |
| Group | 11 — SeekDeep |
| Team leader | Wu Yutong (1240016186) |
| Repository | https://github.com/cccc1ccc/smart-campus-secondhand |
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
| A7 | Seed data: 5,000 listings for the P95 < 2s search benchmark | Chen Moxiong | not started |

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
