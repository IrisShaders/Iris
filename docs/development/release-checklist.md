# Checklist for a new Iris release

Once you have written a changelog for a new release, have all branches merged properly, and have tested the new
release sufficiently, follow these steps to publish that new release to the public.

1. Change the Iris version in `gradle.properties` on the `trunk`, `1.17`, and `1.18` branch.
2. Push those version bump commits to GitHub.
3. Go to the Releases tab on the Iris repository, click `Draft a new release`.
    1. Create a tag relevant to the release version and branch
    2. Target the relevant branch
    3. Create a Release title, relevant to the branch and release version
    4. Paste in the relevant changelog from [here](docs/changelogs)
    5. Release
    6. Repeat for other versions
4. Once the action has run, builds are now posted across CurseForge, GitHub and Modrinth
5. Once Published, download the build-artifacts for each branch. For each version:
    1. Take out the iris-and-sodium JAR that does not contain a -dev or -sources suffix.
    2. Upload that JAR to Modrinth, CurseForge, and GitHub Releases. Remember to attach a proper changelog.
    3. Place that JAR into a previously-empty folder named `mods`.
    4. Compress that folder into a ZIP file.
    5. When you open the ZIP file, it should have a single folder named `mods`, with the JAR file within that folder.
    6. Rename that ZIP file to `Iris-Sodium-1.16.5.zip`, replacing the 1.16.5 part with the relevant version if needed.
    7. Place that ZIP into the correct folder of the Iris-Installer-Files repository.
6. Commit the newly added files in the Iris-Installer-Files repository.
7. Create a new tag on the Iris-Installer-Files repository.
8. Make sure `Iris-Installer-Files/meta.json` contains all editions you want to release.
9. Publish a new GitHub release on the Iris-Installer-Files repository, and attach all edition ZIPs to that release.
10. Send out a release announcement.
