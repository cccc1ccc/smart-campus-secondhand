# Smart Campus Second-hand Trading Platform

Course: Software Engineering (MUST) &nbsp;|&nbsp; Homework ID: Task1 — Project Proposal
Group Number: 11 &nbsp;|&nbsp; Group Name: SeekDeep
Team Leader: Wu Yutong (1240016186)

A campus marketplace for Macau University of Science and Technology students to publish
second-hand items, search and filter listings, post want-to-buy requests, and receive
automatic buyer-seller matches.

---

## 1. Team Profile

| Member | Name / Student No. | Main Functional Ownership | Initial Strength Focus |
| --- | --- | --- | --- |
| Member 1 | Wu Yutong / 1240016186 | User account and product listing management | Programming and organization |
| Member 2 | Wang Yike / 1240012305 | Product search, filtering, sorting and favorites | Programming and system design |
| Member 3 | Chen Moxiong / 1240008865 | Want-to-buy requests and buyer-seller matching | Programming and problem solving |
| Member 4 | Gao Shengzhe / 1240008949 | Transaction status and notifications | Documentation, integration and presentation |

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
| Member 1 (Wu Yutong) | F1, F2 | Complete the user account and product publishing/management workflow. |
| Member 2 (Wang Yike) | F3, F6 | Complete product search, filtering, sorting and the favorites function. |
| Member 3 (Chen Moxiong) | F4, F5 | Complete want-to-buy requests and the rule-based buyer-seller matching function. |
| Member 4 (Gao Shengzhe) | F7, F8 | Complete transaction status and in-system notification functions. |

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

The full proposal document is available at
[`docs/Task1_Project_Proposal_Campus_Secondhand_Platform.docx`](docs/Task1_Project_Proposal_Campus_Secondhand_Platform.docx).
