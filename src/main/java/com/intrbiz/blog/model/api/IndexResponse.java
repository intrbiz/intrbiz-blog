package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("index")
public class IndexResponse extends ContentResponse
{
    public IndexResponse()
    {
        super();
    }

    public IndexResponse(String title, String content)
    {
        super(title, content);
    }

    public IndexResponse(String title)
    {
        super(title);
    }

}
