Source: 'Flow 5 Applications and Settings.dc.html', states S22, S23, S24, dark rows, made with outline.js.
The dock stanza (`bg=#1C1E1D r=34px h=68px ... Applications / Profile / Settings (LIME) ... home bar`) sits at the foot of every S22 state and is left out below.

### S22 Default Dark
"Settings" 18/800
"Account" 13/800 UP mute
bg=card r=20px
  "priya.d@example.com" 15.5/700 text
  "Google account" 13/600 mute
  bg=line h=1px
  "Sign out" 15.5/700 text
"Credits and data" 13/800 UP mute
bg=card r=20px
  "Credits and help" 15.5/700 text
  "4 left, credits never expire" 13/600 mute
  bg=line h=1px
  "Your data" 15.5/700 text
  "See, correct, download or delete" 13/600 mute
"Privacy" 13/800 UP mute
bg=card r=20px
  "Privacy policy" 15.5/700 text
  bg=line h=1px
  "Consent notice" 15.5/700 text
  "Read only, you agreed on 14 Feb 2027" 13/600 mute
  bg=line h=1px
  "Grievance contact" 15.5/700 text

### S22 Scrolled Dark
"Settings" 18/800
"Privacy" 13/800 UP mute
bg=card r=20px (Privacy policy, Consent notice with summary, Grievance contact)
"About" 13/800 UP mute
bg=card r=20px
  "TailorMyResume never invents anything about you." 15.5/700 text
  bg=line h=1px
  "Version" 15.5/700 text
  "1.0.3 (beta)" 13/600 mute
bg=card r=20px
  "Delete account" 15.5/700 err

### S22 Sign-out confirm dialog Dark
(S22 Default behind a scrim)
bg=#1C1E1D r=28px pad=24px 16px 12px 20px gap=12px
  bg=errBg r=14px h=44px w=44px icon
  "Sign out?" 22/800
  "Your facts and applications stay in your account. Sign in with Google to see them again." 15/600 text
  "Cancel" h=48px 15/800
  "Sign out" h=48px 15/800 err

### S22 Offline Dark
"Settings" 18/800
bg=card2 r=18px pad=14px 16px gap=12px icon
  "Settings are readable. Sign out and deletion need a connection." 14.5/700 text
(then the same groups as S22 Default)

### S23 Default Dark
bg=card2 r=24px h=48px w=48px icon      (back button)
"Your data" 18/800
"What TailorMyResume holds about you" AB 26/ UP text
bg=card r=20px pad=16px gap=10px
  bg=card2 r=14px h=44px w=44px icon
  "Profile facts" 16/800
  "15 confirmed, 3 user-stated" 13.5/600 mute
  "18" AB 28/
  "View" outline=1.5px line h=48px gap=7px 14/800 text icon
  "Correct" outline=1.5px line h=48px gap=7px 14/800 text icon
bg=card r=20px pad=16px gap=10px
  bg=card2 r=14px h=44px w=44px icon
  "Applications" 16/800
  "JD text, analyses, resumes" 13.5/600 mute
  "4" AB 28/
bg=card r=20px pad=16px gap=10px
  bg=card2 r=14px h=44px w=44px icon
  "Purchases" 16/800
  "5 applications, ₹149, 14 Apr 2027" 13.5/600 mute
  "1" AB 28/
bg=card r=20px pad=16px gap=10px
  bg=card2 r=14px h=44px w=44px icon
  "Uploaded resume" 16/800
  "Priya_Deshmukh_Resume.pdf" 13.5/600 mute
  "Deleted after reading" bg=card2 r=13px h=26px pad=0 10px 0 7px gap=5px 13/800 LIME icon
"Download my data" bg=LIME r=28px minh=56px pad=0 22px gap=10px 16/800 bg icon
"Delete my data" outline=1.5px line minh=56px gap=10px 16/800 err icon

### S23 Scrolled Dark
(same cards scrolled up; note above the buttons)
"TailorMyResume keeps no date of birth and no photo."

