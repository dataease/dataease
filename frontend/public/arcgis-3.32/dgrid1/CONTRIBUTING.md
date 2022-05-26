_Do you have a contribution?  We welcome contributions, but please ensure that you read the following information
before issuing a pull request.  Also refer back to this document as a checklist before issuing your pull request.
This will save time for everyone._

# Before You Start

## Understanding the Basics

If you don't understand what a *pull request* is, or how to submit one, please refer to the
[documentation][github-docs] provided by GitHub.

## Search GitHub issues first; create an issue if necessary

Is there already an issue that addresses your concern?  Do a bit of searching
in our [issue tracker][] to see if you can find something similar. If not,
please create a new issue before submitting a pull request unless the change is
truly trivial, e.g. typo fixes.

Please note that support questions should not be reported as issues; they should be asked on the
[dojo-interest mailing list][] or #dojo on irc.freenode.net where they may catch more of the community's attention.
Web interfaces are available from the [Dojo Toolkit Community page][].

## Discuss Non-Trivial Contributions with the Committers

If your desired contribution is more than a non-trivial fix, you should discuss it on the mailing list or IRC first
to ensure you are on the right track.  In the case of changesets for new features, this will also provide an
opportunity for early feedback to gauge the committers' interest in incorporating your changes.

## Contributor License Agreement

We require all contributions beyond minor typo corrections to be covered under the Dojo Foundation's
[Contributor License Agreement][cla].  This can be done electronically, and essentially ensures that you are
making it clear that your contributions are your contributions, you have the legal right to contribute, and
you are transferring the copyright of your works to the Dojo Foundation.

