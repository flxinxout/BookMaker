# BookMaker 
BookMaker is a fast Spigot API to create Custom Book for Minecraft 1.8 to 1.16.5.

## Features
- Create a book with your title, author and description
- Add custom pages with custom texts
- Customize your texts with hover and click events
- Give the book to a player or open it SERVER SIDE with a packet implementation 

## What do you have to care about ?
- Make sure you have a project on SDK 8 or greater

## How to use it
Just copy the `BookMaker.java` file from the source code and past it into your project.
<br>

All the parts of the book are created with the **Builder Design Pattern**. it means that you can create the custom book in many ways (in one bloc or in many for example). First, you have to create a new `BookBuilder`
```java
BookBuilder book = BookMaker.build();
```

A `BookBuilder` is an inner class that represents the construction of the custom book. With this you can set the title, the author, add pages, etc. All you have to remember is to finish the construction with the method `.done()` to return an `ItemStack` at the end. Here is an example
```java
BookMaker book = BookMaker.build().withTitle("§aWelcome").withAuthor("flxinxout").done();
```

Now let's write some text into the book. For this, you have to use the inner class `BookMaker.TextBook.java`. It provides a builder like the `BookBuilder` but for the texts specifically.
```java
TextBook text_one = TextBook.of("My First Text ! :)");
// or for more details
TextBook text_one = new TextBook().withText("Click here to go to my github page").withClick(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://google.com")).done();
```

Now the last part is the `BookMaker.PageBook.java` inner class. Like the text builder, this class is the page builder of the custom book. It provides many methods to customize your future pages with `BaseComponent`. `TextBook`, after the `done()` method, return a BaseComponent. To create a simple page we can do that
```java
BaseComponent[] page_one = PageBook.of("My first page (very simple with only one string)").done();
// or with more details
BaseComponent[] page_one = new PageBook().with(new TextBook().withText("Click here to go to my github page").withColor().done()).done();
```

Now here is a full custom book created with this API
```java
ItemStack it = BookMaker.build()
    .withTitle("Title")
    .withAuthor("Author")
    .withPage(new BookMaker.PageBook()
          .with(new BookMaker.TextBook()
                  .withText("Welcome !")
                  .withColor(ChatColor.AQUA)
                  .done())
           .with(BookMaker.TextBook.of("\n\n"))
           .with(new BookMaker.TextBook()
                  .withText("The google page below\n")
                  .withColor(ChatColor.BLUE)
                  .done())
           .with(new BookMaker.TextBook()
                  .withText("LINK")
                  .withClick(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://google.com"))
                   .done())
            .done())
     .withPage(BookMaker.PageBook.of("another page...")).done();
```

If you want to open it server side without giving the itemStack
```java
BookMaker.openBook(player, it);
```





































