# Checklist for a new Iris release

Once you have written a changelog for a new release, have all branches merged properly, and have tested the new
release sufficiently, follow these steps to publish that new release to the public.

1. Change the Iris version in `gradle.properties` on the `trunk`, `1.17`, and `1.18` branch.
2. Change the Sodium version in `gradle.properties` on the `1.16.x/iris`, the `1.17.x/iris`, and the `1.18.x/iris`
   branch, if needed. This is needed if you update the Sodium fork with new code changes at all without a corresponding
   update to upstream Sodium.
3. Push those version bump commits to GitHub.
4. Go to the Actions tab on the Iris repository, and click the build-release workflow. Run the workflow manually on the
   `trunk`, `1.17`, and `1.18` branch.
5. Once complete, download the build-artifacts for each branch. For each version:
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
