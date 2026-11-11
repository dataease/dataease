# Security Policy

## Supported Versions

Security fixes are provided for the release lines listed below. Other versions, development branches, and unreleased builds are out of maintenance scope.

| Version line                            | Supported |
| --------------------------------------- | --------- |
| DataEase v3.x                           | Yes       |
| DataEase v2.10.x                        | Yes       |
| Older releases and development branches | No        |

## Reporting a Vulnerability

**Do not report security issues in public issues, pull requests, discussions, or public chat channels.**

Use GitHub's private vulnerability reporting:

1. Open the **Security** tab of this repository.
2. Under **Reporting**, select **Advisories**.
3. Click **Report a vulnerability**, complete the form, and submit.

The report is visible only to repository maintainers. GitHub adds the reporter as a credited collaborator on the proposed advisory. If the button is unavailable, contact `support@fit2cloud.com` instead and do not post technical details publicly.

## What to Include

- Affected product, version line, and commit or build identifier
- Affected component and the configuration required to trigger the issue
- Reproducible steps or a minimal proof of concept
- Impact: what an attacker can achieve, and the preconditions (authentication, network position, existing permissions)
- Suggested fix or mitigation, if available
- Whether AI assistance was used to find or write the report

Use your own test instance. Do not include live credentials, real customer data, or destructive payloads. Reports without a reproducible proof of concept take longer to triage.

## What to Expect

- Acknowledgement within **5 business days**, before assessing validity.
- Triage outcome within **10 business days**: accepted as a vulnerability, more information requested, or declined with a reason.
- Accepted reports are handled in a draft GitHub Security Advisory and a private fork. We coordinate the disclosure date with the reporter, then publish the advisory together with the fixed release.
- Reporters are credited in the advisory unless they request anonymity.
- This project does not currently run a bug bounty program. Reports are voluntary and unpaid.

These timelines are targets rather than a contractual commitment. We will notify you if a case requires longer.

## Coordinated Disclosure

Please allow a reasonable window to develop and ship a fix before publishing details. If you follow a fixed disclosure policy, state its timeline in your report so we can align on a date.

## Known Design Limitations

Some behaviour follows from the product's functional design and is documented rather than changed in code. For example, certain data-source features accept user-supplied network addresses and can therefore reach internal endpoints (SSRF). In the Enterprise Edition, the built-in **APISIX gateway** can enforce access control to block this risk; Community Edition deployments can place an equivalent third-party gateway in front of the service to restrict egress.

Reports describing only this documented behaviour are normally declined as a known limitation. **This does not apply** if you can demonstrate impact beyond the documented design — for example, bypassing the gateway controls, or accessing data you are not authorised to reach. Please report such cases and we will triage them as security vulnerabilities.

## Safe Harbor

We will not pursue legal action against researchers who act in good faith, test only their own installations or data, avoid privacy violations and service disruption, and follow the coordinated disclosure process described above.
