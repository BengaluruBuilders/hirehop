# TailorMyResume research 04: Market size, hiring trends, and the recruiter side

Date: 2026-09-30
Scope: market size, hiring trends 2025-2026, recruiter and ATS behavior, regulation, tailwind versus headwind.
Method: about 45 web sources. Each source has a grade in the source list.

Source grades:
- A = government or primary data.
- B = reputable press, academic paper, law-firm note, or listed company.
- C = vendor blog, forum, anecdote, or survey run by a seller of the product.

Style note: this file uses plain short sentences. Numbers marked "assumption" are mine. Numbers marked "derived" are my arithmetic on cited data.

---

## 1. Summary

1. The India user pool is large. About 1.1 crore (11 million) students pass out of higher education each year (AISHE 2023-24, grade A). About 97 lakh (9.7 million) of them finish a degree (UG or PG).
2. The paying market is small. At India prices (Rs 300 to Rs 700 per pack), the yearly revenue for a consumer-only app is in the low crores of rupees, not tens of crores. See section 3.
3. Entry-level hiring is split. Non-IT sectors and GCCs grow. IT services cut fresher intake sharply since FY22. Naukri shows fresher hiring up 15% year on year in August 2026.
4. ATS do not auto-reject on resume text. They parse, rank, and match. Humans decide. No major ATS detects AI authorship. The real problems for candidates are generic text, parse errors, and knockout questions.
5. Recruiters distrust AI-written mass applications. The distrust targets generic and fake content, not AI use as such. This favors an honest, specific, human-edited tool.
6. Rules on AI in hiring target employers and vendors, not candidate-side tools. The DPDP Act still applies to TailorMyResume because a resume is personal data. Main DPDP duties start 13 May 2027.
7. The biggest market risk is low willingness to pay. Free ChatGPT, Naukri's own AI resume maker, and cheap Indian resume sites already serve the same need.

---

## 2. Market size inputs

### 2.1 Graduates per year in India (grade A)

Source: AISHE 2023-24, Ministry of Education. The report was published in July 2026. It covers 59,553 responding institutions (1,278 universities, 46,468 colleges, 11,787 standalone institutions).

| Item | Value |
|---|---|
| Total enrolment 2023-24 | about 4.5 crore |
| GER (age 18-23) | 30.0% |
| Total pass-outs (out-turn) 2023-24 | 1.10 crore, all levels |
| Share at UG / PG / Diploma | 71.0% / 17.5% / 8.3% |
| UG pass-outs (derived) | about 78 lakh |
| PG pass-outs (derived) | about 19 lakh |
| Diploma pass-outs | 9.1 lakh |
| B.A. | 24.4 lakh (key results) or 28.6 lakh (chapter text) |
| B.Sc. | 11.5 lakh or 13.1 lakh |
| B.Com. | 9.6 lakh or 10.1 lakh |
| B.E. / B.Tech | 8.2 lakh |
| MBA | 2.8 lakh |
| M.A. / M.Sc. / M.Com. | 7.5 / 3.6 / 1.8 lakh |
| UG enrolment by discipline | Arts 32.1%, Science 13.5%, Engineering 12.9%, Commerce 12.0% |
| PG enrolment by discipline | Social Science 18.6%, Management 18.2%, Science 15.1% |

Data quality note: the AISHE report gives two different figures for B.A., B.Sc., and B.Com. (key results versus chapter text). I show both. The model below uses the lower figure.

Cross-checks:
- AICTE engineering: 12.53 lakh B.Tech enrolment in 2024-25, the highest in eight years. Approved intake for 2025-26 is 15.98 lakh seats (grade B, press reports of AICTE data).
- Computer science is the biggest branch (3.90 lakh enrolled in 2024-25). Civil and mechanical placement rates are far lower than CS (grade B).

### 2.2 Job seekers aged 20-27

No official count of "active job seekers aged 20-27" exists. I derive a range.

- PLFS 2025 (grade A): employed 61.6 crore, unemployment rate 3.1%, LFPR 59.3%, youth (15-29) unemployment 9.9%, urban youth 13.6%, NEET share of youth about 25%.
- Derived: labour force is about 63.6 crore, so about 2.0 crore people are unemployed. Youth were 83% of the unemployed in the earlier ILO report. That gives about 1.6 crore unemployed youth aged 15-29.
- ILO/IHD India Employment Report 2024 (grade A): people with secondary or higher education were 65.7% of unemployed youth in 2022. Apply that share. About 1.0 crore educated unemployed youth (derived, mixes 2022 and 2025 data).
- Graduate unemployment: Forbes India reports 11.2% for graduates in 2025 (grade B). The ILO report gave 29.1% for graduate youth in 2022. Another summary quotes 22.7%, and I could not verify it. Treat graduate unemployment as "between 11% and 29%, depending on age group and year".
- My estimate of degree-holding active seekers aged 20-27: 6 to 10 million at any time (assumption, based on the derived figures plus employed people who search).

