package com.intrbiz.blog.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.blog.db.BlogAdapter;

public class CategoryInfo implements Comparable<CategoryInfo>
{
    private String name;

    private String title;

    private String categoryName;

    public CategoryInfo()
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
        if (this.name.equals(categoryName)) return true;
        CategoryInfo category = this.getCategory();
        return category == null ? false : category.isInCategory(categoryName);
    }

    public List<PostInfo> getPosts()
    {
        try (BlogAdapter blog = BlogAdapter.connect())
        {
            return blog.getPostsInCategory(this.getName());
        }
    }

    public List<PostInfo> getAllPosts()
    {
        List<PostInfo> posts = new LinkedList<PostInfo>();
        posts.addAll(this.getPosts());
        for (CategoryInfo child : this.getAllCategories())
        {
            posts.addAll(child.getAllPosts());
        }
        Collections.sort(posts);
        return posts;
    }

    public List<PostInfo> getRecentPosts(int count)
    {
        List<PostInfo> posts = this.getAllPosts();
        return posts.subList(0, Math.min(count, posts.size()));
    }

    public List<CategoryInfo> getAllCategories()
    {
        try (BlogAdapter blog = BlogAdapter.connect())
        {
            return blog.getCategoriesInCategory(this.getName());
        }
    }

    public List<CategoryInfo> getCategories(int count)
    {
        List<CategoryInfo> categories = this.getAllCategories();
        return categories.subList(0, Math.min(count, categories.size()));
    }

    @Override
    public int compareTo(CategoryInfo o)
    {
        return this.name.compareTo(o.name);
    }
}
