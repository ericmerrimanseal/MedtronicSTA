package com.seal.contracts.generator.ui.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by root on 17.08.15..
 */
public class UIBuildConfig {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String contractsFile;

    @Getter
    @Setter
    private String usersFile;

    @Getter
    @Setter
    private String folderAttachmentsRoot;

    @Getter
    @Setter
    private String folderAttachmentDupliatesRoot;

    @Getter
    @Setter
    private boolean includeAttachments;

    @Getter
    @Setter
    private boolean showWarnings;

    @Getter
    @Setter
    private String template;

    @Getter
    @Setter
    private String supplierProfileFile;

    @Getter
    @Setter
    private boolean validateIfSupplierExists;

    @Getter
    @Setter
    private String defaultOwner;

}
