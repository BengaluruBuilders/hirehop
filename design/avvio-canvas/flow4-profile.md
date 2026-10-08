### S16 Empty Dark
bg=bg r=28px h=800px w=360px
 "Profile" 18/800
 bg=card r=44px h=88px w=88px icon
 "Your profile is empty" AB 28/ UP text
 "Start from your resume, or answer a few short questions." 16/600 mute
 "Import a resume" bg=LIME r=28px minh=56px pad=0 22px gap=10px 16/800 bg icon
 "Answer questions" bg=card2 r=28px minh=56px pad=0 22px gap=10px 16/800 text icon
 dock

### S16 Partly confirmed Dark
bg=bg r=28px h=800px w=360px
 "Profile" 18/800
 bg=warnBg r=18px pad=12px 8px 12px 16px gap=12px icon
  "2 items are not confirmed. TailorMyResume won't use them." 14.5/700
  "Review" outline=1.5px line h=48px gap=7px 14/800 text
 bg=card r=20px pad=16px gap=10px
  "PD" bg=#2E6B4F r=28px h=56px w=56px 19/800 text
  "Priya Deshmukh" 19/800
  "Data Operations Associate" 13.5/600 mute
  "18 facts" AB 26/
  "13 confirmed" bg=card2 r=13px h=26px pad=0 10px 0 7px gap=5px 13/800 LIME icon
  "3 user-stated" bg=card2 r=13px h=26px pad=0 10px 0 7px gap=5px 13/800 text icon
  "2 not confirmed" bg=card2 r=13px h=26px pad=0 10px 0 7px gap=5px 13/800 mute icon
  "Add evidence" bg=LIME r=28px minh=56px pad=0 22px gap=10px 16/800 bg icon
 section rows, each: bg=card r=20px; bg=card2 r=14px h=44px w=44px icon; title 16/800; count 15/800 mute
  Experience 2, Internships 1, Projects 3, Education 1, Coursework 1,
  Skills 9 (with chip "2 not confirmed" bg=card2 r=13px h=26px 13/800 mute icon), Certifications 1
 dock

### S16 Full Dark
 same as Partly confirmed without the warning banner and without the "not confirmed" chips
 "15 confirmed" (LIME chip), "3 user-stated" (text chip), "18 facts"

### S16 Section expanded Dark
 "Profile" 18/800; section rows; the open row (Projects, 3) holds fact rows:
  bg=bg r=16px pad=10px 4px 10px 12px gap=6px
   "P-01" bg=card2 r=8px h=24px pad=0 8px 12/800 LIME
   "Confirmed" bg=card2 r=13px h=26px pad=0 10px 0 7px gap=5px 13/800 LIME icon
   "Library database project (DBMS course)" 14.5/700
   "Edit" h=48px 14/800 LIME
  P-03 uses the "User-stated" chip (13/800 text icon)

### S16 Offline Dark
 same as Full with a banner first:
 bg=card2 r=18px pad=14px 16px gap=12px icon
  "You're offline. You can read and edit your facts. Changes sync when you're back." 14.5/700 text

### S17 New Dark
 top bar: bg=card2 r=24px h=48px w=48px icon (back), "New fact" 18/800
 "P-04" bg=card2 r=8px h=24px 12/800 LIME; "User-stated" chip; "Project" chip (icon)
 bg=card outline=2px LIME r=18px minh=64px pad=12px 16px gap=4px  "Title" 13/700 mute, value 16/700 text
 bg=card r=18px minh=110px pad=12px 16px  "What you did" 13/700 mute, placeholder 16/600 dim
 "Tools" 12/800 UP mute
 "Add a tool" outline=1.5px line h=48px gap=7px 14/800 text icon
 "Save fact" bg=card2 r=28px minh=56px 16/800 dim (disabled)

### S17 Editing Dark
 top bar "Edit fact"; "P-02", "Confirmed" chip, "Project" chip
 Title card, "What you did" card (focused, 2px LIME outline, minh=110px)
 "Tools" 12/800 UP mute; tool chips: bg=card2 r=24px h=48px pad=0 6px 0 16px gap=4px 14.5/800 ("Power BI", "Excel"); "Add a tool"
 "Save fact" bg=LIME r=28px minh=56px 16/800 bg
 "Delete fact" outline=1.5px line minh=56px gap=10px 16/800 err icon

### S17 Dates and numbers Dark
 Title, Start ("Aug 2023"), End ("Mar 2024") as stacked field cards (r=18px minh=64px)
 "Numbers" card (focused) "1,200 records · 4 departments"; note "Only numbers you can stand behind."
 Save fact, Delete fact

### S17 Validation error Dark
 End field: outline=2px err, label 13/700 err; below it "The end date is before the start date." gap=6px 13.5/700 err icon
 "Save fact" bg=card2 dim (disabled); Delete fact

### S17 Delete dialog Dark
 Editing screen behind; sheet bg=#1C1E1D r=28px pad=24px 16px 12px 20px gap=12px
  bg=errBg r=14px h=44px w=44px icon; "Delete P-02?" 22/800
  "Lines in your applications that use it will be marked." 15/600 text
  "Cancel" h=48px 15/800; "Delete" h=48px 15/800 err

### S17 Offline Dark
 banner bg=card2 r=18px pad=14px 16px gap=12px icon "Your changes are saved on this phone and sync when you're back." 14.5/700 text
 then the Editing layout