If the GitHub user ID you are submitting your pull request from differs from the Dojo Community ID or e-mail address
which you have signed your CLA under, you should specifically note what you have your CLA filed under (and for CCLA
that you are listed under your company's authorised contributors).

# Submitting a Pull Request

The following are the general steps you should follow in creating a pull request.  Subsequent pull requests only need
to follow step 3 and beyond:

1. Fork the repository on GitHub
2. Clone the forked repository to your machine
3. Create a feature branch in your local repository
4. Make your changes and commit them to your local repository
5. Rebase and push your commits to your GitHub remote fork/repository
6. Issue a Pull Request to the official repository
7. Your Pull Request is reviewed by a committer and merged into the repository

*Note*: While there are other ways to accomplish the steps using other tools, the examples here will assume the most
actions will be performed via the `git` command line.

## 1. Fork the Repository

When logged in to your GitHub account, and you are viewing the dgrid repository, you will see the *Fork* button.
Clicking this button will show you which organizations you can fork to.  Choose your own account.  Once the process
finishes, you will have your own repository that is "forked" from the official one.

Forking is a GitHub term and not a git term.  Git is a wholly distributed source control system and simply worries
about local and remote repositories and allows you to manage your code against them.  GitHub then adds this additional
layer of structure of how repositories can relate to each other.

## 2. Clone the Forked Repository

Once you have successfully forked your repository, you will need to clone it locally to your machine:

```sh
$ git clone git@github.com:username/dgrid.git
```

This will clone your fork to your current path in a directory named `dgrid`.

Be sure to read dgrid's [README][dgrid-readme] and ensure you have the necessary dependencies for development and
testing.

You should also set up an `upstream` remote.  This will allow you to take changes from SitePen's repository,
merge them into your local clone, and rebase your branches:

```sh
$ cd dgrid
$ git remote add upstream https://github.com/SitePen/dgrid.git
$ git fetch upstream
```

Then you can retrieve upstream changes and rebase on them into your code like this:

```sh
$ git pull --rebase upstream master
```

For more information on maintaining a fork, please see the GitHub Help article [Fork a Repo][] and information on
[rebasing][] from git.

## 3. Create a Branch

The easiest workflow is to keep your master branch in sync with the upstream branch and do not push any of your own
commits to that branch (otherwise you may need to hard-reset it later).  When you want to work on a new feature,
you then ensure you are on the branch you wish to base your work on (usually `master`) and create a new branch from
there.  While the name of the branch can be anything, it can often be easy to use the issue number you might be working
on as a start.  For example:

```sh
$ git checkout -b fix-123-short-description master
Switched to a new branch 'fix-123-short-description'
```

You will then be on the feature branch.  You can verify what branch you are on like this:

```sh
$ git status
# On branch t12345
nothing to commit, working directory clean
```

## 4. Make Changes and Commit

Now you just need to make your changes.  Once you have finished your changes (and tested them) you need to commit them
to your local repository (assuming you have staged your changes for committing via `git add`):

```sh
$ git status
# On branch t12345
# Changes to be committed:
#   (use "git reset HEAD <file>..." to unstage)
#
#        modified:   somefile.js
#
$ git commit -m 'Corrects some defect, fixes #123'
[t12345 0000000] Corrects some defect, fixes #123
 1 file changed, 2 insertions(+), 2 deletions(-)
```

## 5. Rebase and Push Changes

If you have been working on your contribution for a while, the upstream repository may have changed.  You may want to
ensure your work is on top of the latest changes so your pull request can be applied cleanly:

```sh
$ git pull --rebase upstream master
```

When you are ready to push your commit to your GitHub repository for the first time on this branch you would do the
following:

```sh
$ git push -u origin t12345
```

This sets up your local branch to track the newly-created branch on your remote.  After the first time, you can simply
type:

```sh
$ git push
```

## 6. Issue a Pull Request

In order to have your commits merged into the main repository, you need to create a pull request.  The instructions for
this can be found in the GitHub Help article [Creating a Pull Request][].  Essentially you do the following:

1. Go to the GitHub page for your repository (e.g. `https://github.com/username/dgrid/`)
2. Select the feature branch from your repository
3. Click the Pull Request button
4. If necessary, select the appropriate branch that your branch should be merged into
4. Enter a title and description of your pull request, mentioning the corresponding [issue tracker][] ticket
5. Review the "Commits" and "Files changed" tabs; if you see more than just your commits here, double-check that you've
   selected the correct branch to merge into
6. Click "Send Pull Request"

You will be notified about the status of your pull request based on your GitHub notification settings.

## 7. Request is Reviewed and Merged

Your request will be reviewed.  It may be merged directly or [interactively rebased][], or you may receive feedback
or questions on your pull request.

# What Makes a Successful Pull Request?

Having your contribution accepted is more than just the mechanics of getting your contribution into a pull request;
there are several other things that are expected when contributing to dgrid which are covered below.

## Version Support

Code added to dgrid should work with Dojo 1.8 and later, and should support all modern browsers plus IE 8 and later.

## Coding Style and Linting

dgrid 0.4 and later follows SitePen's [style guide].  Pull requests should adhere to this.

## Documentation

If the pull request adds or changes features, make sure to update the documentation in the repository's `doc` folder
as needed.

dgrid source code follows the Dojo Toolkit's approach to [inline API documentation][].  Any pull request should
ensure it has updated or added the appropriate inline documentation.

## Test Cases

If the pull request adds or changes functional behavior or is fixing a defect, the unit test cases should be modified to
reflect this.  The committer reviewing your pull request is likely to request the appropriate changes to the test
cases.

dgrid uses [Intern][] for automated testing.  It is expected that you will have tested your changes against the
existing test cases and appropriate platforms prior to submitting your pull request.

## Licensing

All of your submissions will be licensed under the "New" BSD license.

## Expect Discussion and Rework

Unless your changes are trivial or you have been working with contributing to dgrid for a while,
expect a significant amount of feedback on your pull requests once a committer has the opportunity to review it.
Don't be offended or discouraged by feedback, but keep in mind that the committers have a keener eye to how your
changes may affect existing code, or contain corner cases or incompatibilities.

Also keep in mind that any pull request is essentially asking the dgrid committers to continue maintaining the code you
contribute, so features that seem overly specific to a given application or which expose a wide array of edge cases
when used more generically may be deemed inappropriate for incorporation into the dgrid repository.  In such cases,
you are certainly encouraged to roll your own repository of extensions that you can continue to maintain and
provide to the community.

[github-docs]: http://help.github.com/send-pull-requests
[issue tracker]: https://github.com/SitePen/dgrid/issues
[Dojo Toolkit Community page]: http://dojotoolkit.org/community/
[dojo-interest mailing list]: http://mail.dojotoolkit.org/mailman/listinfo/dojo-interest
[dojo-contrib]: http://mail.dojotoolkit.org/mailman/listinfo/dojo-contributors
[cla]: http://dojofoundation.org/about/cla
[Creating a Pull Request]: https://help.github.com/articles/creating-a-pull-request
[Fork a Repo]: https://help.github.com/articles/fork-a-repo
[dgrid-readme]: README.md
[style guide]: https://github.com/SitePen/.jshintrc#readme
[inline API documentation]: http://dojotoolkit.org/reference-guide/developer/markup.html
[Intern]: http://theintern.io/
[interactively rebased]: http://git-scm.com/book/en/Git-Tools-Rewriting-History#Changing-Multiple-Commit-Messages
[rebasing]: http://git-scm.com/book/en/Git-Branching-Rebasing
