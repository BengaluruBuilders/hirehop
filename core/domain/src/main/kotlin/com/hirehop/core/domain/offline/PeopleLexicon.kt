package com.hirehop.core.domain.offline

internal val softSkillEntries: List<LexiconEntry> = listOf(
    softSkill(
        "Communication",
        loose = listOf(
            "communication skills",
            "verbal communication",
            "written communication",
            "verbal and written communication",
        ),
    ),
    softSkill("Teamwork", loose = listOf("team player", "team work")),
    softSkill("Leadership"),
    softSkill("Problem Solving"),
    softSkill("Analytical Skills", loose = listOf("analytical thinking", "analytical ability", "analytical mindset")),
    softSkill("Critical Thinking"),
    softSkill("Time Management"),
    softSkill("Adaptability"),
    softSkill("Presentation Skills"),
    softSkill("Public Speaking"),
    softSkill("Negotiation"),
    softSkill("Attention to Detail", loose = listOf("detail oriented")),
    softSkill("Creativity"),
    softSkill("Ownership"),
    softSkill("Quick Learner", loose = listOf("fast learner", "willingness to learn", "eager to learn")),
    softSkill("Interpersonal Skills"),
    softSkill("Decision Making"),
    softSkill("Self-motivated"),
    softSkill("Multitasking"),
)

internal val degreeEntries: List<LexiconEntry> = listOf(
    education("B.Tech", "btech", "b tech", "bachelor of technology", implies = listOf("bachelor's degree")),
    education("B.E.", "b.e", "bachelor of engineering", implies = listOf("bachelor's degree"))
        .copy(exactForms = listOf("BE")),
    education("M.Tech", "mtech", "m tech", "master of technology", implies = listOf("master's degree")),
    education("B.Sc", "bsc", "b sc", "bachelor of science", implies = listOf("bachelor's degree")),
    education("M.Sc", "msc", "m sc", "master of science", implies = listOf("master's degree")),
    education("BCA", "bachelor of computer applications", implies = listOf("bachelor's degree")),
    education("MCA", "master of computer applications", implies = listOf("master's degree")),
    education("B.Com", "bcom", "b com", "bachelor of commerce", implies = listOf("bachelor's degree")),
    education("M.Com", "mcom", "m com", "master of commerce", implies = listOf("master's degree")),
    education("BBA", "bachelor of business administration", implies = listOf("bachelor's degree")),
    education("MBA", "master of business administration", implies = listOf("master's degree")),
    education("PGDM"),
    education("CA Inter", "ca intermediate", "ipcc"),
    education("CA Final"),
    education("Chartered Accountant", "chartered accountancy").copy(exactForms = listOf("CA")),
    education("CMA", "cost and management accountant", "icwa"),
    education("CFA"),
    education(
        "Bachelor's Degree",
        "bachelors degree",
        "bachelor degree",
        loose = listOf("undergraduate degree", "graduate degree"),
    ),
    education(
        "Master's Degree",
        "masters degree",
        "master degree",
        loose = listOf("postgraduate degree", "post graduate degree", "postgraduate"),
    ),
    education("Diploma"),
)

internal val fieldOfStudyEntries: List<LexiconEntry> = listOf(
    education("Computer Science", "cse", "comp sci", loose = listOf("computer science engineering")),
    education("Information Technology"),
    education("Electronics and Communication", "ece"),
    education("Electrical Engineering", "eee"),
    education("Mechanical Engineering"),
    education("Civil Engineering"),
    education("Mathematics", "maths"),
    education("Economics"),
)
