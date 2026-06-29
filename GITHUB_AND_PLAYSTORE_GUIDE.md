# Residio Owner — GitHub → Google Play, straight version

This is the exact, no-fluff path to get the app onto phones. Do the steps in order.
Where to log in, where to start, where to end — all marked.

---

## 0. The honest timeline (read once)

You're publishing as an **individual** (no registered business / no D-U-N-S). Google's rule for
personal accounts: before an app can go **public on the Play Store**, you must run a **closed test
with 12 testers kept in for 14 continuous days**, then a 3–7 day review. There is **no way around
this** for personal accounts. So:

- **Today:** app live on the **Internal testing** track → installable on real phones via a private link, within minutes. This is real and usable today.
- **~Day 1:** start the 12-tester closed test.
- **~Day 15–21:** apply for production → review → **public on the Play Store.**

Internal testing gets it on phones now; the public 3-week clock runs in the background.

> **About GitHub Copilot / github.com/copilot:** Copilot is an AI autocomplete that helps *write
> code inside an editor*. It does **not** log into stores and does **not** deploy apps. Subscribing
> to it doesn't move you toward a Play listing. The thing that auto-publishes for you is the
> **GitHub Actions pipeline already in this project** (`.github/workflows/release.yml`). That's the engine.

---

## PART A — Put the app on GitHub

**Log in at:** https://github.com  (free account is fine)

1. Top-right **+** → **New repository**.
2. Name it `residio-owner-app`. Set **Private**. Do **not** add a README (this folder already has one). Click **Create repository**.
3. On your computer, install **Git**: https://git-scm.com/downloads (Mac: it's likely already there).
4. Open **Terminal** (Mac) / **Git Bash** (Windows), then run these — one block, replacing `YOUR-USERNAME`:

```bash
cd "residio-owner-app"          # cd into THIS folder
git init
git add .
git commit -m "Residio Owner — initial native Android app"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/residio-owner-app.git
git push -u origin main
```

**End state:** refresh the GitHub page — all your files are there. Secrets/keystore are NOT uploaded (the `.gitignore` blocks them).

---

## PART B — Create your signing key (do this once, guard it forever)

This key signs every version. **If you lose it, you can never update the app again.** Keep a backup.

In Terminal, from inside the `residio-owner-app` folder:

```bash
keytool -genkey -v -keystore residio-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias residio
```

It asks for a password (twice) and a few name/org fields. **Write the password down.** This creates
`residio-upload.jks` in the folder. It is gitignored — it stays on your machine only.

Now turn it into text so GitHub can store it as a secret:

```bash
base64 -i residio-upload.jks | tr -d '\n' > keystore.base64.txt
```

Open `keystore.base64.txt` — you'll paste its contents in Part D.

---

## PART C — Create the Google Play "robot" (service account)

This lets the pipeline upload builds automatically. ~10 minutes, once.

**Log in at:** https://console.cloud.google.com

1. Create a new project (top bar → **New Project**), name it `residio-play`.
2. Left menu → **IAM & Admin → Service Accounts → + Create service account**. Name `residio-uploader`. Create.
3. On that service account → **Keys → Add key → Create new key → JSON**. A `.json` file downloads. **Keep it safe** — this is `play-service-account.json`.
4. (You'll connect it to Play in Part D, step 5.)

---

## PART D — Wire the secrets into GitHub

**Log in at:** your repo on https://github.com → **Settings → Secrets and variables → Actions → New repository secret.**

Add these **four** secrets (name on the left, value on the right):

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | the whole contents of `keystore.base64.txt` (Part B) |
| `KEYSTORE_PASSWORD` | the keystore password you chose |
| `KEY_ALIAS` | `residio` |
| `KEY_PASSWORD` | same as the keystore password (unless you set a separate one) |
| `PLAY_SERVICE_ACCOUNT_JSON` | the **entire** contents of the `play-service-account.json` file (Part C) |

(That's five rows — the last one is the Play key.) These are encrypted and never visible in the repo.

---

## PART E — Create the app in Play Console & connect the robot

**Log in at:** https://play.google.com/console  → pay the **one-time $25** → choose **Personal** account type.

1. **Create app** → name `Residio Owner` → language, Free, accept declarations.
2. Left menu → **Setup → API access** (or **Users & permissions**) → find the service-account email
   (`residio-uploader@...iam.gserviceaccount.com`) → **Invite / grant access** → give it
   **Admin (or Release)** permission for this app. This is what links Part C to your store.
3. Fill the required **one-time forms** Play shows you: app category, privacy policy URL, content
   rating questionnaire, data-safety form, target audience. (Unavoidable for any app.)

---

## PART F — Ship it (the loop you'll use forever)

**The first upload to a brand-new app must be done by hand** (Google requires the very first AAB
manually). After that, tags auto-ship.

**First release (manual, today):**
1. In Android Studio: **Build → Generate Signed App Bundle** → pick `residio-upload.jks` → build the `.aab`.
2. In Play Console → **Testing → Internal testing → Create new release** → upload that `.aab` → **Review → Roll out**.
3. On the same page, add testers (their emails), copy the **opt-in link**, send it to your phone → install. **App is live to testers.** ✅

**Every release after that (automatic):**
```bash
# make your changes, then:
git add .
git commit -m "what changed"
git tag v1.0.1          # bump the number each time
git push origin main --tags
```
Pushing the tag triggers `.github/workflows/release.yml` → it builds the signed AAB and uploads it
to the **internal** track automatically. Watch it in your repo's **Actions** tab. **Tag = ship.**

> Want a release to go to **production** instead of internal? In `fastlane/Fastfile` the `production`
> lane already exists; change the workflow's last build step from `fastlane internal` to
> `fastlane production` once you've cleared closed testing.

---

## PART G — The road to PUBLIC (the 12-tester rule)

1. In Play Console → **Testing → Closed testing** → create a track → add **12+ testers** (an email list or a Google Group).
2. Get all 12 to **opt in and install**. Keep them in for **14 continuous days**.
3. After 14 days, Play shows **"Apply for production access."** Fill the short form.
4. Create a **Production** release (upload AAB or run the `production` lane) → submit → Google reviews (3–7 days) → **live on the public Play Store.** 🎉

---

## Quick reference — where things live

| Thing | Where |
|---|---|
| App code | `app/src/main/java/com/theempirestays/residio/` |
| App data (offline) | `app/src/main/assets/residio-data.json` |
| Build config / signing | `app/build.gradle.kts` |
| Auto-deploy pipeline | `.github/workflows/release.yml` |
| Upload lanes | `fastlane/Fastfile` |
| Your secret key | `residio-upload.jks` (never leaves your machine / never committed) |

## Apple App Store?
Not covered here on purpose: it needs an **Apple Developer account ($99/year)** and a **Mac with
Xcode**, and this Kotlin code is Android-only. iOS would be a separate build (rewrite, or a
cross-platform rebuild). Tell me when you want to tackle it and I'll lay out that path separately.
