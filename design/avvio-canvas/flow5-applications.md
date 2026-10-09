# Flow 5 outline: S20 Applications and S21 Application workspace (dark)

Made with outline.js from `Flow 5 Applications and Settings.dc.html`. Same format as the other flow files.
Offline, Sync pending and Scrolled states reuse the List layout; only the differences are listed.

### S20 Empty
 "Hi, Priya" 15/700 mute
 "Your facts, every job" AB 32 UP text
 "Paste a job" bg=LIME r=28 minh=56 pad=0 22 gap=10 16/800 icon
 bg=card r=20 pad=16 gap=10: icon tile card2 r=14 44x44; "No applications yet" 16/800; "Paste a JD to see where you stand. It's free." 14.5/600 mute
 dock: bg=#1C1E1D r=34 h=68 pad=6 gap=4; selected item bg=LIME r=28 gap=3 icon + "Applications" 12/800 black; "Profile" 12/700 mute; "Settings" 12/700 mute

### S20 List
 greeting, headline, "Paste a job" as above
 "Your applications" 17/800, "4" 15/800 mute, "Newest update first" 13/700 mute
 card bg=card r=24 pad=16 gap=14:
  logo tile 44x44 r=14 colour fill, letter 18/800 black
  "Associate Analyst" 17/800
  "Northwind GCC, updated 2 h ago" 13.5/600 mute
  status chip bg=card2 r=13 h=26 pad=0 10 0 7 gap=5, 13/800, icon (Interview in LIME, Applied and Saved in text, Rejected in mute)
  "9 of 14 key terms" 13.5/700 icon, gap=6
 dock as above

### S20 Scrolled, compact header
 "Applications" 18/800 title, "Paste a job" compact bg=LIME r=24 h=48 pad=0 16 gap=7 14/800 icon; then the cards

### S20 Offline
 banner bg=card2 r=18 pad=14 16 gap=12 icon, "You can read everything. Changes sync when you're back." 14.5/700; then List

### S20 Sync pending
 first card gains chip "Waiting to sync" (card2, text, icon) after the status chip

### S21 Full
 top bar: two 48 circle icon buttons (back, more), card2
 header card bg=card r=20 pad=16 gap=10: logo tile 56 r=14; role 21/800; "Northwind GCC, updated 2 h ago" 13.5/600 mute; status chip; "Change status" outline 1.5 line h=48 14/800
 card: icon tile 44 r=14, "Gap check" 16/800, "Open" outline h=48; "9 of 14 key terms. This is not a score." 14.5/600
 card: "Resume" 16/800, file name 13.5/700, "Share" and "Review resume" bg=card2 r=24 h=48 pad=0 16 icon
 card: "Cover letter" 16/800, chip "Optional" mute, "Not written yet." 14/600 mute, "Write one" outline h=48 icon
 card: "Prep plan" 16/800, "1 of 3 done" 13.5/800 mute, checkbox rows 24 r=7 (done LIME), items 14.5/700

### S21 Scrolled to prep plan and notes
 card "Prep questions" 16/800 + "Open" outline + "10 questions" 14.5/600
 card "Notes" 16/800 + chip "Saved" (LIME) + field bg=bg r=14 minh=96 pad=12 14 14.5/600
 card "Job description" 16/800 + text 14/600 mute

### S21 Not exported yet
 Resume card: "Your tailored resume is ready to preview." 14.5/600 + "Preview and export" LIME main button

### S21 Status sheet
 sheet bg=sheet r=28 top pad=10 16 0 gap=12, handle 36x4; "Application status" 21/800; rows icon tile 44 r=14 + label 16/700 (Saved, Applied with radio 24, Interview, Offer, Rejected); "Save status" LIME main button

### S21 Chip updated to Interview
 toast bg=#EBEEE7 r=16 minh=56 pad=4 4 4 16 gap=8 icon; "Status changed to Interview" 14.5/700; "Undo" h=48 14.5/800 accent

### S21 Overflow menu
 popup bg=#1C1E1D r=16 w=230 pad=6 0: "Delete application" minh=52 gap=12 15/700 err icon

### S21 Delete dialog
 bg=#1C1E1D r=28 pad=24 16 12 20 gap=12: errBg tile 44 r=14; "Delete this application?" 22/800; "Its resume, letter and notes are deleted. Your profile facts stay." 15/600; "Cancel" h=48 15/800, "Delete" h=48 15/800 err

### S21 Offline read
 banner bg=card2 r=18 under the top bar: "You're offline. You can read this application. Changes sync when you're back." 14.5/700
