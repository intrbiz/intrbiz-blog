package com.intrbiz.blog.model;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.blog.db.BlogAdapter;


@XmlRootElement(name = "menu")
@XmlType(name = "menu")
public class Menu
{
    private String id = UUID.randomUUID().toString();
    
    private String title;

    private String content = "";
    
    private String linkType;
    
    private String link;
    
    // sub menu
    private List<Menu> entries = new LinkedList<Menu>();
    
    public Menu()
    {
        super();
    }
    
    @XmlAttribute(name = "id")
    public String getId()
    {
        return this.id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }

    @XmlAttribute(name = "title")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
    
    @XmlElement(name = "content")
    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @XmlAttribute(name = "link-type")
    public String getLinkType()
    {
        return linkType;
    }

    public void setLinkType(String linkType)
    {
        this.linkType = linkType;
    }

    @XmlAttribute(name = "link")
    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    @XmlElementRef(type = Menu.class)
    public List<Menu> getEntries()
    {
        return entries;
    }

    public void setEntries(List<Menu> entries)
    {
        this.entries = entries;
    }
    
    public CategoryInfo getCategory()
    {
        if (this.getLinkType() != null && this.getLinkType().startsWith("category"))
        {
            try (BlogAdapter adapter = BlogAdapter.connect())
            {
                return adapter.getCategory(this.getLink());
            }
        }
        return null;
    }
    
    public PostInfo getPost()
    {
        if (this.getLinkType() != null && this.getLinkType().startsWith("post"))
        {
            try (BlogAdapter adapter = BlogAdapter.connect())
            {
                return adapter.getPost(this.getLink());
            }
        }
        return null;
    }
    
    public String toString()
    {
        try
        {
            JAXBContext c = JAXBContext.newInstance(Menu.class);
            StringWriter sw = new StringWriter();
            Marshaller m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            m.marshal(this, sw);
            return sw.toString();
        }
        catch (Exception e)
        {
        }
        return super.toString();
    }
}
