# Checklist for a new Iris release

Once you have written a changelog for a new release, have all branches merged properly, and have tested the new
release sufficiently, follow these steps to publish that new release to the public.

1. Change the Iris version in `fabric.mod.json` on the `trunk`, `1.18.2` and `1.19` branch.
2. Push the version bump commit to GitHub.
3. Go to the releases tab on the Iris repository, and click the `Draft a new release` button.
    1. Insert a tag and title relevant to the branch and Iris version
    2. Insert a changelog from the relevant summary file to the Iris version being released
    3. Publish
    4. Repeat for each branch being released
4. The action will generate and publish the relevant JAR file to CurseForge, GitHub and Modrinth.
5. Once complete, download the build for each release. For each version:
    1. Put the JAR into a folder called `mods`
    2. Compress that folder into a ZIP file.
    3. Rename that ZIP file to `Iris-Sodium-1.16.5.zip`, replacing the 1.16.5 part with the relevant version if needed.
    4. Place that ZIP into the correct folder of the Iris-Installer-Files repository.
    5. Commit the newly added files in the Iris-Installer-Files repository.
6. Create a new tag on the Iris-Installer-Files repository.
7. Make sure `Iris-Installer-Files/meta.json` contains all editions you want to release.
8. Publish a new GitHub release on the Iris-Installer-Files repository, and attach all edition ZIPs to that release.
9. Send out a release announcement.
