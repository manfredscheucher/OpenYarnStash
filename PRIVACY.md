# Privacy Policy — OpenYarnStash

**Last updated:** 2025-10-17  
**Maintainers:** Manfred & Helena

## TL;DR
- OpenYarnStash **does not collect** personal or device-identifying data and runs **no servers**.
- The app stores a **plaintext JSON** file on your device with your **yarn**, **projects**, and **assignments (usage)**.
- You can also add **optional images** for yarns and projects, which are stored locally on your device.
- You can **inspect, export, and import** the JSON file. Images are stored in a separate `img` folder.
- Optional: with your **explicit opt-in**, the app can sync the JSON file to **your own Google Drive**. Images are **not** synced. We, the maintainers, **never receive** your data.

---

## 1) What data the app handles
The app handles two types of data, both stored exclusively on your device:
1.  A single plaintext **JSON** file (`stash.json`) containing what you enter:
    - **Yarn** (e.g., name, color, brand, amounts, notes, links, dates)
    - **Projects** (e.g., name, notes, links, dates)
    - **Assignments / Usage** (pairs of project ↔ yarn with amounts)
2.  Optional **Images** you add to your yarns and projects.

No analytics, tracking, advertising IDs, or fingerprinting.

---

## 2) Where data is stored
- **Local (default):** All data is stored on your device.
    - The **JSON file** (`stash.json`) is stored in the app's data directory.
    - **Images** are stored in subfolders within an `img` directory (e.g., `img/yarn/` and `img/project/`).
- **Import/Export:** In **Settings**, you can export/import, verify, and transfer the JSON file. The images are not part of this export and need to be handled manually if you move devices.
- **Optional Google Drive Sync:** If you **explicitly enable** it in **Settings**, the app syncs only the `stash.json` file to **your Google Drive** to share across devices or back it up. **Images are not included in the sync.**
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