### 2.3 Android share and device base in India

- Android share of mobile web traffic in India: 95.2% (StatCounter, grade B). iOS is about 5-7%.
- Active internet users: 958 million in 2025 (IAMAI-Kantar, grade B).
- Smartphone users: about 700+ million (grade C; several press sources). Use as a rough figure only.
- Implication: the Android-only choice loses about 5-8% of India's reach. It loses far more in the US (iOS-heavy). This matters for any US expansion.

### 2.4 US comparison

- NCES (grade A): 2.0 million bachelor's and 880,200 master's degrees in 2021-22. About 2.9 million new graduates a year.
- NY Fed (grade A): recent-graduate (age 22-27) unemployment about 5.6% and underemployment about 42% in Q2 2026.
- NACE Job Outlook 2026 spring update (grade A/B): employers plan a 5.6% rise in Class of 2026 hiring (185 respondents). Earlier projections were flat.

---

## 3. TAM / SAM / SOM (bottom-up, India first)

Currency assumption: Rs 88 per US dollar (assumption; verify before use).

### 3.1 Price assumptions

- Application pack: Rs 199 to Rs 499, one-off, for one job application set.
- Active-job-search plan: Rs 499 to Rs 999 for 30 to 90 days.
- Price reference (grade C): ResumeGyani, an Indian resume site, sells a pro plan at Rs 299 for 7 days. Teal (US) sells $13 per week. Rezi (US) sells $29 per month.
- Model uses Rs 500 blended revenue per paying user per year for TAM and SAM. SOM scenarios use Rs 399 to Rs 699.

### 3.2 Table

| Layer | Definition | Users | Value per year | Key assumptions |
|---|---|---|---|---|
| TAM India | All UG + PG + diploma pass-outs, each year, each buys once | 1.0 crore (10 million) | Rs 500 crore (about $57M) | AISHE pass-outs (rounded down); Rs 500 per user; 100% purchase, which is an upper bound |
| TAM US (comparison) | All bachelor's + master's graduates | 2.9 million | about $87M | NCES 2021-22; $30 per user per year |
| TAM total (India + US) | Sum | about 12.9 million | about $144M | Excludes experienced job seekers on purpose |
| SAM India | Graduates from engineering, commerce, science, MBA, M.Sc., M.Com. who search for private-sector jobs, use Android, and read English resumes; plus an equal number of early-career switchers and unplaced earlier graduates | about 3.6 million per year | about Rs 178 crore (about $20M) | Base cohort 38 lakh (B.E./B.Tech 8.2 + B.Com 10.1 + B.Sc 11.5 + MBA 2.8 + M.Sc 3.6 + M.Com 1.8). 60% actively search. 92% Android. 85% English-comfortable. Result about 1.8M. Double it for carry-over and switchers (assumption) |
| SOM India, year 3, conservative | 1% of SAM users, 3% pay, Rs 399, 1 purchase | about 36,000 users, 1,070 payers | about Rs 4 lakh (about $5K) | Weak distribution, free-tool competition |
| SOM India, year 3, base | 3% of SAM users, 5% pay, Rs 499, 1.5 purchases | about 107,000 users, 5,350 payers | about Rs 40 lakh (about $45K) | Campus ambassadors, YouTube and Instagram content |
| SOM India, year 3, optimistic | 6% of SAM users, 8% pay, Rs 699, 2 purchases | about 214,000 users, 17,100 payers | about Rs 2.4 crore (about $270K) | Strong word of mouth, institution deals |

Notes on the table:
- I excluded B.A. graduates (24 lakh or more) from SAM. Many of them target government or non-corporate jobs. Add them only if the product serves them well.
- The paid-conversion benchmark: Naukri reports "paid penetration" of 2.6% for its job-seeker products (grade B, Storyboard18 summary of Info Edge Q1 FY27; the exact base is not stated). I use 3% to 8% for a focused pack product, which is more optimistic than Naukri.
- To reach Rs 1 crore of yearly revenue in the base case, TailorMyResume needs about 13,000 paying users. That needs about 270,000 active users at 5% conversion.
- Result: a consumer-only India model is a small business at these prices. Bigger outcomes need one or more of: higher price points, US and Gulf users, institution licences (placement cells in 5,875 AICTE institutions and 46,000 colleges), or a large free user base with a strong upsell.

### 3.3 Sensitivity

