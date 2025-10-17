# Privacy Policy — OpenYarnStash

**Last updated:** 2025-10-17  
**Maintainers:** Manfred & Helena

## TL;DR
- OpenYarnStash **does not collect** personal or device-identifying data and runs **no servers**.
- The app stores only a **plaintext JSON** file on your device with your **yarn**, **projects**, and **assignments (usage)**.
- You can **inspect, export, and import** that file.
- Optional: with your **explicit opt-in**, the app can sync that JSON to **your own Google Drive** using Google’s API. We, the maintainers, **never receive** your data.

---

## 1) What data the app handles
A single plaintext **JSON** file containing what you enter:
- **Yarn** (e.g., name, color, brand, amounts, notes, links, dates)
- **Projects** (e.g., name, notes, links, dates)
- **Assignments / Usage** (pairs of project ↔ yarn with amounts)

No analytics, tracking, advertising IDs, or fingerprinting.

---

## 2) Where data is stored
- **Local (default):** The JSON is stored on your device to restore your data on next launch.
- **Import/Export:** In **Settings**, you can export/import, verify, and transfer the file.
- **Optional Google Drive Sync:** If you **explicitly enable** it in **Settings**, the app syncs the same JSON to **your Google Drive** to share across devices or back it up.  
  - Uses **Google’s official API** and **OAuth**.  
  - The file lives in **your** Google account; you can revoke access anytime.  
  - We, the maintainers, **do not have access** to your Drive or files.

> To use Drive sync, you must have Google Drive set up and agree to Google’s terms and privacy policy.

---

## 3) Network access & permissions
- Local usage requires **no network**.  
- If you enable Drive sync, the app uses network access **only** to read/write **your** JSON on Google Drive.  
- No telemetry is sent to our servers (we operate none).

---

## 4) Security considerations
- The JSON file is **plaintext** by design (transparency and portability).  
- Protect your device (screen lock, OS encryption, backups).  
- Drive sync uses HTTPS; access is governed by Google OAuth.

---

## 5) Open source
The entire codebase is public. You can review, build, and modify it. We may publish releases (e.g., on the Play Store) from time to time.

---

## 6) GDPR and similar
We do not operate a backend nor act as a controller for your content. Your data is under **your** control:
- On device: edit/export/delete via the app or your file system.
- On Google Drive: manage or delete files; revoke the app’s access in your Google Account.

If you open issues on GitHub, GitHub’s policies apply to any info you share there.

---

## 7) Third-party services
- **Google Drive** (optional): If enabled by you, Google’s **Terms** and **Privacy Policy** apply to files stored in your Drive.
- External links you add (e.g., shops, patterns) open in your browser; those sites have their own policies.

---

## 8) Changes
Policy updates will be reflected here with a new “Last updated” date.

---

## 9) Contact
Questions or requests? Please open an **issue** in this repository.


⸻

If you want, I can also generate a short badge row (e.g., Kotlin, Compose, Platforms) and a tiny BUILDING.md skeleton to match your current setup.
