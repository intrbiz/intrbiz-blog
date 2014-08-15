package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("error")
public class ErrorResponse extends ContentResponse
{
    public ErrorResponse()
    {
        super();
        this.setStat(APIStatus.ERROR);
    }

    public ErrorResponse(String title, String content)
    {
        super(title, content);
        this.setStat(APIStatus.ERROR);
    }

    public ErrorResponse(String title)
    {
        super(title);
        this.setStat(APIStatus.ERROR);
    }

}
