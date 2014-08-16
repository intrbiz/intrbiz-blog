/*
 * Intrbiz blog smart menu
 */

/* Setup smart menu which fetches content via the JSON API */
$(document).ready(function() 
{
    $(window).on('hashchange', function()
    {
        var hash = location.hash;
        console.log("Hash: " + hash);
        if (hash.match(/^#!.+/))
        {
            var uri = hash.substring(2);
            console.log("URI: " + uri);
            /* Set the tab */
            $("#nav ul li a").each(function(i, el)
            {
                if ($(el).attr('data-link') == uri)
                {
                    var id = "#" + $(el).attr("data-tab");
                    $("#sub-nav > div").removeClass("current");
                    $("#nav ul li a").removeClass("current");
                    $(id).addClass("current");
                    $(el).addClass("current");
                }
            });
            /* Load content */
            $.getJSON('/api' + uri + '?key=' + window.api_key, function(data)
            {
                if (data.stat == 'OK')
                {
                    $("#content").html(data.content);
                    $(document).attr('title', data.title + ' - Intrbiz Blog');
                    /* Highlight code blocks */
                    $('pre code').each(function(i, block)
                    {
                        hljs.highlightBlock(block);
                    });
                }
            });
        }
    });
    /* Link event handlers */
    $("#nav ul li a").click(function(ev) 
    {
        var uri = $(this).attr('data-link');
        if (uri)
        {
            location.hash = "!" + uri;
            ev.preventDefault();
        }
    });
    /* Highlight code blocks */
    $('pre code').each(function(i, block)
    {
        hljs.highlightBlock(block);
    });
}); 
