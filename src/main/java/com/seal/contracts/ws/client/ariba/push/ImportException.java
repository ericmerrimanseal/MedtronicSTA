package com.seal.contracts.ws.client.ariba.push;

/**
 * Created by jantonak on 2.11.2015.
 */
public class ImportException extends Exception {

    public ImportException(Throwable cause) {
        super(cause);
    }
    
    public ImportException(String s) {
        super(s);
    }
}
