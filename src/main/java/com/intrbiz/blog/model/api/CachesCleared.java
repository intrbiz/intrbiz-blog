package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("caches-cleared")
public class CachesCleared extends APIResponse
{
    public CachesCleared()
    {
        super();
    }
}
