## Changelog

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
