# CV Manager Developer Best Practices

## Pull Requests

Pull requests should be kept to a manageable size, able to be reviewed within a few hours. Loose guidelines include ~30 files changed and ~2000 lines changed.

### Squash Merge

All pull requests should be squash merged into develop and master. This keeps the root commit history clean, and reduces the repository size.

When making a squash merge, both a message and a description are included. The message should be concise and accurate (this is what is seen first when scrolling through previous commits). This should resemble the PR title.
The extended description should describe important details about the feature including the distinct changes involved and the affected services. This should resemble the PR description.

#### Synchronizing after a squash commit

When a squash merge or squash commit is executed, multiple previous commits are replaced by a single squash commit. For any branches which still have the non-squished commits, they need to be synchronized.

Checkout develop and check for pending changes.

```sh
git checkout develop
git status
```

Ensure there are no pending changes. If there are any changes present, commit/stash them on a feature branch. The next command will delete all pending changes and re-set the history of your local develop to the remote develop

```sh
git reset --hard origin/develop
```

The recommended approach for synchronizing feature branches after a squash is a [rebase](https://git-scm.com/book/ms/v2/Git-Branching-Rebasing). This will _remove all of the un-squashed commits_, then move through the new commits re-applying them to the updated history. Each step can have it's own merge conflicts, which can be extremely burdensome for large offsets.

```sh
git checkout feature-branch
git rebase develop
```

A secondary approach which can reduce conflicts is a [merge](https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging). This will pull commits from the source branch into the feature branch, _retaining the un-squashed commits_, while presenting all merge conflicts in 1 wave.

```sh
git checkout feature-branch
git merge develop
```
