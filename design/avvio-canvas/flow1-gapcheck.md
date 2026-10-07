# Flow 1, S7 Gap check: canvas outline (dark states)

Source: `Flow 1 First Run.dc.html`, made with `outline.js`. Light uses the same layout.
Row card = `bg=card r=20 pad=16 gap=10`. Status chip = `bg=card2 r=13 h=26 pad=0 10 0 7 gap=5 13/800 icon`.
Top bar on every state: 48 dp circle back button (card2), title "Gap check" 18/800. Home bar 108x4 at the bottom.

### S7 Waiting
- Job card `bg=card r=20 pad=14 16 gap=12`: logo tile 44 r=14, "Associate Analyst, Business Intelligence" 15.5/800, "Northwind Global Capability Centre · Bengaluru · 0 to 2 years" 13.5/600 mute
- Step rows `bg=card r=18 minh=56 pad=14 16 gap=12`, label 15.5/700, trailing word 13/700 mute:
  - done: LIME disc 24 with check, "Reading the JD" / "Done"
  - now: ring, "Matching your facts" / "In progress"
  - waiting: label mute, "Sorting must-haves first" / "Waiting"
- "Up to 20 seconds", "You can leave this screen." (centred note)

### S7 Result
- Top bar: back, "Gap check", trailing 48 dp icon button (share)
- Caption "Associate Analyst, Business Intelligence · Northwind Global Capability Centre"
- Summary card: "9" AB 52, "of 14 key terms" AB 22 UP mute, "You cover 9 of 14 key terms. This is not a score." 15/700, chips "9 Met" (acc) and "5 To prepare" (text)
- Section label "Must-haves to prepare · 3" 13/800 UP mute
- Gap card x3: name 17/800, chip "To prepare", "Asked for: DAX measures in Power BI" 13.5/600 mute, "I have this" (Compact secondary, icon, h=48 r=24), "Add to my prep plan" (Compact outline 1.5 line, icon)
- Main button "Tailor my resume" LIME r=28 minh=56 with icon

### S7 I have this sheet (over Result)
- Sheet `bg=sheet r=28 28 0 0 pad=10 16 0 gap=12`, handle 36x4 line
- Chip "To prepare", "Must-have · DAX" 13.5/700 mute, title "Where have you used DAX?" 21/800
- Text area `bg=card outline=2 LIME r=22 h=112 pad=16`: "I wrote DAX measures for my store sales dashboard in Power BI." 15.5/600
- Fact ID chip "U-01", "Your answer is saved as a User-stated fact."
- "Save as U-01" LIME main button, "Cancel" secondary button

### S7 Share card
- Top bar "Share"
- Card: LIME logo tile 36 r=11 + "HireHop" 19/800, "JD fit" 12/800 UP mute, title 17/800, "Northwind Global Capability Centre · Bengaluru" 13.5/600 mute, "Covers 9 of 14 key terms" AB 30 UP, "This is not a score." 15/700, chips "9 Met", "5 To prepare"
- Note card `bg=card r=18 pad=14 16 gap=12` with icon: "Only the JD fit is shared. No name, contact details or resume facts." 14.5/700
- "Share image" LIME main button, "Cancel" secondary button

### S7 Error
- Top bar "Gap check"; centred 88 dp disc `bg=errBg` with icon
- "The gap check didn't finish" AB 28 UP; "This one did not count towards today's free analyses." 16/600 mute
- "Try again" LIME with icon; "Back to my JD" secondary

### S7 Daily limit
- Top bar "Gap check"; 88 dp disc `bg=warnBg` with icon
- "No free analyses left today" AB 28 UP; "You've used today's 3 free analyses. You get 3 more tomorrow." 16/600 mute
- Chip "0 of 3 left today"; "Back to my JD" secondary

### S7 Offline, last result
- Top bar: back, "Gap check", trailing icon button
- Banner `bg=card2 r=18 pad=14 16 gap=12` icon: "You're offline. This is your last result, from today at 10:12." 14.5/700
- "You cover 9 of 14 key terms. This is not a score." 15/700
- Section label "Met · 9" 13/800 UP mute
- Met rows `bg=card r=18 minh=64 pad=12 16 gap=10`: term 15.5/800, fact ID chip (S-02, S-03, W-01, P-01, S-06, S-01) + source label, "Met" chip (acc)
- "Tailor when I'm back" LIME main button
