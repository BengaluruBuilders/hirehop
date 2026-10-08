# Flow 3 outline: export and payment (S12 to S15), dark states

Source: `Flow 3 Export and Payment.dc.html`, made with `outline.js`. Light uses the same layout.
Repeated blocks are written once. All screens: 360 x 800, status bar, 48 card2 circle back button, bottom gesture bar.

## Shared blocks
- Top bar: back button, title 18/800 ("Export", "Credits and help"). S13 default and error states use the Archivo Black headline 30 UP instead.
- Paper preview: white sheet r=6, 233 x 330 (185 x 262 when a banner sits above), pad 20, resume text 6 to 13 px. While rendering: card with 1.5 line outline, "Usually under 3 seconds" 14/700 mute, label "Preparing the page" 13/700 mute.
- Caption under paper: "Page 1 of 1 · Plain" 13/700 mute ("Preview didn't load" 14/700 mute and "No preview" in error).
- Format switch: card, r=28, pad 4, gap 6; two 48 high r=24 segments "PDF" "DOCX" 15/800; selected = LIME fill, black text, check icon; other = text colour.
- File name row: card r=18, minh 64, pad 12 14, gap 10, icon; label "File name" 12.5/700 mute; value 13.5/700.
- Credit note: "Uses 1 credit. You have 1 free." above the button.
- Main button: "Download PDF" / "Download DOCX" LIME r=28 minh 56 16/800 with icon. Disabled (rendering, offline): card2 fill, dim text. Error: "Try again".

## S12 Export preview
- Rendering: paper placeholder, switch, file name row, disabled "Download PDF".
- Ready, 1 free credit: paper, caption, switch, file name, credit note, LIME "Download PDF".
- DOCX selected: DOCX segment LIME; file name ends .docx; button "Download DOCX".
- No credit: card banner (r=18, pad 14 16, gap 12, icon): "You have 0 credits left. Downloading opens the application pack, so you can choose." 14.5/700. Smaller paper. No credit note. LIME "Download PDF".
- Beta, free downloads: okBg banner "Downloads are free during the beta test."
- Making the file: sheet r=28 top, handle 36x4, "Making your file" 21/800; card rows minh 56 r=18 pad 14 16: "Making your PDF" / "In progress", "Saving it to your phone" (mute) / "Waiting"; note "Nothing is charged until the file is ready."; secondary "Cancel".
- Render error: errBg banner "Your resume is saved. No credit was used. Try again."; card placeholder with icon; button "Try again".
- Offline: card2 banner "You can preview. Downloading needs a connection. Nothing is charged while you're offline."; disabled "Download PDF".

## S13 Application pack
- Default: headline "Application pack" AB 30 UP; "Your Northwind resume is ready. Download it with an application pack." 16/600 mute; pack card (card, 2 LIME outline, r=20, pad 16, gap 10): label "Application pack" 13/800 UP mute, "5 applications" 20/800, "₹149" AB 44, line 1 dp, notes "One-time payment through Google Play. Price includes GST." "No subscription. Nothing renews." "Credits never expire." "Each application: a tailored resume, its export, and prep questions."; LIME "Buy 5 applications"; text button "Refunds and help" minh 48 15/800 LIME.
- Purchase pending: message state, 88 circle warnBg, "Payment pending" AB 28 UP, "We'll add your credits when Google Play confirms it. You don't need to pay again.", chip "Waiting for Google Play", secondary "Back to my resume".
- Success: okBg circle, "Payment confirmed" AB 28 UP, "5 credits, credits never expire.", chip "5 left" (LIME), LIME "Download PDF".
- Cancelled: default plus card banner "No payment was made. Your resume stays saved in Applications."
- Payment failed: default plus errBg banner "Google Play couldn't complete the payment. TailorMyResume added no charge."; button "Try again".
- Offline: card2 banner "You're offline. Buying needs a connection."; disabled "Buy 5 applications".

## S14 Exported
- Headline row: LIME 44 circle icon, "Resume exported" AB 28 UP.
- File card (card r=20 pad 16 gap 10): 44 card2 icon tile, file name 13.5/800, "PDF · 1 page · 48 KB" 13/600 mute, buttons "Share" "Open" (Compact, icons).
- Credit card: number AB 40; paid: "4 left, was 5. 1 credit used. Credits never expire."; free: "Free application used. 0 credits left. Nothing was charged." 14/700.
- Note "Saved to Applications: Associate Analyst, Northwind GCC".
- Card "Did you apply?" 16/800 + LIME compact "Mark as Applied".
- Rows (card r=18 minh 64 pad 8 16 8 10, 44 icon tile, 15/700): "Get prep questions", "Write a cover letter (optional)".
- Status sheet: "Application status" 21/800; rows Saved, Applied, Interview, Offer, Rejected (44 icon tile, 16/700, radio 24 at end); LIME "Save status".
- Marked Applied: card with chip "Applied" (LIME) and "Marked on 14 Apr 2027"; snackbar "Applied, marked on 14 Apr 2027" + "Undo".

## S15 Credits and help
- Count card (card r=20 pad 16): number AB 72, text 15/700: no purchases "left. This is your free application. No purchases yet."; purchases "left. Credits never expire."
- Section label "Purchase history" 13/800 UP mute; empty: "No purchases yet." 14/600 mute.
- Purchase card: "5 applications" 16/800, "₹149" 16/800, "14 Apr 2027 · Google Play" 13.5/600 mute, "Order ID ..." 12.5/600 mute, chip "Paid" (LIME) or "Pending".
- Section label "Help"; three cards with 44 icon tile, title 15.5/800, body 14/600: "How refunds work", "What is a credit?", "Contact support" (copy as in the app).
- Pending: warnBg banner "Payment pending. We'll add your credits when Google Play confirms it."; count 0.
- Offline: card2 banner "You're offline. This is your last saved credit count."
