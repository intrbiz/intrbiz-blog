package com.intrbiz.blog.model;

import java.util.Date;

import com.intrbiz.blog.db.BlogAdapter;

public class PostInfo implements Comparable<PostInfo>
{
    private String name;

    private Date date;

    private String author;

    private String title;

    private String categoryName;

    public PostInfo()
    {
        super();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public CategoryInfo getCategory()
    {
        try (BlogAdapter blog = BlogAdapter.connect())
        {
            return blog.getCategory(this.getCategoryName());
        }
    }

    public boolean isInCategory(String categoryName)
    {
        CategoryInfo category = this.getCategory();
        return category == null ? false : category.isInCategory(categoryName);
    }

    @Override
    public int compareTo(PostInfo o)
    {
        int date = o.date.compareTo(this.date);
        if (date != 0) return date;
        return this.name.compareTo(o.name);
    }
}