- Price: doubling price to Rs 1,000 doubles revenue only if conversion holds. Indian resume sites sell at Rs 299 for a week, so price sensitivity is high (grade C).
- Conversion: a 1-point change in conversion changes base-case revenue by about 20%.
- Audience: adding B.A. graduates adds about 24 lakh people to the cohort. It would raise SAM by about 60% if they convert like other groups. I do not expect that.

---

## 4. Market for resume and career tools

Analyst estimates differ by a large factor. Bias warning: these reports are sold by market-research firms. Definitions are loose. Most start from the same press releases. Use them only for direction.

| Estimate | Size | Growth | Source and grade |
|---|---|---|---|
| Resume builder market | $8.86B (2025), $9.52B (2026), $12.55B (2030) | 7.2-7.4% CAGR | The Business Research Company, via Research and Markets (C) |
| Resume-builder AI app market | $1.4B (2025), $5.8B (2034) | 17.2% CAGR | Dataintelo (C) |
| AI-powered resume builders | $400M (2024), $1.8B (2032) | 20% CAGR | FutureDataStats (C) |

- The spread is more than 3x for the same product class. This shows weak method.
- My bottom-up India TAM is $57M per year. The Dataintelo $1.4B global figure is 25x larger. I trust the bottom-up figure more for a rupee-priced product.
- Real revenue signal in India: Info Edge (Naukri) recruitment billings rose 17.5% to Rs 552.7 crore in Q1 FY27 (grade B, listed company). Naukri says 2.7 lakh AI-powered resumes are downloaded each month and 1.3 lakh AI mock interviews are completed each month. This proves demand for AI resume and mock-interview features. It also proves that the market leader already offers them.
- Naukri also launched "Naukri Pro - AI Resume Maker" in November 2025 (grade B, press release).

---

## 5. Hiring trends 2025-2026

### 5.1 India

Trend 1: Fresher intent is up, but it is selective.
- TeamLease EdTech (HY2 2026, 1,097 employers, grade B): 75% intend to hire freshers in July-December 2026, up from 73% in January-June. IT sector intent fell from 81% to 76%. E-commerce and tech start-ups 93%, retail 93%, manufacturing 89%, FMCG 86%, engineering and infrastructure 66%, logistics 53%. Bengaluru 89%, Mumbai 75%, Chennai 71%, Hyderabad 60%, Delhi 49%, Pune 41%.
- The report itself says the shift is "less about how many freshers get hired, and more about which freshers get hired". Employers ask for internships, projects, and practical skills. Hiring intent is not the same as hires made.
- Naukri JobSpeak, August 2026 (grade B): index 3,028 versus 2,664 a year earlier (+14%). Fresher hiring +15% year on year. AI/ML roles +31%. GCC hiring +10%.

Trend 2: IT services cut fresher intake sharply.
- BusinessToday (March 2026, grade B) reports fresher intake fell from about 600,000 in FY22 to about 120,000 in FY25. This figure is a press estimate and I could not check it against company filings. Treat it as directional.
- Company plans in 2026 (grade B/C): TCS about 25,000 freshers in FY27 (14,000 onboarded in April-June). Infosys about 20,000. Wipro cut its guide to 7,500-8,000. Several firms delayed onboarding by months.
- Press says entry roles in testing, support, and basic coding are the first to be automated. "Experience creep" makes some fresher roles ask for 2-3 years of project work (grade C).

Trend 3: GCCs and AI roles grow.
- Zinnov-NASSCOM (2026, grade B): 2,117 GCCs, 2.36 million employees, $98.4B export value in FY2026.
- A secondary source says 64% of GCCs forecast up to 20% more fresher hiring in 2026 (grade C).
- LinkedIn Grad's Guide 2026 (grade B): entry-level hiring in India rose 168% between 2023 and 2025. AI hiring rose about 60% year on year. The base year for this rise is not stated. Treat with care.

Unemployment among graduates:
- See section 2.2. Youth unemployment is 9.9% (PLFS 2025). Urban youth: 13.6%. Graduates face higher joblessness than less-educated groups.
- India Skills Report 2026 (Wheebox, grade C because Wheebox sells assessments): employability 56.35%.

Campus versus off-campus:
- Evidence is thin. Large IT firms run off-campus and "pool campus" drives open to any eligible graduate (grade C).
- Press says only 30-50% of students in tier-3 engineering colleges get a campus offer (grade C, unsourced).
- Implication: many graduates must apply off-campus. That is the TailorMyResume use case. I could not find a government number for the campus versus off-campus split.

### 5.2 United States

