package com.intrbiz.blog.router;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.error.view.BalsaViewNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.blog.BlogApp;
import com.intrbiz.blog.db.BlogAdapter;
import com.intrbiz.blog.model.CategoryInfo;
import com.intrbiz.blog.model.PostInfo;
import com.intrbiz.blog.model.api.CachesCleared;
import com.intrbiz.blog.model.api.CategoryResponse;
import com.intrbiz.blog.model.api.ErrorResponse;
import com.intrbiz.blog.model.api.IndexResponse;
import com.intrbiz.blog.model.api.PageResponse;
import com.intrbiz.blog.model.api.PostResponse;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidAccessToken;

@Prefix("/api/")
public class APIRouter extends Router<BlogApp>
{
    private Logger logger = Logger.getLogger(APIRouter.class);
    
    @Any("/")
    @JSON()
    @RequireValidAccessToken(@Param("key"))
    @WithDataAdapter(BlogAdapter.class)
    public IndexResponse index(BlogAdapter data)
    {
        var("posts", data.getRecentPosts(10));
        return new IndexResponse("Welcome", encodeBuffered("theme/index"));
    }
    
    @Any("/category/**:category")
    @Order(10)
    @JSON()
    @RequireValidAccessToken(@Param("key"))
    @WithDataAdapter(BlogAdapter.class)
    public CategoryResponse category(BlogAdapter data, String name)
    {
        CategoryInfo category = var("category", data.getCategory(name));
        if (category == null) throw new BalsaNotFound("The category " + name + " could not be found");
        var("posts", category.getRecentPosts(10));
        return new CategoryResponse(category.getTitle(), encodeBuffered("theme/category", "categories/" + name));
    }
    
    @Any("/post/**:post")
    @Order(10)
    @JSON()
    @RequireValidAccessToken(@Param("key"))
    @WithDataAdapter(BlogAdapter.class)
    public PostResponse post(BlogAdapter data, String name)
    {
        PostInfo post = var("post", data.getPost(name));
        if (post == null) throw new BalsaNotFound("The post " + name + " could not be found");
        return new PostResponse(post.getTitle(), encodeBuffered("theme/post", "posts/" + name));
    }
    
    @Any("/**:page")
    @Order(100)
    @JSON()
    @RequireValidAccessToken(@Param("key"))
    public PageResponse page(String page)
    {
        return new PageResponse(Util.ucFirst(page), encodeBuffered("theme/page", "pages/" + page));
    }
    
    @Any("/**")
    @JSON()
    @Catch(BalsaViewNotFound.class)
    public ErrorResponse notFound()
    {
        return new ErrorResponse("Not Found", encodeBuffered("theme/notfound"));
    }
    
    @Any("/**")
    @JSON()
    @Catch()
    @Order(Order.LAST)
    public ErrorResponse error()
    {
        return new ErrorResponse("Error", encodeBuffered("theme/error"));
    }
    
    @Any("/clear-cache")
    @JSON()
    @WithDataAdapter(BlogAdapter.class)
    public CachesCleared clearCache(BlogAdapter data)
    {
        require("127.0.0.1".equals(request().getRemoteAddress()));
        logger.info("Clearing caches, requested by: " + request().getRemoteAddress());
        data.clearCache();
        return new CachesCleared();
    }
}