### S18 Arrived from a scanned PDF Dark
 back button; bg=warnBg r=18px pad=14px 16px gap=12px icon "Your PDF is a scan, so we couldn't read it." 14.5/700 text
 "Let's build your profile here" AB 28/ UP text
 "Four short steps. Each answer becomes a fact you can correct later. You can stop after any step." 15.5/600 mute
 four rows, each bg=card2 r=14px h=44px w=44px icon + 15.5/700: "1. Contact", "2. Education", "3. Skills", "4. Experience"
 "Start with contact" bg=LIME r=28px minh=56px 16/800 bg
 "Save and finish later" minh=48px gap=8px 15/800 LIME

### S18 Step 1, Contact Dark
 top bar "Build your profile" 18/800; "1 of 4"; "Contact"
 progress: four bars bg=LIME|card2 r=3px h=6px (1 LIME)
 labels 11.5/700 (Contact text, others mute; done steps get a check icon)
 "Contact" 22/800
 fields (bg=card r=18px minh=64px pad=12px 16px gap=4px, label 13/700 mute, value 16/700): Full name, Email, Phone, City (focused)
 "Next: Education" LIME; "Save and finish later"

### S18 Step 2, Education Dark
 "2 of 4" "Education"; two LIME bars; Contact label has a check
 "Education" 22/800
 fields: Degree, University or institute, Year you finished, "Coursework you can talk about (optional)" (focused)
 "Next: Skills" LIME; "Save and finish later"

### S18 Step done Dark
 same progress; bg=okBg r=18px pad=14px 16px gap=12px icon "Education is done, filed into your profile" 14.5/700 text
 bg=card r=20px pad=16px gap=10px with fact rows (E-01 "User-stated" chip + text + Edit; C-01 ...)
 "Next: Skills" LIME; "Save and finish later"

### S18 Step 3, Skills Dark
 "3 of 4" "Skills"; "Skills" 22/800
 field "Add a skill you have used" (focused) "Pivot tables"
 skill chips bg=card2 r=24px h=48px 14.5/800: SQL, Excel, Power BI
 note "Each skill becomes a user-stated fact."
 "Next: Experience" LIME; "Save and finish later"

### S18 Step 4, Experience Dark
 "4 of 4" "Experience"; four LIME bars
 "Have you had a job or an internship?" 22/800
 option rows: bg=card r=20px minh=64px pad=0 16px gap=12px 16/800 ("Yes"; "No, not yet" selected: outline=2px LIME)
 bg=card r=18px pad=14px 16px gap=12px icon "No work experience yet? That is fine. Next, we'll ask about your projects." 14.5/700 text
 "Continue to projects" LIME; "Save and finish later"

### S18 Saved for later Dark
 bg=okBg r=44px h=88px w=88px icon
 "Saved for later" AB 28/ UP text
 "2 of 4 steps done. Your answers are saved as user-stated facts." 16/600 mute
 progress ("3 of 4", "Skills", three LIME bars) and labels
 "Go to Profile" bg=LIME r=28px minh=56px 16/800 bg

### S18 Offline Dark
 Step 2 layout with banner bg=card2 r=18px pad=14px 16px gap=12px icon
  "You're offline. Your answers are saved on this phone and sync when you're back." 14.5/700 text

### S19 Category picker Dark
 top bar "Add evidence" 18/800
 "Turn what you did into facts" AB 28/ UP text
 "Where have you done real work?" 16/600 mute
 six cards bg=card r=20px minh=112px pad=14px gap=12px; bg=card2 r=14px h=44px w=44px icon; title 15/800
  Work, Internships, Projects, Coursework, Competitions, Positions of responsibility

### S19 Question Dark
 top bar = category name ("Projects")
 "Question 1 of 4 · Library database project" 13.5/800
 progress bg=card2 r=4px h=8px with bg=LIME r=4px h=8px fill
 bg=card r=20px pad=16px gap=10px
  "One question" 12/800 UP mute
  "What did you build in your Library database project?" 20/800
  bg=card outline=2px LIME r=18px minh=120px pad=12px 16px gap=4px  "Your answer" 13/700 mute, placeholder "Say it plainly, in one or two sentences." 16/600 dim
 "Save answer" bg=card2 r=28px minh=56px 16/800 dim (disabled)
 "Skip this one" minh=48px gap=8px 15/800 LIME

### S19 Typed answer Dark
 same; answer text 16/700 text; "Save answer" bg=LIME 16/800 bg

### S19 Stamped User-stated Dark
 bg=okBg r=18px pad=14px 16px gap=12px icon "Your answer is now fact P-03." 14.5/700 text
 bg=card outline=2px LIME r=20px pad=16px gap=12px
  "P-03" id chip, "User-stated" chip, answer 16.5/800, "From: Library database project" 13/600 mute
 "Next question" bg=LIME 16/800 bg
 "Edit" outline=1.5px line h=48px gap=7px 14/800 text icon

### S19 Filed into Projects Dark
 bg=okBg banner "Filed into Projects"; section row "Projects" 3 with fact rows P-01, P-02, P-03 (P-03 outline=2px LIME, chip "New")
 "Next question" LIME

### S19 Skipped Dark
 "Question 3 of 4 · Library database project"
 bg=card r=16px minh=56px pad=10px 14px gap=10px "Question 2: Which tools did you use?" 14.5/700 + chip "Skipped" (mute, icon)
 note "You can answer skipped questions later from Profile."
 question card "What was the result, in numbers you can stand behind?"; Save answer (disabled); Skip this one

### S19 All done Dark
 bg=okBg r=44px h=88px w=88px icon
 "4 new facts" AB 28/ UP text
 "You can edit them in Profile." 16/600 mute
 id chips P-03, P-04, C-02, S-10
 "Go to Profile" bg=LIME r=28px minh=56px 16/800 bg
