package com.seal.contracts.generator.html;

import lombok.Getter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by Juraj on 01.08.2017.
 */
public class JsonModel {

    @Getter
    private final String title;
    private final String body;

    public JsonModel(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getBody() throws JSONException {
        return new JSONObject(body).toString(4);
    }

}
