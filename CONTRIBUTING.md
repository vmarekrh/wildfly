JBoss EAP 7 Contributions
=========================

# Reporting Upstream
All JBEAP issues should be associated with an upstream community JIRA issue.  

# Commit Messages
Commit messages should start with the JIRA id for the upstream issue that is being backported, e.g. WFLY-xxx, and should also contain the issue summary.  For example:
> [WFLY-5667] Migration: do not migrate parameters ignored by new subsystem

# Pull Requests
A PR's subject should contain the JBEAP issue id followed by the issues title. For example:
> [JBEAP-1860] Migration: do not migrate parameters ignored by new subsystem  

All PRs must include the following in their description:
- URL of upstream PR.
- URL for the upstream JIRA.
- URL of the JBEAP JIRA.

# General Guidelines
- Only commit necessary changes. 
  - Don't refactor for the sake of it!
  - Avoid commiting changes to code formatting and the ordering of imports.
- Split solutions into multiple commits only if each commit is able to standalone (i.e. it does not depend on future commits)
