package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("post")
public class PostResponse extends ContentResponse
{
    public PostResponse()
    {
        super();
    }

    public PostResponse(String title, String content)
    {
        super(title, content);
    }

    public PostResponse(String title)
    {
        super(title);
    }

}
