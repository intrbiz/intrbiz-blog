package com.intrbiz.blog.model.api;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("category")
public class CategoryResponse extends ContentResponse
{
    public CategoryResponse()
    {
        super();
    }

    public CategoryResponse(String title, String content)
    {
        super(title, content);
    }

    public CategoryResponse(String title)
    {
        super(title);
    }

}