- Stanford Digital Economy Lab, August 2026 update (grade B, academic, descriptive not causal): employment of workers aged 22-25 in the most AI-exposed jobs is about 19% below the level in less-exposed jobs. The gap was 15% in July 2025 and 13% in the first paper. The effect works through fewer hires, not more layoffs. Authors say the result is not definitive.
- Indeed Hiring Lab (grade B): entry-level postings fell 7.5% year on year to May 2026. Senior postings rose 14.7%. In Q1 2026, senior roles were 69.3% of software postings and entry-level 4.5%.
- Handshake (grade B/C, vendor): postings 12% below pre-pandemic. 61% of the Class of 2026 are pessimistic about careers.
- NY Fed (grade A): recent-graduate unemployment about 5.6%, underemployment about 42%.
- NACE (grade A/B): employers expect a 5.6% rise in Class of 2026 hiring. 70% use skills-based hiring (up from 65%).

Reading: the US market for new graduates is weak but not collapsing. India is bifurcated: the volume IT channel shrinks while start-ups, retail, manufacturing, and GCCs hire.

---

## 6. The recruiter side

### 6.1 What ATS actually do

Findings (mostly grade C vendor writing, cross-checked with Greenhouse and Workday primary statements where I found them):
- Workday, Greenhouse, Lever, iCIMS, and similar tools parse the resume into fields, then filter and rank against criteria the recruiter sets. Humans make the hiring decision. Most recruiters do not set resume-text auto-reject (grade C claim; treat "over 90%" as unverified).
- Real automatic rejection comes mostly from knockout questions (work authorization, degree required, notice period), parse failures on complex layouts, and closed or "ghost" postings.
- Ranking AI is now standard. Greenhouse launched AI matching in 2025-2026. Greenhouse Real Talent (generally available February 2026) adds matching, spam and fraud detection, and identity check with CLEAR (May 2026). It "does not auto-reject", per Greenhouse-related sources (grade B/C).
- No major ATS has native AI-authorship detection (Jobscan, a vendor, grade C). Third-party detectors are unreliable. A Stanford study cited by Jobscan found a 61% false-positive rate for non-native English essays. Detection claims on resumes are weak, and false accusations of Indian English writers are a risk.
- India ATS: Naukri RMS, Zoho Recruit, and Darwinbox all offer AI resume parsing and screening or matching (grade C, comparison blogs). I found no public evidence that any of them auto-reject on AI authorship. I found no public technical detail on how they rank. Treat their behavior as unknown.
- Legal pressure on ATS ranking is rising. In Mobley v. Workday (N.D. Cal.), the court granted preliminary collective certification on age claims in May 2025. It let the case reach Workday's HiredScore ranking features. Notice went out in February 2026. A June 2026 ruling kept several claims alive (grade B, law-firm notes). A separate suit against Eightfold AI (January 2026) says AI candidate scores are consumer reports under the FCRA.

The "ATS rejects your resume" myth, in short: the ATS ranks and sorts. The rejection happens when a human never reaches a low-ranked resume in a pile of 300 or more, or when a knockout question fails. The fix is clear evidence, matching wording, and a simple layout. It is not a hidden score to beat.

### 6.2 AI floods and recruiter distrust

- Greenhouse 2025 AI in Hiring Report (4,136 respondents, US/UK/Ireland/Germany, published November 2025; grade B/C because Greenhouse sells hiring software): 74% of US job seekers use AI. 49% applied to more jobs than the year before. 34% of recruiters spend up to half their week filtering spam. 91% of recruiters saw candidate deception. 65% of hiring managers caught deceptive AI use. 41% of candidates admit prompt injection to fool AI filters. Only 8% of candidates call AI hiring fair. 62% of Gen-Z entry-level candidates lost trust.
- Ashby 2026 (100M+ applications; grade B/C): an average opening gets 300+ applications, about 3x the 2021 level.
- Gartner (grade B): 6% of candidates admitted interview fraud in a 3,000-person survey. Gartner predicts 1 in 4 candidate profiles will be fake by 2028. Only 26% of candidates trust AI to evaluate them fairly. About 39-40% of candidates used AI in applications (2024-2025).
- LinkedIn: about 11,000 applications per minute, up 45% (reported in vendor blogs; grade C).
- NACE (grade B): career-services staff report students who copy AI resumes they cannot defend in interviews. Three students turned in near-identical resumes from the same prompt.
- Vendor surveys claim 49% to 62% of hiring managers reject AI resumes "without personalization" (Resume.io, Resume Now; grade C, seller of resume tools). The same sources say AI use is accepted when the text is personalized. Another survey says only 19.6% would reject a resume they believe is fully AI-written (TopResume, grade C). Only 18% of managers correctly spotted ChatGPT cover letters (grade C). Conclusion: people cannot reliably detect AI text. They react to generic text.
- India specifics (grade B/C): press in June 2026 says uniform AI resumes no longer separate candidates. Firms interview more people to fill each role, and shift to interviews focused on behavior. Fake certificates and deepfake candidates are a rising risk. I found no Indian survey with numbers on AI-resume rejection.

