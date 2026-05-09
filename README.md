# ⚔️ Project Warriors Git Workflow Guide

> **Pull first, branch before coding, commit small, open a pull request, then merge after review.**

---

## 🌿 1. Do Not Commit Directly to `main`

The `main` branch should always stay stable.

Instead, create a separate branch for each feature or task.

### Example

```bash
git checkout main
git pull
git checkout -b feature/login-page
```

---

## 🔄 2. Pull the Latest Changes Before Starting Work

Before coding, always update your local project:

```bash
git checkout main
git pull
```

This helps reduce merge conflicts and keeps everyone working from the latest version.

---

## 👨‍💻 3. Work on Your Own Branch

Use clear and descriptive branch names:

```text
feature/login-styling
feature/signup-page
feature/backend-auth
fix/navbar-layout
```

---

## ✅ 4. Commit Small, Clear Changes

Avoid committing huge amounts of work at once.

### Good Commit Message Examples

```text
Added login page styling
Updated signup form layout
Fixed navbar spacing
Connected login form to API
```

---

## ⚠️ 5. Avoid Editing the Same File at the Same Time

Merge conflicts usually happen when multiple people edit the same file or the same lines of code.

Before starting work, communicate what you are working on.

### Example

```text
Aaliya → Login page
Team Member 1 → Signup page
Team Member 2 → Backend authentication
Team Member 3 → README/documentation
```

---

## 🔃 6. Keep Your Branch Updated

If changes have been merged into `main`, update your branch before opening a pull request.

```bash
git checkout main
git pull
git checkout your-branch-name
git merge main
```

Fix any conflicts in your branch first before merging.

---

## 🔀 7. Use Pull Requests Before Merging

When your feature is complete:

```bash
git push origin your-branch-name
```

Then open a Pull Request on GitHub.

### Pull Requests Should Include

* Summary of changes
* Files/features changed
* Testing completed
* Known issues

---

## 👀 8. Review Before Merging

Before merging into `main`, check:

* Does the code run?
* Does it break anything?
* Are there merge conflicts?
* Has someone reviewed it?

---

## 💬 9. Communicate Often

Use Teams, Discord, Messenger, or Trello to keep everyone updated.

### Examples

```text
I’m working on the login page.
I’m editing style.css.
I’ve opened a pull request.
Please don’t edit auth files yet.
```

Good communication prevents most conflicts.

---

## 🛠️ 10. If a Conflict Happens, Don’t Panic

A merge conflict simply means Git needs help deciding which code to keep.

* Do not randomly delete code
* Review both changes carefully
* Work together with the person editing the same file
* Ask for help if unsure