### S23 Export preparing Dark
(S23 Default behind a sheet)
bg=sheet r=28px 28px 0 0 pad=10px 16px 0 gap=12px
  bg=line r=2px h=4px w=36px
  "Preparing your data" 21/800
  bg=card r=18px minh=56px pad=14px 16px gap=12px
    bg=LIME r=12px h=24px w=24px icon
    "Collecting your 18 facts" 15.5/700 text
    "Done" 13/700 mute
  bg=card r=18px minh=56px pad=14px 16px gap=12px
    "Adding 4 applications" 15.5/700 text
    "In progress" 13/700 mute
  bg=card r=18px minh=56px pad=14px 16px gap=12px icon
    "Adding 1 purchase record" 15.5/700 mute
    "Waiting" 13/700 mute
  "Usually under a minute. Nothing is sent anywhere until you choose where."
  "Cancel" bg=card2 r=28px minh=56px pad=0 22px gap=10px 16/800 text

### S23 Delete dialog Dark
(S23 Default behind a scrim)
bg=#1C1E1D r=28px pad=24px 16px 12px 20px gap=12px
  bg=errBg r=14px h=44px w=44px icon
  "Delete my data?" 22/800
  "Your 18 facts and 4 applications are deleted. Your account and credits stay." 15/600 text
  "Cancel" h=48px 15/800
  "Delete" h=48px 15/800 err

### S23 Offline Dark
bg=card2 r=18px pad=14px 16px gap=12px icon
  "You're offline. You can see your data. Downloading and deleting need a connection." 14.5/700 text
(S23 Default cards below)
"Download my data" bg=card2 r=28px minh=56px pad=0 22px gap=10px 16/800 dim icon
"Delete my data" bg=card2 r=28px minh=56px pad=0 22px gap=10px 16/800 dim icon

### S24 Default Dark
bg=card2 r=24px h=48px w=48px icon      (back button, no title)
"Delete account" AB 30/ UP text
"priya.d@example.com" 16/700 text
bg=card r=20px pad=16px gap=10px
  "What is deleted" 12/800 UP mute
  "18 profile facts" 15/700
  "4 applications with their JD texts, analyses and resumes" 15/700
  "4 unused credits" 15/700
"Keep my account" outline=1.5px line minh=56px gap=10px 16/800 text
"Delete account" outline=1.5px line minh=56px gap=10px 16/800 err icon

### S24 Deleting Dark
"Deleting your account" AB 28/ UP text
bg=card r=18px minh=56px pad=14px 16px gap=12px
  bg=LIME r=12px h=24px w=24px icon
  "Deleting your 18 facts" 15.5/700 text
  "Done" 13/700 mute
bg=card r=18px minh=56px pad=14px 16px gap=12px
  "Deleting 4 applications" 15.5/700 text
  "In progress" 13/700 mute
bg=card r=18px minh=56px pad=14px 16px gap=12px icon
  "Closing your account" 15.5/700 mute
  "Waiting" 13/700 mute
"Keep the app open until this finishes."

### S24 Done Dark
bg=okBg r=44px h=88px w=88px icon
"Account deleted" AB 28/ UP text
"Your account and data are deleted. Thank you for using TailorMyResume." 16/600 mute
"Back to Welcome" bg=LIME r=28px minh=56px pad=0 22px gap=10px 16/800 bg

### S24 Error Dark
(S24 Default plus, above the card)
bg=errBg r=18px pad=14px 16px gap=12px icon
  "Your account is unchanged. Try again." 14.5/700 text

### S24 Offline Dark
bg=card2 r=18px pad=14px 16px gap=12px icon
  "Deleting needs a connection. Nothing has been deleted." 14.5/700 text
(S24 Default card below)
"Keep my account" outline=1.5px line minh=56px gap=10px 16/800 text
"Delete account" bg=card2 r=28px minh=56px pad=0 22px gap=10px 16/800 dim icon