### 6.3 Do cover letters still matter in India?

- No Indian survey with hard numbers found. The material is career-advice blogs (Taggd, upGrad, Naukri Campus; grade C). They say cover letters matter more for freshers because there is no work history, and that recruiters skim for relevance.
- US survey claim: 83% of hiring managers say cover letters matter (ResumeLab, grade C).
- Practical reading: in high-volume India hiring (TCS NQT, Infosys, Cognizant style), assessment tests and cut-offs come first. The cover letter matters in start-ups, GCCs, and direct or referral applications, where a human reads. It rarely decides a mass-recruiter outcome.
- Product implication: treat the cover letter as optional and short. Put more value in gap analysis, project or proof-of-work suggestions, and interview preparation.

### 6.4 Auto-apply

- Greenhouse: 22% of active seekers admit using auto-apply bots (31% among Gen Z). Grade B/C.
- LinkedIn's terms prohibit third-party bots that automate activity. AI that drafts content the user reviews and sends is treated as compliant (grade C legal blogs).
- TailorMyResume's rule "the user reviews everything and applies on their own" fits the safe side. Do not add auto-apply.

---

## 7. Regulatory and trend risks

These rules mostly target employers and vendors. TailorMyResume is candidate-side. My reading is not legal advice. Ask counsel before launch in each market.

