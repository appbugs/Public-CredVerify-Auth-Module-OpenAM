# VeriClouds CredVerify Authentication Module

A leaked credential detection authentication module by VeriClouds for ForgeRock Access Management 5.5. VeriClouds CredVerify authentication module checks leaked credential through VeriClouds CredVerify Cloud API. If a user login ID and credential combination is found to match to a login ID and credential discovered in previous data breaches, the module will fail the authentication otherwise it will pass the authentication.

It is important to note that the Cloud API call will only pass user login name such as user name or email address to CredVerify Cloud API but not the user's password. CredVerify Cloud API will returned a list of masked user passwords as a result of querying by using login name and the matching to the leaked passwords will be happen in the authentication module which is deployed to a adopter's local environment.

## Installation
Copy the .jar file from the ../target directory into the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed. Restart the web container to pick up the new auth module. The auth module will then be available when navigate to Authentication -> Modules -> + Add Module. Select "VeriClouds CredVerify" in the New Module page.

## Usage
After the module is added, click the "CredVerify" under Module Name column and fill the info for the module configuration

### Configure the CredVerify Authentication Module
**Authentication Level:** 1

**VeriClouds API URL:** https://api.vericlouds.com/index.php

**VeriClouds App Key:** (contact VeriClouds.com to get a trial or proudction key)

**VeriClouds App Secret:** (contact VeriClouds.com to get a trial or proudction secret)

**Check Policy:** Leaked Credential Check Policy. This attribute controls how the leaked credential database is searched for matching user ID and password.

* Enterprise: Try to match to all leaked passwords without matching user ID. This provides the maximum security for enterprises who most often use user name instead of email as user ID.
* Consumer: The combination of user ID and password are searched in leaked credential database.

**User ID Type:** The User ID Type. This setting is only effective when Check Policy is "Consumer"

* (Not Used): Select this option when Check Policy is "Enterprise"
Auto Detect: Auto detect the user ID type based on the user input
* User Name: User name such as "james"
* Email: Email like user ID such as "james@gmail.com"
* Email Hash: Email ID is hashed to protect user privacy
* Phone Number: Phone number like ID such as "2061112222"

### Add CredVerify Authentication Module a Authentication Chain
After the auth module is properly configured, to add it to an authentication chain navigate to "Authentication -> Chains". Create a new auth chain to add the CredVerify auth module to an existing chain. For example, select "ldapService" chain and click "Add Module" to add "CredVerify" to the module after "DataStore". Set the CredVerify auth module to be "required".


## To Build
The code in this repository has binary dependencies that live in the ForgeRock maven repository. Maven can be configured to authenticate to this repository by following the following ForgeRock Knowledge Base Article. To rebuild, run "mvn clean install" in the directory containing the pom.xml

## Disclaimer
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

ForgeRock and VeriClouds do not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. ForgeRock and VeriClouds disclaim all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock and VeriClouds shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.
