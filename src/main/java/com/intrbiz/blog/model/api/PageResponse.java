package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("page")
public class PageResponse extends ContentResponse
{
    public PageResponse()
    {
        super();
    }

    public PageResponse(String title, String content)
    {
        super(title, content);
    }

    public PageResponse(String title)
    {
        super(title);
    }

}
