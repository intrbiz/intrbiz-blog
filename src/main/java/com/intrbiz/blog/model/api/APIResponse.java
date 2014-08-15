package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class APIResponse
{
    public enum APIStatus {
        OK, ERROR
    };

    @JsonProperty("stat")
    private APIStatus stat;

    protected APIResponse(APIStatus stat)
    {
        this.stat = stat;
    }
    
    protected APIResponse()
    {
        this(APIStatus.OK);
    }

    public APIStatus getStat()
    {
        return stat;
    }

    public void setStat(APIStatus stat)
    {
        this.stat = stat;
    }
}
