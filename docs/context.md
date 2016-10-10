This documentation describes a simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's [on-premises API feature](https://structurizr.com/help/on-premises-api). The on-premises API feature provides a way to store workspace data behind your corporate firewall, or using servers anywhere on the Internet, by hosting a local version of the Structurizr API.

![](embed:Context)

This implementation supports the two basic operations required to ```GET``` and ```PUT``` workspaces, with workspace definitions being stored on the file system.