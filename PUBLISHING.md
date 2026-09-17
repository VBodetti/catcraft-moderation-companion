# Publishing CCMC 1.0.0

## 1. Create the GitHub repository

Create a new repository named `catcraft-moderation-companion`.

Do not upload the source ZIP as a single file. Extract the release-source ZIP and place the **contents of the `catcraft-moderation-companion-1.0.0` folder at the repository root** so GitHub shows `src/`, `.github/`, `build.gradle`, `LICENSE`, and `README.md` directly.

Use a fresh repository with a fresh initial commit.

Suggested first commit message:

`Initial release source for CCMC 1.0.0`

## 2. Let GitHub Actions build the candidate

The `Build` workflow installs Java 25 and Gradle 9.5.1 and executes a clean Fabric build against the real dependencies.

After the first push:

1. Open the repository's **Actions** tab.
2. Open the latest **Build** run.
3. Confirm every build step is green.
4. Download the `ccmc-1.0.0-build` workflow artifact.
5. Use the shortest/non-sources JAR from that artifact as the runtime-test candidate.

Do not publish the release until that exact source-built JAR passes the normal CCMC runtime checklist.

## 3. Runtime test the source-built candidate

At minimum verify:

- Minecraft 26.2/Fabric launches without CCMC or Mixin errors.
- Player-name moderation popup works.
- Message and Mail open editable prefills.
- Jail uses `/togglejail PLAYER 1`.
- Unjail uses `/unjail PLAYER`.
- Unmute uses `/lunmute PLAYER`.
- F8 and `/ccmc menu` work.
- Moderator rank does not expose Fly/God.
- Timestamps/history/copy behavior works in `ccmc` mode.
- `auto` mode yields overlapping chat processing when each supported external chat processor is installed, while moderation remains active.
- `external` mode disables only CCMC chat processing.

## 4. Create GitHub 1.0.0 release

After runtime PASS:

1. Create tag `v1.0.0` from the exact tested commit.
2. Create a GitHub Release titled `CatCraft Moderation Companion 1.0.0`.
3. Attach the exact tested production JAR.
4. Use the contents of `CHANGELOG.md` for the release notes.

## 5. Publish on Modrinth

Create a new Modrinth project or use an empty/unpublished project page.

Recommended project metadata:

- Name: `CatCraft Moderation Companion`
- Version: `1.0.0`
- Minecraft: `26.2`
- Loader: `Fabric`
- Environment: `Client-side only`
- License: `MIT`
- Required dependencies: `Fabric API`, `Cloth Config`
- Optional dependency: `Mod Menu`
- Source URL: the new GitHub repository
- Website: `https://www.catcraft.net/`
- Wiki/help: `https://wiki.catcraft.net/?ref=catcraft.net`

Upload the exact runtime-tested production JAR as the primary file. A generated sources JAR may be uploaded as an additional source file if desired.

## 6. Final release identity

The public version line begins at **1.0.0**. Future releases should increment normally from this repository and source tree.
