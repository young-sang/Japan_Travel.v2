package com.japantravel._repo.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConflictException extends RuntimeException{

    private final String code;
    private final Map<String, Object> extra;

    public ConflictException(String message){
        this(null, message, null);
    }

    public ConflictException(String code, String message, Map<String, Object> extra){
        super(message);
        this.code = code;
        this.extra = extra;
    }

    public Map body(){
        Map<String, Object> a = new LinkedHashMap<String, Object>();
        if(code != null)
            a.put("code", code);
        a.put("message", getMessage());
        if(extra != null)
            a.putAll(extra);

        return a;
    }
}
