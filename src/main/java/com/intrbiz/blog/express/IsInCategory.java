package com.intrbiz.blog.express;

import static com.intrbiz.balsa.BalsaContext.*;

import com.intrbiz.blog.model.CategoryInfo;
import com.intrbiz.blog.model.PostInfo;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressException;
import com.intrbiz.express.operator.Function;

public class IsInCategory extends Function
{
    public IsInCategory()
    {
        super("is_in_category");
    }

    @Override
    public Object get(ExpressContext context, Object source) throws ExpressException
    {
        String wantedCategory = String.valueOf(this.getParameter(0).get(context, source));
        PostInfo post = Balsa().var("post");
        if (post != null) return post.isInCategory(wantedCategory);
        CategoryInfo category = Balsa().var("category");
        if (category != null) return category.isInCategory(wantedCategory);
        return false;
    }
}
