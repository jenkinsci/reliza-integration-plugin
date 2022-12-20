## Changelog

### Version 0.2.0 (December 20, 2022)

-   Bump Reliza Java Client dependency to 0.2.4 (Supports API created type response)
-   Bump commons-io to 2.11.0 (Supports API created type response)

### Version 0.1.18 (May 17, 2022)

-   Bump Reliza Java Client dependency to 0.1.106 (Support extended commit list format with author and email)

### Version 0.1.17 (Jan 01, 2022)

-   Bump Reliza Java Client dependency to 0.1.103 (Fixes version retrieval in some scenarios)

### Version 0.1.16 (Dec 30, 2021)

-   Fix setting of build end time on Reliza Hub when BUILD_END_TIME environment variable is not set explicitly

### Version 0.1.15 (Dec 18, 2021)

-   Bump Reliza Java Client dependency to 0.1.99 (Includes log4j 2.17.0  - fixes subsequent CVEs against log4j)

### Version 0.1.14 (Dec 12, 2021)

-   Bump Reliza Java Client dependency to 0.1.77 (Includes log4j 2.15.0  - fixes CVE-2021-44228)
-   Bump Jenkins parent plugin dependency to 4.31

### Version 0.1.13 (Aug 23, 2021)

-   Use original commit if commit list is empty

### Version 0.1.12 (Jul 22, 2021)

-   withReliza can now read commit list
-   getVersion parameter allows withReliza to disregard getting version from Reliza Hub and just to set latest commit
-   useCommitList parameter allows addRelizaRelease to prioritize commit information from commit list over git commit, commit message, and commit time

### Version 0.1.11 (Jul 20, 2021)

-   Add envSuffix parameter to allow for multiple Reliza calls by differentiating parameters

### Version 0.1.10 (Jul 16, 2021)

-   Add an option to not request a version and submit a custom version from the build script
-   Artifact will not be created if SHA_256 is not supplied

### Version 0.1.9 (Jul 15, 2021)

-   Commit message can be passed to withReliza call for intelligent version bumping

### Version 0.1.8 (Jul 5, 2021)

-   Fixed bug with dependency not working with jdk 8 or lower

### Version 0.1.7 (Jun 25, 2021)

-   Update dependencies

### Version 0.1.6 (Apr 29, 2021)

-   Supports parameters commit message and commit list

### Version 0.1.5 (Apr 6, 2021)
-   Parameters jenkinsVersionMeta, customVersionMeta, and customVersionModifier added to withReliza wrapper
-   Status can be passed directly to addRelizaRelease method

### Version 0.1.4 (Mar 31, 2021)
-   Errors in Jenkinsfile example
-   Expose docker tag safe version as env variable

### Version 0.1.3 (Mar 8, 2021)

-   Pushes build URI to Reliza Hub

### Version 0.1.2 (Feb 23, 2021)

-   Added onlyVersion flag for getVersion call
-   Added plugin description

### Version 0.1.1 (Feb 17, 2021)

-   Modified plugin display 

### Version 0.1.0 (Feb 12, 2021)

-   Initial Release
