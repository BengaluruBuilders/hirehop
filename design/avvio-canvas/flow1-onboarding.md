# Flow 1 onboarding, S1 to S6 (dark states; light uses the same layout)

Format: see README.md. Every screen is 360 x 800, bg, home bar at the bottom. S2 to S6 start with a 48 card2 circle back button (S6, S5 Reading use a 18/800 title beside it).

## S1 Default
- Logo tile 36 LIME r=11 + "HireHop" 19/800
- "Your resume, rewritten from your facts" AB 34 UP text
- "HireHop never invents." 17/700 text
- Card r=20 pad=16 gap=10: "Original" 12/800 UP mute; "Made sales reports every week" 15/600 mute; "Rewritten" 12/800 UP mute; "Built weekly sales reports in Excel for 40 stores" 17/800; line 1px; chips "W-01" fact id, "Confirmed" status chip; "Data Operations Associate, Saffron Retail" 13.5/600 mute
- "Get started" LIME r=28 minh=56 16/800

## S2 Paste the job description
Headline "Paste the job description" AB 28 UP. Field card r=22 (h=300 empty, 230 otherwise) pad=16.
- Empty: placeholder "Paste the full job description here." 15.5/600 dim; "Paste" 48 high r=24 card2 button with icon. Analyse disabled (card2 fill, dim text, label "Analyse").
- Pasted: JD text, "312 words" 13.5/800 mute, "Clear" outline 1.5 line 48 high with icon. Button LIME "Analyse this JD" with icon.
- Link only: field outline 2 err; "1 link" 13.5/800 mute (no word count); "Clear". errBg banner r=18 pad=14 16 "Paste the JD text, not the link. HireHop does not open links." Analyse disabled.
- Too short: field outline 2 warn; "7 words"; warnBg banner "This looks shorter than a job description. Paste the full text." Analyse disabled.
- Offline: card2 banner "You're offline. Your JD is saved on this phone." Button LIME "Analyse when I'm back".
- Daily limit: warnBg banner "You've used today's 3 free analyses. You get 3 more tomorrow." Footer "0 of 3 free analyses left today". Analyse this JD disabled.
- Below field always: "Your JD stays on this phone until you tap Analyse." and "2 of 3 free analyses left today".

## S3 Sign in
Headline "Sign in to analyse" AB 30 UP; "Your JD is ready. Sign in with Google to continue." 15/600 mute.
- Card r=20 pad=16: checkbox 24 r=7 (LIME with check when on) + "I am 18 or older" 15.5/700.
- Note "Next, your JD and resume go to HireHop's server to be read. They stay private to your account."
- Button "Continue with Google" (disabled card2/dim until the box is ticked, then LIME). Text button "I'm under 18" minh=48 15/800 LIME.
- Failed: errBg banner "Google sign-in didn't finish. Nothing was sent. Try again." above the button.
- Offline: card2 banner "You're offline. Sign-in needs a connection."; button disabled.
- Under 18: message state, 88 circle card with icon, "HireHop is for people 18 and over" AB 28 UP, "Nothing from this phone was sent." 16/600 mute, "Back to start" LIME.

## S4 Consent
Headline "Nothing is uploaded yet" AB 28 UP; "Tick each purpose to continue." 15/600 mute.
- Three cards r=20 pad=16 gap=10: icon tile 44 r=14 card2 + title 16.5/800 ("Read your resume and JDs", "Keep your facts and analyses", "Delete the uploaded file"); "We do" + text; "We keep" + text; line 1px; checkbox row "I understand" 15.5/700.
- "0 of 3 understood" 13.5/700 mute; "Continue" (disabled until 3 of 3, then LIME); "Not now" text button 15/800 LIME.
- Declined: message state "Nothing has left your phone" AB 28 UP, "You can come back to this at any time." "Review again" LIME, "Back to start" card2.
- Read only from Settings: title "Consent" 18/800, okBg banner "You agreed on 14 Feb 2027", cards end with "Understood" LIME 15/800 + check icon, no footer.

## S5 Add your resume
- Choose: headline "Add your resume" AB 30 UP; "We read it, you confirm each fact, and only confirmed facts are used." 15/600 mute; LIME circle 64 with icon; "Choose your resume (PDF or DOCX)" 17/800; "Opens your phone's file picker" 13.5/600 mute; card r=20 minh=64 pad=10 16 "No resume? Build your profile step by step." 15/700 with 44 tile icon; "HireHop asks for no storage permission."
- Reading: title "Reading your resume" 18/800; file card (name 15.5/800, "2 pages · 184 KB" 13.5/600 mute, 8 high progress, "Up to 30 seconds"); "Facts found so far · 6" 13/800 UP mute; rows r=16 minh=52 pad=12 14 with fact id chip + 14.5/700 text; placeholder row; "You'll confirm each fact next." 15/600 mute.
- Scanned PDF: file card "1 page · scanned image"; warnBg banner "This PDF is a scan, so we can't read its text."; LIME "Start the guided form"; card2 "Choose another file".
- Unsupported: file card "JPG image · 1.2 MB"; warnBg banner "HireHop reads PDF and DOCX files only. This file is a JPG."; LIME "Choose another file"; card2 "Start the guided form".
- Failed: errBg banner "We couldn't read this file. Nothing from it was kept."; LIME "Try again"; card2 "Choose another file".
- Offline: file card "Ready to read"; card2 banner "You're offline. Reading your resume needs a connection."; LIME "Read when I'm back"; card2 "Choose another file".

## S6 Confirm your facts
- Title "Confirm your facts" 18/800; "12 of 18 confirmed" 22/800; progress 8 high LIME on card2.
- Banner r=18 card fill "We removed your date of birth and your photo from this import. HireHop never keeps these." (all confirmed: okBg "All 18 facts are confirmed. HireHop uses only these."; offline: card2 "Your edits are saved on this phone and sync when you're back.")
- Section chips 48 high r=24 with count: selected LIME "Experience 3", others card2 "Projects 2", "Education 2", "Skills 11".
- "Experience · 3" 13/800 UP mute.
- Fact card r=20 pad=16 gap=10: fact id chip, status chip ("Pending" mute / "Confirmed" LIME), title 15.5/800, detail 13.5/600 mute, buttons "Confirm" LIME 48 r=24 with icon + "Edit" outline 1.5 line 48 (confirmed card shows only "Edit").
- Footer note (partly confirmed) "6 facts are still pending. Only confirmed facts are used." 13.5/600 mute; "Continue" LIME (disabled card2/dim when 0 confirmed).
