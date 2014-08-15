package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ContentResponse extends APIResponse
{
    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    public ContentResponse()
    {
        super();
    }

    public ContentResponse(String title)
    {
        super();
        this.title = title;
    }

    public ContentResponse(String title, String content)
    {
        super();
        this.title = title;
        this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
