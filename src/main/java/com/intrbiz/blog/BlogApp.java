package com.intrbiz.blog;

import java.io.File;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaMarkdown;
import com.intrbiz.blog.db.BlogAdapter;
import com.intrbiz.blog.db.impl.GitBlogAdapter;
import com.intrbiz.blog.express.IsInCategory;
import com.intrbiz.blog.router.APIRouter;
import com.intrbiz.blog.router.BlogRouter;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.memory.local.LocalMemoryCacheProvider;

public class BlogApp extends BalsaApplication
{
    @Override
    protected void setup()
    {
        // the directory holding the blog content
        File contentDir = new File(System.getProperty("blog.content", "/data/cellis/Intrbiz/blog"));
        // setup the content dir as a view path
        viewPath(contentDir);
        // enable markdown support
        BalsaMarkdown.enable(this);
        // setup the data store
        DataManager.get().registerDefaultCacheProvider(new LocalMemoryCacheProvider());
        DataManager.get().registerDataAdapter(BlogAdapter.class, GitBlogAdapter.factory(contentDir, this));
        // functions
        function("is_in_category", IsInCategory.class);
        // Setup the application routers
        router(new BlogRouter());
        router(new APIRouter());
    }
    
    public static void main(String[] args) throws Exception
    {
        BlogApp blogApp = new BlogApp();
        blogApp.start();
    }
}