| Rule | What it says | Touches TailorMyResume? |
|---|---|---|
| NYC Local Law 144 (in force since July 2023) | Employers using automated employment decision tools in NYC need a yearly bias audit and candidate notice. | No, for a candidate-side tool. State Comptroller audit of December 2025 called enforcement "ineffective". City agreed to improve. More enforcement is likely. Grade A/B. |
| EU AI Act | Recruitment and selection AI is high-risk (Annex III). Digital Omnibus, Regulation (EU) 2026/1744, in force 27 July 2026, moved the date from 2 August 2026 to 2 December 2027. Most Article 50 transparency duties keep the earlier date. | Low for a candidate-side writing tool. Watch two items: transparency duties for AI-generated content, and the workplace ban on emotion recognition if the voice mock interview is later marked as workplace or education use. Grade B (law-firm notes). |
| Colorado AI Act | Delayed to 30 June 2026, then reported "repealed and replaced" in a June 2026 Skadden note. | Employer-side. Low. Grade B. |
| California CRD regulations (in force 1 October 2025) | Employers stay liable for discrimination by automated decision systems. Records kept 4 years. | Employer-side. Low. Grade B. |
| India DPDP Act 2023 and DPDP Rules 2025 | Rules notified 14 November 2025. Phase 1 (Board set-up) in force. Phase 2: consent-manager framework on 13 November 2026. Phase 3: main duties on 13 May 2027 (notice, consent, security, breach notice, erasure, data-principal rights, children's data). | Yes. A resume holds personal data (name, contact, education, employment). TailorMyResume is a data fiduciary. See below. Grade A/B. |
| US job-seeker suits (Eightfold, January 2026) | Alleges AI scores are FCRA consumer reports. | Employer and vendor side. TailorMyResume does not score candidates for employers. Keep it that way. Grade B. |

DPDP actions for the PRD (my reading):
- Give a clear notice and ask for consent for each purpose: parse resume, tailor, store documents, send to OpenAI.
- Collect the minimum data. Do not ask for photo, caste, religion, or date of birth for the MVP. Profile photos are planned "later"; treat the photo as extra personal data.
- Offer full deletion of profile and documents on request. Set a retention period for inactive accounts.
- Keep security controls: encryption, access control, logging (Rule 6, per compliance blogs; grade C).
- Age gate: children's data needs verifiable parental consent. Set an 18+ terms rule.
- OpenAI is a processor. Sign a processor agreement, disable training on user data, and tell users their text goes to an AI provider.

Other trend risks:
- Platform terms (LinkedIn, Naukri) may restrict scraping or automation. Do not scrape or auto-apply.
- Fraud rules: a tool that invents qualifications creates user risk. TailorMyResume's "never invent qualifications" rule is both an ethical and a legal safeguard.

---

## 8. Tailwind or headwind?

### 8.1 Case for tailwind

1. Volume of applications per opening is up about 3x. Candidates need to stand out with specific, relevant content.
2. Recruiters say generic AI text fails. A tool that pulls from real experience and asks the user to edit is the answer to that failure.
3. Employers ask for proof-of-work and projects. A tool that finds gaps and suggests evidence fits this trend.
4. Demand is proven: Naukri reports 2.7 lakh AI resumes and 1.3 lakh AI mock interviews per month.
5. Fresher intent is up in non-IT sectors, and AI/ML and GCC hiring is rising. There are jobs to apply for.
6. Trust is scarce. 62% of Gen-Z entry-level candidates lost trust in hiring. An "honest, no guarantees" brand is a real difference.
7. Regulation targets employers. The candidate-side tool has low regulatory load.
8. Mobile-first India: 95% Android, 958 million internet users, UPI for small payments.

### 8.2 Case for headwind

1. The core buyers (B.Tech graduates targeting IT services) face the weakest channel. Fresher intake in IT services fell sharply since FY22, and onboarding is delayed.
2. Entry-level roles are the ones AI automates first (Stanford, Indeed). Fewer jobs means fewer active seekers who can pay.
3. Free substitutes exist. ChatGPT is free. Naukri offers its own AI resume maker and mock interviews inside the largest job portal. Indian resume sites sell packs at about Rs 299.
4. AI resumes are commoditized. Recruiters say uniform AI CVs no longer separate candidates. The value of any resume tool falls as all tools produce similar text.
5. Mass recruiters in India use tests and cut-offs. Resume wording matters less there.
6. Low revenue per user in India (section 3). A consumer-only app has a small revenue ceiling.
7. Recruiter distrust of AI is rising. Candidates may fear that any AI-assisted document hurts them. The counter is honest positioning and user edits.
8. Platform risk: OpenAI cost and terms, Google Play policy changes, and job-portal terms.

### 8.3 Net view

The trend is a tailwind for the need and a headwind for the price. Demand for help is high. Willingness to pay is low, and free alternatives are good. TailorMyResume wins only if the product is clearly better on honesty and specificity, and if the distribution cost stays near zero.

---

## 9. Implications for the PRD

1. Position on "honest tailoring plus preparation plan". Do not position on "beat the ATS". Never show an "ATS score" as a promise of acceptance.
2. Keep the rule "never invent qualifications". Add a visible "source of each claim" link from each resume line to the imported profile. This answers recruiter distrust and fraud risk.
3. Do not build auto-apply. State it as a feature: "You review. You apply."
4. Produce ATS-safe output: single column, standard headings, text-based PDF and DOCX. Parse failure is a real cause of rejection.
5. Put weight on gap analysis, proof-of-work suggestions (projects, internships), and interview questions. Employers in India ask for skills and projects. The cover letter should be short and optional.
6. Test price points early: Rs 99, Rs 199, Rs 299 per pack, and Rs 499 to Rs 799 for the search plan. Expect conversion of 2% to 5%. Use Naukri's 2.6% paid penetration as a sober anchor.
7. Plan a second revenue path before month 12: college placement-cell licences, or a US/Gulf price tier (needs iOS or web, which the Android-only plan lacks).
8. Target segments by hiring health. Start with commerce, MBA, non-IT B.Tech, and CS graduates aiming at start-ups and GCCs. Avoid making IT-services mass hiring the core promise.
9. Build DPDP compliance into the MVP: notice, purpose-based consent, deletion, minimal data, processor terms with OpenAI, an 18+ rule. The main duties start 13 May 2027, so build now to avoid rework.
10. For the later voice mock interview: avoid emotion or personality inference. Keep it as practice with the user's own consent.
11. Track two market signals each quarter: Naukri JobSpeak fresher index and TeamLease fresher intent for the IT sector.
12. Open questions for other research tracks: real willingness to pay in India (interviews, price tests), Naukri and free-tool feature comparison, and OpenAI cost per pack.

---

## 10. Source list

| # | Source | URL | Grade |
|---|---|---|---|
| 1 | AISHE 2023-24 report, Ministry of Education (full PDF, read directly) | https://www.dohe-education.gov.in/static/uploads/2026/07/8616f33dfee644ab6b87a2bd0658b18d.pdf | A |
| 2 | AISHE 2021-22 press release, PIB | https://www.pib.gov.in/PressReleasePage.aspx?PRID=1999713&reg=48&lang=2 | A |
| 3 | PLFS Annual Report 2025, PIB release (page blocked for direct fetch; figures confirmed through summaries) | https://www.pib.gov.in/PressReleasePage.aspx?PRID=2246009&lang=1&reg=3 | A |
| 4 | PLFS 2025 summary, Drishti IAS | https://www.drishtiias.com/daily-updates/daily-news-analysis/periodic-labour-force-survey-2025 | B |
| 5 | Forbes India, graduates and youth unemployment 2025 | https://www.forbesindia.com/article/news/indias-jobless-rate-holds-steady-but-graduates-and-youth-struggle-to-find-work/2992612/1 | B |
| 6 | ILO and IHD, India Employment Report 2024 | https://www.ilo.org/publications/india-employment-report-2024-youth-employment-education-and-skills | A |
| 7 | StatCounter, mobile OS share India | https://gs.statcounter.com/os-market-share/mobile/india | B |
| 8 | IAMAI-Kantar Internet in India 2025, via YourStory | https://yourstory.com/2026/01/indias-internet-user-base-crosses-950-million-2025-iamai-report | B |
| 9 | NCES, degrees conferred (Digest and Condition of Education) | https://nces.ed.gov/programs/coe/indicator/cts | A |
| 10 | NY Fed, labor market for recent college graduates | https://www.newyorkfed.org/research/college-labor-market | A |
| 11 | NACE, Class of 2026 spring update press release | https://www.naceweb.org/about-us/press/2026/outlook-brightens-for-college-class-of-2026-entry-level-hiring | A/B |
| 12 | NACE, "The Ghostwritten Candidate" | https://www.naceweb.org/career-readiness/best-practices/the-ghostwritten-candidate-ai-fraud-auto-apply-and-the-fight-for-authentic-career-readiness | B |
| 13 | TeamLease EdTech HY2 2026 via People Matters | https://www.peoplematters.in/news/workforce-planning/fresher-hiring-intent-rises-to-75percent-but-it-demand-loses-ground-report-51352 | B |
| 14 | TeamLease EdTech HY1 2026 Career Outlook (PDF, not opened) | https://www.teamleaseedtech.com/pdf/cor-jan-june-2026.pdf | B |
| 15 | Naukri JobSpeak August 2026 via ANI | https://aninews.in/news/business/aiml-hiring-rises-31-pc-yoy-in-august-gcc-recruitment-grows-10-pc-naukri-jobspeak20260908113325/ | B |
| 16 | BusinessToday, Indian IT fresher hiring slump | https://www.businesstoday.in/technology/story/indian-its-fresher-hiring-slump-signals-structural-shift-not-just-slowdown-521374-2026-03-21 | B |
| 17 | Storyboard18, Info Edge Q1 FY27 and AI | https://www.storyboard18.com/advertising/info-edge-ai-boosts-naukri-hurts-shiksha-in-q1fy27-ws-l-107289.htm | B |
| 18 | Entrackr, Info Edge Q1 FY27 billings | https://entrackr.com/fintrackr/info-edge-posts-rs-737-q1-fy27-billings-naukri-contributes-75-12141672 | B |
| 19 | Zinnov-NASSCOM India GCC Landscape 2026 | https://zinnov.com/centers-of-excellence/zinnov-nasscom-india-gcc-landscape-2026-report/ | B |
| 20 | LinkedIn Grad's Guide 2026 via People Matters | https://www.peoplematters.in/news/strategic-hr/entry-level-hiring-climbs-168percent-as-ai-roles-and-internships-gain-ground-linkedin-49290 | B |
| 21 | India Skills Report 2026 (Wheebox) via Careers360 | https://news.careers360.com/india-skills-report-2026-employability-56-35-pc-ai-tools-digital-gig-economy-workforce-global-talent-hub | C |
| 22 | AICTE data on B.Tech enrolment via Careers360 | https://news.careers360.com/btech-courses-aicte-engineering-enrolment-rise-ai-computer-science-cs-job-decline-civil-mechanical-data-5-yr-high-placement-salary | B |
| 23 | Stanford Digital Economy Lab, Canaries update August 2026 | https://digitaleconomy.stanford.edu/news/canariesaug26/ | B |
| 24 | Indeed Hiring Lab, labor market tilting toward seniority (July 2026) | https://hiringlab.indeed.com/2026/07/23/the-labor-market-is-tilting-toward-seniority/ | B |
| 25 | Handshake, Class of 2026 outlook | https://joinhandshake.com/network-trends/class-of-2026-outlook/ | C |
| 26 | Greenhouse 2025 AI in Hiring Report press release | https://www.greenhouse.com/newsroom/an-ai-trust-crisis-70-of-hiring-managers-trust-ai-to-make-faster-and-better-hiring-decisions-only-8-of-job-seekers-call-it-fair | B/C |
| 27 | Greenhouse and CLEAR partnership | https://www.greenhouse.com/newsroom/greenhouse-and-clear-announce-partnership-to-enable-candidate-verification | B/C |
| 28 | Gartner, candidate trust and fake profiles (via HR Dive) | https://www.hrdive.com/news/fake-job-candidates-ai/757126/ | B |
| 29 | Ashby Talent Trends 2026 | https://www.ashbyhq.com/talent-trends-report/reports/recruiting-operations-benchmarks-talent-trends | B/C |
| 30 | Jobscan, can ATS detect AI resumes | https://www.jobscan.co/blog/can-ats-detect-ai-resume/ | C (vendor) |
| 31 | Resume.io, hiring manager rejection study | https://resume.io/blog/resume-rejections | C (vendor) |
| 32 | Resume Now, AI applicant report | https://www.resume-now.com/job-resources/careers/ai-applicant-report | C (vendor) |
| 33 | Whalesbook, AI resumes in India (June 2026) | https://www.whalesbook.com/news/English/other/AI-Generated-Resumes-Disrupt-Hiring-What-It-Means-For-India/6a2ca8d9d017fdb50995fc4c | C |
| 34 | Taggd, cover letter for freshers | https://taggd.in/blogs/cover-letter-for-fresher/ | C |
| 35 | Comparison of Indian ATS (Zoho Recruit, Naukri RMS, Darwinbox) | https://www.thepeoplesboard.com/tools/top-15-ats-tools-in-india-2026/ | C |
| 36 | Naukri Pro AI Resume Maker launch | https://www.business-standard.com/content/press-releases-ani/naukri-launches-ai-powered-resume-maker-to-help-job-seekers-build-professional-recruiter-ready-cvs-effortlessly-125112100431_1.html | B (company release) |
| 37 | Resume builder market, Research and Markets | https://www.researchandmarkets.com/reports/6089781/resume-builder-market-report | C (analyst) |
| 38 | Resume-Builder AI App market, Dataintelo | https://dataintelo.com/report/resume-builder-ai-app-market | C (analyst) |
| 39 | AI-powered resume builders, FutureDataStats | https://www.futuredatastats.com/ai-powered-resume-builders-market | C (analyst) |
| 40 | ResumeGyani pricing (India) | https://resumegyani.in/ | C |
| 41 | EU AI Act omnibus, Gibson Dunn | https://www.gibsondunn.com/eu-ai-act-omnibus-agreement-postponed-high-risk-deadlines-and-other-key-changes/ | B |
| 42 | NYC LL144 audit, NY State Comptroller | https://www.osc.ny.gov/state-agencies/audits/2025/12/02/enforcement-local-law-144-automated-employment-decision-tools | A |
| 43 | NYC LL144 audit analysis, DLA Piper | https://www.dlapiper.com/en-us/insights/publications/2026/01/critical-audit-of-nyc-ai-hiring-law-signals-increased-risk-for-employers | B |
| 44 | DPDP Rules 2025, PIB | https://www.pib.gov.in/PressReleasePage.aspx?PRID=2190014&reg=3&lang=2 | A |
| 45 | DPDP phased timeline, Sansa Legal | https://www.sansalegal.com/post/dpdp-act-2023-and-rules-2025-phased-implementation-timeline-and-business-compliance-deadlines | B/C |
| 46 | Colorado AI Act replaced, Skadden | https://www.skadden.com/insights/publications/2026/06/colorado-repeals-and-replaces-its-ai-act | B |
| 47 | California CRD AI regulations, Paul Hastings | https://www.paulhastings.com/insights/client-alerts/new-california-regulations-on-employers-use-of-ai-to-make-decisions-go-into-effect-oct-1-2025 | B |
| 48 | Mobley v. Workday, Civil Rights Litigation Clearinghouse | https://clearinghouse.net/case/44074/ | A/B |
| 49 | Eightfold AI suit, Fortune | https://www.fortune.com/2026/01/26/job-seekers-suing-ai-hiring-tool-eightfold-allegedly-compiling-secretive-reports | B |

---

## 11. Known gaps and data conflicts

- No official "active job seeker age 20-27" count. The 6-10 million figure is my estimate.
- AISHE gives two numbers for B.A., B.Sc., and B.Com. out-turn. I show both.
- Graduate unemployment figures conflict (11.2%, 22.7%, 29.1%) because of different years and age bands. I could not read the PLFS 2025 tables directly (PIB page blocked).
- The IT fresher intake fall (600,000 to 120,000) is a press estimate. It may mix definitions.
- No public data on how Naukri RMS, Zoho Recruit, or Darwinbox rank resumes.
- No Indian survey on AI-resume rejection or on cover-letter use. All US survey numbers on this topic come from sellers of resume tools (grade C).
- Exchange rate and price points are assumptions. Test them with real users.
- The DPDP and EU AI Act notes are my reading of law-firm summaries. They are not legal advice.
