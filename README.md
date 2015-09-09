Ooyala AdobeCQ Connector
=========================

###Compatibility
_*Adobe CQ 5.6*_

###Installation

####Pre-Built Package
- Download the latest compatible release from GitHub by clicking the [**releases**](https://github.com/coresecure/Ooyala-AdobeCQ) link above.
    - ooyala-deploy-xxx.zip
  
- Install the zip file into CQs package manager.
    - http://localhost:4502/crx/packmgr/index.jsp
	
####Building with Maven
- Clone the repository
- `cd Ooyala-AdobeCQ`
- `mvn install`
- A complete package can be found under *deploy/target/content-packages/*

####Installing with Maven
- `mvn clean install -Pdeploy,local-author  -Dcq.host=localhost -Dcq.port=4502`


###Using with a Proxy
If you are running a proxy, you must restart your instance by specifying the configuration parameters:
* *proxyHost*
* *nonProxyHosts*
* *proxyPort*

`java -Dhttp.proxyHost=proxy.example.com -Dhttp.nonProxyHosts=*.foo.com|localhost -Dhttp.proxyPort=3128`


###Ooyala Configuration and Usage Documentation
Additional documentation can be found at *documentation/Ooyala-AdobeCQ-Documentation.pdf* which covers:
* Ooyala API Configuration.
* Finding videos in the Content Finder.
* Uploading videos from CQ5 to Ooyala.


##### Built by [Siteworx](http://www.siteworx.com) in NYC.
##### Updated by [Coresecure](https://www.coresecure.com) in Cambridge, Massachusetts
