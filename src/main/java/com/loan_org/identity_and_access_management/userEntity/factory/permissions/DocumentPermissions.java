package com.loan_org.identity_and_access_management.userEntity.factory.permissions;

public class DocumentPermissions {

    public static final String DOCUMENT_UPLOAD = "document:upload";
    public static final String DOCUMENT_DOWNLOAD = "document:download";
    public static final String DOCUMENT_DELETE = "document:delete";
    public static final String DOCUMENT_UPDATE = "document:update";

    private DocumentPermissions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
