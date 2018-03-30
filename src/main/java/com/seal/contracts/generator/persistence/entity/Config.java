package com.seal.contracts.generator.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by root on 27.10.15..
 */
@Entity
@Table(name = "CONFIG")
public class Config {

    public enum ACTION {VALIDATE, GENERATE}

    @Id
    @Getter
    private String id = "0";

    @Getter
    @Setter
    @Column(name = "INFILE", nullable = false)
    private String inFile;

    @Getter
    @Setter
    @Column(name = "ARIBAURL", nullable = false)
    private String aribaURL;

    @Getter
    @Setter
    @Column(name = "ARIBAUSER", nullable = false)
    private String aribaUser;

    @Getter
    @Setter
    @Column(name = "ARIBAPWD", nullable = false)
    private String aribaPwd;

    @Getter
    @Setter
    @Column(name = "USERSFILE", nullable = false)
    private String usersFile;

    @Getter
    @Setter
    @Column(name = "SUPPLIERSFILE", nullable = false)
    private String supplierProfileFile;

    @Getter
    @Setter
    @Column(name = "ARIBATEMPLATE", nullable = false)
    private String aribaTemplate;

    @Getter
    @Setter
    @Column(name = "DEFAULTOWNER", nullable = false)
    private String defaultOwner;

    @Getter
    @Setter
    @Column(name = "ENUMSFILE", nullable = false)
    private String enumsFile;

    @Getter
    @Setter
    @Column(name = "COMMODITYFILE", nullable = false)
    private String commodityCodesFile;

    /**
     * Defines the root folder in the CW the attachments will be stored in
     */
    //TODO To be removed - not really used
    @Getter
    @Setter
    @Column(name = "ROOTFOLDER", nullable = false)
    private String rootFolder;

    @Getter
    @Setter
    @Column(nullable = false)
    private String sealURL;

    @Getter
    @Setter
    @Column(nullable = false)
    private String sealUser;

    @Getter
    @Setter
    @Column(nullable = false)
    private String sealPassword;

    @Getter
    @Setter
    @Column(nullable = false)
    private String defaultOwnerPasswordAdapter = "PasswordAdapter1";

    @Getter
    @Setter
    @Column(nullable = false)
    private boolean useDefaultOwner = false;

    public Config() {
    }

    public Config(String inFile,
                  String aribaURL,
                  String aribaUser,
                  String aribaPwd,
                  String usersFile,
                  String supplierProfileFile,
                  String defaultOwner,
                  String defaultOwnerPasswordAdapter,
                  boolean useDefaultOwner,
                  String aribaTemplate,
                  String enumsFile,
                  String commodityCodesFile,
                  String rootFolder,
                  String sealURL,
                  String sealUser,
                  String sealPassword
    ) {
        this.inFile = inFile;
        this.aribaURL = aribaURL;
        this.aribaUser = aribaUser;
        this.aribaPwd = aribaPwd;
        this.usersFile = usersFile;
        this.supplierProfileFile = supplierProfileFile;
        this.aribaTemplate = aribaTemplate;
        this.defaultOwner = defaultOwner;
        this.enumsFile = enumsFile;
        this.commodityCodesFile = commodityCodesFile;
        this.rootFolder = rootFolder;
        this.sealURL = sealURL;
        this.sealUser = sealUser;
        this.sealPassword = sealPassword;
        this.defaultOwnerPasswordAdapter = defaultOwnerPasswordAdapter;
        this.useDefaultOwner = useDefaultOwner;
    }
}

