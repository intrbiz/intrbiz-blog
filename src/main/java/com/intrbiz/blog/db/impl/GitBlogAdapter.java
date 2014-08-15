package com.intrbiz.blog.db.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult.Status;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.view.BalsaView;
import com.intrbiz.blog.BlogApp;
import com.intrbiz.blog.db.BlogAdapter;
import com.intrbiz.blog.model.CategoryInfo;
import com.intrbiz.blog.model.Menu;
import com.intrbiz.blog.model.PostInfo;
import com.intrbiz.data.DataException;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.DataManager.DataAdapterFactory;
import com.intrbiz.data.cache.Cache;

public class GitBlogAdapter extends BlogAdapter
{
    private Logger logger = Logger.getLogger(GitBlogAdapter.class);
    
    private final File base;
    
    private final BlogApp application;
    
    private final Cache cache;
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private Repository repo;
    
    private Git git;
    
    private Timer timer = new Timer();

    public GitBlogAdapter(File base, BlogApp application, Cache cache)
    {
        super();
        this.base = base;
        this.application = application;
        this.cache = cache;
        // load the repo
        try
        {
            this.repo = new FileRepositoryBuilder().setWorkTree(this.base).setGitDir(new File(this.base, ".git")).readEnvironment().build();
            this.git = Git.wrap(this.repo);
            logger.info("Using Git repo: " + this.repo.getDirectory().getAbsolutePath());
            // get the last commit
            for (RevCommit commit : this.git.log().add(this.repo.resolve(Constants.HEAD)).setMaxCount(1).call())
            {
                logger.info("Last content commit:\r\n" + commit.getFullMessage());
            }
            // setup polling
            if (this.repo.getRemoteNames().stream().anyMatch((r) -> {return "origin".equals(r);}))
            {
                logger.info("Setting up polling of origin remote: " + this.repo.getConfig().getString("remote", "origin", "url"));
                // initial rebase
                this.rebase();
                // setup polling timer
                this.timer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        GitBlogAdapter.this.rebase();
                    }
                    
                }, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(5));
            }
            else
            {
                logger.info("Not polling remote Git repository, no 'origin' remote configured.");
            }
        }
        catch (Exception e)
        {
            throw new DataException("Error updating content repository", e);
        }
    }

    @Override
    public String getName()
    {
        return "blog";
    }

    @Override
    public void close()
    {
    }

    @SuppressWarnings("unchecked")
    protected <T> T loadXMLFile(File file, Class<T> type)
    {
        if (file.exists())
        {
            try
            {
                JAXBContext jaxb = JAXBContext.newInstance(type);
                Unmarshaller u = jaxb.createUnmarshaller();
                return (T) u.unmarshal(file);
            }
            catch (JAXBException e)
            {
                throw new DataException(e);
            }
        }
        return null;
    }
    
    protected ConcurrentMap<String, Menu> scanMenus()
    {
        ConcurrentMap<String, Menu> menus = new ConcurrentHashMap<String, Menu>();
        for (File file : new File(this.base, "menus").listFiles())
        {
            Menu menu = this.loadXMLFile(file, Menu.class);
            String name = file.getName();
            menus.put(name.substring(0, name.lastIndexOf(".")), menu);
            logger.info("Found menu: " + name);
        }
        return menus;
    }
    
    protected ConcurrentMap<String, PostInfo> scanPosts()
    {
        ConcurrentMap<String, PostInfo> posts = new ConcurrentHashMap<String, PostInfo>();
        // scan
        File dir = new File(this.base, "posts");
        Stack<File> work = new Stack<File>();
        work.add(dir);
        while (! work.isEmpty())
        {
            File file = work.pop();
            logger.debug("Processing: " + file.getAbsolutePath());
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    work.add(child);
                }
            }
            else if (file.getName().endsWith(".md"))
            {
                String name = file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
                name = name.substring(0, name.lastIndexOf("."));
                // load the view
                BalsaView view = this.application.getViewEngine().load(null, new String[] { "posts/" + name }, BalsaContext.Balsa());
                // store the info
                if (view != null)
                {
                    try
                    {
                        PostInfo post = new PostInfo();
                        post.setName(name);
                        post.setAuthor(view.getMetadata().getAttribute("author"));
                        post.setDate( view.getMetadata().containsAttribute("date") ? DATE_FORMAT.parse(view.getMetadata().getAttribute("date")) : new Date());
                        post.setTitle(view.getTitle());
                        // category
                        String categoryName = view.getMetadata().getAttribute("category");
                        if (! Util.isEmpty(categoryName)) post.setCategoryName(categoryName);
                        logger.info("Found post: " + post.getName() + " - " + post.getTitle() + " - " + post.getDate() + " - " + post.getAuthor() + " - " + post.getCategoryName());
                        posts.put(post.getName(), post);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return posts;
    }
    
    protected ConcurrentMap<String, CategoryInfo> scanCategories()
    {
        ConcurrentMap<String, CategoryInfo> categories = new ConcurrentHashMap<String, CategoryInfo>();
        // scan
        File dir = new File(this.base, "categories");
        Stack<File> work = new Stack<File>();
        work.add(dir);
        while (! work.isEmpty())
        {
            File file = work.pop();
            logger.debug("Processing: " + file.getAbsolutePath());
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    work.add(child);
                }
            }
            else if (file.getName().endsWith(".md"))
            {
                String name = file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
                name = name.substring(0, name.lastIndexOf("."));
                // load the view
                BalsaView view = this.application.getViewEngine().load(null, new String[] { "categories/" + name }, BalsaContext.Balsa());
                // store the info
                if (view != null)
                {
                    try
                    {
                        CategoryInfo category = new CategoryInfo();
                        category.setName(name);
                        category.setTitle(view.getTitle());
                        // category
                        String categoryName = view.getMetadata().getAttribute("category");
                        if (! Util.isEmpty(categoryName)) category.setCategoryName(categoryName);
                        logger.info("Found category: " + category.getName() + " - " + category.getTitle() + " - " + category.getCategoryName());
                        categories.put(category.getName(), category);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return categories;
    }

    // menus
    
    @Override
    public ConcurrentMap<String, Menu> getMenus()
    {
        ConcurrentMap<String, Menu> menus = this.cache.get("menus");
        if (menus == null)
        {
            menus = this.scanMenus();
            this.cache.put("menus", menus);
        }
        return menus;
    }

    @Override
    public Menu getMenu(String name)
    {
        return this.getMenus().get(name);
    }
    
    // posts
    
    @Override
    public ConcurrentMap<String, PostInfo> getPosts()
    {
        ConcurrentMap<String, PostInfo> posts = this.cache.get("posts");
        if (posts == null)
        {
            posts = this.scanPosts();
            this.cache.put("posts", posts);
        }
        return posts;
    }
    
    @Override
    public PostInfo getPost(String name)
    {
        if (name == null) return null;
        return this.getPosts().get(name);
    }
    
    @Override
    public List<PostInfo> getAllPosts()
    {
        List<PostInfo> posts = new LinkedList<PostInfo>(this.getPosts().values());
        Collections.sort(posts);
        return posts;
    }
    
    @Override
    public List<PostInfo> getRecentPosts(int count)
    {
        List<PostInfo> posts = this.getAllPosts();
        return posts.subList(0, Math.min(count, posts.size()));
    }
    
    @Override
    public List<PostInfo> getPostsInCategory(String categoryName)
    {
        List<PostInfo> posts = new LinkedList<PostInfo>();
        for (PostInfo post : this.getPosts().values())
        {
            if (categoryName.equals(post.getCategoryName()))
                posts.add(post);
        }
        Collections.sort(posts);
        return posts;
    }
    
    // categories
    
    @Override
    public ConcurrentMap<String, CategoryInfo> getCategories()
    {
        ConcurrentMap<String, CategoryInfo> categories = this.cache.get("categories");
        if (categories == null)
        {
            categories = this.scanCategories();
            this.cache.put("categories", categories);
        }
        return categories;
    }
    
    @Override
    public CategoryInfo getCategory(String name)
    {
        if (name == null) return null;
        return this.getCategories().get(name);
    }
    
    @Override
    public List<CategoryInfo> getAllCategories()
    {
        List<CategoryInfo> categories = new LinkedList<CategoryInfo>(this.getCategories().values());
        Collections.sort(categories);
        return categories;
    }
    
    @Override
    public List<CategoryInfo> getCategoriesInCategory(String categoryName)
    {
        List<CategoryInfo> categories = new LinkedList<CategoryInfo>();
        for (CategoryInfo category : this.getCategories().values())
        {
            if (categoryName.equals(category.getCategoryName()))
                categories.add(category);
        }
        Collections.sort(categories);
        return categories;
    }
    
    public void clearCache()
    {
        this.cache.clear();
        this.application.getViewEngine().clearCache();
        System.gc();
        // rescan
        if (BalsaContext.Balsa() != null)
        {
            // only rescan if we have a context
            this.getMenus();
            this.getCategories();
            this.getPosts();
        }
    }
    
    public synchronized void rebase()
    {
        try
        {
            logger.info("Rebasing repo from origin");
            PullResult result = this.git.pull().setRemote("origin").setRemoteBranchName("master").setRebase(true).call();
            logger.info("Rebase complete: " + result.getRebaseResult().getStatus());
            if (result.getRebaseResult().getStatus().isSuccessful() && result.getRebaseResult().getStatus() != Status.UP_TO_DATE)
            {
                logger.info("Got content changes, rescanning content");
                // TODO: we can't rescan as we don't have a context
                this.cache.clear();
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to rebase Git repository", e);
        }
    }
    
    // factory
    
    public static DataAdapterFactory<BlogAdapter> factory(File base, BlogApp blogApp)
    {
        final GitBlogAdapter adapter = new GitBlogAdapter(base, blogApp, DataManager.get().cache("blog"));
        return new DataAdapterFactory<BlogAdapter>()
        {
            @Override
            public GitBlogAdapter create()
            {
                return adapter;
            }
        };
    }
}
