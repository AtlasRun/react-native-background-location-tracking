const config = {
    branches: [
      {
        name: "ft-setup-semantic-release",
      },
      {
        name: "develop",
        prerelease: "rc",
      },
    ],
    verifyConditions: [
      "@semantic-release/changelog",
      "@semantic-release/git",
      "@semantic-release/github",
    ],
    getLastRelease: "@semantic-release/git",
    prepare: [
      {
        path: "@semantic-release/changelog",
        changelogFile: "docs/CHANGELOG.md",
      },
      {
        path: "@semantic-release/npm",
        npmPublish: false,
      },
      {
        path: "@semantic-release/git",
        assets: ["package.json", "docs"],
        message:
          "chore(release): v${nextRelease.version} - <%= new Date().toLocaleDateString('en-US', {year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric'}) %> [skip ci]\n\n${nextRelease.notes}",
      },
    ],
    publish: ["@semantic-release/github"],
  };
  
  module.exports = config;