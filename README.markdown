# Superfeedr XMPP API Java Wrapper

## Thanks to

Thomas RICARD, July 2009

## Dependency :

[Smack 3.1](http://www.igniterealtime.org/downloads/index.jsp)
Smackx library
Java 5+

## Use

To use the Superfeedr java library, just add the file `./distrib/superfeedr.jar` to your classpath.

See the class `org.superfeedr.test.Test` to know how to use it.

## Building

You don't need that if you don't need to change the code. Please just use the built `.jar` file in `/distrib`

It you changed the code, you can rebuild the .jar file, by using `ant` in the wrapper directory or use `maven clean install` if you prefer to use Maven 2

## Example

    import java.net.URL;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.List;

    import org.jivesoftware.smack.XMPPConnection;
    import org.superfeedr.OnNotificationHandler;
    import org.superfeedr.Superfeedr;
    import org.superfeedr.OnResponseHandler;
    import org.superfeedr.extension.notification.ItemExtension;
    import org.superfeedr.extension.notification.SuperfeedrEventExtension;

    /*
    Small Sample/example that you can start to use to track Superfeedr.
    */
    class ExampleProgram {
        public static void main(final String[] args) throws Exception {
        
            List<URL> urls = new ArrayList<URL>();
            // List of urls you want to track
            urls.add(new URL("http://superfeedr.com/dummy.xml"));
            urls.add(new URL("http://blog.superfeedr.com/atom.xml"));
            urls.add(new URL("http://identi.ca/api/statuses/public_timeline.atom"));
        
            // Debugging will display a window that allows you to see incoming/outcoming traffic
            XMPPConnection.DEBUG_ENABLED = true;
            // Use your Superfeedr credentials
            Superfeedr feedr = new Superfeedr("julien", "inc0nueE", "superfeedr.com");
        
            // Adds notification Handler. The onNotification method will be called with a SuperfeedrEventExtension object.
            feedr.addOnNotificationHandler(new OnNotificationHandler() {
                public void onNotification(final SuperfeedrEventExtension event) {
                    // Displays the feed url, the HTTP status and the Log. (See Superfeedr Doc for more info)
                    System.err.println(event.getStatus().getFeedURL());
                    System.err.println(event.getStatus().getHttpExtension().getCode());
                    System.err.println(event.getStatus().getHttpExtension().getInfo());
                    // And now the items
                    if (event.getItems().getItemsCount() == 0) {
                        System.out.println("No items");
                    } else
                    // For each item
                    for (Iterator<ItemExtension> iterator = event.getItems().getItems(); iterator.hasNext();) {
                        ItemExtension item = iterator.next();
                        // Display it's title and link
                        System.out.println(item.getEntry().getTitle());
                        System.out.println(item.getEntry().getLink());
                    }
                    System.out.println("\n\n");
                }
            });
        
            // Actually perform the subscription, and calls the second argument when performed
            // You can obviously do 'unsubscribe' too.
            feedr.unsubscribe(urls, new OnResponseHandler() {
              public void onSuccess(Object response) {
                // TODO Auto-generated method stub
              }
              public void onError(String infos) {
                // TODO Auto-generated method stub
              }
            });
        
            while (true) {
                Thread.sleep(1000);
            }
        }
    }

## Warning

We know our limits and we know *we can’t actively support* wrappers in _every_ languages. Like everybody, we have our favorite languages and platform, and there is little chance that we ever get a deep enough knownledge in all that languages that you guys use to offer great services. So, for us, the *limit of what we can provide and support is [our API (both XMPP and PubSubHubbub)](http://superfeedr.com/documentation).*

However, we’re not blind either and we know how these parsers are important for everybody to get started and integrate Superfeedr into their existing apps, so we take great care of them and try to help people use them or fix problems. 

These libs are not ours, _they wait for your input_, _your documentation_, _your testing_, as well as_ your suggestions for new features_. We will just help gathering them and keeping track by connecting the people who made them with the people who use them.

**Please, fork it and make it better!**

