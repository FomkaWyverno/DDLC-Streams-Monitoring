package ua.wyverno.logger.appenders;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

@Plugin(
        name = "QueueConsole",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class QueueConsoleAppender extends AbstractAppender {

    protected QueueConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }



    @PluginFactory
    public static QueueConsoleAppender createAppender(@PluginAttribute("name") String name,
                                                      @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                                      @PluginElement("Filter") Filter filter,
                                                      @PluginElement("Layout") Layout<? extends Serializable> layout) {
        return new QueueConsoleAppender(name,filter,layout,ignoreExceptions,null);
    }


    @Override
    public void append(LogEvent event) {
        if (this.getFilter() != null && this.getFilter().filter(event) == Filter.Result.DENY) {
            return;
        }
        QueueLoggerEvents.getInstance().addLog(this.getLayout().toSerializable(event).toString());
    }
}