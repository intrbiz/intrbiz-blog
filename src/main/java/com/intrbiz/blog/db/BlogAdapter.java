package com.intrbiz.blog.db;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.blog.model.CategoryInfo;
import com.intrbiz.blog.model.Menu;
import com.intrbiz.blog.model.PostInfo;
import com.intrbiz.data.DataAdapter;
import com.intrbiz.data.DataManager;

public abstract class BlogAdapter implements DataAdapter
{    
    public static BlogAdapter connect()
    {
        return DataManager.getInstance().dataAdapter(BlogAdapter.class);
    }
    
    // menus
    
    public abstract ConcurrentMap<String, Menu> getMenus();
    
    public abstract Menu getMenu(String name);
    
    // posts
    
    public abstract ConcurrentMap<String, PostInfo> getPosts();
    
    public abstract PostInfo getPost(String name);
    
    public abstract List<PostInfo> getAllPosts();
    
    public abstract List<PostInfo> getRecentPosts(int count);
    
    public abstract List<PostInfo> getPostsInCategory(String categoryName);
    
    // categories
    
    public abstract ConcurrentMap<String, CategoryInfo> getCategories();
    
    public abstract CategoryInfo getCategory(String name);
    
    public abstract List<CategoryInfo> getAllCategories();
    
    public abstract List<CategoryInfo> getCategoriesInCategory(String categoryName);
    
    //
    
    public abstract void clearCache();
}
