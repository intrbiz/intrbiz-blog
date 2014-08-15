package com.intrbiz.blog.router;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.error.view.BalsaViewNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.blog.BlogApp;
import com.intrbiz.blog.db.BlogAdapter;
import com.intrbiz.blog.model.CategoryInfo;
import com.intrbiz.blog.model.PostInfo;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Status;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("theme/main")
public class BlogRouter extends Router<BlogApp>
{
    private Logger logger = Logger.getLogger(BlogRouter.class);
    
    @Any("/**")
    @Before()
    @WithDataAdapter(BlogAdapter.class)
    public void themeCommon(BlogAdapter data)
    {
        // setup common theme stuff
        model("main_menu", data.getMenu("main_menu"));
    }
    
    @Any("/")
    @WithDataAdapter(BlogAdapter.class)
    public void index(BlogAdapter data)
    {
        var("posts", data.getRecentPosts(10));
        encode("theme/index");
    }
    
    @Any("/category/**:category")
    @Order(10)
    @WithDataAdapter(BlogAdapter.class)
    public void category(BlogAdapter data, String name)
    {
        CategoryInfo category = var("category", data.getCategory(name));
        if (category == null) throw new BalsaNotFound("The category " + name + " could not be found");
        var("posts", category.getRecentPosts(10));
        encode("theme/category", "categories/" + name);
    }
    
    @Any("/post/**:post")
    @Order(10)
    @WithDataAdapter(BlogAdapter.class)
    public void post(BlogAdapter data, String name)
    {
        PostInfo post = var("post", data.getPost(name));
        if (post == null) throw new BalsaNotFound("The post " + name + " could not be found");
        encode("theme/post", "posts/" + name);
    }
    
    @Any("/**:page")
    @Order(100)
    public void page(String page)
    {
        encode("theme/page", "pages/" + page);
    }
    
    @Any("/**")
    @Catch(BalsaViewNotFound.class)
    @Catch(BalsaNotFound.class)
    @Status(HTTPStatus.NotFound)
    public void notFound()
    {
        encode("theme/notfound");
    }
    
    @Any("/**")
    @Catch()
    @Order(Order.LAST)
    @Status(HTTPStatus.InternalServerError)
    public void error()
    {
        logger.error("Internal server error whilst processing request", balsa().getException());
        encode("theme/error");
    }
}
